package pl.poznan.put.ioiorobot.widgets;

import pl.poznan.put.ioiorobot.motors.Position;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



public class Map extends View {

	private int width;
	private int height;
	
	private int bmpSize = 500;
	private int scale = 3;
	
    private Paint backgroundPaint;
    private Paint trackPaint;
    private Paint headPaint;
    
	private Bitmap bmp = Bitmap.createBitmap(bmpSize, bmpSize, Bitmap.Config.ARGB_8888);
	private Canvas canvasX = new Canvas(bmp);

	private Position position = new Position();
	

    public Map(Context context) {
        super (context);
        init();
        
    }

    public Map(Context context, AttributeSet attrs) {
        super (context, attrs);
        init();
    }

    public Map(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        init();
    }
    
    private void init() {
        setFocusable(true);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.rgb(0x88, 0xee, 0xff));
        backgroundPaint.setStrokeWidth(1);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setColor(Color.rgb(0x00, 0x99, 0x00));
        trackPaint.setStrokeWidth(1);
        trackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        
        headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headPaint.setColor(Color.rgb(0x99, 0x00, 0x00));
        headPaint.setStrokeWidth(1);
        headPaint.setStyle(Paint.Style.FILL_AND_STROKE);
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
    
    
    public void addPosition(double xParam, double yParam, double angleParam){
    	yParam = -yParam + 200;
    	xParam = -xParam;
    	
    	position.set(xParam, yParam, angleParam);
    	
    	int x = (int) (xParam/scale + bmpSize/2);
    	int y = (int) (yParam/scale + bmpSize/2);
    	
    	canvasX.drawCircle(x, y, 2, trackPaint);
    	invalidate();
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, width, height, backgroundPaint);
        canvas.drawBitmap(bmp, 0, 0, trackPaint);

    	int x = (int) (position.x()/scale + bmpSize/2);
    	int y = (int) (position.y()/scale + bmpSize/2);
    	int angle = (int) (position.angle());
    	canvas.drawCircle(x, y, 5, headPaint);

        canvas.save();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	canvasX.drawPaint(backgroundPaint);
    	return super.onTouchEvent(event);
    }

}
