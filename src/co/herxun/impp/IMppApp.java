package co.herxun.impp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.activeandroid.ActiveAndroid;
import com.arrownock.exception.ArrownockException;
import com.arrownock.live.AnLive;
import com.arrownock.push.AnPush;
import com.arrownock.push.AnPushCallbackAdapter;
import com.arrownock.push.AnPushStatus;
import com.arrownock.social.AnSocial;

import co.herxun.impp.imageloader.ImageLoader;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import android.app.Application;
import android.util.Log;

public class IMppApp extends Application {

	public AnSocial anSocial;
	public AnLive anLive;
	public AnPush anPush;
	
	@Override
	public void onCreate(){
		init();
	}
	
	private void init(){
		ActiveAndroid.initialize(this);
		
		try {
			anSocial = new AnSocial(this,getString(R.string.app_key));
			
			anPush = AnPush.getInstance(this);
			anPush.setSecureConnection(true);
			anPush.setCallback(new AnPushCallbackAdapter(){
				@Override
				public void register(boolean err, final String anid, ArrownockException exception) {
					if (!err) {
						try {
							anPush.enable();
						} catch (ArrownockException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
					}
				}
				@Override
				public void statusChanged(AnPushStatus currentStatus, ArrownockException exception) {
					
					if (currentStatus == AnPushStatus.ENABLE) {
						Log.i("push.statusChanged","Push status enalbed");
						
					} else if (currentStatus == AnPushStatus.DISABLE) {
						Log.e("push.statusChanged","Push status disabled");
					}
					
					if (exception != null) {
						Log.e("push.statusChanged","Push status changed with error occuring = "+ exception.toString() );
					}
				}
			});
			List<String> channel = new ArrayList<String>();
			channel.add(Constant.PUSH_CHANNEL);
			anPush.register(channel);
		} catch (ArrownockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
