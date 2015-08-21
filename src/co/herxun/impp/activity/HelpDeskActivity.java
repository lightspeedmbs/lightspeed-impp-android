package co.herxun.impp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import co.herxun.impp.R;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class HelpDeskActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_desk);
		initView();
	}
	
	private void initView(){
		AppBar appbar = (AppBar) findViewById(R.id.helpdesk_app_bar);
		appbar.getLogoView().setImageResource(R.drawable.menu_back);
		appbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		appbar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		appbar.getTextView().setVisibility(View.VISIBLE);
		appbar.getTextView().setText(R.string.cs_help_desk);
		
		findViewById(R.id.helpdesk_btn_customservice).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent(HelpDeskActivity.this,CustomServiceActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
			}
		});
	}
	

	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
}
