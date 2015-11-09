package com.views.analog.camera.encode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import P2P.MediaStream;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import com.basic.APP;
import com.basic.XMSG;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.NewSurfaceTest;

@SuppressLint("ViewConstructor")
public class VSPlayer implements Runnable {
	private String TAG = VSPlayer.class.getSimpleName();
	static final int NEXT_SOUND = 10;
	SurfaceView   _surfaceView;
	SurfaceHolder _holder;
	Canvas _canvas = null;
	boolean _isVPlaying = false;
	long _nRunNum = 1;

	MediaStream _sdk = null;
	IntBuffer _pSrcBuf = null;
	byte[] _pByte = null;
	
	//ByteBuffer _pAudioBuf = null;
	ByteBuffer _realDataBuf = null;
	int _nViewWidth = 0; //窗口宽
	int _nViewHeight = 0; // 窗口高
	int _nFullScreen = 0; //是否全屏
	public int _nFullScreenType = 0; 
	Handler _handler = null;
	int _index = 0;
	static ExecutorService s_threads = null;
	boolean _bDraw = true;
	//AudioTrack _audio;
	//MySound _sound = new MySound();
	int _timeRate = 40;
	public int _imgType = 0;
	String _nodeId;
	int _sensorNo;
	int _dataType;
	private BufferedOutputStream outputStream;
	String fileName = Environment.getExternalStorageDirectory()+"/IPC_test.264";

