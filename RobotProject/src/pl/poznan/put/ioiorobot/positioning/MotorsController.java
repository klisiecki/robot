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

	public boolean isZero(float value) {
		return value >= -0.001 && value <= 0.001;
	}
	
	
	
	private int maxTurnSpeed = 40;
	private void turnSleep() throws InterruptedException {
		Thread.sleep(20);
		if (speed < maxTurnSpeed) {
			speed++;
		}
	}

	public void turnTo(float targetAngle) {
		if (targetAngle > Math.PI) {
			targetAngle -= 2 * Math.PI;
		} else if (targetAngle < -Math.PI) {
			targetAngle += 2 * Math.PI;
		}

		speed = 20;
		float curAngle, lastAngle;
		start();

		float changeAngle = targetAngle - encodersData.getPosition().angle();
		Log.d(Config.TAG, "change = " + changeAngle + ", target = " + targetAngle);
		try {
			if (changeAngle < -Math.PI) {
				direction = Config.maxDirection;
				lastAngle = encodersData.getPosition().angle();
				while ((curAngle = encodersData.getPosition().angle()) > 0 && lastAngle <= curAngle + Math.PI
						&& enabled) {
					lastAngle = curAngle;
					Log.d("loop", "while1: " + encodersData.getPosition().angle());
					turnSleep();
				}
				while (encodersData.getPosition().angle() < targetAngle && enabled) {
					Log.d("loop", "while2: " + encodersData.getPosition().angle());
					turnSleep();
				}
			} else if (changeAngle >= -Math.PI && changeAngle < 0) {
				direction = -Config.maxDirection;
				lastAngle = encodersData.getPosition().angle();
				while ((curAngle = encodersData.getPosition().angle()) > targetAngle && lastAngle + Math.PI >= curAngle
						&& enabled) {
					lastAngle = curAngle;
					turnSleep();
					Log.d("loop", "while3: " + encodersData.getPosition().angle() + " target = " + targetAngle);
				}
				Log.d("loop", "....while3");
			} else if (changeAngle >= 0 && changeAngle < Math.PI) {
				direction = Config.maxDirection;
				lastAngle = encodersData.getPosition().angle();
				while ((curAngle = encodersData.getPosition().angle()) < targetAngle && lastAngle <= curAngle + Math.PI
						&& enabled) {
					// Log.d(Config.TAG, "last = " + lastAngle + ", cur = " +
					// curAngle);
					lastAngle = curAngle;
					turnSleep();
					Log.d(Config.TAG, "while4: " + encodersData.getPosition().angle());
				}
				Log.d(Config.TAG, " " + ((curAngle = encodersData.getPosition().angle()) < targetAngle) + "|"
						+ (lastAngle < curAngle || isZero(curAngle - lastAngle)) + "|" + enabled);
				// Log.d(Config.TAG, "....while4 " + curAngle);
			} else if (changeAngle >= Math.PI) {
				direction = -Config.maxDirection;
				lastAngle = encodersData.getPosition().angle();
				while ((curAngle = encodersData.getPosition().angle()) < 0 && lastAngle + Math.PI >= curAngle
						&& enabled) {
					lastAngle = curAngle;
					turnSleep();
					Log.d("loop", "while5: " + encodersData.getPosition().angle());
				}
				while (encodersData.getPosition().angle() > targetAngle && enabled) {
					turnSleep();
					Log.d("loop", "while6: " + encodersData.getPosition().angle());
				}
			}
		} catch (Exception e) {
		}

		stop();
		direction = 0;
	}

	public void turn(float angle) {
		float targetAngle = encodersData.getPosition().angle() + angle;
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
					float left = 0; // = ((float) Math.abs(speed) + (float)
									// direction * speed / 100f) / 200f;
					float right = 0; // = ((float) Math.abs(speed) - (float)
										// direction * speed / 100f) / 200f;

					if (speed > 0 && regulation == 0) {
						forward();
						left = right = 1;
					} else if (speed > 0 && regulation < 0 && regulation > -Config.maxDirection / 2) {
						forward();
						right = 1;
						left = 1 - 2 * (float) (-regulation) / Config.maxDirection;
					} else if (speed > 0 && regulation <= -Config.maxDirection / 2) {
						left();
						right = 1;
						left = 2 * ((float) -regulation - Config.maxDirection / 2) / Config.maxDirection;
					} else if (speed > 0 && regulation > 0 && regulation < Config.maxDirection / 2) {
						forward();
						right = 1 - 2 * (float) (regulation) / Config.maxDirection;
						left = 1;
					} else if (speed > 0 && regulation >= Config.maxDirection / 2) {
						right();
						right = 2 * ((float) regulation - Config.maxDirection / 2) / Config.maxDirection;
						left = 1;
					} else {
						l1.write(false);
						l2.write(false);
						r1.write(false);
						r2.write(false);
					}

					// float left = ((float) Math.abs(speed) + (float) direction
					// * speed / 100f) / 200f;
					// float right = ((float) Math.abs(speed) - (float)
					// direction * speed / 100f) / 200f;
					//
					// if (speed > 5) {
					// l1.write(true);
					// l2.write(false);
					// r1.write(true);
					// r2.write(false);
					// } else if (speed < -5) {
					// l1.write(false);
					// l2.write(true);
					// r1.write(false);
					// r2.write(true);
					// float tmp = left;
					// left = right;
					// right = tmp;
					// } else {
					// l1.write(false);
					// l2.write(false);
					// r1.write(false);
					// r2.write(false);
					// }

					if (!enabled) {
						left = right = 0;
					}
					if (left < 0.1f)
						left = 0;
					if (right < 0.1f)
						right = 0;
					
					left = left * speed / Config.maxSpeed;
					right = right * speed / Config.maxSpeed;
					lPwm.setDutyCycle(Math.min(left, 1f));
					rPwm.setDutyCycle(Math.min(right, 1f));
					Log.d("motor", "\t\t\tx= " + direction + " , y= " + speed + "      regulation =  " + regulation
							+ "     L = " + left + "   R = " + right);
					Thread.sleep(10);
				} catch (Exception e) {
				}
			}
		}

		private void left() throws ConnectionLostException {
			l1.write(true);
			l2.write(false);
			r1.write(false);
			r2.write(true);
		}

		private void right() throws ConnectionLostException {
			l1.write(false);
			l2.write(true);
			r1.write(true);
			r2.write(false);
		}

		private void forward() throws ConnectionLostException {
			l2.write(true);
			l1.write(false);
			r2.write(true);
			r1.write(false);
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

	private boolean PIDenabled = true;

	public void enablePid() {
		PIDenabled = true;
	}

	public void disablePid() {
		PIDenabled = false;
	}

	private class PID extends TimerTask {
		private int integral = 0;
		private int popError = 0;
		private int iteration = 0;

		private int pdRegulationLenght = 10;
		private int integralBound = Config.maxDirection / 2;

		private int Kp = 10;
		private int Ki = 5;
		private int Kd = 0;

		public void run() {
			if (PIDenabled) {
				int error = direction;

				integral += error / 5;
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
				regulation = Math.min(Math.max(regulation, -Config.maxDirection), Config.maxDirection);
			} else {
				regulation = direction;
			}
		}
	}
}