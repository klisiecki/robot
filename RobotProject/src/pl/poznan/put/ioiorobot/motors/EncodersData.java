package pl.poznan.put.ioiorobot.motors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import pl.poznan.put.ioiorobot.utils.C;

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
	private PositionController positionController;

	private InputStream in;
	private OutputStream out;
	private byte receivedData[] = new byte[255];
	private int offset = 0;
	private Byte b;

	private static Timer timer;

	public EncodersData(IOIO ioio_, int rxPin, int txPin, int baud, Uart.Parity parity, Uart.StopBits stopBits,
			PositionController positionController) throws ConnectionLostException {
		this.ioio_ = ioio_;
		this.positionController = positionController;
		uart = ioio_.openUart(rxPin, txPin, baud, parity, stopBits);

		in = uart.getInputStream();
		out = uart.getOutputStream();

		if (null != timer) {
			timer.cancel();
			timer.purge();
			timer = null;
		}

		timer = new Timer();

		timer.scheduleAtFixedRate(new MyTask(), 0, 200);
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

			int left = 0, right = 0;
			double leftMM = (left / C.encoderResolution * C.gearRatio) * Math.PI * C.wheelDiameter;
			double rightMM = (right / C.encoderResolution * C.gearRatio) * Math.PI * C.wheelDiameter;
			positionController.move(leftMM, rightMM);

		}

	}

}