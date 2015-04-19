package co.herxun.impp.view;

import co.herxun.impp.R;
import co.herxun.impp.utils.Utils;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class BadgeView extends TextView {
	private boolean hideWhenCountZero = true;
	private int count = 0;
	private Context ct ;
	
	public BadgeView(Context context) {
		super(context);
		init(context);
	}
	
	public BadgeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public BadgeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context){
		ct = context;
		setPadding(Utils.px2Dp(context, 4),0,Utils.px2Dp(context, 4),0);
		setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
		setTextSize(TypedValue.COMPLEX_UNIT_DIP , 9);
		setTextColor(context.getResources().getColor(R.color.no2));
		setSingleLine();
		
		setTextColor(0xffffffff);
		setBackgroundColor(0xff000000);
		setBadgeCount(0);
	}
	
	public void setBadgeColor(int color){
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		float corner = Utils.px2Dp(ct, 21);
		gd.setCornerRadii(new float[]{corner,corner,corner,corner,corner,corner,corner,corner});
		setBackgroundDrawable(gd);
	}
	
	public void setHideWhenCountZero(boolean bool){
		hideWhenCountZero = bool;
		setBadgeCount(count);
	}
	
	public void setBadgeCount(int count){
		this.count = count;
		if(hideWhenCountZero && count == 0){
			setVisibility(View.INVISIBLE);
		}else{
			setVisibility(View.VISIBLE);
			setText(count+"");
		}
	}
}
