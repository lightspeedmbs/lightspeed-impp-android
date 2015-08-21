package co.herxun.impp.fragment;

import java.util.Observable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import co.herxun.impp.R;
import co.herxun.impp.activity.ChatActivity;
import co.herxun.impp.adapter.ChatListAdapter;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.GetUnReadedMessageCountCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.view.AppBar;

public class ChatListFragment extends BaseFragment {
    private ListView mListView;
    private ChatListAdapter mChatListAdapter;
    private Dialog mActionDialog;
    private boolean isLongClick = false;
    private TextView noChatLabel;
    
    public ChatListFragment() {
    	this("");
    }
    
    public ChatListFragment(String title) {
        super(title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewShown() {
        if (mChatListAdapter != null) {
            mChatListAdapter.fillLocalData();
        }
        // checkBadge();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        checkBadge();
    }

    private void initView() {
    	noChatLabel = (TextView)getActivity().findViewById(R.id.noChatLabel);
        mListView = (ListView) getActivity().findViewById(R.id.listView);
        mChatListAdapter = new ChatListAdapter(getActivity());
        mListView.setAdapter(mChatListAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isLongClick) {
                    Chat chat = mChatListAdapter.getItem(position);
                    Context ct = parent.getContext();
                    if (chat.topic != null) {
                        Intent i = new Intent(ct, ChatActivity.class);
                        Bundle b = new Bundle();
                        b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
                        i.putExtras(b);
                        ct.startActivity(i);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    } else {
                        if (chat.targetClientId != null) {
                            Intent i = new Intent(ct, ChatActivity.class);
                            Bundle b = new Bundle();
                            b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
                            i.putExtras(b);
                            ct.startActivity(i);
                            getActivity().overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                        }
                    }
                } else {
                    isLongClick = false;
                }
            }
        });

        mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                isLongClick = true;
                AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(getActivity());
                dialogBuiler.setTitle(R.string.chat_delete_chat_confirm);
                dialogBuiler.setPositiveButton(getActivity().getString(R.string.general_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mActionDialog.dismiss();
                                IMManager.getInstance(getActivity()).deleteChat(mChatListAdapter.getItem(position));
                                mChatListAdapter.removeChatFromChatList(mChatListAdapter.getItem(position));
                                mChatListAdapter.removeItem(position);
                                checkBadge();
                                isLongClick = false;
                            }
                        });
                dialogBuiler.setNegativeButton(getActivity().getString(R.string.general_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mActionDialog.dismiss();
                                isLongClick = false;
                            }
                        });
                mActionDialog = dialogBuiler.create();
                mActionDialog.show();
                return false;
            }
        });
        
        mListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        	@Override
        	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        		if(mChatListAdapter.getCount() > 0) {
        			noChatLabel.setVisibility(View.GONE);
        		} else {
        			boolean isSearching = false;
        			AppBar bar = (AppBar) getActivity().findViewById(R.id.toolbar);
        			if(bar != null && bar.getEditText() != null
        							&& bar.getEditText().getText() != null 
        							&& !"".equals(bar.getEditText().getText().toString())) {
        					isSearching = true;
        			} 
        			if(isSearching) {
        				noChatLabel.setVisibility(View.GONE);
        			} else {
        				noChatLabel.setVisibility(View.VISIBLE);
        			}
        		}
        	}
        });
	}

    public void filterList(String text) {
        mChatListAdapter.filter(text);
    }

    private void checkBadge() {
        IMManager.getInstance(getActivity()).getUnReadMessageCount(new GetUnReadedMessageCountCallback() {
            @Override
            public void onFinish(int count) {
                setBadgeCount(count);
            }
        });
    }

    public void update(Observable observable, Object data) {
        if (data instanceof Message) {
        } else if (data instanceof IMManager.UpdateType
                && ((IMManager.UpdateType) data).equals(IMManager.UpdateType.Topic)) {

        }
        if (mChatListAdapter != null) {
            mChatListAdapter.fillLocalData();
        }
    }
}
