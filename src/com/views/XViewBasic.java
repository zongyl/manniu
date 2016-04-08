package com.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.basic.APP;

public class XViewBasic  implements OnClickListener {
	protected Activity ACT;
	public static Activity ACTs;
	protected ViewGroup _root = null;
	protected MyHandler _handler = null;
	protected String _title = "";
	
	public XViewBasic(Activity activity, int viewId, String title) {
		ACTs =activity;
		ACT = activity;
		_title = title;
		_root = (ViewGroup) ACT.findViewById(viewId);
		_handler = new MyHandler();
		APP.RegHandler(viewId, _handler);
	}

	public String GetTitle() {
		return _title;
	}

	public Handler GetHandler() {
		return _handler;
	}

	//
	public void SetInfoEx(Object obj) {
	}
	
	public void SetInfoEx(int param) {
	}

	// 本界面中的View获取
	public View findViewById(int id) {
		return _root.findViewById(id);
	}

	protected void ListenViews(int[] ids) {
		APP.ListenViews(_root, ids, this);
	}

	int _visibility;
	protected void OnVisibility(int visibility) {
		_visibility = visibility;
	}

	protected void setVisibility(int visibility) {
		_root.setVisibility(visibility);
	}

	protected void onClick(int id) {
	}

	@Override
	public void onClick(View arg0) {
		int nID = arg0.getId();
		onClick(nID);
	}

	protected void OnMessage(Message msg) {

	}

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			//System.out.println("XViewBasic: 线程...");
			super.handleMessage(msg);
			OnMessage(msg);
		}
	}
}
