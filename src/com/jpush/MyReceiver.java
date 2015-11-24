package com.jpush;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.adapter.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.utils.Constants;
import com.utils.LogUtil;
import com.utils.SIMCardInfo;
import com.views.Main;

/**
 * 自定义接收器
 * 
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
	private static final String TAG = "JPush";
	public static boolean isCloseApp = true;//是否发送踢用户的弹出框
	@Override
	public void onReceive(Context context, Intent intent) {
		if(!isCloseApp) return;
        Bundle bundle = intent.getExtras();
		Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...
            SharedPreferences preferences = context.getSharedPreferences("jpush_registration", context.MODE_PRIVATE);
            preferences.edit().putString("registrationId", regId).commit();            
            sendRegId(context, regId);
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
        	Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
        	LogUtil.d(TAG, bundle.getString(JPushInterface.EXTRA_MESSAGE)+"-收到踢出账号消息-"+isCloseApp);
        	if(isCloseApp) processCustomMessage(context, bundle);
        
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
        	try {
        		Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
                Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);
            	//APP.ShowToast("通知,通知");
			} catch (Exception e) {
			}
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
        	/*//打开自定义的Activity
        	Intent i = new Intent(context, TestActivity.class);
        	i.putExtras(bundle);
        	//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        	context.startActivity(i);*/
           // String str = printBundle(bundle);
            //APP.ShowToast("JPUSH:"+str);
            try{
            	APP.GetMainActivity().ShowXView(Main.XV_NEW_MAIN);
            	APP.GetMainActivity().Instance.NewMainItem(1);
            }catch(Exception e){
            	Log.v(TAG, "APP IS NULL!");
            	Intent i = new Intent(context, com.views.SplashScreen.class);
            	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            	context.startActivity(i);
            }
        	
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        	
        } else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
        	boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
        	Log.w(TAG, "[MyReceiver]" + intent.getAction() +" connected state change to "+connected);
        } else {
        	Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} 
			else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
	
	//send msg to MainActivity
	private void processCustomMessage(Context context, Bundle bundle) {
		for(String key : bundle.keySet()){
			if(key.equals(JPushInterface.EXTRA_MESSAGE)){
				if("offline".equals(bundle.get(key))){

					String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
					
					com.alibaba.fastjson.JSONObject obj = JSON.parseObject(extras);
					
					String time = obj.getString("time");
					
					offline(context, obj.getString("device"), time.substring(time.length()-8));
				}
			}
		}
		/*if (MainActivity.isForeground) {
			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
			msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					JSONObject extraJson = new JSONObject(extras);
					if (null != extraJson && extraJson.length() > 0) {
						msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
					}
				} catch (JSONException e) {

				}
			}
			context.sendBroadcast(msgIntent);
		}*/
	}
	
	private void offline(Context context, String device, String time){
		Intent intent = new Intent();
		intent.setAction("com.service.EXIT_SERVICE");
		intent.putExtra("device", device);
		intent.putExtra("time", time);
		intent.setPackage("com.manniu.manniu");
		context.startService(intent);
	}
	
	private void sendRegId(Context context, String regId){
		RequestParams params = new RequestParams();
        SIMCardInfo info = new SIMCardInfo(context);
        params.put("registrationId", regId);
        
        if(info.getDeviceId() == null)
        {
        	params.put("imei", info.getDeviceUuid().toString());
        }
        else
        {
        	 params.put("imei", info.getDeviceId());
        }
       
        params.put("system", "android");
        params.put("deviceInfo", info.getBuildFields());
        
        HttpUtil.get(Constants.hostUrl+"/jpush/register", params, new JsonHttpResponseHandler(){
        	@Override
        	public void onSuccess(int statusCode, Header[] headers,
        			JSONObject response) {
        		super.onSuccess(statusCode, headers, response);
        	}
        	
        	@Override
        	public void onFailure(int statusCode, Header[] headers,
        			String responseString, Throwable throwable) {
        		super.onFailure(statusCode, headers, responseString, throwable);
        	}
        	
        	@Override
        	public void onFailure(int statusCode, Header[] headers,
        			Throwable throwable, JSONObject errorResponse) {
        		super.onFailure(statusCode, headers, throwable, errorResponse);
        	}
        	
        });
	}
}