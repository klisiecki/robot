package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PulseInput.PulseMode;
import ioio.lib.api.exception.ConnectionLostException;
import pl.poznan.put.ioiorobot.utils.Config;
import android.util.Log;

/**
 * Implementacja czujnika odległości HCSR04
 */
public class HCSR04DistanceSensor extends AbstractDistanceSensor {
	private DigitalOutput trigger;
	private PulseInput echo;

	public HCSR04DistanceSensor(IOIO ioio_, int servoPin, int triggerPin, int echoPin) throws ConnectionLostException {
		super(ioio_, servoPin);
		trigger = ioio_.openDigitalOutput(triggerPin, false);
		echo = ioio_.openPulseInput(echoPin, PulseMode.POSITIVE);

		if (servoPin >= 0) {
			Thread t = new Thread(this);
			t.start();
		}
	}

	@Override
	public int getDistance() throws ConnectionLostException, InterruptedException {
		trigger.write(true);
		Thread.sleep(1);
		trigger.write(false);
		Thread.sleep(10);
		int echoSeconds = (int) (echo.getDuration() * 1000 * 1000);
		return (int) echoSeconds / 29 / 2 * 10; // *10 zamienia na mm
	}
}
