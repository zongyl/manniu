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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.adapter.DevAdapter;
import com.adapter.ExpandableListViewAdapter;
import com.adapter.Message;
import com.adapter.MsgAdapter2;
import com.adapter.PullToRefreshExpandableListView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.basic.XMSG;
import com.bean.DevCart;
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
	
	ListView listView = null;
	ListView listViewMsg = null;
	/** 下拉刷新设备列表 */
	private PullToRefreshExpandableListView m_pullToRefreshExpandableListView = null;//多画面
	/** 多画面设备列表数据 */
	List<Device> m_devicesList;
	/** 设备列表数据 */
	List<Device> devList;
	/** 选中的设备数据 */
	private List<DevCart> _devCartsList = null;
	
	//List<LiveVideo> liveList;
	
	public ViewPager viewPager; 
	
	//DeviceSQLite sqlite;
	
	public ACache cache;
	
	ArrayList<View> viewList;
	public int _localIndex = 0;//记录本地TAB的操作
	public TextView _localVideo,_localImg; 
	
	private PullToRefreshScrollView scrollViewDev;//设备
	private PullToRefreshScrollView scrollView;//报警
	
	//渲染view的时候，标识是否为刷新
	public boolean isrefresh = false;
	
	float temp = 1;
	public static int devType = 0;

	List<Message> msgList = new ArrayList<Message>();
//	MsgAdapter adapter;
	MsgAdapter2 adapter = null;//报警适配器
	DevAdapter devAdapter = null;//设备
	/** 多画面设备列表的Adapter */
	private ExpandableListViewAdapter m_expandableListViewAdapter = null;
	/** 播放按钮 */
	private View m_playView = null;
	public CheckBox cb;
	HashMap<String, Object> isSelected = new HashMap<String, Object>();
	int pageNo = 1;
