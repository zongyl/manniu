package com.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.afinal.simplecache.ACache;
import org.json.JSONException;
import org.json.JSONObject;
import com.adapter.DevAdapter;
import com.adapter.Message;
import com.adapter.MsgAdapter2;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.basic.XMSG;
import com.bean.Device;
import com.bean.LiveVideo;
import com.ctrl.IXList;
import com.ctrl.RealAlarmListAdapter;
import com.ctrl.XImageBtn;
import com.ctrl.XListAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
//不用的类
public class Fun_Cloud extends XViewBasic implements OnTaskListener ,IXList{
	public static Fun_Cloud instance = null;
	private String TAG = Fun_Cloud.class.getSimpleName();
	ViewFlipper _vf;		// View切换

	String getDevicesServerPath = "", getMsgServerPath = "";
	// 录像查询条件
	int doaminType;
	int isRoot;
	int parentId;
	
	public static int devType = 1;
	public ACache cache;//缓存文件
	//渲染view的时候，标识是否为刷新
	public boolean isrefresh = true;
	Context context;
	private String userId;
	ListView listView = null;//设备列表
	DevAdapter devAdapter = null;
	List<Device> devList;
	
	ListView listViewMsg = null;//消息列表
//	List<Message> msgList = new ArrayList<Message>();
	public List<BriefInfoBean> msgList = new ArrayList<BriefInfoBean>();
	//MsgAdapter2 adapter = null;
	int pageNo = 1;
	
	RealAlarmListAdapter adapter;
	
	public Button btn_cloud_tab_1,btn_cloud_tab_2;
	public Button btn_qrcode,btn_add;
	int _nClickedCount = 0;
	View _lastClieckView = null;
	public final int DOUBLE_CLICKED = 100;
	
//	private Button btn_test;
//	private static String[] strs = new String[] {
//		    "first", "second", "third", "fourth", "fifth"
//		    };
	byte[] buf = new byte[20];//取报警设备类型

	public Fun_Cloud(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		instance = this;
		context = activity;
		cache = ACache.get(context);
		userId = APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "");
		_vf = (ViewFlipper) this.findViewById(R.id.viewflipper);
		
		
		btn_cloud_tab_1 = (Button)findViewById(R.id.cloud_tab_1);
		btn_cloud_tab_2 = (Button)findViewById(R.id.cloud_tab_2);
		btn_cloud_tab_1.setOnClickListener(this);
		btn_cloud_tab_2.setOnClickListener(this);
		
		btn_qrcode = (Button)findViewById(R.id.btn_qrcode);
		btn_qrcode.setOnClickListener(this);
		btn_add = (Button)findViewById(R.id.btn_add);
		btn_add.setOnClickListener(this);
		
//		btn_test = (Button)findViewById(R.id.btn_test);
//		btn_test.setOnClickListener(this);

		getDevicesServerPath = Constants.ETShostUrl + "/query_dev_info";
		getMsgServerPath = Constants.hostUrl + "/android/getMessage";
		
