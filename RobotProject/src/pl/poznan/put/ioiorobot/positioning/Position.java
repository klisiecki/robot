package pl.poznan.put.ioiorobot.positioning;

import android.graphics.Point;

/**
 * Klasa przechowująca pozycję robota w układzie współrzędnych wraz z aktualnym
 * kątem
 */
public class Position {
	private float x;
	private float y;
	private float angle;

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float angle() {
		return angle;
	}

	public Point getPoint() {
		return new Point((int) x, (int) y);
	}

	public Point getVectorPoint() {
//		Log.d(C.TAG, "\t\t\t angle == " + angle + "   x == " + Math.sin(angle)*100 + "    y == " + Math.cos(angle)*100);
		return new Point((int) (Math.sin(angle) * 10000 + x), (int) (Math.cos(angle) * 10000 + y));
	}

	public void addAngle(float angle) {
		this.angle += angle;
		if (this.angle > Math.PI) {
			this.angle -= 2 * Math.PI;
		} else if (this.angle < -Math.PI) {
			this.angle += 2 * Math.PI;
		}
	}

	public Position() {
		x = y = angle = 0.0f;
	}

	public Position(float x, float y, float angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}
	
	public Position(Position position, Point shift) {
		this(position);
		this.x += shift.y * Math.sin(position.angle()) + shift.x * Math.cos(position.angle());
		this.y += shift.y * Math.cos(position.angle()) + shift.x * Math.sin(position.angle());
	}

	public Position(Position position) {
		this.x = position.x();
		this.y = position.y();
		this.angle = position.angle();
	}

	public void set(float x, float y, float angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}
	
	public float distanceTo(Position position) {
		return (float) Math.sqrt((position.x - x)*(position.x - x) + (position.y - y)*(position.y - y));
	}

	// public void move(float x, float y, float angle) {
	// this.x += x;
	// this.y += y;
	// this.angle += angle;
	// if (this.angle > Math.PI) {
	// this.angle -= 2 * Math.PI;
	// }
	// if (this.angle < (-Math.PI)) {
	// this.angle += 2 * Math.PI;
	// }
	//
	// // Log.d(C.TAG, "x = " + this.x + "   y = " + this.y + "   angle = " +
	// angle360);
	//
	// }

}