//	private RealHandler _realHandler = null;
//	private BaseApplication mAPP = null;
	
	byte[] buf = new byte[28];//取报警设备类型
	
	public NewMain(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		//循环登录
		//BaseApplication.getInstance().relogin();
		
		context = activity;
		instance = this;
		cache = ACache.get(context);
		
		userId = Constants.userid;//APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "")		
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
		viewList.add(lf.inflate(R.layout.mydevicelistlayout, null));//多画面
		viewList.add(lf.inflate(R.layout.new_main_tab4, null));//本地
		
		viewPager = (ViewPager) findViewById(R.id.vp_list_new_main);
		viewPager.setAdapter(new MyPagerAdapter(viewList));
		//viewPager.setOffscreenPageLimit(0);//修改ViewPager的缓存页面数量 设置无效果
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				Log.v(TAG, "onPageSelected..."+arg0);
				XListViewRewrite.dismissPopWindow();//切换时关闭本地的删除按钮
				APP.GetMainActivity().tab(arg0);
				isrefresh = false;
				switch (arg0) {
				case 0://设备
					loadDevList();
					LogUtil.d(TAG, "切换到loadDevList...");
					break;
				case 1://报警消息 
					devAdapter = null;
					loadMsgList();
					LogUtil.d(TAG, "切换到loadMsgList...");
					break;
				case 2://多画面
					loadDevMultiList();
					break;
				case 3: //本地
					devAdapter = null;
					loadLocalVideoAndImg();
					LogUtil.d(TAG, "切换到loadLocalVideoAndImg...");
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
	
	public void getSize(){
		APP.ShowToast("SIZE!");
	}
	
	/**
	 * 加载消息列表 
	 */
	int _direction = 0;//0:PULL_FROM_START 1.PULL_FROM_END
	public void loadMsgList(){
		try {
			if(listViewMsg == null){
				listViewMsg = (ListView)findViewById(R.id.msg_list);
				listViewMsg.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if(MsgAdapter2._isOpenAlarm){
							MsgAdapter2._isOpenAlarm = false;
							final Message alrmdata = (Message)adapter._data.get(position);
							if(alrmdata.evt_video.equals("") || alrmdata.evt_vsize <= 0){
	                    		MsgAdapter2._isOpenAlarm = true;
								//APP.ShowToast(context.getString(R.string.Err_recordvide_null));
								return;
							}else{
								Constants.devName = alrmdata.devicename;
		                    	Constants.evt_vsize = alrmdata.evt_vsize;
								android.os.Message me = new android.os.Message();
								me.what = 1001;
								me.obj = alrmdata;
								_handler.sendMessage(me);
							}
							
							/*JSONObject json = null;
							String params = "?ossUrl="+msg.evt_video+"&timeMillis=0";
							Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
							if (Integer.parseInt(map.get("code").toString()) == 200) {
								try {
									json = new JSONObject(map.get("data").toString());
									final String str = json.getString("url");
									if(str.equals("NoSuchKey")){//地址错误
										APP.ShowToast(SDK.GetErrorStr(-1));
										MsgAdapter2._isOpenAlarm = true;
									}else{
										 _handler.postDelayed(new Runnable() {
							                    @Override
							                    public void run() {
							                    	Constants.evt_video = str;
							                    	Constants.devName = msg.devicename;
							                    	Constants.evt_vsize = msg.evt_vsize;
							                    	Constants.evt_ManufacturerType = 1;
							                    	
							                    	Intent intent = new Intent(context, Fun_RecordPlay.class);
							                    	context.startActivity(intent);
							                    }
										 }, 300);
										
										LogUtil.d("MsgAdapter","HttpURLConnectionTools  url.....");
									}
								} catch (JSONException e) {
									MsgAdapter2._isOpenAlarm = true;
									LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
								}
							}*/
						}
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
							APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.MSG_LIST_LOAD, _direction);
						}
					}
				});
				isrefresh = true;
			}
			pageNo = 1;
			if(cache.getAsString(userId + "_msgList")==null){
				Log.d(TAG, "用户:" + userId + ", 消息列表没有缓存!");
				APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.MSG_LIST_LOAD, _direction);
			}else{
				Log.d(TAG, "用户:" + userId + ", 消息列表有缓存，直接加载!!!!!");
				if(isrefresh)
					msgRender(cache.getAsString(userId + "_msgList"), isrefresh);
			}
		} catch (Exception e) {
			Log.d(TAG, ExceptionsOperator.getExceptionInfo(e));
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
				devAdapter = new DevAdapter(context);
				devAdapter.addItem(devList);
				listView.setAdapter(devAdapter);
				if(BackLoginThread.state == 200){
					APP.dismissProgressDialog();
				}
				break;
			case XMSG.MSG_LIST_LOAD:
				if(adapter == null){
					adapter = new MsgAdapter2(context,listViewMsg);
					adapter.addItem(msgList);
					listViewMsg.setAdapter(adapter);
				}else{
					adapter.updateList(msgList);
				}
				break;
			case 1001:
				OnMessage(msg);
				break;
			case XMSG.MULTI_LIST_LOAD:
				if(m_expandableListViewAdapter == null){
					m_expandableListViewAdapter = new ExpandableListViewAdapter(context);
					m_expandableListViewAdapter.addItem(m_devicesList);
					m_pullToRefreshExpandableListView.setAdapter(m_expandableListViewAdapter);
					m_pullToRefreshExpandableListView.setGroupIndicator(null);
				}else{
					m_expandableListViewAdapter.updateList(m_devicesList);
				}
				break;
			}
		}
	}
	
	
	@Override
	public void OnMessage(android.os.Message msg) {
		switch (msg.what) {
		case 1001:
			// 开始异步功能操作
			APP.ShowWaitDlg(this, R.string.querying, 3, msg.obj);
			break;
		}
	}
	
	
	public static boolean _isOpen = true;
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
				case 100://直播收藏
					Intent intent=new Intent(Intent.ACTION_VIEW);
			        intent.setClassName(ACT, "com.views.NewWebActivity"); 
			        String url = Constants.hostUrl + "/LiveAction_toPlays?lc.deviceId="+device.sid;
			        Log.d(TAG, "play live url:" + url +" type= "+ device.type);
			        intent.putExtra("url", url);
			        intent.putExtra("playType", "0");
					APP.GetMainActivity().startActivity(intent);
					break;
				case 101://短片收藏
					Intent intent1=new Intent(Intent.ACTION_VIEW);
			        intent1.setClassName(ACT, "com.views.NewWebActivity"); 
			        String url1 = Constants.hostUrl + "/LiveAction_playVideo?lc.deviceId="+device.sid;
			        Log.d(TAG, "play live url:" + url1);
			        intent1.putExtra("url", url1);
			        intent1.putExtra("playType", "1");
					APP.GetMainActivity().startActivity(intent1);
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
		if(_localVideo == null){
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
		}else if(_localIndex == 1){
			getFragmentView(_localIndex);
		}
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
				scrollViewDev = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_main);
				scrollViewDev.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
					@Override
					public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
						Loger.print("devicelist pull to refresh!");
						isrefresh = true;
						APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
					}
				});
				isrefresh = true;
			}
			//有缓存 就读取缓存  没有缓存 则请求服务器
			if(cache.getAsString(userId + "_devices")==null){
				LogUtil.d(TAG, userId + "用户，没有设备列表的缓存，直接加载!");
				APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
			}else{
				LogUtil.d(TAG, userId + "用户，有设备列表的缓存!");
				if(isrefresh)
					render(cache.getAsString(userId + "_devices"), cache.getAsString(userId + "_collects"), isrefresh);
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 加载多画面设备列表
	 */
	public void loadDevMultiList(){
		try {
			if(m_pullToRefreshExpandableListView == null){
				_devCartsList = new ArrayList<DevCart>();
				m_pullToRefreshExpandableListView = (PullToRefreshExpandableListView) findViewById(R.id.myhome_devslist_refreshview);
				m_pullToRefreshExpandableListView.setOnRefreshListener(new OnPullableRefreshListener());
				m_pullToRefreshExpandableListView.setOnChildClickListener(new OnClickDevicesChildListListener());
				m_playView = findViewById(R.id.myhome_devslist_bottomlayout03);
				m_playView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							if (_devCartsList.size() > 0){
								Intent intent = new Intent(ACT, Fun_RealPlayerActivity.class);
								//intent.setClass(ACT, Fun_RealPlayerActivity.class);
								if(_devCartsList!=null && _devCartsList.size()>0){
									intent.putExtra("RealPlayer_devices", (Serializable) _devCartsList);
								}
								ACT.startActivity(intent);
							}else{
								APP.ShowToast(APP.GetString(R.string.home_select_chn));
							}
						} catch (Exception e) {
							System.out.println(111);
						}
					}
				});
				isrefresh = true;
			}
			//有缓存 就读取缓存  没有缓存 则请求服务器
			if(cache.getAsString(userId + "_devices")==null){
				LogUtil.d(TAG, userId + "用户，没有设备列表的缓存，直接加载!");
				APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.DEVICE_LIST_LOAD, 0);
			}else{
				LogUtil.d(TAG, userId + "用户，有设备列表的缓存!");
				if(isrefresh)
					renderMulti(cache.getAsString(userId + "_devices"), isrefresh);
			}
		} catch (Exception e) {
		}
	}	
	// 下拉刷新相应事件
	public class OnPullableRefreshListener implements com.adapter.PullToRefreshExpandableListView.OnRefreshListener{
		@Override
		// 下拉刷新操作
		public void onRefresh(){
			isrefresh = true;
			
			APP.ShowWaitDlg(NewMain.this, R.string.openning_ReloadData, XMSG.MULTI_LIST_LOAD, 0);
			
		}

	}
	
	// Child点击的监听器
	class OnClickDevicesChildListListener implements ExpandableListView.OnChildClickListener{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id){
			if(m_devicesList.get(groupPosition).online == 0){
				APP.ShowToast(SDK.GetErrorStr(-896));//设备不在线
				return false;
			}
			// 当前选中的devCart
			DevCart devCart = new DevCart();
			devCart.setDeviceInfo(m_devicesList.get(groupPosition));
			devCart.setChannelNum(childPosition + 1);
			
			// 判断数组中是否存在当前的devCart
			boolean isSelected = false;
			int select_index = -1;
			for (int i = 0; i < _devCartsList.size(); i++){
				DevCart _devCart = _devCartsList.get(i);
				if (_devCart.getDeviceInfo().getSid().equals(devCart.getDeviceInfo().getSid()) && devCart.getChannelNum() == _devCart.getChannelNum()){
					isSelected = true;
					select_index = i;
				}
			}

			// 当前通道已选中
			if (isSelected){
				_devCartsList.remove(select_index);
			}else{// 当前通道未选中
				if (_devCartsList.size() >= 4){
					Toast.makeText(context, R.string.alertMsg23, Toast.LENGTH_SHORT).show();
				}else{
					_devCartsList.add(devCart);
				}
			}
			m_expandableListViewAdapter.m_devCartsList.clear();
			m_expandableListViewAdapter.m_devCartsList.addAll(_devCartsList);
			m_expandableListViewAdapter.notifyDataSetChanged();
			return true;
		}

	}
	
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
	 * 加载设备列表 1.设备 2.多画面
	 */
	public int loadDevData2(int type) {
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
						if(type == 1){
							render(str, json.getString("collects"), isrefresh);
						}else{
							renderMulti(str, isrefresh);
						}
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
				String str;
				try {
					str = json.getString("data");
					//str = "[{\"logtime\":\"2016-03-02 06:16:50.0\",\"evt_type\":1,\"evt_time\":\"2016-03-02 06:15:54.0\",\"evt_state\":1,\"typename\":null,\"thumb_url\":\"NoSuchKey\",\"evt_vsize\":4591868,\"devicename\":\"0IPC(manniu203)\",\"uuid\":\"VFMhAQEAAGUwNjFiMjAxMGJmOAAA\",\"evt_picture\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160302061555_fa4aaf3f.jpg?Expires=1456913110&OSSAccessKeyId=3WVh0lsoA8r5uHH6&Signature=MNKgzjOIbINevgPFEC7C9S/8zDw%3D\",\"kid\":72137,\"evt_video\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160302061555_1df28238.mp4\"},{\"logtime\":\"2016-03-01 22:16:14.0\",\"evt_type\":1,\"evt_time\":\"2016-03-01 22:15:17.0\",\"evt_state\":1,\"typename\":null,\"thumb_url\":\"NoSuchKey\",\"evt_vsize\":4595006,\"devicename\":\"0IPC(manniu203)\",\"uuid\":\"VFMhAQEAAGUwNjFiMjAxMGJmOAAA\",\"evt_picture\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160301221517_8a205bb0.jpg?Expires=1456913110&OSSAccessKeyId=3WVh0lsoA8r5uHH6&Signature=lZWiv6be9X8in604jb58JOJdprw%3D\",\"kid\":72113,\"evt_video\":\"http:man-niu.oss-cn-hangzhou.aliyuncs.com/bc565120/cam_0/20160301221518_6343fe85.mp4\"}]";
					if("nologin".equals(str)){
						LogUtil.d(TAG, "报警信息..session超时");
					}else if(str.equals("[]") && pageNo > 1){
						result = SDK.Err_Last_page;
					}else{
						cache.put(userId + "_msgList", str);
						msgRender(str,isrefresh);
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
					Message msg = JSON.toJavaObject((JSON)array.get(i), Message.class);
					msgList.add(msg);
				}
			}
			_handler.sendEmptyMessage(XMSG.MSG_LIST_LOAD);//通过消息更新数据
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
					if(live.getType() == 0){
						dev.type = 100;//直播收藏 
					}else{
						dev.type = 101;//短片收藏 
					}
					devList.add(dev);
				}
			}
			_handler.sendEmptyMessage(XMSG.DEVICE_LIST_LOAD);//通过消息更新数据
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	/**
	 * 渲染多画面列表 
	 * @param isrefresh
	 */
	private void renderMulti(String json,boolean isrefresh){
		try {
			if(m_devicesList == null){
				m_devicesList = new ArrayList<Device>();
			}
			m_devicesList.clear();
			if(!json.equals("{}")){
				JSONArray array = JSON.parseArray(json);
				for(int i = 0; i < array.size(); i++){
					Device dev1 = JSON.toJavaObject((JSON)array.get(i), Device.class);
					m_devicesList.add(dev1);
				}
			}
			_handler.sendEmptyMessage(XMSG.MULTI_LIST_LOAD);//通过消息更新数据
		} catch (Exception e) {
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
            ((ViewPager) view).removeView((View)arg2);  
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
				ret = loadDevData2(1);
				return ret;
			case XMSG.MULTI_LIST_LOAD://多画面
				ret = loadDevData2(2);
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
				Message alrmdata = (Message)obj;
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
				LogUtil.d(TAG, "isrefresh =  "+isrefresh);
				if(isrefresh){
					scrollViewDev.onRefreshComplete();//关闭下拉刷新
				}
				break;
			case XMSG.MULTI_LIST_LOAD://多画面
				isrefresh = false;
				m_pullToRefreshExpandableListView.onRefreshComplete();
				break;
			case XMSG.MSG_LIST_LOAD:
				Integer mRet = (Integer) ret;
				if (mRet != 0) {	// 打开消息列表失败
					APP.ShowToast(SDK.GetErrorStr(mRet));
				}
				if(isrefresh){
					scrollView.onRefreshComplete();//关闭下拉刷新
				}
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
							MsgAdapter2._isOpenAlarm = true;
						}else{
							Constants.evt_video = str;
	                    	Intent intent = new Intent(context, Fun_RecordPlay.class);
	                    	intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  
	                    	context.startActivity(intent);
							LogUtil.d("MsgAdapter","HttpURLConnectionTools  url.....");
						}
					} catch (JSONException e) {
						MsgAdapter2._isOpenAlarm = true;
						LogUtil.d("MsgAdapter", ExceptionsOperator.getExceptionInfo(e));
						APP.ShowToast(SDK.GetErrorStr(-1));
					}
				}else{
					MsgAdapter2._isOpenAlarm = true;
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
