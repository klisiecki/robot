package pl.poznan.put.ioiorobot.widgets;

import pl.poznan.put.ioiorobot.mapobjects.AreaMap;
import pl.poznan.put.ioiorobot.mapobjects.Obstacle;
import pl.poznan.put.ioiorobot.mapobjects.Pattern;
import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.utils.C;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AreaMapWidget extends View {
	
	private Paint backgroundPaint;
	private Paint patternPaint;
	private Paint obstaclePaint;
	private Paint obstaclePaint2;
	private Paint robotPaint;

	private float scale = 10;

	private int width;
	private int height;

	private AreaMap areaMap;

	private Paint trackPaint;

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
		patternPaint.setStrokeWidth(1);
		patternPaint.setStyle(Paint.Style.FILL_AND_STROKE);

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
		// rysowanie markerów
		for (Pattern p : areaMap.getPatterns()) {
			addPoint(p.getPoint(), patternPaint, canvas);
		}

		// rysowanie przeszkód
		for (Obstacle o : areaMap.getObstacles()) {
			if (o.isAccepted()) addPoint(o.getPoint(), obstaclePaint, canvas);
		}

		for (Obstacle o : areaMap.getObstacles()) {
			if (!o.isAccepted()) addPoint(o.getPoint(), obstaclePaint2, canvas);
		}
		
		// rysowanie robota
		canvas.save();
		Position robotPosition = areaMap.getRobotPosition();
		int x = (int) (robotPosition.x() / scale + width / 2);
		int y = (int) (-robotPosition.y() / scale + height / 2);

		canvas.rotate((float) (180.0 * robotPosition.angle() / Math.PI), x, y);

		// robot
		canvas.drawRect(x - C.robotWidth / 2 / scale, y - C.robotLenght / scale, x + C.robotWidth / 2 / scale, y,
				robotPaint);

		// koło lewe
		canvas.drawRect(x - C.robotWidth / 2 / scale - C.robotWidth / 2 / scale / 3, y - C.robotLenght / scale / 3, x
				- C.robotWidth / 2 / scale, y, robotPaint);

		// koło prawe
		canvas.drawRect(x + C.robotWidth / 2 / scale, y - C.robotLenght / scale / 3, x + C.robotWidth / 2 / scale
				+ C.robotWidth / 2 / scale / 3, y, robotPaint);

		canvas.drawCircle(x, y, C.robotWidth / 8 / scale, robotPaint);
		canvas.restore();
		canvas.save();
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
