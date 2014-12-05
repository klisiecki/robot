package pl.poznan.put.ioiorobot.sensors;

import pl.poznan.put.ioiorobot.utils.C;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CopyOfAccelerometer implements IAccelerometer {
	private SensorManager sensorManager;

	public CopyOfAccelerometer(SensorManager sensorManager) {
		this.sensorManager = sensorManager;
	}

	class Listener implements SensorEventListener {
		private Sensor accelerometer;
		private float sensorX;
		private float sensorY;
		private long sensorTime;
		private long mLastT;
		private float mLastDeltaT;

		private float mPosX;
		private float mPosY;
		private float mAccelX;
		private float mAccelY;
		private float mLastPosX;
		private float mLastPosY;

		public Listener() {
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			sensorX = event.values[0];
			sensorY = event.values[1];
			sensorTime = event.timestamp;
			update(sensorX, sensorY, sensorTime);
		}

		private void update(float sx, float sy, long timestamp) {
			final long t = timestamp;
			if (mLastT != 0) {
				final float dT = (float) (t - mLastT) * (1.0f / 1000000000.0f);
				if (mLastDeltaT != 0) {
					final float dTC = dT / mLastDeltaT;
					computePhysics(sx, sy, dT, dTC);

				}
				mLastDeltaT = dT;
			}
			mLastT = t;
		}

		public void computePhysics(float sx, float sy, float dT, float dTC) {
			// Force of gravity applied to our virtual object
			final float m = 1000.0f; // mass of our virtual object
			final float gx = -sx * m;
			final float gy = -sy * m;

			/*
			 * �F = mA <=> A = �F / m We could simplify the code by completely
			 * eliminating "m" (the mass) from all the equations, but it would
			 * hide the concepts from this sample code.
			 */
			final float invm = 1.0f / m;
			final float ax = gx * invm;
			final float ay = gy * invm;

			/*
			 * Time-corrected Verlet integration The position Verlet integrator
			 * is defined as x(t+�t) = x(t) + x(t) - x(t-�t) + a(t)�t�2 However,
			 * the above equation doesn't handle variable �t very well, a
			 * time-corrected version is needed: x(t+�t) = x(t) + (x(t) -
			 * x(t-�t)) * (�t/�t_prev) + a(t)�t�2 We also add a simple friction
			 * term (f) to the equation: x(t+�t) = x(t) + (1-f) * (x(t) -
			 * x(t-�t)) * (�t/�t_prev) + a(t)�t�2
			 */
			final float dTdT = dT * dT;
			final float x = mPosX + dTC * (mPosX - mLastPosX) + mAccelX * dTdT;
			final float y = mPosY + dTC * (mPosY - mLastPosY) + mAccelY * dTdT;
			mLastPosX = mPosX;
			mLastPosY = mPosY;
			mPosX = x;
			mPosY = y;
			mAccelX = ax;
			mAccelY = ay;
			
			Log.d(C.TAG,"("+mPosX+", "+mPosY+")");
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

	}

}
