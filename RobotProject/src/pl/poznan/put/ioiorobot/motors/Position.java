package pl.poznan.put.ioiorobot.motors;

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
		return new Point((int) (Math.cos(angle) * 100 + x), (int) (Math.sin(angle) * 100 + y));
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
