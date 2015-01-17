package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.exception.ConnectionLostException;

import java.util.List;

public interface IDistanceSensor {
	static final int SERVO_MIN = 800; // 600
	static final int SERVO_MAX = 2300; // 2300

	static final int ANGLE_MIN = -60;
	static final int ANGLE_MAX = 60; // TODO robi 16 kroków zamiast 17
	static final int ANGLE_STEP = 5;

	static final int STEP_DELAY = 150;

	static final int RESULTS_SIZE = (ANGLE_MAX - ANGLE_MIN) / ANGLE_STEP;

	public interface DistanceResultListener {
		void onResult(List<Integer> results, IDistanceSensor.AngleDistancePair last);
	}

	void setDistanceResultListener(DistanceResultListener listener);

	List<AngleDistancePair> getResults();

	List<Integer> getResultsOnly();

	void stopSensor();

	void startSensor();

	int getDistance() throws ConnectionLostException, InterruptedException;

	public class AngleDistancePair {
		public AngleDistancePair(int angle, int distance) {
			this.angle = angle;
			this.distance = distance;
		}

		public int angle;
		public int distance;
	}
}
