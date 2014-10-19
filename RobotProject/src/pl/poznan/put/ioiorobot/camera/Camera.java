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
		//OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, context, loaderCallback);
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
		Mat mat = inputFrame.rgba();
		Mat dst = new Mat();
		Mat result = new Mat();
		Imgproc.cvtColor(mat, dst, Imgproc.COLOR_RGB2HSV, 3);
		getYellowMat(dst, dst);
		Point center = detectObject(mat, dst, "C", result);
		xTargetPosition = (int) (((double) center.x / (double) mat.width())*200.0-100.0);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

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
