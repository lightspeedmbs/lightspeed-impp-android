package co.herxun.impp.activity;

import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.view.WindowManager;
import co.herxun.impp.IMppApp;
import co.herxun.impp.view.LoadingDialog;

public class BaseActivity extends ActionBarActivity {
    private LoadingDialog loadingDialog;
	protected boolean isActive = false;
	
	@Override
	protected void onResume() {
		super.onResume();
		IMppApp mApp = (IMppApp) getApplicationContext();
		mApp.activityToForeground(this);
		mApp.addActivity(this);
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    IMppApp mApp = (IMppApp) getApplicationContext();
	    mApp.removeActivity(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		IMppApp mApp = (IMppApp) getApplicationContext();
		mApp.activityToBackground(this);
	}
	
	public void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        Window dialogWindow = loadingDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }
	
	public void dismissLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}
