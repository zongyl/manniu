package com.views.analog.camera.encode;


import com.basic.APP;
import com.basic.XMSG;
import com.utils.Constants;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.view.View.*;

interface OnClickWnd {
	void OnClickWnd(VideoSurfaceView video, boolean bDouble,int index);
}

@SuppressWarnings("deprecation")
public class VideoWndCtrl extends AbsoluteLayout implements OnTouchListener {
	final static int COLOR_BACKGROUD = 0xFF000000; //黑色
	final static int COLOR_SELECTED = 0xFF000000;// 0xFF00A2FF 选中边框颜色
	final static int COLOR_UNSELECTED = 0x00000000; //0x00000000    其它默认边框颜色
	VideoSurfaceView _vs = null;
	View _line[] = new View[12];
	final static int IS = 4;
	int _w = 0, _h = 0;
	int _nMaxIndex = -1, _nSelected = 0;
	OnClickWnd _wndListener = null;

	public VideoWndCtrl(Context context) {
		super(context);
//		for (int i = 0; i < 12; ++i) {
//			_line[i] = new View(context);
//			_line[i].setBackgroundColor(COLOR_UNSELECTED);
//			this.addView(_line[i]);
//		}
		
		_vs = new VideoSurfaceView(context);
		this.addView(_vs);
		_vs.setOnTouchListener(this);
		
		// this.setBackgroundDrawable(getResources().getDrawable(R.drawable.bk_play_wnd));
		this.setBackgroundColor(Color.rgb(0x46, 0x60, 0x82));
		//SetSideColor(0, COLOR_SELECTED);
		
		//一个视频窗口要改的地方1-1 index 从0开始的
		if(Constants.viewNum == 1)
			SetMax(Constants.viewNum-1);
	}

	public void SetOnWndListener(OnClickWnd listener) {
		_wndListener = listener;
	}

	public int GetMaxIndex() {
		return _nMaxIndex;
	}

	public int GetSelectedIndex() {
		return _nMaxIndex;
	}

	public VideoSurfaceView GetSelected() {
		return _vs;
	}
	
	public VideoSurfaceView GetPlayer(int index) {
		return _vs;
	}

	//关联VSPlayer -->DrawBK() 画背景   OnVisibility
	protected void OnVisibility(int visibility) {
		if (_nMaxIndex == -1) {
			for (int i = 0; i < 4; ++i) {
				_vs.OnVisibility(visibility);
				_vs.setVisibility(visibility);
			}
		} else {
			_vs.OnVisibility(visibility);
			_vs.setVisibility(visibility);
		}
	}

	/*void MoveVS(int index, int nFullScreen) {
		int itemW = (_w - 3 * IS) / 2;
		int itemH = (_h - 3 * IS) / 2;
		LayoutParams lp = null;
		if (nFullScreen == 0) {
			switch (index) {
			case 0:
				lp = new LayoutParams(itemW, itemH, IS, IS);
				_vs[0].setLayoutParams(lp);
				break;
			case 1:
				lp = new LayoutParams(_w - IS * 3 - itemW, itemH, IS * 2 + itemW, IS);
				_vs[1].setLayoutParams(lp);
				break;
			case 2:
				lp = new LayoutParams(itemW, _h - IS * 3 - itemH, IS, IS * 2 + itemH);
				_vs[2].setLayoutParams(lp);
				break;
			case 3:
				lp = new LayoutParams(_w - IS * 3 - itemW, _h - IS * 3 - itemH, IS * 2 + itemW, IS * 2 + itemH);
				_vs[3].setLayoutParams(lp);
				break;
			}
		} else if (nFullScreen == 1) {
			lp = new LayoutParams(_w - IS * 2, _h - IS * 2, IS, IS);
			_vs[index].setLayoutParams(lp);
		}
	}*/

