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
import pl.poznan.put.ioiorobot.mapobjects.AreaMap;
import pl.poznan.put.ioiorobot.mapobjects.Obstacle;
import pl.poznan.put.ioiorobot.mapobjects.ObstacleManager;
import pl.poznan.put.ioiorobot.mapobjects.ObstacleManager.ObstacleAcceptedListener;
import pl.poznan.put.ioiorobot.mapobjects.Pattern;
import pl.poznan.put.ioiorobot.mapobjects.PatternsQueue;
import pl.poznan.put.ioiorobot.mapobjects.PatternsQueue.PatternAcceptedListener;
import pl.poznan.put.ioiorobot.motors.EncodersData;
import pl.poznan.put.ioiorobot.motors.EncodersData.PositionChangedListener;
import pl.poznan.put.ioiorobot.motors.IMotorsController;
import pl.poznan.put.ioiorobot.motors.MotorsController;
import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.sensors.BatteryStatus;
import pl.poznan.put.ioiorobot.sensors.IBatteryStatus;
import pl.poznan.put.ioiorobot.sensors.IBatteryStatus.BatteryStatusChangedListener;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor;
import pl.poznan.put.ioiorobot.sensors.IDistanceSensor.DistanceResultListener;
import pl.poznan.put.ioiorobot.sensors.SharpDistanceSensor;
import pl.poznan.put.ioiorobot.utils.C;
import pl.poznan.put.ioiorobot.utils.DAO;
import pl.poznan.put.ioiorobot.widgets.AreaMapWidget;
import pl.poznan.put.ioiorobot.widgets.BatteryStatusBar;
import pl.poznan.put.ioiorobot.widgets.Joystick;
import pl.poznan.put.ioiorobot.widgets.Joystick.JoystickMovedListener;
import pl.poznan.put.ioiorobot.widgets.MapWidget;
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
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Główna klasa aplikacji. Zawiera wszystkie widoki oraz główną pętlę programu.
 *
 */
public class RobotActivity extends IOIOActivity {

	// Views
	private Joystick joystick;
	private SimpleBarGraph barGraph;
	private ToggleButton cameraButton;
	private ToggleButton sensorsButton;
	private ToggleButton startButton;
	private SeekBar seekBar1;
	private SeekBar seekBar2;
	private SeekBar seekBar3;
	private BatteryStatusBar batteryStatusBar;
	private PatternsWidget patternsWidget;
	private MapWidget mapWidget;
	private AreaMapWidget areaMapWidget;

	// Controls
	private MyCamera camera;
	private IMotorsController motorsController;
	private IDistanceSensor distanceSensor;
	private IBatteryStatus batteryStatus;
	private EncodersData encodersData;
	
	private Position robotPosition;
	private PatternsQueue patternsQueue;
	private ObstacleManager obstacleManager;
	private AreaMap areaMap;
	
	private Point screenSize;

	class Looper extends BaseIOIOLooper {

		@Override
		protected void setup() {
			showToast("Connected");

			try {
				motorsController = new MotorsController(ioio_, 16, 17, 14, 1, 2, 3);
				// distanceSensor = new HCSR04DistanceSensor(ioio_, 13, 8, 9);
				distanceSensor = new SharpDistanceSensor(ioio_, 13, 33);
				batteryStatus = new BatteryStatus(ioio_, 46);
				encodersData = new EncodersData(ioio_, 27, 28, 26, 115200, Uart.Parity.NONE, Uart.StopBits.ONE,
						robotPosition);
				initIOIOListeners();
			} catch (ConnectionLostException e) {
				Log.e(C.TAG, e.toString());
				e.printStackTrace();
			}
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			runOnUiThread(new Runnable() {
				public void run() {
					seekBar3.setProgress(100 + motorsController.getRegulacja());
					areaMapWidget.invalidate();
				}
			});

			Thread.sleep(C.loopSleep);
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
		initObjects();
		initView();
		initListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera.resume();
	}

	private void initObjects() {
		patternsQueue = new PatternsQueue();
		obstacleManager = new ObstacleManager();
		robotPosition = new Position();
		areaMap = new AreaMap(robotPosition);
		DAO.setContext(getApplicationContext());
		screenSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(screenSize);
		C.patternSize = Math.min(screenSize.y, screenSize.x) / 7; // było /4
		C.screenSize = screenSize;
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
		startButton = (ToggleButton) findViewById(R.id.startToggleButton);
		seekBar1 = (SeekBar) findViewById(R.id.seekBar1);
		seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
		seekBar3 = (SeekBar) findViewById(R.id.seekBar3);
		batteryStatusBar = (BatteryStatusBar) findViewById(R.id.batteryStatusBar);
		patternsWidget = (PatternsWidget) findViewById(R.id.patternsWidget);
		mapWidget = (MapWidget) findViewById(R.id.mapWidget);
		areaMapWidget = (AreaMapWidget) findViewById(R.id.areaMapWidget);
		areaMapWidget.setAreaMap(areaMap);
	}

	private void initListeners() {

		joystick.setJostickMovedListener(new JoystickMovedListener() {

			@Override
			public void onReleased() {
				if (motorsController != null) {
					motorsController.setDirection(0);
					motorsController.setSpeed(0);
				}
			}

			@Override
			public void onMoved(int xPos, int yPos) {
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
		
		
		startButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					camera.setMode(MyCamera.Mode.MOCK);
				} else {
					camera.setMode(MyCamera.Mode.CAMERA_ONLY);
				}
			}
		});
		

		camera.setPatternFoundListener(new PatternFoundListener() {

			@Override
			public void onPatternFound(final Pattern pattern) {
				pattern.addViewPosition(new Position(robotPosition));
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
						areaMap.addPattern(pattern);
					}
				});
			}
		});
		
		obstacleManager.setObstacleAcceptedListener(new ObstacleAcceptedListener() {
			
			@Override
			public void onObstacleAccepted(Obstacle obstacle) {
				areaMap.addObstacle(obstacle);
				
			}
		});

	}

	private void initIOIOListeners() {
		batteryStatus.setBatteryStatusChangedListener(new BatteryStatusChangedListener() {

			@Override
			public void onBatteryStatusChanged(final int status) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						batteryStatusBar.setValue(status);
					}
				});

			}
		});

		distanceSensor.setDistanceResultListener(new DistanceResultListener() {

			@Override
			public void onResult(final List<Integer> results, final IDistanceSensor.Pair last) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						barGraph.setValues(results);
						//Log.d(C.TAG, "\t\tDISTANCE = " + last.distance);
						if (last.distance < C.maxObstacleDistance) {
							obstacleManager.addObstacle(new Obstacle(robotPosition, last));
						}
					}
				});

			}
		});
		
		encodersData.setPositionChangedListener(new PositionChangedListener() {

			@Override
			public void onPositionChanged(final Position position) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mapWidget.addPosition(position);
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
