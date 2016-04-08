package com.adapter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.manniu.manniu.R;
import com.utils.LogUtil;
import com.views.Main;
import com.views.NewMain;
import com.views.NewMsgDetail;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 实时报警适配器
 * @author jianhua
 *
 */
@SuppressLint({ "DefaultLocale", "InflateParams" })
public class MsgAdapter2 extends BaseAdapter {
    public List<Message> _data = new ArrayList<Message>();
    //报警图片缓存目录 
  	public static String alarmPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/manniu/alarms/";
    private LayoutInflater inflater=null;
//    public MsgImageLoader imageLoader; //用来下载图片的类
    private ImageDownloader imageLoader;
    private Context context;
    private ListView lv;
    public static boolean _isOpenAlarm = true;//是否已经打开视频
    //private MyHandler _handler = new MyHandler();
    // 设备信息
    public MsgAdapter2(Context context, ListView lv) {
    	this.context = context;
    	this.lv = lv;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(imageLoader == null)
        	imageLoader = new ImageDownloader();
        File dir = new File(alarmPath);
		if(!dir.exists()) dir.mkdirs();
    }

    public void setAlarmListToShow() {
		this.notifyDataSetInvalidated();
	}
    
    public void clear(){
		this._data.clear();
	}
    public void addItem(List<Message> data){
		this._data.addAll(data);
		//Collections.reverse(this._data);
	}
    public int updateList(List<Message> data) {
    	_data.clear();
    	this._data.addAll(data);
//    	this._data = data;
		this.notifyDataSetChanged();
		return _data.size();
	}
	
    
    public int getCount() {
        return _data.size();
    }

    public Object getItem(int position) {
        return _data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    
    public class VideoHolder {
    	TextView title;
		TextView time;
		ImageView iv;
//		CheckBox ck;
		ImageView play_image;
	}
    
    byte[] buf = new byte[20];
    //private Map<String, String[]> mp4Caches = new HashMap<String, String[]>();//报警MP4缓存
	public View getView(final int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		VideoHolder vh;
		if (vi == null) {
			vh = new VideoHolder();
			//录像列表适配器 
			vi = inflater.inflate(R.layout.new_msg_item, null);
			vh.title = (TextView)vi.findViewById(R.id.msg_title);
			vh.iv = (ImageView)vi.findViewById(R.id.msg_img);
			vh.play_image = (ImageView) vi.findViewById(R.id.record_play);
			vi.setTag(vh);
			
		}else{
			vh = (VideoHolder) vi.getTag();
		}
		
		if(NewMain.instance.viewPager.getCurrentItem() == 1 && Main.Instance._curIndex == 0){
			final Message msg = (Message)_data.get(position);
			vh.title.setText("\n"+context.getString(R.string.from)+msg.devicename+"\n"+msg.evt_time);
			//点击图片
			vh.iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(_isOpenAlarm){
						_isOpenAlarm = false;
						Intent intent = new Intent(context,NewMsgDetail.class);
						intent.putExtra("position", position);
						intent.putExtra("msgList",(Serializable) _data);
						context.startActivity(intent);
					}
				}
			});
			
			
			//录像回放
			/*vh.play_image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						if(_isOpenAlarm){
							_isOpenAlarm = false;
							final Message msg = (Message)_data.get(position);
							if(msg.evt_video.equals("")){
								_isOpenAlarm = true;
								APP.ShowToast(context.getString(R.string.Err_recordvide_null));
								return;
							}
							String[] tempUrl = mp4Caches.get(getKey(msg.evt_video));
							//先从软引用中拿数据
							if(tempUrl != null && tempUrl.length > 0){
								LogUtil.d("MsgAdapter","从软引用中拿数据--imageName==" + tempUrl+":"+msg.evt_vsize+" : "+tempUrl[0]+":"+msg.devicename+":"+tempUrl[1]);
								Intent intent = new Intent(context, Fun_RecordPlay.class);
//								intent.putExtra("evt_vsize", msg.evt_vsize);
//								intent.putExtra("evt_video", tempUrl[0]);
//								intent.putExtra("deviceName", msg.devicename);
//								intent.putExtra("evt_ManufacturerType",Integer.parseInt(tempUrl[1]));
								context.startActivity(intent);
								
//								Intent intent = new Intent(Intent.ACTION_VIEW);
//								intent.setClassName(context, "com.views.cloud.MainActivity");
//								APP.GetMainActivity().startActivity(intent);
								
								//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_RECORDPLAY);
								_handler.sendEmptyMessageDelayed(100, 300);
								_isOpenAlarm = true;
//								EncodeTask mEncTask = new EncodeTask(msg.evt_vsize,tempUrl[0],msg.devicename,Integer.parseInt(tempUrl[1]));
//								mEncTask.execute((Void) null);
								LogUtil.d("MsgAdapter","从软引用中拿数据  1.1  ");
								
							}else{
								
								new Thread(new Runnable() {
									@Override
									public void run() {//上传视频
										
										SDK.DecodeUuid(msg.uuid, buf);
										int type = buf[3];
										JSONObject json = null;
										String params = "?ossUrl="+msg.evt_video+"&timeMillis=0";
										Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
										if (Integer.parseInt(map.get("code").toString()) == 200) {
											try {
												json = new JSONObject(map.get("data").toString());
												String str = json.getString("url");
												if(str.equals("NoSuchKey")){//地址错误
													APP.ShowToast(SDK.GetErrorStr(-1));
													_isOpenAlarm = true;
												}else{
													//播放
													//SDK.CurlSetOperation(str, msg.evt_vsize, 0);
													Intent intent = new Intent(context, Fun_RecordPlay.class);
//													intent.putExtra("evt_vsize", msg.evt_vsize);
//													intent.putExtra("evt_video", str);
//													intent.putExtra("deviceName", msg.devicename);
//													intent.putExtra("evt_ManufacturerType",type);
													context.startActivity(intent);
													_handler.sendEmptyMessageDelayed(100, 300);
//													Intent intent = new Intent(Intent.ACTION_VIEW);
//													intent.setClassName(context, "com.views.cloud.MainActivity");
//													APP.GetMainActivity().startActivity(intent);
													
//													EncodeTask mEncTask = new EncodeTask(msg.evt_vsize,str,msg.devicename,type);
//													mEncTask.execute((Void) null);
													//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_RECORDPLAY);
													mp4Caches.put(getKey(msg.evt_video), new String[]{str,type+""});
													_isOpenAlarm = true;
													LogUtil.d("MsgAdapter","HttpURLConnectionTools  url.....");
												}
											} catch (JSONException e) {
												_isOpenAlarm = true;
												LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
											}
										}
									}
								}).start();
								
								
							}
						}
					} catch (Exception e) {
						APP.ShowToast(SDK.GetErrorStr(-1));
						_isOpenAlarm = true;
						LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
						return;
					}
				}
			});*/
			//imageLoader.DisplayImage(msg.evt_picture, vh.iv);
			vh.iv.setTag(msg.thumb_url);
			if(msg.evt_picture != null && !msg.evt_picture.equals("") && !msg.evt_picture.equals("NoSuchKey")){
				imageLoader.imageDownload(msg.evt_picture, vh.iv, msg.thumb_url,
					new OnImageDownload() {
						@Override
						public void onDownloadSucc(Bitmap bitmap, String c_url,
								ImageView imageView) {
							ImageView iv = (ImageView) lv.findViewWithTag(c_url);
							if ((bitmap != null) && (iv != null)) {
								iv.setImageBitmap(bitmap);
								iv.setTag("");
								iv.setVisibility(View.VISIBLE);
							}
						}
				});
			}
			if(msg.evt_vsize > 0 && !msg.evt_video.equals("") && !msg.evt_video.equals("NoSuchKey")){
				vh.play_image.setImageResource(R.drawable.img_list_play);
			}else{
				vh.play_image.setImageResource(-1);
			}
		}
		return vi;
	}
	
	private String getKey(String str){
		int last = str.lastIndexOf("/");
		return str.substring(last+1, str.length()-4);
	}
	
	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 100:
