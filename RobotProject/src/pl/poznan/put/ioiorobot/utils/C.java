package pl.poznan.put.ioiorobot.utils;

import android.graphics.Point;

public class C {
	public static final String TAG = "robot";
	public static int patternSize;
	public static Point screenSize;

	public static final double thresholdFactor = 0.5;
	public static final int rectangleFactor = 20;

	public static final int minPatternCoverage = 90;
	public static final int minPatternCount = 4;
	public static final int patternTTL = 10;

	public static final int maxSpeed = 100;

	public static final int wheelsDistance = 150;
	public static final int wheelDiameter = 60;
	public static final int gearRatio = 27;
	public static final int encoderResolution = 128;
}
