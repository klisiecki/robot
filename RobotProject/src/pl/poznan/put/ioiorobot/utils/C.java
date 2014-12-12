package pl.poznan.put.ioiorobot.utils;

import org.opencv.core.Scalar;

import android.graphics.Point;

public class C {
	public static final String TAG = "robot";
	public static int patternSize;
	public static Point screenSize;

	public static final double thresholdFactor = 0.5;
	public static final int rectangleFactor = 20;
	public static final int maxContoursProcessed = 10;
	
	public static final Scalar minColor = new Scalar(20, 40, 64); //min yellow
	public static final Scalar maxColor = new Scalar(70, 255, 255); //max yelow

	public static final int minPatternCoverage = 90;
	public static final int minPatternCount = 4;
	public static final int patternTTL = 10;

	public static final int maxSpeed = 100;

	public static final double wheelsDistance = 205.0;
	public static final double wheelDiameter = 60.0;
	public static final double gearRatio = 27.0;
	public static final double encoderResolution = 128.0;
	
	public static final double sensorDistance = 150.0;
	public static final double cameraDistance = 50.0;
	
	public static final double robotLenght = 200.0;
	public static final double robotWidth = 200.0;
	
	public static final int loopSleep = 100;
	public static final int batterySleep = 1000;
	
	public static final int PIDPeriod = 100;
	
	public static double cameraViewAngle = 0.96;  //55 stopni
	public static int maxObstacleDistance = 500;
}
