package com.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.afinal.simplecache.ACache;
import org.json.JSONException;
import org.json.JSONObject;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnPullEventListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.localmedia.XListViewRewrite;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.utils.Loger;
import com.views.bovine.Fra_SnapActivity;
import com.views.bovine.Fra_VideoActivity;

public class NewMain extends XViewBasic implements OnItemClickListener, OnClickListener ,OnTaskListener{
	private static String TAG = "NewMain";
	public static NewMain instance = null;
	
	private String userId;
	
	String getDevicesServerPath = "", getMsgServerPath = "";
	
	Button btn;
	
	ImageView iv;
	
	Context context;
	
	ListView listView;
	
	List<Device> devList;
	
	//List<LiveVideo> liveList;
	
	ViewPager viewPager; 
	
	//DeviceSQLite sqlite;
	
	ACache cache;
	
	ArrayList<View> viewList;
	public int _localIndex = 0;//记录本地TAB的操作
	public TextView _localVideo,_localImg; 
	
	private PullToRefreshScrollView scrollView;
	
	//渲染view的时候，标识是否为刷新
	boolean isrefresh = false;
	
	float temp = 1;
	public static int devType = 0;

	ArrayList<Message> msgList = null;
//	MsgAdapter adapter;
	MsgAdapter2 adapter;
	public CheckBox cb;
	HashMap<String, Object> isSelected = new HashMap<String, Object>();
	int pageNo = 1;
//	private RealHandler _realHandler = null;
//	private BaseApplication mAPP = null;
	
	public NewMain(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		//循环登录
		//BaseApplication.getInstance().relogin();
		
		context = activity;
		instance = this;
		cache = ACache.get(context);
		
		userId = APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "");
		
		//String serverPath = context.getResources().getString(R.string.server_address);
		//getDevicesServerPath = Constants.hostUrl + "/android/getDevices";
		getDevicesServerPath = Constants.ETShostUrl + "/query_dev_info";
		getMsgServerPath = Constants.hostUrl + "/android/getMessage";
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		
		if( viewList == null){
			viewList = new ArrayList<View>();
		}
		LayoutInflater lf = LayoutInflater.from(context);
		viewList.add(lf.inflate(R.layout.new_main_tab1, null));
		//viewList.add(lf.inflate(R.layout.new_main_tab2, null));
		viewList.add(lf.inflate(R.layout.new_main_tab3, null));//报警
		viewList.add(lf.inflate(R.layout.new_main_tab4, null));//本地
		
		viewPager = (ViewPager) findViewById(R.id.vp_list_new_main);
		viewPager.setAdapter(new MyPagerAdapter(viewList));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				Log.v(TAG, "onPageSelected..."+arg0);
				XListViewRewrite.dismissPopWindow();//切换时关闭本地的删除按钮
				APP.GetMainActivity().tab(arg0);
				switch (arg0) {
				case 0://设备
					loadDevList();
					break;
				case 1://报警消息 
					loadMsgList();
					break;
				case 2: //本地
					loadLocalVideoAndImg();
	                break;
				default:
					break;
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		setCurrentItem(0);
		/*mAPP = (BaseApplication) activity.getApplication();
		// 获得该共享变量实例
		_realHandler = mAPP.getRedlandler();*/
	}

	public void setCurrentItem(int index){
		Log.v(TAG, "setCurrentItem..."+index);
		if(viewPager != null){
			viewPager.setCurrentItem(index);
		}
	}
	
