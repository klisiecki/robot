package pl.poznan.put.ioiorobot.utils;

import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.graphics.Point;

public class Config {
	public static final String TAG = "robot";
	public static int patternSize;
	public static Point screenSize = new Point(1920, 1080);

	
	/* -------------- PRZETWARZANIE OBRAZU ---------------- */

	/* minimalne procentowe pokrycie powierzchni ekranu */
	public static final double thresholdFactor = 0.05;
	
	/* maksymalna procentowa różnica w długości przeciwległych boków czworokąta, przy której jest on nadal uważany za prostokąt */
	public static final int rectangleFactor = 20;
	public static final int maxContoursProcessed = 10;

	/* zakres żółtego koloru w HSV */
	public static final Scalar minColor = new Scalar(18, 64, 64); 
	public static final Scalar maxColor = new Scalar(40, 255, 255);
	
//	public static final Scalar minColor = new Scalar(18, 64, 64); 
//	public static final Scalar maxColor = new Scalar(40, 255, 255);
//	
	
	/* parametry do funnkcji Imgproc.adaptiveThreshold */
	public static final int thresholdBlockSize = 9;
	public static final int thresholdMC = 5;
	
	
	/* -------------- ZNACZNIKI -------------------------- */

	/* minimalne pokrycie dwóch znaczników aby były uznane za ten sam */
	public static final int minPatternCoverage = 80;
	
	/* minimalna liczba znalezień znacznika aby został zaakceptowany */
	public static final int minPatternCount = 2; // MUSI BYĆ >=2
	
	/* po takiej liczbie wykrytych znaczników, dany znacznik jest usuwany jeśli nie został wykryty ponownie */
	public static final int patternTTL = 30;
	
	/* mimalny i maksymalny procentowy udział koloru czarnego w stosunku do białego, 
	 * aby znacznik nie został odrzucony */
	public static final int minPatternFill = 10;
	public static final int maxPatternFill = 70;

	
	/* -------------- WYMIARY -------------------------- */
	
	public static final float wheelsToSensorDistance = 150.0f;
	
	/* przesunięcie kamery względem pozycji robota */
	public static final Point cameraShift = new Point(-5, 2);

	public static final float robotLenght = 200.0f;
	public static final float robotWidth = 150.0f;
	
	public static final float cameraViewAngle = 0.96f; // 55 stopni
	
	
	/* -------------- MAPOWANIE I JAZDA ------------------ */
	
	public static final int mapSize = 2000;
	
	/* najdalszy odczyt brany pod uwagę */
	public static final int maxObstacleDistance = 800; 
	public static final int minFreeDistance = 350;
	
	public static final int distanceToStart = 350;
	
	/* rozmiar kratki na mapie przeszkód */
	public static final int obstacleCellSize = 20;
	
	/* zasięg zaznaczania na mapie przeszkód */
	public static final int obstacleRange = 1; 
	
	/* minimalna liczba zaznaczeń aby odczyt został zaakceptowany */
	public static final int minObstacleCount = 3;

	/* odległość do przebycia przez robota pomiędzy przerwami na szukanie znaczników i przeszkód */
	public static final float robotStepDistance = 1000;
	public static final int framesPerRotate = 3;

	public static final int rounds = 2;
	
	public static final int maxSpeed = 60;
	public static final int maxDirection = 60;


	/* -------------- INNE ------------------------------ */

	/* standardowy sleep spowolniający pętle */
	public static final int loopSleep = 10;
	
	/* odświeżanie widgetu baterii */
	public static final int batterySleep = 1000;

	public static final int PIDPeriod = 100;
}