	/*@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int w = this.getWidth();
		int h = this.getHeight();
		if (_w != w || _h != h) {
			_w = w;
			_h = h;

			if (_nMaxIndex != -1) {
				MoveVS(_nMaxIndex, 1);
			} else {
				for (int i = 0; i < 4; ++i) {
					MoveVS(i, 0);
				}
			}

			int itemW = (w - 3 * IS) / 2;
			int itemH = (h - 3 * IS) / 2;

			LayoutParams lp = null;
			lp = new LayoutParams(itemW, itemH, IS, IS);
			_vs[0].setLayoutParams(lp);
			_vs[0].setVisibility(VISIBLE);

			lp = new LayoutParams(w - IS * 3 - itemW, itemH, IS * 2 + itemW, IS);
			_vs[1].setLayoutParams(lp);

			lp = new LayoutParams(itemW, h - IS * 3 - itemH, IS, IS * 2 + itemH);
			_vs[2].setLayoutParams(lp);

			lp = new LayoutParams(w - IS * 3 - itemW, h - IS * 3 - itemH, IS * 2 + itemW, IS * 2 + itemH);
			_vs[3].setLayoutParams(lp);

			lp = new LayoutParams(IS * 2 + itemW, IS, 0, 0);
			_line[0].setLayoutParams(lp);

			lp = new LayoutParams(IS * 2 + itemW, IS, 0, IS + itemH);
			_line[5].setLayoutParams(lp);

			lp = new LayoutParams(IS * 2 + itemW, IS, 0, h - IS);
			_line[10].setLayoutParams(lp);

			lp = new LayoutParams(w - IS - itemW, IS, IS + itemW, 0);
			_line[1].setLayoutParams(lp);

			lp = new LayoutParams(w - IS - itemW, IS, IS + itemW, IS + itemH);
			_line[6].setLayoutParams(lp);

			lp = new LayoutParams(w - IS - itemW, IS, IS + itemW, h - IS);
			_line[11].setLayoutParams(lp);

			lp = new LayoutParams(IS, IS * 2 + itemH, 0, 0);
			_line[2].setLayoutParams(lp);

			lp = new LayoutParams(IS, IS * 2 + itemH, IS + itemW, 0);
			_line[3].setLayoutParams(lp);

			lp = new LayoutParams(IS, IS * 2 + itemH, w - IS, 0);
			_line[4].setLayoutParams(lp);

			lp = new LayoutParams(IS, h - IS - itemH, 0, IS + itemH);
			_line[7].setLayoutParams(lp);

			lp = new LayoutParams(IS, h - IS - itemH, IS + itemW, IS + itemH);
			_line[8].setLayoutParams(lp);

			lp = new LayoutParams(IS, h - IS - itemH, w - IS, IS + itemH);
			_line[9].setLayoutParams(lp);
			
			//一个视频窗口要改的地方1-2
			if (_nMaxIndex != -1) {
				MoveVS(_nMaxIndex, 1);
			} else {
				for (int i = 0; i < 4; ++i) {
					MoveVS(i, 0);
				}
			}
			
		}
	}*/

	void SetSideColor(int index, int color) {
		if (index < 0 || index > 4) {
			return;
		}

		int a[] = { 0, 2, 3, 5, 1, 3, 4, 6, 5, 7, 8, 10, 6, 8, 9, 11, 0, 1, 2, 4, 7, 9, 10, 11 };

		int s = index * 4;
		int end = index == 4 ? 8 : 4;
		for (int i = 0; i < end; ++i) {
			_line[a[i + s]].setBackgroundColor(color);
		}
	}

