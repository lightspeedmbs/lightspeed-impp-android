package co.herxun.impp.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.controller.SocialManager;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.SpfHelper;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.MaterialEditText;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.im.callback.AnIMAddClientsCallbackData;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMCallbackAdapter;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;
import com.arrownock.im.callback.AnIMGetClientIdCallbackData;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

public class LoginActivity extends ActionBarActivity{
	private MaterialEditText etUsername,etPwd;
	private Button btnSignUp,btnSignIn;
	private String payload;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkBundle();
		autoSignIn();
	}

	private void autoSignIn(){
		if(SpfHelper.getInstance(this).hasSignIn()){
			UserManager.getInstance(this).login(SpfHelper.getInstance(this).getMyUsername(),SpfHelper.getInstance(this).getMyPwd(),new IAnSocialCallback(){
				@Override
				public void onFailure(JSONObject arg0) {
					try {
						String errorMsg = arg0.getJSONObject("meta").getString("message");
						Toast.makeText(getBaseContext(), errorMsg,Toast.LENGTH_LONG).show();
						initView();
					} catch (JSONException e) {
						e.printStackTrace();
						initView();
					}
				}
				@Override
				public void onSuccess(final JSONObject arg0) {
					try {
						JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
	                	User user = new User(userJson);
	                	afterLogin(user);
					} catch (JSONException e) {
						e.printStackTrace();
						initView();
					}
				}
			});
		}else{
			initView();
		}
	}
	
	private void checkBundle(){
		if(getIntent().hasExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD)){
			payload = getIntent().getStringExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD);
		}
	}
	
	private void initView(){
		setContentView(R.layout.activity_login);
		etUsername = (MaterialEditText)findViewById(R.id.et_username);
		etUsername.setLineFocusedColor(getResources().getColor(R.color.no5));
		etUsername.setLineUnFocusedColor(getResources().getColor(R.color.no5));
		etUsername.setLineFocusedHeight(4);
		etUsername.setLineUnFocusedHeight(1);
		etUsername.getEditText().setTextColor(getResources().getColor(R.color.no5));
		etUsername.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		etUsername.getEditText().requestFocus();
		etUsername.getEditText().setHint(R.string.login_username);
		etUsername.getEditText().setHintTextColor(getResources().getColor(R.color.no7));
		etUsername.getEditText().setSingleLine();
		
		etPwd = (MaterialEditText)findViewById(R.id.et_pwd);
		etPwd.setLineFocusedColor(getResources().getColor(R.color.no5));
		etPwd.setLineUnFocusedColor(getResources().getColor(R.color.no5));
		etPwd.setLineFocusedHeight(4);
		etPwd.setLineUnFocusedHeight(1);
		etPwd.getEditText().setTextColor(getResources().getColor(R.color.no5));
		etPwd.getEditText().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		etPwd.getEditText().setHint(R.string.login_pwd);
		etPwd.getEditText().setHintTextColor(getResources().getColor(R.color.no7));
		etPwd.getEditText().setSingleLine();
		etPwd.getEditText().setTransformationMethod(PasswordTransformationMethod.getInstance());
		etPwd.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);

		float corner = Utils.px2Dp(this, 2);
		GradientDrawable bgBtn = new GradientDrawable();
		bgBtn.setColor(getResources().getColor(R.color.no3));
		bgBtn.setCornerRadii(new float[]{corner,corner,corner,corner,corner,corner,corner,corner});
		GradientDrawable bgBtnSelected = new GradientDrawable();
		bgBtnSelected.setColor(getResources().getColor(R.color.no2));
		bgBtnSelected.setCornerRadii(new float[]{corner,corner,corner,corner,corner,corner,corner,corner});
		
		StateListDrawable statesBtnSignUp = new StateListDrawable();
		statesBtnSignUp.addState(new int[] {android.R.attr.state_pressed},bgBtnSelected);
		statesBtnSignUp.addState(new int[] {android.R.attr.state_focused},bgBtnSelected);
		statesBtnSignUp.addState(new int[] { },bgBtn);
		
		StateListDrawable statesBtnSignIn = new StateListDrawable();
		statesBtnSignIn.addState(new int[] {android.R.attr.state_pressed},bgBtnSelected);
		statesBtnSignIn.addState(new int[] {android.R.attr.state_focused},bgBtnSelected);
		statesBtnSignIn.addState(new int[] { },bgBtn);
		
		btnSignUp = (Button)findViewById(R.id.btn_sign_up);
		btnSignUp.setBackgroundDrawable(statesBtnSignUp);
		btnSignUp.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				UserManager.getInstance(LoginActivity.this).signUp(etUsername.getEditText().getText().toString(),etPwd.getEditText().getText().toString(),new IAnSocialCallback(){
					@Override
					public void onFailure(JSONObject arg0) {
						try {
							String errorMsg = arg0.getJSONObject("meta").getString("message");
							Toast.makeText(getBaseContext(), errorMsg,Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					@Override
					public void onSuccess(final JSONObject arg0) {
						try {
							JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
		                	User user = new User(userJson);
		                	afterLogin(user);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
		
		btnSignIn = (Button)findViewById(R.id.btn_sign_in);
		btnSignIn.setBackgroundDrawable(statesBtnSignIn);
		btnSignIn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				UserManager.getInstance(LoginActivity.this).login(etUsername.getEditText().getText().toString(),etPwd.getEditText().getText().toString(),new IAnSocialCallback(){
					@Override
					public void onFailure(JSONObject arg0) {
						try {
							String errorMsg = arg0.getJSONObject("meta").getString("message");
							Toast.makeText(getBaseContext(), errorMsg,Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					@Override
					public void onSuccess(final JSONObject arg0) {
						try {
							JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
		                	User user = new User(userJson);
		                	afterLogin(user);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
		
	}
	
	
	private void afterLogin(User user){
		if(!SpfHelper.getInstance(this).hasSignIn()){
	    	SpfHelper.getInstance(this).saveUserInfo(etUsername.getEditText().getText().toString(), etPwd.getEditText().getText().toString());
		}
    	IMManager.getInstance(this).connect(user.clientId);
    	UserManager.getInstance(this).setCurrentUser(user);
    	
		IMManager.getInstance(this).fetchAllRemoteTopic();
    	UserManager.getInstance(this).fetchMyRemoteFriend(null);
    	UserManager.getInstance(this).fetchFriendRequest(null);

    	Intent i = new Intent(this,MainActivity.class);
    	if(payload!=null){
        	i.putExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD, payload);
    	}
    	startActivity(i);
    	finish();
	}
}