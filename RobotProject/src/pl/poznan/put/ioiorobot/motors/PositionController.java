package pl.poznan.put.ioiorobot.motors;

import pl.poznan.put.ioiorobot.utils.C;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;

public class PositionController {
	private Position position = new Position();

	public Position getPosition() {
		return position;
	}

	public PositionController() {
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
		
		//Log.d(C.TAG, "\t\tleft = " + left + "  right = " + right + "  x = " + x + "  y = " + y + "  angle = " + angle);
	}
	
	public void set(double x, double y, double angle){
		position.set(x, y, angle);
	}
	
}
