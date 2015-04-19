package co.herxun.impp.fragment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONObject;

import com.arrownock.social.IAnSocialCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.activity.ChatActivity;
import co.herxun.impp.activity.CreateTopicActivity;
import co.herxun.impp.activity.SearchUserActivity;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.view.UserDetailView;
import eu.janmuller.android.simplecropimage.CropImage;

public class SettingFragment extends BaseFragment {
	private UserDetailView mUserDetailView;
	private Dialog mActionDialog;
	
	public SettingFragment(String title) {
		super(title);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        return rootView;
	}
	
	public void onViewCreated(View fragView, Bundle savedInstanceState){
		super.onViewCreated(fragView, savedInstanceState);
		final Context ct = fragView.getContext();
		
		mUserDetailView = (UserDetailView) fragView.findViewById(R.id.setting_userDetailView);
		mUserDetailView.setButton(fragView.getContext().getString(R.string.else_sign_out), new OnClickListener(){
			@Override
			public void onClick(View v) {
				UserManager.getInstance(v.getContext()).logout();
			}
		});
		mUserDetailView.getImageView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mActionDialog.show();
			}
		});
		
		AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(ct);
		View view = getActivity().getLayoutInflater().inflate( R.layout.view_friend_alert, null);
		ImageView imgTakePic = (ImageView) view.findViewById(R.id.dialog_img1);
		imgTakePic.setImageResource(R.drawable.dialog_camera);
		ImageView imgPickPic = (ImageView) view.findViewById(R.id.dialog_img2);
		imgPickPic.setImageResource(R.drawable.dialog_upload);
		TextView textTakePic = (TextView) view.findViewById(R.id.dialog_text1);
		textTakePic.setText(R.string.chat_take_picture);
		TextView textPickPic = (TextView) view.findViewById(R.id.dialog_text2);
		textPickPic.setText(R.string.chat_pick_picture);
		view.findViewById(R.id.action_dialog_friend_btn).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mActionDialog.dismiss();
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				try {
					Uri mImageCaptureUri = null;
					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {
						mImageCaptureUri = mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(getActivity()));
					}
					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
					intent.putExtra("return-data", true);
					startActivityForResult(intent, Constant.REQUESTCODE_PHOTO_TAKE);
				} catch (ActivityNotFoundException e) {
					Log.d("", "cannot take picture", e);
				}
			}
		});
		view.findViewById(R.id.action_dialog_topic_btn).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mActionDialog.dismiss();
				
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, ""), Constant.REQUESTCODE_PHOTO_PICK);
			}
		});
		dialogBuiler.setView(view);
		mActionDialog = dialogBuiler.create();
	}
	
	@Override
	public void onViewShown(){
		mUserDetailView.setUserInfo(UserManager.getInstance(getActivity()).getCurrentUser());
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    final Context ct = getActivity();
	    
		if (requestCode == Constant.REQUESTCODE_PHOTO_PICK) {
			String imageFilePath = ImageUtility.getFilePathFromGallery(ct,data);
			startCropImage(imageFilePath);
			

		}else if(requestCode == Constant.REQUESTCODE_PHOTO_TAKE){
			String imageFilePath = ImageUtility.getFileTemp(ct).getPath();
			startCropImage(imageFilePath);
			
		}else if (requestCode == Constant.REQUESTCODE_PHOTO_CROP) {
		    if(data==null || data.getStringExtra(CropImage.IMAGE_PATH) == null){
		    	return;
		    }
		    byte[] fileToUpload;
		    
			String photoFilePath = data.getStringExtra(CropImage.IMAGE_PATH);
			File file = new File(photoFilePath);
		    int size = (int) file.length();
		    fileToUpload = new byte[size];
		    try {
		        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
		        buf.read(fileToUpload, 0, fileToUpload.length);
		        buf.close();
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }

			UserManager.getInstance(ct).updateMyPhoto(fileToUpload,new IAnSocialCallback(){
				@Override
				public void onFailure(JSONObject arg0) {
					
				}
				@Override
				public void onSuccess(JSONObject arg0) {
					mUserDetailView.setUserInfo(UserManager.getInstance(ct).getCurrentUser());
				}
				
			});
			
			ImageUtility.deleteTempFile(ct);
		}
	}	
	
	private void startCropImage(String imageFilePath){
		if (imageFilePath == null || imageFilePath.trim().length() == 0) {
			return;
		}
		Intent intent = new Intent(getActivity(), CropImage.class);
		intent.putExtra(CropImage.IMAGE_PATH, imageFilePath);
		intent.putExtra(CropImage.SCALE, true);
		intent.putExtra(CropImage.ASPECT_X, 1);
		intent.putExtra(CropImage.ASPECT_Y, 1);
		//intent.putExtra(CropImage.OUTPUT_X, 720);
		//intent.putExtra(CropImage.OUTPUT_Y, 720);
		// intent.putExtra(CropImage.CIRCLE_CROP, true);
		intent.putExtra("border-color", 0xFFffffff);
		startActivityForResult(intent, Constant.REQUESTCODE_PHOTO_CROP);
	}
}
