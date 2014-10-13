package pl.poznan.put.ioiorobot.temp;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SimpleBarGraph extends View {

	private Paint backgroundPaint;
	private Paint barPaint;
	
	int graphWidth;
	int graphHeight = 250;
	int maxValue = 150;
	
	private List<Integer> values = new ArrayList<Integer>();


    public SimpleBarGraph(Context context) {
        super (context);
        initSimpleBarGraph();
    }

    public SimpleBarGraph(Context context, AttributeSet attrs) {
        super (context, attrs);
        initSimpleBarGraph();
    }

    public SimpleBarGraph(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        initSimpleBarGraph();
    }

    private void initSimpleBarGraph() {
        setFocusable(true);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.rgb(0xbb, 0xe0, 0xf0));
        backgroundPaint.setStrokeWidth(1);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        
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
	    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
	    //int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	    
	    graphWidth = parentWidth;
	    
	    this.setMeasuredDimension(graphWidth, graphHeight);
	}

    @Override
    protected void onDraw(Canvas canvas) {
    	//####
//    	values.add(10);
//    	values.add(50);
//    	values.add(5);
//    	values.add(30);
    	//####
    	
    	List<Integer> valuesCopy = values;
    	int barsNumber = valuesCopy.size();
    	
    	canvas.drawRect(0, 0, graphWidth, graphHeight, backgroundPaint);
    	
    	for(int i=0; i<barsNumber; i++) {
    		int value = valuesCopy.get(i);
    		int scaledValue = value > maxValue ? maxValue : value;
    		
    		int left = graphWidth*i/barsNumber+5;
    		int top = graphHeight - (int) (graphHeight * ((float)scaledValue / maxValue));
    		int right = graphWidth*(i+1)/barsNumber;
    		int bottom = graphHeight;
    		canvas.drawRect(left, top, right, bottom, barPaint);
    	}

        canvas.save();
    }

}
