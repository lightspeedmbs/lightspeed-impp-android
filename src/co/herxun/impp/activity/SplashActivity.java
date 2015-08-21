package co.herxun.impp.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import co.herxun.impp.R;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.controller.UserManager.AddFriendCallback;
import co.herxun.impp.controller.UserManager.FetchSingleUserCallback;
import co.herxun.impp.im.controller.IMManager;
import co.herxun.impp.im.model.Chat;
import co.herxun.impp.im.model.Message;
import co.herxun.impp.model.User;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.SpfHelper;

import com.arrownock.social.IAnSocialCallback;

public class SplashActivity extends Activity {
    private ProgressBar progress = null;
    private final int SPLASH_DISPLAY_LENGHT = 3000;
    private String payload;

    protected void showLoading() {
        progress.setVisibility(View.VISIBLE);
    }

    protected void dismissLoading() {
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        checkBundle();
        progress = (ProgressBar) this.findViewById(R.id.progress);

        autoSignIn();
        // new Handler().postDelayed(new Runnable() {
        // public void run() {
        // autoSignIn();
        // }
        // }, SPLASH_DISPLAY_LENGHT);
    }

    private void autoSignIn() {
        if (SpfHelper.getInstance(this).hasSignIn()) {
            showLoading();
            UserManager.getInstance(this).login(SpfHelper.getInstance(this).getMyUsername(),
                    SpfHelper.getInstance(this).getMyPwd(), new IAnSocialCallback() {
                        @Override
                        public void onFailure(JSONObject arg0) {
                            try {
                                dismissLoading();
                                String errorMsg = arg0.getJSONObject("meta").getString("message");
                                Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
                                goToLoginActivity();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                dismissLoading();
                                goToLoginActivity();
                            }
                        }

                        @Override
                        public void onSuccess(final JSONObject arg0) {
                            try {
                                JSONObject userJson = arg0.getJSONObject("response").getJSONObject("user");
                                User user = new User(userJson);
                                afterLogin(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                goToLoginActivity();
                            }
                        }
                    });
        } else {
            goToLoginActivity();
        }
    }

    private void goToLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void afterLogin(User user) {
        IMManager.getInstance(this).connect(user.clientId);
        UserManager.getInstance(this).setCurrentUser(user);

        IMManager.getInstance(this).fetchAllRemoteTopic();
        UserManager.getInstance(this).fetchMyRemoteFriend(null);

        IMManager.getInstance(this).bindAnPush();

        goToMainActivity();
    }

    private void checkBundle() {
        if (getIntent().hasExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD)) {
            payload = getIntent().getStringExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD);
        }
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        if (payload != null) {
            i.putExtra(Constant.INTENT_EXTRA_KEY_PAYLOAD, payload);
        }
        dismissLoading();
        startActivity(i);
        finish();
    }
}
