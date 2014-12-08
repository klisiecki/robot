package pl.poznan.put.ioiorobot.sensors;

import java.util.List;

public interface IDistanceSensor {
	
	public interface DistanceResultListener {
		void onResult(List<Integer> results);
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
