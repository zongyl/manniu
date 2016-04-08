package com.views;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.Timer;

import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.adapter.DevAdapter.ViewHolder;
import com.adapter.HttpUtil;
import com.backprocess.BackLoginThread;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.basic.APP;
import com.basic.XMSG;
import com.ctrl.PopMenu;
import com.ctrl.PopupMenu;
import com.ctrl.XImageBtn;
import com.jpush.MyReceiver;
import com.manniu.manniu.R;
import com.utils.ConfigXml;
import com.utils.Constants;
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.Loger;
import com.utils.SetSharePrefer;
import com.utils.UpdateManager;
import com.views.bovine.Fun_AnalogVideo;

public class Main extends Activity implements OnClickListener {
	public static Main Instance = null;			// 全局唯一Active，方便其它view使用
	private String TAG = Main.class.getSimpleName();
	
	public LocationClient locationClient = null;
	public BDLocationListener bdLocationListener = new MyLocationListener();
	
	//public final static int XV_REALPLAY = 0;//IPC视频
	public final static int XV_NEW_MAIN = 0;//新版本 . 主页面
	public final static int XV_NEW_MSG = 1;//牛眼
	public final static int XV_NEW_WEB = 2;//新版本 . web
	public final static int XV_NEW_MORE = 3;//新版本 . 更多
	//public final static int XV_MEDIA = 4;//测试页
	//public final static int XV_RECORDPLAY = 4;//录像播放
	
	//public static List<MenuInfo> menulists;
	public Button _btnMainMore,
	_btnMoreAlarm, 
	btn_mes, 
	btn_msgs, 
	btn_webs, 
	btn_mores, 
	btn_addDevice,
	btn_main_tab_1,//设备
	//btn_main_tab_2, //收藏
	btn_main_tab_3,//设备
	btn_main_tab_multi,//多画面
	btn_main_tab_4; //本地
	
	XImageBtn _showMain, btn_me, btn_msg, btn_web, btn_more, btn_add, btn_share, /*btn_share1,*/ btn_webmore, btn_msgedit, btn_qrcode;
	
	View main_tab;
	
	TextView title,titleMore/*, titleSquTv*/, sq_live, sq_short/*,sq_nearby*/;
	
	LinearLayout titleSqu;
	
	Timer _timer;
	private Context context;
	// 屏幕常亮功能
	private PowerManager.WakeLock _wakeLock = null;
	LinearLayout _contentView = null;

	TextView _tvTitle = null;		// 标题
	public int _curIndex = XV_NEW_MAIN;	// 当前显示功能
	int _lastIndex = XV_NEW_MAIN; //最后一次点击功能
	XViewBasic[] _xViews = new XViewBasic[4];		// 功能对象数组      数组和ViewFlipper中的成员要一一对应
	Stack<Integer> _lastXV = new Stack<Integer>();	// 显示堆栈

	MyHandler _handler;		// 消息对象
	ViewFlipper _vf;		// 页面切换
	
	/*//声明通知（消息）管理器
	NotificationManager	_notificationManager;
	Intent _intent;
	PendingIntent _pendingIntent;
	//声明Notification对象
	Notification _notification;*/
	FrameLayout _layoutHead;
	
	//检查网络广播接收器
	//NetWakeReceiver netReceiver;
	
	public BackLoginThread _loginThead = null;//4楼软解码线程
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Loger.print("Main onCreate time:" + NewLogin.sdf.format(new Date()));
		BaseApplication.getInstance().addActivity(this);
		
		// 设置为无标题格式
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		context = this.getApplicationContext();
		
		locationClient = new LocationClient(context);
		locationClient.registerLocationListener(bdLocationListener);
		//menulists = MenuUtils.getMenuList(0,0);
		ConfigXml xml = new ConfigXml(this);
        xml.getElementTextValue();
		Instance = this;
		_handler = new MyHandler();
		APP.Init(this);
		//加载布局文件
		setContentView(R.layout.main);
		APP.RegHandler(R.layout.main, _handler); // 注册到APP中心
		// 界面常用View保存
		_tvTitle = (TextView) findViewById(R.id.tvTitle);
		_vf = (ViewFlipper) findViewById(R.id.viewflipper);
		_showMain = (XImageBtn) findViewById(R.id.btnShowMain);
		_showMain.setOnClickListener(this);
		_layoutHead = (FrameLayout) this.findViewById(R.id.layoutHead);
		