	// 处理选择事件
	// long _lastClick = 0;
	public void Selected(int index, int nClickedCount) {
		if (index < 0 || index > 3) {
			return;
		}

		if (index != _nSelected) {
			SetSideColor(_nSelected, COLOR_UNSELECTED);
			SetSideColor(index, COLOR_SELECTED);
			_nSelected = index;
		}
		
		/*// 处理双击事件
		if (nClickedCount > 1) {
			//隐藏上下标题栏
			if (Fun_RealPlay.instance._videWnd.GetSelected().IsPlay()) {
				Fun_RealPlay.instance.setView();
			}
		}
		if(nClickedCount == 1){
			if(nCmdType == 0  && Fun_RealPlay.instance._bPtzShow){
				Fun_RealPlay.instance.setPTZsit(vectorB);
				//APP.SendMsg(R.id.fun_realplay, XMSG.ON_CLICKED_PTZ_SIT, vectorB);
			}	
		}

		if (_wndListener != null) {
			_wndListener.OnClickWnd(_vs[index], nClickedCount > 1,index);
		}*/
	}

	//窗口最大化
	public void SetMax(int index) {
		/*if (index < 0 || index > 3) {
			return;
		}
		_nMaxIndex = index;
		SetSideColor(_nSelected, COLOR_UNSELECTED);
		SetSideColor(4, COLOR_SELECTED);
		for (int i = 0; i < 4; ++i) {
			if (i != index) {
				_vs[i].setVisibility(View.GONE);
			}
		}
		MoveVS(_nMaxIndex, 1);*/
	}

	public void Restore() {
		/*if (_nMaxIndex == -1) {
			return;
		}

		SetSideColor(4, COLOR_UNSELECTED);
		SetSideColor(_nSelected, COLOR_SELECTED);
		for (int i = 0; i < 4; ++i) {
			if (i != _nMaxIndex) {
				_vs[i].setVisibility(View.VISIBLE);
			}
		}
		MoveVS(_nMaxIndex, 0);
		_nMaxIndex = -1;*/
	}

	int _nClickedCount = 0;
	View _lastClieckView = null;
	protected MyHandler _handler = new MyHandler();
	public final static int DOUBLE_CLICKED = 100;

	//1.点击视频窗口事件 发送消息(双击就是点两次)
	public void onClick(View v) {
		if (v.equals(_lastClieckView)) {
			//主要控制双击事件 单个视频可以注释
			_nClickedCount++;
		} else {
			_nClickedCount = 1;
			Message msg = new Message();
			msg.what = DOUBLE_CLICKED;
			msg.obj = v;
			_handler.sendMessageDelayed(msg, 300);
		}
		_lastClieckView = v;
	}

	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			//android.os.Handler.java //父类中的方法 中为空
			super.handleMessage(msg); //这句什么都没有操作  可以不要
			switch (msg.what) {
			case DOUBLE_CLICKED:
				Selected(((VSPlayer) msg.obj).GetIndex(), _nClickedCount);
				_nClickedCount = 0;
				_lastClieckView = null;
				break;
			}
		}
	}

	private float x1;
	private float x2;
	Vector vectorA;
	Vector vectorB;
	float baseValue;
	float nLenStart, nLenEnd = 0;
	public static int nCmdType = 0; //双指2 单指1   关闭PTZ 初始化一下
	int _times = 0;
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return true;
	}
	
	public class Vector {
		public float X = 0;
		public float Y = 0;
		
		public float getX() {
			return X;
		}
		public void setX(float x) {
			X = x;
		}
		public float getY() {
			return Y;
		}
		public void setY(float y) {
			Y = y;
		}
	}

	/*float dotProduct(Vector vectorA, Vector vectorB) {
		return vectorA.X * vectorB.X + vectorA.Y * vectorB.Y;
	}
	public int getAsin(Vector vectorA, Vector vectorB){
		int x1 = (int)vectorA.X,x2=(int)vectorB.X; //点1坐标;
        int y1 = (int)vectorA.Y,y2=(int)vectorB.Y; //点2坐标
        int x = Math.abs(x1-x2);
        int y = Math.abs(y1-y2);
        double z=Math.sqrt(x*x+y*y);
        return Math.round((float)(Math.asin(y/z)/Math.PI*180));//最终角度
	}*/
	
	
}
