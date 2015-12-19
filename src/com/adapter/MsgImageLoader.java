package com.adapter;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.ExceptionsOperator;
import com.utils.FileUtil;
import com.utils.LogUtil;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * @author: li_jianhua Date: 2015-12-17 下午3:10:48
 * To change this template use File | Settings | File Templates.
 * Description：加载报警图片
 */
public class MsgImageLoader {
	public static String TAG = "DevImageLoader";
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    Handler handler = new Handler();//handler to display images in UI thread
    
    public MsgImageLoader(Context context){
        executorService=Executors.newFixedThreadPool(4);
    }
    // 当进入listview时默认的图片，可换成你自己的默认图片
    final int stub_id=R.drawable.lock_bg1;
    public void DisplayImage(String url, ImageView imageView){
        imageViews.put(imageView, url);
        queuePhoto(url, imageView);
        imageView.setImageResource(stub_id);
    }
        
    private void queuePhoto(String url, ImageView imageView){
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    //最后从指定的url中下载图片
    private Bitmap getBitmap(String url){
    	Bitmap bitmap=null;
        try {
//        	if(url.startsWith("http")){
//    			String name = url.substring(url.indexOf("aliyuncs.com")+12, url.length());
//    			File file = new File(DevAdapter.rootPath + devSid + name);
//    			//Log.d(TAG, devSid+"--"+name);
//    			if(file.exists()){
//    				bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
//    				return bitmap;
//    			}else{
    				byte[] bytes = HttpUtil.executeGetBytes(url);
    				//FileUtil.toFile(bytes, DevAdapter.rootPath + devSid + name);
    				//bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
    				bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    				return bitmap;
//    			}
//    		}
        } catch (Exception ex){
        	LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(ex));
        }
       return null;
    }
    
    //Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		//public String devSid;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url);
				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}
    /**
     * 防止图片错位
     * @param photoToLoad
     * @return
     */
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    /**
     *  用于在UI线程中更新界面
     *
     */
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

}
