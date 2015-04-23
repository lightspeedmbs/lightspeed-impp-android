package co.herxun.impp.imageloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.herxun.impp.utils.ImageUtility;
import co.herxun.impp.utils.Utils;

import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
    
    MemoryCache memoryCache=new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, HashMap<String,Object>> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, HashMap<String,Object>>());
    ExecutorService executorService;
    Handler handler=new Handler();//handler to display images in UI thread
    private Integer defaultDrawable ;
    
    public static ImageLoader sImageLoader;
    private Context ct;
    
    public ImageLoader(Context context){
    	ct = context;
        fileCache=new FileCache(context);
        executorService=Executors.newFixedThreadPool(5);
    }
    
    public static ImageLoader getInstance(Context ct){
    	if(sImageLoader==null){
    		sImageLoader = new ImageLoader(ct);
    	}
    	return sImageLoader;
    }
    
    public void DisplayImage(String url, ImageView imageView,Integer defaultDrawableId,boolean roundCorner){
    	defaultDrawable=defaultDrawableId;
    	HashMap<String,Object> item = new HashMap<String,Object>();
    	item.put("url", url);
    	item.put("defaultDrawableId", defaultDrawableId);
        imageViews.put(imageView, item);
        
        Bitmap bitmap=memoryCache.get(url);
        if(bitmap!=null){
        	if(roundCorner){
        		bitmap = ImageUtility.getRoundedShape(bitmap);
        	}
            imageView.setImageBitmap(bitmap);
        }else{
            queuePhoto(url, imageView, defaultDrawableId,roundCorner);
            if(defaultDrawableId!=null) imageView.setImageResource(defaultDrawableId);
        }
    }

    private void queuePhoto(String url, ImageView imageView,Integer defaultDrawable,boolean roundCorner)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView,defaultDrawable,roundCorner);
        executorService.submit(new PhotosLoader(p));
    }
    
    private Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b!=null)
            return b;
        
        //from web
        try {
            Bitmap bitmap=null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            copyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1=new FileInputStream(f);
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
            FileInputStream stream2=new FileInputStream(f);
            Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public Integer defaultDrawable;
        public ImageView imageView;
        public boolean roundCorner;
        public PhotoToLoad(String u, ImageView i,Integer d,boolean roundCorner){
            url=u; 
            imageView=i;
            defaultDrawable=d;
            this.roundCorner = roundCorner;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            try{
                if(imageViewReused(photoToLoad))
                    return;
                Bitmap bmp=getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                if(imageViewReused(photoToLoad))
                    return;
                BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=(String) imageViews.get(photoToLoad.imageView).get("url");
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
        	bitmap=b;
        	photoToLoad=p;
        }
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null){
            	if(photoToLoad.roundCorner){
            		bitmap = ImageUtility.getRoundedShape(bitmap);
            	}
                photoToLoad.imageView.setImageBitmap(bitmap);
            }else if(photoToLoad.defaultDrawable!=null){
            	photoToLoad.imageView.setImageResource(photoToLoad.defaultDrawable);
            }else{
            	photoToLoad.imageView.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }
    public void copyStream(InputStream is, OutputStream os){
        final int buffer_size=1024;
        try{
            byte[] bytes=new byte[buffer_size];
            for(;;){
              int count=is.read(bytes, 0, buffer_size);
              if(count==-1)
                  break;
              os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}
