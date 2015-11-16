package com.backprocess;

import java.util.Map;

import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;
import org.json.JSONException;
import org.json.JSONObject;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.utils.MD5Util;
import com.views.BaseApplication;
import com.views.Main;
import com.views.NewLogin;
import com.views.NewSurfaceTest;
import com.views.bovine.Fun_AnalogVideo;

/**
 * @author: li_jianhua Date: 2015-8-26 下午2:13:57
 * To change this template use File | Settings | File Templates.
 * Description：
 */
public class BackLoginThread implements Runnable{
	public final static int QUERY_OK = 200;
	private String TAG = BackLoginThread.class.getSimpleName();
	public static boolean runFlag;
	//200成功  0:ETS/IDM 1:登出操作  2:用户登录 3:牛眼
	public static int state = 0;
	Thread _thread = null;
	public int error_Count= 0;
	private MyHandler _handler = new MyHandler();
    private final int LOGIN_SEND_MSG = 100;
	
	
	public BackLoginThread() {
		this._thread = new Thread(this);
	}
	
	public void start() {
		try {
			synchronized (_thread) {
        		if(_thread.isAlive()){
        			// 唤醒登录线程
        			_thread.notify();
        		}else{
            		// 启动登录线程
        			_thread.start();
        		}
			}
			runFlag = true;
		} catch (Exception e) {
			//System.out.println("打开失败!");
		}
	}
	
	public void stop() {
		try {
			APP.dismissProgressDialog();
			runFlag = false;
			stopService();
			//_thread.interrupt();
			_thread = null;
			state = 0;
		} catch (Exception e) {
			return;
		}
	}
	Intent _intent = null;
	public void waitIDMLogin(){
		if(_thread != null){
			_intent = new Intent();
			_intent.setAction("com.service.EXIT_SERVICE");
			_intent.putExtra("type", "2");
			_intent.setPackage("com.manniu.manniu");
			BaseApplication.getInstance().startService(_intent);
		}
	}
	
	private void stopService(){
		if(_intent != null)
			BaseApplication.getInstance().stopService(_intent);
	}
	
	public void loginIDMError(){
		Intent intent = new Intent();
		intent.setAction("com.service.EXIT_SERVICE");
		intent.putExtra("type", "3");
		intent.setPackage("com.manniu.manniu");
		BaseApplication.getInstance().startService(intent);
	}
	
	@Override
	public void run() {
		while(true){
			try {
				if(_thread == null) return;
				synchronized (_thread) {
					if(runFlag){
						switch (state) {
						case 0:
							//LogUtil.d("BackLoginThread",111);
							//long t1 = System.currentTimeMillis();
							int nRet = SDK.Login("cms.9wingo.com", 9511, 3, Constants.userid, Constants.userName, "pwd");
							//long t2 = System.currentTimeMillis();
							//LogUtil.d("BackLoginThread", Constants.userid+"-----IDM登录中--------"+Constants.userName +" nRet= "+nRet + " time="+(t2-t1));
							if(nRet == 0){
								APP.dismissProgressDialog();
								state = 200;
								//SDK._isRun = true;
								//如果正在播放视频，要起一个线程不断的连接
								if(NewSurfaceTest.isPlay){
									NewSurfaceTest.instance.startTimer(1);
									NewSurfaceTest._runFlag = true;
								}
								runFlag = false;//连接成功置成false 
								_thread.wait();//
							}else{
								Thread.sleep(10000);
								error_Count ++;
								if(error_Count > 2){
									error_Count = 0;
									runFlag = false;
									_handler.sendEmptyMessage(LOGIN_SEND_MSG);
								}
							}
							break;
						case 3:
							LogUtil.d("BackLoginThread","start  牛眼登录 IDM ....");
							long t1 = System.currentTimeMillis();
							int status = SDK.Login("cms.9wingo.com", 9511, 4, Fun_AnalogVideo.instance._devSid, Constants.userName, "pwd");//模拟TYPE 传4
							long t2 = System.currentTimeMillis();
							LogUtil.d("BackLoginThread", Constants.userid+"-----IDM登录中--------"+Constants.userName +" nRet= "+status + " time="+(t2-t1));
							if(status == 0){
								APP.dismissProgressDialog();
								state = 200;
								//SDK._isRun = true;
								runFlag = false;//连接成功置成false 
								_thread.wait();//
							}else{
								Thread.sleep(10000);
								error_Count ++;
								if(error_Count > 2){
									error_Count = 0;
									runFlag = false;
									_handler.sendEmptyMessage(LOGIN_SEND_MSG);
								}
							}
							break;
						case 1:
							error_Count = 0;
							waitIDMLogin();
							//IPC
							if(NewSurfaceTest.isPlay){
								SDK.P2PClose(SDK._sessionId);
							}
							//牛眼
							if(AnalogvideoActivity.instance != null && SDK._createChnlFlag == 0){//如果正在发送数据 --停止发送
								//SDK.P2PClose(SDK._sessionId);//暂时不支持主动关 要新加接口
								AnalogvideoActivity.instance.sendCheageData();
								SDK._createChnlFlag = -1;
							}
							SDK._sessionId = 0;
							SDK.Logout();
							if(Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog){
								//waitIDMLogin();
								state = 3;
							}else{
								state = 0;
							}
							break;
						case 2:
							if(isUserLogin){
								isUserLogin = false;
								waitIDMLogin();
							}
							String tempStr = "?username="+Constants.userName+"&password="+MD5Util.MD5(MD5Util.MD5(APP.GetSharedPreferences(NewLogin.SAVEFILE, "pwd0", "")))+"&registrationId="+1+"&deviceInfo="+2+"&time_token="+3;
							loginForETS(tempStr);
							break;
						}
					}
				}
			} catch (Exception e) {
				error_Count = 0;
				APP.dismissProgressDialog();
				runFlag = false;
				LogUtil.d("BackLoginThread", ExceptionsOperator.getExceptionInfo(e));
				return;
			}
		}
	}
	
	private boolean isUserLogin = true;//用户登录标识用于只显示一次弹出框提示
	private void loginForETS(String params){
		try {
			JSONObject json = null;
			Map<String, Object> map = HttpURLConnectionTools.get(Constants.ETShostUrl+"/logon_v1"+params);
			if (Integer.parseInt(map.get("code").toString()) != 200) {
				_handler.sendEmptyMessage(XMSG.LOGIN_NETWORK_ERROR);
			}else{
				json = new JSONObject(map.get("data").toString());
				String msg = "";
				try {
					msg = json.getString("msg");
					if("success".equals(msg)){
						Constants.session_Id = json.getString("session_id");
						state = 0;
						isUserLogin = true;
					}
				} catch (JSONException e) {
				}
			}
		} catch (Exception e) {
			_handler.sendEmptyMessage(XMSG.LOGIN_NETWORK_ERROR);
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	
	
	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case LOGIN_SEND_MSG:
				APP.dismissProgressDialog();
				loginIDMError();
				_thread = null;
				break;
			case XMSG.LOGIN_NETWORK_ERROR:
				APP.ShowToast(APP.GetMainActivity().getText(R.string.Err_CONNET).toString());
				break;
			}
		}
	}

}
