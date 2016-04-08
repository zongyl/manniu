package com.mining.app.zxing.camera;

import com.utils.LogUtil;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

/**
 * 描述: 相机自动对焦
 */
final class AutoFocusCallback implements Camera.AutoFocusCallback {

	private static final String TAG = AutoFocusCallback.class.getSimpleName();

	private static final long AUTOFOCUS_INTERVAL_MS = 1500L;

	private Handler autoFocusHandler;
	private int autoFocusMessage;

	void setHandler(Handler autoFocusHandler, int autoFocusMessage) {
		this.autoFocusHandler = autoFocusHandler;
		this.autoFocusMessage = autoFocusMessage;
	}

	public void onAutoFocus(boolean success, Camera camera) {
		if (autoFocusHandler != null) {
			Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
			autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
			autoFocusHandler = null;
		} else {
			LogUtil.d(TAG, "Got auto-focus callback, but no handler for it");
		}
	}

}
