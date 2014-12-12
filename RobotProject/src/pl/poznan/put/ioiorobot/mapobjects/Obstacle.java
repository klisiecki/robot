package pl.poznan.put.ioiorobot.mapobjects;

import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.utils.C;
import android.graphics.Point;

public class Obstacle extends Point {

		public Obstacle(Position robotPosition, IDistanceSensor.Pair sensor){
			super();
			
			double angle = robotPosition.angle() + sensor.angle/180.0 * Math.PI;
			if (angle > Math.PI) {
				angle -= 2 * Math.PI;
			} else if (angle < -Math.PI) {
				angle += 2 * Math.PI;
			}
			
			//pozycja robota + pozycja czujnika + odczyt czujnika
			this.x = (int) (robotPosition.x() + C.sensorDistance * Math.sin(robotPosition.angle()) + sensor.distance * Math.sin(angle));
			this.y = (int) (robotPosition.y() + C.sensorDistance * Math.cos(robotPosition.angle()) + sensor.distance * Math.cos(angle));
		}
}
