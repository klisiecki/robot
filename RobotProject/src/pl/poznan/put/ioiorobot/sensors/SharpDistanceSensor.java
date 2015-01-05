package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

import java.util.Arrays;

import pl.poznan.put.ioiorobot.utils.C;
import android.util.Log;

/**
 * Implementacja czujnika odległości Sharp 2Y0A21
 */
public class SharpDistanceSensor extends AbstractDistanceSensor {
	private AnalogInput input;
	int size = 100;
	float[] buffer = new float[size];
	
	public SharpDistanceSensor(IOIO ioio_, int servoPin, int pin) throws ConnectionLostException {
		super(ioio_, servoPin);
		input = ioio_.openAnalogInput(pin);
		input.setBuffer(size);

		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public int getDistance() throws ConnectionLostException, InterruptedException {
		//wzór z https://www.sparkfun.com/products/242
		for (int i = 0; i < size; i++) {
			buffer[i] = Math.round(input.getVoltageBuffered() * 1000) / 1000.0f;
		}
		Arrays.sort(buffer);
		Log.d(C.TAG, Arrays.toString(buffer));
		int val = (int) (41.543 * Math.pow(buffer[size/2] + 0.30221,-1.5281)) * 10; // *10 zamienia na mm
		return val;
	}


}
