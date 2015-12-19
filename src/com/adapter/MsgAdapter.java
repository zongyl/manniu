package com.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.basic.APP;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.views.Fun_RecordPlay;
import com.views.NewMsg;

public class MsgAdapter extends BaseAdapter{
	
	private final String TAG = "MsgAdapter";
	
	private Context context;
	
	private List<?> items;
	
	LayoutInflater inflater;
	/** 选中项 */
	public HashMap<String, Object> isSelected;
	/** 是否显示多选框 */
	public boolean show = false;
	
	int temp  = 0;
	
	//(CheckBox)findViewById(R.id.msg_ck_all);
	
	CheckBox cb;
	public MsgImageLoader _msgImageLoader;
	
	static class ViewHolder{
		TextView title;
		TextView time;
		ImageView iv;
		CheckBox ck;
		Button play_image;
	}
	
	public MsgAdapter(Context _context, List<?> _items){
		this.context = _context;
		this.items = _items;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_msgImageLoader = new MsgImageLoader(context);
	}
	
	@Override
	public int getCount() {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		Set<String> keys = null;
		if(isSelected!=null){
			Log.v(TAG, "渲染组件->isSelected:"+isSelected);
		//	Log.v(TAG, "渲染组件->当前条目title:"+msg.title);
			keys = isSelected.keySet();
//			if(keys.contains(msg.title)){
//				Log.v(TAG, "渲染组件->当前条目已选中  msg.title:"+msg.title+":"+isSelected.get(msg.title));
//				Log.v(TAG, "holder.title:"+holder.title);
//				Log.v(TAG, "holder.ck:"+holder.ck);
//					holder.ck.setChecked((Boolean)isSelected.get(msg.title));
//			} 
		}
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.new_msg_item, null);
			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(R.id.msg_title);
			//holder.time = (TextView)convertView.findViewById(R.id.msg_time);
			holder.iv = (ImageView)convertView.findViewById(R.id.msg_img);
			holder.ck = (CheckBox)convertView.findViewById(R.id.cb);
			holder.play_image = (Button) convertView.findViewById(R.id.record_play);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		final Message msg = (Message)items.get(position);
		holder.title.setText(/*msg.title+*/"\n"+context.getString(R.string.from)+msg.devicename+"\n"+msg.evt_time);
		//holder.time.setText(msg.logtime);
		
		//报警图片加载
		//Log.d(TAG, "报警图片：" + msg.evt_picture);
		//ImageLoader.getInstance().displayImage(msg.evt_picture, holder.iv);
		
		_msgImageLoader.DisplayImage(msg.evt_picture, holder.iv);
		
		if(show){
			holder.ck.setVisibility(View.VISIBLE);
		}else{
			holder.ck.setVisibility(View.GONE);
		}
		
		holder.ck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isSelected==null){
					isSelected = new HashMap<String, Object>();
				}
				Log.v(TAG, "onCheckedChanged isSelected:"+isSelected);
				Log.v(TAG, "onCheckedChanged msg.title:"+msg.title);
				Log.v(TAG, "onCheckedChanged isChecked:"+isChecked);
				isSelected.put(msg.title, isChecked);
				
				if(cb == null){
					//cb = (CheckBox).findViewById(R.id.msg_ck_all);
				}
				
				//根据当前列表选中情况，设置全选checkbox NewMsg 
				NewMsg msg;
				Log.v(TAG, "onCheckedChanged isSelected:"+isSelected);
				int i =  sumByChecked(isSelected, true);
				if(i==0){
				APP.ShowToast(context.getString(R.string.select_allno));	
				}
				if(getCount()==i){
					APP.ShowToast(context.getString(R.string.select_all));
				}
				APP.GetMainActivity().setValue(i);
			temp++;
			Log.v(TAG, "==========================================================:"+temp);
			}
		});
		
		holder.ck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.v(TAG, "...OnClickListener...");
				if(isSelected!=null){
					//APP.ShowToast("OnClickListener：已选中:"+sumByChecked(isSelected, true));
				}
			}
		});
		
		//录像回放
		holder.play_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Message msg = (Message)items.get(position);
					System.out.println(msg.devicename+"--"+msg.logtime+"--"+msg.evt_vsize);
					System.out.println(msg.evt_video);
					
					JSONObject json = null;
					String params = "?ossUrl="+msg.evt_video+"&timeMillis=0";
					Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
					if (Integer.parseInt(map.get("code").toString()) == 200) {
						json = new JSONObject(map.get("data").toString());
						try {
							String str = json.getString("url");
							if(str.equals("NoSuchKey")){//地址错误
								APP.ShowToast(SDK.GetErrorStr(-1));
							}else{
								//播放
								//SDK.CurlSetOperation(str, msg.evt_vsize, 0);
								
								Intent intent = new Intent(APP.GetMainActivity(), Fun_RecordPlay.class);
								intent.putExtra("evt_vsize", msg.evt_vsize);
								intent.putExtra("evt_video", str);
								intent.putExtra("deviceName", msg.devicename);
								APP.GetMainActivity().startActivity(intent);
							}
						} catch (JSONException e) {
						}
					}
				} catch (Exception e) {
					LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
					return;
				}
			}
		});
		
		/*if(keys!=null){
			//Log.v(TAG, "渲染组件->isSelected:"+isSelected);
			Log.v(TAG, "渲染组件->当前条目title:"+msg.title);
			if(keys.contains(msg.title)){
				Log.v(TAG, "渲染组件->当前条目已选中  msg.title:"+msg.title+":"+isSelected.get(msg.title));
				//Log.v(TAG, "holder.title:"+holder.title);
				//Log.v(TAG, "holder.ck:"+holder.ck);
				
				if((Boolean)isSelected.get(msg.title)){
					//测试 
					holder.ck.setChecked(true);
				}else{
					holder.ck.setChecked(false);
				}
			} 
		}*/
		
		return convertView;
	}
	
	/**
	 * @param map
	 * @param b
	 * @return
	 */
	public int sumByChecked(Map<String, Object> map, boolean b){
		int i = 0;
		Set<String> keys = map.keySet();
		for(String str : keys){
			Log.v(TAG, "sumByChecked:"+str+":"+map.get(str));
			if((Boolean)map.get(str)){
				i++;
			}
		}
		return b?i:map.size()-i;
	}

}