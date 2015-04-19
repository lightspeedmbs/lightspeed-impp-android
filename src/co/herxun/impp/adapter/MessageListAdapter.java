package co.herxun.impp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.GetMessageCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.view.MessageListItem;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.DBug;

public class MessageListAdapter extends BaseAdapter {
	private Chat chat;
	private List<Message> msgList;
	private Map<String,ChatUser> userMap;
	private Context ct;
	private Handler handler;
	private String mClientId;
	
	private Map<String,Integer> msgIdIndexMap;
	
	public MessageListAdapter(Context ct,Chat chat){
		this.ct = ct;
		this.chat = chat;
		mClientId = IMManager.getInstance(ct).getCurrentClientId();
		msgList = new ArrayList<Message>();
		userMap = new HashMap<String,ChatUser>();
		msgIdIndexMap = new HashMap<String,Integer>();
		handler = new Handler();
	}
	
	public void setUserMap(Map<String,ChatUser> userMap){
		this.userMap = userMap;
		notifyDataSetChanged();
	}
	
	public void applyData(List<Message> msgs){
		msgList.clear();
		msgList.addAll(msgs);
		
		notifyDataSetChanged();
		
	}
	
	public void fillLocalData(final GetMessageCallback callback){
		IMManager.getInstance(ct).getMessageByChat(chat, new GetMessageCallback(){
			@Override
			public void onFinish(final List<Message> data) {
				applyData(data);
				
				for(int i=0;i<data.size();i++){
					Message msg = data.get(i);
					if(msg.status!=null && msg.status.equals(Message.STATUS_SENDING)){
						msgIdIndexMap.put(msg.msgId, i);
					}
				}
				if(callback!=null){
					callback.onFinish(data);
				}
				
			}
		});
	}
	
	public int addMessage(Message msg){
		msgList.add(msg);
		notifyDataSetChanged();
		if(msg.status.equals(Message.STATUS_SENDING)){
			msgIdIndexMap.put(msg.msgId, msgList.size()-1);
		}
		return msgList.size()-1;
	}
	
	public void updateMessageStatus(String msgId,String status){
		if(msgIdIndexMap.containsKey(msgId)){
			Message msg = msgList.get(msgIdIndexMap.get(msgId));
			msg.status = status;
			notifyDataSetChanged();
		}
	}
	
	public void setMessageReadAck(String msgId){
		if(msgIdIndexMap.containsKey(msgId)){
			Message msg = msgList.get(msgIdIndexMap.get(msgId));
			msg.readACK = true;
			notifyDataSetChanged();
		}
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msgList.size();
	}

	@Override
	public Message getItem(int position) {
		// TODO Auto-generated method stub
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MessageListItem view = (MessageListItem) convertView;
		if (convertView == null) {
			view = new MessageListItem(parent.getContext());
		}
		
		view.setOwner(msgList.get(position).isMine(), userMap.get(msgList.get(position).fromClient));
		view.setMessageData(msgList.get(position));
		
		
		return view;
	}
	
}
