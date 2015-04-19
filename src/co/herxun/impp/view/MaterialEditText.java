package co.herxun.impp.view;

import org.xmlpull.v1.XmlPullParser;

import co.herxun.impp.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MaterialEditText extends FrameLayout {
	private EditText editText;
	private View line;
	private int focusedLineColor = 0xff000000;
	private int unFocusedLineColor = 0xff000000;
	private int focusedLineHeight = 1;
	private int unFocusedLineHeight  = 4;
	
	public MaterialEditText(Context ct) {
		super(ct);
		init(ct);
	}


	public MaterialEditText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public MaterialEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context ct){
		removeAllViews();
		if(getEdittextAttr(ct)!=null){
			editText = new EditText(ct,getEdittextAttr(ct)); 
		}else{
			editText = new EditText(ct); 
		}

		int padding = px2Dp(8);
		editText.setBackgroundColor(Color.TRANSPARENT);
		editText.setPadding(0, padding, 0, padding);
		editText.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams rlpEt = new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
		addView(editText,rlpEt);
		
		RelativeLayout lineWrapper = new RelativeLayout(ct);
		lineWrapper.setBackgroundColor(ct.getResources().getColor(R.color.no1));
		FrameLayout.LayoutParams rlpLineWrapper = new FrameLayout.LayoutParams(-1,focusedLineHeight,Gravity.BOTTOM);
		addView(lineWrapper,rlpLineWrapper);
		
		line = new View(ct);
		line.setBackgroundColor(unFocusedLineColor);
		RelativeLayout.LayoutParams rlpLine = new RelativeLayout.LayoutParams(-1,focusedLineHeight);
		rlpLine.addRule(RelativeLayout.CENTER_VERTICAL);
		lineWrapper.addView(line,rlpLine);
		
		editText.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) line.getLayoutParams();
				if(hasFocus){
					rlp.height = focusedLineHeight;
					line.setLayoutParams(rlp);
					line.setBackgroundColor(focusedLineColor);
				}else{
					rlp.height = unFocusedLineHeight;
					line.setLayoutParams(rlp);
					line.setBackgroundColor(unFocusedLineColor);
				}
			}
		});
	}
	
	public void setLineFocusedHeight(int height){
		focusedLineHeight = height;
		init(getContext());
	}
	public void setLineUnFocusedHeight(int height){
		unFocusedLineHeight = height;
		init(getContext());
	}
	
	
	public void setLineFocusedColor(int color){
		focusedLineColor = color;
	}
	public void setLineUnFocusedColor(int color){
		unFocusedLineColor = color;
	}

	public EditText getEditText(){
		return editText;
	}
	
	public int px2Dp(int px){
		return (int) (px * getContext().getResources().getDisplayMetrics().density);
	}
	
	public AttributeSet getEdittextAttr(Context ct){
		try{
			int res = ct.getResources().getIdentifier("my_edittext", "layout", ct.getPackageName());
			XmlPullParser parser = ct.getResources().getXml(res);
			int state=0;
			do {
			    try {
			        state = parser.next();
			    } catch (Exception e1) {
			        e1.printStackTrace();
			    }       
			    if (state == XmlPullParser.START_TAG) {
			        if (parser.getName().equals("EditText")) {
			            return  Xml.asAttributeSet(parser);
			        }
			    }
			} while(state != XmlPullParser.END_DOCUMENT);
	        return null;
		}catch(Exception e){
			e.printStackTrace();
	        return null;
		}
		
	}
}
