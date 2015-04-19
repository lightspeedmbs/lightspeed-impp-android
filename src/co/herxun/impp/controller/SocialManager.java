package co.herxun.impp.controller;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.arrownock.exception.ArrownockException;
import com.arrownock.social.AnSocial;
import com.arrownock.social.AnSocialFile;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import co.herxun.impp.IMppApp;
import co.herxun.impp.utils.DBug;

public class SocialManager {
	public static SocialManager sSocialManager;
	private Context ct;
	private AnSocial anSocial;
	
	private SocialManager(Context ct){
		this.ct = ct;
		anSocial = ((IMppApp)ct.getApplicationContext()).anSocial;
	}
	
	public static SocialManager getInstance(Context ct){
		if(sSocialManager==null){
			sSocialManager = new SocialManager(ct);
		}
		return sSocialManager;
	}
	
	public void createPhoto(String userId,byte[] data,final IAnSocialCallback callback){
		AnSocialFile AnFile = new AnSocialFile("photo", new ByteArrayInputStream(data));
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("photo", AnFile);
		params.put("mime_type", "image/png");
		params.put("user_id", userId);

		try {
			anSocial.sendRequest("photos/create.json", AnSocialMethod.POST, params, new IAnSocialCallback(){
				@Override
				public void onFailure(JSONObject arg0) {
					if(callback!=null){
						callback.onFailure(arg0);
					}
				}
				@Override
				public void onSuccess(JSONObject arg0) {
					if(callback!=null){
						callback.onSuccess(arg0);
					}
				}
			});
		} catch (ArrownockException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
