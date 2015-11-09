package com.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.manniu.manniu.R;
import com.views.XViewBasic;

public class AsyncImageLoader extends XViewBasic {  
	final static String TAG="AsyncImageLoader";
	public AsyncImageLoader(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		// TODO Auto-generated constructor stub
	}
	
	
	public static Context getContext(){
		return ACTs;
	}

	
	static String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/photos/";
    static ImageView singImageView; //针对于单张图片异步加载   
	private static HashMap<String, SoftReference<Drawable>> singleImageCache = null;  
    
	
   /** 
    * 通过图片地址,返回drawable 
    * @param url 
    * @return 
    */  
   public static Drawable loadImageFromUrl(String url,String userId) {  
       
	   ByteArrayOutputStream out = null;  
       Drawable drawable = null;  
       int BUFFER_SIZE = 1024*16;  
       InputStream inputStream = null;  
       try{  
           inputStream = new URL(url).openStream();  
           BufferedInputStream in = new BufferedInputStream(inputStream, BUFFER_SIZE);  
           out = new ByteArrayOutputStream(BUFFER_SIZE);  
           int length = 0;  
           byte[] tem = new byte[BUFFER_SIZE];  
           length = in.read(tem);  
           while(length != -1){  
               out.write(tem, 0, length);  
               length = in.read(tem);  
           }  
           in.close();  
           byte[] buf = out.toByteArray();
           Bitmap bt = BitmapFactory.decodeByteArray(buf, 0, buf.length);
           saveTolocal(bt,userId);
           BitmapDrawable bd = new BitmapDrawable(bt);
           drawable = (Drawable)bd;
           //drawable = Drawable.createFromStream(new ByteArrayInputStream(out.toByteArray()), "src");  
          
       }catch(Exception e){  
           e.printStackTrace();  
       }finally{  
           if(inputStream != null){  
               try{  
                   inputStream.close();  
               }catch(Exception e){
            	   e.printStackTrace();
               }  
           }  
       }  
       return drawable;  
   }  
       
   public static void saveTolocal(Bitmap bitmap,String userId){
	   Log.v(TAG, "begin to save to local");
	   String sdStatus = Environment.getExternalStorageState();  
       if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用  
              return;  
          }  
       FileOutputStream b = null;
       
       File file =new File(path);
       if(!file.exists()){
    	   file.mkdirs();
       }
       File filename = new File(path,userId+".jpg");
       
       if(filename.exists()){
    	   return;   
       }
       Log.v(TAG, "path of path save to local:"+filename.getAbsolutePath());
       try {
           b = new FileOutputStream(filename);
           bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件,文件名为fileName
            
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } finally {
           try {
               //关闭流
               b.flush();
               b.close();
           } catch (IOException e) {
               e.printStackTrace();
           }

       }
   }
    /** 
     * 异步设置单张imageview图片,采取软引用 
    * @param url 网络图片地址 
    * @param imageView 需要设置的imageview 
    */  
   public static void setImageViewFromUrl(final String url, final ImageView imageView,final String userid){  
       singImageView = imageView;  
       //如果软引用为空,就新建一个   
       if(singleImageCache == null){  
           singleImageCache = new HashMap<String, SoftReference<Drawable>>();  
       }  
       //如果软引用中已经有了相同的地址,就从软引用中获取   
       if(singleImageCache.containsKey(url)){  
           SoftReference<Drawable> soft = singleImageCache.get(url);  
           Drawable draw = soft.get();  
           singImageView.setImageDrawable(draw);  
           return;  
       }  
       final Handler handler = new Handler(getContext().getMainLooper()){  
           @Override  
           public void handleMessage(Message msg) {  
        	   super.handleMessage(msg);
               singImageView.setImageDrawable((Drawable)msg.obj);  
           }  
       }; 
        new Thread(){  
            public void run() {  
                Drawable drawable = loadImageFromUrl(url,userid);  
                if(drawable == null){  
                   Log.e("single imageview", "single imageview of drawable is null,and we take the failure picture"); 
                   drawable =  getContext().getResources().getDrawable(R.drawable.event_list_fail_pic);
                   singleImageCache.put(url, new SoftReference<Drawable>(drawable));  
                    
                }else{  
                    //把已经读取到的图片放入软引用   
                    singleImageCache.put(url, new SoftReference<Drawable>(drawable));  
                }  
                Message message = handler.obtainMessage(0, drawable);  
                handler.sendMessage(message);  
            };  
        }.start();  
    }  
}