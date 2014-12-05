package pl.poznan.put.ioiorobot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

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
	        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(file, Context.MODE_PRIVATE));
	        outputStreamWriter.write(data);
	        outputStreamWriter.close();
	    }
	    catch (IOException e) {
	        Log.e("Exception", "File write failed: " + e.toString());
	    } 
	}
	
	public static void writeToExternal(String data, String fileName) {
		String path = "/storage/sdcard0/debug/"+fileName;
		//Environment.getExternalStorageDirectory(); //u mnie zwraca pamięc telefonu
		try {
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fOut = new FileOutputStream(file);
			OutputStreamWriter oss = new OutputStreamWriter(fOut);
			oss.append(data);
			oss.close();
			Log.e(C.TAG, "SAVED");
		} catch (Exception e) {
			Log.e(C.TAG, e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
