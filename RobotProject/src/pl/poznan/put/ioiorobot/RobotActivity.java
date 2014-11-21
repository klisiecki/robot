package pl.poznan.put.ioiorobot;

import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.List;

import org.opencv.android.CameraBridgeViewBase;

import pl.poznan.put.ioiorobot.camera.MyCamera;
import pl.poznan.put.ioiorobot.motors.EncodersData;
import pl.poznan.put.ioiorobot.motors.IMotorsController;
import pl.poznan.put.ioiorobot.motors.MotorsController;
import pl.poznan.put.ioiorobot.sensors.BatteryStatus;
import pl.poznan.put.ioiorobot.sensors.HCSR04DistanceSensor;
import pl.poznan.put.ioiorobot.sensors.IAccelerometer;
import pl.poznan.put.ioiorobot.sensors.IBatteryStatus;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.utils.MyConfig;
import pl.poznan.put.ioiorobot.utils.DAO;
import pl.poznan.put.ioiorobot.widgets.BatteryStatusBar;
import pl.poznan.put.ioiorobot.widgets.Joystick;
import pl.poznan.put.ioiorobot.widgets.JoystickMovedListener;
import pl.poznan.put.ioiorobot.widgets.SimpleBarGraph;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RobotActivity extends IOIOActivity {

	private static final String TAG = "robot";

	// Views
	private Joystick joystick;
	private SimpleBarGraph barGraph;
	private ToggleButton cameraButton;
	private ToggleButton sensorsButton;
	private RelativeLayout layout;
	private SeekBar seekBar1;
	private SeekBar seekBar2;
	private SeekBar seekBar3;
	private BatteryStatusBar batteryStatusBar;

	// Controls
	private IMotorsController motorsController;
	private IDistanceSensor distanceSensor;
	private IBatteryStatus batteryStatus;
	private IAccelerometer accelerometer;
	private EncodersData encodersData;
	private MyCamera camera;

	private Point screenSize = new Point();

	class Looper extends BaseIOIOLooper {

		@Override
		protected void setup() {
			showToast("Connected");

			try {
				motorsController = new MotorsController(ioio_, 16, 17, 14, 1, 2, 3);
				distanceSensor = new HCSR04DistanceSensor(ioio_, 13, 8, 9);
				batteryStatus = new BatteryStatus(ioio_, 46);
				encodersData = new EncodersData(ioio_, 27, 28, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
			} catch (ConnectionLostException e) {
				Log.e(TAG, e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {

			if (distanceSensor.getResults() != null) {
				List<Integer> distances = distanceSensor.getResultsOnly();
				runOnUiThread(new Runnable() {
					public void run() {
						barGraph.setValues(distanceSensor.getResultsOnly());
					}
				});
			}

			if (cameraButton.isChecked()) {
				motorsController.setDirection(camera.getxTargetPosition());

				// Log.d("robot", "camera.getxTargetPosition(): " +
				// camera.getxTargetPosition());

				if (distanceSensor.getResults() != null && sensorsButton.isChecked()) {
					List<Integer> distances = distanceSensor.getResultsOnly();
					int val = distances.get(distances.size() / 2);
					motorsController.setSpeed(val > 10 ? 50 : 0);
				} else {
					motorsController.setSpeed(50);
				}
			}

			runOnUiThread(new Runnable() {
				public void run() {
					batteryStatusBar.setValue(batteryStatus.getStatus());

					seekBar3.setProgress(100 + motorsController.getRegulacja());
				}
			});

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
		DAO.setContext(getApplicationContext());
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		MyConfig.patternSize = screenSize.y / 4;
		// cam = Camera.open();
		// Parameters params = cam.getParameters();
		// params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		// cam.setParameters(params);

		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and
		// higher, but as
		// a general rule, you should design your app to hide the status bar
		// whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		decorView.setSystemUiVisibility(uiOptions);

		Log.d(TAG, "onCreate");
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		camera.resume();
	}

	private void initView() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		camera = new MyCamera((CameraBridgeViewBase) findViewById(R.id.camera_view), this);
		joystick = (Joystick) findViewById(R.id.joystick);
		barGraph = (SimpleBarGraph) findViewById(R.id.distanceBarGraph);
		cameraButton = (ToggleButton) findViewById(R.id.cameraToggleButton);
		sensorsButton = (ToggleButton) findViewById(R.id.sensorToggleButton);
		layout = (RelativeLayout) findViewById(R.id.relative2);
		// accelerometer = new Accelerometer((SensorManager)
		// (getSystemService(SENSOR_SERVICE)));
		seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
		seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
		batteryStatusBar = (BatteryStatusBar) findViewById(R.id.batteryStatusBar);
	}

	private void initListeners() {

		joystick.setJostickMovedListener(new JoystickMovedListener() {

			@Override
			public void OnReleased() {
				if (motorsController != null) {
					motorsController.setDirection(0);
					motorsController.setSpeed(0);
				}
			}

			@Override
			public void OnMoved(int xPos, int yPos) {
				if (motorsController != null) {
					motorsController.setDirection(xPos);
					motorsController.setSpeed((int) (Math.sqrt(xPos * xPos + yPos * yPos) * (yPos > 0 ? 1 : -1)));
				}

			}
		});

		cameraButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// motorsController.setSpeed(0);
					// motorsController.setDirection(0);
					camera.setMode(MyCamera.Mode.PROCESSING);
				} else {
					camera.setMode(MyCamera.Mode.CAMERA_ONLY);
				}
			}
		});

		sensorsButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (distanceSensor != null) {
					if (isChecked) {
						distanceSensor.startSensor();
					} else {
						distanceSensor.stopSensor();
					}
				}

			}
		});
	}

	private void handleTouch(int x, int y) {
		int slotSize = screenSize.y / 4;
		if (y < slotSize) {
			showToast("slot " + x / slotSize);
		}
	}

	private void showToast(final String message) {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}
}
