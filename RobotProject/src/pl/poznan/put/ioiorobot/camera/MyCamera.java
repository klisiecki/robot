package pl.poznan.put.ioiorobot.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import pl.poznan.put.ioiorobot.utils.DAO;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.SeekBar;

/**
 * @author karol
 *
 */
public class MyCamera implements CvCameraViewListener2 {

	private CameraBridgeViewBase cameraView;
	private BaseLoaderCallback loaderCallback;
	private Context context;

	private SeekBar seekBar1;
	private SeekBar seekBar2;
	private SeekBar seekBar3;

	private int xTargetPosition;

	public int getxTargetPosition() {
		return xTargetPosition;
	}

	public MyCamera(final CameraBridgeViewBase cameraView, final Context context) {
		super();
		DAO.writeToExternal("test", "testSD.txt");
		this.cameraView = cameraView;
		this.context = context;

		cameraView.setCvCameraViewListener(this);

		loaderCallback = new BaseLoaderCallback(context) {
			@Override
			public void onManagerConnected(int status) {
				cameraView.setCameraIndex(0);
				cameraView.enableFpsMeter();
				cameraView.enableView();

				/*
				 * // // Żadna próba wymuszenia większej rozdzielczości nie
				 * pomogła :/ WindowManager wm = (WindowManager)
				 * context.getSystemService(Context.WINDOW_SERVICE); Display
				 * display = wm.getDefaultDisplay(); DisplayMetrics metrics =
				 * new DisplayMetrics(); display.getMetrics(metrics); int width
				 * = metrics.widthPixels; int height = metrics.heightPixels;
				 * Log.d("robot", "camera size= " + width + " x " + height);
				 * 
				 * cameraView.setMaxFrameSize(800, 600);
				 */

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
		// Log.d("robot", "onCameraViewStarted " + width + " x " + height);
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// return finColorShapes(inputFrame);
		return findRegularShapes(inputFrame);
		// return inputFrame.rgba();
		// return inputFrame.rgba();
		// Zwrócenie obrazu w innym rozmiarze niż wejściowy powoduje brak obrazu
	}

	private Mat findColorShapes(CvCameraViewFrame inputFrame) {
		Mat mat = inputFrame.rgba();
		Mat dst = new Mat();
		Mat result = new Mat();
		Imgproc.cvtColor(mat, dst, Imgproc.COLOR_RGB2HSV, 3);

		Log.d("robot", CameraUtils.getPixelColor(dst, 100, 100) + "");
		getYellowMat(dst, dst);
		Point center = detectObject(mat, dst, "C", result);
		xTargetPosition = (int) (((double) center.x / (double) mat.width()) * 200.0 - 100.0);
		return result;
	}

	public static void getBlueMat(Mat src, Mat dst) {
		Core.inRange(src, new Scalar(100, 100, 100), new Scalar(120, 255, 255), dst);
	}

	public static void getWhiteMat(Mat src, Mat dst) {
		Core.inRange(src, new Scalar(0, 0, 70), new Scalar(255, 100, 255), dst);
	}

	public void getYellowMat(Mat src, Mat dst) {
		// Core.inRange(src, new Scalar(seekBar2.getProgress(),
		// seekBar1.getProgress(), 60),
		// new Scalar(seekBar3.getProgress(), 255, 255), dst);

		// Core.inRange(src, new Scalar(20, 10, 10), new Scalar(45, 255, 255),
		// dst);
		Core.inRange(src, new Scalar(20, 80, 64), new Scalar(30, 255, 255), dst);
	}

	public static Point detectObject(Mat src, Mat image, String text, Mat dst) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		src.copyTo(dst);

		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		int k = getBiggestContourIndex(contours);
		Rect bounds = setContourRect(contours, k);

		Point center = CameraUtils.getCenter(bounds.tl(), bounds.br());
		Core.rectangle(dst, bounds.tl(), bounds.br(), new Scalar(255, 255, 0), 2, 8, 0);

		return center;
	}

	public static int getBiggestContourIndex(List<MatOfPoint> contours) {
		double maxArea = 0;
		Iterator<MatOfPoint> each = contours.iterator();
		int j = 0;
		int k = -1;
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			double area = Imgproc.contourArea(wrapper);
			if (area > maxArea) {
				maxArea = area;
				k = j;
			}
			j++;
		}
		return k;
	}

