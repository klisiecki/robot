package pl.poznan.put.ioiorobot.mapobjects;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.ioiorobot.motors.Position;
import pl.poznan.put.ioiorobot.utils.C;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

public class AreaMap {
	private List<Pattern> patterns = new ArrayList<Pattern>();
	private List<Obstacle> obstacles = new ArrayList<Obstacle>();
	private Position robotPosition;

	// TEMP start
	private Bitmap bmp;
	private Canvas canvasX;
	private Paint backgroundPaint;
	private Paint patternPaint;
	private Paint obstaclePaint;
	private Paint robotPaint;

	private double scale = 10;
	private int width = 500;
	private int height = 500;

	public AreaMap(Position robotPosition) {
		this.robotPosition = robotPosition;

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setColor(Color.rgb(0x88, 0xee, 0xff));
		backgroundPaint.setStrokeWidth(1);
		backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		patternPaint.setColor(Color.rgb(0x00, 0xff, 0x00));
		patternPaint.setStrokeWidth(1);
		patternPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		obstaclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		obstaclePaint.setColor(Color.rgb(0xff, 0x00, 0x00));
		obstaclePaint.setStrokeWidth(1);
		obstaclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

		robotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		robotPaint.setColor(Color.rgb(0x00, 0x00, 0xff));
		robotPaint.setStrokeWidth(1);
		robotPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		canvasX = new Canvas(bmp);
	}

	private void addPoint(Point p, Paint paint) {
		if (p != null) {
			int x = (int) (-p.x / scale + width / 2);
			int y = (int) (-p.y / scale + height / 2);

			canvasX.drawCircle(x, y, 6, paint);
		}
		// Log.d(C.TAG, "point: " + p);
	}

	public Bitmap drawMap() {
		Log.d(C.TAG, "drawMap");
		bmp.eraseColor(Color.WHITE);
		// canvasX.drawRect(0, 0, width, height, patternPaint);
		for (Pattern p : patterns) {
			addPoint(p.getPoint(), patternPaint);
		}

		for (Obstacle o : obstacles) {
			addPoint(o, obstaclePaint);
		}

		Log.d(C.TAG, "\t\t\t\t\t\t\trobot position: " + robotPosition.getPoint());
		canvasX.save();
		
		int x = (int) (-robotPosition.x() / scale + width / 2);
		int y = (int) (-robotPosition.y() / scale + height / 2);
		canvasX.rotate((float) (-180.0*robotPosition.angle()/Math.PI), (float) x, (float) y);

		
		
		canvasX.drawRect((float) (x - C.robotWidth / 2 / scale),
				(float) (y - C.robotWidth / scale), (float) (x + C.robotWidth / 2
						/ scale), (float) (y), robotPaint);
		canvasX.drawCircle(x, y, 6, patternPaint);
		canvasX.restore();
		
		return bmp;
	}

	// TEMP end

	public void addPattern(Pattern pattern) {
		patterns.add(pattern);
	}

	public void addObstacle(Obstacle obstacle) {
		obstacles.add(obstacle);
	}
}
