package pl.poznan.put.ioiorobot.motors;

import java.util.Timer;
import java.util.TimerTask;

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
	
	
	private static Timer timer;

	public EncodersData(IOIO ioio_, int rxPin, int txPin, int baud, Uart.Parity  parity, Uart.StopBits stopBits) throws ConnectionLostException {
		this.ioio_ = ioio_;
		uart = ioio_	.openUart(rxPin, txPin, baud, parity, stopBits);

		
		
		if(null != timer)
	    {
	        timer.cancel();
	        timer.purge();
	        timer = null;
	    }

	    timer = new Timer();
				
		timer.scheduleAtFixedRate(new PID(), 0, 100);
	}

	

	private class PID extends TimerTask {
		private int calka = 0;
		private int popBlad = 0;
		private int iteracja = 0;
		
		private int dlugoscRegulacjiPd = 10;
		private int granicaCalki = 100;
		
		private int Kp = 5;
		private int Ki = 0;
		private int Kd = 5;
		
		public void run() {

		}
	}
}