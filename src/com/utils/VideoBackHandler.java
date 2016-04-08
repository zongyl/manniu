package com.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * 录像回放
 * @author pc
 *
 */
public class VideoBackHandler extends Handler{

	public static String TAG = "VideoBackHandler";
	
	private Bundle data;
	
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case 1:
			data = msg.getData();
			LogUtil.d(TAG, data.getString("file_list"));
			break;
		default:
			break;
		}
		super.handleMessage(msg);
	}
}
