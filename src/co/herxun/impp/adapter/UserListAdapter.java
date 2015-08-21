package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.arrownock.social.IAnSocialCallback;

import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.FetchFriendCallback;
import co.herxun.impp.model.Friend;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.UserListItem;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
	private List<User> userList;
	private Map<String,Boolean> friendStatus;
	private Context ct;
	
	public UserListAdapter(Context ct){
		this.ct = ct;
		userList = new ArrayList<User>();
		friendStatus = new HashMap<String,Boolean>();
	}
	
	public void applyData(List<User> users){
		userList.clear();
		userList.addAll(users);
		
		refreshFriendStatus();
		
		notifyDataSetChanged();
		
	}
	
	private void refreshFriendStatus(){
		UserManager.getInstance(ct).getMyLocalFriends(new FetchFriendCallback(){
			@Override
			public void onFinish(List<Friend> friends) {
				friendStatus.clear();
				for(Friend f :friends){
					friendStatus.put(f.targetClientId, f.isMutual);
				}
				notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public int getCount() {
		return userList.size();
	}

	@Override
	public User getItem(int position) {
		return userList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NewFriendListItem view = (NewFriendListItem) convertView;
		if (convertView == null) {
			view = new NewFriendListItem(parent.getContext());
		}
		
		view.setData(userList.get(position));
		
		return view;
	}
	
	public class NewFriendListItem extends UserListItem{
		private TextView textFriendStatus;
		public NewFriendListItem(Context ct) {
			super(ct);

			setLayoutParams(new AbsListView.LayoutParams(-1,Utils.px2Dp(ct, 56)));
			textFriendStatus = new TextView(ct);
			textFriendStatus.setGravity(Gravity.CENTER_VERTICAL);
			textFriendStatus.setPadding(Utils.px2Dp(ct, 24), 0, Utils.px2Dp(ct, 24), 0);
			RelativeLayout.LayoutParams rlpTextFS = new RelativeLayout.LayoutParams(-2,Utils.px2Dp(ct, 36));
			rlpTextFS.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rlpTextFS.addRule(RelativeLayout.CENTER_VERTICAL);
			rlpTextFS.rightMargin = Utils.px2Dp(ct,16);
			addView(textFriendStatus,rlpTextFS);
		}
		
		public void setData(final User user){
			setIcon(user.userPhotoUrl, R.drawable.friend_default);
			setName(user.userName);

			float corner = Utils.px2Dp(ct, 2);
			GradientDrawable bgBtn = new GradientDrawable();
			bgBtn.setCornerRadii(new float[]{corner,corner,corner,corner,corner,corner,corner,corner});
			
			if(friendStatus.containsKey(user.clientId)){
				if(friendStatus.get(user.clientId)){
					textFriendStatus.setTextColor(ct.getResources().getColor(R.color.no9));
					bgBtn.setColor(getResources().getColor(R.color.no6));
					textFriendStatus.setBackgroundDrawable(bgBtn);
					textFriendStatus.setText(R.string.friend_request_status_isfriend);
				}else{
					textFriendStatus.setTextColor(ct.getResources().getColor(R.color.no5));
					textFriendStatus.setBackgroundColor(ct.getResources().getColor(R.color.no1));
					textFriendStatus.setText(R.string.friend_request_status_sent);
				}
				textFriendStatus.setOnClickListener(null);
			}else{
				textFriendStatus.setTextColor(ct.getResources().getColor(R.color.no5));
				textFriendStatus.setBackgroundColor(ct.getResources().getColor(R.color.no3));
				textFriendStatus.setText(R.string.friend_request_status_add);
				textFriendStatus.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						DBug.e(user.userName, user.clientId);
						textFriendStatus.setText(R.string.friend_request_status_requesting);
						
						UserManager.getInstance(ct).sendFriendRequest(user,new IAnSocialCallback(){
							@Override
							public void onFailure(JSONObject arg0) {
								textFriendStatus.setText(R.string.friend_request_status_add);
							}

							@Override
							public void onSuccess(JSONObject arg0) {
								refreshFriendStatus();
							}
						});
					}
				});
			}
		}
	}
}
