package co.herxun.impp.fragment;

import java.util.Observable;
import java.util.Observer;

import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import co.herxun.impp.R;
import co.herxun.impp.activity.ChatActivity;
import co.herxun.impp.activity.CreateTopicActivity;
import co.herxun.impp.activity.SearchUserActivity;
import co.herxun.impp.adapter.ChatListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.GetUnReadedMessageCountCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.im.view.ChatView;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;

public class ChatListFragment extends BaseFragment{
	private ListView mListView;
	private ChatListAdapter mChatListAdapter;
	private Dialog mActionDialog;
	
	public ChatListFragment(String title) {
		super(title);
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewShown(){
		if(mChatListAdapter!=null){
			mChatListAdapter.fillLocalData();
		}
		//checkBadge();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        return rootView;
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		
		initView();
		checkBadge();
	}
	
	private void initView(){
		mListView = (ListView) getActivity().findViewById(R.id.listView);
		mChatListAdapter = new ChatListAdapter(getActivity());
		mListView.setAdapter(mChatListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Chat chat = mChatListAdapter.getItem(position);
				Context ct = parent.getContext();
				if(chat.topic!=null){
					Intent i = new Intent(ct,ChatActivity.class);
					Bundle b = new Bundle();
					b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
					i.putExtras(b);
					ct.startActivity(i);
					getActivity().overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
				}else {
					if(chat.targetClientId!=null){
						Intent i = new Intent(ct,ChatActivity.class);
						Bundle b = new Bundle();
						b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
						i.putExtras(b);
						ct.startActivity(i);
						getActivity().overridePendingTransition(R.anim.slide_in_right,android.R.anim.fade_out);
					}
				}
			}
		});
		
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
				AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(getActivity());
				dialogBuiler.setTitle(R.string.chat_delete_chat_confirm);
				dialogBuiler.setPositiveButton(getActivity().getString(R.string.general_ok), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mActionDialog.dismiss();
						IMManager.getInstance(getActivity()).deleteChat(mChatListAdapter.getItem(position));
						mChatListAdapter.removeItem(position);
						checkBadge();
					}});
				dialogBuiler.setNegativeButton(getActivity().getString(R.string.general_cancel), new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mActionDialog.dismiss();
					}});
				mActionDialog = dialogBuiler.create();
				mActionDialog.show();
				return false;
			}
		});
	}

	public void filterList(String text){
		mChatListAdapter.filter(text);
	}
	
	private void checkBadge(){
		IMManager.getInstance(getActivity()).getUnReadMessageCount(new GetUnReadedMessageCountCallback(){
			@Override
			public void onFinish(int count) {
				setBadgeCount(count);
			}
		});
	}
	
	public void update(Observable observable, Object data) {
		if(data instanceof Message){
		}else if(data instanceof IMManager.UpdateType && ((IMManager.UpdateType)data).equals(IMManager.UpdateType.Topic)){
			
		}
		if(mChatListAdapter!=null){
			mChatListAdapter.fillLocalData();
		}
		
	}
}
