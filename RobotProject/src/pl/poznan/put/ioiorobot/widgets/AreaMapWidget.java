package pl.poznan.put.ioiorobot.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import pl.poznan.put.ioiorobot.mapobjects.AreaMap;
import pl.poznan.put.ioiorobot.mapobjects.Obstacle;
import pl.poznan.put.ioiorobot.mapobjects.Pattern;
import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.utils.C;
import pl.poznan.put.ioiorobot.utils.DAO;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class AreaMapWidget extends View {

	private Paint backgroundPaint;
	private Paint patternPaint;
	private Paint obstaclePaint;
	private Paint obstaclePaint2;
	private Paint robotPaint;

	private float scale = 6;

	private int width;
	private int height;

	private AreaMap areaMap;

	private Paint trackPaint;

	private static Timer timer;
	boolean requestSave = false;

	public void setAreaMap(AreaMap areaMap) {
		this.areaMap = areaMap;
	}

	public AreaMapWidget(Context context) {
		super(context);
		init();

	}

	public AreaMapWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AreaMapWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setFocusable(true);
		trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		trackPaint.setColor(Color.rgb(0x00, 0x99, 0x00));
		trackPaint.setStrokeWidth(1);
		trackPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setColor(Color.rgb(0x88, 0xee, 0xff));
		backgroundPaint.setStrokeWidth(1);
		backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		patternPaint.setColor(Color.rgb(0x00, 0xaa, 0x00));
		patternPaint.setStrokeWidth(5);
		patternPaint.setStyle(Paint.Style.STROKE);

		obstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		obstaclePaint.setColor(Color.rgb(0xdd, 0x00, 0x00));
		obstaclePaint.setStrokeWidth(1);
		obstaclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		obstaclePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		obstaclePaint2.setColor(Color.rgb(0xdd, 0xdd, 0x00));
		obstaclePaint2.setStrokeWidth(1);
		obstaclePaint2.setStyle(Paint.Style.FILL_AND_STROKE);

		robotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		robotPaint.setColor(Color.rgb(0x00, 0x00, 0xdd));
		robotPaint.setStrokeWidth(4);
		robotPaint.setStyle(Paint.Style.STROKE);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredWidth = 1000;
		int desiredHeight = 1000;
		width = measureSize(widthMeasureSpec, desiredWidth);
		height = measureSize(heightMeasureSpec, desiredHeight);
		width = height = Math.min(width, height);

		setMeasuredDimension(width, height);
	}

	private int measureSize(int measureSpec, int desired) {
		int result = 0;
		int mode = MeasureSpec.getMode(measureSpec);
		int size = MeasureSpec.getSize(measureSpec);

		if (mode == MeasureSpec.EXACTLY) {
			result = size;
		} else if (mode == MeasureSpec.AT_MOST) {
			result = Math.min(desired, size);
		} else {
			result = desired;
		}

		return result;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		Bitmap  bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), 
				  Bitmap.Config.ARGB_8888); 
		//canvas.setBitmap(bitmap);
		Canvas myCanvas = new Canvas(bitmap);
		
		// rysowanie markerów
		for (Pattern p : areaMap.getPatterns()) {
			Point position = p.getPoint();
			if (position != null) {
				Bitmap bmp = p.getBitmap();
				Rect source = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

				int x = (int) (position.x / scale + width / 2); // +
																// bmp.getWidth()
																// / 2
				int y = (int) (-position.y / scale + height / 2); // +
																	// bmp.getWidth()
																	// / 2

				int size = (int) (C.robotWidth * 2);
				Rect dest = new Rect(x - (int) (size / 2 / scale), y - (int) (size / 2 / scale), x
						+ (int) (size / 2 / scale), y + (int) (size / 2 / scale));

				canvas.drawBitmap(bmp, source, dest, patternPaint);
				myCanvas.drawBitmap(bmp, source, dest, patternPaint);
				addPoint(position, patternPaint, canvas);
			}

//			List<Position> viewPositions = new ArrayList<Position>(p.getViewPositions());
//			for (Position pos : viewPositions) {
//				canvas.drawLine(pos.getPoint().x / scale + width / 2, -pos.getPoint().y / scale + width / 2,
//						pos.getVectorPoint().x / scale + width / 2, -pos.getVectorPoint().y / scale + width / 2,
//						patternPaint);
//			}

		}

		// rysowanie przeszkód
		for (Obstacle o : areaMap.getObstacles()) {
			if (o.isAccepted())
				addPoint(o.getPoint(), obstaclePaint, canvas);
		}

		for (Obstacle o : areaMap.getObstacles()) {
			if (!o.isAccepted())
				addPoint(o.getPoint(), obstaclePaint2, canvas);
		}

		// rysowanie robota
		canvas.save();
		myCanvas.save();
		Position robotPosition = areaMap.getRobotPosition();
		int x = (int) (robotPosition.x() / scale + width / 2);
		int y = (int) (-robotPosition.y() / scale + height / 2);

		canvas.rotate((float) (180.0 * robotPosition.angle() / Math.PI), x, y);
		myCanvas.rotate((float) (180.0 * robotPosition.angle() / Math.PI), x, y);

		// robot
		canvas.drawRect(x - C.robotWidth / 2 / scale, y - C.robotLenght / scale, x + C.robotWidth / 2 / scale, y, robotPaint);
		myCanvas.drawRect(x - C.robotWidth / 2 / scale, y - C.robotLenght / scale, x + C.robotWidth / 2 / scale, y, robotPaint);

		// koło lewe
		canvas.drawRect(x - C.robotWidth / 2 / scale - C.robotWidth / 2 / scale / 3, y - C.robotLenght / scale / 3, x - C.robotWidth / 2 / scale, y, robotPaint);
		myCanvas.drawRect(x - C.robotWidth / 2 / scale - C.robotWidth / 2 / scale / 3, y - C.robotLenght / scale / 3, x - C.robotWidth / 2 / scale, y, robotPaint);

		// koło prawe
		canvas.drawRect(x + C.robotWidth / 2 / scale, y - C.robotLenght / scale / 3, x + C.robotWidth / 2 / scale + C.robotWidth / 2 / scale / 3, y, robotPaint);
		myCanvas.drawRect(x + C.robotWidth / 2 / scale, y - C.robotLenght / scale / 3, x + C.robotWidth / 2 / scale + C.robotWidth / 2 / scale / 3, y, robotPaint);

		canvas.drawCircle(x, y, C.robotWidth / 8 / scale, robotPaint);
		myCanvas.drawCircle(x, y, C.robotWidth / 8 / scale, robotPaint);
		canvas.restore();
		myCanvas.restore();
		canvas.save();
		myCanvas.save();
		
		Log.e(C.TAG, "onDraw");
		if(requestSave) {
			DAO.savetBitmap(bitmap, "map"+Calendar.getInstance().get(Calendar.MILLISECOND));
			requestSave = false;
		}
	}
	
	public void saveBitmap() {
		requestSave = true;
	}

	private void addPoint(Point p, Paint paint, Canvas canvas) {
		if (p != null) {
			int x = (int) (p.x / scale + width / 2);
			int y = (int) (-p.y / scale + height / 2);

			canvas.drawCircle(x, y, 3, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		invalidate();
		return super.onTouchEvent(event);
	}

}
