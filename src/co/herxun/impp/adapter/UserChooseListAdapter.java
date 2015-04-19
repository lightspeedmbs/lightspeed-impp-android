package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.json.JSONObject;

import com.arrownock.exception.ArrownockException;
import com.arrownock.social.IAnSocialCallback;

import co.herxun.impp.R;
import co.herxun.impp.activity.SearchUserActivity;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.FetchFriendCallback;
import co.herxun.impp.controller.UserManager.FetchUserCallback;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.FetchLocalTopicCallback;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.im.model.TopicMember;
import co.herxun.impp.model.Friend;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.UserListItem;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class UserChooseListAdapter extends BaseAdapter{
	private List<User> userList;
	private List<User> allUserList;
	private Set<String> chosenClientId;
	private Context ct;
	private ChooseListener mChooseListener;
	
	public UserChooseListAdapter(Context ct){
		this.ct = ct;
		userList = new ArrayList<User>();
		allUserList = new ArrayList<User>();
		chosenClientId = new HashSet<String>();
	}
	
	public void applyData(List<User> users){
		allUserList.clear();
		allUserList.addAll(users);
		filter("");
		notifyDataSetChanged();
		
	}
	public void fillLocalData(final FetchUserCallback callback){
		UserManager.getInstance(ct).getMyLocalFriends(new FetchUserCallback(){
			@Override
			public void onFinish(List<User> users) {
				applyData(users);
				if(callback!=null){
					callback.onFinish(users);
				}
			}
		});
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return userList.size();
	}

	@Override
	public User getItem(int position) {
		// TODO Auto-generated method stub
		return userList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UserChooseListItem view = (UserChooseListItem) convertView;
		if (convertView == null) {
			view = new UserChooseListItem(parent.getContext());
		}
		
		view.setData(userList.get(position));
		
		return view;
	}
	
	public class UserChooseListItem extends UserListItem{
		private CheckBox mCheckBox;
		public UserChooseListItem(Context ct) {
			super(ct);

			setLayoutParams(new AbsListView.LayoutParams(-1,Utils.px2Dp(ct, 56)));
			
			mCheckBox = new CheckBox(ct);
			RelativeLayout.LayoutParams rlpTextFS = new RelativeLayout.LayoutParams(-2,-2);
			rlpTextFS.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rlpTextFS.addRule(RelativeLayout.CENTER_VERTICAL);
			rlpTextFS.rightMargin = Utils.px2Dp(ct, 16);
			addView(mCheckBox,rlpTextFS);
			
		}
		
		public void setData(final User user){
			setIcon(user.userPhotoUrl, R.drawable.friend_default);
			setName(user.userName);

			mCheckBox.setChecked(chosenClientId.contains(user.clientId));
			mCheckBox.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if(mCheckBox.isChecked()){
						chosenClientId.add(user.clientId);
					}else{
						if(chosenClientId.contains(user.clientId)){
							chosenClientId.remove(user.clientId);
						}
					}
					if(mChooseListener!=null){
						mChooseListener.onChooseChange(chosenClientId);
					}
				}
				
			});
			
		}
	}
	
	public void filter(String charText) {
		userList.clear();
		if (charText.length() == 0) {
			userList.addAll(allUserList);
		} else {
			for (User user : allUserList) {
				if (user.userName.contains(charText)) {
					userList.add(user);
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public void filter(Set<String> filterClient){
		userList.clear();
		if (filterClient.size() == 0) {
			userList.addAll(allUserList);
		} else {
			for (User user : allUserList) {
				if (!filterClient.contains(user.clientId)) {
					userList.add(user);
				}
			}
		}
		notifyDataSetChanged();
	}
	
	public Set<String> getChosenUser(){
		return  chosenClientId;
	}
	
	public void setChooseListener(ChooseListener lsr){
		mChooseListener = lsr;
	}
	
	public interface ChooseListener{
		public void onChooseChange(Set<String> chosenUser);
	}
}
