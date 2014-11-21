package pl.poznan.put.ioiorobot.widgets;

import pl.poznan.put.ioiorobot.utils.MyConfig;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PatternsWidget extends View {
	
	private int width;
	private int height;
	
	private Paint borderPaint;
	

	public PatternsWidget(Context context) {
		super(context);
		init();
	}

	public PatternsWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PatternsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setColor(Color.rgb(0xbb, 0xe0, 0xf0));
		borderPaint.setStrokeWidth(5);
		borderPaint.setStyle(Paint.Style.STROKE);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		width = MyConfig.patternSize;
		height = MyConfig.patternSize;
		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(0, 0, width, height, borderPaint);
		canvas.save();
	}
	

}
