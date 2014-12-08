package pl.poznan.put.ioiorobot.camera;

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

public class Pattern {
	private static int nextId = 0;

	private int id;
	private int size;
	private boolean[][] array;
	private Bitmap bitmap;
	private int count = 1;
	private int ttl = C.patternTTL;
	private List<Position> viewPositions = new ArrayList<Position>();

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
