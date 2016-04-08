package com.localsnap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.localmedia.ImageLoader;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-8-7 上午8:46:46
 * To change this template use File | Settings | File Templates.
 * Description： 图片 gridview 适配器
 */
public class ImageGridAdapter extends BaseAdapter {

	private TextCallback textcallback = null;
	final String TAG = getClass().getSimpleName();
	Activity act;
	public List<ImageItem> dataList;
	public Map<String, String> map = null;
	public List<Holder> holderList = null;
	//BitmapCache cache;
	public ImageLoader imageLoader; //用来下载图片的类
	int index;
	//private Handler mHandler;
	private int selectTotal = 0;
	public List<Bitmap> grid_listbit = new ArrayList<Bitmap>();
	/*
	ImageCallback callback = new ImageCallback() {
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap,
				Object... params) {
			if (imageView != null && bitmap != null) {
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag())) {
					((ImageView) imageView).setImageBitmap(bitmap);
					listbit.add(bitmap);
				} else {
					Log.e(TAG, "callback, bmp not match");
				}
			} else {
				Log.e(TAG, "callback, bmp null");
			}
		}
	};*/

	//选择图片回调事件接口
	public static interface TextCallback {
		public void onListen(int count,String path);
	}

	public void setTextCallback(TextCallback listener) {
		textcallback = listener;
	}

	public ImageGridAdapter(Activity act, List<ImageItem> list,int index) {
		imageLoader = new ImageLoader(act.getApplicationContext());
		this.act = act;
		dataList = list;
//		if(cache == null)
//			cache = new BitmapCache();
		if(map == null){
			map = new HashMap<String, String>();
		}
		if(holderList == null){
			holderList = new ArrayList<Holder>();
		}
		this.index = index;
	}

	@Override
	public int getCount() {
		int count = 0;
		if (dataList != null) {
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class Holder {
		private ImageView iv;
		private ImageView selected;
		private TextView text;
		
		public ImageView getIv() {
			return iv;
		}
		public void setIv(ImageView iv) {
			this.iv = iv;
		}
		public ImageView getSelected() {
			return selected;
		}
		public void setSelected(ImageView selected) {
			this.selected = selected;
		}
		public TextView getText() {
			return text;
		}
		public void setText(TextView text) {
			this.text = text;
		}
	}
	public int _nClickedCount = 0;
	public View _lastClieckView = null;
//	public MyHandler _handler = new MyHandler();
	public final static int DOUBLE_CLICKED = 2;
	//int mCount = 0;
	//String tem = "";
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder holder;
//		if (position == 0) {
//			mCount++;
//		} else {
//			mCount = 0;
//		}
//		if (mCount > 1) {
//			System.out.println(" position1 = " + position+"--"+convertView);
//		}
		if (convertView == null) {
			holder = new Holder();
			convertView = View.inflate(act, R.layout.item_image_grid, null);
			holder.iv = (ImageView) convertView.findViewById(R.id.image);
			holder.selected = (ImageView) convertView
					.findViewById(R.id.isselected);
			holder.text = (TextView) convertView
					.findViewById(R.id.item_image_grid_text);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		final ImageItem item = dataList.get(position);
		holder.iv.setTag(item.imagePath);
		//这里是画相册里面的图片-默认的相册参数置空
		//cache.displayBmp(holder.iv, "", item.imagePath,callback);
		Bitmap bit = imageLoader.DisplayImage2(item.imagePath, holder.iv);
		grid_listbit.add(bit);
				
		if (item.isSelected) {
			holder.selected.setImageResource(R.drawable.icon_data_select);  
			holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
		} else {
			holder.selected.setImageResource(-1);
			holder.text.setBackgroundColor(0x00000000);
		}
		if(index == 1){
			holderList.add(holder);
		}
			
		holder.iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					String path = dataList.get(position).imagePath;
					if(index == 0){
						Intent intent=new Intent(Intent.ACTION_VIEW);
						intent.putExtra("position", position);
						intent.setClassName(Fun_ImgGridActivity.instance.context, "com.localsnap.BrowseAlbumActivity"); 
						Fun_ImgGridActivity.instance.startActivity(intent);
					}else{
						item.isSelected = !item.isSelected;
						if (item.isSelected) {
							holder.selected.setImageResource(R.drawable.icon_data_select);
							holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
							selectTotal++;
							if (textcallback != null)
								textcallback.onListen(selectTotal,path);
							map.put(path, path);

						} else if (!item.isSelected) {
							holder.selected.setImageResource(0);
							holder.text.setBackgroundColor(0x00000000);
							selectTotal--;
							if (textcallback != null)
								textcallback.onListen(selectTotal,path);
							map.remove(path);
						}
					}
				} catch (Exception e) {
					LogUtil.e("ImageGridAdapter",ExceptionsOperator.getExceptionInfo(e));
				}
			}
		});
		return convertView;
	}
	
	/*//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOUBLE_CLICKED:
				onSelected(msg,_nClickedCount,msg.arg1);
				_nClickedCount = 0;
				_lastClieckView = null;
				break;
			}
		}
	}
	
	// 处理选择事件
	public  void onSelected(Message msg,int nClickedCount,int position) {
		Holder holder = (Holder) msg.obj;
		String path = dataList.get(position).imagePath;
		ImageItem item = dataList.get(position);
		// 处理双击事件
		if(2 == nClickedCount){
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setClassName(Fun_ImgGridActivity.instance.context, "com.localsnap.BrowseAlbumActivity"); 
			Fun_ImgGridActivity.instance.startActivity(intent);
            
		}else{
			item.isSelected = !item.isSelected;
			if (item.isSelected) {
				holder.selected.setImageResource(R.drawable.icon_data_select);
				holder.text.setBackgroundResource(R.drawable.bgd_relatly_line);
				selectTotal++;
				if (textcallback != null)
					textcallback.onListen(selectTotal,path);
				map.put(path, path);

			} else if (!item.isSelected) {
				holder.selected.setImageResource(0);
				holder.text.setBackgroundColor(0x00000000);
				selectTotal--;
				if (textcallback != null)
					textcallback.onListen(selectTotal,path);
				map.remove(path);
			}
		}

	}*/
}
