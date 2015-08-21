package co.herxun.impp.activity;

import java.util.Observable;
import java.util.Observer;

import co.herxun.impp.R;
import co.herxun.impp.adapter.FriendRequestListAdapter;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriendRequestActivity extends BaseActivity implements Observer{
	private AppBar mAppbar;
	private ListView mListView;
	private FriendRequestListAdapter mFriendRequestListAdapter;
	private TextView noRequestLabel;
	TextView textTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_request);
		
		initView();
		IMManager.getInstance(this).addObserver(this);
	}
	
	private void initView(){
		noRequestLabel = (TextView) findViewById(R.id.noRequestLabel);
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
        
		textTitle = new TextView(this);
		textTitle.setPadding(Utils.px2Dp(this, 16), Utils.px2Dp(this,24), 0, Utils.px2Dp(this, 8));
		textTitle.setText(R.string.friend_friend_request);
		mListView.addHeaderView(textTitle);
		mFriendRequestListAdapter = new FriendRequestListAdapter(this);
		mListView.setAdapter(mFriendRequestListAdapter);
		
		mFriendRequestListAdapter.fetchRemoteData(true);
		
		mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        	@Override
        	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        		if(mFriendRequestListAdapter.getCount() > 0) {
        			noRequestLabel.setVisibility(View.GONE);
        			textTitle.setVisibility(View.VISIBLE);
        		} else {
        			noRequestLabel.setVisibility(View.VISIBLE);
        			textTitle.setVisibility(View.GONE);
        		}
        	}
        });
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
