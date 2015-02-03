package pl.poznan.put.ioiorobot.positioning;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.core.Mat;

import pl.poznan.put.ioiorobot.utils.Config;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.text.format.Time;
import android.util.Log;

/**
 * Klasa obsługjąca sterowanie silnikami
 */
public class MotorsController implements IMotorsController {
	private static final int FREQUENCY = 100;

	private int direction;
	private int speed;

	private int regulation = 0;

	private boolean enabled = true;

	private IOIO ioio_;
	private DigitalOutput l1;
	private DigitalOutput l2;
	private PwmOutput lPwm;
	private DigitalOutput r1;
	private DigitalOutput r2;
	private PwmOutput rPwm;

	private EncodersData encodersData;

	private static Timer timerPID;

	public MotorsController(IOIO ioio_, int l1Pin, int l2Pin, int lPwmPin, int r1Pin, int r2Pin, int rPwmPin,
			EncodersData encodersData) throws ConnectionLostException {
		direction = 0;
		speed = 0;
		this.ioio_ = ioio_;
		l1 = ioio_.openDigitalOutput(l1Pin, false);
		l2 = ioio_.openDigitalOutput(l2Pin, false);
		lPwm = ioio_.openPwmOutput(lPwmPin, FREQUENCY);
		r1 = ioio_.openDigitalOutput(r1Pin, false);
		r2 = ioio_.openDigitalOutput(r2Pin, false);
		rPwm = ioio_.openPwmOutput(rPwmPin, FREQUENCY);

		this.encodersData = encodersData;

		MotorThread t = new MotorThread();

		if (null != timerPID) {
			timerPID.cancel();
			timerPID.purge();
			timerPID = null;
		}

		timerPID = new Timer();

		timerPID.scheduleAtFixedRate(new PID(), 0, Config.PIDPeriod);
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		if (direction > Config.maxDirection) {
			this.direction = Config.maxDirection;
		} else if (direction < -Config.maxDirection) {
			this.direction = -Config.maxDirection;
		} else {
			this.direction = direction;
		}
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		if (speed > Config.maxSpeed) {
			this.speed = Config.maxSpeed;
		} else if (speed < -Config.maxSpeed) {
			this.speed = -Config.maxSpeed;
		} else {
			this.speed = speed;
		}
	}

	
	private void turnTo(float targetAngle) {
		speed = Config.maxSpeed;
		start();

		float changeAngle = targetAngle - encodersData.getPosition().angle();
		try {
			if (changeAngle < -Math.PI) {
				direction = Config.maxDirection;
				while (encodersData.getPosition().angle() > 0 && enabled)
					Thread.sleep(20);
				while (encodersData.getPosition().angle() < targetAngle && enabled)
					Thread.sleep(20);
			} else if (changeAngle >= -Math.PI && changeAngle < 0) {
				direction = -Config.maxDirection;
				while (encodersData.getPosition().angle() > targetAngle && enabled)
					Thread.sleep(20);
			} else if (changeAngle >= 0 && changeAngle < Math.PI) {
				direction = Config.maxDirection;
				while (encodersData.getPosition().angle() < targetAngle && enabled)
					Thread.sleep(20);
			} else if (changeAngle >= Math.PI) {
				direction = -Config.maxDirection;
				while (encodersData.getPosition().angle() < 0 && enabled)
					Thread.sleep(20);
				while (encodersData.getPosition().angle() > targetAngle && enabled)
					Thread.sleep(20);
			}
		} catch (Exception e) {
		}

		stop();
		direction = 0;
	}

	public void turn(float angle) {
		float targetAngle = encodersData.getPosition().angle() + angle;
		if (targetAngle > Math.PI) {
			targetAngle -= 2 * Math.PI;
		} else if (targetAngle < -Math.PI) {
			targetAngle += 2 * Math.PI;
		}
		turnTo(targetAngle);
	}

	public int getRegulacja() {
		return regulation;
	}

	class MotorThread extends Thread {
		public MotorThread() {
			start();
			Log.e(Config.TAG, "MotorThread constructor");
		}

		@Override
		public void run() {
			while (true) {

				try {
					float left = 0; // = ((float) Math.abs(speed) + (float) direction * speed / 100f) / 200f;
					float right = 0; // = ((float) Math.abs(speed) - (float) direction * speed / 100f) / 200f;

					if (speed > 0 && direction == 0) {
						l1.write(true);
						l2.write(false);
						r1.write(true);
						r2.write(false);
						left = right = (float)speed/Config.maxSpeed;
					} else if (speed > 0 && direction < 0) {
						l1.write(false);
						l2.write(true);
						r1.write(true);
						r2.write(false);
						left = right = (float)speed/Config.maxSpeed/2;
					} else if (speed > 0 && direction > 0) {
						l1.write(true);
						l2.write(false);
						r1.write(false);
						r2.write(true);
						left = right = (float)speed/Config.maxSpeed/2;
					} else {
						l1.write(false);
						l2.write(false);
						r1.write(false);
						r2.write(false);
					}
					
					
					
//					float left = ((float) Math.abs(speed) + (float) direction * speed / 100f) / 200f;
//					float right = ((float) Math.abs(speed) - (float) direction * speed / 100f) / 200f;
//
//					if (speed > 5) {
//						l1.write(true);
//						l2.write(false);
//						r1.write(true);
//						r2.write(false);
//					} else if (speed < -5) {
//						l1.write(false);
//						l2.write(true);
//						r1.write(false);
//						r2.write(true);
//						float tmp = left;
//						left = right;
//						right = tmp;
//					} else {
//						l1.write(false);
//						l2.write(false);
//						r1.write(false);
//						r2.write(false);
//					}
					

					if (!enabled) {
						left = right = 0;
					}
					if (left < 0.1f)
						left = 0;
					if (right < 0.1f)
						right = 0;
					lPwm.setDutyCycle(Math.min(left, 1f)); // float [0 ; 1]
					rPwm.setDutyCycle(Math.min(right, 1f));
					// Log.d(C.TAG, "\t\t\tx= " + direction + " , y= " + speed
					// + "     L = " + left + "   R = " + right);
					Thread.sleep(1);
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void stop() {
		enabled = false;

	}

	@Override
	public void start() {
		enabled = true;
	}

	private class PID extends TimerTask {
		private int integral = 0;
		private int popError = 0;
		private int iteration = 0;

		private int pdRegulationLenght = 10;
		private int integralBound = 100;

		private int Kp = 5;
		private int Ki = 0;
		private int Kd = 5;

		public void run() {
			int error = direction;

			integral += error;
			integral = Math.min(Math.max(integral, -integralBound), integralBound);

			int differential = error - popError;

			if (iteration == pdRegulationLenght) {
				popError = error;
				iteration = 0;
			} else {
				iteration++;
			}

			/* Obliczenie właściwej wartości regulacji. */
			regulation = Math.round((Kp * error + Kd * differential + Ki * integral) / (Kp + Kd + Ki));
		}
	}
}