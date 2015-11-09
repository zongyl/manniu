package com.views.analog.camera.encode;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

@SuppressLint("ViewConstructor")
public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private String TAG = VideoSurfaceView.class.getSimpleName();
	SurfaceHolder _holder;
	Canvas _canvas = null;
	boolean _isVPlaying = false;
	
	int _nViewWidth = 0; //窗口宽
	int _nViewHeight = 0; // 窗口高
	boolean _bDraw = true;

	public VideoSurfaceView(Context context) {
		super(context);
		getHolder().addCallback(VideoSurfaceView.this);

	}
	//关联wideoWndCtril   OnVisibility
	protected void OnVisibility(int visibility) {
		_bDraw = visibility == View.VISIBLE;
//		DrawBK();
//		DrawBK();
	}


	public boolean IsPlay() {
		return _isVPlaying;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		synchronized (this) {
			_holder = holder;
		}
		Fun_RealPlay.instance.initSuface(holder.getSurface());
		System.out.println(22);
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (width != _nViewWidth || height != _nViewHeight) {
			_nViewWidth = width;
			_nViewHeight = height;
		}
		//Fun_RealPlay.instance.isScreenChange();
//		if(Fun_RealPlay.isPlay){
//			if(isScreenChange()){//横
//				APP.SendMsg(R.id.fun_realplay, XMSG.CROSS_SCREEN, 0);
//			}else{
//				APP.SendMsg(R.id.fun_realplay, XMSG.VERTICAL_SCREEN, 0);
//			}
//		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println(333);
	}
	
	//判断横竖屏
	@SuppressWarnings("static-access")
	public boolean isScreenChange() {
		Configuration cf = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = cf.orientation ; //获取屏幕方向
		if(ori == cf.ORIENTATION_LANDSCAPE){
			//横屏
			return true;
		}else if(ori == cf.ORIENTATION_PORTRAIT){
			//竖屏
			return false;
		}
		return false;
	}
	
	//背景在VSPlayer/DrawBK画的.
		public void DrawBK() {
			try {
				synchronized (this) {
					if (_holder == null || this.getVisibility() != View.VISIBLE) {
						return;
					}
					_canvas = _holder.lockCanvas(null);
					if (_canvas == null) {
						return;
					}
					Paint paint = new Paint();
					paint.setColor(Color.BLACK);
					_canvas.drawRect(new Rect(0, 0, _nViewWidth, _nViewHeight), paint);
					paint = null;
					if (_holder != null)
						_holder.unlockCanvasAndPost(_canvas);
				}
			} catch (Exception e) {
				LogUtil.e(TAG, "DrawBK  "+ExceptionsOperator.getExceptionInfo(e));
				return;
			}
		}

	public int Start() {
		try {
//			DrawBK();
//			DrawBK();
		} catch (Exception e) {
			LogUtil.e(TAG, "MediaStream   start....."+ExceptionsOperator.getExceptionInfo(e));
			return 100;
		}
		return _isVPlaying ? 0 : -1;
	}

	
	//停止视频
	public void Stop() {
		try {
			_holder = null;
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}


	public void run() {
		synchronized (this) {
		try {

			
		} catch (Exception e) {
			return;
		}
		}
	}
	





}
