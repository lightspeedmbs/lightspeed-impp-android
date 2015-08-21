package co.herxun.impp.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.adapter.FragmentPagerAdapter;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.AddFriendCallback;
import co.herxun.impp.controller.UserManager.FetchSingleUserCallback;
import co.herxun.impp.fragment.BaseFragment;
import co.herxun.impp.fragment.ChatListFragment;
import co.herxun.impp.fragment.ExploreFragment;
import co.herxun.impp.fragment.FriendListFragment;
import co.herxun.impp.fragment.SettingFragment;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.controller.IMManager.GetUnReadedMessageCountCallback;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.im.model.Topic;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;
import co.herxun.impp.view.SlidingTabLayout;

public class MainActivity extends BaseActivity implements Observer {
    private SlidingTabLayout mSlidingTabLayout;
    public ViewPager mViewPager;
    private AppBar mAppbar;

    private ChatListFragment mChatListFragment;
    private FriendListFragment mFriendListFragment;
    private ExploreFragment mExploreFragment;
    private SettingFragment mSettingFragment;

    private List<BaseFragment> fragList;

    private boolean doubleBackToExistPressedOnce = false;

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        public void onPageScrollStateChanged(int arg0) {
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int location) {
            BaseFragment frag = fragList.get(location);
            frag.onViewShown();

            mAppbar.initLayout();
            mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
            if (frag instanceof ChatListFragment) {
                onChatListFragmentShown((ChatListFragment) frag);
            } else if (frag instanceof FriendListFragment) {
                onFriendListFragmentShown((FriendListFragment) frag);
            } else if (frag instanceof ExploreFragment) {
                mAppbar.getMenuItemView().setVisibility(View.GONE);
            } else if (frag instanceof SettingFragment) {
                mAppbar.getMenuItemView().setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMManager.getInstance(this).addObserver(this);
        setContentView(R.layout.activity_main);
        checkBundle();
        initView();

        new Thread(new Runnable() {
            public void run() {
                User user = UserManager.getInstance(MainActivity.this).getCurrentUser();
                addDefaultFriend(user);
            }
        }).start();

    }

    private void addDefaultFriend(final User user) {
        // 自动添加默认好友
        final String defaultFriendId = getString(R.string.default_friend_id);
        if (defaultFriendId == null || defaultFriendId.trim().isEmpty()) {
            return;
        }
        User defaultFriend = UserManager.getInstance(this).getUserByUserId(defaultFriendId);
        if (defaultFriend != null && user.isFriend(defaultFriend.clientId)) {
            Log.e("defaultFriend", "default friend is exist.");
            return;
        }
        UserManager.getInstance(this).fetchSIngleUserDataByUserId(defaultFriendId, new FetchSingleUserCallback() {

            @Override
            public void onFinish(final User toUser) {
                if (toUser == null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Log.e("fetchSIngleUserDataByUserId default friend", "default friend doesn't exists");
                        }
                    });
                } else {
                    UserManager.getInstance(MainActivity.this).addFriend(user, toUser, new AddFriendCallback() {

                        @Override
                        public void onFinish(boolean isOK) {
                            if (isOK) {
                                Log.e("fetchSIngleUserDataByUserId default friend", "add default friend ok.");
                                Message msg = new Message();
                                Chat chat = IMManager.getInstance(MainActivity.this).addChat(toUser.clientId);
                                msg.currentClientId = user.clientId;
                                msg.chat = chat;
								msg.message = getString(R.string.welcome_message_from_default_user) + "😊";
                                msg.msgId = IMManager.WELCOME_MESSAGE_ID;
                                msg.fromClient = toUser.clientId;
                                msg.status = Message.STATUS_SENT;
                                msg.type = Message.TYPE_TEXT;
                                msg.readed = false;
                                msg.update();
                                IMManager.getInstance(MainActivity.this).setChanged();
                                IMManager.getInstance(MainActivity.this).notifyObservers(msg);
                            } else {
                                Log.e("fetchSIngleUserDataByUserId default friend", "add default friend failed.");
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkBundle();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(mViewPager.getCurrentItem());
        }
        updateTabBadge();
    }

    private void checkBundle() {
        if (getIntent().hasExtra("payload")) {
            String payload = getIntent().getStringExtra("payload");
            String alert = null;

            Chat chat = null;
            try {
                JSONObject json = new JSONObject(payload);
                alert = json.getJSONObject("android").getString("alert");
                if (json.has("topic_id")) {
                    Topic topic = new Topic();
                    topic.topicId = json.getString("topic_id");
                    topic = topic.getFromTable();
                    chat = IMManager.getInstance(this).addChat(topic);
                } else if (json.has("from")) {
                    chat = IMManager.getInstance(this).addChat(json.getString("from"));
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (alert != null
                    && (alert.contains(getString(R.string.anlive_push_call)) || alert
                            .contains(getString(R.string.anlive_push_video_call)))) {
                DBug.e("###push", alert + "?");
            } else {
                if (chat != null) {
                    Intent i = new Intent(this, ChatActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable(Constant.INTENT_EXTRA_KEY_CHAT, chat);
                    i.putExtras(b);
                    startActivity(i);
                }
            }
        }
    }

    private void initView() {
        mChatListFragment = new ChatListFragment(getString(R.string.tab_title_chat));
        mFriendListFragment = new FriendListFragment(getString(R.string.tab_title_friend));
        mExploreFragment = new ExploreFragment(getString(R.string.tab_title_explore));
        mSettingFragment = new SettingFragment(getString(R.string.tab_title_setting));

        mAppbar = (AppBar) findViewById(R.id.toolbar);
        mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        fragList = new ArrayList<BaseFragment>();
        fragList.add(mExploreFragment);
        fragList.add(mChatListFragment);
        fragList.add(mFriendListFragment);
        fragList.add(mSettingFragment);
        FragmentPagerAdapter mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), fragList);
        mViewPager.setAdapter(mFragmentPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.no13);
            }
        });
        mSlidingTabLayout.setOnPageChangeListener(mOnPageChangeListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnPageChangeListener.onPageSelected(0);
            }
        }, 300);
    }

    private void onChatListFragmentShown(final ChatListFragment frag) {
        mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
        mAppbar.getMenuItemView().setImageResource(R.drawable.menu_search);
        mAppbar.getMenuItemView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppbar.getMenuItemView().setVisibility(View.GONE);
                mAppbar.getLogoView().setImageResource(R.drawable.menu_back);
                RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
                rlpLogo.width = Utils.px2Dp(v.getContext(), 56);
                rlpLogo.height = Utils.px2Dp(v.getContext(), 56);
                rlpLogo.leftMargin = 0;
                mAppbar.getLogoView().setLayoutParams(rlpLogo);
                mAppbar.getLogoView().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
                        rlpLogo.width = Utils.px2Dp(v.getContext(), 54);
                        rlpLogo.height = Utils.px2Dp(v.getContext(), 26);
                        rlpLogo.leftMargin = Utils.px2Dp(v.getContext(), 16);
                        mAppbar.getLogoView().setLayoutParams(rlpLogo);
                        mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
                        mAppbar.getEditText().setVisibility(View.GONE);
                        mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
                        mAppbar.getEditText().setText("");
                    }
                });
                mAppbar.getEditText().setVisibility(View.VISIBLE);
                mAppbar.getEditText().requestFocus();
                mAppbar.getEditText().setHint(R.string.friend_list_search);
                mAppbar.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                        frag.filterList(cs.toString());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    public void afterTextChanged(Editable arg0) {
                    }
                });
            }
        });
    }

    private void onFriendListFragmentShown(final FriendListFragment frag) {
        mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
        mAppbar.getMenuItemView().setImageResource(R.drawable.menu_search);
        mAppbar.getMenuItemView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppbar.getMenuItemView().setVisibility(View.GONE);
                mAppbar.getLogoView().setImageResource(R.drawable.menu_back);
                RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
                rlpLogo.width = Utils.px2Dp(v.getContext(), 56);
                rlpLogo.height = Utils.px2Dp(v.getContext(), 56);
                rlpLogo.leftMargin = 0;
                mAppbar.getLogoView().setLayoutParams(rlpLogo);
                mAppbar.getLogoView().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RelativeLayout.LayoutParams rlpLogo = (LayoutParams) mAppbar.getLogoView().getLayoutParams();
                        rlpLogo.width = Utils.px2Dp(v.getContext(), 54);
                        rlpLogo.height = Utils.px2Dp(v.getContext(), 26);
                        rlpLogo.leftMargin = Utils.px2Dp(v.getContext(), 16);
                        mAppbar.getLogoView().setLayoutParams(rlpLogo);
                        mAppbar.getLogoView().setImageResource(R.drawable.menu_logo);
                        mAppbar.getEditText().setVisibility(View.GONE);
                        mAppbar.getMenuItemView().setVisibility(View.VISIBLE);
                        mAppbar.getEditText().setText("");
                    }
                });
                mAppbar.getEditText().setVisibility(View.VISIBLE);
                mAppbar.getEditText().requestFocus();
                mAppbar.getEditText().setHint(R.string.friend_list_search);
                mAppbar.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                        frag.filterList(cs.toString());
                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    }

                    public void afterTextChanged(Editable arg0) {
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                doubleBackToExistPressedOnce = false;
            }
        };
        if (!doubleBackToExistPressedOnce) {
            doubleBackToExistPressedOnce = true;
            Toast.makeText(this, getString(R.string.general_press_again_to_exit), Toast.LENGTH_SHORT).show();
            h.postDelayed(r, 2000);
        } else {
            h.removeCallbacks(r);
            super.onBackPressed();
        }
    }

    public void updateTabBadge() {
        mExploreFragment.setBadgeCount(mExploreFragment.getLikeCount());
        IMManager.getInstance(this).getUnReadMessageCount(new GetUnReadedMessageCountCallback() {
            @Override
            public void onFinish(int count) {
                mChatListFragment.setBadgeCount(count);
            }
        });
    }

    @Override
    public void update(final Observable observable, final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (data instanceof Message) {
                    Message msgData = (Message) data;
                    if (!msgData.readed) {
                        int badgeCount = mChatListFragment.getBadgeCount();
                        mChatListFragment.setBadgeCount(++badgeCount);
                    }
                    mChatListFragment.update(observable, data);

                } else if (data instanceof IMManager.UpdateType
                        && ((IMManager.UpdateType) data).equals(IMManager.UpdateType.Topic)) {
                    mChatListFragment.update(observable, data);

                } else if (data instanceof IMManager.UpdateType
                        && ((IMManager.UpdateType) data).equals(IMManager.UpdateType.Like)) {
                    mExploreFragment.notifyLike();
                    mExploreFragment.setBadgeCount(mExploreFragment.getLikeCount());
                    DBug.e("mExploreFragment.getLikeCount()", mExploreFragment.getLikeCount() + "?");
                    DBug.e("mExploreFragment.badgeCount", mExploreFragment.getBadgeCount() + "?");
                }
                mSlidingTabLayout.refreshAllTab();
            }
        });
    }
}
