package com.dummies.androidgame.crazyeights;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class TitleView extends View {

	private Bitmap titleGraphic;
	private Bitmap playButtonUp;
	private Bitmap playButtonDown;
	private int screenW;
	private int screenH;
	private boolean playButtonPressed;
	private Context myContext;
	
	public TitleView(Context context) {
		super(context);
		this.myContext = context;

		this.titleGraphic = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.title_graphic);
		this.playButtonUp = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.play_button_up);
		this.playButtonDown = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.play_button_down);
	}

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        this.screenW = w;
        this.screenH = h;
        System.out.println("SCREEN W: " + this.screenW);
        System.out.println("SCREEN H: " + this.screenH);
    }
	
	@Override 
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(this.titleGraphic,
				(this.screenW - this.titleGraphic.getWidth()) / 2, 0, null);

		if (this.playButtonPressed) {
			canvas.drawBitmap(this.playButtonDown,
					(this.screenW - this.playButtonUp.getWidth()) / 2,
					(int) (this.screenH * 0.7), null);
		} else {
			canvas.drawBitmap(this.playButtonUp,
					(this.screenW - this.playButtonUp.getWidth()) / 2,
					(int) (this.screenH * 0.7), null);
		}
	}
	 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();
		int X = (int) event.getX();
		int Y = (int) event.getY();

		switch (eventaction) {

		case MotionEvent.ACTION_DOWN:
			if (X > (this.screenW - this.playButtonUp.getWidth()) / 2
					&& X < ((this.screenW - this.playButtonUp.getWidth()) / 2) + this.playButtonUp.getWidth()
					&& Y > (int) (this.screenH * 0.7)
					&& Y < (int) (this.screenH * 0.7) + this.playButtonUp.getHeight()) {
				this.playButtonPressed = true;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			break;

		case MotionEvent.ACTION_UP:
			if (this.playButtonPressed) {
				Intent gameIntent = new Intent(this.myContext, GameActivity.class); // Intent request from an app to the Android system to perform an operation.	
				this.myContext.startActivity(gameIntent); // Lanza el Intent
			}
			this.playButtonPressed = false;
			break;
		}
		
		invalidate();
		
		return true;
	}
}
