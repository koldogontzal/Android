package com.dummies.androidgame.crazyeights;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class CrazyEightsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TitleView tView = new TitleView(this);
		
		tView.setKeepScreenOn(true); // Evita que la pantalla se oscurezca debido a la inactividad del usuario
		
		super.requestWindowFeature(Window.FEATURE_NO_TITLE); // No muestra el titulo de la actividad
		
		super.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); // Muestra la actividad en pantalla completa

		super.setContentView(tView);
    }
}
