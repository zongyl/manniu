package com.manniu.manniu;

import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gcm.GCMBaseIntentService;
import com.utils.LogUtil;
import com.views.BaseApplication;

public class GCMIntentService extends GCMBaseIntentService{

	public static final String TAG = "GCMIntentService";

	private NotificationManager m;
	private Notification n;
	
	@Override
	public void onCreate() {
		super.onCreate();
		m = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	}
	
	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		//GcmListenerService service;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onMessage(Context context, Intent intent) {
		LogUtil.d(TAG, "onMessage");
		Bundle data = intent.getExtras();
		Set<String> keys = data.keySet();
		for(String key : keys){
			LogUtil.d(TAG, "key:"+key +" value:"+ data.getString(key));
		}
		//key:gcm.notification.title value:google push title
		//key:gcm.notification.body value:google push title
		n = new Notification.Builder(this)
		.setTicker("new message!")
		.setContentTitle(data.getString("gcm.notification.title"))
        .setContentText(data.getString("gcm.notification.body"))
        .setSmallIcon(R.drawable.del_button_sel)
		.build();
		m.notify(1, n);
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Intent intent = new Intent();
		intent.putExtra("", regId);
		context.sendBroadcast(intent);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		
	}
	
	@Override
	protected String[] getSenderIds(Context context) {
		return new String[]{BaseApplication.getInstance().SENDER_ID};
	}
	
	private void showNotifiy(String msg){
		
	}
}
