package pl.poznan.put.ioiorobot.motors;

public interface IMotorsController {
	public static final int MAX_VALUE = 100;
	
	int getDirection();

	void setDirection(int direction);

	int getSpeed();

	void setSpeed(int speed);
}
