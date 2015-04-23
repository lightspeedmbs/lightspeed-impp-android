package co.herxun.impp.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arrownock.exception.ArrownockException;

import co.herxun.impp.R;
import co.herxun.impp.R.layout;
import co.herxun.impp.adapter.PostListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.WallManager;
import co.herxun.impp.controller.UserManager.FetchUserCallback;
import co.herxun.impp.controller.WallManager.LikeCallback;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Post;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.WallView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class WallActivity extends BaseActivity {
	private AppBar appbar;
	private WallView mWallView;
	private FrameLayout header;
	private ImageView addBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wall);
		
		initView();
		initData();
	}
	
	private void initView(){
		appbar = (AppBar) findViewById(R.id.wall_app_bar);
		appbar.getLogoView().setImageResource(R.drawable.menu_back);
		appbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		appbar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		appbar.getTextView().setVisibility(View.VISIBLE);
		appbar.getTextView().setText(R.string.wall_circle);
		
		mWallView = (WallView)findViewById(R.id.wall_wallView);
		header = new FrameLayout(this);
		LayoutInflater.from(this).inflate(R.layout.view_wall_header,header);
		header.setLayoutParams(new AbsListView.LayoutParams(-1,Utils.px2Dp(this, 112)));
		mWallView.setHeaderView(header);
		
		addBtn = (ImageView)findViewById(R.id.wall_addBtn);
		addBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(),CreatePostActivity.class);
				startActivityForResult(intent, 0);
				overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
			}
		});
	}
	
	private void initData(){
		TextView textUserName = (TextView) header.findViewById(R.id.view_wall_header_text);
		ImageView imgUserIcon = (ImageView) header.findViewById(R.id.view_wall_header_icon);
		User me = UserManager.getInstance(this).getCurrentUser();
		textUserName.setText(me.userName);
		ImageLoader.getInstance(this).DisplayImage(me.userPhotoUrl, imgUserIcon, R.drawable.friend_default, true);
		
		UserManager.getInstance(this).getMyLocalFriends(new FetchUserCallback(){
			@Override
			public void onFinish(List<User> data) {
				Set<String> friendSet = new HashSet<String>();
				for(User friend : data){
					friendSet.add(friend.userId);
				}
				String wallId = getString(R.string.wall_id);
				
				WallManager mWallManager = new WallManager(WallActivity.this,wallId,friendSet);
				mWallManager.setOnLikeListener(new LikeCallback(){
					public void onFailure(Post post) {}
					@Override
					public void onSuccess(Post post) {
						try {
							Map<String,String> cData = new HashMap<String,String>();
							cData.put(Constant.FRIEND_REQUEST_KEY_TYPE, Message.TYPE_LIKE);
							cData.put("notification_alert", UserManager.getInstance(WallActivity.this).getCurrentUser().userName+" 對你的貼文按讚");
							IMManager.getInstance(WallActivity.this).getAnIM().sendBinary(post.owner.clientId, new byte[1], Constant.FRIEND_REQUEST_TYPE_SEND, cData);
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				mWallView.setWallManager(mWallManager);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	  	if(resultCode == Activity.RESULT_OK){
	  		initData();
	  	}
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
}
