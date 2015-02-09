package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.Arrays;

import pl.poznan.put.ioiorobot.utils.Config;
import android.util.Log;

/**
 * Implementacja czujnika odległości Sharp 2Y0A21
 */
public class SharpDistanceSensor extends AbstractDistanceSensor {
	private static final int BUFFER_SIZE = 100;

	private AnalogInput input;
	float[] buffer = new float[BUFFER_SIZE];

	public SharpDistanceSensor(IOIO ioio_, int servoPin, int pin) throws ConnectionLostException {
		super(ioio_, servoPin);
		input = ioio_.openAnalogInput(pin);
		input.setBuffer(BUFFER_SIZE);

		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public int getDistance() throws ConnectionLostException, InterruptedException {
		// wzór z https://www.sparkfun.com/products/242
		for (int i = 0; i < BUFFER_SIZE; i++) {
			buffer[i] = input.getVoltageBuffered();
		}
		Arrays.sort(buffer);
		int val = (int) (41.543 * Math.pow(buffer[BUFFER_SIZE / 2] + 0.30221, -1.5281)) * 10; // *10
																								// zamienia
																								// na
																								// mm
		return val;
	}

	@Override
	public void getDistanceInit() throws ConnectionLostException, InterruptedException {
	}

}
