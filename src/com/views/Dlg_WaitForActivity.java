package com.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.manniu.manniu.R;
//注册
public class Dlg_WaitForActivity extends Dialog{
	MyHandler _handler = new MyHandler();
	TextView _textView;

	public Dlg_WaitForActivity(Context context, int theme) {
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
		setCanceledOnTouchOutside(false);//点击对话框以外的区域对话框不消失么
	}

	// 屏蔽返回按键
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.dismiss();
			//返回停止线程
			if(NewSurfaceTest.instance != null && Main.Instance._curIndex == Main.XV_NEW_MAIN){
				NewSurfaceTest.instance.stopTimer();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void Show(OnTaskListener user, String text) {
		Show(user, text, 0, 0, 0, null);
	}

	//显示
	public void Show(OnTaskListener user, String text, int what, int arg1, int arg2, Object obj) {
		_text = text;
	}

	String _text;
	// 更新信息揭示
	public void UpdateText(String text) {
		_text = text;
		_handler.sendEmptyMessage(50);
	}
	
	public void UpdateTextReal(String text) {
		_text = text;
		_handler.sendEmptyMessage(1);
	}

	
	// 消息处理
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			_textView.setText(_text);
		}
	}

	/*private boolean runFlag;
	Thread _sthread = null;
	public void start() {
		try {
			runFlag = true;
			if(_sthread == null){
				_sthread = new Thread(this);
			}
			_sthread.start();
			LogUtil.d("DecoderQueue", "....DecoderQueue.start()....");
		} catch (Exception e) {
			System.out.println("打开失败!");
		}
	}
	public void stop() {
		try {
			runFlag = false;
			_sthread = null;
		} catch (Exception e) {
		}
	}
	@Override
	public void run() {
		if(runFlag){
		}
		
	}*/
}