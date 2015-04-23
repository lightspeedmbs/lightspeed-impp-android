package co.herxun.impp.activity;

import java.util.Date;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import co.herxun.impp.IMppApp;
import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Utils;

import com.arrownock.exception.ArrownockException;
import com.arrownock.live.AudioState;
import com.arrownock.live.IAnLiveEventListener;
import com.arrownock.live.LocalVideoView;
import com.arrownock.live.VideoState;
import com.arrownock.live.VideoView;

public class VideoActivity extends BaseActivity implements IAnLiveEventListener {
    public static VideoActivity instance;
    private Button btnAnswer, btnReject, btnMute, btnDisableCamera, btnHangUp;
    private FrameLayout remoteVideoView, localVideoView, userInfoFrame;
    private IMppApp mApp;
    private User targetUser;
    private ImageView ivPhoto;
    private TextView textUserName;
    private TextView textStatus;
    private LinearLayout controlView, functionView, userInfoView;
    private View touchView,viewTopMargin;

    public static final int INVITATION_RECEIVED = 0;
    public static final int INVITATION_SEND = 1;
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_VOICE = "voice";

    private int localVideoViewTouchX = 0;
    private int localVideoViewTouchY = 0;
    private long time = 0;
    private int mode = -1;
    private String type = "";
    private boolean enableAudio = true;
    private boolean enableVideo = true;
    private boolean controlUIVisible = true;
    private int controlUIVisibleTime = 0;

    private Handler timingHandler;
    private Runnable timingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (IMppApp) getApplicationContext();
        instance = this;
        setContentView(R.layout.activity_video);
        initView();
        checkBundle();

