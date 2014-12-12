package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.util.Log;

public class SharpDistanceSensor extends AbstractDistanceSensor {
	private AnalogInput input;
	
	public SharpDistanceSensor(IOIO ioio_, int servoPin, int pin) throws ConnectionLostException {
		super(ioio_, servoPin);
		input = ioio_.openAnalogInput(pin);

		Thread t = new Thread(this);
		t.start();
	}

	@Override
	protected int getDistance() throws ConnectionLostException, InterruptedException {
		//wzór z https://www.sparkfun.com/products/242
		int val = (int) (41.543 * Math.pow(input.getVoltage() + 0.30221,-1.5281)) * 10; // *10 zamienia na mm
		return val;
	}


}
