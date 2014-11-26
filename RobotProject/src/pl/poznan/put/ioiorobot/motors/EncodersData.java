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
	private PositionController positionController;

	private InputStream in;
	private OutputStream out;
	private byte receivedData[] = new byte[255];
	private int offset = 0;
	private Byte b;

	private static Timer timer;

	public EncodersData(IOIO ioio_, int rxPin, int txPin, int requestPin, int baud, Uart.Parity parity, Uart.StopBits stopBits,
			PositionController positionController) throws ConnectionLostException {
		this.ioio_ = ioio_;
		this.positionController = positionController;
		uart = ioio_.openUart(rxPin, txPin, baud, parity, stopBits);
		request = ioio_.openDigitalOutput(requestPin);
		request.write(false);

		in = uart.getInputStream();
		out = uart.getOutputStream();

		if (null != timer) {
			timer.cancel();
			timer.purge();
			timer = null;
		}

		timer = new Timer();

		timer.scheduleAtFixedRate(new MyTask(), 0, 100);
	}

	private class MyTask extends TimerTask {

		public void run() {

			// try {
			// out.write(65);
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// // Ignore
			// }
			// } catch (IOException e) {
			// // TODO ???
			// }
			
			
			try {
				request.write(true);
				Thread.sleep(10);
				request.write(false);
				Thread.sleep(10);
			} catch (ConnectionLostException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*
			int i = 0;
			StringBuilder s = new StringBuilder();

			while (i != 10) {
				try {
					// in.read(receivedData, 0, 255);
					i = in.read();
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Ignore
					}
				} catch (IOException e) {
					// TODO ???
				}

				if (i >= 48 && i <= 57)
					s.append(Integer.toString(i - 48));
			}



			// Log.d("robot", "\t\t\tUART RECEIVED: " +
			// Byte.toString(receivedData[0]) + "  |  " +
			// Arrays.toString(receivedData));
			
			Log.d("robot", "\t\t\tUART RECEIVED: " + s.toString());
*/

			BufferedReader br = new BufferedReader( new InputStreamReader(uart.getInputStream()) );
			String line = null;
			try {
				line = br.readLine();
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			if(line != null) {
				Log.d("robot", "\t\t\tUART RECEIVED: " + line);
				
				String pattern= "^ *-?[0-9]+.[0-9]+ +-?[0-9]+.[0-9]+ +-?[0-9]+.[0-9]+$";
				
		        if(line.matches(pattern)){
		        	Log.d("robot", "\t\t\t\t\tOK");
		        	
		        	line = line.trim();
		        	
		        	String [] parts = line.split(" +");  
		        	
		        	for(String s : parts){
		        		Log.d("robot", "\t\t\t\t\t\t\t\t\t"+s);
		        	}
		        	
		        	positionController.set(StrToDouble(parts[0]), StrToDouble(parts[1]), StrToDouble(parts[2]));
		        	
		        }
		        else {
		        	Log.d("robot", "\t\t\t\t\t--");
		        }
			}
			else {
				Log.d(C.TAG, "\t\t\tUART DATA PROBLEM");
			}
			


			
			
			
			
//			int num = 0;
//			
//			if(line != null) {
//				num =  StrToInt(line);
//				Log.d("robot", "\t\t\tUART RECEIVED: " + num);
//			}
//			else {
//				Log.d(C.TAG, "\t\t\tUART DATA PROBLEM");
//			}
//			
//			if(positionController != null) {
//				positionController.set();
//			}
//			else {
//				Log.d("robot", "\t\t\tpositionController == null");
//			}
//
//			double left = num;
//			double right = 0;
//			//if(left!=0 || right != 0) {
//				double leftMM = (left / (C.encoderResolution * C.gearRatio)) * Math.PI * C.wheelDiameter;
//				double rightMM = (right / (C.encoderResolution * C.gearRatio)) * Math.PI * C.wheelDiameter;
//				if(positionController != null) {
//					positionController.move(leftMM, rightMM);
//				}
//				else {
//					Log.d("robot", "\t\t\tpositionController == null");
//				}
//			//}
			

		}

	}

	
	private int StrToInt(String s) {
		try {
			int i = Integer.parseInt(s);
			return i;
		}
		catch(NumberFormatException e) {
			return 0;
		}
		
	}
	
	private double StrToDouble(String s) {
		try {
			double i = Double.parseDouble(s);
			return i;
		}
		catch(NumberFormatException e) {
			return 0.0;
		}
		
	}
}