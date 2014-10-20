package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;

public class BatteryStatus implements IBatteryStatus {
	private final static double MIN = 5.5;
	private final static double MAX = 7.5;
	
	private final static int CAPACITY = 100;

	private AnalogInput pin;

	public BatteryStatus(IOIO ioio, int pinNr) throws ConnectionLostException {
		pin = ioio.openAnalogInput(pinNr);
		pin.setBuffer(CAPACITY);
	}

	@Override
	public int getStatus() {
		double val = -1;
		try {
			double sum = 0.0;
			for (int i = 0; i < CAPACITY; i++) {
				sum += pin.getVoltageBuffered(); 
			}
			val = (sum / CAPACITY) * 2;
		} catch (Exception e) {
		}
		
		Log.d("robot", val + " volt");
		int result = (int) ((val - MIN) / (MAX - MIN) * 100);
		return Math.max(Math.min(result, 100), 0);
	}
}
