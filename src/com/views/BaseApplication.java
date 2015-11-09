package com.views;

import java.util.Stack;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.basic.APP;
import com.jpush.MyReceiver;
import com.nmbb.vlc.ui.VlcVideoActivity;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.utils.CrashHandler;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.Loger;
import com.utils.NetWakeReceiver;
import com.views.XViewBasic.MyHandler;
import com.views.analog.camera.encode.Fun_RealPlay.RealHandler;

/**
 * 自己实现Application，实现数据共享
 */
public class BaseApplication extends Application {
    private static final String TAG = "BaseApplication";
    private MyHandler myhandler = null;
    
    private RealHandler redlandler = null;
    
    private static Stack<Activity> activityStack;
	private static BaseApplication singleton;
	//检查网络广播接收器
	NetWakeReceiver netReceiver;
	
    public MyHandler getMyhandler() {
		return myhandler;
	}

	public void setMyhandler(MyHandler myhandler) {
		this.myhandler = myhandler;
	}

	@Override
	public void onCreate() {

		Log.d(TAG, "[ExampleApplication] onCreate");
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());

		//JPushInterface.setDebugMode(true); // 设置开启日志,发布时请关闭日志
		JPushInterface.init(this); // 初始化 JPushe

		singleton = this;
		initImageLoader(getApplicationContext());// 初始化图像库配置
		netReceiver = new NetWakeReceiver();
		// 注册广播接收器
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		intentFilter.setPriority(1000);
		registerReceiver(netReceiver, intentFilter);
	}
	
	public void initImageLoader(Context context){
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.threadPoolSize(5);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs(); // Remove for release app
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config.build());
	}
	

	public static BaseApplication getInstance(){
		return singleton;
	}
	
	public void addActivity(Activity activity){
		if(activityStack == null){
			activityStack = new Stack<Activity>();
		}
		activityStack.add(activity);
	}
	
	public RealHandler getRedlandler() {
		return redlandler;
	}
	public void setRedlandler(RealHandler redlandler) {
		this.redlandler = redlandler;
	}

	public void finishActivity(Activity activity){
		if(activity != null){
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}
	
	public void finishActivity(){
		Activity activity = activityStack.lastElement();
		finishActivity(activity);
	}
	
	public void finishAllActivity(){
		for(Activity activity : activityStack){
			if(activity != null){
				Log.v("finishAllActivity:", activity.getLocalClassName());
				activity.finish();
			}
		}
		activityStack.clear();
	}
	
	/**
	 * 服务器返回noLogin的时候调用
	 */
	public void relogin(){
		finishAllActivity();
		Loger.closePrint();
		Intent intent = new Intent(this, SplashScreen.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	/**
	 * 退出
	 * 销毁服务器端 session
	 * 清空sharedpreferences
	 * 清空activity
	 */
	@SuppressWarnings("static-access")
	public void exitApp(String flag){
		try {
			MyReceiver.isCloseApp = false;
			if("exit".equals(flag)){
				clearSharedPre(new String[]{NewLogin.SAVEFILE,VlcVideoActivity.COLLECT_PRE,VlcVideoActivity.PRAISE_PRE});
			}else if("play".equals(flag)){
				SharedPreferences preferences = getSharedPreferences(NewLogin.SAVEFILE, this.MODE_PRIVATE);
				preferences.edit().putString("pwd0", "").commit();
				clearSharedPre(new String[]{VlcVideoActivity.COLLECT_PRE,VlcVideoActivity.PRAISE_PRE});
			}
			//HttpURLConnectionTools.get(Constants.hostUrl + "/android/doLogout?userId="+APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "")+"&sessionId="+Constants.sessionId);
			finishAllActivity();
			Loger.closePrint();
		} catch (Exception e) {
			if(e != null) LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}finally{
			try {
				 //取消广播接收器
		        if (netReceiver != null) {
		        	unregisterReceiver(netReceiver);
		    	}
			} catch (Exception e2) {
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void clearSharedPre(String[] arr){
		for(String str:arr){
			SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(str,APP.GetMainActivity().MODE_PRIVATE);
			if(preferences!=null){
				preferences.edit().clear().commit();
			}
		}
	}

}