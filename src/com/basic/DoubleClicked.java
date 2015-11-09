package com.basic;

import android.view.View;

////双击事件的简单实现
public class DoubleClicked {
	static View _view = null;
	static long _lastTime = 0;

	public static void Reset() {
		_view = null;
		_lastTime = 0;
	}

	public static synchronized int OnClicked(View v) {
		long lCurTime = System.currentTimeMillis();
		int nCliecked = 1;
		if (v.equals(_view) && lCurTime - _lastTime < 1200) {
			nCliecked = 2;
			lCurTime = 0;
		}
		_view = v;
		_lastTime = lCurTime;
		return nCliecked;
	}
}