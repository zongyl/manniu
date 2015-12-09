package com.views;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.analog.camera.audio.AudioQueue;
import com.views.analog.camera.encode.DecoderDebugger;
import com.views.analog.camera.encode.DecoderQueue;
import com.views.bovine.Fun_AnalogVideo;
import com.vss.vssmobile.decoder.H264Dec;
import com.vss.vssmobile.decoder.Mp4Enc;

@SuppressLint({ "WrongCall", "NewApi" })
public class NewSurfaceTest extends Activity implements SurfaceHolder.Callback, OnClickListener{
	private String TAG = NewSurfaceTest.class.getSimpleName();
	public static NewSurfaceTest instance = null;
	RelativeLayout footer;//, header;
	FrameLayout framelayout;
	FrameLayout _hreadframeLayout;
	LinearLayout.LayoutParams params;
    SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    int ret;
    private int[] pixels;
    Button play, fullscreen, cut, video,_btnBack,_btnGpu;//back, more,
    TextView _devName;
    public static boolean isPlay = false; //视频播放状态
    private boolean isGpu = false;//软硬切换
   // private MyOrientationEventListener myOrientationEventListener = null;
    Handler handler = new Handler();
    private MyHandler _handler = null;
    public static int _playId = 0; //播放ID
    public boolean isResume = false;
    
    private Dlg_WaitForActivity _dlgWait = null;
    /** 视频解码器 */
	long m_decoder = -1;
	Lock m_decoderLock = new ReentrantLock();
	private final int MAX_IMG_BUFFER_SIZE = 2560 * 1600 * 3;
	public Rect m_rect = null;
	/** Image数据 */
	byte[] m_imageData = null;
	/** 绘图源数据 */
	Bitmap m_imageBitmap = null;
	public DecoderDebugger _decoderDebugger = null;//视频硬解码 ok
	//public decoderThead _deThead = null;//4楼软解码线程
	//public VSPlayer _vsPlayer = null;//SDK软解码库暂时不用
	public DecoderQueue _decoderQueue;//解码队列
	public AudioQueue _sQueue; //音频软解码 AAC  卡
    
//	private RealHandler _realHandler = null;
//	private BaseApplication mAPP = null;
    
    private int channelNo = 0;
    
    private boolean isNvr(){
    	if(getIntent().getExtras().containsKey("nvr")){
    		return true;
    	}
    	return false;
    }
    
	/**
	 * 以数组形式返回 尺寸 [0] = width, [1] = height
	 * @return
	 */
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
	String devName = "",devSid = "";
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_surface_test);
		instance = this;
		
		devName = getIntent().getExtras().getString("deviceName");//设备名称
		devSid = getIntent().getExtras().getString("deviceSid");
		channelNo = getIntent().getExtras().getInt("channel");
				
		framelayout = (FrameLayout)findViewById(R.id.frame);
		//_hreadframeLayout = (LinearLayout) this.findViewById(R.id.hhheader);
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.hhheader);
		params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
		m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay1);
		m_surfaceHolder = m_prevewview.getHolder(); 
		m_surfaceHolder.addCallback((Callback) this);	
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		footer = (RelativeLayout)findViewById(R.id.pagefooter);
		//header = (RelativeLayout)findViewById(R.id.pageheader);
		play = (Button)findViewById(R.id.btn_play_play);
		fullscreen = (Button)findViewById(R.id.btn_play_fullscreen);
		//back = (Button)findViewById(R.id.btn_play_back);
		//more = (Button)findViewById(R.id.btn_play_more);
		cut = (Button)findViewById(R.id.btn_play_cut);
		video = (Button)findViewById(R.id.btn_play_video);
		_btnGpu = (Button) findViewById(R.id.btn_play_gpu);
		_devName = (TextView)findViewById(R.id.dev_name);
		if(isNvr()){
			_devName.setText(devName +" channel:"+ (channelNo+1));
		}else{
			_devName.setText(devName);
		}
		play.setOnClickListener(this);
		fullscreen.setOnClickListener(this);
		//back.setOnClickListener(this);
		//more.setOnClickListener(this);
		cut.setOnClickListener(this);
		video.setOnClickListener(this);
		_btnGpu.setOnClickListener(this);
		_btnBack = (Button) this.findViewById(R.id.btn_back_video);
		_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		
		m_prevewview.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				//alert("onLongClick!");
				return false;
			}
		});
		
		m_prevewview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				show(footer);
			}
		});
		
		pixels = getSize();
		layout();
		if(_dlgWait == null){
			_dlgWait = new Dlg_WaitForActivity(this,R.style.dialog);
			_dlgWait.setCancelable(true);
		}
		// 解码器
		//m_decoder = H264Dec.InitDecoder();
		
		//_deThead = new decoderThead();
