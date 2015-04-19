package co.herxun.impp.im.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import co.herxun.impp.controller.SocialManager;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;

public class VoiceHelper {
	public static VoiceHelper sVoiceHelper;
	private boolean recording = false;
	public RecordTimer mRecordTimer;
	private MediaRecorder mRecorder = null;
	private String RECORD_TEMP_FILE = "record_temp_file.mp4";
	public File mFileRecord;
	public MediaPlayer mPlayer;
	private Context ct;
	private VoiceRecordCallback callback;
	private  VoiceHelper(Context ct){
		this.ct = ct;
	}
	
	public static VoiceHelper getInstance(Context ct){
		if(sVoiceHelper==null){
			sVoiceHelper = new VoiceHelper(ct);
		}
		return sVoiceHelper;
	}

	
	public void setRecordCallback(VoiceRecordCallback callback){
		this.callback = callback;
	}
	
	public boolean isRecording(){
		return recording;
	}
	
	public class RecordTimer implements Runnable {
		private int mInterval = 100;
		private int mSec = 0;
		private Handler handler;
		
		public RecordTimer(){
			handler = new Handler();
		}
		@Override
		public void run() {
			mSec += mInterval;
			handler.postDelayed(this, mInterval);
		}

		public void reset() {
			mSec = 0;
		}

		public int getTime() {
			return mSec;
		}
		
		public void stop(){
			handler.removeCallbacks(this);
		}
	}
	
	public void startRecord(){
		recording = true;
		
		if (mRecorder != null) {
            return;
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        String path = ct.getCacheDir().getPath();
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mFileRecord = new File(path, "/"+RECORD_TEMP_FILE);
        mRecorder.setOutputFile(mFileRecord.getAbsolutePath());

        try {
            mRecorder.prepare();
            mRecorder.start();

            mRecordTimer = new RecordTimer();
    		mRecordTimer.run();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void stopRecord(){
		recording = false;
		mRecordTimer.stop();

		if (mRecorder != null) {
			try {
				mRecorder.stop();
			} catch (RuntimeException e) {
				e.printStackTrace();

				mRecorder.release();
				mRecorder = null;
				if(callback!=null){
					callback.onFailed();
				}
			}

			mRecorder.release();
			mRecorder = null;
			if(callback!=null){
				callback.onSuccess(mFileRecord);
			}

		} else {
			if(callback!=null){
				callback.onFailed();
			}
		}
	}
	
	public void cancelRecord(){
		recording = false;
		mRecordTimer.stop();

		if (mRecorder != null) {
			try {
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;
			} catch (RuntimeException e) {
				e.printStackTrace();

				mRecorder.release();
				mRecorder = null;
			}
		}
		if(callback!=null){
			callback.onCancel();
		}
	}
	
	public void playVoice(byte[] data){
		 File tempFile = null;
		 try {
		        // create temp file that will hold byte array
			 	tempFile = File.createTempFile("temp", "mp4", ct.getCacheDir());
			 	tempFile.deleteOnExit();
		        FileOutputStream fos = new FileOutputStream(tempFile);
		        fos.write(data);
		        fos.close();
		    } catch (IOException ex) {
		        String s = ex.toString();
		        ex.printStackTrace();
		    }
		
		try {
			if(mPlayer!=null){
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
			}
			mPlayer = new MediaPlayer();
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					if (mPlayer != null) {
						mPlayer.stop();
						mPlayer.release();
						mPlayer = null;
			        }
				}
			});
			mPlayer.setDataSource(tempFile.getAbsolutePath());
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public interface VoiceRecordCallback{
		public void onCancel();
		public void onFailed();
		public void onSuccess(File file);
	}
}