//				Intent intent = new Intent(context, Fun_RecordplayActivity_MediaPlayer.class);
//				intent.putExtra("fileName", Environment.getExternalStorageDirectory()+"/manniu/records/201603090842290IPC(manniu203).mp4");
//				context.startActivity(intent);
				
//				Intent intent = new Intent(context, NewSurfaceTest.class);
//				intent.putExtra("channel", 0);
//				intent.putExtra("deviceSid", "VFMhAQEAAGUwNjFiMjAxMGJmOAAA");
//				intent.putExtra("deviceName", 203);
//				context.startActivity(intent);
				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(context, "com.views.cloud.MainActivity");
				context.startActivity(intent);
				
				break;
			}
		}
	}
	
	//异步方法
	private class EncodeTask extends AsyncTask<Void, Void, Void> {
		private int _size;
		private String _url;
		private String _devName;
		private int _type;
		// 构造函数
		EncodeTask(int size,String url,String devName,int type) {
			this._size = size;
			this._url = url;
			this._devName = devName;
			this._type = type;
		}
		@Override
		protected Void doInBackground(Void... params) {
//			Intent intent = new Intent(Intent.ACTION_VIEW);
//			intent.setClassName(context, "com.views.Fun_RecordPlay");
//			intent.putExtra("evt_vsize", _size);
//			intent.putExtra("evt_video", _url);
//			intent.putExtra("deviceName", _devName);
//			intent.putExtra("evt_ManufacturerType",_type);
//			context.startActivity(intent);
			
//			Intent intent = new Intent(context, Fun_RecordplayActivity_MediaPlayer.class);
//			intent.putExtra("fileName", Environment.getExternalStorageDirectory()+"/manniu/records/201603090842290IPC(manniu203).mp4");
//			context.startActivity(intent);
//			_isOpenAlarm = true;
			
			//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_RECORDPLAY);
			_isOpenAlarm = true;
//			Intent intent = new Intent(Intent.ACTION_VIEW);
//			intent.setClassName(context, "com.views.cloud.MainActivity");
//			context.startActivity(intent);
			LogUtil.d("MsgAdapter","从软引用中拿数据  1.2..........  ");
			return null;
		}
	}
	
     
}
