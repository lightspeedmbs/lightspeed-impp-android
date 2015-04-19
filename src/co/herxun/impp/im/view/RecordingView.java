package co.herxun.impp.im.view;

import java.util.ArrayList;
import java.util.List;

import co.herxun.impp.R;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class RecordingView extends RelativeLayout {
	private ImageView imgRecordBack,imgRecording;
	private TextView textRecording;
	private ValueAnimator animEengine;
	
	public RecordingView(Context ct) {
		super(ct);
		
		this.setBackgroundColor(0xCC000000);
		
		LinearLayout ll = new LinearLayout(ct);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER_HORIZONTAL);
		RelativeLayout.LayoutParams rlpll = new RelativeLayout.LayoutParams(-2,-2);
		rlpll.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(ll,rlpll);
		
		FrameLayout frameClock = new FrameLayout(ct);
		ll.addView(frameClock,new LinearLayout.LayoutParams(-2,-2));
		
		imgRecordBack = new ImageView(ct);
		imgRecordBack.setImageResource(R.drawable.voice_record_bg);
		FrameLayout.LayoutParams rlpimgRecordBack = new FrameLayout.LayoutParams(Utils.px2Dp(ct, 55),Utils.px2Dp(ct, 55));
		frameClock.addView(imgRecordBack,rlpimgRecordBack);
		
		imgRecording = new ImageView(ct);
		imgRecording.setImageResource(R.drawable.voice_record);
		FrameLayout.LayoutParams rlpImg = new FrameLayout.LayoutParams(Utils.px2Dp(ct, 55),Utils.px2Dp(ct, 55));
		frameClock.addView(imgRecording,rlpImg);
		
		textRecording = new TextView(ct);
		textRecording.setTextColor(ct.getResources().getColor(R.color.no5));
		LinearLayout.LayoutParams rlpTxt = new LinearLayout.LayoutParams(-2,-2);
		rlpTxt.topMargin = Utils.px2Dp(ct, 16);
		ll.addView(textRecording,rlpTxt);
		
		animEengine = ValueAnimator.ofFloat(0,360);
		animEengine.setDuration(1600);
		animEengine.setInterpolator(new LinearInterpolator());
		animEengine.setRepeatMode(ValueAnimator.RESTART);
		animEengine.setRepeatCount(ValueAnimator.INFINITE);
		animEengine.addUpdateListener(new AnimatorUpdateListener(){
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				imgRecording.setRotation((Float) animation.getAnimatedValue());
			}
		});
	}

	public void setRecording(){
		imgRecording.setVisibility(View.VISIBLE);
		imgRecordBack.setImageResource(R.drawable.voice_record_bg);
		if(!animEengine.isRunning()){
			animEengine.start();
		}
	
		textRecording.setText(R.string.chat_recording);
	}
	
	public void setRecordFailed(){
		animEengine.end();
		imgRecording.setVisibility(View.GONE);
		imgRecordBack.setImageResource(R.drawable.voice_alert);
    	
		textRecording.setText(R.string.chat_record_tooshort);
	}
	
	public void setReleaseToCancel(){
		animEengine.end();
		imgRecording.setVisibility(View.GONE);
		imgRecordBack.setImageResource(R.drawable.voice_alert);
    	
		textRecording.setText(R.string.chat_record_release_to_cancel);
	}
	
	public void show(){
		setVisibility(View.VISIBLE);
	}
	public void hide(){
		animEengine.end();
		setVisibility(View.GONE);
	}
}
