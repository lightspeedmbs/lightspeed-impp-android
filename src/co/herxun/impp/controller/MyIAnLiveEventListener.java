package co.herxun.impp.controller;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import co.herxun.impp.R;
import co.herxun.impp.activity.VideoActivity;

import com.arrownock.exception.ArrownockException;
import com.arrownock.live.AudioState;
import com.arrownock.live.IAnLiveEventListener;
import com.arrownock.live.LocalVideoView;
import com.arrownock.live.VideoState;
import com.arrownock.live.VideoView;

public class MyIAnLiveEventListener implements IAnLiveEventListener{

	private Context ct;
	
	public MyIAnLiveEventListener(Context ct){
		this.ct = ct;
	}
	
	@Override
	public void onError(String arg0, ArrownockException arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocalVideoSizeChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocalVideoViewReady(LocalVideoView arg0) {
		if(VideoActivity.instance!=null){
			VideoActivity.instance.onLocalVideoViewReady(arg0);
		}
	}

	@Override
	public void onReceivedInvitation(boolean isValid, String sessionId, String clientId,String type, Date createdAt) {
		if(isValid){
			Intent i = new Intent(ct,VideoActivity.class);
			i.putExtra("type", type);
			i.putExtra("mode", VideoActivity.INVITATION_RECEIVED);
			i.putExtra("clientId", clientId);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ct.startActivity(i);
	        ((Activity) ct).overridePendingTransition(R.anim.push_up_in,android.R.anim.fade_out);
		}
	}

	@Override
	public void onRemotePartyAudioStateChanged(String arg0, AudioState arg1) {
		if(VideoActivity.instance!=null){
			VideoActivity.instance.onRemotePartyAudioStateChanged(arg0, arg1);
		}
	}

	@Override
	public void onRemotePartyConnected(String arg0) {
		if(VideoActivity.instance!=null){
			VideoActivity.instance.onRemotePartyConnected(arg0);
		}
	}

	@Override
	public void onRemotePartyDisconnected(String arg0) {
		if(VideoActivity.instance!=null){
			VideoActivity.instance.onRemotePartyDisconnected(arg0);
		}
	}

	@Override
	public void onRemotePartyVideoSizeChanged(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemotePartyVideoStateChanged(String arg0, VideoState arg1) {
		if(VideoActivity.instance!=null){
			VideoActivity.instance.onRemotePartyVideoStateChanged(arg0, arg1);
		}
	}

	@Override
	public void onRemotePartyVideoViewReady(String arg0, VideoView arg1) {
		if(VideoActivity.instance!=null){
			VideoActivity.instance.onRemotePartyVideoViewReady(arg0, arg1);
		}
	}

}
