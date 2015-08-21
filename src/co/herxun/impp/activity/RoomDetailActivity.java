package co.herxun.impp.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.controller.RoomManager;
import co.herxun.impp.controller.RoomManager.AddTopicMembersCallback;
import co.herxun.impp.controller.RoomManager.LikeCallback;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.AddTopicCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.Post;
import co.herxun.impp.model.Room;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.RoomWallView;

import com.arrownock.exception.ArrownockException;

public class RoomDetailActivity extends BaseActivity {
    private AppBar appbar;
    private RoomWallView mWallView;
    private FrameLayout header;
    private ImageView addBtn;
    private Room room;
    private Button joinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        checkBundle();
        initView();
        initData();
    }

    private void initView() {
        appbar = (AppBar) findViewById(R.id.wall_app_bar);
        appbar.getLogoView().setImageResource(R.drawable.menu_back);
        appbar.getLogoView().setLayoutParams(
                new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56), Utils.px2Dp(this, 56)));
        appbar.getLogoView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        appbar.getTextView().setVisibility(View.VISIBLE);
        appbar.getTextView().setText(R.string.wall_room);
        appbar.getMenuItemView().setImageResource(R.drawable.menu_chat);

        mWallView = (RoomWallView) findViewById(R.id.wall_wallView);
        header = new FrameLayout(this);
        LayoutInflater.from(this).inflate(R.layout.view_room_header, header);
        // header.setLayoutParams(new AbsListView.LayoutParams(-1,
        // Utils.px2Dp(this, 300)));
        mWallView.setHeaderView(header);

        addBtn = (ImageView) findViewById(R.id.wall_addBtn);
        addBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CreateRoomPostActivity.class);
                intent.putExtra(Constant.INTENT_EXTRA_KEY_ROOM, room.roomId);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            }
        });

        joinBtn = (Button) findViewById(R.id.btn_room_join);
    }

    private void checkBundle() {
        if (getIntent().hasExtra(Constant.INTENT_EXTRA_KEY_ROOM)) {
            String roomId = getIntent().getStringExtra(Constant.INTENT_EXTRA_KEY_ROOM);
            RoomManager roomManager = new RoomManager(RoomDetailActivity.this);
            room = roomManager.getLocalRoom(roomId);
        }
    }

    private void initData() {
        TextView textUserName = (TextView) header.findViewById(R.id.view_wall_header_text);
        TextView textDescription = (TextView) header.findViewById(R.id.room_description);
        ImageView imgUserIcon = (ImageView) header.findViewById(R.id.view_wall_header_icon);
        final User me = UserManager.getInstance(this).getCurrentUser();
        textUserName.setText(room.name);
        if (room.description != null && room.description.length() > 0) {
            textDescription.setText(room.description);
        }

        appbar.getMenuItemView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Topic topic = new Topic();
                topic.topicId = room.topicId;
                Topic t = topic.getFromTable();
                Chat chat = IMManager.getInstance(RoomDetailActivity.this).addChat(t);
                IMManager.getInstance(RoomDetailActivity.this).notifyChatUpdated();
                Intent i = new Intent(RoomDetailActivity.this, ChatActivity.class);
                Bundle b = new Bundle();
                b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
                i.putExtras(b);
                startActivity(i);
            }
        });

        ImageLoader.getInstance(this).DisplayImage(room.photoUrl, imgUserIcon, R.drawable.room_default, true);
        final RoomManager roomManager = new RoomManager(RoomDetailActivity.this);
        // 退出房间
        if (room.usersIds.indexOf(me.userId) != -1) {
            addBtn.setVisibility(View.VISIBLE);
            mWallView.setRoomWallManager(roomManager, room.roomId, true);
            appbar.getMenuItemView().setVisibility(View.VISIBLE);
            joinBtn.setText(getString(R.string.room_header_quit_btn));
            joinBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoading();
                    Set<String> client = new HashSet<String>();
                    client.add(me.clientId);
                    Topic topic = new Topic();
                    topic.topicId = room.topicId;
                    final Topic t = topic.getFromTable();
                    IMManager.getInstance(RoomDetailActivity.this).removeTopicMembers(client, t,
                            new AddTopicCallback() {

                                @Override
                                public void onFinish(String topicId) {
                                    roomManager.removeTopicMembers(room.roomId, me.userId,
                                            new AddTopicMembersCallback() {

                                                @Override
                                                public void onFinish(String roomId) {
                                                    t.delete();
                                                    finish();
                                                    dismissLoading();
                                                }

                                                @Override
                                                public void onFailure(final String errorMsg) {
                                                    runOnUiThread(new Runnable() {
                                                        public void run() {
                                                            dismissLoading();
                                                            Toast.makeText(getBaseContext(), errorMsg,
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
                                            });

                                }

                                @Override
                                public void onError(final ArrownockException error) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            dismissLoading();
                                            Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    });
                                }
                            });
                }
            });
        } else {
            // 加入房间
            addBtn.setVisibility(View.GONE);
            mWallView.setRoomWallManager(roomManager, room.roomId, false);
            appbar.getMenuItemView().setVisibility(View.GONE);
            joinBtn.setText(getString(R.string.room_header_join_btn));
            joinBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLoading();
                    Set<String> client = new HashSet<String>();
                    client.add(me.clientId);
                    IMManager.getInstance(RoomDetailActivity.this).addTopicMembers(client, room.topicId,
                            new AddTopicCallback() {

                                @Override
                                public void onFinish(String topicId) {
                                    roomManager.addTopicMembers(room.roomId, me.userId, new AddTopicMembersCallback() {

                                        @Override
                                        public void onFinish(String roomId) {
                                            initData();
                                        }

                                        @Override
                                        public void onFailure(final String errorMsg) {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    dismissLoading();
                                                    Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            });
                                        }
                                    });
                                }

                                @Override
                                public void onError(final ArrownockException error) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            dismissLoading();
                                            Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    });
                                }
                            });
                }
            });
        }

        roomManager.setOnLikeListener(new LikeCallback() {
            public void onFailure(Post post) {
            }

            @Override
            public void onSuccess(Post post) {
                try {
                    Map<String, String> cData = new HashMap<String, String>();
//                    cData.put(Constant.FRIEND_REQUEST_KEY_TYPE, Message.TYPE_LIKE);
                    cData.put("notification_alert",
                            UserManager.getInstance(RoomDetailActivity.this).getCurrentUser().userName + " 對你在 "
                                    + room.name + " 的貼文按讚");
                    IMManager.getInstance(RoomDetailActivity.this).getAnIM()
                            .sendBinary(post.owner.clientId, new byte[1], Constant.FRIEND_REQUEST_TYPE_SEND, cData);
                } catch (ArrownockException e) {
                    e.printStackTrace();
                }
            }
        });
        dismissLoading();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            initData();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
