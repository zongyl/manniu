package com.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

/**
 * @author: li_jianhua Date: 2015-12-17 下午3:10:48
 * To change this template use File | Settings | File Templates.
 * Description：加载报警图片
 */
public class MsgImageLoader {
	private String TAG = "MsgImageLoader";
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
//    public static int countData = 0;
//    public static boolean _isRefresh = false;
    //FileCache fileCache;
    Handler handler = new Handler();//handler to display images in UI thread
    private Map<String, SoftReference<Bitmap>> imageCaches = new HashMap<String, SoftReference<Bitmap>>();
    
    public MsgImageLoader(Context context){
        executorService=Executors.newFixedThreadPool(5);
        //fileCache = new FileCache(context);
    }
    // 当进入listview时默认的图片，可换成你自己的默认图片
    final int stub_id=R.drawable.lock_bg1;
    public void DisplayImage(String url, ImageView imageView){
    	String tempUrl = url.substring(0,url.indexOf("?"));
    	SoftReference<Bitmap> currBitmap = imageCaches.get(getUrlKey(tempUrl));
		Bitmap softRefBitmap = null;
		if(currBitmap != null){
			softRefBitmap = currBitmap.get();
		}
		//先从软引用中拿数据
		if(currBitmap != null && softRefBitmap != null){
			imageView.setImageBitmap(softRefBitmap);
			//System.out.println("2222222222222222222");
		}else{
			imageViews.put(imageView, url);
	        queuePhoto(url, imageView);
	        imageView.setImageResource(stub_id);
		}
    }
        
    private void queuePhoto(String url, ImageView imageView){
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    //最后从指定的url中下载图片
    private Bitmap getBitmap(String url){
    	Bitmap bitmap=null;
        try {
        	if(url.startsWith("http")){
        		String tempUrl = url.substring(0,url.indexOf("?"));
    			File file = new File(MsgAdapter2.alarmPath + tempUrl.substring(tempUrl.lastIndexOf("/")+1,tempUrl.length()));
    			if(file.exists()){
    				bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
    				System.out.println("sdk..."+getUrlKey(tempUrl));
    				//将读取的数据放入到软引用中
    				if(imageCaches.get(getUrlKey(tempUrl)) == null){
    					imageCaches.put(getUrlKey(tempUrl), new SoftReference<Bitmap>(bitmap));
    				}
    				return bitmap;
    			}else{
    				byte[] bytes = HttpUtil.executeGetBytes(url);
    				//存入SDK文件
    				//FileUtil.toFile(bytes, MsgAdapter2.alarmPath + tempUrl.substring(tempUrl.lastIndexOf("/")+1,tempUrl.length()));
    				BitmapUtils.saveBitmap2(getBitmapByBytes(bytes), MsgAdapter2.alarmPath + tempUrl.substring(tempUrl.lastIndexOf("/")+1,tempUrl.length()));
    				bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
    				System.out.println("url..."+getUrlKey(tempUrl));
    				//将读取的数据放入到软引用中
    				if(imageCaches.get(getUrlKey(tempUrl)) == null){
    					imageCaches.put(getUrlKey(tempUrl), new SoftReference<Bitmap>(bitmap));
    				}
    				//bitmap = getBitmapByBytes(bytes);
    				return bitmap;
    			}
    		}
        } catch (Exception ex){
        	LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(ex));
        }
       return null;
    }
    
    public String getUrlKey(String str){
		str = str.substring(str.lastIndexOf("/")+1,str.length());
		str = str.substring(0, str.indexOf("."));
		return str;
	}
 	
 	/** 
 	 * 根据图片字节数组，对图片可能进行二次采样，不致于加载过大图片出现内存溢出 
 	 * @param bytes 
 	 * @return 
 	 */  
 	public Bitmap getBitmapByBytes(byte[] bytes){  
 	    //对于图片的二次采样,主要得到图片的宽与高  
 		try {
 			BitmapFactory.Options options = new BitmapFactory.Options();  
 	 	    options.inJustDecodeBounds = true;  //仅仅解码边缘区域  
 	 	    //如果指定了inJustDecodeBounds，decodeByteArray将返回为空  
 	 	    BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);  
 	 	    // 找到正确的刻度值，它应该是2的幂。
 			final int REQUIRED_SIZE = 70;
 			int width_tmp = options.outWidth, height_tmp = options.outHeight;
 			int scale = 1;
 			while (true) {
 				if (width_tmp / 2 < REQUIRED_SIZE
 						|| height_tmp / 2 < REQUIRED_SIZE)
 					break;
 				width_tmp /= 2;
 				height_tmp /= 2;
 				scale *= 2;
 			}
 	 	  
 	 	    //不再只加载图片实际边缘  
 	 	    options.inJustDecodeBounds = false;  
 	 	    //并且制定缩放比例  
 	 	    options.inSampleSize = scale;  
 	 	    return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);  
		} catch (Exception e) {
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
				LogUtil.e(TAG,"handler error....");
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
			try {
				if (imageViewReused(photoToLoad))
					return;
//				countData ++;
//				if(countData > 5){
//					_isRefresh = true;
//					countData = 0;
//				}
				System.out.println("111111111111111111111111111111111");
				if (bitmap != null){
					photoToLoad.imageView.setImageBitmap(bitmap);
				}else
					photoToLoad.imageView.setImageResource(stub_id);
				
			} catch (Exception e) {
				LogUtil.e(TAG,"BitmapDisplayer error....");
			}
		}
	}

}
