package pl.poznan.put.ioiorobot.camera;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;

import pl.poznan.put.ioiorobot.utils.Config;

public class CameraHelper {
	
	/**
	 * Zwraca true gdy długości boków a oraz b różnią się maksymalnie o
	 *         C.rectangleFactor %
	 */
	public static boolean areSimilar(int a, int b) {
		return Math.abs((double) (a - b) / a) < (double) Config.rectangleFactor / 100.0;
	}

	/**
	 * Zwraca odległość pomiędzy punktami a oraz b
	 */
	public static int getDistance(Point a, Point b) {
		return (int) Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	/**
	 * Zwraca kąt między wektorami p1 -> p0 i p1 -> p2
	 */
	public static double getAngle(Point p0, Point p1, Point p2) {
		return Math.toDegrees(Math.atan2(p0.x - p1.x, p0.y - p1.y) - Math.atan2(p2.x - p1.x, p2.y - p1.y));
	}

	/**
	 * Metoda określająca, czy podana lista punktów może tworzyć kwadrat
	 */
	public static boolean couldBeSquare(List<Point> points) {
		if (points.size() != 4) {
			return false;
		}

		int top = CameraHelper.getDistance(points.get(0), points.get(1));
		int bottom = CameraHelper.getDistance(points.get(2), points.get(3));
		int left = CameraHelper.getDistance(points.get(0), points.get(3));
		int right = CameraHelper.getDistance(points.get(1), points.get(2));

		if (!CameraHelper.areSimilar(top, bottom) || !CameraHelper.areSimilar(left, right)
				|| !CameraHelper.areSimilar(left, top)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Metoda sortująca wierzchołki czworokąta
	 * 
	 * @return true gry posortowano, false gdy nie da się posortować
	 */
	public static boolean sortCorners(List<Point> cornersArray) {
		double x = 0, y = 0;
		for (Point p : cornersArray) {
			x += p.x;
			y += p.y;
		}

		Point center = new Point(x / 4, y / 4);
		List<Point> top = new ArrayList<Point>();
		List<Point> bot = new ArrayList<Point>();

		for (int i = 0; i < cornersArray.size(); i++) {
			if (cornersArray.get(i).y < center.y) {
				top.add(cornersArray.get(i));
			} else {
				bot.add(cornersArray.get(i));
			}
		}

		if (top.size() != 2) {
			return false;
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
		return true;
	}

}