	public static Rect setContourRect(List<MatOfPoint> contours, int k) {
		Rect boundRect = new Rect();
		Iterator<MatOfPoint> each = contours.iterator();
		int j = 0;
		while (each.hasNext()) {
			MatOfPoint wrapper = each.next();
			if (j == k) {
				return Imgproc.boundingRect(wrapper);
			}
			j++;
		}
		return boundRect;
	}

	private Mat findRegularShapes(CvCameraViewFrame inputFrame) {
		Mat imgRgba = inputFrame.rgba();
		Mat imgGray = inputFrame.gray();
		Mat grayCopy = new Mat();
		imgGray.copyTo(grayCopy);

		// Log.d("robot", "seekBar1 = " + seekBar1.getProgress() +
		// "      seekBar2 = " + seekBar2.getProgress()
		// + "      seekBar3 = " + seekBar3.getProgress());

		Mat mask = new Mat();
		imgRgba.copyTo(mask);
		Imgproc.cvtColor(mask, mask, Imgproc.COLOR_RGB2HSV, 3);
		// getYellowMat(mask, mask);
		getWhiteMat(mask, mask);

//		Mat image = new Mat();
//		baseImgRgba.copyTo(image, mask);
//
//		/* Imgproc.cvtColor(image, image, Imgproc.COLOR_HSV2RGB, 4); */
//		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);

		// return image;

		/* Imgproc.threshold(image, image, 127.0, 255.0, Imgproc.THRESH_BINARY); */

		int blockSize = 9; // seekBar1.getProgress()*2 + 3
		int C = 7; // seekBar2.getProgress()
		Imgproc.adaptiveThreshold(grayCopy, grayCopy, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,
				blockSize, C);

		/*
		 * int size = seekBar3.getProgress()+1;
		 * 
		 * if(seekBar1.getProgress() > 50) { Imgproc.erode(image, image,
		 * Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new
		 * Size(size,size))); } if(seekBar2.getProgress() > 50) {
		 * Imgproc.dilate(image, image,
		 * Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new
		 * Size(size,size))); }
		 * 
		 * 
		 * if(seekBar1.getProgress() > 50) { Mat kernel =
		 * Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new
		 * Size(size,size)); Imgproc.morphologyEx(image, image,
		 * Imgproc.MORPH_CLOSE, kernel); }
		 * 
		 * 
		 * Imgproc.Canny(image, image, seekBar1.getProgress(),
		 * 3*seekBar1.getProgress());
		 */

		// return image;

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(grayCopy, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Mat resultImage = new Mat();
		imgRgba.copyTo(resultImage);

		MatOfPoint maxCnt = null;
		int slotNr = 0;

		List<MatOfPoint> contours2 = new ArrayList<MatOfPoint>();

		for (MatOfPoint cnt : contours) {

			// Pomijanie małych obiektów
			int threshold = 500; // seekBar1.getProgress()*10;
			if (Imgproc.contourArea(cnt) > threshold)
				contours2.add(cnt);
		}

		Collections.sort(contours2, new Comparator<MatOfPoint>() {

			@Override
			public int compare(MatOfPoint lhs, MatOfPoint rhs) {
				return Imgproc.contourArea(lhs) > Imgproc.contourArea(rhs) ? -1 : 1;
			}
		});

		for (MatOfPoint cnt : contours2) {
			drawContour(resultImage, cnt);
			Mat fragment = cutFragment(imgGray, resultImage, cnt, true);

			if (maxCnt == null || Imgproc.contourArea(cnt) > Imgproc.contourArea(maxCnt)) {
				maxCnt = new MatOfPoint(cnt);
			}

			if (warpFragmentFromContour(resultImage, cnt, fragment, slotNr) && slotNr < 6) {
				slotNr++;
				int[][] data = ImageProcessing.getPattern(fragment);
				//DAO.saveItemAsync(ImageProcessing.getPattern(fragment), "pattern"+slotNr);
				String temp = ImageProcessing.tabToString(data);
				DAO.writeToExternal(temp, "array7."+slotNr);
			}
		}

		// Rysowanie największego znalezionego obszaru
//		if (maxCnt != null) {
//			Mat fragment = cutFragment(baseImgRgba, resultImage, maxCnt, false);
//			CameraUtils.drawBounds(resultImage, maxCnt, new Scalar(255, 0, 0), 3);
//			// fragment = fragment.clone();
//			if (fragment.width() != 0 && fragment.height() != 0) {
//
//				// findCornerHarris(fragment);
//				// findCornerHoughTransform(fragment);
//
//				// warpFragmentFromContour(resultImage, maxCnt, fragment);
//			}
//		}

		return resultImage;
	}

	/**
	 * Funkcja tworzy czworokąt z podanego konturu, przekształca do kwadratu i
	 * wyświetla na górze ekranu
	 * 
	 * @param resultImage
	 * @param maxCnt
	 * @param fragment
	 */
	private boolean warpFragmentFromContour(Mat resultImage, MatOfPoint maxCnt, Mat fragment, int slot) {
		List<Point> points = getRectanglePointsFromContour(maxCnt);
		if (couldBeRectangle(points)) {
			Rect rect = Imgproc.boundingRect(maxCnt);

			Point fragmentTL = rect.tl();
			try {
				// TODO czasem sypie, naprawić
				sortCorners(points);
			} catch (Exception e) {
			}
			for (Point p : points) {
				// rysujemy
				Core.circle(resultImage, p, 5, new Scalar(0, 255, 0), 5);
				// przesuwamy do współrzędnych fragmentu
				p.x -= fragmentTL.x;
				p.y -= fragmentTL.y;
			}
			Mat fragment2 = warp(fragment, points.get(0), points.get(3), points.get(2), points.get(1));
			fragment2.copyTo(fragment);
			Imgproc.cvtColor(fragment, fragment, Imgproc.COLOR_GRAY2RGBA);
			showFragment2(resultImage, fragment, slot, cameraView.getHeight() / 4);
			Imgproc.cvtColor(fragment, fragment, Imgproc.COLOR_RGB2GRAY);
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
				double angle = getAngle(p0, p1, p2);
				if (angle > maxAngle) {
					maxAngle = angle;
					maxIndex = i % points.size();
				}
			}
			points.remove(maxIndex);
		}
		return points;
	}
	
