package com.dummies.androidgame.crazyeights;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class CrazyEightsView extends View {

	private Paint colorPaint;
	private int circleX;
	private int circleY;
	private int color;
	private float radius;
	private float radiusStill;
	

	public CrazyEightsView(Context context) {
		super(context);
		colorPaint = new Paint();
		colorPaint.setAntiAlias(true);
		color = Color.rgb((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
		colorPaint.setColor(color);
		circleX = 100;
		circleY = 100;
		radius = 60;
		radiusStill = radius;
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(circleX, circleY, radius, colorPaint);
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		int rojo = Color.red(this.color);
		int verde = Color.green(this.color);
		int azul = Color.blue(this.color);
		int canal = (int) (Math.random() * 3);
		int variacion = (int) (Math.random() * 40) - 20;
			
		switch(canal) {
		case 0:
			// Canal rojo
			rojo = Math.max(0, Math.min(255, rojo + variacion));
			break;
		case 1:
			// Canal verde
			verde = Math.max(0, Math.min(255, verde + variacion));
			break;
		case 2:
			// Canal azul
			azul = Math.max(0, Math.min(255, azul + variacion));
			break;			
		}
		color = Color.rgb(rojo, verde, azul);
		colorPaint.setColor(color);
		
		int eventaction = event.getAction();
		int X = (int) event.getX();
		int Y = (int) event.getY();
		
		
		
		switch (eventaction) {
		case MotionEvent.ACTION_DOWN:
			circleX = X;
			circleY = Y;
			break;
			
		case MotionEvent.ACTION_MOVE:
			
			float distancia = (float)Math.sqrt((circleX - X) * (circleX - X) + (circleY - Y) * (circleY - Y));
			this.radius = this.radiusStill * (float)Math.exp(-distancia / 100f);
			
			circleX = X;
			circleY = Y;
			break;
			
		case MotionEvent.ACTION_UP:
			circleX = X;
			circleY = Y;
			this.radius = this.radiusStill;
			break;
		}
		invalidate();
		return true;
	}
}
