package com.views;

import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import com.manniu.manniu.R;
import com.utils.Constants;

public class Dlg_Ptz extends Dialog implements OnTouchListener {
	
	java.util.Timer _timer = null;	// 定时器，
	
	public Dlg_Ptz(Context context, int theme) {
		super(context, theme);

		// 标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dlg_ptz);
		Window window = getWindow();
		WindowManager.LayoutParams wl = window.getAttributes();

		// wl.x = 0; // 这两句设置了对话框的位置．
		// wl.y = 0;
		// wl.width = 150;
		// wl.height = 400;
		wl.alpha = 0.6f; // 这句设置了对话框的透明度
		wl.dimAmount = 0f; // 控制灰度的值，当为1时，界面除了我们的dialog内容是高亮显示的
		wl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
		wl.flags = wl.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
		wl.flags = wl.flags | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wl);
		this.setCanceledOnTouchOutside(true);//点击其他区域dialog消失
		// window.setBackgroundDrawableResource(R.drawable.back);
		
		int ids[]={R.id.ptzz0, R.id.ptzz1, R.id.ptzi0, R.id.ptzi1, R.id.ptzf0, R.id.ptzf1, 
				R.id.ptzl, R.id.ptzr, R.id.ptzu, R.id.ptzd};
		for(int i = 0; i < ids.length; ++i){
			this.findViewById(ids[i]).setOnTouchListener(this);
		}
		
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//0- 按下  1-抬起
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundColor(Constants.COLOR_SELECTED);
			//按下后开个定时器,隔个60毫秒发送个云台命令,弹起把定时器关了.
			startTimer(v,event,0);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			v.setBackgroundColor(0);
			stopTimer();
			eventHandle(v,event,1);
			break;
		}
		return true;
	}
	
	//定时器  60毫秒发一次
	public void startTimer(View view, MotionEvent event,int nStop) {
		synchronized (this) {
			if (_timer != null) {
				_timer.cancel();
				_timer = null;
			}
			_timer = new java.util.Timer();
			_timer.schedule(new PTZProcessTask(view,event,nStop), 40, 60);
		}
	}

	public void stopTimer() {
		synchronized (this) {
			if (_timer != null) {
				_timer.cancel();
				_timer = null;
			}
		}
	}
	
	//实现定时器类
	class PTZProcessTask extends TimerTask{
		private View _view;
		private MotionEvent _event;
		private int _nStop;
		
		public PTZProcessTask(View view,MotionEvent event,int nStop){
			this._view = view;
			this._event = event;
			this._nStop = nStop;
		}
		@Override
		public void run() {
			eventHandle(_view,_event,_nStop);
		}

	}
	
	public void eventHandle(View v, MotionEvent m,int nStop){
		//System.out.println("PTZ::"+v.getId()+"--");
		int []cmds = new int[4];
		switch(v.getId()){
		case R.id.ptzz0:
			cmds[0] = 2;
			cmds[1] = 1; //1放大
			nStop = 1;
			break;
		case R.id.ptzz1:
			cmds[0] = 2;
			cmds[1] = 1; //1放大
			nStop = 2;
			break;
		case R.id.ptzi0:
			cmds[0] = 2;
			cmds[1] = 2; //2聚焦
			nStop = 1;
			break;
		case R.id.ptzi1:
			cmds[0] = 2;
			cmds[1] = 2; //2聚焦
			nStop = 2;
			break;
		case R.id.ptzf0:
			cmds[0] = 2;
			cmds[1] = 3; //3光圈 
			nStop = 1;
			break;
		case R.id.ptzf1:
			cmds[0] = 2;
			cmds[1] = 3;//3光圈 
			nStop = 2;
			break;
		case R.id.ptzl:
			cmds[0] = 0; // 0方向
			cmds[1] = 3; //左
			break;
		case R.id.ptzr:
			cmds[0] = 0;
			cmds[1] = 4; //右
			break;
		case R.id.ptzu:
			cmds[0] = 0;
			cmds[1] = 1; //上
			break;
		case R.id.ptzd:
			cmds[0] = 0;
			cmds[1] = 2; //下
			break;
		default:
			break;
			//return true;
		}
		cmds[2] = Fun_Setting.PtzStep; //步长
		cmds[3] = nStop;
		////APP.SendMsg(R.id.fun_realplay, XMSG.ON_CLICKED_PTZ, cmds);
		//return true;
	}

}