	private boolean couldBeRectangle(List<Point> points) {
		if(points.size() != 4) {
			return false;
		}
		
		int top = getDistance(points.get(0), points.get(1));
		int bottom = getDistance(points.get(2), points.get(3));
		int left = getDistance(points.get(0), points.get(3));
		int right = getDistance(points.get(1), points.get(2));
		
		Log.d("robot", top + " vs " + bottom + "     " + left + "  vs " + right);
		
		if(!areEqual(top, bottom) || !areEqual(left, right)) {
			return false;
		}
		else {
			return true;
		}
	}
	
	private int getDistance(Point a, Point b){
		return (int) Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
	}
	
	private boolean areEqual(int a, int b){
		int acceptedError = 30;	
		return Math.abs((double)(a-b) / a) < (double)acceptedError/100.0; 
	}

	private void findCornerHoughTransform(Mat fragment) {
		// http://opencv-code.com/tutorials/automatic-perspective-correction-for-quadrilateral-objects/

		Mat image = fragment.clone();

		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY, 1);

		Imgproc.Canny(image, image, 100, 100);

		Mat lines = new Mat();
		int threshold = 100;
		int minLineSize = 15;
		int lineGap = 10;

		Imgproc.HoughLinesP(image, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);

		// Rysowanie znalezionych linii
		for (int x = 0; x < lines.cols(); x++) {
			double[] vec = lines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			Point start = new Point(x1, y1);
			Point end = new Point(x2, y2);

			Core.line(fragment, start, end, new Scalar(90, 255, 90), 3);
		}

		List<Point> cornersArray = new ArrayList<Point>();

