package pl.poznan.put.ioiorobot.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 *	Komponent umożliwiający ręczne sterowanie robotem
 */
public class Joystick extends View {

	private int width;
	private int height;
	private int joystickRadius;
	private int maxValue;

	private Paint backgroundPaint;
	private Paint joystickPaint;
	private int touchX, touchY;

	private JoystickMovedListener listener;

	public interface JoystickMovedListener {
		public void onMoved(int xPos, int yPos);

		public void onReleased();
	}

	public Joystick(Context context) {
		super(context);
		initJoystick();
	}

	public Joystick(Context context, AttributeSet attrs) {
		super(context, attrs);
		initJoystick();
	}

	public Joystick(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initJoystick();
	}

	private void initJoystick() {
		setFocusable(true);

		backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		backgroundPaint.setColor(Color.rgb(0xbb, 0xe0, 0xf0));
		backgroundPaint.setStrokeWidth(1);
		backgroundPaint.setStyle(Paint.Style.STROKE);

		joystickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		joystickPaint.setColor(Color.rgb(0x00, 0x77, 0xaa));
		joystickPaint.setStrokeWidth(1);
		joystickPaint.setStyle(Paint.Style.STROKE);

		maxValue = 100;
	}

	public void setJostickMovedListener(JoystickMovedListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int desiredWidth = 1000;
		int desiredHeight = 1000;
		width = measureSize(widthMeasureSpec, desiredWidth);
		height = measureSize(heightMeasureSpec, desiredHeight);
		width = height = Math.min(width, height);
		joystickRadius = (int) (Math.min(width, height) * 0.15);

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
		canvas.drawCircle(width / 2, height / 2, Math.min(width / 2, height / 2), backgroundPaint);
		canvas.drawCircle(width / 2 + touchX, height / 2 + touchY, joystickRadius, joystickPaint);
		canvas.save();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionType = event.getAction();
		if (actionType == MotionEvent.ACTION_MOVE) {

			touchX = (int) event.getX();
			touchX = Math.max(Math.min(touchX, width), 0) - width / 2;

			touchY = (int) event.getY();
			touchY = Math.max(Math.min(touchY, height), 0) - height / 2;

			int size = Math.min(width / 2, height / 2) - joystickRadius;
			if (touchX > size) {
				touchX = size;
			}
			if (touchX < -size) {
				touchX = -size;
			}
			if (touchY > size) {
				touchY = size;
			}
			if (touchY < -size) {
				touchY = -size;
			}

			if (listener != null) {
				int posX = touchX != 0 ? (int) ((double) touchX / size * maxValue) : 0;
				int posY = touchY != 0 ? (int) ((double) touchY / size * maxValue) : 0;
				listener.onMoved(posX, -posY);
			}

			invalidate();
		} else if (actionType == MotionEvent.ACTION_UP) {
			touchX = 0;
			touchY = 0;
			if (listener != null) {
				listener.onReleased();
			}
			invalidate();
		}
		return true;
	}
}
