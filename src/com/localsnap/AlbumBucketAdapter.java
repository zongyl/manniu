package com.localsnap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.localsnap.BitmapCache.ImageCallback;
import com.manniu.manniu.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumBucketAdapter extends BaseAdapter {
	final String TAG = getClass().getSimpleName();

	Activity act;
	/**
	 * 相册适配置器
	 */
	List<AlbumBucket> dataList;
	BitmapCache cache;
	public List<Bitmap> listbit = new ArrayList<Bitmap>();
	
	ImageCallback callback = new ImageCallback() {
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap,
				Object... params) {
			try
			{
			if (imageView != null && bitmap != null && params!=null && params.length>0 && bitmap.getRowBytes()*bitmap.getHeight()>0/*此处需要这么写才能更好兼容Android版本*/) {
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag()) ) {
					((ImageView) imageView).setImageBitmap(bitmap);
					listbit.add(bitmap);
				} else {
					Log.e(TAG, "callback, bmp not match");
				}
			} else {
				Log.e(TAG, "callback, bmp null");
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	public AlbumBucketAdapter(Activity act, List<AlbumBucket> list) {
		this.act = act;
		dataList = list;
		Collections.reverse(this.dataList);
		if(cache == null)
			cache = new BitmapCache();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count = 0;
		if (dataList != null) {
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	class Holder {
		private ImageView iv;
		private ImageView selected;
		private TextView name;
		private TextView count;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		Holder holder;
		if (arg1 == null) {
			holder = new Holder();
			arg1 = View.inflate(act, R.layout.item_image_bucket, null);
			holder.iv = (ImageView) arg1.findViewById(R.id.image);
			holder.selected = (ImageView) arg1.findViewById(R.id.isselected);
			holder.name = (TextView) arg1.findViewById(R.id.name);
			holder.count = (TextView) arg1.findViewById(R.id.count);
			arg1.setTag(holder);
		} else {
			holder = (Holder) arg1.getTag();
		}
		AlbumBucket item = dataList.get(arg0);
		holder.name.setText(item.bucketName);
		holder.count.setText("" + item.count);
		holder.selected.setVisibility(View.GONE);
		if (item.imageList != null && item.imageList.size() > 0) {
			String thumbPath = item.imageList.get(0).thumbnailPath;
			String sourcePath = item.imageList.get(0).imagePath;
			holder.iv.setTag(sourcePath);
			//画相册时只用取第一个就可以了
			cache.displayBmp(holder.iv, thumbPath, sourcePath, callback);
		} else {
			holder.iv.setImageBitmap(null);
			Log.e(TAG, "no images in bucket " + item.bucketName);
		}
		return arg1;
	}

}
