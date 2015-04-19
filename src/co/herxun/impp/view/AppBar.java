package co.herxun.impp.view;

import java.util.ArrayList;

import co.herxun.impp.R;
import co.herxun.impp.utils.Utils;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public class AppBar extends RelativeLayout {
	private ImageView logo, menuItem;
	private EditText etSearch;
	private TextView textTitle;
	private int id = 1;
	public AppBar(Context ct) {
		super(ct);
		init(ct);
	}

	public AppBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		init(context);
	}

	public AppBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context ct){
		logo = new ImageView(ct);
		logo.setId(id++);
		addView(logo);

		etSearch = new EditText(ct);
		etSearch.setSingleLine();
		etSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
		etSearch.setBackgroundColor(Color.TRANSPARENT);
		etSearch.setGravity(Gravity.CENTER_VERTICAL);
		etSearch.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		etSearch.setTextColor(ct.getResources().getColor(R.color.no5));
		etSearch.setHintTextColor(0x4cffffff);
		etSearch.setPadding(0, 0, 0, 0);
		etSearch.setHint(R.string.friend_search_username);
		addView(etSearch);
		
		textTitle = new TextView(ct);
		textTitle.setSingleLine();
		textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		textTitle.setEllipsize(TruncateAt.MIDDLE);
		textTitle.setTextColor(ct.getResources().getColor(R.color.no5));
		addView(textTitle);
		
		menuItem = new ImageView(ct);
		menuItem.setId(id++);
		addView(menuItem);
		
		initLayout();
	}
	
	public void initLayout(){
		RelativeLayout.LayoutParams rlpLogo = new RelativeLayout.LayoutParams(Utils.px2Dp(getContext(), 54), Utils.px2Dp(getContext(), 26));
		rlpLogo.leftMargin = Utils.px2Dp(getContext(), 16);
		rlpLogo.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlpLogo.addRule(RelativeLayout.CENTER_VERTICAL);
		logo.setLayoutParams(rlpLogo);
		
		RelativeLayout.LayoutParams rlpEt = new RelativeLayout.LayoutParams(-1,-2);
		rlpEt.leftMargin = Utils.px2Dp(getContext(), 16);
		rlpEt.addRule(RelativeLayout.RIGHT_OF,logo.getId());
		rlpEt.addRule(RelativeLayout.CENTER_VERTICAL);
		etSearch.setLayoutParams(rlpEt);
		etSearch.setVisibility(View.GONE);
		etSearch.setText("");
		
		RelativeLayout.LayoutParams rlpTt = new RelativeLayout.LayoutParams(-1,-2);
		rlpTt.leftMargin = Utils.px2Dp(getContext(), 16);
		rlpTt.addRule(RelativeLayout.RIGHT_OF,logo.getId());
		rlpTt.addRule(RelativeLayout.LEFT_OF,menuItem.getId());
		rlpTt.addRule(RelativeLayout.CENTER_VERTICAL);
		textTitle.setLayoutParams(rlpTt);
		textTitle.setVisibility(View.GONE);
		textTitle.setText("");
		
		RelativeLayout.LayoutParams rlpMenuItem = new RelativeLayout.LayoutParams(
				Utils.px2Dp(getContext(), 56), Utils.px2Dp(getContext(), 56));
		rlpMenuItem.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		menuItem.setLayoutParams(rlpMenuItem);
		menuItem.setVisibility(View.GONE);
	}
	
	public ImageView getLogoView() {
		return logo;
	}

	public ImageView getMenuItemView() {
		return menuItem;
	}
	public EditText getEditText() {
		return etSearch;
	}
	public TextView getTextView() {
		return textTitle;
	}
}
