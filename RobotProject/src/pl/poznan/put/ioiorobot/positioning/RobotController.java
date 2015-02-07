package pl.poznan.put.ioiorobot.positioning;

import pl.poznan.put.ioiorobot.camera.MyCamera;
import pl.poznan.put.ioiorobot.sensors.FrontDistanceSensor;
import pl.poznan.put.ioiorobot.utils.Config;
import android.util.Log;

public class RobotController {

	private float distance;
	private Position lastPosition;
	private Position position;
	private MyCamera camera;
	private IMotorsController motorsController;
	private FrontDistanceSensor frontDistanceSensor;
	private static DrivingThread drivingThread;
	private static CameraThread cameraThread;
	private boolean motorsRunning = false;

	public RobotController(Position position, MyCamera camera, IMotorsController motorsController,
			FrontDistanceSensor frontDistanceSensor) {
		super();
		this.position = position;
		this.lastPosition = new Position(position);
		this.camera = camera;
		this.motorsController = motorsController;
		this.frontDistanceSensor = frontDistanceSensor;

		// if (null != drivingThread) {
		// drivingThread.interrupt();
		// drivingThread = null;
		// }

		if (drivingThread == null) {
			drivingThread = new DrivingThread();
			drivingThread.start();
		}
		// if (null != cameraThread) {
		// cameraThread.interrupt();
		// cameraThread = null;
		// }
		if (cameraThread == null) {
			cameraThread = new CameraThread();
			cameraThread.start();
		}
	}

	private boolean running = false;
	private boolean killed = false;

	public void kill() {
		disable();
		killed = true;
	}

	public void enable() {
		running = true;
	}

	public void disable() {
		motorsController.stop();
		running = false;
		Log.d("thread", "DISABLE");
	}

	private class CameraThread extends Thread {
		@Override
		public void run() {
			distance = 0;
			// Log.d("thread", "camera");
			while (!killed) {
				if (running) {
					// Log.d("thread", "camera thread " +
					// Thread.currentThread().getId());
					motorsRunning = true;
					while (distance < Config.robotStepDistance) {
						distance += position.distanceTo(lastPosition);
						lastPosition = new Position(position);

						try {
							sleep(Config.loopSleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					distance = 0;
					motorsRunning = false;

					float angle = position.angle();
					int i = 0;
					do {
						angle -= Math.PI / 9;
						doCameraProcessing();
						Log.d(Config.TAG, "rotate " + i + " to " + angle);
						motorsController.turnTo(angle);
					} while (i++ < 9);

				}
			}
			try {
				sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
				Log.d("thread", "camera.. ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}
	}

	void doCameraProcessing() {
		camera.setFramesToProcess(Config.framesPerRotate);
		while (!camera.isReady()) {
			try {
				Thread.sleep(Config.loopSleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class DrivingThread extends Thread {
		@Override
		public void run() {
			// Log.d("thread", "driving");
			try {
				while (!killed) {
					// Log.d("thread", "driving.. " +
					// Thread.currentThread().getId());
					if (running && motorsRunning) {
						// motorsController.start();
						motorsController.setSpeed((int) (Config.maxSpeed));
						if (frontDistanceSensor.isFreeLeft()) {
							Log.d(Config.TAG, "left");
							motorsController.turn(-(float) Math.PI / 60);
							Log.d(Config.TAG, "left DONE");
						} else if (frontDistanceSensor.isFreeCenter()) {
							Log.d("loop", "przod");
							Log.d(Config.TAG, "center");
							motorsController.start();
						} else if (frontDistanceSensor.isFreeRight()) {
							Log.d(Config.TAG, "right");
							motorsController.turn((float) Math.PI / 6);
						} else {
							Log.d(Config.TAG, "stop");
							motorsController.stop();
						}
					} else {
						motorsController.stop();
					}
					sleep(Config.loopSleep);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("thread", "driving.. ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
		}
	}
}