		//+ 按钮   添加设备
		btn_add = (XImageBtn)findViewById(R.id.btn_add);
		btn_add.setOnClickListener(this);
		//分享按钮
		btn_share = (XImageBtn)findViewById(R.id.btn_share);
		btn_share.setOnClickListener(this);
		
		btn_addDevice = (Button) this.findViewById(R.id.btn_addDevice);
		btn_addDevice.setOnClickListener(this);

		btn_webmore = (XImageBtn)findViewById(R.id.btn_webmore);
		btn_webmore.setOnClickListener(this);

		btn_msgedit = (XImageBtn)findViewById(R.id.btn_msgedit);
		btn_msgedit.setOnClickListener(this);

		btn_qrcode = (XImageBtn)findViewById(R.id.btn_qrcode);
		btn_qrcode.setOnClickListener(this);
		
		btn_mes = (Button)findViewById(R.id.btn_mes);
		btn_msgs = (Button)findViewById(R.id.btn_msgs);
		btn_webs = (Button)findViewById(R.id.btn_webs);
		btn_mores = (Button)findViewById(R.id.btn_mores);

		btn_mes.setOnClickListener(this);
		btn_msgs.setOnClickListener(this);
		btn_webs.setOnClickListener(this);
		btn_mores.setOnClickListener(this);
		
		title = (TextView)findViewById(R.id.tvTitle);
		titleMore =(TextView) findViewById(R.id.titleMore);
		//titleSquTv =(TextView) findViewById(R.id.titleSquTv);//广场title
		titleSqu =(LinearLayout) findViewById(R.id.titleSqu);
		
		sq_live  = (TextView) findViewById(R.id.sq_live);
		sq_short = (TextView) findViewById(R.id.sq_short);
		//sq_nearby = (TextView) findViewById(R.id.sq_near);
		
		sq_live.setOnClickListener(this);
		sq_short.setOnClickListener(this);
		//sq_nearby.setOnClickListener(this);
		
		main_tab = findViewById(R.id.main_tab);
		btn_main_tab_1 = (Button)findViewById(R.id.main_tab_1);
	//	btn_main_tab_2 = (Button)findViewById(R.id.main_tab_2);
		btn_main_tab_3 = (Button)findViewById(R.id.main_tab_3);
		btn_main_tab_multi = (Button)findViewById(R.id.main_tab_multi);
		btn_main_tab_4 = (Button)findViewById(R.id.main_tab_4);

		btn_main_tab_1.setOnClickListener(this);
	//	btn_main_tab_2.setOnClickListener(this);
		btn_main_tab_3.setOnClickListener(this);
		btn_main_tab_multi.setOnClickListener(this);
		btn_main_tab_4.setOnClickListener(this);
		//netReceiver = new NetWakeReceiver();
		_loginThead = new BackLoginThread();
		_loginThead.start();
		//_loginThead.waitIDMLogin();
		
		//if(NewLogin.instance != null) NewLogin.instance.closeLogin();
		_xViews[XV_NEW_MAIN] = new NewMain(this, R.id.new_main, APP.GetString(R.string.cloud));
//		_xViews[XV_NEW_MAIN] = new Fun_Cloud(this, R.id.fun_cloud, APP.GetString(R.string.niuyan));
		_xViews[XV_NEW_MSG] = new Fun_AnalogVideo(this, R.id.lay_setting, APP.GetString(R.string.niuyan));
		_xViews[XV_NEW_WEB] = new NewSquare(this, R.id.new_web, APP.GetString(R.string.square));
		_xViews[XV_NEW_MORE] = new NewMoresMe(this, R.id.new_more, APP.GetString(R.string.personel));
		//_xViews[XV_RECORDPLAY] = new Fun_RecordPlay3(this, R.id.fun_recordplay, "录像播放");
		//_xViews[XV_MEDIA] = new Fun_Cloud(this, R.id.fun_cloud, Constants.childTitle);
		
		_curIndex = XV_NEW_MORE;
		ShowXView(XV_NEW_MAIN);
		
