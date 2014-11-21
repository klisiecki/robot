package pl.poznan.put.ioiorobot.motors;

import java.util.Timer;
import java.util.TimerTask;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import android.text.format.Time;
import android.util.Log;

public class MotorsController implements IMotorsController {
	private static final int FREQUENCY = 100;

	private int direction;
	private int speed;

	private int regulacja=0;

	private boolean enabled = true;

	private IOIO ioio_;
	private DigitalOutput l1;
	private DigitalOutput l2;
	private PwmOutput lPwm;
	private DigitalOutput r1;
	private DigitalOutput r2;
	private PwmOutput rPwm;
	
	private static Timer timerPID;

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

		
		if(null != timerPID)
	    {
	        timerPID.cancel();
	        timerPID.purge();
	        timerPID = null;
	    }

	    timerPID = new Timer();
				
		timerPID.scheduleAtFixedRate(new PID(), 0, 100);
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
	
	public int getRegulacja() {
		return regulacja;
	}

	class MotorThread extends Thread {
		public MotorThread() {
			start();
			Log.e("robot", "MotorThread constructor");
		}

		@Override
		public void run() {
			while (true) {
//				speed = 100;
//				direction = -100;
				
				try {
					if (speed > 5) {
						l1.write(true);
						l2.write(false);
						r1.write(true);
						r2.write(false);
					} else if (speed < -5) {
						l1.write(false);
						l2.write(true);
						r1.write(false);
						r2.write(true);
					} else {
						l1.write(false);
						l2.write(false);
						r1.write(false);
						r2.write(false);
					}
					// float left = Math.min( ((float) Math.abs(speed) +
					// direction)/100f, 1f);
					// float right = Math.min( ((float) Math.abs(speed) -
					// direction)/100f, 1f);

					float left = ((float) Math.abs(speed) + (float) direction
							* speed / 100f) / 200f;
					float right = ((float) Math.abs(speed) - (float) direction
							* speed / 100f) / 200f;
					if (!enabled) {
						left = right = 0;
					}
					if (left < 0.2f)
						left = 0;
					if (right < 0.2f)
						right = 0;
					lPwm.setDutyCycle(Math.min(left, 1f));
					rPwm.setDutyCycle(Math.min(right, 1f));
					//Log.d("robot", "\t\t\tx= " + direction + " , y= " + speed + "     L = " + left + "   R = " + right);
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
		private int calka = 0;
		private int popBlad = 0;
		private int iteracja = 0;
		
		private int dlugoscRegulacjiPd = 10;
		private int granicaCalki = 100;
		
		private int Kp = 5;
		private int Ki = 0;
		private int Kd = 5;
		
		public void run() {
//			Log.d("robot", "PID begin");
			
			int blad = direction;

			
			calka += blad;
			calka = Math.min(Math.max(calka, -granicaCalki), granicaCalki);
//			if (calka > granicaCalki) {
//				calka = granicaCalki;
//			} else if (calka < -granicaCalki) {
//				calka = -granicaCalki;
//			}
			
			
			int rozniczka = blad - popBlad;
			
			if (iteracja == dlugoscRegulacjiPd) {
				popBlad = blad;
				iteracja = 0;
			} else {
				iteracja++;
			}

			
			/* Obliczenie właściwej wartości regulacji. */
			regulacja = Math.round( (Kp * blad + Kd * rozniczka + Ki * calka) / (Kp+Kd+Ki));

			
			//Log.d("robot", "Regulacja = " + regulacja);
		}
	}
}