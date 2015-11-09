package com.utils;

import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;

import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.views.Main;
import P2P.SDK;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * @author: li_jianhua Date: 2015-10-21 下午3:45:12
 * To change this template use File | Settings | File Templates.
 * Description：
 */
public class PhoneStatReceiver extends BroadcastReceiver{
	
//	private static final String TAG = "PhoneStatReceiver";  
	private static boolean incomingFlag = false;  
	//private static String incoming_number = null;  
	private static boolean isOffhook = false;

	@Override  
	public void onReceive(Context context, Intent intent) {
		// 如果是拨打电话
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			incomingFlag = false;
//			String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//			Log.i(TAG, "call OUT:" + phoneNumber);
		} else {
			// 如果是来电
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING://响铃:来电号码
				incomingFlag = true;// 标识当前是来电
				//incoming_number = intent.getStringExtra("incoming_number");
				//Log.i(TAG, "RINGING :" + incoming_number);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK://接听电话中
				if (incomingFlag) {
					//Log.i(TAG, "incoming ACCEPT :" + incoming_number);
					//关闭牛眼
					if(Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog){
						AnalogvideoActivity.instance.clearAnalog();
						SDK.Logout();
						AnalogvideoActivity.instance.finish();
						isOffhook = true;
					}
				}
				break;
			case TelephonyManager.CALL_STATE_IDLE://挂断电话
				if (incomingFlag) {
					if(Main.Instance._curIndex == Main.XV_NEW_MSG && isOffhook){
						APP.SendMsg(R.id.lay_setting, XMSG.ANALOG, "");
						isOffhook = false;
					}
				}
				break;
			}
		}
	}

}
