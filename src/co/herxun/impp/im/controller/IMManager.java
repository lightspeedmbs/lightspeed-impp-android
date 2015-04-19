package co.herxun.impp.im.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.controller.MyIAnLiveEventListener;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.im.model.TopicMember;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.im.AnIMMessage;
import com.arrownock.im.AnIMStatus;
import com.arrownock.im.callback.AnIMAddClientsCallbackData;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMBindAnPushServiceCallbackData;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;
import com.arrownock.im.callback.AnIMGetClientIdCallbackData;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.AnIMGetSessionInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicInfoCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMMessageSentCallbackData;
import com.arrownock.im.callback.AnIMNoticeCallbackData;
import com.arrownock.im.callback.AnIMReadACKCallbackData;
import com.arrownock.im.callback.AnIMReceiveACKCallbackData;
import com.arrownock.im.callback.AnIMRemoveClientsCallbackData;
import com.arrownock.im.callback.AnIMRemoveTopicCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.im.callback.AnIMUnbindAnPushServiceCallbackData;
import com.arrownock.im.callback.AnIMUpdateTopicCallbackData;
import com.arrownock.im.callback.IAnIMCallback;
import com.arrownock.im.callback.IAnIMHistoryCallback;
import com.arrownock.live.AnLive;

public class IMManager extends Observable{
	private static IMManager sIMManager;
	private AnIM anIM;
	private Handler handler;
	private Context ct;
	
	private final static int RECONNECT_RATE = 1000;
	private String currentClientId ;
	
	public enum UpdateType{
		Topic,Chat,FriendRequest
	}
	
	private IMManager(Context ct){
		this.ct = ct;
		handler = new Handler();
		try {
			anIM = new AnIM(ct,ct.getString(R.string.app_key));
			anIM.setCallback(imCallback);
		} catch (ArrownockException e) {
			
			e.printStackTrace();
		}
	}
	
	public static IMManager getInstance(Context ct){
		if(sIMManager==null){
			sIMManager = new IMManager(ct);
		}
		return sIMManager;
	}
	
	public AnIM getAnIM(){
		return anIM;
	}
	
	public void connect(String clientId){
		this.currentClientId = clientId;
		try {
			anIM.connect(clientId);
		} catch (ArrownockException e) {
			
			e.printStackTrace();
		}
	}
	
	private void checkCoonnection(){
		if(!anIM.isOnline() && currentClientId != null){
			 connect(currentClientId);
		}
	}
	
	public String getCurrentClientId(){
		return currentClientId;
	}
	
	
	
	//TODO Message
	