		for (int i = 0; i < lines.cols(); i++) {
			for (int j = i + 1; j < lines.cols(); j++) {

				double[] a = lines.get(0, i);
				double[] b = lines.get(0, j);

				int x1 = (int) a[0], y1 = (int) a[1], x2 = (int) a[2], y2 = (int) a[3];
				int x3 = (int) b[0], y3 = (int) b[1], x4 = (int) b[2], y4 = (int) b[3];

				Point pt = new Point();
				float d = ((float) (x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
				if (d != 0) {
					pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
					pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
				} else {
					pt.x = -1;
					pt.y = -1;
				}

				if (pt.x >= 0 && pt.y >= 0) {
					Core.circle(fragment, pt, 5, new Scalar(255, 90, 200), 5);
				}

				cornersArray.add(pt);

			}
		}

		if (cornersArray.size() != 0) {
			MatOfPoint corners = new MatOfPoint();
			corners.fromList(cornersArray);

			MatOfPoint2f cnt2f = new MatOfPoint2f(corners.toArray());
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			double epsilon = 0.02 * Imgproc.arcLength(cnt2f, true);
			Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);

			if (approxCurve.toList().size() == 4) {

				// Get mass center
				Point center = new Point(0.0, 0.0);
				for (int i = 0; i < cornersArray.size(); i++) {
					center.x += cornersArray.get(i).x;
					center.y += cornersArray.get(i).y;
				}
				center = new Point(center.x * (double) cornersArray.size(), center.x * (double) cornersArray.size());

				if (cornersArray.size() == 4)
					sortCorners(cornersArray, center);

				Core.circle(fragment, cornersArray.get(0), 15, new Scalar(100, 100, 100), 5);
				Core.circle(fragment, cornersArray.get(1), 15, new Scalar(150, 150, 150), 5);
				Core.circle(fragment, cornersArray.get(2), 15, new Scalar(200, 200, 200), 5);
				Core.circle(fragment, cornersArray.get(3), 15, new Scalar(250, 250, 250), 5);

				fragment = warp(fragment, cornersArray.get(0), cornersArray.get(1), cornersArray.get(2),
						cornersArray.get(3));

				// fragment = warp(fragment, new Point(fragment.width()*2/10,
				// 0),
				// new Point(0, fragment.height()),
				// new Point(fragment.width(), fragment.height()),
				// new Point(fragment.width()*8/10, 0) );

			}

		}

		// Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGB, 4);

	}

	/**
	 * Prosta wersja, nie działa dla każdego czworokątu ale powinno wystarczyć
	 * 
	 * @param cornersArray
	 */
	private void sortCorners(List<Point> cornersArray) {
		double x = 0, y = 0;
		for (Point p : cornersArray) {
			x += p.x;
			y += p.y;
		}

		Point center = new Point(x / 4, y / 4);
		sortCorners(cornersArray, center);
	}

	void sortCorners(List<Point> cornersArray, Point center) {
		List<Point> top = new ArrayList<Point>();
		List<Point> bot = new ArrayList<Point>();

		for (int i = 0; i < cornersArray.size(); i++) {
			if (cornersArray.get(i).y < center.y)
				top.add(cornersArray.get(i));
			else
				bot.add(cornersArray.get(i));
		}

		Point tl = top.get(0).x > top.get(1).x ? top.get(1) : top.get(0);
		Point tr = top.get(0).x > top.get(1).x ? top.get(0) : top.get(1);
		Point bl = bot.get(0).x > bot.get(1).x ? bot.get(1) : bot.get(0);
		Point br = bot.get(0).x > bot.get(1).x ? bot.get(0) : bot.get(1);

		cornersArray.clear();
		cornersArray.add(tl);
		cornersArray.add(tr);
		cornersArray.add(br);
		cornersArray.add(bl);
	}

	private void showFragment2(Mat resultImage, Mat fragment, int position, int size) {
		Mat slot = resultImage.submat(0, size, position * size, position * size + size);
		Imgproc.resize(fragment, fragment, slot.size());
		fragment.copyTo(slot);
		Core.rectangle(slot, new Point(0, 0), new Point(slot.width(), slot.height()), new Scalar(0, 0, 0), 5);
	}

