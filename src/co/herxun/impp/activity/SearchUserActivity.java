package co.herxun.impp.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;

import co.herxun.impp.R;
import co.herxun.impp.R.layout;
import co.herxun.impp.adapter.UserListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.FetchFriendCallback;
import co.herxun.impp.controller.UserManager.FetchUserCallback;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.model.Friend;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

public class SearchUserActivity extends Activity {
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
		mAppbar.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
		mAppbar.getEditText().setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
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
        mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

				Map<String,String> c_data = new HashMap<String,String>();
				try {
					String s = IMManager.getInstance(SearchUserActivity.this).getAnIM().sendBinary(mUserListAdapter.getItem(position).clientId, new byte[1], "test",c_data);
				} catch (ArrownockException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        });
	}
	
	public void fillRemoteData(String username){
		UserManager.getInstance(this).searchRemoteUser(username, new FetchUserCallback(){
			@Override
			public void onFinish(final List<User> users) {
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						mUserListAdapter.applyData(users);
					}
				});
			}
		});
	}
	
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
	
}
