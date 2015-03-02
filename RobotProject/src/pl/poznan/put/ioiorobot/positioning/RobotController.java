package pl.poznan.put.ioiorobot.positioning;

import pl.poznan.put.ioiorobot.camera.MyCamera;
import pl.poznan.put.ioiorobot.sensors.FrontDistanceSensor;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.utils.Config;
import android.util.Log;

public class RobotController {

	private float distance;
	private Position lastPosition;
	private Position startPosition;
	private final Position position;
	private MyCamera camera;
	private IMotorsController motorsController;
	private FrontDistanceSensor frontDistanceSensor;
	private IDistanceSensor sharpSensor;
	private static DrivingThread drivingThread;
	private static CameraThread cameraThread;
	private boolean motorsRunning = false;

	public RobotController(Position position, MyCamera camera, IMotorsController motorsController,
			FrontDistanceSensor frontDistanceSensor, IDistanceSensor sharpSensor) {
		super();
		this.position = position;
		this.lastPosition = new Position(position);
		this.camera = camera;
		this.motorsController = motorsController;
		this.frontDistanceSensor = frontDistanceSensor;
		this.sharpSensor = sharpSensor;

		if (drivingThread == null) {
			drivingThread = new DrivingThread();
			drivingThread.start();
		}

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
		sharpSensor.startSensor();
	}

	public void disable() {
		motorsController.stop();
		running = false;
		sharpSensor.stopSensor();
	}

	private class CameraThread extends Thread {
		@Override
		public void run() {
			distance = 0;
			try {
				while (!killed) {
					if (running) {
						motorsRunning = true;
						camera.setLedMode(false);
						motorsController.enablePid();
						while (distance < Config.robotStepDistance) {
							distance += position.distanceTo(lastPosition);
							roundDistance += position.distanceTo(lastPosition);
							lastPosition = new Position(position);
							sleep(Config.loopSleep);
						}
						distance = 0;

						motorsRunning = false;
						camera.setLedMode(true);
						motorsController.disablePid();
						motorsController.stop();
						float angle = position.angle();
						float startAngle = angle;
						doCameraProcessing();
						int i = 0;
						do {
							angle += 2 * Math.PI / 9;
							motorsController.turnTo(angle);
							doCameraProcessing();
						} while (++i < 3 && running);
						angle = startAngle;
						motorsController.turnTo(startAngle);
						i = 0;
						do {
							angle -= 2 * Math.PI / 9;
							motorsController.turnTo(angle);
							doCameraProcessing();
						} while (++i < 3 && running);
						motorsController.turnTo(startAngle);
					}
				}

				sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	private void doCameraProcessing() throws InterruptedException {
		Thread.sleep(1000);
		camera.setFramesToProcess(Config.framesPerRotate);
		while (!camera.isReady() && running) {
			Thread.sleep(Config.loopSleep);
		}
	}

	private int startPositonCount = 0;
	private float roundDistance = 0;

	private void checkStartPosition() {
		if (startPosition.distanceTo(position) < Config.distanceToStart && roundDistance > 1000) {
			startPositonCount++;
			roundDistance = 0;
		}
	}

	private class DrivingThread extends Thread {
		@Override
		public void run() {
			try {
				while (!running) {
					sleep(Config.loopSleep);
				}
				running = false;
				sleep(2000);
				while (!killed && frontDistanceSensor.isFreeCenter() && frontDistanceSensor.isFreeLeft()) {
					motorsController.setSpeed(Config.maxSpeed);
					motorsController.setDirection(0);
					motorsController.start();
					sleep(Config.loopSleep);
				}
				running = true;
				startPosition = new Position(position);
				roundDistance = 0;
				while (!killed) {
					if (running && motorsRunning) {
						checkStartPosition();
						if (startPositonCount == Config.rounds) {
							disable();
						}

						int value = (Config.minFreeDistance - Math.min(frontDistanceSensor.getLeft(),
								Config.minFreeDistance * 2)) / 12;
						if (frontDistanceSensor.getCenter() < Config.minFreeDistance + 10) {
							value = (Config.minFreeDistance + 10 - frontDistanceSensor.getCenter());
						}
						motorsController.start();
						motorsController.setSpeed((int) (Config.maxSpeed));
						motorsController.setDirection(Math.max(Math.min(value, Config.maxDirection),
								-Config.maxDirection));

					} else if (!running) {
						motorsController.stop();
					}
					sleep(Config.loopSleep);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
