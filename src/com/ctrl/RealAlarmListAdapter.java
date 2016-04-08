package com.ctrl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adapter.ImageDownloader;
import com.adapter.OnImageDownload;
import com.manniu.manniu.R;
import com.views.BriefInfoBean;
import com.views.Fun_Cloud;
import com.views.NewMsgDetail;
/**
  	实时报警
 */
public class RealAlarmListAdapter extends BaseAdapter{
	
	private Context _context;
	public List<BriefInfoBean> _data = new ArrayList<BriefInfoBean>();
	//报警图片缓存目录 
  	public static String alarmPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/manniu/alarms/";
//	public List<Message> _data = new ArrayList<Message>();
	
	public static boolean _isOpenAlarm = true;//是否已经打开视频
	byte[] buf = new byte[20];
	//private Handler _handler;
	private ImageDownloader imageLoader;
	private ListView lv;
	public RealAlarmListAdapter(Context context, ListView lv){
		this._context = context;
		this.lv = lv;
		//_handler = new Handler();
		if(imageLoader == null)
        	imageLoader = new ImageDownloader();
        File dir = new File(alarmPath);
		if(!dir.exists()) dir.mkdirs();
	}

	@Override
	public int getCount() {
		return _data.size();
	}
	public void Clear(){
		this._data.clear();
	}
	public void addItem(List<BriefInfoBean> data){
		this._data.addAll(data);
		//Collections.reverse(this._data);
	}
	public int updateList(List<BriefInfoBean> data) {
		_data.clear();
		this._data.addAll(data);
		//if(this._data.size() > 49){
		//	this._data.remove(0);
		//}
		//Collections.reverse(this._data);
		this.notifyDataSetChanged();
		return _data.size();
	}
	
	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		TextView title;
		TextView time;
		ImageView iv;
		//ImageView play_image;
	}
	
	@Override
	public View getView(final int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if(view == null){
			holder = new ViewHolder();
			view = LayoutInflater.from(_context).inflate(R.layout.new_msg_item, null);
			holder.iv = (ImageView)view.findViewById(R.id.msg_img);
			holder.title = (TextView) view.findViewById(R.id.msg_title);
			//holder.play_image = (ImageView) view.findViewById(R.id.record_play);
			view.setTag(holder);
			
		}else {
			holder = (ViewHolder) view.getTag();
		}
		
		if(Fun_Cloud.instance._index == 1){
			BriefInfoBean msg = _data.get(position);
			holder.title.setText("\n"+_context.getString(R.string.from)+msg.getName()+"\n"+msg.getInfo());
//			holder.title.setText("\n"+_context.getString(R.string.from)+msg.devicename+"\n"+msg.evt_time);
			//点击图片
			holder.iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(_isOpenAlarm){
						_isOpenAlarm = false;
						Intent intent = new Intent(_context,NewMsgDetail.class);
						intent.putExtra("position", position);
						intent.putExtra("msgList",(Serializable) _data);
						_context.startActivity(intent);
					}
				}
			});
			holder.iv.setTag(msg.evt_picture);
			if(msg.evt_picture != null && !msg.evt_picture.equals("") && !msg.evt_picture.equals("NoSuchKey")){
				imageLoader.imageDownload(msg.evt_picture, holder.iv, msg.thumb_url,
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
			
			/*holder.iv.setTag(msg.evt_picture);
			if(msg.evt_picture != null && !msg.evt_picture.equals("") && !msg.evt_picture.equals("NoSuchKey")){
				imageLoader.imageDownload(msg.evt_picture, holder.iv, msg.thumb_url,
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
			}*/
		}
		/*//录像回放
		holder.play_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if(_isOpenAlarm){
						_isOpenAlarm = false;
						final BriefInfoBean msg = (BriefInfoBean)_data.get(position);
						if(msg.getEvt_video().equals("")){
							_isOpenAlarm = true;
							APP.ShowToast(_context.getString(R.string.Err_recordvide_null));
							return;
						}
						final String[] tempUrl = mp4Caches.get(getKey(msg.getEvt_video()));
						//先从软引用中拿数据
						if(tempUrl != null && tempUrl.length > 0){
							LogUtil.d("MsgAdapter","从软引用中拿数据--imageName==" + tempUrl+":"+msg.getSize()+" : "+tempUrl[0]+":"+msg.getName()+":"+tempUrl[1]);
//							
							
//							Intent intent = new Intent(Intent.ACTION_VIEW);
//							intent.setClassName(_context, "com.views.cloud.MainActivity");
//							APP.GetMainActivity().startActivity(intent);
							
//							Intent intent = new Intent(_context, NewSurfaceTest.class);
//							intent.putExtra("channel", 0);
//							intent.putExtra("deviceSid", "VFMhAQEAAGUwNjFiMjAxMGJmOAAA");
//							intent.putExtra("deviceName", 203);
//							_context.startActivity(intent);
							
							 _handler.postDelayed(new Runnable() {
				                    @Override
				                    public void run() {
				                    	Constants.evt_video = tempUrl[0];
				                    	Constants.devName = msg.getName();
				                    	Constants.evt_vsize = msg.getSize();
				                    	Constants.evt_ManufacturerType = Integer.parseInt(tempUrl[1]);
				                    	Intent intent = new Intent(_context, Fun_RecordPlay.class);
//										intent.putExtra("evt_vsize", msg.getSize());
//										intent.putExtra("evt_video", tempUrl[0]);
//										intent.putExtra("deviceName", msg.getName());
//										intent.putExtra("evt_ManufacturerType",Integer.parseInt(tempUrl[1]));
										_context.startActivity(intent);
				                    }
				                }, 300);
							
							
							//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_RECORDPLAY);
							//_handler.sendEmptyMessageDelayed(100, 300);
							_isOpenAlarm = true;
//							EncodeTask mEncTask = new EncodeTask(msg.evt_vsize,tempUrl[0],msg.devicename,Integer.parseInt(tempUrl[1]));
//							mEncTask.execute((Void) null);
							LogUtil.d("MsgAdapter","从软引用中拿数据  1.1  ");
							
						}else{
							SDK.DecodeUuid(msg.getUuid(), buf);
							final int type = buf[3];
							JSONObject json = null;
							String params = "?ossUrl="+msg.getEvt_video()+"&timeMillis=0";
							Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
							if (Integer.parseInt(map.get("code").toString()) == 200) {
								try {
									json = new JSONObject(map.get("data").toString());
									final String str = json.getString("url");
									if(str.equals("NoSuchKey")){//地址错误
										APP.ShowToast(SDK.GetErrorStr(-1));
										_isOpenAlarm = true;
									}else{
										 _handler.postDelayed(new Runnable() {
							                    @Override
							                    public void run() {
							                    	Constants.evt_video = str;
							                    	Constants.devName = msg.getName();
							                    	Constants.evt_vsize = msg.getSize();
							                    	Constants.evt_ManufacturerType = type;
							                    	
							                    	Intent intent = new Intent(_context, Fun_RecordPlay.class);
													_context.startActivity(intent);
							                    }
										 }, 300);
										
//										Intent intent = new Intent(Intent.ACTION_VIEW);
//										intent.setClassName(_context, "com.views.cloud.MainActivity");
//										APP.GetMainActivity().startActivity(intent);
										
//										Intent intent = new Intent(_context, NewSurfaceTest.class);
//										intent.putExtra("channel", 0);
//										intent.putExtra("deviceSid", "VFMhAQEAAGUwNjFiMjAxMGJmOAAA");
//										intent.putExtra("deviceName", 203);
//										_context.startActivity(intent);
										
//										EncodeTask mEncTask = new EncodeTask(msg.evt_vsize,str,msg.devicename,type);
//										mEncTask.execute((Void) null);
										//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_RECORDPLAY);
										mp4Caches.put(getKey(msg.getEvt_video()), new String[]{str,type+""});
										_isOpenAlarm = true;
										LogUtil.d("MsgAdapter","HttpURLConnectionTools  url.....");
									}
								} catch (JSONException e) {
									_isOpenAlarm = true;
									LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
								}
							}
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
		
		return view;
	}
	
	private String getKey(String str){
		int last = str.lastIndexOf("/");
		return str.substring(last+1, str.length()-4);
	}

}
