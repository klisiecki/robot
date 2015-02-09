package pl.poznan.put.ioiorobot.widgets;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.ioiorobot.mapping.Pattern;
import pl.poznan.put.ioiorobot.utils.Config;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PatternsWidget extends View {

	private int width;
	private int height;

	private Paint choosenBorderPaint;
	private Paint borderPaint;
	private Paint patternPaint;

	private List<Pattern> patterns;

	public void addPattern(Pattern pattern) {
		patterns.add(pattern);
		invalidate();
	}

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
		borderPaint.setColor(Color.rgb(0x00, 0xff, 0x00));
		borderPaint.setStrokeWidth(5);
		borderPaint.setStyle(Paint.Style.STROKE);

		choosenBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		choosenBorderPaint.setColor(Color.rgb(0xff, 0x00, 0x00));
		choosenBorderPaint.setStrokeWidth(8);
		choosenBorderPaint.setStyle(Paint.Style.STROKE);

		patternPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		patternPaint.setColor(Color.rgb(0xbb, 0xe0, 0xf0));
		patternPaint.setStrokeWidth(1);
		patternPaint.setStyle(Paint.Style.STROKE);

		patterns = new ArrayList<Pattern>();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		width = Config.screenSize.x;
		height = Config.patternSize;
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int i = 0;
		for (Pattern p : patterns) {
			canvas.drawBitmap(p.getBitmap(), (i++) * Config.patternSize, 0, patternPaint);
		}
		canvas.drawRect(0, 0, Config.patternSize * (patterns.size()), height, borderPaint);
		canvas.save();
	}

}
