package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.arrownock.exception.ArrownockException;
import com.arrownock.social.IAnSocialCallback;

import co.herxun.impp.R;
import co.herxun.impp.R.layout;
import co.herxun.impp.adapter.FriendRequestListAdapter;
import co.herxun.impp.adapter.UserListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.model.FriendRequest;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;

public class FriendRequestActivity extends BaseActivity implements Observer{
	private AppBar mAppbar;
	private ListView mListView;
	private FriendRequestListAdapter mFriendRequestListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_request);
		
		initView();
		IMManager.getInstance(this).addObserver(this);
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
		
		mListView = (ListView)findViewById(R.id.friend_request_listView);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);
        
		TextView textTitle = new TextView(this);
		textTitle.setPadding(Utils.px2Dp(this, 16), Utils.px2Dp(this,24), 0, Utils.px2Dp(this, 8));
		textTitle.setText(R.string.friend_friend_request);
		mListView.addHeaderView(textTitle);
		mFriendRequestListAdapter = new FriendRequestListAdapter(this);
		mListView.setAdapter(mFriendRequestListAdapter);
		
		mFriendRequestListAdapter.fetchRemoteData(true);
		
	}

	@Override
	public void update(Observable observable, final Object data) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(data instanceof IMManager.UpdateType && ((IMManager.UpdateType)data).equals(IMManager.UpdateType.FriendRequest)){
					mFriendRequestListAdapter.fetchRemoteData(true);
				}
			}
		});
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
}
