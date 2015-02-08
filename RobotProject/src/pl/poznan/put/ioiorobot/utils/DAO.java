package pl.poznan.put.ioiorobot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DAO {
	static Context context;

	public static void setContext(Context context) {
		DAO.context = context;
	}

	public static Object getItem(String name) {
		Object object = null;
		if (context != null) {
			FileInputStream fis;
			try {
				fis = context.openFileInput(name);
				ObjectInputStream ois = new ObjectInputStream(fis);
				object = ois.readObject();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

	public static void saveItem(final Object item, final String name) {
		if (context != null) {
			FileOutputStream fos;
			try {
				fos = context.openFileOutput(name, Context.MODE_PRIVATE);
				ObjectOutputStream oos;
				oos = new ObjectOutputStream(fos);
				oos.writeObject(item);
				oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static void saveItemAsync(final Object item, final String name) {
		Thread t = new Thread() {
			public void run() {
				saveItem(item, name);
			}
		};
		t.start();
	}

	public static void writeToFile(String data, String file) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file,
					Context.MODE_PRIVATE));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		} catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		}
	}

	public static void writeToExternal(String data, String fileName) {
		String path = "/storage/sdcard0/debug/" + fileName;
		// Environment.getExternalStorageDirectory(); //u mnie zwraca pamięc
		// telefonu
		try {
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter oss = new OutputStreamWriter(fOut);
			oss.append(data);
			oss.close();
			Log.e(Config.TAG, "SAVED");
		} catch (Exception e) {
			Log.e(Config.TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	public static String readFromExternal(String fileName) {
		String path = "/storage/sdcard0/debug/" + fileName;
		char[] result = null;
		try {
			File file = new File(path);
			file.createNewFile();
			FileInputStream fOut = new FileInputStream(file);
			InputStreamReader oss = new InputStreamReader(fOut);
			oss.read(result);
			oss.close();
			return new String(result);
		} catch (Exception e) {
			Log.e(Config.TAG, e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public static void writeToExternalAsync(final String data, final String fileName) {
		Thread t = new Thread() {
			public void run() {
				writeToExternal(data, fileName);
			}
		};

		t.start();
	}

	public static void saveBitmap(Bitmap bitmap, String fileName) {
		File file = new File("/storage/sdcard0/debug/" + fileName + ".png"); // TODO
																				// ścieżki
																				// z
																				// Environment..

		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Mat getMock() {
		// return matFromJson(readFromExternal("mock"));
		//return mock;
		return getImage();
	}

	static Mat mock;

	public static void saveMock(Mat mat) {
		// writeToExternal(matToJson(mat), "mock");
		
		saveImage(mat);
		mock = new Mat();
		mat.copyTo(mock);
	}

	public static Mat getMock2() {
		return (Mat) getItem("mock2");
	}

	public static void saveMock2(Mat mock) {
		saveItem(mock, "mock2");
	}

	public static void saveImage(Mat mat) {
		Mat mIntermediateMat = new Mat();
		Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);
		File path = new File("/storage/sdcard0/debug/");
		path.mkdirs();
		File file = new File(path, "image.png");
		String filename = file.toString();
		Boolean bool = Highgui.imwrite(filename, mIntermediateMat);
		if (bool)
			Log.i(Config.TAG, "SUCCESS writing image to external storage");
		else
			Log.i(Config.TAG, "Fail writing image to external storage");
	}
	
	public static Mat getImage() {
		File file = new File("/storage/sdcard0/debug/image.png");
		if (file.exists()) {
			Mat mat = Highgui.imread(file.getAbsolutePath());
			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
			return mat;
		}
		return null;
	}

	public static String matToJson(Mat mat) {
		JsonObject obj = new JsonObject();

		if (mat.isContinuous()) {
			int cols = mat.cols();
			int rows = mat.rows();
			int elemSize = (int) mat.elemSize();

			byte[] data = new byte[cols * rows * elemSize];

			mat.get(0, 0, data);

			obj.addProperty("rows", mat.rows());
			obj.addProperty("cols", mat.cols());
			obj.addProperty("type", mat.type());

			// We cannot set binary data to a json object, so:
			// Encoding data byte array to Base64.
			String dataString = new String(Base64.encode(data, Base64.DEFAULT));

			obj.addProperty("data", dataString);

			Gson gson = new Gson();
			String json = gson.toJson(obj);

			return json;
		} else {
			Log.e(Config.TAG, "Mat not continuous.");
		}
		return "{}";
	}

	public static Mat matFromJson(String json) {
		JsonParser parser = new JsonParser();
		JsonObject JsonObject = parser.parse(json).getAsJsonObject();

		int rows = JsonObject.get("rows").getAsInt();
		int cols = JsonObject.get("cols").getAsInt();
		int type = JsonObject.get("type").getAsInt();

		String dataString = JsonObject.get("data").getAsString();
		byte[] data = Base64.decode(dataString.getBytes(), Base64.DEFAULT);

		Mat mat = new Mat(rows, cols, type);
		mat.put(0, 0, data);

		return mat;
	}
}
