package pl.poznan.put.ioiorobot.positioning;

public interface IMotorsController {
	
	int getDirection();

	void setDirection(int direction);

	int getSpeed();

	void setSpeed(int speed);
	
	void stop();
	
	void start();

	void turn(float d);
	
	void turnTo(float d);
}
