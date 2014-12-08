package pl.poznan.put.ioiorobot.motors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import pl.poznan.put.ioiorobot.utils.C;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import android.text.format.Time;
import android.util.Log;

public class EncodersData {

	private IOIO ioio_;
	private Uart uart;
	private DigitalOutput request;
	private Position position;

	private InputStream uartInput;
	private OutputStream uartOutput;

	private static Timer timer;

	public EncodersData(IOIO ioio_, int rxPin, int txPin, int requestPin, int baud, Uart.Parity parity,
			Uart.StopBits stopBits, Position position) throws ConnectionLostException {
		this.ioio_ = ioio_;
		this.position = position;
		uart = ioio_.openUart(rxPin, txPin, baud, parity, stopBits);
		request = ioio_.openDigitalOutput(requestPin);
		request.write(false);

		uartInput = uart.getInputStream();
		uartOutput = uart.getOutputStream();

		try {
			uartOutput.write(1); // wysyła cokolwiek do Arduino, aby zresetowało
									// liczniki
		} catch (IOException e) {
			e.printStackTrace();
		}

		EncodersDataThread t = new EncodersDataThread();
		
//		if (null != timer) { timer.cancel(); timer.purge(); timer = null; }
//		
//		timer = new Timer();
//		
//		timer.scheduleAtFixedRate(new MyTask(), 0, 50);
		 
	}

//	private class MyTask extends TimerTask {
//
//		public void run() {
//			getData();
//		}
//	}
	
	private class EncodersDataThread extends Thread {
		public EncodersDataThread(){
			start();
		}
		
		@Override
		public void run() {
			while(true) {
				getData();
			}
		}
	}

	private void getData() {
		try {
			request.write(true);
			Thread.sleep(5);
			request.write(false);
			Thread.sleep(50);
		} catch (ConnectionLostException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(uartInput));
		String line = null;
		try {
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (line != null) {
			// Log.d(C.TAG, "\t\t\tUART RECEIVED: " + line);

			String pattern = "^> *-?[0-9]+.[0-9]+ +-?[0-9]+.[0-9]+ +-?[0-9]+.[0-9]+<$";

			if (line.matches(pattern)) {
//				Log.d(C.TAG, "\t\t\tUART RECEIVED OK: " + line);

				line = line.substring(1, line.length() - 1);
				line = line.trim();

				String[] parts = line.split(" +");

				position.set(StrToDouble(parts[0]), StrToDouble(parts[1]), StrToDouble(parts[2]));
			} else {
//				 Log.d(C.TAG, "\t\t\t\t\tNO MATCH");
			}
		} else {
//			Log.d(C.TAG, "\t\t\tUART DATA PROBLEM");
		}
	}

	private int StrToInt(String s) {
		try {
			int i = Integer.parseInt(s);
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}

	}

	private double StrToDouble(String s) {
		try {
			double i = Double.parseDouble(s);
			return i;
		} catch (NumberFormatException e) {
			return 0.0;
		}

	}
}