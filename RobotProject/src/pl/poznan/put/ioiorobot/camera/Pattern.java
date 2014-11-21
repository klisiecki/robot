package pl.poznan.put.ioiorobot.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Core.MinMaxLocResult;

import pl.poznan.put.ioiorobot.utils.MyConfig;

public class Pattern {
	private boolean[][] array;

	public boolean[][] getMat() {
		return array;
	}

	private int size;

	public Pattern(Mat mat) {
		this();
		MinMaxLocResult minmax = Core.minMaxLoc(mat);
		double min = minmax.minVal;
		double max = minmax.maxVal;
		double mean = (min + max) / 2;
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				double[] val = mat.get(i, j);
				if (val != null) {
					double brigtness = val[0];
					if (brigtness > mean) {
						array[i][j] = true;
					} else {
						array[i][j] = false;
					}
				} else {
				}
			}
		}
	}

	public Pattern() {
		size = MyConfig.patternSize;
		array = new boolean[size][size];
	}

	public void set(int i, int j, boolean value) {
		array[i][j] = value;
	}

	public int compareTo(Pattern otherPattern) {
		boolean[][] otherMat = otherPattern.getMat();
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
