package pl.poznan.put.ioiorobot.motors;

import pl.poznan.put.ioiorobot.utils.C;
import android.graphics.Point;

public class PositionController {
	private Position position;

	public Position getPosition() {
		return position;
	}

	public PositionController() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param left
	 * @param right
	 */
	public void move(double left, double right) {
		double angle = (right - left) / C.wheelDiameter;
		double distance = (right + left) / 2;
		double x = distance * Math.sin(position.angle() + angle);
		double y = distance * Math.cos(position.angle() + angle);
		position.move(x, y, angle);
	}
}
