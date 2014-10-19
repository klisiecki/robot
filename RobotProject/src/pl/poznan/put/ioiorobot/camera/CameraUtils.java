package pl.poznan.put.ioiorobot.camera;

import org.opencv.core.Point;

public class CameraUtils {
	public static Point getCenter(Point tl, Point br) {
		return new Point((tl.x + br.x) / 2, (tl.y + br.y) / 2);
	}
}
