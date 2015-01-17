package pl.poznan.put.ioiorobot.mapobjects;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.utils.C;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

/**
 * Wzorzec (marker) wraz z dodatkowymi informacjami o jego położeniu itp.
 */
/**
 * @author karol
 *
 */
public class Pattern {
	private static int nextId = 0;

	private int id;
	private int size;
	private boolean[][] array;
	private Bitmap bitmap;
	private int count = 1;
	private int ttl = C.patternTTL;
	private float cameraAngle;
	private Point position;
	private List<Position> viewPositions = new ArrayList<Position>();

	public List<Position> getViewPositions() {
		return viewPositions;
	}

	public Point getPoint() {
		return position;
	}

	public void addViewPosition(Position p) {
		// Log.d(C.TAG, id + " ...adViewPosition" + p);
		// jednorazowe dodanie kąta wynikającego z położenia patternu na obrazie
		// kamery
		p.addAngle(cameraAngle);
		cameraAngle = 0;
		viewPositions.add(p);
		if (viewPositions.size() >= 2) {
			recalculatePosition();
		}
	}

	/**
	 * Układ równań z:
	 * http://stackoverflow.com/questions/2931573/determining-if-
	 * two-rays-intersect
	 * 
	 * @param as
	 * @param ae
	 * @param bs
	 * @param be
	 * @return
	 */
	public Point intersection2(final Point as, final Point ae, final Point bs, final Point be) {
		Point ad = new Point(ae.x - as.x, ae.y - as.y);
		Point bd = new Point(be.x - bs.x, be.y - bs.y);

		float u, v;
		if ((ad.x * bd.y - ad.y * bd.x) == 0) {
			return null;
		}
		u = (float) (as.y * bd.x + bd.y * bs.x - bs.y * bd.x - bd.y * as.x) / (ad.x * bd.y - ad.y * bd.x);

		if (bd.x != 0) {
			v = (float) (as.x + ad.x * u - bs.x) / bd.x;
		} else {
			v = (float) (as.y + ad.y * u - bs.y) / bd.y;
		}

		if (u <= 0 || v <= 0) {
			return null;
		}

		return new Point((int) (as.x + ad.x * u), (int) (as.y + ad.y * u));
	}

	private void recalculatePosition() {
		List<Point> intersections = new ArrayList<Point>();
		for (int i = 0; i < viewPositions.size(); i++) {
			for (int j = i + 1; j < viewPositions.size(); j++) {
				Point o1 = viewPositions.get(i).getPoint();
				Point p1 = viewPositions.get(i).getVectorPoint();
				Point o2 = viewPositions.get(j).getPoint();
				Point p2 = viewPositions.get(j).getVectorPoint();
				Point intersection = intersection2(o1, p1, o2, p2);
				if (intersection != null) {
					intersections.add(intersection);
				}
			}
		}

		if (intersections.size() == 0) {
			position = null;
			return;
		} else {
			position = new Point(0, 0);

			for (Point p : intersections) {
				position.x += p.x;
				position.y += p.y;
			}

			position.x /= intersections.size();
			position.y /= intersections.size();

		}
	}

	public void merge(Pattern p) {
		for (Position pos : p.viewPositions) {
			addViewPosition(new Position(pos));
		}
	}

	public int incrementCount() {
		ttl = C.patternTTL * 2;
		return ++count;
	}

	public boolean checkTTL() {
		if ((--ttl) < 0) {
			return false;
		}
		return true;
	}

	public boolean[][] getArray() {
		return array;
	}

	public int getId() {
		return id;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public int getCount() {
		return count;
	}

	public Pattern(Mat mat, float cameraAngle) {
		this(mat);
		this.cameraAngle = cameraAngle;
	}

	public Pattern(Mat mat) {
		this();
		Imgproc.resize(mat, mat, new Size(size, size));
		// int blockSize = size+1; // seekBar1.getProgress()*2 + 3
		// int C = 7; // seekBar2.getProgress()
		// Imgproc.adaptiveThreshold(mat, mat, 255,
		// Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,
		// blockSize, C);

		MinMaxLocResult minmax = Core.minMaxLoc(mat);
		double min = minmax.minVal;
		double max = minmax.maxVal;
		double mean = (min + max) / 2;
		bitmap = Bitmap.createBitmap(size, size, Config.ARGB_4444);
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double[] val = mat.get(i, j);
				if (val != null) {
					double brigtness = val[0];
					if (brigtness > mean) {
						array[i][j] = true;
						bitmap.setPixel(j, i, Color.WHITE);
					} else {
						array[i][j] = false;
						bitmap.setPixel(j, i, Color.BLACK);
					}
				} else {
				}
			}
		}

	}

	public Pattern() {
		id = nextId++;
		size = C.patternSize;
		array = new boolean[size][size];
	}

	public void set(int i, int j, boolean value) {
		array[i][j] = value;
	}

	public int compareTo(Pattern otherPattern) {
		boolean[][] otherMat = otherPattern.getArray();
		int result = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (array[i][j] == otherMat[i][j]) {
					result++;
				}
			}
		}
		result /= (size * size / 100);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("");
		result.append("Pattern id = " + id + ", size = " + size + "\n");
		for (int i = 0; i < array.length; i++) {
			boolean[] subtab = array[i];
			for (int j = 0; j < subtab.length; j++) {
				result.append(array[i][j] ? "1" : "0");
			}
			result.append("\n");
		}
		return result.toString();
	}
}
