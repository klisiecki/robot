package pl.poznan.put.ioiorobot;

import org.opencv.android.CameraBridgeViewBase;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import pl.poznan.put.ioiorobot.camera.Camera;
import pl.poznan.put.ioiorobot.motors.IMotorsController;
import pl.poznan.put.ioiorobot.motors.MotorsController;
import pl.poznan.put.ioiorobot.sensors.HCSR04DistanceSensor;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.temp.SimpleBarGraph;
import pl.poznan.put.ioiorobot.temp.VerticalSeekBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends IOIOActivity {
	private SeekBar speedBar;
	private SeekBar directionBar;

	private TextView textView;
	private IMotorsController motorsController;
	private IDistanceSensor distanceSensor;
	private Camera camera;
	private SimpleBarGraph barGraph;
	
	

	class Looper extends BaseIOIOLooper {
		@Override
		protected void setup() {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
				}
			});

			try {
				motorsController = new MotorsController(ioio_, 1, 2, 3, 16, 17, 14);
				motorsController.setSpeed(50);
				distanceSensor = new HCSR04DistanceSensor(ioio_, 13, 8, 9);
			} catch (ConnectionLostException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			if (distanceSensor.getResults() != null)
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
	//					textView.setText(distanceSensor.getResults().toString());
						barGraph.setValues(distanceSensor.getResultsOnly());
						// TODO Auto-generated method stub
						
					}
				});
			motorsController.setDirection(camera.getxTargetPosition());
			Log.d(TAG, camera.getxTargetPosition()+"");
			Thread.sleep(50);
		}

		@Override
		public void disconnected() {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), "Disonnected!", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private static final String TAG = "robot";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		speedBar = (VerticalSeekBar) findViewById(R.id.speedBar);
		directionBar = (SeekBar) findViewById(R.id.directionBar);
		
		if (speedBar != null)
		speedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int progress = 100;
				seekBar.setProgress(progress);
				this.onProgressChanged(seekBar, progress, false);	
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (motorsController != null) {
					motorsController.setSpeed(progress - 100);
				}
				if (textView != null) {
					textView.setText((progress - 100) + "");
				}
				Log.d(TAG, (progress - 100) + " progress");
			}
		});

		directionBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				seekBar.setProgress(100);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (motorsController != null) {
					motorsController.setDirection(progress - 100);
				}
				if (textView != null) {
					textView.setText((progress - 100) + "");
				}
				Log.d(TAG, (progress - 100) + " progress");
			}
		});

		camera = new Camera((CameraBridgeViewBase) findViewById(R.id.camera_view), this);
		barGraph = (SimpleBarGraph) findViewById(R.id.simpleBarGraph1);
		Log.d(TAG, "konstruktor");
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera.resume();
	}

}
