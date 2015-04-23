package co.herxun.impp.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;

import co.herxun.impp.R;
import co.herxun.impp.R.layout;
import co.herxun.impp.adapter.UserChooseListAdapter;
import co.herxun.impp.adapter.UserChooseListAdapter.ChooseListener;
import co.herxun.impp.adapter.UserListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.FetchUserCallback;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class CreateTopicActivity extends BaseActivity  implements Observer{
	private AppBar mAppbar;
	private ListView mListView;
	private UserChooseListAdapter mUserChooseListAdapter;
	private String type ;
	private Set<String> filterClients;
	private Topic mTopic;
	
	public final static String TYPE_CREATE = "type_create";
	public final static String TYPE_INVITE = "type_invite";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_topic);
		
		checkBundle();
		
		initView();
		
		IMManager.getInstance(this).addObserver(this);
	}
	
	private void checkBundle(){
		if(getIntent().getExtras()!=null && getIntent().getExtras().containsKey(Constant.INTENT_EXTRA_KEY_TOPIC_EDIT_TYPE)){
			type = getIntent().getExtras().getString(Constant.INTENT_EXTRA_KEY_TOPIC_EDIT_TYPE);
			if(type.equals(TYPE_INVITE)){
				mTopic =  (Topic) getIntent().getExtras().getSerializable(Constant.INTENT_EXTRA_KEY_TOPIC);
				mTopic = mTopic.getFromTable();
				
				String[] filterUsers =  getIntent().getExtras().getStringArray(Constant.INTENT_EXTRA_KEY_TOPIC_EDIT_FILTER_MEMBERS);
				filterClients = new HashSet<String>();
				for(String client : filterUsers){
					filterClients.add(client);
				}
			}
		}
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
		mAppbar.getMenuItemView().setImageResource(R.drawable.menu_done);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56));
		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mAppbar.getMenuItemView().setLayoutParams(rlp);
		mAppbar.getMenuItemView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(type.equals(TYPE_INVITE)){
					Set<String> members = mUserChooseListAdapter.getChosenUser();
					IMManager.getInstance(v.getContext()).addTopicMembers(members, mTopic);
				}else if(type.equals(TYPE_CREATE)){
					String topicName = "";
					Set<String> members = mUserChooseListAdapter.getChosenUser();
					members.add(UserManager.getInstance(v.getContext()).getCurrentUser().clientId);
					for(String member:members){
						topicName += UserManager.getInstance(v.getContext()).getUserByClientId(member).userName+",";
					}
					topicName = topicName.substring(0,topicName.length()-1);
					IMManager.getInstance(v.getContext()).createTopic(topicName,members);
				}
				finish();
			}
		});
		
		if(type.equals(TYPE_CREATE)){
			mAppbar.getEditText().setVisibility(View.VISIBLE);
			mAppbar.getEditText().setSingleLine();
			mAppbar.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
			mAppbar.getEditText().addTextChangedListener(new TextWatcher() {
			    @Override
			    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			    	mUserChooseListAdapter.filter(cs.toString());   
			    }
			    @Override
			    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			    public void afterTextChanged(Editable arg0) { }
			});
		}else if(type.equals(TYPE_INVITE)){
			mAppbar.getTextView().setVisibility(View.VISIBLE);
			mAppbar.getTextView().setText(R.string.chat_topic_invite);
		}
		
		enableSumitButton(false);
		
		mListView = (ListView)findViewById(R.id.friend_search_listView);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);
		mUserChooseListAdapter = new UserChooseListAdapter(this);
		mListView.setAdapter(mUserChooseListAdapter);
		mUserChooseListAdapter.setChooseListener(new ChooseListener(){
			@Override
			public void onChooseChange(Set<String> chosenUser) {
				enableSumitButton(chosenUser.size()>0);
			}
		});
		
		mUserChooseListAdapter.fillLocalData(new FetchUserCallback(){
			@Override
			public void onFinish(List<User> users) {
				if(type.equals(TYPE_INVITE)){
					mUserChooseListAdapter.filter(filterClients);
				}
			}
		});
	}
	
	public void enableSumitButton(boolean enable){
		if(enable){
			mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
		}else{
			mAppbar.getMenuItemView().setVisibility(View.GONE);
		}
	}

	@Override
	public void update(Observable observable, final Object data) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				if(data instanceof IMManager.UpdateType && ((IMManager.UpdateType)data).equals(IMManager.UpdateType.Topic)){
					//finish();
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
