package pl.poznan.put.ioiorobot.mapping;

import pl.poznan.put.ioiorobot.positioning.Position;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.utils.Config;
import android.graphics.Point;

/**
 * Przeszkoda na mapie
 */
public class Obstacle {

	private Point point;
	private int count;
	private boolean accepted;

	public boolean isAccepted() {
		return accepted;
	}

	public void accept() {
		accepted = true;
	}

	public int getCount() {
		return count;
	}

	public Point getPoint() {
		return point;
	}

	public void increment() {
		count++;
	}

	public Obstacle(Point p) {
		this.point = p;
		count = 1;
		accepted = false;
	}

	public Obstacle(Position robotPosition, IDistanceSensor.AngleDistancePair sensor) {
		super();
		point = new Point();
		double angle = robotPosition.angle() - sensor.angle / 180.0 * Math.PI;
		if (angle > Math.PI) {
			angle -= 2 * Math.PI;
		} else if (angle < -Math.PI) {
			angle += 2 * Math.PI;
		}

		// pozycja robota + pozycja czujnika + odczyt czujnika
		this.point.x = (int) (robotPosition.x() + Config.wheelsToSensorDistance * Math.sin(robotPosition.angle()) + sensor.distance
				* Math.sin(angle));
		this.point.y = (int) (robotPosition.y() + Config.wheelsToSensorDistance * Math.cos(robotPosition.angle()) + sensor.distance
				* Math.cos(angle));
	}
}
