package pl.poznan.put.ioiorobot.motors;

public class Position {
	private double x;
	private double y;
	private double angle;

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double angle() {
		return angle;
	}

	public Position() {
		x = y = angle = 0.0;
	}
	
	public Position(double x, double y, double angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	public void set(double x, double y, double angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	// public void move(double x, double y, double angle) {
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
