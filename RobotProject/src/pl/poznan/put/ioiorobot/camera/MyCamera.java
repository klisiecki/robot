package pl.poznan.put.ioiorobot.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import pl.poznan.put.ioiorobot.mapping.Pattern;
import pl.poznan.put.ioiorobot.utils.Config;
import pl.poznan.put.ioiorobot.utils.DAO;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

/**
 * Główna klasa przetwarzająca obraz
 */
public class MyCamera implements CvCameraViewListener2 {
	
	private boolean showDebug = true;
	private int framesToProcess;
	private Mode mode = Mode.CAMERA_ONLY;
	private MyJavaCameraView cameraView;
	private BaseLoaderCallback loaderCallback;
	private Context context;
	private PatternFoundListener patternFoundListener;
	private Mat imgRbgaRaw;
	
	public void setPatternFoundListener(PatternFoundListener patternFoundListener) {
		this.patternFoundListener = patternFoundListener;
	}

	public MyCamera(final CameraBridgeViewBase cameraView, final Context context) {
		super();
		this.cameraView = (MyJavaCameraView) cameraView;
		this.context = context;

		cameraView.setCvCameraViewListener(this);

		loaderCallback = new BaseLoaderCallback(context) {
			@Override
			public void onManagerConnected(int status) {
				cameraView.setCameraIndex(0);
				cameraView.enableFpsMeter();
				cameraView.enableView();
			}
		};
	}

