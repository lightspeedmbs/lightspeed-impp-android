package co.herxun.impp.utils;

import java.io.File;

import co.herxun.impp.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

public class ImageUtility {
	public static Bitmap getRoundedTopCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;
		final Rect topRightRect = new Rect(bitmap.getWidth() / 2, 0,
				bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		final Rect bottomRect = new Rect(0, bitmap.getHeight() / 2,
				bitmap.getWidth(), bitmap.getHeight());

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		// Fill in upper right corner
		canvas.drawRect(topRightRect, paint);
		// Fill in bottom corners
		canvas.drawRect(bottomRect, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap getRoundedBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Config.ARGB_8888);
		
		int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
		
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, size, size);
		final RectF rectF = new RectF(rect);
		final float roundPx = size;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
	
	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
		// TODO Auto-generated method stub
		if (scaleBitmapImage.getWidth() >= scaleBitmapImage.getHeight()){
			scaleBitmapImage = Bitmap.createBitmap(scaleBitmapImage, 
				scaleBitmapImage.getWidth()/2 - scaleBitmapImage.getHeight()/2,0,
				scaleBitmapImage.getHeight(),scaleBitmapImage.getHeight()
		    );
		}else{
			scaleBitmapImage = Bitmap.createBitmap(scaleBitmapImage,
				0,scaleBitmapImage.getHeight()/2 - scaleBitmapImage.getWidth()/2,
				scaleBitmapImage.getWidth(),scaleBitmapImage.getWidth() 
		    );
		}
		
		int size = Math.min(scaleBitmapImage.getWidth(), scaleBitmapImage.getHeight());
		Bitmap targetBitmap = Bitmap.createBitmap(size, size,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) size - 1) / 2,
				((float) size - 1) / 2,
				(Math.min(((float) size), ((float) size)) / 2),
				Path.Direction.CCW);

		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
				sourceBitmap.getHeight()), new Rect(0, 0, size,size), null);
		return targetBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Context ct, int resId,
			int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(ct.getResources(), resId, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(ct.getResources(), resId, options);
	}

	public static Bitmap decodeSampledBitmapFromResource(Context ct, int resId,
			int sampleSize) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(ct.getResources(), resId, options);

		// Calculate inSampleSize
		options.inSampleSize = sampleSize;

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(ct.getResources(), resId, options);
	}

	
//	public static File getFileTemp(Context ct, String fileName) {
//		File mFileTemp;
//		String state = Environment.getExternalStorageState();
//		if (Environment.MEDIA_MOUNTED.equals(state)) {
//			mFileTemp = new File(Environment.getExternalStorageDirectory()
//					+ "/Tiniroom");
//			mFileTemp.mkdirs();
//			mFileTemp = new File(Environment.getExternalStorageDirectory()
//					+ "/Tiniroom", fileName);
//		} else {
//			mFileTemp = new File(ct.getFilesDir() + "/Tiniroom");
//			mFileTemp.mkdirs();
//			mFileTemp = new File(ct.getFilesDir() + "/Tiniroom", fileName);
//		}
//		Log.e("createFile", mFileTemp.getAbsolutePath());
//		return mFileTemp;
//	}

	public static File getFileTemp(Context ct) {
		final String TEMP_PHOTO_FILE_NAME = ct.getString(R.string.app_name)+"temp_photo.png";
		File mFileTemp;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mFileTemp = new File(Environment.getExternalStorageDirectory(),
					TEMP_PHOTO_FILE_NAME);
		} else {
			mFileTemp = new File(ct.getFilesDir(), TEMP_PHOTO_FILE_NAME);
		}
		return mFileTemp;
	}
	
	public static void deleteTempFile(Context ct) {
		File mFileTemp = getFileTemp(ct);
		mFileTemp.delete();
	}

	public static Uri getUri() {
		String state = Environment.getExternalStorageState();
		if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
			return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

		return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getFilePathFromGallery(Context ct,Intent data){
		String IMAGE_FILEPATH = null;
		if (data != null && data.getData() != null) {
			Uri _uri = data.getData();
			// User had pick an image.
			Cursor cursor = ct.getContentResolver().query(_uri,new String[] { android.provider.MediaStore.Images.ImageColumns.DATA },null, null, null);
			if(cursor!=null && cursor.getCount()>0 ){
				cursor.moveToFirst();
			}
			// Link to the image
			IMAGE_FILEPATH = cursor.getString(0);
			cursor.close();
			
			if(IMAGE_FILEPATH!=null){
				//Log.e("startCrop", IMAGE_FILEPATH);
			}else{
				//Log.e("startCrop", "imageFilePath==null");
				if (_uri != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
				    try {
				        if( _uri == null ) {
				            IMAGE_FILEPATH = _uri.getPath();
				        } else {
				            // get the id of the image selected by the user
				            String wholeID = DocumentsContract.getDocumentId(data.getData());
				            String id = wholeID.split(":")[1];

				            String[] projection = { MediaStore.Images.Media.DATA };
				            String whereClause = MediaStore.Images.Media._ID + "=?";
				            Cursor cursor2 = ct.getContentResolver().query(ImageUtility.getUri(), projection, whereClause, new String[]{id}, null);
				            if( cursor2 != null ){
				                int column_index = cursor2.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				                if (cursor2.moveToFirst()) {
				                    IMAGE_FILEPATH = cursor2.getString(column_index);
				                }

				                cursor2.close();
				            } else {
				                IMAGE_FILEPATH = _uri.getPath();
				            }
				        }
				    } catch (Exception e) {
				        Log.e("Failed to get image",e.getMessage());
				    }
				}
			}
		}
		
		return IMAGE_FILEPATH;
	}
}