package co.herxun.impp.activity;

import java.util.List;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import co.herxun.impp.R;
import co.herxun.impp.adapter.UserListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.FetchUserCallback;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

public class SearchUserActivity extends BaseActivity {
	private AppBar mAppbar;
	private ListView mListView;
	private UserListAdapter mUserListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_user);
		
		initView();
	}
	
	private void initView(){
		mAppbar = (AppBar)findViewById(R.id.toolbar);
		mAppbar.getLogoView().setImageResource(R.drawable.menu_back);
		mAppbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		mAppbar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
//		mAppbar.getMenuItemView().setImageResource(R.drawable.ic_launcher);
//		mAppbar.getMenuItemView().setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				mAppbar.getEditText().setText("");
//			}
//		});
		mAppbar.getEditText().setVisibility(View.VISIBLE);
		mAppbar.getEditText().setSingleLine();
		mAppbar.getEditText().setHint(R.string.friend_request_search);
		mAppbar.getEditText().setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mAppbar.getEditText().setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					 fillRemoteData(mAppbar.getEditText().getText().toString());
				}
				return false;
			}
		});
		
		mListView = (ListView)findViewById(R.id.friend_search_listView);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);
		mUserListAdapter = new UserListAdapter(this);
		mListView.setAdapter(mUserListAdapter);
	}
	
	public void fillRemoteData(String username){
		showLoading();
		UserManager.getInstance(this).searchRemoteUser(username, new FetchUserCallback(){
			@Override
			public void onFinish(final List<User> users) {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mUserListAdapter.applyData(users);
					}
				});
				dismissLoading();
			}
		});
	}
	
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
	
}
