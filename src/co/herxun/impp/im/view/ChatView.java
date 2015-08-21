package co.herxun.impp.im.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.adapter.MessageListAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.GetMessageCallback;
import co.herxun.impp.im.controller.VoiceHelper;
import co.herxun.impp.im.controller.VoiceHelper.VoiceRecordCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.ChatUser;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;

import com.arrownock.im.callback.AnIMMessageSentCallbackData;
import com.arrownock.im.callback.AnIMReadACKCallbackData;

public class ChatView extends RelativeLayout implements Observer {
    private Context ct;
    private ListView historyListView;
    private EditText inputEt;
    private TextView sendBtn, cancelBtn;
    public ImageView imgAttachment;
    private IMManager sIMManger;
    private MessageListAdapter mMessageListAdapter;
    private LinearLayout recordBox, inputBox;
    private TextView btnRecord;
    private RecordingView recordingView;
    private VoiceHelper sVoiceHelper;

    private Chat mChat;
    private ChatUser myUser;

    public ChatView(Context context) {
        super(context);
        init(context);
    }

    public ChatView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public ChatView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setChat(ChatUser myUser, Chat chat) {
        mChat = chat;
        this.myUser = myUser;

        mMessageListAdapter = new MessageListAdapter(getContext(), mChat);
        historyListView.setAdapter(mMessageListAdapter);
        historyListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mMessageListAdapter.fillLocalData(new GetMessageCallback() {
            @Override
            public void onFinish(List<Message> data) {
                historyListView.setSelection(mMessageListAdapter.getCount() - 1);

                for (Message msg : data) {
                    if (msg.readed) {
                        continue;
                    }
                    if(msg != null) {
                    	boolean sendACK = false;
                    	if(mChat.topic == null && !IMManager.WELCOME_MESSAGE_ID.equals(msg.msgId)) {
                    		sendACK = true;
                    	}
                    	IMManager.getInstance(ct).setMessageReaded(msg, sendACK);
                    }
                }
            }
        });
    }

    public void setUserMap(Map<String, ChatUser> userMap) {
        mMessageListAdapter.setUserMap(userMap);
    }

    private void init(Context ct) {
        this.ct = ct;
        sVoiceHelper = VoiceHelper.getInstance(ct);

        sIMManger = IMManager.getInstance(ct);
        sIMManger.addObserver(this);

        generateView();
        setFunction();
    }

