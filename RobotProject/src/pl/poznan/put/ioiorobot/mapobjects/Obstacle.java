package pl.poznan.put.ioiorobot.mapobjects;

import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.utils.C;
import android.graphics.Point;

/**
 * Przeszkoda na mapie
 */
public class Obstacle {

	private Point point;
	private int value;
	private boolean accepted;

	public boolean isAccepted() {
		return accepted;
	}

	public void accept() {
		accepted = true;
	}

	public int getValue() {
		return value;
	}

	public void increment() {
		value++;
	}

	public Obstacle(Point p) {
		this.point = p;
		value = 1;
		accepted = false;
	}

	public Obstacle(Position robotPosition, IDistanceSensor.Pair sensor) {
		super();
		point = new Point();
		double angle = robotPosition.angle() - sensor.angle / 180.0 * Math.PI;
		if (angle > Math.PI) {
			angle -= 2 * Math.PI;
		} else if (angle < -Math.PI) {
			angle += 2 * Math.PI;
		}

		// pozycja robota + pozycja czujnika + odczyt czujnika
		this.point.x = (int) (robotPosition.x() + C.wheelsToSensorDistance * Math.sin(robotPosition.angle()) + sensor.distance
				* Math.sin(angle));
		this.point.y = (int) (robotPosition.y() + C.wheelsToSensorDistance * Math.cos(robotPosition.angle()) + sensor.distance
				* Math.cos(angle));
	}

	public Point getPoint() {
		return point;
	}
}