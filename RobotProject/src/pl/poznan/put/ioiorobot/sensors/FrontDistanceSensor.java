package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.Arrays;

import pl.poznan.put.ioiorobot.utils.Config;
import android.drm.DrmStore.RightsStatus;
import android.util.Log;

public class FrontDistanceSensor implements Runnable {
	private IDistanceSensor leftSensor;
	private IDistanceSensor centerSensor;
	private IDistanceSensor rightSensor;
	private int freeDistance;

	private final static int BUFFOR_SIZE = 1;
	private int[] leftTab = new int[BUFFOR_SIZE];
	private int[] centerTab = new int[BUFFOR_SIZE];
	private int[] rightTab = new int[BUFFOR_SIZE];

	private int tabPos = 0;
	private static Thread t;
	private boolean killed;

	public FrontDistanceSensor(IOIO ioio_, int lTriggerPin, int lEchoPin, int cTriggerPin, int cEchoPin,
			int rTriggerPin, int rEchoPin, int freeDistance) throws ConnectionLostException {
		leftSensor = new HCSR04DistanceSensor(ioio_, -1, lTriggerPin, lEchoPin);
		centerSensor = new HCSR04DistanceSensor(ioio_, -1, cTriggerPin, cEchoPin);
		rightSensor = new HCSR04DistanceSensor(ioio_, -1, rTriggerPin, rEchoPin);
		this.freeDistance = freeDistance;

		if (t == null || t.isAlive()) {
			t = new Thread(this);
			t.start();
		}

		killed = false;
	}

	public void kill() {
		t.interrupt();
		killed = true;
	}

	public int getLeft() {
		return median(leftTab);
	}
	
	public int getCenter() {
		return median(centerTab);
	}
	
	public int getRight() {
		return median(rightTab);
	}
	
	public boolean isFreeLeft() throws ConnectionLostException, InterruptedException {
		return median(leftTab) > freeDistance;
	}

	public boolean isFreeCenter() throws ConnectionLostException, InterruptedException {
		return median(centerTab) > freeDistance/2;
	}

	public boolean isFreeRight() throws ConnectionLostException, InterruptedException {
		return median(rightTab) > freeDistance;
	}

	private int median(int[] tab) {
		int[] copy = Arrays.copyOf(tab, BUFFOR_SIZE);
		Arrays.sort(copy);
		return copy[BUFFOR_SIZE / 2];
	}

	@Override
	public void run() {
		try {
			leftSensor.getDistanceInit();
			centerSensor.getDistanceInit();
			rightSensor.getDistanceInit();
			while (!killed) {
				leftTab[tabPos] = leftSensor.getDistance();
				centerTab[tabPos] = centerSensor.getDistance();
				rightTab[tabPos] = rightSensor.getDistance();
				tabPos = (tabPos + 1) % BUFFOR_SIZE;
				Thread.sleep(10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