	public Message sendMessage(ChatUser user,Message message){
		Chat chat = message.chat;
		String msgId = null ;
		Map<String,String> customData = new HashMap<String,String>();
		customData.put("name", user.getUsername());
		customData.put("photoUrl", user.getIconUrl());
		try {
			if(chat.topic!=null){
				if(message.type.equals(Message.TYPE_TEXT)){
					customData.put("notification_alert", user.getUsername()+"："+message.message);
					msgId = anIM.sendMessageToTopic(chat.topic.topicId, message.message,customData,true);
					
				}else if(message.type.equals(Message.TYPE_IMAGE)){
					customData.put("notification_alert", user.getUsername()+" "+ct.getString(R.string.noti_image));
					customData.put("type", Message.TYPE_IMAGE);
					customData.put("url", message.fileURL);
					msgId = anIM.sendBinaryToTopic(chat.topic.topicId, message.content, message.type, customData,true);
					
				}else if(message.type.equals(Message.TYPE_RECORD)){
					customData.put("notification_alert", ct.getString(R.string.noti_record).replace("#", user.getUsername()));
					msgId = anIM.sendBinaryToTopic(chat.topic.topicId, message.content, message.type, customData,true);
				}
			}else{
				if(message.type.equals(Message.TYPE_TEXT)){
					customData.put("notification_alert", user.getUsername()+"："+message.message);
					msgId = anIM.sendMessage(chat.targetClientId, message.message,customData,true);
					
				}else if(message.type.equals(Message.TYPE_IMAGE)){
					customData.put("notification_alert", user.getUsername()+" "+ct.getString(R.string.noti_image));
					customData.put("type", Message.TYPE_IMAGE);
					customData.put("url", message.fileURL);
					msgId = anIM.sendBinary(chat.targetClientId, message.content, message.type, customData, true);
					
				}else if(message.type.equals(Message.TYPE_RECORD)){
					customData.put("notification_alert", ct.getString(R.string.noti_record).replace("#", user.getUsername()));
					msgId = anIM.sendBinary(chat.targetClientId, message.content, message.type, customData,true);
				}
			}
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
		message.msgId = msgId;
		message.currentClientId = currentClientId;
		message.fromClient = currentClientId;
		message.status = Message.STATUS_SENDING;
		message.readed = true;		
		
		message.fromUsername = user.getUsername();
		message.fromUserIconUrl = user.getIconUrl();
		message.update();

		setChanged();
		notifyObservers(message);
		
		return message;
	}
	
	public void setMessageReaded(Message msg,boolean sendAck){
		msg.readed = true;
		msg.update();
		
		if(sendAck){
			try {
				Set<String> client = new HashSet<String>();
				client.add(msg.fromClient);
				getAnIM().sendReadACK(client, msg.msgId);
			} catch (ArrownockException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void getMessageByChat(final Chat chat,final GetMessageCallback callback){
		new Thread(new Runnable(){
			@Override
			public void run() {
				final List<Message> data = chat.messages();
				handler.post(new Runnable(){
					@Override
					public void run() {
						if(callback!=null){
							callback.onFinish(data);
						}
					}
				});
			}
		}).start();
	}
	
	public interface GetMessageCallback{
		public void onFinish(List<Message> data);
	}
	
	public void getTotalUnReadMessageCount(final GetUnReadedMessageCountCallback callback){
		new Thread(new Runnable(){
			@Override
			public void run() {
		    	final List<Message> messages = new Select().from(Message.class).where("readed = \""+0+"\" and currentClientId = \""+currentClientId+"\"").execute();   	
		    	
				handler.post(new Runnable(){
					@Override
					public void run() {
						if(callback!=null){
							if(messages==null){
								callback.onFinish(0);
							}else{
								callback.onFinish(messages.size());
							}
						}
					}
				});
			}
		}).start();
	}
	
	public interface GetUnReadedMessageCountCallback{
		public void onFinish(int count);
	}
	
	//TODO Chat
	
	public void getAllMyChat(final GetChatCallback callback){
		new Thread(new Runnable(){
			@Override
			public void run() {
				final List<Chat> data = new Select().from(Chat.class).where("currentClientId = ? ",currentClientId).execute();
				Collections.sort(data, new Comparator<Chat>(){
					@Override
					public int compare(Chat chat1, Chat chat2) {
						if(chat1.lastMessage()==null||chat2.lastMessage()==null){
							return 1;
						}
						
						long time1 = chat1.lastMessage().timestamp;
						long time2 = chat2.lastMessage().timestamp;
						if (time1 > time2) {
							return -1;
						} else if (time1 < time2) {
							return 1;
						} else {
							return 0;
						}
					}
				});
				
				handler.post(new Runnable(){
					@Override
					public void run() {
						if(callback!=null){
							callback.onFinish(data);
						}
					}
				});
			}
		}).start();
	}
	
	public Chat addChat(Topic topic){
		Chat chat = new Chat();
		chat.currentClientId = currentClientId;
		chat.topic = new Select().from(Topic.class).where("topicId = ?",topic.topicId).executeSingle();
		chat.update();
		
		return chat.getFromTable();
	}
	public Chat addChat(String targetClientId){
		Chat chat = new Chat();
		chat.currentClientId = currentClientId;
		chat.targetClientId = targetClientId;
		chat.update();
		
		return chat.getFromTable();
	}
	
	public void deleteChat(Chat chat){
		chat.getFromTable().delete();
		
		List<Message> msgs = chat.messages();
		ActiveAndroid.beginTransaction();
		try {
		        for (Message msg : msgs) {
		        	msg.delete();
		        }
		        ActiveAndroid.setTransactionSuccessful();
		}finally {
		        ActiveAndroid.endTransaction();
		}
	}
	
	public void notifyChatUpdated(){
		setChanged();
		notifyObservers(UpdateType.Chat);
	}
	
	public interface GetChatCallback{
		public void onFinish(List<Chat> data);
	}
	
	//TODO Topic
	
	public void createTopic(String topicName,Set<String> members){
		try {
			IMManager.getInstance(ct).getAnIM().createTopic(topicName,currentClientId, members);
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}
	
	public void getMyLocalTopic(final FetchLocalTopicCallback callback){
		new Thread(new Runnable(){
			@Override
			public void run() {
//				final List<Topic> data= new Select().from(Topic.class)
//						.innerJoin(TopicMember.class).on("Topic.topicId = TopicMember.topicId").where("TopicMember.clientId = ?", currentClientId).execute();
				
				List<Topic> data= new Select().from(Topic.class).execute();
				final List<Topic> filterdata = new ArrayList<Topic>();
				for(Topic topic : data){
					List<TopicMember> members = new Select().from(TopicMember.class).where("topicId = ?",topic.topicId).execute();
					for(TopicMember member : members){
						if(member.clientId.equals(currentClientId)){
							filterdata.add(topic);
							break;
						}
					}
				}
				
				handler.post(new Runnable(){
					@Override
					public void run() {
						if(callback!=null){
							callback.onFinish(filterdata);
						}
					}
				});
			}
		}).start();
	}
	
	public void removeAllTopics(){
		new Delete().from(Topic.class).execute();
	}
	
	public void fetchAllRemoteTopic(){
		checkCoonnection();
		try {
			anIM.getMyTopicList();
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}
	
	public interface FetchLocalTopicCallback{
		public void onFinish(List<Topic> data);
	}
	
	
	public Topic updateTopic(Topic topic){
		topic = topic.update();
		if (topic.topicName == null) {
			try {
				anIM.getTopicInfo(topic.topicId);
			} catch (ArrownockException e) {
				e.printStackTrace();
			}
		} 
		return topic;
	}
	
	public Topic updateTopicName(String topicName,Topic topic){
		topic.topicName = topicName;
		topic = topic.update();
		if (topic.topicName != null) {
			try {
				anIM.updateTopic(topic.topicId, topic.topicName, topic.ownerClientId);
			} catch (ArrownockException e) {
				e.printStackTrace();
			}
		} 
		notifyTopicUpdated();
		return topic;
	}
	
	public Topic addTopicMembers(Set<String> clients,Topic topic){
		for(String client : clients){
			topic.addMember(client);
		}
		if (topic.topicName != null) {
			try {
				anIM.addClientsToTopic(topic.topicId, clients);
			} catch (ArrownockException e) {
				e.printStackTrace();
			}
		} 
		notifyTopicUpdated();
		return topic;
	}
	
	public Topic leaveTopic(Chat chat,Topic topic){
		chat.getFromTable().delete();
		topic.removeMember(currentClientId);
		
		try {
			if(topic.ownerClientId.equals(currentClientId)){
				anIM.removeTopic(topic.topicId);
			}else{
				Set<String> client = new HashSet<String>();
				client.add(currentClientId);
				anIM.removeClientsFromTopic(topic.topicId,client);
			}
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
		notifyTopicUpdated();
		return topic;
	}
	
	
	public void notifyTopicUpdated(){
		setChanged();
		notifyObservers(UpdateType.Topic);
	}
	
	
	
	//TODO callback
	
	private void handleFriendRequest(final String fromClientId,final String friendRequestType){
		handler.post(new Runnable(){
			@Override
			public void run() {
				if(friendRequestType.equals(Constant.FRIEND_REQUEST_TYPE_APPROVE)){
					Toast.makeText(ct, ct.getString(R.string.friend_request_accepted), Toast.LENGTH_LONG).show();
					User user = new User();
					user.clientId = fromClientId;
					UserManager.getInstance(ct).saveUser(user);
					UserManager.getInstance(ct).addFriendLocal(fromClientId,true);
				}else if(friendRequestType.equals(Constant.FRIEND_REQUEST_TYPE_SEND)){
					Toast.makeText(ct, ct.getString(R.string.friend_request_received), Toast.LENGTH_LONG).show();
					User user = new User();
					user.clientId = fromClientId;
					UserManager.getInstance(ct).saveUser(user);
				}
				notifyFriendRequest();
			}
		});
	}
	public void notifyFriendRequest(){
		setChanged();
		notifyObservers(UpdateType.FriendRequest);
	}
	
	private void handleChatMessage(Object data){
		
		Message msg = new Message();
		if(data instanceof AnIMMessageCallbackData){
			AnIMMessageCallbackData msgData = (AnIMMessageCallbackData)data;
			
			Chat chat = IMManager.getInstance(ct).addChat(msgData.getFrom());
			
			msg.currentClientId = currentClientId;
			msg.chat = chat;
			msg.message = msgData.getMessage();
			msg.msgId = msgData.getMsgId();
			msg.fromClient = msgData.getFrom();
			msg.status = Message.STATUS_SENT;
			msg.type = Message.TYPE_TEXT;
			msg.readed = false;
			
			if(msgData.getCustomData()!=null){
				if( msgData.getCustomData().containsKey("name"))
					msg.fromUsername = msgData.getCustomData().get("name");
				if( msgData.getCustomData().containsKey("photoUrl"))
					msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
			}
			msg.update();
			
		}else if(data instanceof AnIMTopicMessageCallbackData){
			AnIMTopicMessageCallbackData msgData = (AnIMTopicMessageCallbackData)data;
			
			Topic topic = new Topic();
			topic.topicId = msgData.getTopic();
			topic = IMManager.getInstance(ct).updateTopic(topic);
			
			Chat chat = IMManager.getInstance(ct).addChat(topic);
			
			msg.currentClientId = currentClientId;
			msg.chat = chat;
			msg.message = msgData.getMessage();
			msg.msgId = msgData.getMsgId();
			msg.fromClient = msgData.getFrom();
			msg.status = Message.STATUS_SENT;
			msg.type = Message.TYPE_TEXT;
			msg.readed = false;		
			
			if(msgData.getCustomData()!=null){
				if( msgData.getCustomData().containsKey("name"))
					msg.fromUsername = msgData.getCustomData().get("name");
				if( msgData.getCustomData().containsKey("photoUrl"))
					msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
			}
			msg.update();
			
		}else if(data instanceof AnIMMessage){
			AnIMMessage msgData = (AnIMMessage)data;
			Chat chat;
			
			if(msgData.getTopicId()!=null && msgData.getTopicId().length()>0){
				Topic topic = new Topic();
				topic.topicId = msgData.getTopicId();
				topic = IMManager.getInstance(ct).updateTopic(topic);
				msg.topicId = msgData.getTopicId();
				chat = IMManager.getInstance(ct).addChat(topic);
			}else{
				chat = IMManager.getInstance(ct).addChat(msgData.getFrom());
			}
			
			msg.currentClientId = currentClientId;
			msg.chat = chat;
			msg.message = msgData.getMessage();
			msg.msgId = msgData.getMsgId();
			msg.fromClient = msgData.getFrom();
			msg.status = Message.STATUS_SENT;
			msg.readed = false;
			
			
			if(msgData.getFileType()==null){
				msg.type = Message.TYPE_TEXT;
			}else{
				msg.type = msgData.getFileType();
				msg.content = msgData.getContent();
				if(msgData.getCustomData()!=null && msgData.getCustomData().containsKey("url")){
					msg.fileURL = msgData.getCustomData().get("url");
				}
			}
			
			if(msgData.getCustomData()!=null){
				if( msgData.getCustomData().containsKey("name"))
					msg.fromUsername = msgData.getCustomData().get("name");
				if( msgData.getCustomData().containsKey("photoUrl"))
					msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
			}
			msg.update();
			
		}else if(data instanceof AnIMBinaryCallbackData){
			AnIMBinaryCallbackData msgData = (AnIMBinaryCallbackData)data;
			
			Chat chat = IMManager.getInstance(ct).addChat(msgData.getFrom());
			
			msg.currentClientId = currentClientId;
			msg.chat = chat;
			msg.msgId = msgData.getMsgId();
			msg.fromClient = msgData.getFrom();
			msg.status = Message.STATUS_SENT;
			msg.type = msgData.getFileType();
			msg.readed = false;
			msg.content = msgData.getContent();
			msg.fileURL = msgData.getCustomData().get("url");
			
			if(msgData.getCustomData()!=null){
				if( msgData.getCustomData().containsKey("name"))
					msg.fromUsername = msgData.getCustomData().get("name");
				if( msgData.getCustomData().containsKey("photoUrl"))
					msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
			}
			msg.update();
			
		}else if(data instanceof AnIMTopicBinaryCallbackData){
			AnIMTopicBinaryCallbackData msgData = (AnIMTopicBinaryCallbackData)data;
			
			Topic topic = new Topic();
			topic.topicId = msgData.getTopic();
			topic = IMManager.getInstance(ct).updateTopic(topic);
			
			Chat chat = IMManager.getInstance(ct).addChat(topic);
			
			msg.currentClientId = currentClientId;
			msg.chat = chat;
			msg.msgId = msgData.getMsgId();
			msg.fromClient = msgData.getFrom();
			msg.status = Message.STATUS_SENT;
			msg.type = msgData.getFileType();
			msg.readed = false;
			msg.content = msgData.getContent();
			msg.fileURL = msgData.getCustomData().get("url");
			
			if(msgData.getCustomData()!=null){
				if( msgData.getCustomData().containsKey("name"))
					msg.fromUsername = msgData.getCustomData().get("name");
				if( msgData.getCustomData().containsKey("photoUrl"))
					msg.fromUserIconUrl = msgData.getCustomData().get("photoUrl");
			}
			msg.update();
			
		}

		setChanged();
		notifyObservers(msg);
	}
	
	private void handleOfflineHistory(List<AnIMMessage> data){
		for(int i = data.size()-1;i>=0;i--){
			AnIMMessage msg = data.get(i);
			if( msg.getFileType()==null){
				handleChatMessage(msg);
			}else{
				if(msg.getFileType().equals(Constant.FRIEND_REQUEST_TYPE_SEND)){
					handleFriendRequest(msg.getFrom(),msg.getCustomData().get(Constant.FRIEND_REQUEST_KEY_TYPE));
				}else if(msg.getFileType().equals(Message.TYPE_IMAGE)||msg.getFileType().equals(Message.TYPE_RECORD)){
					handleChatMessage(msg);
				}
			}
		}
	}
	
	private IAnIMCallback imCallback = new IAnIMCallback(){
		@Override
		public void addClientsToTopic(AnIMAddClientsCallbackData data) {
			
		}

		@Override
		public void bindAnPushService(AnIMBindAnPushServiceCallbackData data) {
			
		}

		@Override
		public void createTopic(AnIMCreateTopicCallbackData data) {
			if(data.isError()){
				data.getException().printStackTrace();
			}else{
				try {
					anIM.getTopicInfo(data.getTopic());
				} catch (ArrownockException e) {
					e.printStackTrace();
				}
			}
			notifyTopicUpdated();
		}

		@Override
		public void getClientId(AnIMGetClientIdCallbackData data) {
			
		}

		@Override
		public void getClientsStatus(AnIMGetClientsStatusCallbackData data) {
			
		}

		@Override
		public void getSessionInfo(AnIMGetSessionInfoCallbackData data) {
			
		}

		@Override
		public void getTopicInfo(AnIMGetTopicInfoCallbackData data) {
			Topic topic = new Topic();
			topic.topicId = data.getTopicId();
			topic.topicName = data.getTopicName();
			topic.ownerClientId = data.getOwner();
			for(String clientId :data.getParties()){
				topic.addMember(clientId);
			}
			IMManager.getInstance(ct).updateTopic(topic);
			notifyTopicUpdated();
		}

		@Override
		public void getTopicList(final AnIMGetTopicListCallbackData data) {
			//removeAllTopics();
			getMyLocalTopic(new IMManager.FetchLocalTopicCallback(){
				@Override
				public void onFinish(List<Topic> localTopicList) {
					Set<String> filterTopicSet = new HashSet<String>();
					for(Topic topic : localTopicList){
						filterTopicSet.add(topic.topicId);
					} 
					List<JSONObject> topicList = data.getTopicList();
					if(topicList!=null &&topicList.size()>0){
						for(JSONObject j:topicList){
							Topic topic = new Topic();
							topic.parseJSON(j);
							topic = IMManager.getInstance(ct).updateTopic(topic);
							filterTopicSet.remove(topic.topicId);
							
							try {
								JSONArray parties = j.getJSONArray("parties");
								for(int i =0;i<parties.length();i++){
									String clientId = parties.getString(i);
									if(UserManager.getInstance(ct).getUserByClientId(clientId)==null){
										UserManager.getInstance(ct).fetchUserDataByClientId(clientId);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						notifyTopicUpdated();
					}else{
					}

					for(String topicId : filterTopicSet){
						new Delete().from(Topic.class).where("topicId = ?",topicId).executeSingle();
					}
				}
			});
		}

		@Override
		public void messageSent(AnIMMessageSentCallbackData data) {
			Message msg = new Message();
			msg.msgId = data.getMsgId();
			msg.currentClientId = currentClientId;
			msg.readed = true;

			if(data.isError()){
				data.getException().printStackTrace();
				msg.status = Message.STATUS_FAILED;
			}else{
				msg.status = Message.STATUS_SENT;
			}
			
			msg.update();
			
			
			setChanged();
			notifyObservers(data);
		}

		@Override
		public void receivedBinary(AnIMBinaryCallbackData data) {
			if(data.getFileType()!=null && data.getFileType().equals(Constant.FRIEND_REQUEST_TYPE_SEND)){
				handleFriendRequest(data.getFrom(),data.getCustomData().get(Constant.FRIEND_REQUEST_KEY_TYPE));
			}else if(data.getFileType()!=null &&  (data.getFileType().equals(Message.TYPE_IMAGE) || data.getFileType().equals(Message.TYPE_RECORD))){
				handleChatMessage(data);
			}
			
			setChanged();
			notifyObservers(data);
		}

		@Override
		public void receivedMessage(AnIMMessageCallbackData data) {
			handleChatMessage(data);
			
			setChanged();
			notifyObservers(data);
		}

		@Override
		public void receivedNotice(AnIMNoticeCallbackData data) {
			
		}

		@Override
		public void receivedReadACK(AnIMReadACKCallbackData data) {
			Message msg = new Message();
			msg.msgId = data.getMsgId();
			msg.currentClientId = currentClientId;
			msg.readACK = true;
			msg.update();
			
			setChanged();
			notifyObservers(data);
		}

		@Override
		public void receivedReceiveACK(AnIMReceiveACKCallbackData data) {
			
		}

		@Override
		public void receivedTopicBinary(AnIMTopicBinaryCallbackData data) {
			if(data.getFileType()!=null && (data.getFileType().equals(Message.TYPE_IMAGE) || data.getFileType().equals(Message.TYPE_RECORD))){
				handleChatMessage(data);
			}
			
			setChanged();
			notifyObservers(data);
		}

		@Override
		public void receivedTopicMessage(AnIMTopicMessageCallbackData data) {
			handleChatMessage(data);
			
			setChanged();
			notifyObservers(data);
		}

		@Override
		public void removeClientsFromTopic(AnIMRemoveClientsCallbackData data) {
			
		}

		@Override
		public void removeTopic(AnIMRemoveTopicCallbackData data) {
			
		}

		@Override
		public void statusUpdate(final AnIMStatusUpdateCallbackData data) {
			if (data.getStatus() == AnIMStatus.ONLINE) {
				MyIAnLiveEventListener lsr = new MyIAnLiveEventListener(ct);
				IMppApp app = (IMppApp) ct.getApplicationContext();
				try {
					app.anLive = AnLive.initialize(ct, getAnIM(),lsr);
				} catch (Exception e) {
					app.anLive = null;
				}
				
				try {
					anIM.bindAnPushService(app.anPush.getAnID(), ct.getString(R.string.app_key), currentClientId);
				} catch (ArrownockException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				anIM.getOfflineHistory(currentClientId, 100, new IAnIMHistoryCallback() {
					@Override
					public void onError(ArrownockException data) {
					}

					@Override
					public void onSuccess(List<AnIMMessage> data, int count) {
						handleOfflineHistory(data);
						if (count > 0) {
							anIM.getOfflineHistory(currentClientId, 100, this);
						}
					}
				});
				anIM.getOfflineTopicHistory(currentClientId, 100, new IAnIMHistoryCallback() {
					@Override
					public void onError(ArrownockException data) {
					}

					@Override
					public void onSuccess(List<AnIMMessage> data, int count) {
						handleOfflineHistory(data);
						if (count > 0) {
							anIM.getOfflineTopicHistory(currentClientId, 100, this);
						}
					}
				});
	        }else if (data.getStatus() == AnIMStatus.OFFLINE) {
	        	if(data.getException()!=null){
					data.getException().printStackTrace();
	        		if (data.getException().getErrorCode() == ArrownockException.IM_FORCE_CLOSED
							|| data.getException().getErrorCode() == ArrownockException.IM_FAILED_DISCONNECT) {	
	        			handler.post(new Runnable(){
							@Override
							public void run() {
								Toast.makeText(ct, data.getException().getMessage(), Toast.LENGTH_LONG).show();
							}
	        			});
					}else{
						handler.postDelayed(new Runnable(){
							@Override
							public void run() {
								connect(currentClientId);
							}
						}, RECONNECT_RATE);
					}
				}else{
					
				}
	        }
			
		}

		@Override
		public void unbindAnPushService(AnIMUnbindAnPushServiceCallbackData data) {
			
		}

		@Override
		public void updateTopic(AnIMUpdateTopicCallbackData data) {
		}
	};
}