//		try {
//			if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
//		} catch (Exception e) {
//		}
		NewMain._isOpen = true;//打开视频标志位
		_handler = new MyHandler();
		_btnGpu.setClickable(true);
		cut.setClickable(true);
		
//		mAPP = (BaseApplication)this.getApplication();
//		_realHandler = new RealHandler();
//		// 设置共享变量
//		mAPP.setRedlandler(_realHandler);
	}

	/**
	 * 显示、隐藏  
	 * @param v
	 */
	private void showOrHide(View v){
		if(v.isShown()){
			v.setVisibility(View.INVISIBLE);
			handler.removeCallbacks(dis);
		}else{
			v.setVisibility(View.VISIBLE);
		}
	}
	
	private void show(View v){
		if(!v.isShown()){
			v.setVisibility(View.VISIBLE);
			handler.postDelayed(dis, 2000);
		}
	}
	
	private void orientation(){
		Configuration cfg = getResources().getConfiguration();	
		if(cfg.orientation == 1){
			_hreadframeLayout.setVisibility(View.GONE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}else if(cfg.orientation == 2){
			_hreadframeLayout.setVisibility(View.VISIBLE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}
	
	private void alert(String msg){
		Toast.makeText(this, msg, 1000).show();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		pixels = getSize();
		layout();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if(_decoderDebugger == null){
				_decoderDebugger = new DecoderDebugger(holder.getSurface(),NewSurfaceTest.this);
			}
			//_sQueue = new AudioQueue();
			_dlgWait.show();
			_dlgWait.UpdateText(getText(R.string.P2PConnect).toString());
			_handler.sendEmptyMessage(XMSG.P2PConnect);
			//LogUtil.i(TAG, "surfaceCreated");
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			_width = width;
			_height = height;
			m_rect = new Rect(0, 0, _width, _height);
			isScreenChange();
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("debug", "....surfaceDestroyed....");
		//SDK.end();
	}
	
//	private byte[] _data;
//	public void setData(byte[] data,int length){
//    	this._data = data;
//    	if(null != _data){
//    		EncodeTask mEncTask = new EncodeTask(data,length);
//			mEncTask.execute((Void) null);
//		}
//    }
	
	//异步方法
//	private class EncodeTask extends AsyncTask<Void, Void, Void> {
//		private byte[] mData;
//		private int length;
//		// 构造函数
//		EncodeTask(byte[] data,int length) {
//			this.mData = data;
//			this.length = length;
//		}
//		@Override
//		protected Void doInBackground(Void... params) {
//			h264Decoder(mData,length);
//			return null;
//		}
//	}
	
	//硬解时截图
	public void h264DecoderSnapImg(final byte[] data,final int len){
		try {
			if(_snapImg){
				screenCount++;
				if(screenCount < 20){
					if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
					//m_imageBitmap = BitmapUtils.Bytes2Bimap(data);
					m_imageBitmap = BitmapUtils.getScreenBitmap(data, m_imageData, len);
					if(m_imageBitmap != null){
						long fileLong = snapPic(m_imageBitmap, _fileName);
						//if(fileLong >= 204800){
							_decoderQueue._startSnap = false;
							_snapImg = false;
							screenCount=0;
							m_imageBitmap = null;
							cut.setClickable(true);
						//}
					}
				}else{
					_decoderQueue._startSnap = false;
					_snapImg = false;
					screenCount=0;
					_handler.sendEmptyMessage(100);
					cut.setClickable(true);
				}
			}
		} catch (Exception e) {
		}
	}
	
	private int screenCount = 0;
	Canvas videoCanvas = null;
	private int i_flag = 0;//I帧标志位
	//软解码方法
	public void h264Decoder(byte[] data,int len,int isIFrame){
		try {
			if(i_flag == 0 && isIFrame == 1){
				i_flag = 1;
			}
			if(i_flag == 1){//第一帧保证I帧
				//截图
				if(_snapImg && isIFrame == 1){
					h264DecoderSnapImg(data, len);
				}
				//录像
				if(_decoderQueue._isRecording){
					if(NewMain.devType == 4){
						_decoderQueue.raf.write(data);
					}else{
						Mp4Enc.InsertVideoBuffer(Mp4Enc.handle, data, len);
					}
				}
				if(_decoderDebugger.canDecode){
					if(NewMain.devType == 1){//IPC去头
						//int exHead =  Integer.valueOf(G.byte2hex(bean.getData(), 22, 1));
						int exHead = (int)data[22];
						int realHead = 24 + exHead;
						int realLen = len - realHead - 8;
						byte[] newbuf = new byte[realLen];
						System.arraycopy(data, realHead, newbuf, 0, realLen);
						_decoderDebugger.decoder(newbuf, realLen);
					}else{ //模拟不用去头
						byte[] newbuf = new byte[len-32];
						System.arraycopy(data, 24, newbuf, 0, len-32);
						if(NewSurfaceTest.instance._decoderDebugger.decoder(newbuf, newbuf.length) == -1) i_flag = 0;
					}
				}else{
					int[] frameParam = new int[4];
					byte[] bmpBuff = null;
					ByteBuffer bytBuffer = null;
					Bitmap bmp = null;

					long handle = m_decoder;
					try {
						if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
					} catch (Exception e) {
					}
					byte[] outBytes = m_imageData;
					m_decoderLock.lock();
					int nRet = H264Dec.DecoderNal(handle, data, len, frameParam, outBytes);
					m_decoderLock.unlock();

					if (nRet > 0) {
						int width_frame = frameParam[2];//352
						int height_frame = frameParam[3];//288
						if (width_frame > 0 && height_frame > 0) {
							bmpBuff = new byte[width_frame * height_frame * 2];

							bmp = Bitmap.createBitmap(width_frame, height_frame,
									android.graphics.Bitmap.Config.RGB_565);
							if (bmpBuff != null) {
								System.arraycopy(outBytes, 0, bmpBuff, 0, width_frame * height_frame * 2);
								bytBuffer = ByteBuffer.wrap(outBytes);
								bmp.copyPixelsFromBuffer(bytBuffer);
							}
							//Canvas videoCanvas = null;
							try {
								videoCanvas = m_surfaceHolder.lockCanvas();
								if (videoCanvas != null) {
									videoCanvas.drawColor(Color.BLACK);
									Rect rect = m_rect;
									videoCanvas.drawBitmap(bmp, null, rect, null);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								m_imageBitmap = bmp;//软解截图时用这个
								if (videoCanvas != null) {
									m_surfaceHolder.unlockCanvasAndPost(videoCanvas);
									videoCanvas = null;
								}
							}
						}
					}
//					if(_snapImg){
//						_decoderQueue._startSnap = false;
//						_snapImg = false;
//						//_handler.sendEmptyMessage(100);
//						long fileLong = snapPic(m_imageBitmap, _fileName);
//						if(fileLong >= 204800){// || NewMain.devType == 4
//							_decoderQueue._startSnap = false;
//							_snapImg = false;
//						}
//					}
					if (!bmp.isRecycled()) {
						bmp.recycle();
						}
				}
			}
			
		} catch (Exception e) {
		}
	}
	
	//软解码方法
	public synchronized void h264Decoder2(byte[] outBytes,int len){
		try {
			if(!_decoderDebugger.canDecode){
				Bitmap bmp = null;
				byte[] bmpBuff = null;
				ByteBuffer bytBuffer = null;
				if (len > 0) {
					int width_frame = SDK._width;//352
					int height_frame = SDK._height;//288
					if (width_frame > 0 && height_frame > 0) {
						bmpBuff = new byte[width_frame * height_frame * 3];

						bmp = Bitmap.createBitmap(width_frame, height_frame,
								android.graphics.Bitmap.Config.RGB_565);
						if (bmpBuff != null) {
							System.arraycopy(outBytes, 0, bmpBuff, 0, width_frame * height_frame * 2);
							bytBuffer = ByteBuffer.wrap(outBytes);
							bmp.copyPixelsFromBuffer(bytBuffer);
						}
						
						try {
							videoCanvas = m_surfaceHolder.lockCanvas();
							if (videoCanvas != null) {
								videoCanvas.drawColor(Color.BLACK);
								Rect rect = m_rect;
								videoCanvas.drawBitmap(bmp, null, rect, null);
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							m_imageBitmap = bmp;//软解截图时用这个
							if (videoCanvas != null) {
								m_surfaceHolder.unlockCanvasAndPost(videoCanvas);
								videoCanvas = null;
							}
						}
					}
				}
				if(_snapImg){
//					_decoderQueue._startSnap = false;
//					_snapImg = false;
					//_handler.sendEmptyMessage(100);
					long fileLong = snapPic(m_imageBitmap, _fileName);
					if(fileLong >= 204800){// || NewMain.devType == 4
						_decoderQueue._startSnap = false;
						_snapImg = false;
						cut.setClickable(true);
					}
				}
				if (!bmp.isRecycled()) {
					bmp.recycle();
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	private void layout(){
		if(pixels[0] > pixels[1]){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			show(footer);
			params.height = pixels[1];
			params.width = pixels[0];
			framelayout.setLayoutParams(params);
		}else{
			show(footer);
			params.height = pixels[1]/2;
			params.width = pixels[0];
			framelayout.setLayoutParams(params);
		}
	}
	
	
	boolean isStop = true;//stop 标志位
	//boolean isCloseChannel = true;//是否关闭通道
	public void stop(){
		if(isStop){
			try {
				//LogUtil.d(TAG, "start...stop...");
				isStop = false;
				long t3= System.currentTimeMillis();
				if (_decoderQueue != null && _decoderQueue._isRecording) {//如果正在录像关闭录像
					stopRecordingVideo();
				}
				//_deThead.de_stop();
				if(_decoderQueue != null){//视频队列
					_decoderQueue.Stop();
					_decoderQueue = null;
				}
				if(_sQueue != null){//音频队列
					_sQueue.Stop();
					_sQueue = null;
				}
				if(_decoderDebugger != null){
					if(_decoderDebugger.isCanDecode()) _decoderDebugger.close();
					_decoderDebugger = null;
				}
				//H264Dec.UninitDecoder(m_decoder);
				long t4= System.currentTimeMillis();
				LogUtil.d(TAG, " 退出app .time= "+(t4-t3));
				SDK.isInitDecoder = false;
				if(isPlay){
					LogUtil.i(TAG, "start p2pclose ...isPlay = "+isPlay+"--"+SDK._sessionId);
					long t1= System.currentTimeMillis();
					SDK.P2PCloseChannel(SDK._sessionId, channelNo);
					SDK.P2PClose(SDK._sessionId);
					SDK._sessionId = 0;
					isPlay = false;
					long t2= System.currentTimeMillis();
					LogUtil.d(TAG, "end p2pclose 退出SDK.time "+(t2-t1));
				}
				finish();
				System.gc();
			} catch (Exception e) {
				LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}
	long[] _sessionId = new long[1];
	private int p2p_Count= 0;//P2P连接失败次数标识
	//type 0-初始化要开 1-断线打开
	public synchronized void doPlay(int type){
		try{
			SDK.DataSourceDeviceType(NewMain.devType);
			long t1 = System.currentTimeMillis();
			int nRet = SDK.P2PConnect(devSid,_sessionId);
			long t2 = System.currentTimeMillis();
			LogUtil.d(TAG, "SDK.P2PConnect return= "+nRet+" time= "+(t2-t1)+" devSid = "+devSid +" devName = "+devName +" channelNo = "+channelNo);
			if(nRet == 0){
				SDK._sessionId = _sessionId[0];
				long t3 = System.currentTimeMillis();
				_playId = SDK.P2PCreateChannel(SDK._sessionId, channelNo,1,20,10000, 352,288);
				long t4 = System.currentTimeMillis();
				LogUtil.i(TAG,"SDK.P2PCreateChannel return="+_playId+" sessionId= "+SDK._sessionId+" time="+(t4-t3));
				stopTimer();
				if(_playId > 0){
					//模拟目前不要发消息
					if(NewMain.devType ==1)
						_handler.sendEmptyMessageDelayed(XMSG.ON_PLAY,12000);//12秒收不到数据 提示打开视频失败!
					if(_decoderDebugger != null) _decoderDebugger.flag = 0;
					if(type == 0){
						isPlay = true;
						//LogUtil.i(TAG,"..isPlay===="+isPlay);
						if(_decoderQueue == null){
							_decoderQueue = new DecoderQueue();
							_decoderQueue.Start();
						}
						if(NewMain.devType == 4){//模拟打开音频队列，IPC目前没有音频 
							_sQueue = new AudioQueue();
							_sQueue.Start();
						}
					}else if(type == 1){
						closeWait();
					}
				}else{//创建通道失败不用重试直接退出
					Message msg = new Message();
					msg.what = XMSG.CREATECHANLL;
					msg.obj = _playId;
					_handler.sendMessage(msg);
				}
			}else{//连接失败消息处理
				//连接3次不成功关闭
				p2p_Count ++;
				if(p2p_Count == 3){
					errorP2PConnect(nRet);
				}
			}
		}catch(Exception e){
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	private void errorP2PConnect(int nRet){
		SDK.SendJsonPck(1,SDK.getJsonString(devSid));
		closeWait();
		Message msg = new Message();
		msg.what = XMSG.SMS_P2PConnect;
		msg.obj = nRet;
		if(_handler != null) _handler.sendMessage(msg);
		SDK._sessionId = 0;
		_runFlag = false;
		p2p_Count = 0;
		this.finish();
	}
	
	public void closeWait(){
		if(_dlgWait.isShowing()) _dlgWait.dismiss();
	}
	public void showGpu(){
		//判断一下 如果支持硬解把图片换一下
		if(_decoderDebugger.isCanDecode()){
			_handler.sendEmptyMessage(XMSG.PLAY_GPU_OK);
		}else{
			_handler.sendEmptyMessage(XMSG.PLAY_GPU);
		}
	}
	
	
	//网络断开打开提示框
	public void openWait(){
		if(_decoderQueue != null){
			_decoderQueue.clearQueue();
		}
		_dlgWait.show();
		_dlgWait.UpdateText(getText(R.string.Video_NetWork_reload).toString());
	}
	
	
	public java.util.Timer _timer = null;
	public static boolean _runFlag = true; //判断是否打洞
	//定时器 管理 P2PConnect 连接
	public void startTimer(final int type) {
		try {
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
					if(_runFlag){
						//每次打开定时器之前清空消息队列
						if(_handler != null) _handler.removeMessages(XMSG.ON_PLAY);
						doPlay(type);
					}
				}
			}, 0, 5000);
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}
	void stopTimer() {
		try {
			if (_timer != null) {
				_timer.cancel();
				_timer = null;
				_runFlag = false;
			}
		} catch (Exception e) {
		}
	}
	
	int _width, _height;
	/**
	 * 隐藏
	 */
	Runnable dis = new Runnable(){
		@Override
		public void run() {
			showOrHide(footer);
		}
	};

	//sdk 收到异常情况关闭 1设备忙  -5000连接异常
	public void closeNewSurface(int error){
		try {
			if(_handler != null)
				_handler.sendEmptyMessage(error);
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	//截图功能
	@SuppressLint("SimpleDateFormat")
	public long snapPic(Bitmap bitmap,String fileName) {
		BitmapUtils.saveBitmap(bitmap, fileName);
		File file = new File(fileName);
		if(file.isFile() && file.exists()){// && file.length() >= 204800 夜间抓图会小于200K 
			if(fileName.indexOf("images") != -1){
				_handler.sendEmptyMessage(XMSG.PLAY_SNAP);
			}
		}
		return file.length();
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getFileName(){
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String strDate = sdf.format(date);
		String month = (calendar.get(Calendar.MONTH)+1) < 10 ?"0"+(calendar.get(Calendar.MONTH)+1):""+(calendar.get(Calendar.MONTH)+1);
		String path = Fun_AnalogVideo.ImagePath +File.separator+ calendar.get(Calendar.YEAR)+ month + File.separator;
		File dir = new File(path);
		if(!dir.exists()) dir.mkdirs();
		String fileName = path + strDate + devName + ".bmp";
		fileName = fileName.replace(" ", "");
		fileName = fileName.replace("-", "");
		fileName = fileName.replace(":", "");
		return fileName;
	}
	
	public String _fileName = "";//截图文件名
	public String _recordfileName = "";//录像文件名
	public boolean _snapImg = false;//软解截图
	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.btn_play_play:
				stop();
				break;
			case R.id.btn_play_fullscreen:
				orientation();
				break;
			case R.id.btn_play_cut:
				cut.setClickable(false);
				_fileName = getFileName();
				_decoderQueue._startSnap = true;
				_snapImg = true;
				
				break;
			case R.id.btn_play_video:
				// 如果正在录像，则停止
				if (_decoderQueue._isRecording && isPlay) {
					APP.ShowToast(getText(R.string.Video_record_end).toString());
					stopRecordingVideo();
				} else {	// 开始录像
					video.setBackgroundResource(R.drawable.control_icon_small_video_p);
					String strDate = DateUtil.getCurrentStringDate(DateUtil.DEFAULT_DATE_TIME_FORMAT);
					_recordfileName = Fun_AnalogVideo.RecordPath + strDate + devName;
					File dir = new File(Fun_AnalogVideo.RecordPath.substring(0,Fun_AnalogVideo.RecordPath.length()-1));
					if(!dir.exists()) dir.mkdirs();
					if((int)SdCardUtils.getSurplusStorageSize(Fun_AnalogVideo.RecordPath) > 20){
						_fileName = _recordfileName + ".bmp";
						_decoderQueue._startSnap = true;
						_snapImg = true;
						_dlgWait.show();
						_dlgWait.UpdateText(getText(R.string.set_pwd).toString());
						startNotify();
					}else{
						APP.ShowToast(getText(R.string.Video_Storage_space_err).toString());
					}
				}
				break;
			case R.id.btn_play_gpu://软硬解切换
				if(isGpu){
					_btnGpu.setClickable(false);
					_decoderQueue.Stop();
					isGpu = false;
					_decoderDebugger.canDecode = false;
					_decoderDebugger.close();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					_btnGpu.setBackgroundResource(R.drawable.gpu_false);
					_decoderQueue.Start();
					APP.ShowToast(getText(R.string.Video_stop_gpu).toString());
					_btnGpu.setClickable(true);
					SDK.SetDecoderModel(1);
				}else{
					_btnGpu.setClickable(false);
					isGpu = true;
					_decoderDebugger.canDecode = true;
					stopPause();
					SDK.SetDecoderModel(0);
					NewSurfaceTest.this.recreate();
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}
	}
	//停止录像
	private void stopRecordingVideo(){
		video.setBackgroundResource(R.drawable.control_icon_small_video_n);
		_decoderQueue._isRecording = false;
//		if(NewMain.devType == 1){
//			Mp4Enc.stop(Mp4Enc.handle);
//		}else{
			_decoderQueue.h264ToMp4();
//		}
	}
		
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
			stop();
		}
		return true;
	}
	
	public void stopPause(){
		try {
			LogUtil.d(TAG, "start...stopPause...");
			if (_decoderQueue != null && _decoderQueue._isRecording) {//如果正在录像关闭录像
				stopRecordingVideo();
			}
			if(_sQueue != null) _sQueue.Stop();
			if(_decoderQueue != null){
				_decoderQueue.Stop();
				_decoderQueue = null;
			}
			if(_decoderDebugger != null){
				_decoderDebugger.close();
				_decoderDebugger = null;
			}
			SDK.isInitDecoder = false;
			if(isPlay){
				SDK.P2PCloseChannel(SDK._sessionId, channelNo);
				SDK.P2PClose(SDK._sessionId);
				SDK._sessionId = 0;
				isPlay = false;
			}
		} catch (Exception e) {
			LogUtil.i(TAG, "stopPause.."+ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	//暂停/恢复
	public void playPause(int bPause){
		if(bPause == 0){
			//doPlay();
		}
		if(bPause == 1 && isPlay){
			stopPause();
			isResume = true;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//Log.i("debug", ".......onResume........");
		if(isResume){
			playPause(0);
			isResume = false;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//Log.i("debug", ".......onPause........");
		playPause(1);
	}
	
	@Override
	protected void onStop() {
		try {
			if(!isGpu){
				SDK.SetDecoderModel(0);
			}
			SDK._flag = 0;
			//SDK.Ffmpegh264DecoderUninit();
			isStop = true;
			closeWait();
			stopTimer();
			_handler.removeMessages(XMSG.ON_PLAY);
			LogUtil.i(TAG, "onStop.....end..");
		} catch (Exception e) {
		}
		super.onStop();
	}
	@Override
    protected void onDestroy(){
		super.onDestroy();
		try {
			instance = null;
			m_imageData = null;
			if (m_imageBitmap != null && !m_imageBitmap.isRecycled()) {  
				m_imageBitmap.recycle();
			}
			m_imageBitmap = null;
			m_prevewview = null;
			m_surfaceHolder = null;
			_decoderDebugger = null;
			_handler = null;
			LogUtil.i(TAG, "onDestroy.....end..");
			//System.gc();
		} catch (Exception e) {
		}
    }
	
	@SuppressWarnings("static-access")
	public void isScreenChange() {
		Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = mConfiguration.orientation ; //获取屏幕方向
		if(ori == mConfiguration.ORIENTATION_LANDSCAPE){//横屏
			_hreadframeLayout.setVisibility(View.GONE);
		}else if(ori == mConfiguration.ORIENTATION_PORTRAIT){//竖屏
			_hreadframeLayout.setVisibility(View.VISIBLE);
		}
	}
	
	
	/*class MyOrientationEventListener extends OrientationEventListener{
		public MyOrientationEventListener(Context context, int rate) {
			super(context, rate);
		}

		@Override
		public void onOrientationChanged(int orientation) {			
			if(orientation > 350 || orientation < 10){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);				
			}else if(orientation > 80 && orientation < 100){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}else if(orientation > 170 && orientation < 190){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);				
			}else if(orientation > 260 && orientation < 280){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}else{}
		}
	}*/
	
	private int recordCount = 0;
	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case XMSG.CREATECHANLL://创建通道失败
					closeWait();
					APP.ShowToast(SDK.GetErrorStr(_playId));
					SDK.P2PCloseChannel(SDK._sessionId, channelNo);
					SDK.P2PClose(SDK._sessionId);
					SDK._sessionId = 0;
					NewSurfaceTest.this.finish();
					break;
				case XMSG.SMS_P2PConnect:
					int ret = (Integer) msg.obj;
					APP.ShowToast(SDK.GetErrorStr(ret));
					break;
				case XMSG.P2PConnect:
					//doPlay(0);
					startTimer(0);
					_runFlag = true;
					_dlgWait.UpdateText(getText(R.string.reopen_stream).toString());
					break;
				case -5000:
					//isCloseChannel = true;
					APP.ShowToast(getText(R.string.Video_NetWork_Err).toString());
					if(isPlay){
						openWait();
						startTimer(1);
						_runFlag = true;
					}
					break;
				case 1://设备忙
					//isCloseChannel = false;
					APP.ShowToast(getText(R.string.Video_Dviece_BUSY).toString());
					stop();
					break;
				case XMSG.ON_PLAY:
					if(!SDK.isInitDecoder && _playId > 0 && isPlay){
						APP.ShowToast(getText(R.string.video_failopen).toString());
						LogUtil.d(TAG, "12秒未收到数据调 stop()....");
						stop();
					}
					break;
				case XMSG.PLAY_SNAP:
					APP.ShowToast(getText(R.string.Video_snap_success).toString());
					break;
				case 100:
					APP.ShowToast("截图失败!");
					//btnPhotoFileClick();
					break;
				case XMSG.PLAY_GPU:
					isGpu = false;
					_btnGpu.setBackgroundResource(R.drawable.gpu_false);
					APP.ShowToast(getText(R.string.Video_err_gpu).toString());
					_btnGpu.setClickable(false);
					break;
				case XMSG.PLAY_GPU_OK:
					isGpu = true;
					_btnGpu.setBackgroundResource(R.drawable.gpu_true);
					break;
				case XMSG.GetNotify://定时判断截图成功
					recordCount ++;
					File efile = new File(_recordfileName + ".bmp");
					if(recordCount < 10){
						if(efile.exists()){
//							long handle = Mp4Enc.handle;
//							Mp4Enc.startwrite(handle,_recordfileName + ".mp4");
							_decoderQueue.recordFile(_recordfileName);
							stopNotify();
							closeWait();
							recordCount = 0;
						}
					}else{
						LogUtil.i(TAG, "录像次数。。。"+recordCount);
						recordCount = 0;
						video.setBackgroundResource(R.drawable.control_icon_small_video_n);
						_decoderQueue._isRecording = false;
						efile.delete();
						stopNotify();
						closeWait();
						APP.ShowToast(getText(R.string.Video_record_error).toString());
						break;
					}
					
					if (_bNotify) {
						_handler.sendEmptyMessageDelayed(XMSG.GetNotify, 500); //延迟发送
					}
					break;
				}
			} catch (Exception e) {
			}
		}
	}
	
	boolean _bNotify = false;
	private void startNotify() {
		if (!_bNotify) {
			_handler.sendEmptyMessage(XMSG.GetNotify);
			_bNotify = true;
		}
	}
	private void stopNotify() {
		_bNotify = false;
	}
	
	//解码队列线程
	public class decoderThead implements Runnable{
		private boolean runFlag;
		Thread _sthread = null;
		public final static int MAX_SIZE = 500;
		public int i_flag = 0;//I帧标志位
		private Queue<DevQueueBean> _queue = new LinkedList<DevQueueBean>();
		
		public void de_start() {
			try {
				synchronized (_queue) {
					runFlag = true;
					if(_sthread == null){
						_sthread = new Thread(this);
					}
					_sthread.start();
				}
			} catch (Exception e) {
				LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
			}
		}
		public void de_stop() {
			try {
				runFlag = false;
				_sthread = null;
				while (_queue.size() > 0) {
					_queue.poll();
				}
			} catch (Exception e) {
			}
		}
		
		public boolean isRunFlag() {
			return runFlag;
		}

		public void addData(byte[] data,int length){
			try {
				synchronized (_queue) {
					if(!isStop ) return;
					if(_queue.size() < MAX_SIZE){
						_queue.offer(new DevQueueBean(data,length));	
						//Log.i(TAG, "............111");
					}
				}
			} catch (Exception e) {
			}
		}
		@Override
		public void run() {
			while(runFlag){
				try {
					synchronized (_queue) {
						if(_queue != null && _queue.size() > 0){
							DevQueueBean bean = _queue.poll();
//							h264Decoder(bean.getData(), bean.getLength());
//							Log.i(TAG, ".........软解码..........222");
							
							if(NewMain.devType == 1){//IPC去头
								//int exHead =  Integer.valueOf(G.byte2hex(bean.getData(), 22, 1));
								int exHead = (int)bean.getData()[22];
								int realHead = 24 + exHead;
								int realLen = bean.getLength() - realHead - 8;
								byte[] newbuf = new byte[realLen];
								System.arraycopy(bean.getData(), realHead, newbuf, 0, realLen);
								if(_decoderDebugger.decoder(newbuf, realLen) == -1) i_flag = 0;
							}else{ //模拟不用去头
								byte[] newbuf = new byte[bean.getLength()-32];
								System.arraycopy(bean.getData(), 24, newbuf, 0, bean.getLength()-32);
								if(_decoderDebugger.decoder(newbuf, newbuf.length) == -1) i_flag = 0;
							}
							
						}
					}
				} catch (Exception e) {
					LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
				}
			}
		}
		
	}
	
	public class DevQueueBean{
		public byte[] data;
		public int length;
		DevQueueBean(){
		}
		DevQueueBean(byte[] data,int length){
			this.data = data;
			this.length = length;
		}
		
		public byte[] getData() {
			return data;
		}
		public void setData(byte[] data) {
			this.data = data;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
	}
	
	/**
	 * 自己实现 Handler 处理消息更新UI
	 * @author mark
	 */
	/*@SuppressLint("HandlerLeak")
	public class RealHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case XMSG.PLAY:
				closeWait();
				showGpu();
				break;
			}
		}
	}*/


	
}