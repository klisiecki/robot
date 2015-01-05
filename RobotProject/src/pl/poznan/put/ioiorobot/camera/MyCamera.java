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

import pl.poznan.put.ioiorobot.R;
import pl.poznan.put.ioiorobot.mapobjects.Pattern;
import pl.poznan.put.ioiorobot.utils.C;
import android.app.Activity;
import android.content.Context;
import android.widget.SeekBar;

/**
 * Główna klasa przetwarzająca obraz
 */
public class MyCamera implements CvCameraViewListener2 {

	public interface PatternFoundListener {
		void onPatternFound(Pattern pattern);
	}

	public enum Mode {
		PROCESSING, CAMERA_ONLY, MOCK
	}

	private Mode mode = Mode.CAMERA_ONLY;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	private CameraBridgeViewBase cameraView;
	private BaseLoaderCallback loaderCallback;
	private Context context;
	private PatternFoundListener patternFoundListener;

	private SeekBar seekBar1;
	private SeekBar seekBar2;
	private SeekBar seekBar3;

	public void setPatternFoundListener(PatternFoundListener patternFoundListener) {
		this.patternFoundListener = patternFoundListener;
	}

	public MyCamera(final CameraBridgeViewBase cameraView, final Context context) {
		super();
		this.mode = Mode.CAMERA_ONLY;
		this.cameraView = cameraView;
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

		seekBar1 = (SeekBar) ((Activity) context).findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar) ((Activity) context).findViewById(R.id.seekBar2);
		seekBar3 = (SeekBar) ((Activity) context).findViewById(R.id.seekBar3);
	}

	public void resume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, loaderCallback);
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO rozmiar można brać stąd
	}

	@Override
	public void onCameraViewStopped() {
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (mode == Mode.PROCESSING) {
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
		// TODO patterny z oryginalnego obrazu (bez nałożonych kresek i ramek)
		// pobranie klatki w RGB
		Mat imgRgba = inputFrame.rgba();

		// filtrowanie klatki wg ustalonego koloru
		Mat mask = new Mat();
		imgRgba.copyTo(mask);
		Imgproc.cvtColor(mask, mask, Imgproc.COLOR_RGB2HSV, 3);
		Core.inRange(mask, C.minColor, C.maxColor, mask);
		

		Mat maskedImage = new Mat();
		imgRgba.copyTo(maskedImage, mask);

		Mat maskedImageGray = new Mat();
		Imgproc.cvtColor(maskedImage, maskedImageGray, Imgproc.COLOR_RGB2GRAY);


		Mat maskedImageGrayThresholded = new Mat();
		Imgproc.adaptiveThreshold(maskedImageGray, maskedImageGrayThresholded, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY_INV, 9, 7); // blockSize = 9, mC = 7;

		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(maskedImageGrayThresholded, contours, new Mat(), Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// pętla dla wszystkich konturów zawierających poszukiwany kolor
		for (MatOfPoint cnt : contours) {
			// Pomijanie małych obiektów
			int threshold = (int) (C.screenSize.x * C.screenSize.y * C.thresholdFactor / 100);
			if (Imgproc.contourArea(cnt) > threshold) {

				Rect r = Imgproc.boundingRect(cnt);
				// Rysowanie różowego prostokąta wokół analizowanych fragmentów
				Core.rectangle(imgRgba, r.tl(), r.br(), new Scalar(255, 0, 255), 5);

				Mat subMat = imgRgba.submat(r);
				processMat(subMat);
			}
		}

		return imgRgba;
	}

	/**
	 * Funkcja przeszukująca fragment obrazu w celu znalezienia markerów
	 * 
	 * @param imgRgba
	 */
	private void processMat(Mat imgRgba) {

		Mat imgGray = new Mat();
		Imgproc.cvtColor(imgRgba, imgGray, Imgproc.COLOR_RGB2GRAY);

		Mat grayThresholded = new Mat();
		Imgproc.adaptiveThreshold(imgGray, grayThresholded, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
				Imgproc.THRESH_BINARY_INV, 9, 7);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(grayThresholded, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		List<MatOfPoint> contoursSorted = new ArrayList<MatOfPoint>();

		for (MatOfPoint cnt : contours) {
			// Pomijanie małych obiektów
			int threshold = (int) (C.screenSize.x * C.screenSize.y * C.thresholdFactor / 100);
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
			drawContour(imgRgba, cnt);
			Mat fragment = cutContour(imgGray, imgRgba, cnt);

			if (warpFragmentFromContour(imgRgba, cnt, fragment)) {
				contoursProcessed++;
				Pattern pattern = new Pattern(fragment, calculateCameraAngle(imgRgba, cnt));

				if (patternFoundListener != null) {
					patternFoundListener.onPatternFound(pattern);
				}
			}
			if (contoursProcessed == C.maxContoursProcessed) {
				break;
			}
		}
	}

	/**
	 * Metoda obliczająca rzeczywisty kąt, o jaki znaleziony kontur jest
	 * odchylony od osi aparatu (bazując na znanym kącie widzenia kamery)
	 * 
	 * @param image
	 * @param cnt
	 * @return kąt w radianach, wartości ujemne na lewo, dodatnie na prawo
	 */
	private float calculateCameraAngle(Mat image, MatOfPoint cnt) {
		Rect r = Imgproc.boundingRect(cnt);
		int center = r.x + r.width / 2;
		float result = (float) (center / image.width() * C.cameraViewAngle - C.cameraViewAngle / 2);
		return result;
	}

	/**
	 * Funkcja tworzy czworokąt z podanego konturu i przekształca do kwadratu
	 * 
	 * @param resultImage
	 * @param cnt
	 * @param fragment
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
				Core.circle(resultImage, p, 5, new Scalar(0, 255, 0), 5);

				// przesuwamy do współrzędnych fragmentu (zmiany są wprowadzane
				// w tablicy points!)
				p.x -= fragmentTL.x;
				p.y -= fragmentTL.y;
			}

			// To musi być tak jak jest, w 2 linijkach, nie udoskonalać!
			Mat fragmentTmp = warp(fragment, points.get(0), points.get(3), points.get(2), points.get(1));
			fragmentTmp.copyTo(fragment);

			// Imgproc.cvtColor(fragment, fragment, Imgproc.COLOR_GRAY2RGBA);
			// showFragment2(resultImage, fragment, slot, MyConfig.patternSize);
			// Imgproc.cvtColor(fragment, fragment, Imgproc.COLOR_RGB2GRAY);

			return true;
		}
		return false;
	}

	/**
	 * Przekształca każdy kontur w maksymalnie czworokąt, poprzez usuwanie
	 * wierzchołków przy największych kątach
	 * 
	 * @param cnt
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
	 *            //TODO usunąc
	 * @param cnt
	 * @return
	 */
	private Mat cutContour(Mat baseImage, Mat resultImage, MatOfPoint cnt) {
		Rect rect = Imgproc.boundingRect(cnt);
		Core.rectangle(resultImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
				new Scalar(255, 0, 0), 3);
		Mat fragment = baseImage.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
		return fragment;
	}

	private void drawContour(Mat resultImage, MatOfPoint cnt) {
		MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		double epsilon = 0.01 * Imgproc.arcLength(cnt2f, true);
		Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);

		List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
		cntList.add(cnt);
		Imgproc.drawContours(resultImage, cntList, 0, new Scalar(0, 0, 255), 3);

		for (Point p : approxCurve.toList()) {
			Core.circle(resultImage, p, 5, new Scalar(255, 255, 0), 5);
		}
	}

	/**
	 * Metoda wycinająca obszar wyznaczony przez podane punkty i rozciągając ten
	 * fragment na kwadratowy obraz
	 * 
	 * @param inputMat
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 * @return wycięty i rozciągnięty fragment
	 */
	public static Mat warp(Mat inputMat, Point p1, Point p2, Point p3, Point p4) {
		int resultWidth, resultHeight;
		resultWidth = resultHeight = C.patternSize;

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
}
