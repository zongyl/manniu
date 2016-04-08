package com.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * 图片异步下载类，包括图片的软应用缓存以及将图片存放到SDCard或者文件中
 *
 */
public class ImageDownloader {
	
	public static int flag=0;
	private static final String TAG = "ImageDownloader";
	private HashMap<String, MyAsyncTask> map = new HashMap<String, MyAsyncTask>();
	private Map<String, SoftReference<Bitmap>> imageCaches = new HashMap<String, SoftReference<Bitmap>>();
	/**
	 * 
	 * @param url 该mImageView对应的url
	 * @param mImageView
	 * @param path 文件存储路径  pic_thumb 缩略图片 
	 * @param mActivity
	 * @param download OnImageDownload回调接口，在onPostExecute()中被调用
	 */
	public void imageDownload(String url,ImageView mImageView,String pic_thumb,OnImageDownload download){
		String tempUrl = url.substring(0,url.indexOf("?"));
		//pic_local = MsgAdapter2.alarmPath + tempUrl.substring(tempUrl.lastIndexOf("/")+1,tempUrl.length());
		if(pic_thumb != null && !pic_thumb.equals("") && !pic_thumb.equals("NoSuchKey")){
			url = pic_thumb;
		}else{
			mImageView.setImageResource(R.drawable.lock_bg1);
			mImageView.setVisibility(View.VISIBLE);
			return;
		}
		SoftReference<Bitmap> currBitmap = imageCaches.get(getUrlKey(tempUrl));
		Bitmap softRefBitmap = null;
		if(currBitmap != null){
			softRefBitmap = currBitmap.get();
		}
		//先从软引用中拿数据
		if(currBitmap != null && mImageView != null && softRefBitmap != null && url.equals(mImageView.getTag())){
			System.out.println("从软引用中拿数据--imageName==" + tempUrl);
			mImageView.setImageBitmap(softRefBitmap);
			mImageView.setVisibility(View.VISIBLE);
		}
		else if(mImageView != null && url.equals(mImageView.getTag())){
			//软引用中没有，从文件中拿数据
			//Bitmap bitmap = getBitmapFromFile(pic_local);
			Bitmap bitmap = null;
			File file = new File(MsgAdapter2.alarmPath + tempUrl.substring(tempUrl.lastIndexOf("/")+1,tempUrl.length()));
			if(file.exists()){
				bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
				mImageView.setImageBitmap(bitmap);
				mImageView.setVisibility(View.VISIBLE);
				//将读取的数据放入到软引用中
				imageCaches.put(getUrlKey(tempUrl), new SoftReference<Bitmap>(bitmap));
			}
			//文件中也没有，此时根据mImageView的tag，即url去判断该url对应的task是否已经在执行，如果在执行，本次操作不创建新的线程，否则创建新的线程。
			else if(url != null && needCreateNewTask(mImageView)){
				MyAsyncTask task = new MyAsyncTask(url, mImageView, MsgAdapter2.alarmPath + tempUrl.substring(tempUrl.lastIndexOf("/")+1,tempUrl.length()),download);
				if(mImageView != null){
					Log.i(TAG, "执行MyAsyncTask --> " + flag);
					flag++;
					task.execute();
					//将对应的url对应的任务存起来
					map.put(url, task);
				}
			}
		}
	}
	
	/**
	 * 判断是否需要重新创建线程下载图片，如果需要，返回值为true。
	 * @param url
	 * @param mImageView
	 * @return
	 */
	private boolean needCreateNewTask(ImageView mImageView){
		boolean b = true;
		if(mImageView != null){
			String curr_task_url = (String)mImageView.getTag();
			if(isTasksContains(curr_task_url)){
				b = false;
			}
		}
		return b;
	}
	
	/**
	 * 检查该url（最终反映的是当前的ImageView的tag，tag会根据position的不同而不同）对应的task是否存在
	 * @param url
	 * @return
	 */
	private boolean isTasksContains(String url){
		boolean b = false;
		if(map != null && map.get(url) != null){
			b = true;
		}
		return b;
	}
	
	/**
	 * 删除map中该url的信息，这一步很重要，不然MyAsyncTask的引用会“一直”存在于map中
	 * @param url
	 */
	private void removeTaskFormMap(String url){
		if(url != null && map != null && map.get(url) != null){
			map.remove(url);
			System.out.println("当前map的大小=="+map.size());
		}
	}
	
	/**
	 * 从文件中拿图片
	 * @param mActivity 
	 * @param imageName 图片名字
	 * @param path 图片路径
	 * @return
	 */
	private Bitmap getBitmapFromFile(String pic_local){
		Bitmap bitmap = null;
		if(pic_local != null){
			File file = null;
			try {
				file = new File(pic_local);
				if(file.exists())
					bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			} catch (Exception e) {
				e.printStackTrace();
				bitmap = null;
			}
		}
		return bitmap;
	}
	
	public String getUrlKey(String str){
		str = str.substring(str.lastIndexOf("/")+1,str.length());
		str = str.substring(0, str.indexOf("."));
		return str;
	}
	
	/**
	 * 异步下载图片的方法
	 */
	private class MyAsyncTask extends AsyncTask<String, Void, Bitmap>{
		private ImageView mImageView;
		private String url;
		private OnImageDownload download;
		private String pic_local;
		
		public MyAsyncTask(String url,ImageView mImageView,String pic_local,OnImageDownload download){
			this.mImageView = mImageView;
			this.url = url;
			this.pic_local = pic_local;
			this.download = download;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap data = null;
			try{
			byte[] bytes = HttpUtil.executeGetBytes(url);
			//data = getBitmapByBytes(bytes);
			data = BitmapUtils.parseByte2Bitmap(bytes);
			String tempUrl = url.substring(0,url.indexOf("?"));
			imageCaches.put(getUrlKey(tempUrl), new SoftReference<Bitmap>(data.createScaledBitmap(data, 100, 100, true)));
			if(data!=null)
				BitmapUtils.saveBitmap2(data, pic_local);//保存到本地
			}catch(Exception e){
				e.printStackTrace();
			}
			return data;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			//回调设置图片
			if(download != null){
				download.onDownloadSucc(result,url,mImageView);
				//该url对应的task已经下载完成，从map中将其删除
				removeTaskFormMap(url);
			}
			super.onPostExecute(result);
		}
		
	}
}
