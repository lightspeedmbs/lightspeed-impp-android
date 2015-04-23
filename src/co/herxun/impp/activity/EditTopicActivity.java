package co.herxun.impp.activity;

import java.util.Set;

import co.herxun.impp.R;
import co.herxun.impp.R.color;
import co.herxun.impp.R.drawable;
import co.herxun.impp.R.id;
import co.herxun.impp.R.layout;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.MaterialEditText;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;

public class EditTopicActivity extends BaseActivity {
	private AppBar mAppBar;
	private MaterialEditText etTopicName;
	private Topic mTopic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_topic);
		
		mAppBar = (AppBar)findViewById(R.id.edit_topic_app_bar);
		mAppBar.getLogoView().setImageResource(R.drawable.menu_back);
		mAppBar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		mAppBar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mAppBar.getTextView().setVisibility(View.VISIBLE);
		mAppBar.getTextView().setText(R.string.chat_topic_edit);
		
		mAppBar.getMenuItemView().setVisibility(View.VISIBLE);
		mAppBar.getMenuItemView().setImageResource(R.drawable.menu_done);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56));
		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mAppBar.getMenuItemView().setLayoutParams(rlp);
		
		etTopicName = (MaterialEditText)findViewById(R.id.edit_topic_materialEditText);
		etTopicName.setLineFocusedColor(getResources().getColor(R.color.no1));
		etTopicName.setLineUnFocusedColor(getResources().getColor(R.color.no1));
		etTopicName.setLineFocusedHeight(4);
		etTopicName.setLineUnFocusedHeight(1);
		etTopicName.getEditText().setTextColor(getResources().getColor(R.color.no1));
		etTopicName.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		etTopicName.getEditText().setSingleLine();
		etTopicName.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
		
		checkBundle();
	}
	
	private void checkBundle(){
		if(getIntent().getExtras()!=null && getIntent().getExtras().containsKey(Constant.INTENT_EXTRA_KEY_TOPIC)){
			mTopic = (Topic) getIntent().getExtras().getSerializable(Constant.INTENT_EXTRA_KEY_TOPIC);
			
			mAppBar.getMenuItemView().setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					IMManager.getInstance(v.getContext()).updateTopicName(etTopicName.getEditText().getText().toString(),mTopic);
					onBackPressed();
				}
			});
		}
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
	
}