		change(R.id.btn_mes, R.drawable.common_bar_cloud_sel);
		change(btn_add, btn_qrcode);
		//_handler.sendEmptyMessageDelayed(XMSG.UPDATEA_APP, 10000); //延迟发送升级提示  -- 测试注了,打包时开启来
		MyReceiver.isCloseApp = true;
		initLocation();
		//删除过期的日志
		LogUtil.deleteSDcardExpiredLog();
	}
	
	/**
	 * 初始化定位
	 */
	private void initLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setScanSpan(1000);
		option.setIsNeedAddress(true);
		option.setIsNeedLocationDescribe(true);
		//option.setCoorType("");
		locationClient.setLocOption(option);
	}

	/**
	 * 定位方法
	 */
	public void startLocation(){
		locationClient.start();
	}
	
	private class MyLocationListener implements BDLocationListener{
		@Override
		public void onReceiveLocation(BDLocation location) {
			StringBuffer sb = new StringBuffer();
			sb.append("time:");
			sb.append(location.getTime());
			sb.append("\naltitude:");
			sb.append(location.getAltitude());
			sb.append("\nlatitude:");
			sb.append(location.getLatitude());// 获取纬度信息
			sb.append("\nlongitude:");
			sb.append(location.getLongitude());// 获取经度信息
			sb.append("\ncity:");
			sb.append(location.getCity());
			sb.append("\nerror code:");
			sb.append(location.getLocType());
			sb.append("\nerror address:");
			sb.append(location.getAddrStr());
			sb.append("\nerror locationDescribe:");
			sb.append(location.getLocationDescribe());
			LogUtil.d("BaiduLocation:", sb.toString());
			if(location.getLocType() == 161){
				locationClient.stop();
			}
			
			JSONObject json = new JSONObject();
			try {
				json.put("latitude", location.getLatitude());
				json.put("longitude", location.getLongitude());
				json.put("address", location.getAddrStr());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			Message msg = new Message();
			msg.what = XMSG.LOCATION;
			Bundle data = new Bundle();
			data.putString("location", json.toString());
			msg.setData(data);
			_handler.sendMessage(msg);
		}
	}
	
	
	public void stopUpdateCheck() {
		if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
	}
	
	public void executeCheck(){
		@SuppressWarnings("static-access")
		String checkTime = APP.GetMainActivity().getSharedPreferences("Info_Login",APP.GetMainActivity().MODE_PRIVATE).getString("check_time", "");
		String timeToday = DateUtil.getCurrentStringDate("yyyyMMdd");
		/*if(timeToday.equals(checkTime)){
			return;
		}*/
		UpdateManager.getUpdateManger().checkAppUpdate(_xViews[_curIndex].ACT, false,true);
		SetSharePrefer.write("Info_Login", "check_time", timeToday);
	}
	
	public NewMain getNewMain(){
		return (NewMain)_xViews[XV_NEW_MAIN];
	}
	
	//添加设备成功之后 调此方法
	public void NewMainreLoad(){
		NewMain.instance.cache.remove(Constants.userid + "_devices");
		((NewMain)_xViews[XV_NEW_MAIN]).loadDevList();
		//((Fun_Cloud)_xViews[XV_NEW_MAIN]).loadDevList();
	}
	
	/**
	 * 
	 * @param index 0 1 2 分别代表  设备、报警、本地
	 */
	public void NewMainItem(int index){
		APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
		change(R.id.btn_mes, R.drawable.common_bar_cloud_sel);
		change(btn_qrcode, btn_add);
		tab(index);
		((NewMain)_xViews[XV_NEW_MAIN]).setCurrentItem(index);
	}
	
	public void NewMainreLoadImg(){
		((NewMain)_xViews[XV_NEW_MAIN]).loadLocalVideoAndImg();
	}
	
	public void ShowXView(int index) {
		_lastIndex = _curIndex;
		if (_curIndex == index) {
			return;
		}
		
		if(index == XV_NEW_MSG/* || index == XV_NEW_MAIN*/){ //牛眼或云端 不显示MAIN的主菜单
			_layoutHead.setVisibility(View.GONE);
		}else{
			_layoutHead.setVisibility(View.VISIBLE);
		}
		
		/*if(index == XV_RECORDPLAY){
			_layoutHead.setVisibility(View.GONE);
			findViewById(R.id.layOutBottom2).setVisibility(View.GONE);
		}else{
			findViewById(R.id.layOutBottom2).setVisibility(View.VISIBLE);
		}*/
		
//		if(index == XV_REALPLAY){
//			_layoutHead.setVisibility(View.GONE);
//			findViewById(R.id.layOutBottom2).setVisibility(View.GONE);
//		}else{
//			_layoutHead.setVisibility(View.VISIBLE);
//			findViewById(R.id.layOutBottom2).setVisibility(View.VISIBLE);
//		}
		
		//显示第几个子项
		_vf.setDisplayedChild(index);

		_xViews[_curIndex].OnVisibility(View.INVISIBLE);//view隐藏起来
		String temTitle = _xViews[index].GetTitle();
		/*if(index == XV_MEDIA && Constants.childFlge == 1){
			temTitle = Constants.childTitle;
		}*/
		
		_tvTitle.setText(temTitle);
		_xViews[index].OnVisibility(View.VISIBLE); //显示
		_curIndex = index;
		
	}
	
	/*public void onStart(){
		super.onStart();
        //注册广播接收器
		IntentFilter intentFilter = new IntentFilter(); 
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
		intentFilter.setPriority(1000); 
		registerReceiver(netReceiver, intentFilter);
    }
    
    @Override
    public void onStop(){
        //取消广播接收器
        if (netReceiver != null) {
        	unregisterReceiver(netReceiver);
    	}
        super.onStop();
    }*/

	@Override
	protected void onDestroy() {
//		_mapView.destroy();
//		mBMapManager.destroy();
		super.onDestroy();
	}

	//我们对Acitivity的进行管理时，特别是想利用它的生命周期来巧妙进行一些操作时，我们一定要注意，
	//不要随意删改方法自带的调用基类的super.XX()方法
	@Override
	protected void onPause() {
//		_mapView.onPause();
//		mBMapManager.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		_mapView.onResume();
//		mBMapManager.start();
		// 启用屏幕常亮功能
		_wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "MyActivity");
		_wakeLock.acquire();
		System.gc();
	}
	
	
	//主界面返回按钮事件
	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.btn_mes:
				APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
				change(R.id.btn_mes, R.drawable.common_bar_cloud_sel);
				change(btn_qrcode, btn_add);
				break;
			case R.id.btn_msgs: 	
				APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MSG);//XV_SETTING 牛眼
				change(R.id.btn_msgs, R.drawable.common_bar_eye_sel);
				change(btn_msgedit);
				break;
			case R.id.btn_webs: 	
				APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_WEB);
				change(R.id.btn_webs, R.drawable.common_bar_find_sel);
				//change(btn_webmore);
				change(titleSqu);
				break;
			case R.id.btn_mores: 	
				APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MORE);
				change(R.id.btn_mores, R.drawable.common_bar_me_sel);
				change(titleMore);
				break;
			case R.id.btn_share:
				/*ShareSDK.initSDK(this);
				OnekeyShare oks = new OnekeyShare();
				oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
				oks.setText(getString(R.string.share_txt));
				oks.setImageUrl("http://www.9wingo.com/images/qrcode.png");
				oks.show(this);*/
				APP.ShowToast("starting...");
				locationClient.start();
				break;
			/*case R.id.btn_share1:
				APP.ShowToast("stoping...");
				locationClient.stop();
				break;*/
			case R.id.btn_add:
				forward("com.views.NewMainAddDev");
				break;
			case R.id.btn_webmore:
				createMenu(v);
				break;
			case R.id.btn_msgedit:
				NewMsg msg = (NewMsg)_xViews[XV_NEW_MSG];
				msg.edit();
				showOrHide(R.id.layOutBottom2);
				showOrHide(R.id.msg_frame_bom);
				/*if(findViewById(R.id.layOutBottom2).getVisibility() == 0){
					findViewById(R.id.layOutBottom2).setVisibility(View.GONE);
				}else{
					findViewById(R.id.layOutBottom2).setVisibility(View.VISIBLE);
				}*/
				break;
			case R.id.main_tab_1://设备
				//tab(0);
				((NewMain)_xViews[XV_NEW_MAIN]).setCurrentItem(0);
				break;
			case R.id.main_tab_3://报警
				//tab(1);
				((NewMain)_xViews[XV_NEW_MAIN]).setCurrentItem(1);
				break;
			case R.id.main_tab_multi://多画面
				((NewMain)_xViews[XV_NEW_MAIN]).setCurrentItem(2);
				break;
			case R.id.main_tab_4://本地
				//tab(2);
				((NewMain)_xViews[XV_NEW_MAIN]).setCurrentItem(3);
				break;
			case R.id.btn_addDevice://添加模拟摄像机
				Intent intent2 = new Intent(Intent.ACTION_VIEW);
		        intent2.setClassName(this, "com.views.About_MobilephoneActivity");  
		        startActivity(intent2);
				break;
			case R.id.btn_qrcode://扫一扫
				forward("com.mining.app.zxing.decoding.QRcode_DecodeActivity");
				break;
			//广场分类点击事件
			case R.id.sq_live:
				changeTitleBg(sq_live);
				//NewSquare.instance.getFragmentView(0);
				break;
			case R.id.sq_short:
				changeTitleBg(sq_short);
				//NewSquare.instance.getFragmentView(1);
				break;
			/*case R.id.sq_near:
				changeTitleBg(sq_nearby);
				//NewSquare.instance.getFragmentView(2);
				break;*/
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			return;
		}
	}
	
	
	
	public void changeTitleBg(View v){
		switch(v.getId()){
		case R.id.sq_live:
			NewSquare.instance.getFragmentView(0);
			sq_live.setBackgroundResource(R.drawable.label_bg_sel);
			sq_live.setTextColor(getResources().getColor(R.color.white));
			//sq_nearby.setTextColor(getResources().getColor(R.color.blue_menu));
			//sq_nearby.setBackgroundDrawable(null);
			sq_short.setTextColor(getResources().getColor(R.color.blue_menu));
			sq_short.setBackgroundDrawable(null);
			break;
		case R.id.sq_short:
			NewSquare.instance.getFragmentView(1);
			sq_live.setTextColor(getResources().getColor(R.color.blue_menu));
			sq_live.setBackgroundDrawable(null);
			//sq_nearby.setTextColor(getResources().getColor(R.color.blue_menu));
			//sq_nearby.setBackgroundDrawable(null);
			sq_short.setBackgroundResource(R.drawable.label_bg_sel);
			sq_short.setTextColor(getResources().getColor(R.color.white));
			break;
		/*case R.id.sq_near:
			//NewSquare.instance.getFragmentView(2);
			sq_live.setTextColor(getResources().getColor(R.color.blue_menu));
			sq_live.setBackgroundDrawable(null);
			sq_short.setTextColor(getResources().getColor(R.color.blue_menu));
			sq_short.setBackgroundDrawable(null);
			sq_nearby.setBackgroundResource(R.drawable.label_bg_sel);
			sq_nearby.setTextColor(getResources().getColor(R.color.white));
			break;*/
		}
	}
	
	private void forward(String activity){
		Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setClassName(this, activity);  
        startActivity(intent);
	}
	
	public void setValue(int count){
		Button btn = (Button)findViewById(R.id.msg_bom_del);
		if(count > 0){
			btn.setText(getString(R.string.delete)+count);
		}else{
			btn.setText(getString(R.string.delete));
		}
	}
	
	private void showOrHide(int resId){
		if(findViewById(resId).getVisibility() == 0){
			findViewById(resId).setVisibility(View.GONE);
		}else{
			findViewById(resId).setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 下面的按钮显示
	 * @param resId
	 * @param drawableId
	 */
	private void change(int resId, int drawableId){
		findViewById(R.id.btn_mes).setBackgroundResource(R.drawable.common_bar_cloud);
		findViewById(R.id.btn_msgs).setBackgroundResource(R.drawable.common_bar_eye);
		findViewById(R.id.btn_webs).setBackgroundResource(R.drawable.common_bar_find);
		findViewById(R.id.btn_mores).setBackgroundResource(R.drawable.common_bar_me);
		btn_mes.setTextColor(getResources().getColor(R.color.gray));
		btn_msgs.setTextColor(getResources().getColor(R.color.gray));
		btn_webs.setTextColor(getResources().getColor(R.color.gray));
		btn_mores.setTextColor(getResources().getColor(R.color.gray));
		findViewById(resId).setBackgroundResource(drawableId);
		((Button)findViewById(resId)).setTextColor(getResources().getColor(R.color.blue_menu));
	}
	
	/**
	 *以最省内存的方式读取本地资源的图片
	 * @return
	 */
	private Bitmap getBitMap(String path){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		return BitmapFactory.decodeFile(path, opt);
	}
	
	private BitmapDrawable getBitmapDrawable(int resId){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		InputStream is = getResources().openRawResource(resId);
		Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
		BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
		//setBackgroundDrawable(bd);
		return bd;
	} 
	
	/**
	 * 上面的按钮  
	 */
	/*private void change(XImageBtn... btns){
		title.setVisibility(View.GONE);
		_showMain.setVisibility(View.GONE);
		btn_add.setVisibility(View.GONE);
		btn_share.setVisibility(View.GONE);
		btn_webmore.setVisibility(View.GONE);
		btn_msgedit.setVisibility(View.GONE);
		main_tab.setVisibility(View.GONE);
		btn_qrcode.setVisibility(View.GONE);
		for(XImageBtn btn : btns){
			btn.setVisibility(View.VISIBLE);
			if(btn.getId() == R.id.btn_add){
				title.setVisibility(View.GONE);
				titleMore.setVisibility(View.GONE);
				titleSqu.setVisibility(View.GONE);
				main_tab.setVisibility(View.VISIBLE);
			}
		}
	}*/
	
	private void change(View... btns){
		title.setVisibility(View.GONE);
		_showMain.setVisibility(View.GONE);
		btn_add.setVisibility(View.GONE);
		btn_share.setVisibility(View.GONE);
		btn_webmore.setVisibility(View.GONE);
		btn_msgedit.setVisibility(View.GONE);
		main_tab.setVisibility(View.GONE);
		btn_qrcode.setVisibility(View.GONE);
		for(View btn : btns){
			btn.setVisibility(View.VISIBLE);
			if(btn.getId() == R.id.btn_add){
				title.setVisibility(View.GONE);
				titleMore.setVisibility(View.GONE);
				titleSqu.setVisibility(View.GONE);
				main_tab.setVisibility(View.VISIBLE);
			}
		}
	}
	
	
	private void change(TextView v){
		title.setVisibility(View.GONE);
		_showMain.setVisibility(View.GONE);
		btn_add.setVisibility(View.GONE);
		btn_share.setVisibility(View.GONE);
		btn_webmore.setVisibility(View.GONE);
		btn_msgedit.setVisibility(View.GONE);
		main_tab.setVisibility(View.GONE);
		btn_qrcode.setVisibility(View.GONE);
		if(v.getText().toString().equals(context.getString(R.string.personel))){
			titleMore.setVisibility(View.VISIBLE);
			titleSqu.setVisibility(View.GONE);
		}else{
			titleSqu.setVisibility(View.VISIBLE);
			titleMore.setVisibility(View.GONE);
		}
	}
	
	public void tab(){
		APP.ShowToast(getString(R.string.alert_msg));
	}
	
	class BmpHandler extends Handler{
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			Log.d(TAG, "bmpHandler:"+msg);
			byte[] bytes = msg.getData().getByteArray("bytes");
			Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			iv.setImageBitmap(bmp);
		}
	}
	
	Runnable runnable = new Runnable(){
		@Override
		public void run() {
			byte[] bytes = HttpUtil.executeGetBytes(getResources().getString(R.string.server_address)+"/images/1.jpeg");
			android.os.Message msg = new android.os.Message();
			Bundle data = new Bundle();
			data.putByteArray("bytes", bytes);
			msg.setData(data);
			bmpHandler.sendMessage(msg);
		}
	};
	
	BmpHandler bmpHandler;
	ImageView iv;
	
	/**
	 * 更新 ListView
	 */
	public void updateView(ViewHolder viewHolder){
		ListView listView = (ListView)findViewById(R.id.main_list);
		Log.d(TAG, "firstVisible:"+listView.getFirstVisiblePosition());
		Log.d(TAG, "childCount:"+listView.getChildCount());
		Log.d(TAG, "position:"+listView.getCheckedItemPosition());
		iv = viewHolder.iv;
		bmpHandler = new BmpHandler();
		new Thread(runnable).start();
	}
	//tab按钮事件 
	public void tab(int index){
		btn_main_tab_1.setTextColor(getResources().getColor(R.color.gray));
		//btn_main_tab_2.setTextColor(getResources().getColor(R.color.gray));
		btn_main_tab_3.setTextColor(getResources().getColor(R.color.gray));
		btn_main_tab_multi.setTextColor(getResources().getColor(R.color.gray));
		btn_main_tab_4.setTextColor(getResources().getColor(R.color.gray));
		switch (index) {
		case 0:
			btn_main_tab_1.setTextColor(getResources().getColor(R.color.blue_menu));
			break;
		/*case 1:
			btn_main_tab_2.setTextColor(getResources().getColor(R.color.blue_menu));
			break;*/
		case 1:
			btn_main_tab_3.setTextColor(getResources().getColor(R.color.blue_menu));
			break;
		case 2:
			btn_main_tab_multi.setTextColor(getResources().getColor(R.color.blue_menu));
			break;
		case 3:
			btn_main_tab_4.setTextColor(getResources().getColor(R.color.blue_menu));
			break;
		default:
			break;
		}
		
		
		/*if(index == 1){
			btn_main_tab_1.setBackgroundResource(R.drawable.tab_left_sel);
			btn_main_tab_1.setTextColor(getResources().getColor(R.color.white));
			btn_main_tab_2.setBackgroundResource(R.drawable.tab_right);
			btn_main_tab_2.setTextColor(getResources().getColor(R.color.blue_menu));
		}else if(index == 2){
			btn_main_tab_1.setBackgroundResource(R.drawable.tab_left);
			btn_main_tab_1.setTextColor(getResources().getColor(R.color.blue_menu));
			btn_main_tab_2.setBackgroundResource(R.drawable.tab_right_sel);
			btn_main_tab_2.setTextColor(getResources().getColor(R.color.white));
		}*/
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
		case 1:
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				//显示扫描到的内容
				//mTextView.setText(bundle.getString("result"));
				
				APP.ShowToast(bundle.getString("result"));
				
				//显示
				//mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
			}
			break;
		}
    }
	
	//创建更多下拉菜单
	public void createMenu(View v) {
		PopupMenu menu = new PopupMenu(context);
		//menu.setHeaderTitle("用户菜单");
		menu.setOnItemSelectedListener(this);
		menu.add(0, R.string.web_refresh).setParams(findViewById(R.id.webView01));
		menu.add(1, R.string.web_copy_link).setParams(((WebView)findViewById(R.id.webView01)).getUrl());
		menu.add(2, R.string.web_on_browser).setParams(((WebView)findViewById(R.id.webView01)).getUrl());
		menu.add(3, R.string.clip_show).setParams();
		
		/*menu.setOnItemSelectedListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				APP.ShowToast(""+v.getBackground());
			}
		});*/
		menu.show(v);
	}
	
	PopMenu popMenu;
	public void createMenu1(View v){
		popMenu = new PopMenu(context);
		popMenu.addItems(new String[]{"AAAAA","BBBBB","CCCCC"});
		// 菜单项点击监听器
		popMenu.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				System.out.println("下拉菜单点击" + position);
				popMenu.dismiss();
			}
		});
		popMenu.showAsDropDown(v);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void reLoad2(){
		if(isChangingConfigurations())
		//	SDK.Logout();
		if(isChangingConfigurations())
			reLoadLogin();
	}
	
	@SuppressWarnings("static-access")
	public void reLoadLogin(){
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(NewLogin.SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		String strIP = preferences.getString("ip0", "");
		String user = preferences.getString("user0", "");
		String password = preferences.getString("pwd0", "");
		int ret = 0;//SDK.Login(strIP, 9005, user, password);
		if (0 == ret) {
//			SDK.LOAD_FLAG = 1;
		}
	}
	public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

	int []noty_t = new int[5]; //返回功能状态码
	int []noty_p1 = new int[5];  //返回设备状态
	int []noty_p2 = new int[5];
	byte []noty_ids = new byte[5 * 64]; //返回的设备ID 
	byte []noty_msg = new byte[5 * 256]; //返回消息 存放实时报警信息
	public static List<String> onlineMap = new ArrayList<String>();
	int smsCount = 0;
	//没有从XViewBasic继承 所以要写这个方法
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) { //子类对象通过该方法接收信息
			super.handleMessage(msg);
			switch (msg.what) {
			case XMSG.LOCATION:
				//定位
				//Log.d(TAG, "location:" + msg.getData().getString("location"));
				Intent intent = new Intent("com.views.NewMainAddDev");
				intent.putExtras(msg.getData());
				sendBroadcast(intent);
				break;
			case XMSG.LOGIN:			// 登录结果处理
				if(msg.arg1 == 0){		// 登录成功
					//Main.this.StartNotify();
				}else{ 
					Main.this.ExitApp("close");
				}
				break;
			case XMSG.UPDATEA_APP:
				executeCheck();
				break;
			case XMSG.SELECTED_SUB_FUN:		// 功能，子功能页面切换消息
			case XMSG.SELECTED_FUN:
				if(!Constants.netWakeState){
					APP.ShowToast(APP.GetString(R.string.Err_NetConnect));
					break;
				}
				if (_xViews[msg.arg1] == null) {
					break;
				}
				if (msg.obj != null) {
					_xViews[msg.arg1].SetInfoEx(msg.obj);
				}
				//_xViews[msg.arg1].SetInfoEx(msg.arg2);

				// 保存上一次的显示
				if (_curIndex != 0 && _curIndex != msg.arg1 && msg.what != XMSG.SELECTED_FUN) {
					_lastXV.push(_curIndex);
				}
				ShowXView(msg.arg1);

				// 到了主功能层了，就去了深层调用
				if (msg.what == XMSG.SELECTED_FUN) {
					_lastXV.clear();
				}
				break;
			case XMSG.GetNotify: {
				
			}
				break;
			}
		}
	}

	public void ExitApp(String flag) {
		try {
			LogUtil.d(TAG, "main---start   sdk.logout:");
			stopUpdateCheck();
			_loginThead.stop();
			SDK.Logout();
			SDK.UnInit();
			// 停止屏幕常亮功能
			if (_wakeLock != null) {
				_wakeLock.release();
				_wakeLock = null;
			}
			//清空账号下面的报警缓存(报警图片地址默认1小时失效，因此每次退出需要清空)
			NewMain.instance.cache.remove(Constants.userid + "_msgList");
			//解决设备掉线后状态不能通知过来 所以要清空，实时取数据
			NewMain.instance.cache.remove(Constants.userid + "_devices");
			NewMain.instance = null;
			BaseApplication.getInstance().exitApp(flag);
			//点击退出登录返回登录页面不需要关进程
			if(!"exit".equals(flag))
				System.exit(0);		// 退出操作
				//android.os.Process.killProcess(android.os.Process.myPid());
		} catch (Exception e) {
			Log.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}

	long _lLastBack = 0;
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
			if (_lastXV.isEmpty()) {
				long cur = System.currentTimeMillis();
				if ((cur - _lLastBack < 1500)) {
					this.ExitApp("close");
				} else {
//					if(this._curIndex == XV_RECORDPLAY){
//						_lastIndex = XV_NEW_MAIN;
//						this.ShowXView(_lastIndex);
//					}else{
						_lLastBack = cur;
						Toast.makeText(this, getString(R.string.reclick_tip), Toast.LENGTH_SHORT).show();
//					}
				}
			} else {
				this.onClick(_showMain);
			}
		}
		return true;
	}

//	boolean _bNotify = false;
//	public void StartNotify() {
//		if (!_bNotify) {
//			_handler.sendEmptyMessage(XMSG.GetNotify);
//			_bNotify = true;
//		}
//	}
//
//	public void StopNotify() {
//		if(Main.Instance._notificationManager != null)
//			Main.Instance._notificationManager.cancel(1);//清空状态栏通知
//		_bNotify = false;
//	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.add("menu");
		return super.onPrepareOptionsMenu(menu);
	}
	
	
	
}
