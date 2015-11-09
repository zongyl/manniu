package com.mining.app.zxing.decoding;

import java.util.concurrent.CountDownLatch;

import com.mining.app.zxing.decoding.QRcode_DecodeActivity;

import android.os.Handler;
import android.os.Looper;

/**
 * 描述: 解码线程
 */
final class DecodeThread extends Thread {

	QRcode_DecodeActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	DecodeThread(QRcode_DecodeActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}