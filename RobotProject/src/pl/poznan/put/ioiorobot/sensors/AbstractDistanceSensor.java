package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.ioiorobot.utils.C;
import android.util.Log;

/**
 * Bazowa klasa sensora odległości, zawiera obsługę serwa obracającego faktyczny czujnik
 */
public abstract class AbstractDistanceSensor implements IDistanceSensor, Runnable {

	protected PwmOutput servo;
	private boolean isRunning = false;
	private List<AngleDistancePair> results;

	private DistanceResultListener listener;

	public void setDistanceResultListener(DistanceResultListener listener) {
		this.listener = listener;
	}

	public AbstractDistanceSensor(IOIO ioio_, int servoPin) throws ConnectionLostException {
		if (servoPin >= 0) {
			servo = ioio_.openPwmOutput(servoPin, 100);
		}
		results = new ArrayList<IDistanceSensor.AngleDistancePair>(RESULTS_SIZE);
		for (int i = 0; i < RESULTS_SIZE; i++) {
			results.add(new AngleDistancePair(0, 0));
		}
	}

	@Override
	public List<AngleDistancePair> getResults() {
		return results;
	}

	@Override
	public List<Integer> getResultsOnly() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (IDistanceSensor.AngleDistancePair pair : results) {
			result.add(pair.distance);
		}
		return result;
	}

	@Override
	public void stopSensor() {
		isRunning = false;
	}

	abstract public int getDistance() throws ConnectionLostException, InterruptedException;

	@Override
	public void startSensor() {
		isRunning = true;
	}
	
	@Override
	public void run() {
		Log.d(C.TAG, "distance sensor run");
		try {
			int position = ANGLE_MIN;
			servo.setPulseWidth(map(position));
			Thread.sleep(1000);
			while (true) {
				if (isRunning) {
					for (int i = 0; i < RESULTS_SIZE; i++) {
						servo.setPulseWidth(map(position));
						Thread.sleep(STEP_DELAY);
						results.set(i, new AngleDistancePair(position, getDistance()));
						// Log.d(C.TAG, i + "+, " + position + ", map: " +
						// map(position) + " | "+results.get(i).distance);
						if (i != RESULTS_SIZE - 1) {
							position += ANGLE_STEP;
						}
						if (listener != null) {
							listener.onResult(getResultsOnly(), results.get(i));
						}
					}
					position = ANGLE_MIN;
					servo.setPulseWidth(map(position));
					Thread.sleep(1000);
/*
					for (int i = RESULTS_SIZE - 2; i > 0; i--) {
						position -= ANGLE_STEP;
						servo.setPulseWidth(map(position));
						Thread.sleep(STEP_DELAY);
						results.set(i, new Pair(position, getDistance()));
//						Log.d(" ", i + "-, " + position + ", map: " + map(position) + " | " + results.get(i).distance);
						if (listener != null) {
							listener.onResult(getResultsOnly(), results.get(i));
						}
					}
					position -= ANGLE_STEP;
*/
				} else {
					servo.setPulseWidth(0);
					Thread.sleep(200);
				}
			}
		} catch (Exception e) {
			Log.e(C.TAG, e.toString());
		}
	}
	
	private long map(long x) {
		return (x - ANGLE_MIN) * (SERVO_MAX - SERVO_MIN) / (ANGLE_MAX - ANGLE_MIN) + SERVO_MIN;
	}
	

}
