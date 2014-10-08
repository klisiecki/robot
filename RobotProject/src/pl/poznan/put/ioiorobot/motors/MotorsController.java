package pl.poznan.put.ioiorobot.motors;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

public class MotorsController implements IMotorsController {
	private static final int FREQUENCY = 100;

	private int direction;
	private int speed;

	private IOIO ioio_;
	private DigitalOutput l1;
	private DigitalOutput l2;
	private PwmOutput lPwm;
	private DigitalOutput r1;
	private DigitalOutput r2;
	private PwmOutput rPwm;

	public MotorsController(IOIO ioio_, int l1Pin, int l2Pin, int lPwmPin,
			int r1Pin, int r2Pin, int rPwmPin) throws ConnectionLostException {
		direction = 0;
		speed = 0;
		this.ioio_ = ioio_;
		l1 = ioio_.openDigitalOutput(l1Pin, false);
		l2 = ioio_.openDigitalOutput(l2Pin, false);
		lPwm = ioio_.openPwmOutput(lPwmPin, FREQUENCY);
		r1 = ioio_.openDigitalOutput(r1Pin, false);
		r2 = ioio_.openDigitalOutput(r2Pin, false);
		rPwm = ioio_.openPwmOutput(rPwmPin, FREQUENCY);
		
		MotorThread t = new MotorThread();
		t.start();
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		if (direction > MAX_VALUE) {
			this.direction = MAX_VALUE;
		} else if (direction < -MAX_VALUE) {
			this.direction = -MAX_VALUE;
		} else {
			this.direction = direction;
		}
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		if (speed > MAX_VALUE) {
			this.speed = MAX_VALUE;
		} else if (speed < -MAX_VALUE) {
			this.speed = -MAX_VALUE;
		} else {
			this.speed = speed;
		}
	}

	class MotorThread extends Thread {
		@Override
		public void run() {
			try {
				if (speed > 0) {
					l1.write(true);
					l2.write(false);
					r1.write(true);
					r2.write(false);
				} else {
					l1.write(false);
					l2.write(false);
					r1.write(false);
					r2.write(false);
					
				}
			} catch (ConnectionLostException e) {
			}
		}
	}
}
