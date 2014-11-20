package pl.poznan.put.ioiorobot.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SimpleBarGraph extends View {

	private int width;
	private int height;

	private Paint backgroundPaint;
	private Paint barPaint;

//	int graphWidth;
	int maxValue = 150;

	private List<Integer> values = new ArrayList<Integer>();

	public SimpleBarGraph(Context context) {
		super(context);
		initSimpleBarGraph();
	}

	public SimpleBarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSimpleBarGraph();
	}

	public SimpleBarGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSimpleBarGraph();
	}

	private void initSimpleBarGraph() {
		setFocusable(true);

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setColor(Color.rgb(0xbb, 0xe0, 0xf0));
		backgroundPaint.setStrokeWidth(1);
		backgroundPaint.setStyle(Paint.Style.STROKE);

		barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		barPaint.setColor(Color.rgb(0x00, 0x77, 0xaa));
		barPaint.setStrokeWidth(1);
		barPaint.setStyle(Paint.Style.FILL_AND_STROKE);

	}

	public void setValues(List<Integer> values) {
		this.values = values;
		this.invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredWidth = 3000;
		int desiredHeight = 3000;
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
		//Log.e("robot","onDraw");
		List<Integer> valuesCopy = values;
		int barsNumber = valuesCopy.size();

		canvas.drawRect(0, 0, width, height, backgroundPaint);

		for (int i = 0; i < barsNumber; i++) {
			int value = valuesCopy.get(i);
			int scaledValue = value > maxValue ? maxValue : value;

			int left = width * i / barsNumber + 5;
			int top = height - (int) (height * ((float) scaledValue / maxValue));
			int right = width * (i + 1) / barsNumber;
			int bottom = height;
			canvas.drawRect(left, top, right, bottom, barPaint);
		}

		canvas.save();
	}

}
