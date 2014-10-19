package pl.poznan.put.ioiorobot;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.List;

import org.opencv.android.CameraBridgeViewBase;

import pl.poznan.put.ioiorobot.camera.Camera;
import pl.poznan.put.ioiorobot.motors.IMotorsController;
import pl.poznan.put.ioiorobot.motors.MotorsController;
import pl.poznan.put.ioiorobot.sensors.HCSR04DistanceSensor;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.widgets.SimpleBarGraph;
import pl.poznan.put.ioiorobot.widgets.VerticalSeekBar;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class MainActivity extends IOIOActivity {
	
	private static final String TAG = "robot";

	// Views
	private SeekBar speedBar;
	private SeekBar directionBar;
	private SimpleBarGraph barGraph;

	// Controls
	private IMotorsController motorsController;
	private IDistanceSensor distanceSensor;
	private Camera camera;

	class Looper extends BaseIOIOLooper {
		
		@Override
		protected void setup() {
			showToast("Connected");

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
			List<Integer> distances;
			if (distanceSensor.getResults() != null) {
				distances = distanceSensor.getResultsOnly();
				runOnUiThread(new Runnable() {
					public void run() {
						barGraph.setValues(distanceSensor.getResultsOnly());
					}
				});
				int val = distances.get(distances.size()/2);
				motorsController.setSpeed(val  > 10 ? 50 : 0);
			}
			motorsController.setDirection(camera.getxTargetPosition());
			Thread.sleep(100);
		}

		@Override
		public void disconnected() {
			showToast("Disconnected!");
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initView();
		initListeners();
		Log.d(TAG, "onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera.resume();
	}

	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		
		speedBar = (VerticalSeekBar) findViewById(R.id.speedBar);
		directionBar = (SeekBar) findViewById(R.id.directionBar);
		camera = new Camera((CameraBridgeViewBase) findViewById(R.id.camera_view), this);
		barGraph = (SimpleBarGraph) findViewById(R.id.simpleBarGraph1);
	}

	private void initListeners() {
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
			}
		});
	}
	
	private void showToast(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
