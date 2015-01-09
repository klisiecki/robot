package pl.poznan.put.ioiorobot;

import pl.poznan.put.ioiorobot.camera.MyCamera;
import pl.poznan.put.ioiorobot.motors.IMotorsController;
import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.sensors.FrontDistanceSensor;
import pl.poznan.put.ioiorobot.utils.C;

public class Controller extends Thread {

	private float distance;
	private Position lastPosition;
	private Position position;
	private MyCamera camera;
	private IMotorsController motorsController;
	private FrontDistanceSensor frontDistanceSensor;
	private DrivingThread drivingThread;
	private boolean motorsRunning = false;

	public Controller(Position position, MyCamera camera, IMotorsController motorsController,
			FrontDistanceSensor frontDistanceSensor) {
		super();
		this.position = position;
		this.lastPosition = new Position(position);
		this.camera = camera;
		this.motorsController = motorsController;
		this.frontDistanceSensor = frontDistanceSensor;
		drivingThread = new DrivingThread();
		drivingThread.start();
	}

	@Override
	public void run() {
		distance = 0;
		while (true) {
			motorsRunning = true;
			while (distance < C.robotStepDistance) {
				distance += position.distanceTo(lastPosition);
				lastPosition = new Position(position);

				try {
					sleep(C.loopSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			distance = 0;
			motorsRunning = false;
			camera.setFramesToProcess(C.framesPerRotate);

			while (!camera.isReady()) {
				try {
					sleep(C.loopSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class DrivingThread extends Thread {
		@Override
		public void run() {
			try {
				while (true) {
					if (motorsRunning) {
						motorsController.setSpeed((int) (C.maxSpeed / 2.5));
						if (frontDistanceSensor.isFreeLeft()) {
							motorsController.turn(-(float) Math.PI / 60);
						} else if (frontDistanceSensor.isFreeCenter()) {
							motorsController.start();
						} else if (frontDistanceSensor.isFreeRight()) {
							motorsController.turn((float) Math.PI / 2);
						} else {
							motorsController.stop();
						}
					} else {
						motorsController.stop();
					}
					sleep(C.loopSleep);
				}
			} catch (Exception e) {

			}
		}
	}
}