	private void showFragment(Mat resultImage, Mat fragment, int height) {
		int slotHeight = Math.min(height, resultImage.height());
		int slotWidth = Math.min(slotHeight * fragment.width() / fragment.height(), resultImage.width());
		Mat slot = resultImage.submat(0, slotHeight, 0, slotWidth);
		Imgproc.resize(fragment, slot, slot.size());
		Core.rectangle(slot, new Point(0, 0), new Point(slot.width(), slot.height()), new Scalar(0, 0, 0), 20);
	}

	private void findCornerHarris(Mat fragment) {
		Mat fragmentGray = new Mat();
		fragment.copyTo(fragmentGray);

		Mat cornerMap = new Mat();
		fragmentGray.copyTo(cornerMap);

		Imgproc.cvtColor(fragment, fragmentGray, Imgproc.COLOR_RGB2GRAY);

		int blockSize1 = 10; // 2
		int apertureSize = 3; // 3
		double k = 0.04; // 0.04

		Imgproc.cornerHarris(fragmentGray, cornerMap, blockSize1, apertureSize, k, Imgproc.BORDER_DEFAULT);

		for (int y = 0; y < fragment.height(); y++)
			for (int x = 0; x < fragment.width(); x++) {
				double[] harris = cornerMap.get(y, x);
				if (harris[0] > 10e-04) {
					Core.circle(fragment, new Point(x, y), 5, new Scalar(255, 90, 200), 5);
				}
			}

		// Imgproc.cvtColor(cornerMap, cornerMap, Imgproc.COLOR_GRAY2RGB, 4);
		// fragment = cornerMap;

	}

	private Mat cutFragment(Mat baseImage, Mat resultImage, MatOfPoint cnt, boolean drawRect) {
		Rect rect = Imgproc.boundingRect(cnt);
		CameraUtils.drawBounds(resultImage, cnt, new Scalar(255, 0, 0), 1);
		Mat fragment = baseImage.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
		return fragment;
	}

	private double getAngle(Point p0, Point p1, Point p2) {
		return Math.toDegrees(Math.atan2(p0.x - p1.x, p0.y - p1.y) - Math.atan2(p2.x - p1.x, p2.y - p1.y));
	}

	private double angleBetween(Point center, Point current, Point previous) {

		return Math.toDegrees(Math.atan2(current.x - center.x, current.y - center.y)
				- Math.atan2(previous.x - center.x, previous.y - center.y));
	}

	private void drawContour(Mat resultImage, MatOfPoint cnt) {

		MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		double epsilon = 0.01 * Imgproc.arcLength(cnt2f, true);
		Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);

		List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
		cntList.add(cnt);
		Imgproc.drawContours(resultImage, cntList, 0, new Scalar(0, 0, 255), 1);

		for (Point p : approxCurve.toList()) {
			Core.circle(resultImage, p, 5, new Scalar(255, 255, 0), 5);
		}
	}

	public static Mat warp(Mat inputMat, Point p1, Point p2, Point p3, Point p4) {
		int resultWidth = 500;
		int resultHeight = 500;

		Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

		Point ocvPIn1 = new Point(p1.x, p1.y);
		Point ocvPIn2 = new Point(p2.x, p2.y);
		Point ocvPIn3 = new Point(p3.x, p3.y);
		Point ocvPIn4 = new Point(p4.x, p4.y);
		List<Point> source = new ArrayList<Point>();
		source.add(ocvPIn1);
		source.add(ocvPIn2);
		source.add(ocvPIn3);
		source.add(ocvPIn4);
		Mat startM = Converters.vector_Point2f_to_Mat(source);

		Point ocvPOut1 = new Point(0, 0);
		Point ocvPOut2 = new Point(0, resultHeight);
		Point ocvPOut3 = new Point(resultWidth, resultHeight);
		Point ocvPOut4 = new Point(resultWidth, 0);
		List<Point> dest = new ArrayList<Point>();
		dest.add(ocvPOut1);
		dest.add(ocvPOut2);
		dest.add(ocvPOut3);
		dest.add(ocvPOut4);
		Mat endM = Converters.vector_Point2f_to_Mat(dest);

		Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

		Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight),
				Imgproc.INTER_CUBIC);
		return outputMat;
	}

}
