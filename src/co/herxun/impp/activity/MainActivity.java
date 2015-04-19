package co.herxun.impp.activity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.arrownock.social.IAnSocialCallback;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import co.herxun.impp.R;
import co.herxun.impp.adapter.FragmentPagerAdapter;
import co.herxun.impp.controller.SocialManager;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.fragment.BaseFragment;
import co.herxun.impp.fragment.ChatListFragment;
import co.herxun.impp.fragment.ExploreFragment;
import co.herxun.impp.fragment.FriendListFragment;
import co.herxun.impp.fragment.SettingFragment;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.SlidingTabLayout;
import eu.janmuller.android.simplecropimage.CropImage;

public class MainActivity extends ActionBarActivity {
	
	
	private SlidingTabLayout mSlidingTabLayout;
	private ViewPager mViewPager;
	private AppBar mAppbar;
	
	private ChatListFragment mChatListFragment;
	private FriendListFragment mFriendListFragment;
	private ExploreFragment mExploreFragment;
	private SettingFragment mSettingFragment;
	
	private List<BaseFragment> fragList;
	
	private boolean doubleBackToExistPressedOnce = false;
	
	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener(){
		public void onPageScrollStateChanged(int arg0) {}
		public void onPageScrolled(int arg0, float arg1, int arg2) {}
		@Override
		public void onPageSelected(int location) {
			BaseFragment frag = fragList.get(location);
			frag.onViewShown();
			
			mAppbar.initLayout();
			mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
			if(frag instanceof ChatListFragment){
				onChatListFragmentShown((ChatListFragment)frag);
			}else if(frag instanceof FriendListFragment){
				onFriendListFragmentShown((FriendListFragment)frag);
			}else if(frag instanceof ExploreFragment){
				mAppbar.getMenuItemView().setVisibility(View.GONE);
			}else if(frag instanceof SettingFragment){
				mAppbar.getMenuItemView().setVisibility(View.GONE);
			}
			
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		checkBundle();
		initView();
	}

	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
		checkBundle();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(mOnPageChangeListener!=null){
			mOnPageChangeListener.onPageSelected(mViewPager.getCurrentItem());
		}
		
	}
	