		loadDevList();
//		loadMsgList();
		
//		int[] itemType = {1, 0, 1 };
//		int[] itemId = {R.id.iv_state, R.id.tv_dvr_name, R.id.im_talk };
//		_adpDvrs = new XListAdapter(ACT, this, this.findViewById(R.id.msg_list), R.layout.list_item_talk, itemType, itemId);
//		listViewMsg = (ListView)findViewById(R.id.msg_list);
		/*listViewMsg.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, strs));
		listViewMsg.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setClassName(context, "com.views.cloud.MainActivity");
//				context.startActivity(intent);
				Intent intent = new Intent(context, NewSurfaceTest.class);
				intent.putExtra("channel", 0);
				intent.putExtra("deviceSid", "VFMhAQEAAGUwNjFiMjAxMGJmOAAA");
				intent.putExtra("deviceName", 203);
				context.startActivity(intent);
				}
			});*/
		
	}
	
	@Override
	public void OnClickedItem(XListAdapter parent, View item, int postion,
			Object obj) {
		if(item.getId() == R.id.im_talk){ // 打开空调
//			CommonChannelBean chnl = (CommonChannelBean) obj;
//			if(chnl.getState() != 1){
//				APP.ShowError("设备不在线！");
//			}else{
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setClassName(ACT, "com.views.Air_CondCtrlActivity");
//				Bundle bundle=new Bundle(); 
//				bundle.putString("devId", chnl.getDeviceId());  
//		        bundle.putString("devName", chnl.getChannelName());  
//		        bundle.putInt("sensorNo", chnl.getNum()); 
//		        bundle.putInt("dataType", chnl.getDataType()); 
//		        //把附加的数据放到意图当中  
//		        intent.putExtras(bundle);
//				ACT.startActivity(intent);
//			}
		}
	}
	
	
	
	/**
	 * 加载设备列表
	 */
	public void loadDevList(){
		try {
			if(listView == null){
				listView = (ListView)findViewById(R.id.main_list);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
							if (v.equals(_lastClieckView)) {
								_nClickedCount++;
							} else {
								_nClickedCount = 1;
								android.os.Message msg = new android.os.Message();
								msg.what = DOUBLE_CLICKED;
								msg.obj = v;
								msg.arg1 = position;
								_handler.sendMessageDelayed(msg, 300);
							}
							_lastClieckView = v;
						}
					});
			}
			//有缓存 就读取缓存  没有缓存 则请求服务器
			if(cache.getAsString(userId + "_devices")==null){
				LogUtil.d(TAG, userId + "用户，没有设备列表的缓存，直接加载!");
				APP.ShowWaitDlg(Fun_Cloud.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
			}else{
				LogUtil.d(TAG, userId + "用户，有设备列表的缓存!");
				if(isrefresh){
					render(cache.getAsString(userId + "_devices"), cache.getAsString(userId + "_collects"), true);
					isrefresh = false;
				}
				if(_index != 0){
					ShowPage(0);
				}
			}
			
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	/**
	 * 加载设备列表
	 */
	public int loadDevData2() {
		int result = 0;
		JSONObject json = null;
		try {
			String params = "?sid="+userId+"&cid="+Constants.session_Id;
			
			LogUtil.d(TAG, "request string:"+getDevicesServerPath+params);
			
			Map<String, Object> map = HttpURLConnectionTools.get(getDevicesServerPath+params);
			if (Integer.parseInt(map.get("code").toString()) == 200) {
				json = new JSONObject(map.get("data").toString());
				LogUtil.d(TAG, "json:" + json.toString());
				String str;
				try {
					str = json.getString("data");
					if("nologin".equals(str)){
						LogUtil.d(TAG, "列表session超时");
					}else{
						devList = new ArrayList<Device>();
						cache.put(userId + "_devices", str);
						cache.put(userId + "_collects", json.getString("collects"));
						render(str, json.getString("collects"), true);
					}
				} catch (JSONException e) {
					result = SDK.Err_refresh;
					LogUtil.d(TAG, "/android/getDevices...error..json:"+json.toString()+"\n"+e.getMessage());
				}
			}else{
				result = SDK.Err_SER_FAIL;
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
		return result;
	}
	
	/**
	 * 渲染设备列表 
	 * @param json 设备
	 * @param collects 收藏
	 * @param isrefresh
	 */
	private void render(String json, String collects, boolean isrefresh){
		Log.d(TAG, "render.isrefresh:"+isrefresh);
		Log.d(TAG, "render.json:"+json);
		Log.d(TAG, "render.collects:"+collects);
		try {
			if(devList == null){
				devList = new ArrayList<Device>();
			}
			devList.clear();
			if(!json.equals("{}")){
				JSONArray array = JSON.parseArray(json);
				for(int i = 0; i < array.size(); i++){
					Device dev1 = JSON.toJavaObject((JSON)array.get(i), Device.class);
					devList.add(dev1);
				}
			}
			
			if(!collects.equals("{}")){
				JSONArray collectArray = JSON.parseArray(collects);
				for(int i = 0; i < collectArray.size(); i++){
					JSON obj = (JSON)collectArray.get(i);
					LiveVideo live = JSON.toJavaObject((JSON)collectArray.get(i), LiveVideo.class);
					Device dev = new Device();
					dev.devname = live.getLivename();
					dev.sid = live.getSid();
					dev.logo = live.getImg();
					dev.online = 0;
					dev.userid = "";
					dev.type = 100;//收藏 
					devList.add(dev);
				}
			}
			_handler.sendEmptyMessage(XMSG.DEVICE_LIST_LOAD);//通过消息更新数据
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	
	/**
	 * 加载消息列表 
	 */
	int _direction = 0;//0:PULL_FROM_START 1.PULL_FROM_END
	//private int _flag = 0;//记录是否第一次切换
	public void loadMsgList(){
		try {
			if(listViewMsg == null){
				listViewMsg = (ListView)findViewById(R.id.msg_list);
				listViewMsg.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
//						Intent intent = new Intent(Intent.ACTION_VIEW);
//						intent.setClassName(context, "com.views.cloud.MainActivity");
//						context.startActivity(intent);
						final BriefInfoBean msg = (BriefInfoBean)adapter._data.get(position);
//						final Message msg = (Message)adapter._data.get(position);
						
						if(RealAlarmListAdapter._isOpenAlarm){
							RealAlarmListAdapter._isOpenAlarm = false;
							final BriefInfoBean alrmdata = (BriefInfoBean)adapter._data.get(position);
							if(alrmdata.evt_video.equals("")){
								RealAlarmListAdapter._isOpenAlarm = true;
								APP.ShowToast(context.getString(R.string.Err_recordvide_null));
								return;
							}else{
//								Constants.devName = alrmdata.devicename;
//		                    	Constants.evt_vsize = alrmdata.evt_vsize;
		                    	/*Constants.devName = alrmdata.getName();
		                    	Constants.evt_vsize = alrmdata.getSize();
								android.os.Message me = new android.os.Message();
								me.what = 1001;
								me.obj = alrmdata;
								_handler.sendMessage(me);*/
								
								SDK.DecodeUuid(alrmdata.uuid, buf);
								final int type = buf[3];
								Constants.evt_ManufacturerType = type;
								
								JSONObject json = null;
								String params = "?ossUrl="+msg.evt_video+"&timeMillis=0";
								Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
								if (Integer.parseInt(map.get("code").toString()) == 200) {
									try {
										json = new JSONObject(map.get("data").toString());
										final String str = json.getString("url");
										if(str.equals("NoSuchKey")){//地址错误
											APP.ShowToast(SDK.GetErrorStr(-1));
											RealAlarmListAdapter._isOpenAlarm = true;
										}else{
											 _handler.postDelayed(new Runnable() {
								                    @Override
								                    public void run() {
								                    	Constants.evt_video = str;
//								                    	Constants.devName = msg.getName();
//								                    	Constants.evt_vsize = msg.getSize();
								                    	Constants.devName = alrmdata.name;
								                    	Constants.evt_vsize = alrmdata.size;
								                    	Constants.evt_ManufacturerType = type;
								                    	Intent intent = new Intent(context, Fun_RecordPlay.class);
								                    	context.startActivity(intent);
								                    }
											 }, 300);
											
											 RealAlarmListAdapter._isOpenAlarm = true;
											LogUtil.d("MsgAdapter","HttpURLConnectionTools  url.....");
										}
									} catch (JSONException e) {
										RealAlarmListAdapter._isOpenAlarm = true;
										LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
									}
								}
								
								
								
							}
						}
						
						
						
						
						
						}
					});
					
				isrefresh = true;
			}
			pageNo = 1;
			if(cache.getAsString(userId + "_msgList")==null){
				Log.d(TAG, "用户:" + userId + ", 消息列表没有缓存!");
				APP.ShowWaitDlg(Fun_Cloud.this, R.string.openning_ReloadData, XMSG.MSG_LIST_LOAD, _direction);
			}else{
				Log.d(TAG, "用户:" + userId + ", 消息列表有缓存，直接加载!!!!!");
				if(isrefresh){
					msgRender(cache.getAsString(userId + "_msgList"), true);
					isrefresh = false;
				}
				if(_index != 1){
					ShowPage(1);
				}
			}
		} catch (Exception e) {
			Log.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	/**
	 * 加载报警消息
	 */
	private int loadMsgData(Mode dur){
		int result = 0;
		String params = "?userId="+APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "")+"&sessionId="+Constants.sessionId+"&pageSize=10";
		if(Mode.PULL_FROM_START == dur){
			msgList.clear();
			Log.d(TAG, "msgList clearing!!");
		}else if(Mode.PULL_FROM_END == dur){
			//msgList.clear();//测试时只加载下一页数据
			//累加
			pageNo += 1;
			params += "&pageNo="+pageNo;
		}
		Log.d(TAG, "params:"+params.toString());
		
		JSONObject json = null;
		try {
			Map<String, Object> map = HttpURLConnectionTools.get(getMsgServerPath+params);
			if (Integer.parseInt(map.get("code").toString()) == 200) {
				json = new JSONObject(map.get("data").toString());
				//LogUtil.d(TAG, "json:" + json.toString());
				String str;
				try {
					str = json.getString("data");
					//str = "[{\"logtime\":\"2016-03-02 06:16:50.0\",\"evt_type\":1,\"evt_time\":\"2016-03-02 06:15:54.0\",\"evt_state\":1,\"typename\":null,\"thumb_url\":\"NoSuchKey\",\"evt_vsize\":4591868,\"devicename\":\"0IPC(manniu203)\",\"uuid\":\"VFMhAQEAAGUwNjFiMjAxMGJmOAAA\",\"evt_picture\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160302061555_fa4aaf3f.jpg?Expires=1456913110&OSSAccessKeyId=3WVh0lsoA8r5uHH6&Signature=MNKgzjOIbINevgPFEC7C9S/8zDw%3D\",\"kid\":72137,\"evt_video\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160302061555_1df28238.mp4\"},{\"logtime\":\"2016-03-01 22:16:14.0\",\"evt_type\":1,\"evt_time\":\"2016-03-01 22:15:17.0\",\"evt_state\":1,\"typename\":null,\"thumb_url\":\"NoSuchKey\",\"evt_vsize\":4595006,\"devicename\":\"0IPC(manniu203)\",\"uuid\":\"VFMhAQEAAGUwNjFiMjAxMGJmOAAA\",\"evt_picture\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160301221517_8a205bb0.jpg?Expires=1456913110&OSSAccessKeyId=3WVh0lsoA8r5uHH6&Signature=lZWiv6be9X8in604jb58JOJdprw%3D\",\"kid\":72113,\"evt_video\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160301221518_6343fe85.mp4\"}]";
					if("nologin".equals(str)){
						LogUtil.d(TAG, "报警信息..session超时");
					}else{
						cache.put(userId + "_msgList", str);
						msgRender(str,true);
					}
				} catch (Exception e) {
					result = SDK.Err_refresh;
					LogUtil.d(TAG, "/android/getDevices...error..json:"+json.toString()+"\n"+e.getMessage());
				}
			}else{
				result = SDK.Err_SER_FAIL;
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
		return result;
	}
	
	/**
	 * 渲染消息列表 
	 * @param isrefresh
	 */
	private void msgRender(String json,boolean isrefresh){
		try {
			if(!json.equals("{}")){
				JSONArray array = JSON.parseArray(json);
				for(int i = 0; i < array.size(); i++){
					BriefInfoBean bean = new BriefInfoBean();
					Message msg = JSON.toJavaObject((JSON)array.get(i), Message.class);
					bean.setUuid(msg.uuid);
					bean.setName(msg.devicename);
					bean.setEvt_video(msg.evt_video);
					bean.setSize(msg.evt_vsize);
					bean.setInfo(msg.evt_time);
					bean.setEvt_picture(msg.evt_picture);
					bean.setThumb_url(msg.thumb_url);
					msgList.add(bean);
				}
			}
			_handler.sendEmptyMessage(XMSG.MSG_LIST_LOAD);//通过消息更新数据
		} catch (Exception e) {
		}
	}
	
	

	@Override
	protected void OnVisibility(int visibility) {
//		if(_index == 0){
//			_adp1.Clear();
//			_adp1.notifyDataSetChanged();
//		}
		super.OnVisibility(visibility);
	}


	// 消息处理
	@Override
	public void OnMessage(android.os.Message msg) {
		switch (msg.what) {
		case DOUBLE_CLICKED:
			selected(msg,_nClickedCount,msg.arg1);
			_nClickedCount = 0;
			_lastClieckView = null;
			break;
		case XMSG.DEVICE_LIST_LOAD:
			if(devAdapter == null){
				devAdapter = new DevAdapter(ACT);
				devAdapter.addItem(devList);
				listView.setAdapter(devAdapter);
			}else{
				devAdapter.updateList(devList);
			}
			if(BackLoginThread.state == 200){
				APP.dismissProgressDialog();
			}
			break;
		case XMSG.MSG_LIST_LOAD:
			if(adapter == null){
				adapter = new RealAlarmListAdapter(context,listViewMsg);
				adapter.addItem(msgList);
				listViewMsg.setAdapter(adapter);
			}else{
				adapter.updateList(msgList);
			}
			break;
			
		case 1001:
			// 开始异步功能操作
			APP.ShowWaitDlg(this, R.string.querying, 3, msg.obj);
			break;
		}
	}
	
	public static boolean _isOpen = true;//实时视频是否打开
	public void selected(android.os.Message msg,int nClickedCount,int position) {
		try {
			if(nClickedCount == 1 && _isOpen && devList.size() > 0){
				_isOpen = false;
				Device device = devList.get(position);
				if(BackLoginThread.state != 200 && device.type != 100){
					if(!BackLoginThread.runFlag){
						Main.Instance._loginThead.start();
					}
					//如果IDM不在线 弹出登录框
					Main.Instance._loginThead.waitIDMLogin();
					_isOpen = true;
					return;
				}
				switch (device.type) {
				case 1:
					if(device.online!=0){
						devType = device.type;
						Intent intent = new Intent(ACT, NewSurfaceTest.class);
						intent.putExtra("channel", device.channelNo==null?0:device.channelNo);
						intent.putExtra("deviceSid", device.sid);
						intent.putExtra("deviceName", device.devname);
						ACT.startActivity(intent);
						//实时视频
//						android.os.Message message = new android.os.Message();
//						message.what = XMSG.PLAY;
//						message.obj = device;
//						_realHandler.sendMessage(message);
					}else{
						_isOpen = true;
						APP.ShowToastLong(APP.GetString(R.string.Video_Dviece_login));
					}
					break;
				case 4:
					if(device.online!=0){
						devType = device.type;
						Intent intent = new Intent(ACT, NewSurfaceTest.class);
						intent.putExtra("channel", 0);
						intent.putExtra("deviceSid", device.sid);
						intent.putExtra("deviceName", device.devname);
						ACT.startActivity(intent);
					}else{
						_isOpen = true;
						APP.ShowToastLong(APP.GetString(R.string.Video_Dviece_login));
					}
					break;
				case 100:
					Intent intent=new Intent(Intent.ACTION_VIEW);
			        intent.setClassName(ACT, "com.views.NewWebActivity"); 
			        String url = Constants.hostUrl + "/LiveAction_toPlays?lc.deviceId="+device.sid;
			        Log.d(TAG, "play live url:" + url);
			        intent.putExtra("url", url);
			        intent.putExtra("playType", "0");
					APP.GetMainActivity().startActivity(intent);
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
		}
	}

	// 查询结果与查询条件View切换
	public int _index = 0;
	void ShowPage(int index){
		Animation rInAnim = AnimationUtils.loadAnimation(ACT, R.anim.push_right_in);
		Animation rOutAnim = AnimationUtils.loadAnimation(ACT, R.anim.push_right_out);
		_vf.setInAnimation(rInAnim);
		_vf.setOutAnimation(rOutAnim);
		_vf.setDisplayedChild(index);
		if(index == 1){//报警
			btn_cloud_tab_2.setTextColor(context.getResources().getColor(R.color.blue_menu));
			btn_cloud_tab_1.setTextColor(context.getResources().getColor(R.color.gray));
		}else{
			btn_cloud_tab_1.setTextColor(context.getResources().getColor(R.color.blue_menu));
			btn_cloud_tab_2.setTextColor(context.getResources().getColor(R.color.gray));
		}
		_index = index;
	}
	
	// 功能按钮消息处理
	protected void onClick(int id) {
		switch (id) {
		case R.id.cloud_tab_1:
			loadDevList();
			break;
		case R.id.cloud_tab_2:
			loadMsgList();
//			if(_index != 1){
//				ShowPage(1);
//			}
			break;
		case R.id.btn_qrcode://扫一扫
			forward("com.mining.app.zxing.decoding.QRcode_DecodeActivity");
			break;
		case R.id.btn_add:
			forward("com.views.NewMainAddDev");
			break;
		case R.id.btn_test:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClassName(context, "com.views.cloud.MainActivity");
			context.startActivity(intent);
			break;
		
		}
	}
	private void forward(String activity){
		Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setClassName(context, activity);  
        context.startActivity(intent);
	}

	// 后台任务处理……
	@SuppressLint("UseSparseArrays")
	@Override
	public Object OnDoInBackground(int what, int arg1, int arg2, Object obj) {
		try {
			Integer ret = 0;
			switch (what) {
			case XMSG.DEVICE_LIST_LOAD:
				ret = loadDevData2();
				return ret;
			case XMSG.MSG_LIST_LOAD:
				Mode dur;
				if(arg1 == 0){
					dur = Mode.PULL_FROM_START;
				}else{
					dur = Mode.PULL_FROM_END;
				}
				ret = loadMsgData(dur);
				return ret;
			case 3:
				BriefInfoBean alrmdata = (BriefInfoBean)obj;
//				Message alrmdata = (Message)obj;
				String params = "?ossUrl="+alrmdata.evt_video+"&timeMillis=0";
				Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
				SDK.DecodeUuid(alrmdata.uuid, buf);
				int type = buf[3];
				Constants.evt_ManufacturerType = type;
				return map;
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return -1;
	}

	// 处理结果……
	@Override
	public int OnPostExecute(int what, int arg1, int arg2, Object obj, Object ret) {
		try {
			switch (what) {
			case XMSG.DEVICE_LIST_LOAD:
				Integer nRet = (Integer) ret;
				if (nRet != 0) {	// 打开设备列表失败
					APP.ShowToast(SDK.GetErrorStr(nRet));
				}
				//ShowPage(0);
				break;
			case XMSG.MSG_LIST_LOAD:
				Integer mRet = (Integer) ret;
				if (mRet != 0) {	// 打开消息列表失败
					APP.ShowToast(SDK.GetErrorStr(mRet));
				}
				ShowPage(1);
				break;
			case 3:
				Map<String, Object> map = (Map<String, Object>) ret;
				JSONObject json = null;
				if (Integer.parseInt(map.get("code").toString()) == 200) {
					try {
						json = new JSONObject(map.get("data").toString());
						final String str = json.getString("url");
						if(str.equals("NoSuchKey")){//地址错误
							APP.ShowToast(SDK.GetErrorStr(-1));
							RealAlarmListAdapter._isOpenAlarm = true;
						}else{
							 _handler.postDelayed(new Runnable() {
				                    @Override
				                    public void run() {
				                    	Constants.evt_video = str;
				                    	Intent intent = new Intent(context, Fun_RecordPlay.class);
				                    	intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  
				                    	context.startActivity(intent);
										LogUtil.d("MsgAdapter","HttpURLConnectionTools  url.....");
				                    }
							 }, 300);
						}
					} catch (JSONException e) {
						RealAlarmListAdapter._isOpenAlarm = true;
						LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
						APP.ShowToast(SDK.GetErrorStr(-1));
					}
				}else{
					RealAlarmListAdapter._isOpenAlarm = true;
					APP.ShowToast(SDK.GetErrorStr(-1));
				}
				break;
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return 0;
	}

}
