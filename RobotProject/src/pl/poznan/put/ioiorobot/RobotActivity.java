package pl.poznan.put.ioiorobot;

import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.util.List;

import org.opencv.android.CameraBridgeViewBase;

import pl.poznan.put.ioiorobot.camera.MyCamera;
import pl.poznan.put.ioiorobot.camera.MyCamera.PatternFoundListener;
import pl.poznan.put.ioiorobot.camera.Pattern;
import pl.poznan.put.ioiorobot.camera.PatternsQueue;
import pl.poznan.put.ioiorobot.camera.PatternsQueue.PatternAcceptedListener;
import pl.poznan.put.ioiorobot.motors.EncodersData;
import pl.poznan.put.ioiorobot.motors.IMotorsController;
import pl.poznan.put.ioiorobot.motors.MotorsController;
import pl.poznan.put.ioiorobot.motors.PositionController;
import pl.poznan.put.ioiorobot.sensors.BatteryStatus;
import pl.poznan.put.ioiorobot.sensors.HCSR04DistanceSensor;
import pl.poznan.put.ioiorobot.sensors.IAccelerometer;
import pl.poznan.put.ioiorobot.sensors.IBatteryStatus;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.utils.DAO;
import pl.poznan.put.ioiorobot.utils.C;
import pl.poznan.put.ioiorobot.widgets.BatteryStatusBar;
import pl.poznan.put.ioiorobot.widgets.Joystick;
import pl.poznan.put.ioiorobot.widgets.JoystickMovedListener;
import pl.poznan.put.ioiorobot.widgets.PatternsWidget;
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
	private SeekBar seekBar1;
	private SeekBar seekBar2;
	private SeekBar seekBar3;
	private BatteryStatusBar batteryStatusBar;
	private PatternsWidget patternsWidget;

	// Controls
	private MyCamera camera;
	private IMotorsController motorsController;
	private IDistanceSensor distanceSensor;
	private IBatteryStatus batteryStatus;
	private IAccelerometer accelerometer;
	private EncodersData encodersData;
	private PositionController positionController;
	private PatternsQueue patternsQueue;
	private Point screenSize = new Point();

	class Looper extends BaseIOIOLooper {

		@Override
		protected void setup() {
			showToast("Connected");

			try {
				motorsController = new MotorsController(ioio_, 16, 17, 14, 1, 2, 3);
				distanceSensor = new HCSR04DistanceSensor(ioio_, 13, 8, 9);
				batteryStatus = new BatteryStatus(ioio_, 46);
				encodersData = new EncodersData(ioio_, 27, 28, 9600, Uart.Parity.NONE, Uart.StopBits.ONE, positionController);
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
		patternsQueue = new PatternsQueue();
		positionController = new PositionController();
		initView();
		initListeners();
		DAO.setContext(getApplicationContext());
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		C.patternSize = screenSize.y / 4;
		C.screenSize = screenSize;

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
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		getWindow().getDecorView().setSystemUiVisibility(uiOptions);
		
		setContentView(R.layout.activity_main);

		camera = new MyCamera((CameraBridgeViewBase) findViewById(R.id.camera_view), this);
		joystick = (Joystick) findViewById(R.id.joystick);
		barGraph = (SimpleBarGraph) findViewById(R.id.distanceBarGraph);
		cameraButton = (ToggleButton) findViewById(R.id.cameraToggleButton);
		sensorsButton = (ToggleButton) findViewById(R.id.sensorToggleButton);
		seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
		seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
		batteryStatusBar = (BatteryStatusBar) findViewById(R.id.batteryStatusBar);
		patternsWidget = (PatternsWidget) findViewById(R.id.patternsWidget1);
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

		camera.setPatternFoundListener(new PatternFoundListener() {

			@Override
			public void onPatternFound(final Pattern pattern) {
				Log.d("robot", "pattern found actity");
				patternsQueue.add(pattern);
			}
		});
		
		patternsQueue.setPatternAcceptedListener(new PatternAcceptedListener() {
			
			@Override
			public void onPatternAccepted(final Pattern pattern) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						patternsWidget.addPattern(pattern);
					}
				});
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
