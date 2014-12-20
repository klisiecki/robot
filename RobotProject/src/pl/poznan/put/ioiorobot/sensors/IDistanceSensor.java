package pl.poznan.put.ioiorobot.sensors;

import java.util.List;

public interface IDistanceSensor {
	static final int SERVO_MIN = 800; // 600
	static final int SERVO_MAX = 2300; // 2300

	static final int ANGLE_MIN = -60;
	static final int ANGLE_MAX = 60; //TODO robi 16 kroków zamiast 17 
	static final int ANGLE_STEP = 5;

	static final int STEP_DELAY = 300;

	static final int RESULTS_SIZE = (ANGLE_MAX - ANGLE_MIN) / ANGLE_STEP;
	
	public interface DistanceResultListener {
		void onResult(List<Integer> results, IDistanceSensor.Pair last);
	}
	
	void setDistanceResultListener(DistanceResultListener listener);
	
	List<Pair> getResults();
	
	List<Integer> getResultsOnly();
	
	void stopSensor();
	
	void startSensor();
	
	public class Pair {
		public Pair(int angle, int distance) {
			this.angle = angle;
			this.distance = distance;
		}
		public int angle;
		public int distance;
		@Override
		public String toString() {
			return distance + "";
		}
		
	}
}
