package com.adapter;

import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.basic.APP;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.views.Fun_RecordPlay;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 实时报警适配器
 * @author jianhua
 *
 */
@SuppressLint("DefaultLocale")
public class MsgAdapter2 extends BaseAdapter {
    private List<?> _items;
    private LayoutInflater inflater=null;
    public MsgImageLoader imageLoader; //用来下载图片的类
    private Context context;
    
    // 设备信息
    public MsgAdapter2(Context context, List<?> _items) {
    	this.context = context;
        this._items = _items;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(imageLoader == null)
        	imageLoader = new MsgImageLoader(context);
    }

    public void setAlarmListToShow() {
		this.notifyDataSetInvalidated();
	}
    
//	public int UpdateList(String path, String type,String query,boolean bQuery) {
//		Constants.data.clear();
//		this.notifyDataSetChanged();
//		return Constants.data.size();
//	}
	
    
    public int getCount() {
        return _items.size();
    }

    public Object getItem(int position) {
        return _items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    
    public class VideoHolder {
    	TextView title;
		TextView time;
		ImageView iv;
		CheckBox ck;
		Button play_image;
	}
    
	public View getView(final int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		VideoHolder vh;
		if (vi == null) {
			vh = new VideoHolder();
			//录像列表适配器 
			vi = inflater.inflate(R.layout.new_msg_item, null);
			vh.title = (TextView)vi.findViewById(R.id.msg_title);
			//holder.time = (TextView)convertView.findViewById(R.id.msg_time);
			vh.iv = (ImageView)vi.findViewById(R.id.msg_img);
			vh.ck = (CheckBox)vi.findViewById(R.id.cb);
			vh.play_image = (Button) vi.findViewById(R.id.record_play);
			vi.setTag(vh);
			
			final Message msg = (Message)_items.get(position);
			vh.title.setText("\n"+context.getString(R.string.from)+msg.devicename+"\n"+msg.evt_time);
			
			//录像回放
			vh.play_image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Message msg = (Message)_items.get(position);
//						System.out.println(msg.devicename+"--"+msg.logtime+"--"+msg.evt_vsize);
//						System.out.println(msg.evt_video);
						int type = SDK.AnalysisFactoryType(msg.uuid);
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
									intent.putExtra("evt_ManufacturerType", type);
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
			
			imageLoader.DisplayImage(msg.evt_picture, vh.iv);
			
		}else{
			vh = (VideoHolder) vi.getTag();
		}
		return vi;
	}
	
     
}