    private void generateView() {
        int viewId = 1;

        this.setBackgroundColor(ct.getResources().getColor(R.color.no5));

        FrameLayout viewInput = new FrameLayout(ct);
        viewInput.setId(viewId++);
        RelativeLayout.LayoutParams lpInputBox = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        lpInputBox.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        viewInput.setLayoutParams(lpInputBox);
        addView(viewInput);

        inputBox = new LinearLayout(ct);
        inputBox.setOrientation(LinearLayout.HORIZONTAL);
        inputBox.setBackgroundColor(ct.getResources().getColor(R.color.no6));
        viewInput.addView(inputBox);

        imgAttachment = new ImageView(ct);
        imgAttachment.setImageResource(R.drawable.compose_bu);
        LinearLayout.LayoutParams lpAtt = new LinearLayout.LayoutParams(Utils.px2Dp(ct, 48), Utils.px2Dp(ct, 48));
        lpAtt.gravity = Gravity.CENTER_VERTICAL;
        inputBox.addView(imgAttachment, lpAtt);

        inputEt = new EditText(ct);
        inputEt.setBackgroundColor(Color.TRANSPARENT);
        inputEt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        inputEt.setHint(R.string.setting_send_msg);
        inputEt.setHintTextColor(ct.getResources().getColor(R.color.no8));
        inputEt.setTextColor(ct.getResources().getColor(R.color.no11));
        inputEt.setMaxHeight(Utils.px2Dp(ct, 76));
        inputEt.setPadding(0, Utils.px2Dp(ct, 12), 0, Utils.px2Dp(ct, 12));
        LinearLayout.LayoutParams lpInputEt = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
        lpInputEt.weight = 1;
        lpInputEt.gravity = Gravity.CENTER_VERTICAL;
        inputBox.addView(inputEt, lpInputEt);

        sendBtn = new TextView(ct);
        sendBtn.setGravity(Gravity.CENTER_VERTICAL);
        sendBtn.setText(R.string.chat_send);
        sendBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        sendBtn.setPadding(Utils.px2Dp(ct, 16), 0, Utils.px2Dp(ct, 16), 0);
        sendBtn.setTextColor(ct.getResources().getColor(R.color.no2));
        inputBox.addView(sendBtn, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

        FrameLayout viewDisplay = new FrameLayout(ct);
        RelativeLayout.LayoutParams lpViewDisplay = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        lpViewDisplay.addRule(RelativeLayout.ABOVE, viewInput.getId());
        addView(viewDisplay, lpViewDisplay);

        historyListView = new ListView(ct);
        historyListView.setBackgroundColor(0x00ffffff);
        viewDisplay.addView(historyListView, new LayoutParams(-1, -1));

        View topMargin = new View(ct);
        topMargin.setLayoutParams(new AbsListView.LayoutParams(Utils.px2Dp(ct, -1), Utils.px2Dp(ct, 16)));
        historyListView.addHeaderView(topMargin);

        recordingView = new RecordingView(ct);
        viewDisplay.addView(recordingView, new LayoutParams(-1, -1));
        recordingView.setVisibility(View.GONE);

        recordBox = new LinearLayout(ct);
        recordBox.setOrientation(LinearLayout.HORIZONTAL);
        recordBox.setBackgroundColor(ct.getResources().getColor(R.color.no6));
        viewInput.addView(recordBox, new LayoutParams(-1, Utils.px2Dp(ct, 48)));

        btnRecord = new TextView(ct);
        btnRecord.setGravity(Gravity.CENTER);
        btnRecord.setTextColor(ct.getResources().getColor(R.color.no5));
        btnRecord.setText(R.string.chat_record_press);
        LinearLayout.LayoutParams lpInputBtRe = new LinearLayout.LayoutParams(0, Utils.px2Dp(ct, 32));
        lpInputBtRe.weight = 1;
        lpInputBtRe.setMargins(Utils.px2Dp(ct, 16), 0, 0, 0);
        lpInputBtRe.gravity = Gravity.CENTER_VERTICAL;
        recordBox.addView(btnRecord, lpInputBtRe);

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(ct.getResources().getColor(R.color.no2));
        float corner = Utils.px2Dp(ct, 2);
        gd.setCornerRadii(new float[] { corner, corner, corner, corner, corner, corner, corner, corner });
        btnRecord.setBackgroundDrawable(gd);

        cancelBtn = new TextView(ct);
        cancelBtn.setGravity(Gravity.CENTER_VERTICAL);
        cancelBtn.setText(R.string.chat_record_cancel);
        cancelBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        cancelBtn.setPadding(Utils.px2Dp(ct, 16), 0, Utils.px2Dp(ct, 16), 0);
        cancelBtn.setTextColor(ct.getResources().getColor(R.color.no2));
        recordBox.addView(cancelBtn,
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

    }

    private void setFunction() {
        showRecordBox(false);

        sendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = inputEt.getText().toString();
                if (text != null && text.length() > 0) {
                    Message msg = new Message();
                    msg.message = inputEt.getText().toString();
                    msg.type = Message.TYPE_TEXT;
                    sendMessage(msg);

                    inputEt.setText("");
                }
            }
        });

        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordBox(false);
                recordingView.setVisibility(View.GONE);
            }
        });

        btnRecord.setOnTouchListener(new OnTouchListener() {
            private Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());

                    if (!sVoiceHelper.isRecording()) {
                        recordingView.show();
                        sVoiceHelper.startRecord();
                        btnRecord.setText(R.string.chat_record_release_to_cancel);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        recordingView.setReleaseToCancel();
                    } else {
                        recordingView.setRecording();
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (sVoiceHelper.isRecording()) {

                        btnRecord.setText(R.string.chat_record_press);

                        DBug.e("mRecordTimer", sVoiceHelper.mRecordTimer.getTime() + "");
                        if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                            recordingView.hide();
                            sVoiceHelper.cancelRecord();
                        } else if (sVoiceHelper.mRecordTimer.getTime() < 1000) {
                            sVoiceHelper.cancelRecord();
                            recordingView.setRecordFailed();
                        } else {
                            recordingView.hide();
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sVoiceHelper.stopRecord();
                                }
                            }, 500);
                        }
                    }
                    break;
                }
                return true;
            }
        });

        sVoiceHelper.setRecordCallback(new VoiceRecordCallback() {
            public void onCancel() {
            }

            public void onFailed() {
            }

            public void onSuccess(File file) {
                // sVoiceHelper.playVoice();

                int size = (int) file.length();
                byte[] fileToSend = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(fileToSend, 0, fileToSend.length);
                    buf.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Message msg = new Message();
                msg.type = Message.TYPE_RECORD;
                msg.content = fileToSend;
                sendMessage(msg);
            }
        });

    }

    public void showRecordBox(boolean show) {
        if (show) {
            recordBox.setVisibility(View.VISIBLE);
            inputBox.setVisibility(View.GONE);
            InputMethodManager imm = (InputMethodManager) ct.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(inputEt.getWindowToken(), 0);
        } else {
            recordBox.setVisibility(View.GONE);
            inputBox.setVisibility(View.VISIBLE);
        }
    }

    public Message sendMessage(Message message) {
        message.chat = mChat;
        Message msg = sIMManger.sendMessage(myUser, message);
        return msg;
    }

    public void appendMessage(Message msg) {
        final int index = mMessageListAdapter.addMessage(msg);
        historyListView.smoothScrollToPosition(index + 1);
    }

    @Override
    public void update(Observable observable, final Object data) {
        if (!isShown()) {
            return;
        }
        if (data instanceof Message) {
            final Message msg = (Message) data;
            DBug.e("msg.chat", msg.chat.topic + "," + msg.chat.targetClientId);
            DBug.e("mChat", mChat.topic + "," + mChat.targetClientId);
            if ((msg.chat.topic != null && mChat.topic != null && msg.chat.topic.topicId.equals(mChat.topic.topicId))
                    || (msg.chat.targetClientId != null && mChat.targetClientId != null && msg.chat.targetClientId
                            .equals(mChat.targetClientId))) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        User u = UserManager.getInstance(ct).getUserByClientId(msg.fromClient);
                        Map<String, ChatUser> map = mMessageListAdapter.getUserMap();
                        ChatUser chatUser = map.get(msg.fromClient);
                        if (chatUser != null) {
                            chatUser.setIconUrl(u.userPhotoUrl);
                        } else {
                            chatUser = new ChatUser(u.clientId, u.userName, u.userPhotoUrl);
                            map.put(u.clientId, chatUser);
                        }
                        mMessageListAdapter.setUserMap(map);
                        
                        appendMessage(msg);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DBug.e("****newMessage", msg.readed + "?");
                                boolean sendReadAck = mChat.topic == null && !msg.readed;
                                if (msg != null) {
                                	if(IMManager.WELCOME_MESSAGE_ID.equals(msg.msgId)) {
                                		sendReadAck = false;
                                	}
                                	IMManager.getInstance(ct).setMessageReaded(msg, sendReadAck);
                                }
                            }
                        }).start();
                    }
                });
            }
        } else if (data instanceof AnIMMessageSentCallbackData) {
            post(new Runnable() {
                @Override
                public void run() {
                    AnIMMessageSentCallbackData msgData = (AnIMMessageSentCallbackData) data;

                    if (msgData.isError()) {
                        mMessageListAdapter.updateMessageStatus(msgData.getMsgId(), Message.STATUS_FAILED);
                    } else {
                        mMessageListAdapter.updateMessageStatus(msgData.getMsgId(), Message.STATUS_SENT);
                    }
                }
            });

        } else if (data instanceof AnIMReadACKCallbackData) {
            post(new Runnable() {
                @Override
                public void run() {
                    AnIMReadACKCallbackData msgData = (AnIMReadACKCallbackData) data;

                    mMessageListAdapter.setMessageReadAck(msgData.getMsgId());
                }
            });

        }
    }
}
