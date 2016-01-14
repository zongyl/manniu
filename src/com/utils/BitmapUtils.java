package com.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import P2P.SDK;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.widget.ImageView;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.views.XViewBasic;


public class BitmapUtils extends XViewBasic { 
	private static String TAG ="BitmapUtils";
    public BitmapUtils(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
	}
    public static Context getContext(){
    	return ACTs;
    }
	/** 
    * 加载本地图片 
    * @param url 
    * @return 
    */  
	public static String path =Environment.getExternalStorageDirectory().getAbsolutePath()+"/photos/";
	public static Uri getImageFromCamer(Context context,String ImageFileName, int REQUE_CODE_CAMERA) {
	    if(ImageFileName==null||"".equals(ImageFileName)){
	    	return null;
	    }
	    //File cameraFile = new File(ImageFileName);
		ContentValues values = new ContentValues();
		values.put(Media.TITLE, ImageFileName);
	    Uri imageFileUri = context.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
	    Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageFileUri);
	    ((Activity) context).startActivityForResult(i, REQUE_CODE_CAMERA);
	    return  imageFileUri;
	}
	public static void getImageFromPhoto(Context context, int REQUE_CODE_PHOTO) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        ((Activity) context).startActivityForResult(intent, REQUE_CODE_PHOTO);
 
    }
	
	/**
	 *以最省内存的方式读取本地资源的图片
	 * @return
	 */
	public static Bitmap getBitMap(String path){
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			bitmap = BitmapFactory.decodeFile(path, opt);
		} catch (Exception e) {
		}
		return bitmap;
	}
	
	public static void startPhotoZoom(Context context, Uri uri,
            int REQUE_CODE_CROP) {
        int  dp = 400;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);//输出是X方向的比例
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高，切忌不要再改动下列数字，会卡死
        intent.putExtra("outputX", dp);//输出X方向的像素
        intent.putExtra("outputY", dp);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);//设置为不返回数据
        ((Activity) context).startActivityForResult(intent, REQUE_CODE_CROP);
    }
	
	public static void loadImage(String imgname,String userid,ImageView imagetarget){
		try {
			if(imgname.equals("")){
				if(Constants.userid.equals("")) return;
				imgname = "images/users/"+Constants.userid +".jpg";
			}
			if(getContext().getResources().getString(R.string.default_photo).equals(imgname)){
				imagetarget.setImageResource(R.drawable.images_nophoto_bg);//默认头像
			}else{
//				String temp = replace(imgname);
//				String [] strs =temp.split("/");
//				String ImageName  = strs[2];
//				File tempfile = new File(BitmapUtils.getPath(),ImageName);
//				if(!tempfile.exists()){
					//Log.v("headimage original", "from http");
					/*String image_url = getContext().getResources().getString(R.string.server_address)+"/"+replace(imgname);
					AsyncImageLoader.setImageViewFromUrl(image_url, imagetarget,userid);*/
					String url=com.utils.Constants.hostUrl+File.separator+imgname;
					DisplayImageOptions options = new DisplayImageOptions.Builder()
					.showImageForEmptyUri(R.drawable.images_nophoto_bg)//没有图片资源时的默认图片 
					.showImageOnFail(R.drawable.event_list_fail_pic)//加载失败时的图片  
					.cacheOnDisk(false)								//启用外存缓存
					.cacheInMemory(false)                           //启用内存缓存 
					.resetViewBeforeLoading(false)
					.imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.considerExifParams(true)						//启用EXIF和JPEG图像格式
					//.displayer(new FadeInBitmapDisplayer(300))
					.build();
					ImageLoader imageloader = ImageLoader.getInstance();
					imageloader.displayImage(url, imagetarget, options,null);
					//Log.v(TAG, imageloader.getDiskCache().get(url)+"");
//				}else{
//					Log.v("headimage original", "from SD card and path is:"+tempfile.getAbsolutePath());
//					Bitmap bt = BitmapFactory.decodeFile(BitmapUtils.getPath()+ImageName);//从Sd中找头像，转换成Bitmap
//					imagetarget.setImageBitmap(bt);
//				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	//反斜杠处理
	public static String replace(String a){
		
		return a.replaceAll("\\\\","/");
	}
	
	public static Bitmap getBitmapFromUri(Context mContext,Uri uri)
    {
	     try
	     {
	      // 读取uri所在的图片
		      Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
		      return bitmap;
	     }
	     catch (Exception e)
	     {
		      e.printStackTrace();
		      return null;
	     }
    }
	
    //创建文件目录
    public static String getPath(){
    	return path;
    }
    
    /**保存图片
     * @param percent为100分比*/
	public static File CompressToFile(Bitmap bitmap, String filename,int percent) {
		File fileDir = new File(path);
		if(!fileDir.exists()){
			fileDir.mkdir();
		}else{
			File file = new File(path,filename);
			if(file.isFile() && file.exists()){
				file.delete();
			}
		}
		File imagefile = new File(path,filename);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, percent, baos);

		InputStream sbs = new ByteArrayInputStream(baos.toByteArray());
		OutputStream os = null;
		try {
			os = new FileOutputStream(imagefile);
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			try {
				while ((bytesRead = sbs.read(buffer, 0, 1024)) != -1)
					os.write(buffer, 0, bytesRead);
				os.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try {
				os.close();
				sbs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		Log.v(TAG, imagefile.getAbsolutePath());
		return imagefile;
	}
   	
	/**销毁头像文件(缓存)*/
	public static void clearCache(String filename){
		File fileDir = new File(path);
		if(fileDir.exists()){
			File file = new File(path,filename);
			if(file.isFile() && file.exists()){
				file.delete();
			}
		}
	}
   	/** 保存方法 */
   	public static void saveBitmap(Bitmap bitmap, String filename) {
   		if(bitmap==null && bitmap.getRowBytes()*bitmap.getHeight()>0/*此处需要这么写才能更好兼容Android版本*/)
   		{
   			return;
   		}
   		File f = new File(filename);
   		if (f.exists()) {
   			f.delete();
   		}
   		try {
   			FileOutputStream out = new FileOutputStream(f);
   			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
   			out.flush();
   			out.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
   	}
   	public static void saveBitmap2(Bitmap bitmap, String filename) {
   		File f = new File(filename);
   		if (f.exists()) {
   			f.delete();
   		}
   		try {
   			FileOutputStream out = new FileOutputStream(f);
   			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
   			out.flush();
   			out.close();
   		} catch (FileNotFoundException e) {
   			e.printStackTrace();
   		} catch (IOException e) {
   			e.printStackTrace();
   		}
   	}
	public static void rename(String iMAGE_FILE_NAME, String _UserId) {
		File file =new File(path,iMAGE_FILE_NAME);
		if(file.exists()){
			file.renameTo(new File(path,_UserId+".jpg"));
		}
	}
	
	
	/**软解码获取当前帧的Bitmap*/
	public static Bitmap getScreenBitmap(byte[] data,byte[] outBytes,int len){
		Bitmap bmp = null;
		try {
			byte[] bmpBuff = null;
			ByteBuffer byteBuffer = null;
			if(outBytes==null)
				return null;
			int nRet = SDK.ScreenShots(data, len,outBytes);
			if(nRet > 0){
				int width_frame = SDK._width;
				int height_frame = SDK._height;
				if (width_frame > 0 && height_frame > 0) {
					bmpBuff = new byte[width_frame * height_frame * 3];

					bmp = Bitmap.createBitmap(width_frame,
							height_frame,
							android.graphics.Bitmap.Config.RGB_565);

					if (bmpBuff != null) {
						System.arraycopy(outBytes, 0, bmpBuff, 0,len);
						byteBuffer = ByteBuffer.wrap(outBytes);
						bmp.copyPixelsFromBuffer(byteBuffer);
					}
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return bmp;
	}
	
	
}





