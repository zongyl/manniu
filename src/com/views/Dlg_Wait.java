package com.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.manniu.manniu.R;
import com.views.bovine.Fun_AnalogVideo;

@SuppressWarnings("unchecked")
public class Dlg_Wait extends Dialog {
	OnTaskListener _user;
	MyTask _task; 
	MyHandler _handler = new MyHandler();
	TextView _textView;

	public Dlg_Wait(Context context, int theme) {
		super(context, theme);
		// 设置为无标题模式  requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dlg_wait);
		_textView = (TextView) this.findViewById(R.id.textView1);
		Window window = getWindow();
		WindowManager.LayoutParams wl = window.getAttributes();
		wl.alpha = 0.7f; // 透明度     wl.dimAmount = 0f; 	// 背景可见度,1.0f时候,背景全部变黑暗
		// wl.x = 0;
		// wl.width = 150;
		// wl.height = 400;
		// wl.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
		// wl.flags = wl.flags |
		// WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		// wl.flags = wl.flags | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wl);
		setCanceledOnTouchOutside(false);
	}

	// 屏蔽返回按键
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.dismiss();
			if(Main.Instance != null && Main.Instance._curIndex == Main.XV_NEW_MSG){
				if(Fun_AnalogVideo.instance != null) Fun_AnalogVideo.instance.stopTimer();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void Show(OnTaskListener user, String text) {
		Show(user, text, 0, 0, 0, null);
	}

	//显示
	public void Show(OnTaskListener user, String text, int what, int arg1, int arg2, Object obj) {
		_user = user;
		_text = text;
		_task = new MyTask();
		_task.execute(what, arg1, arg2, obj);
	}

	String _text;
	// 更新信息揭示
	public void UpdateText(String text) {
		_text = text;
		_handler.sendEmptyMessage(100);
	}
	
	public void UpdateTextNoDelay(String text){
		_text = text;
		_handler.sendEmptyMessage(0);
	}
	// 异步任务操作
	@SuppressWarnings("rawtypes")
	protected class MyTask extends AsyncTask {
		int what = 0, arg1 = 0, arg2 = 0;
		Object obj = null;

		@Override
		protected void onPreExecute() {
			_textView.setText(_text);
			Dlg_Wait.this.show();
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			what = (Integer) arg0[0];
			arg1 = (Integer) arg0[1];
			arg2 = (Integer) arg0[2];
			obj = arg0[3];
			return _user.OnDoInBackground(what, arg1, arg2, obj);
		}

		@Override
		protected void onPostExecute(Object ret) {
			_user.OnPostExecute(what, arg1, arg2, obj, ret);
			Dlg_Wait.this.dismiss();
		}
	}
	
	// 消息处理
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			_textView.setText(_text);
		}
	}
}