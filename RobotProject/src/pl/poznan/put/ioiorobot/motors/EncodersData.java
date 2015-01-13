package pl.poznan.put.ioiorobot.motors;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Timer;

/**
 * Klasa komunikująca się z Arduino, służy do przechowywania aktualnej pozycji
 * robnota i informowania o jej zmianach
 */
public class EncodersData {

	private PositionChangedListener listener;
	private Uart uart;
	private DigitalOutput request;
	private Position position;

	private InputStream uartInput;
	private OutputStream uartOutput;

	private static Timer timer;

	public EncodersData(IOIO ioio_, int rxPin, int txPin, int requestPin, int baud, Uart.Parity parity,
			Uart.StopBits stopBits, Position position) throws ConnectionLostException {
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

		EncodersDataThread encodersDataThread = new EncodersDataThread();
		encodersDataThread.start();
	}

	private class EncodersDataThread extends Thread {

		@Override
		public void run() {
			while (true) {
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
			String pattern = "^> *-?[0-9]+.[0-9]+ +-?[0-9]+.[0-9]+ +-?[0-9]+.[0-9]+<$";

			if (line.matches(pattern)) {
				line = line.substring(1, line.length() - 1);
				line = line.trim();

				String[] parts = line.split(" +");

				position.set(parseFloat(parts[0]), parseFloat(parts[1]), parseFloat(parts[2]));
				if (listener != null) {
					listener.onPositionChanged(position);
				}
			}
		}
	}

	private float parseFloat(String s) {
		try {
			float i = Float.parseFloat(s);
			return i;
		} catch (NumberFormatException e) {
			return 0.0f;
		}
	}
	
	public Position getPosition() {
		return new Position(position);
	}
	
	public void setPositionChangedListener(PositionChangedListener listener) {
		this.listener = listener;
	}
	
	public interface PositionChangedListener {
		public void onPositionChanged(Position position);
	}

}