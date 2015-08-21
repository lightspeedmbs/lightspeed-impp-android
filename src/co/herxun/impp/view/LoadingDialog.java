package co.herxun.impp.view;

import co.herxun.impp.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class LoadingDialog extends ProgressDialog{

    
    private String progressText ;
    private boolean mCanBack = true;
    private Context mContext;

    public LoadingDialog(Context context) {
        super(context, R.style.noAlpha);
    }
    
    public LoadingDialog(Context context, String text) {
        super(context);
        this.progressText = text;
    }
    
    public LoadingDialog(Context context, String text, boolean canBack) {
        super(context);
        this.progressText = text;
        mContext = context;
        mCanBack = canBack;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_progress_dialog);
    }
    
    public void setDismissListener(OnDismissListener listener){
        setOnDismissListener(listener);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(!mCanBack){
			Toast.makeText(mContext,"操作正在进行，请稍后！" , Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
    

}