	/**
	 * 加载消息列表 
	 */
	int _direction = 0;//0:PULL_FROM_START 1.PULL_FROM_END
	public void loadMsgList(){
		listView = (ListView)findViewById(R.id.msg_list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(ACT,NewMsgDetail.class);
				intent.putExtra("position", position);
//				Bundle bd = new Bundle();
//				bd.putParcelableArrayList("msgList", msgList);
//				intent.putExtras(bd);
				intent.putExtra("msgList",(Serializable) msgList);
				ACT.startActivity(intent);
			}
		});
		
		scrollView = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_msg);
		scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				Log.d(TAG, "refreshing...");
			}
		});
		
		scrollView.setOnPullEventListener(new OnPullEventListener<ScrollView>() {
			@Override
			public void onPullEvent(
					PullToRefreshBase<ScrollView> refreshView,
					State state, Mode direction) {
				if(State.REFRESHING == state){
					isrefresh = true;
					if(Mode.PULL_FROM_END == direction){
						//Log.d(TAG, "——————————————————向上拉——————————————————————");
						_direction = 1;
					}else if(Mode.PULL_FROM_START == direction){
						//Log.d(TAG, "——————————————————向下拉——————————————————————");
						_direction = 0;
					}
//					loadMsgData(direction);
					APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.MSG_LIST_LOAD, _direction);
				}
			}
		});
		
