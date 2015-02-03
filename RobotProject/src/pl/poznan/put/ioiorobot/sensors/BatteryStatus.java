package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import pl.poznan.put.ioiorobot.utils.Config;

/**
 * Klasa obsługująca stan naładowania baterii
 */
public class BatteryStatus implements IBatteryStatus {
	private final static double MIN = 5.5;
	private final static double MAX = 6.2;

	private final static int CAPACITY = 100;

	private AnalogInput pin;

	private BatteryStatusChangedListener listener;

	public void setBatteryStatusChangedListener(BatteryStatusChangedListener listener) {
		this.listener = listener;
	}

	public BatteryStatus(IOIO ioio, int pinNr) throws ConnectionLostException {
		pin = ioio.openAnalogInput(pinNr);
		pin.setBuffer(CAPACITY);

		(new Thread() {
			@Override
			public void run() {
				while (true) {
					if (listener != null) {
						listener.onBatteryStatusChanged(getStatus());
					}
					try {
						sleep(Config.batterySleep);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
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

		int result = (int) ((val - MIN) / (MAX - MIN) * 100);
		return Math.max(Math.min(result, 100), 0);
	}
}
