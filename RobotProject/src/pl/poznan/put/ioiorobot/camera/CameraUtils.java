package pl.poznan.put.ioiorobot.camera;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class CameraUtils {
	public static Point getCenter(Point tl, Point br) {
		return new Point((tl.x + br.x) / 2, (tl.y + br.y) / 2);
	}
	
	public static List<Double> getPixelColor(Mat mat, int x, int y) {
		List<Double> result = new ArrayList<Double>();
		double[] buff = mat.get(x, y);

		for (int i = 0; i < buff.length; i++) {
			result.add(buff[i]);
		}
		
		return result;
	}
	

}
