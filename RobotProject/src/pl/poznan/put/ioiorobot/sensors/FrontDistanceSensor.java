package pl.poznan.put.ioiorobot.sensors;

import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

public class FrontDistanceSensor {
	private IDistanceSensor leftSensor;
	private IDistanceSensor centerSensor;
	private IDistanceSensor rigntSensor;
	private int freeDistance;

	public FrontDistanceSensor(IOIO ioio_, int lTriggerPin, int lEchoPin, int cTriggerPin, int cEchoPin,
			int rTriggerPin, int rEchoPin, int freeDistance) throws ConnectionLostException {
		leftSensor = new HCSR04DistanceSensor(ioio_, -1, lTriggerPin, lEchoPin);
		centerSensor = new HCSR04DistanceSensor(ioio_, -1, cTriggerPin, cEchoPin);
		rigntSensor = new HCSR04DistanceSensor(ioio_, -1, rTriggerPin, rEchoPin);
		this.freeDistance = freeDistance;
	}

	public boolean isFreeLeft() throws ConnectionLostException, InterruptedException {
		return leftSensor.getDistance() > freeDistance;
	}

	public boolean isFreeCenter() throws ConnectionLostException, InterruptedException {
		return centerSensor.getDistance() > freeDistance;
	}

	public boolean isFreeRight() throws ConnectionLostException, InterruptedException {
		return rigntSensor.getDistance() > freeDistance;
	}

}