	private void checkBundle(){
		if(getIntent().hasExtra("payload")){
			String payload = getIntent().getStringExtra("payload");
			String alert = null; 
			
			Chat chat = null;
			try {
				JSONObject json = new JSONObject(payload);
				alert = json.getJSONObject("android").getString("alert");
				if(json.has("topic_id")){
					Topic topic = new Topic();
					topic.topicId = json.getString("topic_id");
					topic = topic.getFromTable();
					chat = IMManager.getInstance(this).addChat(topic);
				}else if(json.has("from")){
					chat = IMManager.getInstance(this).addChat(json.getString("from"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(alert!=null && (alert.contains(getString(R.string.anlive_push_call)) || alert.contains(getString(R.string.anlive_push_video_call)))){
			}else{
				if(chat!=null){
					Intent i = new Intent(this,ChatActivity.class);
					Bundle b = new Bundle();
					b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
					i.putExtras(b);
					startActivity(i);
				}
			}
		}
	}
	
	private void initView(){
		mChatListFragment = new ChatListFragment(getString(R.string.tab_title_chat));
		mFriendListFragment = new FriendListFragment(getString(R.string.tab_title_friend));
		mExploreFragment = new ExploreFragment(getString(R.string.tab_title_explore));
		mSettingFragment = new SettingFragment(getString(R.string.tab_title_setting));

		mAppbar = (AppBar)findViewById(R.id.toolbar);
		mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
		
		mViewPager = (ViewPager)findViewById(R.id.viewpager);
		fragList = new ArrayList<BaseFragment>();
		fragList.add(mChatListFragment);
		fragList.add(mFriendListFragment);
		fragList.add(mExploreFragment);
		fragList.add(mSettingFragment);
		FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(),fragList);
		mViewPager.setAdapter(mFragmentPagerAdapter);
		
		mSlidingTabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setDistributeEvenly(true);
		mSlidingTabLayout.setViewPager(mViewPager);
		mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
		    @Override
		    public int getIndicatorColor(int position) {
		        return getResources().getColor(R.color.no13);
		    }
		});
		mSlidingTabLayout.setOnPageChangeListener(mOnPageChangeListener);
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				mOnPageChangeListener.onPageSelected(0);
			}
		}, 300);
	}
	
	private void onChatListFragmentShown(final ChatListFragment frag){
		mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
		mAppbar.getMenuItemView().setImageResource(R.drawable.menu_search);
		mAppbar.getMenuItemView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mAppbar.getMenuItemView().setVisibility(View.GONE);
				mAppbar.getLogoView().setImageResource(R.drawable.menu_back);
				RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
				rlpLogo.width = Utils.px2Dp(v.getContext(), 56);
				rlpLogo.height = Utils.px2Dp(v.getContext(), 56);
				rlpLogo.leftMargin = 0;
				mAppbar.getLogoView().setLayoutParams(rlpLogo);
				mAppbar.getLogoView().setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
						rlpLogo.width = Utils.px2Dp(v.getContext(), 54);
						rlpLogo.height = Utils.px2Dp(v.getContext(), 26);
						rlpLogo.leftMargin = Utils.px2Dp(v.getContext(), 16);
						mAppbar.getLogoView().setLayoutParams(rlpLogo);
						mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
						mAppbar.getEditText().setVisibility(View.GONE);
						mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
						mAppbar.getEditText().setText("");
					}
				});
				mAppbar.getEditText().setVisibility(View.VISIBLE);
				mAppbar.getEditText().requestFocus();
				mAppbar.getEditText().setHint(R.string.friend_list_search);
				mAppbar.getEditText().addTextChangedListener(new TextWatcher() {
				    @Override
				    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
						frag.filterList(cs.toString());
				    }
				    @Override
				    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
				    public void afterTextChanged(Editable arg0) { }
				});
			}
		});
	}
	
	private void onFriendListFragmentShown(final FriendListFragment frag){
		mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
		mAppbar.getMenuItemView().setImageResource(R.drawable.menu_search);
		mAppbar.getMenuItemView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mAppbar.getMenuItemView().setVisibility(View.GONE);
				mAppbar.getLogoView().setImageResource(R.drawable.menu_back);
				RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
				rlpLogo.width = Utils.px2Dp(v.getContext(), 56);
				rlpLogo.height = Utils.px2Dp(v.getContext(), 56);
				rlpLogo.leftMargin = 0;
				mAppbar.getLogoView().setLayoutParams(rlpLogo);
				mAppbar.getLogoView().setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
						rlpLogo.width = Utils.px2Dp(v.getContext(), 54);
						rlpLogo.height = Utils.px2Dp(v.getContext(), 26);
						rlpLogo.leftMargin = Utils.px2Dp(v.getContext(), 16);
						mAppbar.getLogoView().setLayoutParams(rlpLogo);
						mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
						mAppbar.getEditText().setVisibility(View.GONE);
						mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
						mAppbar.getEditText().setText("");
					}
				});
				mAppbar.getEditText().setVisibility(View.VISIBLE);
				mAppbar.getEditText().requestFocus();
				mAppbar.getEditText().setHint(R.string.friend_list_search);
				mAppbar.getEditText().addTextChangedListener(new TextWatcher() {
				    @Override
				    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
						frag.filterList(cs.toString());
				    }
				    @Override
				    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
				    public void afterTextChanged(Editable arg0) { }
				});
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onBackPressed() {
		Handler h = new Handler();
		Runnable r =new Runnable(){
			@Override
			public void run() {
				doubleBackToExistPressedOnce = false;
			}
		};
		if(!doubleBackToExistPressedOnce){
			doubleBackToExistPressedOnce = true;
			Toast.makeText(this,getString(R.string.general_press_again_to_exit),Toast.LENGTH_SHORT).show();
			h.postDelayed(r, 2000);
		}else{
			h.removeCallbacks(r);
			super.onBackPressed();
		}
	}
}
