package pl.poznan.put.ioiorobot.camera;

import java.util.ArrayList;
import java.util.Iterator;
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
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.SeekBar;

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
		// return findColorShapes(inputFrame);
		return findRegularShapes(inputFrame);
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
		Core.inRange(src, new Scalar(20, 10, 10), new Scalar(45, 255, 255), dst);
	}

	public static Point detectObject(Mat src, Mat image, String text, Mat dst) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		src.copyTo(dst);

		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

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
		Mat baseImgRgba = inputFrame.rgba();
		Mat baseImgGray = inputFrame.gray();

		Log.d("robot", "seekBar1 = " + seekBar1.getProgress() + "      seekBar2 = " + seekBar2.getProgress()
				+ "      seekBar3 = " + seekBar3.getProgress());

		Mat mask = new Mat();
		baseImgRgba.copyTo(mask);
		Imgproc.cvtColor(mask, mask, Imgproc.COLOR_RGB2HSV, 3);
		getYellowMat(mask, mask);

		Mat image = new Mat();
		baseImgRgba.copyTo(image, mask);

		/* Imgproc.cvtColor(image, image, Imgproc.COLOR_HSV2RGB, 4); */
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);

		// return image;

		/* Imgproc.threshold(image, image, 127.0, 255.0, Imgproc.THRESH_BINARY); */

		int blockSize = 9; // seekBar1.getProgress()*2 + 3
		int C = 7; // seekBar2.getProgress()
		Imgproc.adaptiveThreshold(image, image, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,
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
		Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Mat resultImage = baseImgRgba;

		MatOfPoint maxCnt = null;

		for (MatOfPoint cnt : contours) {

			// Pomijanie małych obiektów
			int threshold = 600; // seekBar1.getProgress()*10;
			if (Imgproc.contourArea(cnt) < threshold)
				continue;

			drawContour(resultImage, cnt);

			Mat fragment = cutFragment(baseImgRgba, resultImage, cnt);
			// Core.rectangle(fragment, new Point(0, 0), new
			// Point(fragment.height()/2, fragment.width()/2), new Scalar(0,
			// 255, 0), 10);

			if (maxCnt == null) {
				maxCnt = new MatOfPoint(cnt);
			} else if (Imgproc.contourArea(cnt) > Imgproc.contourArea(maxCnt)) {
				maxCnt = new MatOfPoint(cnt);
			}

		}

		// Rysowanie największego znalezionego obszaru
		if (maxCnt != null) {
			// fragment.copyTo(resultImage.rowRange(0,
			// fragment.height()-1).colRange(0, fragment.width()-1));
			Mat fragment = cutFragment(baseImgRgba, resultImage, maxCnt);
			if (fragment.width() != 0 && fragment.height() != 0) {
				
				findCornerHarris(fragment);
				
//				fragment = warp(fragment, new Point(fragment.width()*2/10, 0),
//						new Point(0, fragment.height()), 
//						new Point(fragment.width(), fragment.height()),
//						new Point(fragment.width()*8/10, 0) );
				
				
				showFragment(resultImage, fragment, 400);
			}
		}

		return resultImage;
	}

	private void showFragment(Mat resultImage, Mat fragment, int height) {
		{
		int slotHeight = Math.min(height, resultImage.height());
		int slotWidth = Math.min(slotHeight * fragment.width() / fragment.height(), resultImage.width());
		Mat slot = resultImage.submat(0, slotHeight, 0, slotWidth); // (0,
																	// fragment.height()-1,
																	// 0,
																	// fragment.width()-1);
		
		Imgproc.resize(fragment, slot, slot.size());
		Core.rectangle(slot, new Point(0, 0), new Point(slot.width(), slot.height()), new Scalar(0, 0, 0), 20);
		}
	}

	
	private void findCornerHarris(Mat fragment) {
		Mat fragmentGray = new Mat();
		fragment.copyTo(fragmentGray);
		
		Mat cornerMap = new Mat();
		fragmentGray.copyTo(cornerMap);
		
		Imgproc.cvtColor(fragment, fragmentGray, Imgproc.COLOR_RGB2GRAY);
		
		int blockSize1 = 10; //2
		int apertureSize = 3; //3
		double k = 0.04; //0.04
						
		Imgproc.cornerHarris(fragmentGray, cornerMap, blockSize1, apertureSize, k, Imgproc.BORDER_DEFAULT );
		
		for(int y=0; y < fragment.height(); y++)
			for(int x=0; x < fragment.width(); x++) {
				double[] harris = cornerMap.get(y, x);
				if( harris[0] > 10e-04 ){
					Core.circle(fragment, new Point(x,y), 5, new Scalar(255,90, 200), 5);
				}
			}				
		
//				Imgproc.cvtColor(cornerMap, cornerMap, Imgproc.COLOR_GRAY2RGB, 4);
//				fragment = cornerMap;
	
	}

	private Mat cutFragment(Mat baseImgRgba, Mat resultImage, MatOfPoint cnt) {
		Mat fragment;

		Rect rect = Imgproc.boundingRect(cnt);
		Core.rectangle(resultImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
				new Scalar(255, 0, 0), 5);
		fragment = baseImgRgba.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);

		return fragment;
	}

	private void drawContour(Mat resultImage, MatOfPoint cnt) {

		MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		double epsilon = 0.01 * Imgproc.arcLength(cnt2f, true);
		Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);

		if (approxCurve.toList().size() == 3) {
			List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
			cntList.add(cnt);
			Imgproc.drawContours(resultImage, cntList, 0, new Scalar(255, 0, 0), 4);
		} else if (approxCurve.toList().size() == 4) {
			List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
			cntList.add(cnt);

			Imgproc.drawContours(resultImage, cntList, 0, new Scalar(0, 255, 0), 4);
		} else if (approxCurve.toList().size() == 5) {
			List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
			cntList.add(cnt);
			Imgproc.drawContours(resultImage, cntList, 0, new Scalar(0, 0, 255), 4);
		} else {
			List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
			cntList.add(cnt);

			Imgproc.drawContours(resultImage, cntList, 0, new Scalar(0, 255, 255), 4);
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
