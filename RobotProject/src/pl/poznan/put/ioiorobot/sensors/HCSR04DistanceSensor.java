package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.util.Log;

public class HCSR04DistanceSensor extends Thread implements IDistanceSensor {
	private static final int SERVO_MIN = 600;
	private static final int SERVO_MAX = 2300;

	private static final int ANGLE_MIN = -90;
	private static final int ANGLE_MAX = 90;
	private static final int ANGLE_STEP = 10;
	
	private static final int STEP_DELAY = 40;

	private static final int RESULTS_SIZE = (ANGLE_MAX - ANGLE_MIN) / ANGLE_STEP;

	private PwmOutput servo;
	private DigitalOutput trigger;
	private PulseInput echo;

	private List<Pair> results;

	public HCSR04DistanceSensor(IOIO ioio_, int servoPin, int triggerPin, int echoPin) throws ConnectionLostException {
		servo = ioio_.openPwmOutput(servoPin, 100);
		trigger = ioio_.openDigitalOutput(triggerPin, false);
		echo = ioio_.openPulseInput(echoPin, PulseMode.POSITIVE);
		results = new ArrayList<IDistanceSensor.Pair>(RESULTS_SIZE);
		for (int i = 0; i < RESULTS_SIZE; i++) {
			results.add(new Pair(0, 0));
		}
		start();
	}

	@Override
	public List<Pair> getResults() {
		return results;
	}

	@Override
	public void run() {
		try {
			int position = ANGLE_MIN;
			servo.setPulseWidth(map(position));
			Thread.sleep(1000);
			while (true) {
				for (int i = 0; i < RESULTS_SIZE; i++) {
					servo.setPulseWidth(map(position));
					Thread.sleep(STEP_DELAY);
					results.set(i, new Pair(position,getDistance()));
					position += ANGLE_STEP;
					Log.d("robot", i +"+, "+ position + ", map: "+map(position));
				}

				for (int i = RESULTS_SIZE - 1; i >= 0; i--) {
					servo.setPulseWidth(map(position));
					Thread.sleep(STEP_DELAY);
					results.set(i, new Pair(position,getDistance()));
					position -= ANGLE_STEP;
					Log.d("robot", i +"-, "+ position + ", map: "+map(position));
				}
			}
		} catch (Exception e) {
			Log.e("robot",e.toString());
		}
	}

	private int getDistance() throws ConnectionLostException, InterruptedException {
		TimeUnit.MICROSECONDS.sleep(2);
		trigger.write(true);
		TimeUnit.MICROSECONDS.sleep(10);
		trigger.write(false);
		int echoSeconds = (int) (echo.getDuration() * 1000 * 1000);
		return (int) echoSeconds / 29 / 2;
	}

	/*
	 * 
	 */
	private long map(long x) {
		return (x - ANGLE_MIN) * (SERVO_MAX - SERVO_MIN) / (ANGLE_MAX - ANGLE_MIN) + SERVO_MIN;
	}
}
