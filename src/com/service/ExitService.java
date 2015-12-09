package com.service;

import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;
import P2P.SDK;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.basic.APP;
import com.manniu.manniu.R;
import com.utils.LogUtil;
import com.views.BaseApplication;
import com.views.Main;
import com.views.NewSurfaceTest;
//账号已在另一个手机上登录，弹出提示框
public class ExitService extends Service {

	public static String TAG = "ExitService";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate...");
	}
	//type 1,踢出帐号  2，IMD登录失败 3.IDM 登录5次失败弹出退出APP提示
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand...");
		try{
			int type = Integer.parseInt(intent.getStringExtra("type"));
			if(type == 1){
				APP.ShowConfirmDialog2(getString(R.string.tip_title), getString(R.string.user_in_other_dev), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						LogUtil.d(TAG, "start ..踢出账号....");
						if(Main.Instance!= null && Main.Instance._loginThead != null){
							Main.Instance.stopUpdateCheck();
							Main.Instance._loginThead.stop();//停止IDM线程
						}
						SDK.Logout();
						SDK.UnInit();
						BaseApplication.getInstance().exitApp("play");
						LogUtil.d(TAG, "end ..踢出账号....");
						System.exit(0);		// 退出操作
					}
				}, getString(R.string.exit),this);
			}else if(type == 2){
				APP.showProgressDialog(ExitService.this,getString(R.string.sys_logining));
			}else if(type == 3){
				APP.ShowConfirmDialog(getString(R.string.tip_title), getString(R.string.Err_idmLogin), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							if(NewSurfaceTest.isPlay){
								NewSurfaceTest.instance.stop();
							}
							//关闭牛眼
							if(Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog){
								AnalogvideoActivity.instance.clearAnalog();
								AnalogvideoActivity.instance.finish();
							}
							Main.Instance.stopUpdateCheck();
							Main.Instance._loginThead.stop();
							SDK.Logout();
							SDK.UnInit();
							Main.Instance.finish();
							BaseApplication.getInstance().relogin();
						} catch (Exception e) {
						}
					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				},getString(R.string.relogin), getString(R.string.cancel),this);
			}
		}catch(Exception e){
			Log.d(TAG, "offline exception!!" + e.getMessage());
		}
		return super.onStartCommand(intent, flags, startId);
//		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "ExitService onDestroy...");
		super.onDestroy();
	}

}
