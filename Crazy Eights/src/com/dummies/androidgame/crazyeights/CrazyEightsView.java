package com.dummies.androidgame.crazyeights;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class CrazyEightsView extends View {

	private Paint redPaint;
	private int circleX;
	private int circleY;
	private float radius;
	
	public CrazyEightsView(Context context) {
		super(context);
		redPaint = new Paint();
		redPaint.setAntiAlias(true);
		redPaint.setColor(Color.RED);
		circleX = 100;
		circleY = 100;
		radius = 30;
	}
	
	 @Override 
	 protected void onDraw(Canvas canvas) {
		 canvas.drawCircle(circleX, circleY, radius, redPaint);
	 }
	 
	 public boolean onTouchEvent(MotionEvent event) {
	        int eventaction = event.getAction();   
	        int X = (int)event.getX();
	        int Y = (int)event.getY();

	        switch (eventaction ) {

	        case MotionEvent.ACTION_DOWN: 
	        	break;

	        case MotionEvent.ACTION_MOVE: 
	        	break;
	        	
	        case MotionEvent.ACTION_UP:
	        	circleX = X;
	        	circleY = Y;
	        	break;
	        } 
	        invalidate();
			return true;
	        
	}
	 
}