	public void resume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, loaderCallback);
		
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Camera mCamera = cameraView.getCamera();
		Camera.Parameters param = mCamera.getParameters();
		param.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		mCamera.setParameters(param);
	}

	@Override
	public void onCameraViewStopped() {
	}
	
	
	//temp begin
	private boolean mocking = false;
	
	public void setMocking(boolean mocking) {
		this.mocking = mocking;
	}
	
	private boolean requestSaveMock = false;
	
	public void saveMock() {
		requestSaveMock = true;
	}
	
	//temp end

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if(requestSaveMock) {
			DAO.saveMock(inputFrame.rgba());
			requestSaveMock = false;
		}
		
		if (mode == Mode.PROCESSING || framesToProcess > 0) {
			framesToProcess--;
			return processFrame(inputFrame);
		} else if (mode == Mode.MOCK) {
			if (patternFoundListener != null) {
				patternFoundListener.onPatternFound(new Pattern(inputFrame.gray(), 0));
			}
			mode = Mode.CAMERA_ONLY;
			return inputFrame.rgba();
		} else {
			return inputFrame.rgba();
		}
	}

	/**
	 * Główna funkcja prztwarzająca obraz
	 * 
	 * @param inputFrame
	 * @return
	 */
	private Mat processFrame(CvCameraViewFrame inputFrame) {
		// pobranie klatki w RGB
		Mat imgRgba = inputFrame.rgba();
		
		if (mocking && DAO.getMock() != null) {
			imgRgba = DAO.getMock();
		}
		
		imgRbgaRaw = new Mat();
		imgRgba.copyTo(imgRbgaRaw);

		// filtrowanie klatki wg ustalonego koloru
		Mat mask = new Mat();
		imgRgba.copyTo(mask);
		Imgproc.cvtColor(mask, mask, Imgproc.COLOR_RGB2HSV, 3);
		Core.inRange(mask, Config.minColor, Config.maxColor, mask);
		
		Mat maskedImage = new Mat();
		imgRgba.copyTo(maskedImage, mask);

		Mat maskedImageGray = new Mat();
		Imgproc.cvtColor(maskedImage, maskedImageGray, Imgproc.COLOR_RGB2GRAY);

		Mat maskedImageGrayThresholded = new Mat();
		Imgproc.adaptiveThreshold(maskedImageGray, maskedImageGrayThresholded, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY_INV, Config.thresholdBlockSize, Config.thresholdMC); // blockSize = 9, mC = 7;
		

		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(maskedImageGrayThresholded, contours, new Mat(), Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// pętla dla wszystkich konturów zawierających poszukiwany kolor
		for (MatOfPoint cnt : contours) {
			// Pomijanie małych obiektów
			int threshold = (int) (Config.screenSize.x * Config.screenSize.y * Config.thresholdFactor / 100);
			if (Imgproc.contourArea(cnt) > threshold) {
				Rect r = Imgproc.boundingRect(cnt);
				Mat subMat = imgRgba.submat(r);
				processMat(subMat);
				
				if(showDebug) {
					drawContour(imgRgba, cnt, new Scalar(204,0,204));
					// Rysowanie zielonego prostokąta wokół analizowanych fragmentów
					Core.rectangle(imgRgba, r.tl(), r.br(), new Scalar(0, 200, 0), 3);
				}
				
			}
		}

		return imgRgba;
	}

	/**
	 * Funkcja przeszukująca fragment obrazu w celu znalezienia markerów
	 */
	private void processMat(Mat imgRgba) {

		Mat imgGray = new Mat();
		Imgproc.cvtColor(imgRgba, imgGray, Imgproc.COLOR_RGB2GRAY);

		Mat grayThresholded = new Mat();
		Imgproc.adaptiveThreshold(imgGray, grayThresholded, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY_INV, Config.thresholdBlockSize, Config.thresholdMC);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(grayThresholded, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		List<MatOfPoint> contoursSorted = new ArrayList<MatOfPoint>();

		for (MatOfPoint cnt : contours) {
			// Pomijanie małych obiektów
			int threshold = (int) (Config.screenSize.x * Config.screenSize.y * Config.thresholdFactor / 100);
			if (Imgproc.contourArea(cnt) > threshold)
				contoursSorted.add(cnt);
		}

		Collections.sort(contoursSorted, new Comparator<MatOfPoint>() {
			@Override
			public int compare(MatOfPoint lhs, MatOfPoint rhs) {
				return Imgproc.contourArea(lhs) > Imgproc.contourArea(rhs) ? -1 : 1;
			}
		});

		int contoursProcessed = 0;

		// Przeglądanie znalezionych konturów od największych do najmniejszych
		for (MatOfPoint cnt : contoursSorted) {
			if(showDebug) drawContour(imgRgba, cnt, new Scalar(0,0,255));
			Mat fragment = cutContour(imgGray, imgRgba, cnt);

			if (warpFragmentFromContour(imgRgba, cnt, fragment)) {
				contoursProcessed++;
				Pattern pattern = new Pattern(fragment, calculateCameraAngle(imgRbgaRaw, cnt));

				if (patternFoundListener != null) {
					patternFoundListener.onPatternFound(pattern);
				}
			}
			if (contoursProcessed == Config.maxContoursProcessed) {
				break;
			}
		}
	}

	/**
	 * Metoda obliczająca rzeczywisty kąt, o jaki znaleziony kontur jest
	 * odchylony od osi aparatu (bazując na znanym kącie widzenia kamery)
	 * 
	 * @return kąt w radianach, wartości ujemne na lewo, dodatnie na prawo
	 */
	private float calculateCameraAngle(Mat image, MatOfPoint cnt) {
		Rect r = Imgproc.boundingRect(cnt);
		int center = r.x + r.width / 2; //pozycja konturu w submacie
		
		Size wholeSize = new Size();
		Point offset = new Point();
		image.locateROI(wholeSize, offset);
		
		float result = ((float) center + (float)offset.x) / (float)wholeSize.width * Config.cameraViewAngle - Config.cameraViewAngle / 2;
		return result;
	}

	/**
	 * Funkcja tworzy czworokąt z podanego konturu i przekształca do kwadratu
	 * 
	 * @return true gdy udało się uzyskać kwadrat
	 */
	private boolean warpFragmentFromContour(Mat resultImage, MatOfPoint cnt, Mat fragment) {
		List<Point> points = getRectanglePointsFromContour(cnt);

		// jeżlii znaleziony kontur przypomina kwadrat oraz udało się
		// posortować jego wierzchołki
		if (CameraHelper.couldBeSquare(points) && CameraHelper.sortCorners(points)) {
			Rect rect = Imgproc.boundingRect(cnt);

			Point fragmentTL = rect.tl();
			for (Point p : points) {
				// rysujemy
				if(showDebug) Core.circle(resultImage, p, 10, new Scalar(255, 255, 255), 10);

				// przesuwamy do współrzędnych fragmentu (zmiany są wprowadzane
				// w tablicy points!)
				p.x -= fragmentTL.x;
				p.y -= fragmentTL.y;
			}

			// To musi być tak jak jest, w 2 linijkach, nie udoskonalać!
			Mat fragmentTmp = warp(fragment, points.get(0), points.get(3), points.get(2), points.get(1));
			fragmentTmp.copyTo(fragment);

			return true;
		}
		return false;
	}

	/**
	 * Przekształca każdy kontur w maksymalnie czworokąt, poprzez usuwanie
	 * wierzchołków przy największych kątach
	 * 
	 * @return lista maksymalnie 4 punktów
	 */
	private List<Point> getRectanglePointsFromContour(MatOfPoint cnt) {
		MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		double epsilon = 0.01 * Imgproc.arcLength(cnt2f, true);
		Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);

		List<Point> points = new LinkedList<Point>(approxCurve.toList());

		while (points.size() > 4) {
			int maxIndex = 1;
			double maxAngle = 0;
			for (int i = 1; i <= points.size(); i++) {
				Point p0 = points.get(i - 1);
				Point p1 = points.get(i % points.size());
				Point p2 = points.get((i + 1) % points.size());
				double angle = CameraHelper.getAngle(p0, p1, p2);
				if (angle > maxAngle) {
					maxAngle = angle;
					maxIndex = i % points.size();
				}
			}
			points.remove(maxIndex);
		}
		return points;
	}

	/**
	 * @param baseImage
	 * @param resultImage
	 * @param cnt
	 * @return
	 */
	private Mat cutContour(Mat baseImage, Mat resultImage, MatOfPoint cnt) {
		Rect rect = Imgproc.boundingRect(cnt);
		if(showDebug) Core.rectangle(resultImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
				new Scalar(255, 0, 0), 3);
		Mat fragment = baseImage.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
		return fragment;
	}

	private void drawContour(Mat resultImage, MatOfPoint cnt, Scalar color) {
		MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		double epsilon = 0.01 * Imgproc.arcLength(cnt2f, true);
		Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);

		List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
		cntList.add(cnt);
		Imgproc.drawContours(resultImage, cntList, 0, color, 3);

		for (Point p : approxCurve.toList()) {
//			Core.circle(resultImage, p, 5, new Scalar(255, 255, 0), 5);
		}
	}

	/**
	 * Metoda wycinająca obszar wyznaczony przez podane punkty i rozciągając ten
	 * fragment na kwadratowy obraz
	 */
	public static Mat warp(Mat inputMat, Point p1, Point p2, Point p3, Point p4) {
		int resultWidth, resultHeight;
		resultWidth = resultHeight = Config.patternSize;

		Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

		List<Point> source = new ArrayList<Point>();
		source.add(new Point(p1.x, p1.y));
		source.add(new Point(p2.x, p2.y));
		source.add(new Point(p3.x, p3.y));
		source.add(new Point(p4.x, p4.y));
		Mat startM = Converters.vector_Point2f_to_Mat(source);

		List<Point> dest = new ArrayList<Point>();
		dest.add(new Point(0, 0));
		dest.add(new Point(0, resultHeight));
		dest.add(new Point(resultWidth, resultHeight));
		dest.add(new Point(resultWidth, 0));
		Mat endM = Converters.vector_Point2f_to_Mat(dest);

		Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

		Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight),
				Imgproc.INTER_CUBIC);

		return outputMat;
	}
	
	public interface PatternFoundListener {
		void onPatternFound(Pattern pattern);
	}

	public enum Mode {
		PROCESSING, CAMERA_ONLY, MOCK
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public void switchDebug() {
		this.showDebug = !showDebug;
	}
	
	public void setFramesToProcess(int framesToProcess) {
		this.framesToProcess = framesToProcess;
	}
	
	public boolean isReady() {
		return framesToProcess == 0;
	}
}
