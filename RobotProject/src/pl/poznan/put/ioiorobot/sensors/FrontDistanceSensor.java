package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.Arrays;

import pl.poznan.put.ioiorobot.utils.C;
import android.util.Log;

public class FrontDistanceSensor implements Runnable {
	private IDistanceSensor leftSensor;
	private IDistanceSensor centerSensor;
	private IDistanceSensor rigntSensor;
	private int freeDistance;
	
	private final static int BUFFOR_SIZE = 9;
	private int[] leftTab = new int[BUFFOR_SIZE];
	private int[] centerTab = new int[BUFFOR_SIZE];
	private int[] rightTab = new int[BUFFOR_SIZE];
	
	private int tabPos = 0;

	public FrontDistanceSensor(IOIO ioio_, int lTriggerPin, int lEchoPin, int cTriggerPin, int cEchoPin,
			int rTriggerPin, int rEchoPin, int freeDistance) throws ConnectionLostException {
		leftSensor = new HCSR04DistanceSensor(ioio_, -1, lTriggerPin, lEchoPin);
		centerSensor = new HCSR04DistanceSensor(ioio_, -1, cTriggerPin, cEchoPin);
		rigntSensor = new HCSR04DistanceSensor(ioio_, -1, rTriggerPin, rEchoPin);
		this.freeDistance = freeDistance;
		

		Thread t = new Thread(this);
		t.start();
	}

	public boolean isFreeLeft() throws ConnectionLostException, InterruptedException {
		double distance =  median(leftTab);
		Log.d(C.TAG, distance+"");
		return median(leftTab) > freeDistance;
	}

	public boolean isFreeCenter() throws ConnectionLostException, InterruptedException {
		return median(centerTab) > freeDistance;
	}

	public boolean isFreeRight() throws ConnectionLostException, InterruptedException {
		return median(rightTab) > freeDistance;
	}
	
	private int median(int[] tab) {
		int[] copy = Arrays.copyOf(tab, BUFFOR_SIZE);
		//Arrays.sort(copy);
		return copy[BUFFOR_SIZE/2];
	}

	@Override
	public void run() {
		while(true) {
			try {
				leftTab[tabPos] = leftSensor.getDistance();
				centerTab[tabPos] = centerSensor.getDistance();
				rightTab[tabPos] = rigntSensor.getDistance();
				tabPos = (tabPos+1) % BUFFOR_SIZE;
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
	}

}
