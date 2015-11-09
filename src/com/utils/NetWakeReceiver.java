package com.utils;

import com.basic.APP;
import com.manniu.manniu.R;
import com.views.Main;
import com.views.NewSurfaceTest;

import P2P.SDK;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-9-19 上午11:57:50 To
 * change this template use File | Settings | File Templates. Description：
 */
public class NetWakeReceiver extends BroadcastReceiver {
	static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (intent.getAction().equals(ACTION) && intent != null) {
				Constants.netWakeState = isNetworkAvailable(context);
				if (Constants.netWakeState == false){
					
					//如果正在播放视频
//					if(NewSurfaceTest.instance != null && NewSurfaceTest.instance.isPlay){
//						NewSurfaceTest.instance.openWait();
//					}
					
				}else{
//					if(NewSurfaceTest.instance != null && NewSurfaceTest.instance.isPlay){
//						NewSurfaceTest.instance.closeWait();
//					}
				}
			}
		} catch (Exception e) {
			LogUtil.e("NetWakeReceiver", ExceptionsOperator.getExceptionInfo(e));
			//Toast.makeText(context, APP.GetString(R.string.Err_NetConnect), Toast.LENGTH_LONG).show();
			return;
		}
	}
	
	/**  
     * 检测网络连接是否可用  
     * @param ctx  
     * @return true 可用; false 不可用  
     */  
	private boolean isNetworkAvailable(Context context) {
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm == null) {
				return false;
			}
			NetworkInfo[] netinfo = cm.getAllNetworkInfo();
			if (netinfo == null) {
				return false;
			}
			for (int i = 0; i < netinfo.length; i++) {
				if (netinfo[i].isConnected()) {
					Constants.netWakeType = netinfo[i].getType();
					return true;
				}
			}
		}
		return false;
	}

}
