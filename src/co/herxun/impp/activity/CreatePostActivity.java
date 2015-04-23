package co.herxun.impp.activity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import co.herxun.impp.R;
import co.herxun.impp.controller.SocialManager;
import co.herxun.impp.controller.UserManager;
import co.herxun.impp.utils.Constant;
import co.herxun.impp.utils.DBug;
import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;
import co.herxun.impp.view.AppBar;

import com.arrownock.social.IAnSocialCallback;

public class CreatePostActivity extends BaseActivity {
	private AppBar appbar;
	private EditText etContent;
	private GridView gdPhotos;
	private Dialog mActionDialog;
	private PhotoGridAdapter mPhotoGridAdapter;
	private List<Object> dataList;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
	}
	
	private void initView(){
		setContentView(R.layout.activity_create_post);
		
		appbar = (AppBar) findViewById(R.id.create_post_app_bar);
		appbar.getLogoView().setImageResource(R.drawable.menu_back);
		appbar.getLogoView().setLayoutParams(new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56)));
		appbar.getLogoView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				onBackPressed();
			}
		});
		appbar.getTextView().setVisibility(View.VISIBLE);
		appbar.getTextView().setText(R.string.wall_create_post_title);

		appbar.getMenuItemView().setVisibility(View.VISIBLE);
		appbar.getMenuItemView().setImageResource(R.drawable.menu_done);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(this, 56),Utils.px2Dp(this, 56));
		rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		appbar.getMenuItemView().setLayoutParams(rlp);
		appbar.getMenuItemView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				createPost();
			}
		});
		
		
		etContent = (EditText) findViewById(R.id.wall_create_et);
		gdPhotos = (GridView) findViewById(R.id.wall_create_grid);
		dataList = new ArrayList<Object>();
		mPhotoGridAdapter = new PhotoGridAdapter(dataList);
		gdPhotos.setAdapter(mPhotoGridAdapter);
		
		AlertDialog.Builder dialogBuiler = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate( R.layout.view_friend_alert, null);
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
						mImageCaptureUri = mImageCaptureUri = Uri.fromFile(ImageUtility.getFileTemp(CreatePostActivity.this));
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

		dataList.add(PhotoGridAdapter.ADD_BUTTON);
		mPhotoGridAdapter.notifyDataSetChanged();
	}
	
	private void createPost(){
		List<byte[]> data = new ArrayList<byte[]>();
		for(Object photo : dataList){
			if(photo instanceof byte[]){
				data.add((byte[])photo);
			}
		}

		if(data.size()==0 && etContent.getText().toString().length()==0){
			return;
		}

		appbar.getMenuItemView().setEnabled(false);
		SocialManager.createPost(this, getString(R.string.wall_id), UserManager.getInstance(this).getCurrentUser().userId, 
				etContent.getText().toString() ,data, new IAnSocialCallback(){
					@Override
					public void onFailure(JSONObject arg0) {
						DBug.e("createPost.onFailure",arg0.toString());
						appbar.getMenuItemView().setEnabled(true);
					}
					@Override
					public void onSuccess(JSONObject arg0) {
						DBug.e("createPost.onSuccess",arg0.toString());
						setResult(Activity.RESULT_OK);
						onBackPressed();
					}
			
		});
	}
	
	
	private byte[] getDataFromFilePath(String filePath){
		Bitmap bmResized = null ;
		File file = new File(filePath);
	    int size = (int) file.length();
	    byte[] imgData = new byte[size];
		try{
		    try {
		        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
		        buf.read(imgData, 0, imgData.length);
		        buf.close();
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		    
		    BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        FileInputStream stream1=new FileInputStream(filePath);
	        BitmapFactory.decodeStream(stream1,null,o);
	        stream1.close();
	        
	        //Find the correct scale value. It should be the power of 2.
	        final int REQUIRED_SIZE=256;
	        int width_tmp=o.outWidth, height_tmp=o.outHeight;
	        int scale=1;
	        while(true){
	            if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                break;
	            width_tmp/=2;
	            height_tmp/=2;
	            scale*=2;
	        }
	        
	        //decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        FileInputStream stream2=new FileInputStream(filePath);
	        bmResized = BitmapFactory.decodeStream(stream2, null, o2);
	        stream2.close();

	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    ByteArrayOutputStream blob = new ByteArrayOutputStream();
//	    Bitmap original = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
//	    original.compress(Bitmap.CompressFormat.JPEG, 100, blob);
	    
	    bmResized.compress(Bitmap.CompressFormat.JPEG, 100, blob);
	    imgData = blob.toByteArray();
	    
        return imgData;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
		if (requestCode == Constant.REQUESTCODE_PHOTO_PICK) {
			String imageFilePath = ImageUtility.getFilePathFromGallery(this,data);
			if(imageFilePath!=null){
				dataList.remove(dataList.size()-1);
				mPhotoGridAdapter.notifyDataSetChanged();
				dataList.add(getDataFromFilePath(imageFilePath));
				dataList.add(PhotoGridAdapter.ADD_BUTTON);
				mPhotoGridAdapter.notifyDataSetChanged();
			}

		}else if(requestCode == Constant.REQUESTCODE_PHOTO_TAKE){
			String imageFilePath = ImageUtility.getFileTemp(this).getPath();
			if(imageFilePath!=null){
				dataList.remove(dataList.size()-1);
				mPhotoGridAdapter.notifyDataSetChanged();
				dataList.add(getDataFromFilePath(imageFilePath));
				dataList.add(PhotoGridAdapter.ADD_BUTTON);
				mPhotoGridAdapter.notifyDataSetChanged();
			}
		}
	}	
	
	
	public class PhotoGridAdapter extends BaseAdapter {
		public static final String ADD_BUTTON = "add_button";
		private List<Object> data ;
		public PhotoGridAdapter(List<Object> data){
			this.data = data;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PhotoGridItem view = (PhotoGridItem) convertView;
			if (convertView == null) {
				view = new PhotoGridItem(parent.getContext());
			}
			view.setData(data.get(position));
			
			return view;
		}
		
		public class PhotoGridItem extends FrameLayout{
			private BtnAdd btnAdd;
			private ImageView imgPhoto;
			public PhotoGridItem(Context ct) {
				super(ct);
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				int width = size.x;
				
				setLayoutParams(new AbsListView.LayoutParams(width/gdPhotos.getNumColumns()-Utils.px2Dp(ct, 8),width/gdPhotos.getNumColumns()-Utils.px2Dp(ct, 8)));
				
				btnAdd = new BtnAdd(ct);
				addView(btnAdd);
				imgPhoto = new ImageView(ct);
				imgPhoto.setScaleType(ScaleType.CENTER_CROP);
				addView(imgPhoto);
			}
			
			public void setData(Object data){
				btnAdd.setVisibility(View.GONE);
				imgPhoto.setVisibility(View.GONE);
				if(data instanceof String){
					btnAdd.setVisibility(View.VISIBLE);
				}else if(data instanceof byte[]){
					imgPhoto.setVisibility(View.VISIBLE);
					
					byte[] imgData = (byte[]) data;
					Bitmap bitmap = BitmapFactory.decodeByteArray(imgData, 0, imgData.length);
					imgPhoto.setImageBitmap(bitmap);
				}
			}
		}
		
		public class BtnAdd extends RelativeLayout{
			public BtnAdd(Context context) {
				super(context);
				setBackgroundColor(context.getResources().getColor(R.color.no6));
				ImageView imgBtn = new ImageView(context);
				imgBtn.setImageResource(R.drawable.compose_bu);
				RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(Utils.px2Dp(context, 48),Utils.px2Dp(context, 48));
				rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
				addView(imgBtn,rlp);
				
				setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						mActionDialog.show();
					}
				});
			}
			
		} 
	}
	

	@Override
	public void onBackPressed(){
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.slide_out_right);
	}
}
