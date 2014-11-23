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
	
	public void move(double x, double y, double angle) {
		this.x += x;
		this.y += y;
		this.angle += angle;
		if (angle > Math.PI) {
			angle -= 2 * Math.PI;
		}
		if (angle < (-Math.PI)) {
			angle += 2 * Math.PI;
		}
	}
}
