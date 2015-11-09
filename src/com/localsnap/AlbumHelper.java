package com.localsnap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import android.content.ContentResolver;
import android.content.Context;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-8-7 上午8:56:46
 * To change this template use File | Settings | File Templates.
 * Description： 相册专辑列表
 */

public class AlbumHelper {
	final String TAG = getClass().getSimpleName();
	Context context;
	ContentResolver cr;

	// 缩略图列表
	//public HashMap<String, String> thumbnailList = null;
	// 专辑列表
	//public List<HashMap<String, String>> albumList = null;
	//文件夹列表
	public HashMap<String, AlbumBucket> bucketList = null;

	private static AlbumHelper instance;

	private AlbumHelper() {
//		if(bucketList == null){
//			bucketList = new HashMap<String, AlbumBucket>();
//		}
	}

	public static AlbumHelper getHelper() {
		if (instance == null) {
			instance = new AlbumHelper();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.context == null) {
			this.context = context;
			cr = context.getContentResolver();
		}
		if(bucketList == null){
			bucketList = new HashMap<String, AlbumBucket>();
		}
//		if(albumList == null){
//			albumList = new ArrayList<HashMap<String, String>>();
//		}
//		if(thumbnailList == null){
//			thumbnailList = new HashMap<String, String>();
//		}
	}
	/**
	 * 是否创建了图片集
	 */
	//boolean hasBuildImagesBucketList = false;
	
	/**
	 * 得到图片集
	 */
	public void buildImagesBucketList(String path) {
		//获取所有的文件夹
		File[] files = new File(path).listFiles();
		for(File file: files){
			if(file.isDirectory() && file.listFiles().length > 0){
				AlbumBucket bucket = new AlbumBucket();
				bucket.imageList = new ArrayList<ImageItem>();
				bucket.bucketName = file.getName();
				File[] imgFiles = new File(file.getPath()).listFiles();
				List<ImageItem> imglist = new ArrayList<ImageItem>();
				for(int i = imgFiles.length - 1; i > -1; --i){
					bucket.count++;
					ImageItem imageItem = new ImageItem();
					imageItem.imageId = "001";
					imageItem.imagePath = imgFiles[i].getPath();
					imageItem.thumbnailPath = imgFiles[0].getPath();
					imglist.add(imageItem);
					bucket.imageList = imglist;
					bucketList.put(file.getName(), bucket);
				}
			}else{
				if(bucketList != null){
					bucketList.remove(file.getName());
					File f = new File(path+file.getName());
					if(f.isDirectory())
						f.delete();
				}
			}
		}
		//hasBuildImagesBucketList = true;
	}

	/**
	 * 得到图片集
	 * @param refresh
	 * @return
	 */
	public List<AlbumBucket> getImagesBucketList(String path) {
		File file = new File(path);
		List<AlbumBucket> tmpList = new ArrayList<AlbumBucket>();
		if(file.isDirectory()){
			buildImagesBucketList(path);
			Iterator<Entry<String, AlbumBucket>> itr = bucketList.entrySet()
					.iterator();
			while (itr.hasNext()) {
				Map.Entry<String, AlbumBucket> entry = (Map.Entry<String, AlbumBucket>) itr
						.next();
				tmpList.add(entry.getValue());
			}
			
		}
		return tmpList;
	}

}
