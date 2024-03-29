package pl.poznan.put.ioiorobot.camera;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

/**
 *	Klasa rozszerzająca JavaCameraView o dostęp do pola mCamera
 */
public class MyJavaCameraView extends JavaCameraView {
	
	public Camera getCamera() {
		return mCamera;
	}

	public MyJavaCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

}
