package pl.poznan.put.ioiorobot.camera;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.util.Log;

public class ImageProcessing {
	public static final int PATTERN_SIZE = 100;

	public static int[][] getPattern(Mat mat) {
		int[][] result = new int[mat.height()][mat.width()];
		MinMaxLocResult minmax = Core.minMaxLoc(mat);
		double min = minmax.minVal;
		double max = minmax.maxVal;
		double mean = (min + max) / 2;
		for (int i = 0; i < mat.height(); i++) {
			for (int j = 0; j < mat.width(); j++) {
				// Mat subMat = mat.submat(j, j+1, i, i+1);
				// double brigtness = getBrigtness(Core.mean(subMat));
				double[] val = mat.get(i, j);
				if (val != null) {
					double brigtness = val[0];
					if (brigtness > mean) {
						result[i][j] = 1;
					} else {
						result[i][j] = 0;
					}
				} else {
					Log.e("robot", "i = " + i + ", j = " + j);
					Log.e("robot", "no pixel!");
					Log.e("robot", mat.toString());
					return null;
				}
			}
		}

		for (int i = 0; i < PATTERN_SIZE; i++) {
			for (int j = 0; j < PATTERN_SIZE; j++) {
				// Mat subMat = mat.submat(i * step, i * step + step, j * step,
				// j
				// * step + step);
				// double brigtness = getBrigtness(Core.mean(subMat));
				// if (brigtness > meanValue) {
				// result[i][j] = 1;
				// } else {
				// result[i][j] = 0;
				// }
			}
		}

		return result;
	}

	public static double getBrigtness(Scalar color) {
		return color.val[0];
	}

	public static String tabToString(int[][] tab) {
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < tab.length; i++) {
			int[] subtab = tab[i];
			for (int j = 0; j < subtab.length; j++) {
				result.append(tab[i][j]);
			}
			result.append("\n");
		}

		return result.toString();
	}

	public static int getCoverage(int[][] p1, int[][] p2) {
		int result = 0;
		for (int i = 0; i < PATTERN_SIZE; i++) {
			for (int j = 0; j < PATTERN_SIZE; j++) {
				if (p1[i][j] == p2[i][j]) {
					result++;
				}
			}
		}
		result /= (PATTERN_SIZE * PATTERN_SIZE / 100);
		return result;
	}
}
