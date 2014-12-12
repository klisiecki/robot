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

public class Pattern {
	private static int nextId = 0;

	private int id;
	private int size;
	private boolean[][] array;
	private Bitmap bitmap;
	private int count = 1;
	private int ttl = C.patternTTL;
	private double cameraAngle;
	private Point position;
	private List<Position> viewPositions = new ArrayList<Position>();

	
	public List<Position> getViewPositions() {
		return viewPositions;
	}
	public Point getPoint(){
		return position;
	}

	public void addViewPosition(Position p) {
		Log.d(C.TAG, id+ " ...adViewPosition" + p);
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
	 * http://stackoverflow.com/questions/7446126/opencv-2d-line-intersection-
	 * helper-function
	 * 
	 * @param o1
	 * @param p1
	 * @param o2
	 * @param p2
	 * @return
	 */
	public Point intersection(final Point o1, final Point p1, final Point o2, final Point p2) {
		Log.d(C.TAG, "intersection");
		Point x = new Point(o2.x - o1.x, o2.y - o1.y);
		Point d1 = new Point(p1.x - o1.x, p1.y - o1.y); //wektory
		Point d2 = new Point(p2.x - o2.x, p2.y - o2.y);
		
		Log.d(C.TAG, "d1 = "+d1 + ", d2 = "+d2);

		int cross = d1.x * d2.y - d1.y * d2.x;
		Log.d(C.TAG, "cross = "+cross);
		if (cross == 0) {
			return null;
		}
		double t1 = (x.x * d2.y - x.y * d2.x) / cross;
		Point r = new Point(o1.x + (int) (d1.x * t1), o1.y + (int) (d1.y * t1));
		return r;

	}

	private void recalculatePosition() {
		if (viewPositions.size() >= 2) {
			Point o1 = viewPositions.get(0).getPoint();
			Point p1 = viewPositions.get(0).getVectorPoint();
			Point o2 = viewPositions.get(viewPositions.size()-1).getPoint();
			Point p2 = viewPositions.get(viewPositions.size()-1).getVectorPoint();

			Point p = intersection(o1, p1, o2, p2);

			if (p != null) {
				position = p;
			} else {
				Log.d(C.TAG, "NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				viewPositions.remove(1);
			}
		} else {
			//TODO dodawanie kolejnych punktów
		}
	}

	public void merge(Pattern p) {
		for (Position pos : p.getViewPositions()) {
			addViewPosition(new Position(pos));
		}
	}

	public int incrementCount() {
		ttl = C.patternTTL * 2;
		return ++count;
	}

	public boolean check() {
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

	public Pattern(Mat mat, double cameraAngle) {
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