        Log.e("VideoActivity", "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mode == INVITATION_SEND) {
            sendMsg();
        }
        if (mApp.anLive != null) {
            mApp.anLive.hangup();
        }
    }

    private void initView() {
        btnAnswer = (Button) findViewById(R.id.btnAnswer);
        btnReject = (Button) findViewById(R.id.btnReject);
        remoteVideoView = (FrameLayout) findViewById(R.id.remoteVideoView);
        localVideoView = (FrameLayout) findViewById(R.id.localVideoView);
        ivPhoto = (ImageView) findViewById(R.id.imgPhoto);
        textUserName = (TextView) findViewById(R.id.textUserName);
        textStatus = (TextView) findViewById(R.id.textStatus);
        btnDisableCamera = (Button) findViewById(R.id.btnDisableVideo);
        btnMute = (Button) findViewById(R.id.btnMute);
        btnHangUp = (Button) findViewById(R.id.btnHangUp);
        controlView = (LinearLayout) findViewById(R.id.controlView);
        functionView = (LinearLayout) findViewById(R.id.functionView);
        userInfoView = (LinearLayout) findViewById(R.id.userInfoView);
        touchView = (View) findViewById(R.id.viewTouch);
        viewTopMargin = (View) findViewById(R.id.live_top_margin);
        userInfoFrame = (FrameLayout) findViewById(R.id.userInfoFrame);
        
        localVideoView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    localVideoViewTouchX = (int) event.getX();
                    localVideoViewTouchY = (int) event.getY() + getStatusBarHeight();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    localVideoView.setX(event.getRawX() - localVideoViewTouchX);
                    localVideoView.setY(event.getRawY() - localVideoViewTouchY);
                }
                return true;
            }
        });

        touchView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControlUI(!controlUIVisible);
            }
        });
        btnAnswer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    mApp.anLive.answer(true);
                } catch (ArrownockException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        btnReject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mApp.anLive != null) {
                    mApp.anLive.hangup();
                }
                finish();
        		overridePendingTransition(android.R.anim.fade_in,R.anim.push_up_out);
            }
        });
        btnHangUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mApp.anLive != null) {
                    mApp.anLive.hangup();
                }
                finish();
        		overridePendingTransition(android.R.anim.fade_in,R.anim.push_up_out);
            }
        });
        btnMute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                controlUIVisibleTime = 0;
                if (enableAudio) {
                    mApp.anLive.setAudioState(AudioState.OFF);
                    btnMute.setText(R.string.anlive_unmute);
                } else {
                    mApp.anLive.setAudioState(AudioState.ON);
                    btnMute.setText(R.string.anlive_mute);
                }
                enableAudio = !enableAudio;
            }
        });
        btnDisableCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                controlUIVisibleTime = 0;
                if (enableVideo) {
                    localVideoView.setVisibility(View.GONE);
                    mApp.anLive.setVideoState(VideoState.OFF);
                    btnDisableCamera.setText(R.string.anlive_enable_camera);
                } else {
                    localVideoView.setVisibility(View.VISIBLE);
                    mApp.anLive.setVideoState(VideoState.ON);
                    btnDisableCamera.setText(R.string.anlive_disable_camera);
                }
                enableVideo = !enableVideo;
            }
        });
    }

    private void checkBundle() {
        mode = getIntent().getIntExtra("mode", -1);
        if (getIntent().hasExtra("clientId")) {
            String clientId = getIntent().getStringExtra("clientId");
            targetUser = UserManager.getInstance(this).getUserByClientId(clientId);
        }
        if (getIntent().hasExtra("type")) {
            type = getIntent().getStringExtra("type");
        }

        showUserInfo();
        setMode();
    }

    private void setMode() {
        if (type.equals(TYPE_VIDEO)) {
            textStatus.setText(R.string.anlive_received_video_invitation);
        } else if (type.equals(TYPE_VOICE)) {
            btnDisableCamera.setVisibility(View.GONE);
            textStatus.setText(R.string.anlive_received_voice_invitation);
        }
        
    	controlView.setVisibility(View.GONE);
    	functionView.setVisibility(View.GONE);
        switch (mode) {
        case INVITATION_RECEIVED:
        	controlView.setVisibility(View.VISIBLE);
            if (type.equals("video")) {
                textStatus.setText(R.string.anlive_received_video_invitation);
            } else if (type.equals("voice")) {
                textStatus.setText(R.string.anlive_received_voice_invitation);
            }
            break;
        case INVITATION_SEND:
        	functionView.setVisibility(View.VISIBLE);
            textStatus.setText(R.string.anlive_waiting_reply);
            break;
        }
    }

    private void toggleControlUI(boolean show) {
        controlUIVisible = show;
        if (show) {
            userInfoView.setVisibility(View.VISIBLE);
            userInfoView.animate().alpha(1).setDuration(300).setListener(null).start();
        	switch (mode) {
            case INVITATION_RECEIVED:
                controlView.setVisibility(View.VISIBLE);
                controlView.animate().alpha(1).setDuration(300).start();
                break;
            case INVITATION_SEND:
                functionView.setVisibility(View.VISIBLE);
                functionView.animate().alpha(1).setDuration(300).start();
                break;
            }
        } else {
            controlUIVisibleTime = 0;
            userInfoView.animate().alpha(0).setDuration(300).setListener(new AnimatorListener() {
                public void onAnimationCancel(Animator arg0) {
                }

                public void onAnimationRepeat(Animator arg0) {
                }

                public void onAnimationStart(Animator arg0) {
                }

                @Override
                public void onAnimationEnd(Animator arg0) {
                    userInfoView.setVisibility(View.GONE);
                    switch (mode) {
                    case INVITATION_RECEIVED:
                        controlView.setVisibility(View.GONE);
                        break;
                    case INVITATION_SEND:
                        functionView.setVisibility(View.GONE);
                        break;
                    }
                }

            }).start();
            switch (mode) {
            case INVITATION_RECEIVED:
                controlView.animate().alpha(0).setDuration(300).start();
                break;
            case INVITATION_SEND:
                functionView.animate().alpha(0).setDuration(300).start();
                break;
            }
            
        }
    }

    private void showUserInfo() {
        ImageLoader.getInstance(this).DisplayImage(targetUser.userPhotoUrl, ivPhoto, R.drawable.friend_default, true);
        textUserName.setText(targetUser.userName);
    }

    private void startTiming() {
        timingHandler = new Handler();
        timingRunnable = new Runnable() {
            @Override
            public void run() {
                time++;
                if (type.equals(TYPE_VIDEO)) {
                    textStatus.setText(getString(R.string.anlive_video_oncall) + " " + getTimeStr());
                } else if (type.equals(TYPE_VOICE)) {
                    textStatus.setText(getString(R.string.anlive_voice_oncall) + " " + getTimeStr());
                }
                timingHandler.postDelayed(this, 1000);
                checkControlUiVisibility();
            }
        };
        timingHandler.post(timingRunnable);
    }

    private void checkControlUiVisibility() {
        if (controlUIVisible) {
            controlUIVisibleTime++;
            if (controlUIVisibleTime >= 5) {
            	if(type.equals(TYPE_VIDEO)){
                    toggleControlUI(false);
            	}
            }
        }
    }

    private void sendMsg() {
//        if (ChatActivity.instance != null) {
//            ChatActivity.instance.sendVideoCallNotice((type.equals(TYPE_VIDEO) ? "[视频" : "[语音") + "通话 " + getTimeStr()
//                    + "]");
//        }
    }

    private String getTimeStr() {
        String timeStr = (time / 60 >= 10 ? "" : "0") + time / 60 + ":" + (time % 60 >= 10 ? "" : "0") + time % 60;
        if (time / 3600 > 0) {
            timeStr = time / 3600 + ":" + timeStr;
        }
        return timeStr;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onError(String arg0, ArrownockException e) {
        e.printStackTrace();
    }

    @Override
    public void onLocalVideoSizeChanged(int arg0, int arg1) {
    }

    @Override
    public void onLocalVideoViewReady(final LocalVideoView view) {
        Log.e("VideoActivity", "onLocalVideoViewReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(Utils.px2Dp(view.getContext(), 96), view.getVideoHeight()
                        * Utils.px2Dp(view.getContext(), 96) / view.getVideoWidth());
                flp.gravity = Gravity.RIGHT | Gravity.TOP;
                view.setLayoutParams(flp);
                localVideoView.removeAllViews();
                localVideoView.addView(view);
            }
        });
    }

    @Override
    public void onRemotePartyVideoViewReady(String arg0, final VideoView view) {
        Log.e("VideoActivity", "onLocalVideoViewReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	ivPhoto.setVisibility(View.GONE);
            	LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewTopMargin.getLayoutParams();
            	lp.weight = 0.08f;
            	viewTopMargin.setLayoutParams(lp);
            	LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) userInfoFrame.getLayoutParams();
            	lp2.weight = 0.92f;
            	userInfoFrame.setLayoutParams(lp2);
            	
                DisplayMetrics metric = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metric);
                int height = metric.heightPixels;

                FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(view.getVideoWidth() * height
                        / view.getVideoHeight(), height);
                flp.gravity = Gravity.TOP;
                view.setLayoutParams(flp);
                remoteVideoView.removeAllViews();
                remoteVideoView.addView(view);
            }
        });
    }

    @Override
    public void onReceivedInvitation(boolean arg0, String arg1, String arg2, String arg3, Date arg4) {
    }

    @Override
    public void onRemotePartyAudioStateChanged(String arg0, final AudioState arg1) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (arg1.equals(AudioState.ON)) {
//                    ivState.setImageResource(R.drawable.ic_launcher);
//
//                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(300, 300);
//                    flp.gravity = Gravity.CENTER;
//                    ivState.setLayoutParams(flp);
//                } else {
//                    ivState.setImageResource(R.drawable.menu_done);
//
//                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(300, 300);
//                    flp.gravity = Gravity.CENTER;
//                    ivState.setLayoutParams(flp);
//                }
//            }
//        });
    }

    @Override
    public void onRemotePartyConnected(String arg0) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startTiming();
                mode = INVITATION_SEND;
                setMode();
            }
        });
    }

    @Override
    public void onRemotePartyDisconnected(String arg0) {
        Log.e("videoActivity", "onRemotePartyDisconnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (timingHandler != null) {
                    timingHandler.removeCallbacks(timingRunnable);
                }
                finish();
        		overridePendingTransition(android.R.anim.fade_in,R.anim.push_up_out);
            }
        });
    }

    @Override
    public void onRemotePartyVideoSizeChanged(String arg0, int arg1, int arg2) {
    }

    @Override
    public void onRemotePartyVideoStateChanged(String arg0, final VideoState arg1) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (arg1.equals(VideoState.ON)) {
//                    remoteVideoView.setVisibility(View.VISIBLE);
//                } else {
//                    remoteVideoView.setVisibility(View.INVISIBLE);
//                    ivState.setImageResource(R.drawable.ic_launcher);
//
//                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(300, 300);
//                    flp.gravity = Gravity.CENTER;
//                    ivState.setLayoutParams(flp);
//                }
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoActivity.instance = null;
    }
}