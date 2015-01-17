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
import pl.poznan.put.ioiorobot.camera.MyJavaCameraView;
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
import pl.poznan.put.ioiorobot.sensors.FrontDistanceSensor;
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
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

/**
 * Główna klasa aplikacji. Zawiera wszystkie widoki oraz główną pętlę programu.
 *
 */
public class RobotActivity extends IOIOActivity {

	private SeekBar seekBar1;
	private SeekBar seekBar2;
	private SeekBar seekBar3;

	// Views
	private Joystick joystick;
	private SimpleBarGraph barGraph;
	private ToggleButton flashlightButton;
	private ToggleButton cameraButton;
	private ToggleButton sensorsButton;
	private ToggleButton startButton;
	private BatteryStatusBar batteryStatusBar;
	private PatternsWidget patternsWidget;
	private MapWidget mapWidget;
	private AreaMapWidget areaMapWidget;
	private AreaMapWidget areaMapWidgetBig;
	private ViewFlipper mapViewFlipper;
	private MyJavaCameraView javaCameraView;

	// Controls
	private MyCamera camera;
	private IMotorsController motorsController;
	private IDistanceSensor distanceSensor;
	private IBatteryStatus batteryStatus;
	private FrontDistanceSensor frontDistanceSensor;
	private EncodersData encodersData;
	private RobotController controller;
	private PatternsQueue patternsQueue;
	private ObstacleManager obstacleManager;

	private AreaMap areaMap;
	private Position robotPosition;

	private Point screenSize;
	
	private Button capMockBtn;
	private ToggleButton mockingBtn;

	class Looper extends BaseIOIOLooper {

		@Override
		protected void setup() {
			showToast("Connected");

			try {
				encodersData = new EncodersData(ioio_, 27, 28, 26, 115200, Uart.Parity.NONE, Uart.StopBits.ONE,
						robotPosition);
				motorsController = new MotorsController(ioio_, 16, 17, 14, 1, 2, 3, encodersData);
				frontDistanceSensor = new FrontDistanceSensor(ioio_, 6, 7, 8, 9, 10, 11, C.minFreeDistance);
				distanceSensor = new SharpDistanceSensor(ioio_, 13, 33);
				batteryStatus = new BatteryStatus(ioio_, 46);
				controller = new RobotController(robotPosition, camera, motorsController, frontDistanceSensor);
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
				}
			});

			Log.d(C.TAG,
					"frontDistanceSensor: " + frontDistanceSensor.isFreeLeft() + "\t"
							+ frontDistanceSensor.isFreeCenter() + "\t" + frontDistanceSensor.isFreeRight());

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
		initWindow();
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

	private void initWindow() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		getWindow().getDecorView().setSystemUiVisibility(uiOptions);

	}

	private void initView() {
		setContentView(R.layout.activity_main);

		javaCameraView = (MyJavaCameraView) findViewById(R.id.camera_view);
		camera = new MyCamera((CameraBridgeViewBase) javaCameraView, this);
		joystick = (Joystick) findViewById(R.id.joystick);
		barGraph = (SimpleBarGraph) findViewById(R.id.distanceBarGraph);
		flashlightButton = (ToggleButton) findViewById(R.id.flashlightButton);
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
		areaMapWidgetBig = (AreaMapWidget) findViewById(R.id.areaMapWidgetBig);
		areaMapWidget.setAreaMap(areaMap);
		areaMapWidgetBig.setAreaMap(areaMap);
		mapViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		
		
		javaCameraView.getCamera();
		
		capMockBtn = (Button) findViewById(R.id.camMockButton);
		mockingBtn = (ToggleButton) findViewById(R.id.mockImage);
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
		
		flashlightButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Camera mCamera = javaCameraView.getCamera();
				Camera.Parameters param = mCamera.getParameters();
				param.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH: Camera.Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(param);				
			}
		});

		cameraButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
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
					// camera.setMode(MyCamera.Mode.MOCK);
					controller.start();
				} else {
					// camera.setMode(MyCamera.Mode.CAMERA_ONLY);
					controller.interrupt();
					motorsController.stop();
				}
			}
		});

		camera.setPatternFoundListener(new PatternFoundListener() {

			@Override
			public void onPatternFound(final Pattern pattern) {
				pattern.addViewPosition(new Position(robotPosition, C.cameraShift));
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
				areaMapWidget.invalidate();
				areaMapWidgetBig.invalidate();

			}
		});

		
		mockingBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				camera.setMocking(isChecked);
				
			}
		});
		
		capMockBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				camera.saveMock();
				
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
			public void onResult(final List<Integer> results, final IDistanceSensor.AngleDistancePair last) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						barGraph.setValues(results);
						// Log.d(C.TAG, "\t\tDISTANCE = " + last.distance);
						if (last.distance < C.maxObstacleDistance) {
							obstacleManager.addObstacle(new Obstacle(robotPosition, last));
							areaMapWidgetBig.invalidate();
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
						areaMapWidgetBig.invalidate();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(android.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.showMap:
			mapViewFlipper.showNext();
			return true;
		case R.id.showDebug:
			camera.switchDebug();
			return true;
		case R.id.saveMap:
			areaMapWidget.saveBitmap();
			areaMapWidget.invalidate();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	};
}