//		cb = (CheckBox)findViewById(R.id.msg_ck_all);
//		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				selectAll(isChecked);
//			}
//		});
		isrefresh = false;
		pageNo = 1;
		if(cache.getAsObject(userId + "_msgList")==null){
			Log.d(TAG, "用户:" + userId + ", 消息列表没有缓存!");
//			loadMsgData(Mode.PULL_FROM_START);
			APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.MSG_LIST_LOAD, _direction);
		}else{
			Log.d(TAG, "用户:" + userId + ", 消息列表有缓存，直接加载!!!!!");
			msgList = (ArrayList<Message>) cache.getAsObject(userId + "_msgList");
			msgRender(isrefresh);
		}
		
	}
	
	int _nClickedCount = 0;
	View _lastClieckView = null;
	private MyHandler _handler = new MyHandler();
	public final static int DOUBLE_CLICKED = 100;
	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg); //这句什么都没有操作  可以不要
			switch (msg.what) {
			case DOUBLE_CLICKED:
				selected(msg,_nClickedCount,msg.arg1);
				_nClickedCount = 0;
				_lastClieckView = null;
				break;
			case XMSG.DEVICE_LIST_LOAD:
				listView.setAdapter(new DevAdapter(context, devList));
				if(BackLoginThread.state == 200){
					APP.dismissProgressDialog();
				}
				break;
			case XMSG.MSG_LIST_LOAD:
				adapter = new MsgAdapter2(context, msgList);
				listView.setAdapter(adapter);
				break;
			/*case 1002:
				if(APP.IsWaitDlgShow()){
					startTimer();
	        	}
				break;
			case XMSG.DEVICE_LIST_ISREFRESH:
				if(isrefresh){
					scrollView.onRefreshComplete();//关闭下拉刷新
				}
				break;
			case 1003:
				int a = msg.arg1;
				APP.ShowToast(SDK.GetErrorStr(a));
				break;*/
			}
		}
	}
	
	public static boolean _isOpen = true;
	public void selected(android.os.Message msg,int nClickedCount,int position) {
		try {
			if(nClickedCount == 1 && _isOpen && devList.size() > 0){
				_isOpen = false;
				Device device = devList.get(position);
				if(BackLoginThread.state != 200 && device.type != 100){
					Main.Instance._loginThead.start();
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
	
	/**
	 * 加载本地存储
	 */
	public void loadLocalVideoAndImg(){
		_localVideo = (TextView) findViewById(R.id.local_text1);
    	_localImg = (TextView) findViewById(R.id.local_text2);
    	if(_localIndex == 0){
    		if(context == null){
    			LogUtil.d(TAG, "cnotext is null!");
    		}
    		if(_localVideo == null){
    			LogUtil.d(TAG, "_localVideo is null!");
    		}
    		try{
    			_localVideo.setTextColor(context.getResources().getColor(R.color.blue_menu));
    		}catch(Exception e){
    			LogUtil.e(TAG, "" + e.toString());
    		}
    		
    	}else{
    		_localImg.setTextColor(context.getResources().getColor(R.color.blue_menu));
    	}
    	getFragmentView(_localIndex);
    	_localVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_localIndex = 0;
				_localVideo.setTextColor(context.getResources().getColor(R.color.blue_menu));
				_localImg.setTextColor(context.getResources().getColor(R.color.text_color));
				getFragmentView(0);
			}
		});
    	_localImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_localIndex = 1;
				_localImg.setTextColor(context.getResources().getColor(R.color.blue_menu));
				_localVideo.setTextColor(context.getResources().getColor(R.color.text_color));
				getFragmentView(1);
			}
		});
	}
	
	@SuppressLint("NewApi")
	public void getFragmentView(int type){
		try {
			if(type == 0){
				android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	Fra_VideoActivity fragment = new Fra_VideoActivity(); 
	        	frTransaction.replace(R.id.ui_container, fragment);// 使用当前Fragment的布局替代id_content的控件
	        	frTransaction.commitAllowingStateLoss();
			}else{
				android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	Fra_SnapActivity fragment = new Fra_SnapActivity(); 
	        	frTransaction.replace(R.id.ui_container, fragment);// 使用当前Fragment的布局替代id_content的控件
	        	frTransaction.commitAllowingStateLoss();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	/**
	 * 加载设备列表
	 */
	public void loadDevList(){
		try {
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
			
//			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
//				@Override
//				public boolean onItemLongClick(AdapterView<?> parent, View view,
//						int position, long id) {
//					APP.ShowToast("long click!");
//					return false;
//				}
//			});
			
			scrollView = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_main);
			scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
				@Override
				public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
					Loger.print("devicelist pull to refresh!");
					isrefresh = true;
					APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
				}
			});
			isrefresh = false;
			//有缓存 就读取缓存  没有缓存 则请求服务器
			if(cache.getAsString(userId + "_devices")==null){
				LogUtil.d(TAG, userId + "用户，没有设备列表的缓存，直接加载!");
				APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
			}else{
				LogUtil.d(TAG, userId + "用户，有设备列表的缓存!");
				render(cache.getAsString(userId + "_devices"), cache.getAsString(userId + "_collects"), isrefresh);
				APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
			}
		} catch (Exception e) {
		}
	}
	
	/*//初始化列表
    private void initDeviceList(){
		APP._dlgWait.show();
    	APP.SetWaitDlgText(context.getText(R.string.openning_ReloadData).toString());
		_handler.sendEmptyMessageDelayed(1002, 1000);
    }
	public java.util.Timer _timer = null;
	private int error_Count= 0;
	//定时器 如果长时间打洞不成功或打洞成功收不到数据 关闭页面
	public void startTimer() {
		try {
			if(BackLoginThread.state == 200){
				if (_timer != null) {
					_timer.cancel();
					_timer = null;
				}
				if(_timer == null){
					_timer = new java.util.Timer();
				}
				_timer.schedule(new TimerTask() {
					@Override
					public void run() {
						int ret = loadDevData2();
						if (ret != 0) {	// 打开列表失败
							android.os.Message msg = new android.os.Message();
							msg.what = 1003;
							msg.arg1 = ret;
							_handler.sendMessage(msg);
							error_Count ++;
							if(error_Count > 3){
								stopTimer();
								error_Count = 0;
								if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
							}
						}else{
							stopTimer();
							if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
						}
						_handler.sendEmptyMessage(XMSG.DEVICE_LIST_ISREFRESH);
					}
				}, 100, 3000);
			}else{
				if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
				APP.ShowToastLong(APP.GetString(R.string.Video_Dviece_login));
				_handler.sendEmptyMessage(XMSG.DEVICE_LIST_ISREFRESH);
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}

	public void stopTimer() {
		if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
	}*/
	
	/**
	 * 全选
	 * @param sel
	 */
	private void selectAll(boolean sel){
//		isSelected.put("移动侦测报警", sel);
//		isSelected.put("移动侦测报警1", sel);
//		isSelected.put("移动侦测报警2", sel);
//		isSelected.put("移动侦测报警3", sel);
//		adapter.isSelected = isSelected;
//		adapter.notifyDataSetChanged();	
//		APP.GetMainActivity().setValue(adapter.sumByChecked(adapter.isSelected, true));
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
						render(str, json.getString("collects"), isrefresh);
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
	 * 加载消息
	 */
	private int loadMsgData(Mode dur){
		int result = 0;
		if(msgList == null){
			Log.d(TAG, "msgList is null!");
			msgList = new ArrayList<Message>();
		}else{
			Log.d(TAG, "msgList is not null!");
		}
//		RequestParams params = new RequestParams();
//		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
//		params.put("sessionId", Constants.sessionId);
		String params = "?userId="+APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "")+"&sessionId="+Constants.sessionId+"&pageSize=10";
		if(Mode.PULL_FROM_START == dur){
			msgList.clear();
			Log.d(TAG, "msgList clearing!!");
		}else if(Mode.PULL_FROM_END == dur){
			//累加
			pageNo += 1;
			//params.put("pageNo", pageNo);
			params += "&pageNo="+pageNo;
		}
		
		Log.d(TAG, "params:"+params.toString());
		
		
		JSONObject json = null;
		try {
			Map<String, Object> map = HttpURLConnectionTools.get(getMsgServerPath+params);
			if (Integer.parseInt(map.get("code").toString()) == 200) {
				json = new JSONObject(map.get("data").toString());
				LogUtil.d(TAG, "json:" + json.toString());
				String str;
				try {
					str = json.getString("data");
					if("nologin".equals(str)){
						LogUtil.d(TAG, "报警信息..session超时");
					}else{
						JSONArray array = JSON.parseArray(json.getString("data"));
						for(int i = 0; i < array.size(); i++){
							String str2 = array.get(i).toString();
							Message msg = JSON.toJavaObject((JSON)JSON.parseObject(str2), Message.class);
							msgList.add(msg);
						}
						cache.put(userId + "_msgList", msgList);
						Log.d(TAG, "用户:" + userId + ", 已缓存!");
						msgRender(isrefresh);
					}
				} catch (JSONException e) {
					result = SDK.Err_refresh;
					LogUtil.d(TAG, "/android/getDevices...error..json:"+json.toString()+"\n"+e.getMessage());
				}
			}else{
				result = SDK.Err_SER_FAIL;
			}
		} catch (Exception e) {
		}
		return result;
		
		
		
		
		/*HttpUtil.get(getMsgServerPath, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				Log.d(TAG, "onSuccess");
				if(statusCode == 200){
					Log.d(TAG, "message json ："+json);
					try {
						if("nologin".equals(json.getString("data"))){
							LogUtil.d(TAG, "报警信息..session超时");
							//BaseApplication.getInstance().relogin();
						}else{
							JSONArray array = JSON.parseArray(json.getString("data"));
							for(int i = 0; i < array.size(); i++){
								String str = array.get(i).toString();
								Message msg = JSON.toJavaObject((JSON)JSON.parseObject(str), Message.class);
								msgList.add(msg);
							}
							cache.put(userId + "_msgList", msgList);
							Log.d(TAG, "用户:" + userId + ", 已缓存!");
							msgRender(isrefresh);
						}
					} catch (Exception e) {
						LogUtil.e(TAG, "/android/getMessage...error.."+e.getMessage());
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,String string, Throwable throwable) {
				Log.d(TAG, "onFailure");
				APP.ShowToast(ACT.getResources().getString(R.string.E_SER_FAIL));
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,Throwable throwable, JSONObject errorResponse) {
				Log.d(TAG, "onFailure:");
				if(errorResponse == null){
					Log.d(TAG, "errorResponse is null!");
				}else{
					Log.d(TAG, "errorResponse is:" + errorResponse);
				}
				scrollView.onRefreshComplete();
				APP.ShowToast(ACT.getResources().getString(R.string.E_SER_FAIL));
			}
			@Override  
            public void onFinish() {  
                super.onFinish();  
            }
			
		});*/
	}
	
	/**
	 * 渲染消息列表 
	 * @param isrefresh
	 */
	private void msgRender(boolean isrefresh){
		try {
			/*if(adapter!=null&&adapter.show){
				Log.v(TAG, "isSelected："+isSelected);
				Log.v(TAG, "adapter.isSelected："+adapter.isSelected);
				HashMap<String, Object> maps = new HashMap<String, Object>();
				if(adapter.isSelected!=null){
					//isSelected = adapter.isSelected;
					maps = adapter.isSelected;
				}
				adapter = new MsgAdapter(context, msgList);
				listView.setAdapter(adapter);
				adapter.show = true;
				adapter.notifyDataSetChanged();
				//adapter.isSelected = isSelected;
				adapter.isSelected = maps;
				adapter.notifyDataSetChanged();
			}else{
//				adapter = new MsgAdapter(context, msgList);
//				listView.setAdapter(adapter);
				_handler.sendEmptyMessage(XMSG.MSG_LIST_LOAD);//通过消息更新数据
			}*/
			_handler.sendEmptyMessage(XMSG.MSG_LIST_LOAD);//通过消息更新数据
//			if(isrefresh){
//				scrollView.onRefreshComplete();
//			}
		} catch (Exception e) {
		}
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
					//TODO 多通道显示多个
//					if(dev1.channels > 1){
//						for(int j=0;j<dev1.channels;j++){
//							dev1 = JSON.toJavaObject((JSON)array.get(i), Device.class);
//							dev1.channelNo = j;
//							devList.add(dev1);
//						}
//					}else{
						devList.add(dev1);
//					}
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
			
			//listView.setAdapter(new DevAdapter(context, devList));
			_handler.sendEmptyMessage(XMSG.DEVICE_LIST_LOAD);//通过消息更新数据
			
//			if(isrefresh){
//				scrollView.onRefreshComplete();//关闭下拉刷新
//			}
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}
		
	class MyPagerAdapter extends PagerAdapter {
		
		public String TAG = "ViewPagerAdapter";
		
		private int mChildCount = 0;
		//界面列表
	    private ArrayList<View> views;
	    
	    private boolean isFirst = true;
	    
	    public MyPagerAdapter (ArrayList<View> views){
	        this.views = views;
	    }
	       
		/**
		 * 获得当前界面数
		 */
		@Override
		public int getCount() {
			 if (views != null) {
	             return views.size();
	         }      
	         return 0;
		}

		/**
		 * 初始化position位置的界面
		 */
	    @Override
	    public Object instantiateItem(View view, int position) {
	        ((ViewPager) view).addView(views.get(position), 0);
	        if(position == 0 && isFirst){
	        	loadDevList();
	        	isFirst = false;
	        }
	        return views.get(position);
	    }
	    
	    /**
		 * 判断是否由对象生成界面
		 */
		@Override
		public boolean isViewFromObject(View view, Object arg1) {
			return (view == arg1);
		}

		/**
		 * 销毁position位置的界面
		 */
	    @Override
	    public void destroyItem(View view, int position, Object arg2) {
	        ((ViewPager) view).removeView(views.get(position));       
	    }
	    @Override
	    public void notifyDataSetChanged() {         
	          mChildCount = getCount();
	          super.notifyDataSetChanged();
	    }
	    
	    @Override
	    public int getItemPosition(Object object)   {          
	          if ( mChildCount > 0) {
	          mChildCount --;
	          return POSITION_NONE;
	          }
	          return super.getItemPosition(object);
	    }
	    
	}

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
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return -1;
	}

	@Override
	public int OnPostExecute(int what, int arg1, int arg2, Object obj,
			Object ret) {
		try {
			switch (what) {
			case XMSG.DEVICE_LIST_LOAD:
				Integer nRet = (Integer) ret;
				if (nRet != 0) {	// 打开设备列表失败
					APP.ShowToast(SDK.GetErrorStr(nRet));
				}
				if(isrefresh){
					scrollView.onRefreshComplete();//关闭下拉刷新
				}
			case XMSG.MSG_LIST_LOAD:
				Integer mRet = (Integer) ret;
				if (mRet != 0) {	// 打开消息列表失败
					APP.ShowToast(SDK.GetErrorStr(mRet));
				}
				if(isrefresh){
					scrollView.onRefreshComplete();//关闭下拉刷新
				}
				break;
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return 0;
	}
}