package com.utils;

import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSONObject;
import com.basic.APP;
import com.manniu.manniu.R;
import com.views.NewDeviceSet;

/**
 * 设备设置 handler
 * @author pc
 *
 */
public class DevSetHandler extends Handler{

	public static final String TAG = DevSetHandler.class.getSimpleName();
	
	private Bundle data;
	
	private String spFileName = "";
	
	private Set<String> keys;
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		LogUtil.d(TAG, "设备设置 handler");
		switch (msg.what) {
		case 1://从服务器获取到设置信息
			data = msg.getData();
			spFileName = MD5Util.MD5(data.getString("deviceId")) + NewDeviceSet.FILE;
			LogUtil.d(TAG, "文件名为:" + spFileName);
			JSONObject jsonObj = JSONObject.parseObject(data.getString("setting"));
			keys = jsonObj.keySet();
			for(String key: keys){
				SetSharePrefer.write(spFileName, key, jsonObj.getString(key));
			}
			LogUtil.d(TAG, "write end!");
			
			Intent intent = new Intent("com.views.NewDeviceSet");
			APP.GetMainActivity().sendBroadcast(intent);
			break;
		case 2://
			APP.ShowToast(APP.GetString(R.string.set_success));
			break;
		case 3://设备不在线
			LogUtil.d(TAG, "设备不在线!");
			APP.ShowToast(APP.GetString(R.string.GW_ERRCODE_P2P_DEST_DISCONNECT));
			break;
		default:
			break;
		}
		
	}
}
