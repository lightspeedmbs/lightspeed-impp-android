package co.herxun.impp.fragment;

import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.ChatActivity;
import co.herxun.impp.activity.CreateTopicActivity;
import co.herxun.impp.activity.FriendRequestActivity;
import co.herxun.impp.activity.SearchUserActivity;
import co.herxun.impp.activity.UserDetailActivity;
import co.herxun.impp.adapter.FriendListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.BadgeView;

import com.arrownock.social.IAnSocialCallback;

public class FriendListFragment extends BaseFragment implements Observer{
	private ListView mListView;
	private ImageView imgNewBtn;
	private FriendListAdapter mFriendListAdapter;
	private Context ct;
	private BadgeView friendRequestBadge;
	private AlertDialog mActionDialog;
	private Handler handler;
	private TextView noFriendLabel;
	private View friendRequestLine;
	
	public FriendListFragment() {
		this("");
	}
	
	public FriendListFragment(String title) {
		super(title);
		handler = new Handler();
	}
	
	@Override
	public void onViewShown(){
		initData();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend, container, false);
        ct = getActivity();
        
        return rootView;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		IMManager.getInstance(ct).addObserver(this);
		UserManager.getInstance(ct).addObserver(this);
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
//		IMManager.getInstance(ct).deleteObserver(this);
//		UserManager.getInstance(ct).deleteObserver(this);
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
        initView(ct);
	}
	
	private void initView(final Context ct){
		noFriendLabel = (TextView) getActivity().findViewById(R.id.noFriendLabel);
		friendRequestLine = (View) getActivity().findViewById(R.id.friend_request_line);
		friendRequestBadge = (BadgeView) getActivity().findViewById(R.id.friend_request_badgeView);
        mListView = (ListView) getActivity().findViewById(R.id.friend_listView);
        mListView.setDivider(null);
        mListView.setDividerHeight(0);
        friendRequestBadge.setTextColor(ct.getResources().getColor(R.color.no5));
        friendRequestBadge.setBadgeColor(ct.getResources().getColor(R.color.no1));
        getActivity().findViewById(R.id.friend_request_row).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(),FriendRequestActivity.class));
				getActivity().overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
			}
        });
        
        mFriendListAdapter = new FriendListAdapter(ct);
        mListView.setAdapter(mFriendListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				position -= mListView.getHeaderViewsCount();
				if(mFriendListAdapter.getItem(position) instanceof Topic){
					Topic topic = (Topic) mFriendListAdapter.getItem(position);
					Chat chat = IMManager.getInstance(ct).addChat(topic);
					IMManager.getInstance(ct).notifyChatUpdated();
					Intent i = new Intent(ct,ChatActivity.class);
					Bundle b = new Bundle();
					b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
					i.putExtras(b);
					ct.startActivity(i);
				}else if(mFriendListAdapter.getItem(position) instanceof User){
					User user = (User) mFriendListAdapter.getItem(position);
					Intent i = new Intent(view.getContext(),UserDetailActivity.class);
					i.putExtra(Constant.INTENT_EXTRA_KEY_CLIENT,user.clientId);
					view.getContext().startActivity(i);
					((Activity) view.getContext()).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
				}
			}
        });
        
        mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        	@Override
        	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        		if(mFriendListAdapter.getCount() > 0) {
        			noFriendLabel.setVisibility(View.GONE);
        			friendRequestLine.setVisibility(View.GONE);
        		} else {
        			friendRequestLine.setVisibility(View.VISIBLE);
        			boolean isSearching = false;
        			AppBar bar = (AppBar) getActivity().findViewById(R.id.toolbar);
        			if(bar != null && bar.getEditText() != null
        							&& bar.getEditText().getText() != null 
        							&& !"".equals(bar.getEditText().getText().toString())) {
        					isSearching = true;
        			} 
        			if(isSearching) {
        				noFriendLabel.setVisibility(View.GONE);
        			} else {
	        			noFriendLabel.setVisibility(View.VISIBLE);
        			}
        		}
        	}
        });
        
        imgNewBtn = (ImageView) getActivity().findViewById(R.id.img_new_btn);
        imgNewBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mActionDialog.show();
			}
        });
        
		AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(ct);
		View view = getActivity().getLayoutInflater().inflate( R.layout.view_friend_alert, null);
		view.findViewById(R.id.action_dialog_friend_btn).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ct,SearchUserActivity.class);
		    	ct.startActivity(i);
				getActivity().overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
		    	mActionDialog.dismiss();
			}
		});
		view.findViewById(R.id.action_dialog_topic_btn).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent i = new Intent(ct,CreateTopicActivity.class);
				Bundle b = new Bundle();
				b.putString(Constant.INTENT_EXTRA_KEY_TOPIC_EDIT_TYPE, CreateTopicActivity.TYPE_CREATE);
				i.putExtras(b);
		    	ct.startActivity(i);
				getActivity().overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
		    	mActionDialog.dismiss();
			}
		});
		dialogBuiler.setView(view);
		mActionDialog = dialogBuiler.create();
	}

	public void initData(){
        mFriendListAdapter.fillLocalData();
		//mFriendListAdapter.fillRemoteData(true);
        int badgeCount = UserManager.getInstance(ct).getLocalPendingFriendRequestCount();
        friendRequestBadge.setBadgeCount(badgeCount);
	}
	
	private void updateFriendRequestBadge(){
		UserManager.getInstance(ct).fetchFriendRequest(new IAnSocialCallback(){
			@Override
			public void onFailure(JSONObject arg0) {
			}
			@Override
			public void onSuccess(JSONObject arg0) {
				handler.post(new Runnable(){
					@Override
					public void run() {
						friendRequestBadge.setBadgeCount(UserManager.getInstance(ct).getLocalPendingFriendRequestCount());
					}
				});
			}
		});
	}
	
	public void filterList(String text){
		mFriendListAdapter.filter(text);
	}
	
	@Override
	public void update(final Observable observable, final Object data) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				if(observable instanceof IMManager){
					if(data instanceof IMManager.UpdateType && ((IMManager.UpdateType)data).equals(IMManager.UpdateType.Topic)){
						mFriendListAdapter.fillLocalData();
					}else if(data instanceof IMManager.UpdateType && ((IMManager.UpdateType)data).equals(IMManager.UpdateType.FriendRequest)){
						updateFriendRequestBadge();
						mFriendListAdapter.fillLocalData();
					}
				}else if(observable instanceof UserManager){
					if(data instanceof UserManager.UpdateType){
						mFriendListAdapter.fillLocalData();
						if(((UserManager.UpdateType)data).equals(UserManager.UpdateType.Friend)){
							
						}
					}
				}
			}
		});
	}
}
