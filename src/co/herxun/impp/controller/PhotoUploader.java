package co.herxun.impp.controller;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import co.herxun.impp.utils.DBug;

import com.arrownock.social.IAnSocialCallback;

import android.content.Context;
import android.os.Handler;

public class PhotoUploader{
	private List<byte[]> dataList;
	private List<String> urlList;
	private Handler handler;
	private String userId;
	private Context ct;
	private UploadRunnable mUploadRunnable;
	private PhotoUploadCallback callback;
	
	private int curretIndex = 0;
	
	public PhotoUploader(Context ct,String userId, List<byte[]> dataList,PhotoUploadCallback callback){
		this.dataList = dataList;
		this.userId = userId;
		this.ct = ct;
		this.callback = callback;
		curretIndex = 0;
		
		handler = new Handler();
		urlList = new ArrayList<String>();
		mUploadRunnable = new UploadRunnable();
		
		DBug.e("PhotoUploader.dataList.size",dataList.size()+"");
	}
	
	public void startUpload(){
		if(dataList!=null && dataList.size()>0){
			handler.post(mUploadRunnable);
		}else{
			if(callback!=null){
				callback.onSuccess(urlList);
			}
		}
	}
	
	private class UploadRunnable implements Runnable{
		private UploadRunnable mUploadRunnable;
		public UploadRunnable(){
			mUploadRunnable = this;
		}
		@Override
		public void run() {
			SocialManager.createPhoto(ct, userId, dataList.get(curretIndex++), new IAnSocialCallback(){
				@Override
				public void onFailure(JSONObject arg0) {
					if(callback!=null){
						callback.onFailure(arg0.toString());
					}
				}
				@Override
				public void onSuccess(JSONObject json) {
					try {
						String url = json.getJSONObject("response").getJSONObject("photo").getString("url");
						urlList.add(url);
					} catch (JSONException e) {
						e.printStackTrace();
						if(callback!=null){
							callback.onFailure(e.getMessage());
						}
					}
					if(curretIndex < dataList.size()){
						handler.post(mUploadRunnable);
					}else{
						if(callback!=null){
							callback.onSuccess(urlList);
						}
					}
				}
			});
		}
	}
	
	public interface PhotoUploadCallback{
		public void onFailure(String errorMsg);
		public void onSuccess(List<String> urlList);
	}
	
}