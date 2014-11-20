package pl.poznan.put.ioiorobot.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BatteryStatusBar extends View {

	private int width;
	private int height;

	private Paint borderPaint;
	private Paint stripsPaint;
	private Paint textPaint;

	private int value = 0;
	

	public BatteryStatusBar(Context context) {
		super(context);
		init();
	}

	public BatteryStatusBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BatteryStatusBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setFocusable(true);

		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setColor(Color.rgb(0xbb, 0xe0, 0xf0));
		borderPaint.setStrokeWidth(6);
		borderPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		stripsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		stripsPaint.setColor(Color.rgb(0x33, 0x99, 0xee));
		stripsPaint.setStrokeWidth(1);
		stripsPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.rgb(0x00, 0x00, 0x00));
		textPaint.setTextSize(60);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		textPaint.setTextAlign(Align.CENTER);
	}

	public void setValue(int value) {
		this.value = Math.min(Math.max(value, 0), 100);
		this.invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredWidth = 200;
		int desiredHeight = 100;
		width = measureSize(widthMeasureSpec, desiredWidth);
		height = measureSize(heightMeasureSpec, desiredHeight);

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
		int stripsNum = 10;
		int spacing = 2;
		int borderLeft = 5;
		int borderRight = 20;
		
		canvas.drawRect(0, 0, width-borderRight, height, borderPaint);
		canvas.drawRect(width-borderRight, height*1/3, width, height*2/3, borderPaint);

		int stripsToDraw = (int) Math.ceil( value * stripsNum / 100.0 );
		int stripsWidht = (width - borderLeft - borderRight) / stripsNum;
		
		for (int i = 0; i < stripsToDraw; i++) {
			int left = borderLeft + i*stripsWidht ;
			int top = 0 + 5;
			int right = left + stripsWidht - spacing;
			int bottom = height - 5;
			canvas.drawRect(left, top, right, bottom, stripsPaint);
		}
		
		canvas.drawText(value + " % ", width/2, height*5/7, textPaint);

		canvas.save();
	}

}
