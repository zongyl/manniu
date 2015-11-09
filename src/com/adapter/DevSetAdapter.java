package com.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bean.Menu;
import com.manniu.manniu.R;

/**
 * 设备设置 
 * @author pc
 *
 */
public class DevSetAdapter extends BaseAdapter{

	private static String TAG = "DevSetAdapter";
	
	private Context context;
	
	private List<Menu> items;
	
	LayoutInflater inflater;
	
	private int itemResId;

	public DevSetAdapter(Context _context, List<Menu> _items){
		this.context = _context;
		this.items = _items;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public DevSetAdapter(Context _context, List<Menu> _items, int resId){
		this.context = _context;
		this.items = _items;
		this.itemResId = resId;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	} 
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Log.d(TAG, "getViewgetViewgetView!");
		
		View rowView = null;
		TextView tv = null;
		ImageView im = null;
		TextView version = null;
		switch (itemResId) {
		case R.layout.new_device_set_item0:
			rowView = inflater.inflate(itemResId, null);
			tv = (TextView)rowView.findViewById(R.id.dev_item0_tv0);
			tv.setText(items.get(position).text);
			
			im = (ImageView)rowView.findViewById(R.id.dev_item0_ic0);
			im.setBackgroundResource(items.get(position).iconResid);		
			
			rowView.setOnTouchListener(new Listener(items.get(position).tag));
			
			break;
		case R.layout.new_device_set_item1:
			rowView = inflater.inflate(itemResId, null);
			tv = (TextView)rowView.findViewById(R.id.dev_item1_tv0);
			tv.setText(items.get(position).text);
			
			rowView.setOnTouchListener(new Listener(items.get(position).tag));
			break;
		default:
			break;
		}
		return rowView;
	}
	
	/**
	 * 事件处理
	 * @param tag
	 */
	private void onClick(String tag){

		if(Constants.DEV_SET_1.equals(tag)){
			Intent intent2 = new Intent(Intent.ACTION_VIEW);
	        intent2.setClassName(context, "com.views.About_MobilephoneActivity");  
	        context.startActivity(intent2);
		}else if(Constants.DEV_SET_2.equals(tag)){
			
		}else if(Constants.DEV_SET_3.equals(tag)){
			
		}else if(Constants.DEV_SET_4.equals(tag)){
			
		}else if(Constants.DEV_SET_5.equals(tag)){
			
		}else if(Constants.DEV_SET_6.equals(tag)){
			
		}else if(Constants.DEV_SET_7.equals(tag)){
			
		}else if(Constants.DEV_SET_8.equals(tag)){
			
		}else if(Constants.DEV_SET_9.equals(tag)){
			
		}else{
			
		}
	}

	/**
	 * 触摸事件监听
	 * @author zongyl
	 *
	 */
	class Listener implements OnTouchListener {
		
		private String tag;
		
		public Listener(String tag){
			this.tag = tag;
		}
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				v.setBackgroundResource(R.color.graywhite);
				break;
			case MotionEvent.ACTION_UP:
				v.setBackgroundResource(R.color.white);
				onClick(tag);
				break;
			case MotionEvent.ACTION_CANCEL:
				v.setBackgroundResource(R.color.white);
				break;
			default:
				break;
			}
			return true;
		}
		}
	
}