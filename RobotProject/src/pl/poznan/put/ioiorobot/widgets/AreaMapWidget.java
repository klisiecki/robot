package pl.poznan.put.ioiorobot.widgets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import pl.poznan.put.ioiorobot.mapping.AreaMap;
import pl.poznan.put.ioiorobot.mapping.Obstacle;
import pl.poznan.put.ioiorobot.mapping.Pattern;
import pl.poznan.put.ioiorobot.positioning.Position;
import pl.poznan.put.ioiorobot.utils.Config;
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

	private float scale = 5;

	private int widgetWidth;
	private int widgetHeight;

	private AreaMap areaMap;

	private Paint trackPaint;
	private Bitmap bitmap;

	private static int offset = 0;
	private Point tl = new Point(Config.mapSize / 2 - offset, Config.mapSize / 2 - offset);
	private Point br = new Point(Config.mapSize / 2 + offset, Config.mapSize / 2 + offset);
	
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
		patternPaint.setStrokeWidth(2);
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

		bitmap = Bitmap.createBitmap(Config.mapSize, Config.mapSize, Bitmap.Config.ARGB_8888);
	}

	private float widgetRatio;

	private float ratio(int x, int y) {
		return (float) x / (float) y;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredWidth = 1920;
		int desiredHeight = 1080;
		widgetWidth = measureSize(widthMeasureSpec, desiredWidth);
		widgetHeight = measureSize(heightMeasureSpec, desiredHeight);
		widgetRatio = ratio(widgetWidth, widgetHeight);
		setMeasuredDimension(widgetWidth, widgetHeight);
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
		bitmap.eraseColor(Color.TRANSPARENT);
		Canvas myCanvas = new Canvas(bitmap);

		drawObstacles(myCanvas);
		drawPatterns(myCanvas);
		if (!requestSave) {
			drawRobot(myCanvas);
		}

		int mapWidth = br.x - tl.x;
		int mapHeight = br.y - tl.y;
		Bitmap bmp = Bitmap.createBitmap(bitmap, tl.x, tl.y, mapWidth, mapHeight);
		float mapRatio = ratio(mapWidth, mapHeight);
		int dstWidth, dstHeight;
		if (widgetRatio < mapRatio) {
			dstWidth = widgetWidth;
			dstHeight = (int) (((float) widgetWidth / (float) mapWidth) * mapHeight);
		} else {
			dstWidth = (int) (((float) widgetHeight / (float) mapHeight) * mapWidth);
			dstHeight = widgetHeight;
		}
		Bitmap bmpScaled = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
		canvas.drawBitmap(bmpScaled, 0, 0, backgroundPaint);

		if (requestSave) {
			DAO.saveBitmap(bmpScaled, "map_" + Calendar.getInstance().getTime());
			requestSave = false;
		}
	}

	private void drawPatterns(Canvas myCanvas) {
		for (Pattern p : areaMap.getPatterns()) {
			Point position = p.getPoint();
			if (position != null) {
				Bitmap bmp = p.getBitmap();
				Rect source = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

				int x = (int) (position.x / scale + Config.mapSize / 2);
				int y = (int) (-position.y / scale + Config.mapSize / 2); 

				int size = 200;
				Rect dest = new Rect(x - (int) (size / 2 / scale), y - (int) (size / 2 / scale), x
						+ (int) (size / 2 / scale), y + (int) (size / 2 / scale));

				myCanvas.drawBitmap(bmp, source, dest, patternPaint);
				addPoint(position, patternPaint, myCanvas);
//				drawPatternViewPositions(myCanvas, p);
			}
		}
	}

	private void drawObstacles(Canvas canvas) {
		for (Obstacle o : areaMap.getObstacles()) {
			if (o.isAccepted())
				addPoint(o.getPoint(), obstaclePaint, canvas);
		}
	}

	private void drawPatternViewPositions(Canvas canvas, Pattern p) {
		List<Position> viewPositions = new ArrayList<Position>(p.getViewPositions());
		for (Position pos : viewPositions) {
			canvas.drawLine(pos.getPoint().x / scale + Config.mapSize / 2, -pos.getPoint().y / scale + Config.mapSize
					/ 2, pos.getVectorPoint().x / scale + Config.mapSize / 2, -pos.getVectorPoint().y / scale
					+ Config.mapSize / 2, patternPaint);
		}
	}

	private void drawRobot(Canvas canvas) {
		canvas.save();
		Position robotPosition = areaMap.getRobotPosition();
		int x = (int) (robotPosition.x() / scale + Config.mapSize / 2);
		int y = (int) (-robotPosition.y() / scale + Config.mapSize / 2);

		canvas.rotate((float) (180.0 * robotPosition.angle() / Math.PI), x, y);

		// robot
		canvas.drawRect(x - Config.robotWidth / 2 / scale, y - Config.robotLenght / scale, x + Config.robotWidth / 2
				/ scale, y, robotPaint);

		// koło lewe
		canvas.drawRect(x - Config.robotWidth / 2 / scale - Config.robotWidth / 2 / scale / 3, y - Config.robotLenght
				/ scale / 3, x - Config.robotWidth / 2 / scale, y, robotPaint);

		// koło prawe
		canvas.drawRect(x + Config.robotWidth / 2 / scale, y - Config.robotLenght / scale / 3, x + Config.robotWidth
				/ 2 / scale + Config.robotWidth / 2 / scale / 3, y, robotPaint);

		canvas.drawCircle(x, y, Config.robotWidth / 8 / scale, robotPaint);
		canvas.restore();
		canvas.save();

		tl.x = Math.min(tl.x, x - 50);
		tl.y = Math.min(tl.y, y - 50);

		br.x = Math.max(br.x, x + 50);
		br.y = Math.max(br.y, y + 50);
	}

	public void saveBitmap() {
		requestSave = true;
	}


	private void addPoint(Point p, Paint paint, Canvas canvas) {
		if (p != null) {
			int x = (int) (p.x / scale + Config.mapSize / 2);
			int y = (int) (-p.y / scale + Config.mapSize / 2);
			canvas.drawCircle(x, y, 1, paint);

			tl.x = Math.min(tl.x, x - 20);
			tl.y = Math.min(tl.y, y - 20);

			br.x = Math.max(br.x, x + 20);
			br.y = Math.max(br.y, y + 20);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		invalidate();
		return super.onTouchEvent(event);
	}
}
