package pl.poznan.put.ioiorobot.motors;

public interface IMotorsController {
	
	int getDirection();

	void setDirection(int direction);

	int getSpeed();

	void setSpeed(int speed);
	
	int getRegulacja();
	
	void stop();
	
	void start();
}
