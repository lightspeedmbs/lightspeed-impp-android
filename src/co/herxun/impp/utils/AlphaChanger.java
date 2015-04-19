package co.herxun.impp.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class AlphaChanger implements OnTouchListener{
	float alpha = 0.5f ;
	public AlphaChanger(float alpha){
		this.alpha = alpha;
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			v.setAlpha(alpha);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			v.setAlpha(1);
			break;
		}
		return false;
	}
}
