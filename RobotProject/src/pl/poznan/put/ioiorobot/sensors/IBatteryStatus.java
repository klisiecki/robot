package pl.poznan.put.ioiorobot.sensors;

public interface IBatteryStatus {
	int getStatus();
	
	void setBatteryStatusChangedListener(BatteryStatusChangedListener listener);
	
	public interface BatteryStatusChangedListener {
		void onBatteryStatusChanged(int status);
	}
}
