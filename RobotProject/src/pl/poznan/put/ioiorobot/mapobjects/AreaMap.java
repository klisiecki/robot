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

/**
 * Klasa reprezentująca mapę przestrzeni tworzoną przez robota
 */
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

	private float scale = 6;
	private int width = 300;
	private int height = 300;

	public AreaMap(Position robotPosition) {
		this.robotPosition = robotPosition;

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

		robotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		robotPaint.setColor(Color.rgb(0x00, 0x00, 0xdd));
		robotPaint.setStrokeWidth(4);
		robotPaint.setStyle(Paint.Style.STROKE);

		bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		canvasX = new Canvas(bmp);
	}
	
	public void setSize(int width, int height){
		this.width = width;
		this.height = height;
		bmp = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888);
		canvasX = new Canvas(bmp);
	}

	private void addPoint(Point p, Paint paint) {
		if (p != null) {
			int x = (int) (p.x / scale + width / 2);
			int y = (int) (-p.y / scale + height / 2);

			canvasX.drawCircle(x, y, 3, paint);
		}
	}

	public Bitmap drawMap() {
		//Log.d(C.TAG, "drawMap");
		bmp.eraseColor(Color.WHITE);
		// canvasX.drawRect(0, 0, width, height, patternPaint);
		
		//rysowanie markerów
		for (Pattern p : patterns) {
			addPoint(p.getPoint(), patternPaint);
		}

		//rysowanie przeszkód
		for (Obstacle o : obstacles) {
			addPoint(o, obstaclePaint);
		}

		//rysowanie robota
		canvasX.save();
		int x = (int) (robotPosition.x() / scale + width / 2);
		int y = (int) (-robotPosition.y() / scale + height / 2);
		
		canvasX.rotate((float) (180.0 * robotPosition.angle() / Math.PI), x, y);

		// robot
		canvasX.drawRect(x - C.robotWidth / 2 / scale, 
						 y - C.robotLenght / scale, 
						 x + C.robotWidth / 2 / scale, 
						 y,
				robotPaint);
		
		// koło lewe
		canvasX.drawRect(x - C.robotWidth / 2 / scale - C.robotWidth / 2 / scale / 3, 
						 y - C.robotLenght / scale / 3, 
						 x - C.robotWidth / 2 / scale, 
						 y,
				robotPaint);
		
		// koło prawe
		canvasX.drawRect(x + C.robotWidth / 2 / scale, 
						 y - C.robotLenght / scale / 3, 
						 x + C.robotWidth / 2 / scale + C.robotWidth / 2 / scale / 3, 
						 y,
				robotPaint);
		
		canvasX.drawCircle(x, y, C.robotWidth / 8 / scale, robotPaint);
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
