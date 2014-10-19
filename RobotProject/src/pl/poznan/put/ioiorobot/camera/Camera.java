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
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;

public class Camera implements CvCameraViewListener2 {

	private CameraBridgeViewBase cameraView;
	private BaseLoaderCallback loaderCallback;
	private Context context;
	
	private int xTargetPosition;

	public int getxTargetPosition() {
		return xTargetPosition;
	}
	
	public Camera(final CameraBridgeViewBase cameraView, Context context) {
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
				cameraView.setMaxFrameSize(800, 480);
			}
		};
	}

	public void resume() {
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, loaderCallback);
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return findColorShapes(inputFrame);
		//return findRegularShapes(inputFrame);

	}

	private Mat findColorShapes(CvCameraViewFrame inputFrame) {
		Mat mat = inputFrame.rgba();
		Mat dst = new Mat();
		Mat result = new Mat();
		Imgproc.cvtColor(mat, dst, Imgproc.COLOR_RGB2HSV, 3);
		getYellowMat(dst, dst);
		Point center = detectObject(mat, dst, "C", result);
		xTargetPosition = (int) (((double) center.x / (double) mat.width())*200.0-100.0);
		return result;
	}
	
	private Mat findRegularShapes(CvCameraViewFrame inputFrame) {
		Mat img = inputFrame.rgba();
		Mat gray = inputFrame.gray();
		
		
//		Mat grayThreshold = gray;;
//		//Imgproc.threshold(gray, grayThreshold, 127.0, 255.0, Imgproc.THRESH_BINARY);
//		Imgproc.adaptiveThreshold(gray, grayThreshold, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
//		
//		Imgproc.erode(grayThreshold, grayThreshold, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
//		//return grayThreshold;
		
		Mat canny = gray;
		Mat edges = canny;
		Imgproc.Canny(canny, edges, 50, 100);
		
		
		return canny;
		
//		Mat contourImg = canny;
		
//		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//		Imgproc.findContours(grayThreshold, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
//
//		
//		for(MatOfPoint cnt : contours) {
//			MatOfPoint2f cnt2f = new MatOfPoint2f(cnt.toArray());
//			MatOfPoint2f approxCurve = new MatOfPoint2f();
//			double epsilon = 0.01 * Imgproc.arcLength(cnt2f, true);
//			Imgproc.approxPolyDP(cnt2f, approxCurve, epsilon, true);
//			
//			if(approxCurve.toList().size()==3) {
//				List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
//				cntList.add(cnt);
//				Imgproc.drawContours(img, cntList, 0, new Scalar(255, 0, 0), 4);
//			}
//			else if(approxCurve.toList().size()==4) {
//				List<MatOfPoint> cntList = new ArrayList<MatOfPoint>();
//				cntList.add(cnt);
//				
//				Imgproc.drawContours(img, cntList, 0, new Scalar(0,255, 0), 4);
//			}
//		}
//		
//		return img;		
	}

	public static void getBlueMat(Mat src, Mat dst) {
		Core.inRange(src, new Scalar(100, 100, 100), new Scalar(120, 255, 255), dst);
	}

	public static void getYellowMat(Mat src, Mat dst) {
		Core.inRange(src, new Scalar(20, 100, 100), new Scalar(30, 255, 255), dst);
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

}
