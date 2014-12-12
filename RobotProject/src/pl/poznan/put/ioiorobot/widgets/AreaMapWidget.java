package pl.poznan.put.ioiorobot.widgets;

import java.util.Timer;
import java.util.TimerTask;

import pl.poznan.put.ioiorobot.mapobjects.AreaMap;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AreaMapWidget extends View {

	private int width;
	private int height;

	private AreaMap areaMap;
	
	private Paint trackPaint;
	
	//private static Timer timerX;

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
		
		
//		if (null != timerX) {
//			timerX.cancel();
//			timerX.purge();
//			timerX = null;
//		}
//
//		timerX = new Timer();
//
//		timerX.scheduleAtFixedRate(new Update(), 0, 500);
		
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
		// canvas.drawRect(0, 0, width, height, backgroundPaint);
		 canvas.drawBitmap(areaMap.drawMap(), 0, 0, trackPaint);

		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		invalidate();
		return super.onTouchEvent(event);
	}
	
	
//	private class Update extends TimerTask {
//		public void run() {
//			AreaMapWidget.this.invalidate();
//		}
//	}

}