	public VSPlayer(SurfaceView surfaceView) {
		this._surfaceView = surfaceView;
		//线程池
		if (s_threads == null) {
			s_threads = Executors.newFixedThreadPool(4);
		}
		_handler = new MyHandler();
		_nFullScreen = 0;
		_imgType = 0;
		_holder = _surfaceView.getHolder();
		//getHolder().addCallback(VSPlayer.this);
		if (null == _pSrcBuf) {
			//_pSrcBuf = IntBuffer.allocate(704 * 576 + 128);
			_pSrcBuf = IntBuffer.allocate(1024 * 1024 + 128);
		}
		_pByte = new byte[1024 * 1024 + 128];
		_nFullScreenType = 0;
		if (1 == _nFullScreen) {
			_nFullScreenType = 1;
		}
		_sdk = new MediaStream();
		File f = new File(fileName);
	    touch (f);
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }

	}
	
	public void touch(File f) {
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//关联wideoWndCtril   OnVisibility
	protected void OnVisibility(int visibility) {
		_bDraw = visibility == View.VISIBLE;
		DrawBK();
		DrawBK();
	}

	int GetIndex() {
		return _index;
	}

	public int GetFullScreenType() {
		return _nFullScreenType;
	}

	public MediaStream GetMediaSDK() {
		return _sdk;
	}

	public boolean IsPlay() {
		return _isVPlaying;
	}

//	public void surfaceCreated(SurfaceHolder holder) {
//		synchronized (this) {
//			_holder = holder;
//		}
//	}
//	
//	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		if (width != _nViewWidth || height != _nViewHeight) {
//			_nViewWidth = width;
//			_nViewHeight = height;
//		}
//		DrawBK();
//		DrawBK();
//	}
	
/*	//判断横竖屏
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
	}*/

	//背景在VSPlayer/DrawBK画的.
	public void DrawBK() {
		try {
			synchronized (this) {
				if (_holder == null) {
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
					nLastW = nLastH = 0;
				if (_holder != null)
					_holder.unlockCanvasAndPost(_canvas);
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "DrawBK  "+ExceptionsOperator.getExceptionInfo(e));
			return;
		}
	}
	
	public void DrawBK2() {
		try {
			if (_holder == null) {
				return;
			}
			_canvas = _holder.lockCanvas(null);
			if (_canvas == null) {
				return;
			}
			if (_isVPlaying) {
				Paint paint = new Paint();
				paint.setColor(Color.BLACK);
				_canvas.drawRect(new Rect(0, 0, _nViewWidth, _nViewHeight), paint);
				paint = null;
			} 
			if (_holder != null)
				_holder.unlockCanvasAndPost(_canvas);
		} catch (Exception e) {
			LogUtil.e(TAG, "DrawBK  "+ExceptionsOperator.getExceptionInfo(e));
			return;
		}
	}

	public int Start() {
		try {
			//this._sdk = video;
			_isVPlaying = true;
			//nLastW = nLastH = 0;
			// 因为是双缓冲，画两次，把前后缓冲背景都改变
			DrawBK();
			DrawBK();
			this.StartTimer(_timeRate);
		} catch (Exception e) {
			LogUtil.e(TAG, "MediaStream   start....."+ExceptionsOperator.getExceptionInfo(e));
			return 100;
		}
		return _isVPlaying ? 0 : -1;
	}
	
	//停止视频
	public void Stop() {
		try {
			if (_isVPlaying) {
				//flag = 0;
				_isVPlaying = false;
				this.StopTimer();
				_sdk._nFrameRate = 25;
				LogUtil.d(TAG,"Stop()  ok.....");
				File f = new File(fileName);
				if(f.exists()) f.delete();
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}


	//慢放
	public int Slow() {
		if (_sdk == null) {
			return 0;
		}
		return _sdk.Slow();
	}
	//快进
	public int Fast() {
		return _sdk.Fast();
	}

	public boolean Puase() {
		return _sdk.Pause();
	}

//	public void surfaceDestroyed(SurfaceHolder holder) {
//	}

	int nLastW = 0, nLastH = 0; //图片宽高
	int[] pFInfo = new int[7];
	int nStartX = 0, nStartY = 0;//绘图起点 都是0全屏
	int nRotateX = 0, nRotateY = 0; //旋转点(中心坐标点)   横过来的话会用到,不然用不到
	float fScaleX = 1f, fScaleY = 1f;   //图片缩放处理
	boolean bEddy = false;
	long _lastTime = 0;
	int _nCount = 0;
	byte[] data = new byte[256];
	long time;
	float realdata;
	String unit;
	long t11 = 0;
	float _lastfScaleX = 1f, _lastfScaleY = 1f;
	boolean thredState = true;
	int flag = 0; //播放帧标识
	boolean playFlag = true;//是否画图
	//int drop = 0;//是否转码
	public void run() {
		synchronized (this) {
		try {
			int nRet = 1;
			if (_isVPlaying == false || _bDraw == false || thredState == false) {
				return;
			}
			//实时视频 打开码流 图片
//				long t12 = System.currentTimeMillis();
//				System.out.println("执行时间："+(t12-t11));
//				t11 = t12;
			playFlag = true;
			//nRet = _sdk.GetBmp32Frame(_pSrcBuf.array(), pFInfo);
			nRet = _sdk.GetBmp32Frame(_pByte, pFInfo);
			
			if(playFlag){
				if(pFInfo[0] >= 1000){
					pFInfo[0] = Constants.imgViewWidth;
					pFInfo[1] = Constants.imgViewHeight;
				}
				if (_holder == null || 0 != nRet || pFInfo[0] <= 0 || pFInfo[1] <= 0) {
					return;
				}
				outputStream.write(_pByte);
				if (_timeRate != _sdk.NextTime) {
					_timeRate = _sdk.NextTime;
					_handler.sendEmptyMessage(XMSG.UPDATE_TIME_RATE); //发消息通道帧率改变
				}
				if (_bDraw == false) {
					return;
				}
				if (nLastW != pFInfo[0] || nLastH != pFInfo[1]) {
					this.DrawBK();
					this.DrawBK();
					nLastW = pFInfo[0];
					nLastH = pFInfo[1];
					//this._nFullScreen == 1 && 全屏时改变显示策略
					if (this._nFullScreen == 1 && (pFInfo[0] / pFInfo[1]) > (_nViewWidth / _nViewHeight)) {
						_nFullScreenType = 1;
						bEddy = true;
					} else {
						_nFullScreenType = 0;
						bEddy = false;
					}
					nStartX = 0;
					nStartY = 0;
					if (bEddy) {
						if((float) _nViewWidth / (float) nLastH < (float) _nViewHeight / (float) nLastW){
							//缩放的中心轴不在你旋转后的图片的中心上，就会偏
							//fScaleY = fScaleX = (float) _nViewWidth / (float) nLastH; //缩放
							fScaleY = fScaleX = (float) _nViewHeight / (float) nLastW; //不缩放
							float h0 = (_nViewHeight - fScaleY * nLastW) / 2f;
							nRotateX = (int) ((_nViewWidth - h0) / 2f);
							nRotateY = (int) (nRotateX + h0);
							fScaleY = (float) _nViewWidth / (float) nLastH;
						}else{
							fScaleY = fScaleX = (float) _nViewHeight / (float) nLastW;
						}
					} else {
						fScaleX = (float) _nViewWidth / nLastW;
						fScaleY = (float) _nViewHeight / nLastH;
						nRotateX = nRotateY = 0;
						//下面不要就是全屏，但图片拉伸了 所以还是要加缩放
						if (fScaleX < fScaleY) { //竖屏
							//APP.SendMsg(R.id.fun_realplay, XMSG.VERTICAL_SCREEN, 0);
							
							fScaleY = fScaleX;
							nStartY = (int) ((_nViewHeight - nLastH * fScaleY) / (2f * fScaleY));
						} else { //横屏
							//APP.SendMsg(R.id.fun_realplay, XMSG.CROSS_SCREEN, 0);
							
							_nFullScreen = 0;
							bEddy = false;
							playFlag = false;
//								fScaleX = fScaleY;
//								nStartX = (int) ((_nViewWidth - nLastW * fScaleX) / (2f * fScaleX));
						}
						if(_lastfScaleX != fScaleX && _lastfScaleY != fScaleY){
							_lastfScaleX = fScaleX;
							_lastfScaleY = fScaleY;
							this.DrawBK();
							this.DrawBK();
						}
					}
				}
			}
			if(playFlag){
				_canvas = _holder.lockCanvas(null); // 锁定画布，一般在锁定后就可以通过其返回的画布对象Canvas，在其上面画图等操作了
				if (_canvas == null) {
					return;
				}
				if (bEddy) {
					_canvas.rotate(90f, nRotateX, nRotateY);
				}
				_canvas.scale(fScaleX, fScaleY); // 图片缩放处理
				//offset就是起始偏移量（起始绘制点）stride 表示行扫描的宽度。hasAlpha 表示透明度Paint : 画笔对象
				if(nLastW < 1000 && _canvas != null && _pSrcBuf != null){
					//_canvas.drawBitmap(_pSrcBuf.array(), 0, Constants.imgViewWidth, nStartX, nStartY, Constants.imgViewWidth, Constants.imgViewHeight, false, null);
					Bitmap bitmap = Bytes2Bimap(_pByte);
					if(bitmap != null){
						//_canvas.drawBitmap(Bytes2Bimap(_pByte),0,0,null);
						//_canvas.drawBitmap(Bytes2Bimap(_pByte), null, NewSurfaceTest.instance.m_rect, null);
					}
				}
				if (_holder != null){
					_holder.unlockCanvasAndPost(_canvas); 
				}
			}
		} catch (Exception e) {
			this.StopTimer();
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			APP.ShowToast("视频打开失败");
			return;
		}
		}
	}
	
	
	public Bitmap Bytes2Bimap(byte[] outBytes) {
		Bitmap bmp = null;
		byte[] bmpBuff = null;
		ByteBuffer bytBuffer = null;
		try {
			if (outBytes.length != 0) {
				int width_frame = 352;
				int height_frame = 288;
				if (width_frame > 0 && height_frame > 0) {
					bmpBuff = new byte[width_frame * height_frame * 2];

					bmp = Bitmap.createBitmap(width_frame, height_frame,
							android.graphics.Bitmap.Config.RGB_565);

					if (bmpBuff != null) {
						System.arraycopy(outBytes, 0, bmpBuff, 0, width_frame
								* height_frame * 2);
						bytBuffer = ByteBuffer.wrap(outBytes);
						bmp.copyPixelsFromBuffer(bytBuffer);
					}
				}
		     }
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return bmp;
	}
	
	public Bitmap Bytes2Bimap2(byte[] b) {
		Bitmap bitmap = null;
		try {
			if (b.length != 0) {
		    	 bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
		     } else {
		         return bitmap;
		     }
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return bitmap;
	}

	public static java.util.Timer _timer = null;

	//定时器
	void StartTimer(int time) {
		try {
			synchronized (this) {
				//_isVPlaying = true;
				if (_timer != null) {
					_timer.cancel();
					_timer = null;
				}
				if(_timer == null){
					_timer = new java.util.Timer();
				}
				_timer.schedule(new TimerTask() {
					@Override
					public void run() {
						_handler.sendEmptyMessage(XMSG.UPDATE_VIEW);
					}
				}, 40, time);//那个40是指的第一次调用run的时间,time是指的调用间隔. (1000/20 1秒取50帧)
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}

	void StopTimer() {
		synchronized (this) {
			if (_timer != null) {
				//_isVPlaying = false;
				_timer.cancel();
				_timer = null;
			}
		}
	}

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case XMSG.UPDATE_VIEW:
				s_threads.execute(VSPlayer.this);
				break;
			case XMSG.UPDATE_SOUND:
				//s_threads.execute(_sound);
				break;
			case XMSG.UPDATE_TIME_RATE:
				if (_sdk != null && _sdk.IsPlay()) {
					VSPlayer.this.StopTimer();
					VSPlayer.this.StartTimer(_sdk.NextTime);
					//System.out.println("_sdk.NextTime:"+_sdk.NextTime+"--"+_sdk._nSpeed);
				}
				break;
			}
		}
	}
}
