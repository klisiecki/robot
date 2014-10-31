package pl.poznan.put.ioiorobot.camera;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class ImageProcessing {
	public static final int PATTERN_SIZE = 100;

	public static int[][] getPattern(Mat mat) {
		int[][] result = new int[PATTERN_SIZE][PATTERN_SIZE];
		Scalar meanColor = Core.mean(mat);
		double meanValue = getBrigtness(meanColor);

		for (int i = 0; i < PATTERN_SIZE; i++) {
			for (int j = 0; j < PATTERN_SIZE; j++) {
				Mat subMat = mat.submat(i * PATTERN_SIZE, i * PATTERN_SIZE + PATTERN_SIZE, j * PATTERN_SIZE, j
						* PATTERN_SIZE + PATTERN_SIZE);
				double brigtness = getBrigtness(Core.mean(subMat));
				if (brigtness > meanValue) {
					result[i][j] = 1;
				} else {
					result[i][j] = 0;
				}
			}
		}

		return result;
	}

	public static double getBrigtness(Scalar color) {
		return color.val[0];
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
