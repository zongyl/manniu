package com.views;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
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
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.analog.camera.audio.AudioQueue;
import com.views.analog.camera.encode.DecoderDebugger;
import com.views.analog.camera.encode.DecoderQueue;
import com.views.bovine.Fun_AnalogVideo;
import com.vss.vssmobile.decoder.H264Dec;

@SuppressLint("WrongCall")
public class NewSurfaceTest2 extends XViewBasic{
	private String TAG = NewSurfaceTest2.class.getSimpleName();
	public static NewSurfaceTest2 instance = null;
	RelativeLayout footer;//, header;
	FrameLayout framelayout;
	//LinearLayout _hreadframeLayout;
	FrameLayout _hreadframeLayout;
	LinearLayout.LayoutParams params;
    SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    public Surface _surface;
    int ret;
    private int[] pixels;
    Button play, fullscreen, cut, video,_btnBack;//back, more,
    TextView _devName;
    public boolean isPlay = false; //视频播放状态
   // private MyOrientationEventListener myOrientationEventListener = null;
    Handler handler = new Handler();
    private MyHandler _handler = null;
    public int _playId = 0;
    
    private Dlg_WaitForActivity _dlgWait = null;
    /** 视频解码器 */
	long m_decoder = -1;
	Lock m_decoderLock = null;
	
	private final int MAX_IMG_BUFFER_SIZE = 2560 * 1600 * 3;
	public Rect m_rect = null;
	/** Image数据 */
	byte[] m_imageData = null;
	/** 绘图源数据 */
	Bitmap m_imageBitmap = null;
	public DecoderDebugger _decoderDebugger = null;//视频硬解码 ok
	public decoderThead _deThead = null;//4楼软解码线程
	//public VSPlayer _vsPlayer = null;//SDK软解码库
	public DecoderQueue _decoderQueue;//解码队列
	public AudioQueue _sQueue; //音频软解码 AAC  卡
    
//    private String getDeviceName(){
//    	return getIntent().getExtras().getString("deviceName");
//    }
    
//    private int getDeviceChannel(){
//    	return getIntent().getExtras().getInt("channel");
//    }
	/**
	 * 以数组形式返回 尺寸 [0] = width, [1] = height
	 * @return
	 */
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		APP.GetMainActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
	String devName = "",devSid = "";
	@SuppressLint("NewApi")
	public NewSurfaceTest2(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		//setContentView(R.layout.new_surface_test);
		instance = this;
				
		framelayout = (FrameLayout)findViewById(R.id.frame);
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.hhheader);
		params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
		m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay1);
		m_surfaceHolder = m_prevewview.getHolder(); 
		//m_surfaceHolder.addCallback((Callback) this);	
		// 为surfaceHolder添加一个回调监听器
		m_surfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {	
				_width = width;
				_height = height;
				m_rect = new Rect(0, 0, _width, _height);
				isScreenChange();
			}
			@Override
			public void surfaceCreated(SurfaceHolder holder) {	
				if(_decoderDebugger == null){
					_surface = holder.getSurface();
					//_decoderDebugger = new DecoderDebugger(_surface);
					
				}
				_sQueue = new AudioQueue();
				//_handler.sendEmptyMessage(XMSG.P2PConnect);
			}
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}
			
		});
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		footer = (RelativeLayout)findViewById(R.id.pagefooter);
		//header = (RelativeLayout)findViewById(R.id.pageheader);
		play = (Button)findViewById(R.id.btn_play_play);
		fullscreen = (Button)findViewById(R.id.btn_play_fullscreen);
		//back = (Button)findViewById(R.id.btn_play_back);
		//more = (Button)findViewById(R.id.btn_play_more);
		cut = (Button)findViewById(R.id.btn_play_cut);
		video = (Button)findViewById(R.id.btn_play_video);
		_devName = (TextView)findViewById(R.id.dev_name);
		_devName.setText(devName);
		play.setOnClickListener(this);
		fullscreen.setOnClickListener(this);
		//back.setOnClickListener(this);
		//more.setOnClickListener(this);
		cut.setOnClickListener(this);
		video.setOnClickListener(this);
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
		//ProgressDialog MyDialog = ProgressDialog.show(this, " " , " 正在打开视频 ... ", true);
		// 解码器
		m_decoderLock = new ReentrantLock();
		m_decoder = H264Dec.InitDecoder();
		_deThead = new decoderThead();
		if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
		//_vsPlayer = new VSPlayer(m_prevewview);
		NewMain._isOpen = true;
		isCloseChannel = true;
		_handler = new MyHandler();
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
	
	private void showOrHide(View v, int visible){
		v.setVisibility(visible);
	}
	
	private Bitmap cut(){
		View view = APP.GetMainActivity().getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();		
	}
	
	private Bitmap surfaceBmp(){
		Bitmap bitmap = Bitmap.createBitmap(_width, _height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		doDraw(canvas);
		return bitmap;
	}
	
	public void saveBitmap(Bitmap bitmap){
		Log.i("debug", "....saveBitmap.....");
		File file = new File("/sdcard/"+System.currentTimeMillis()+".png");
		if(file.exists()){
			file.delete();
		}
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			Log.i("debug", "....save success!.....");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void orientation(){
		Configuration cfg = APP.GetMainActivity().getResources().getConfiguration();	
		if(cfg.orientation == 1){
			_hreadframeLayout.setVisibility(View.GONE);
			APP.GetMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}else if(cfg.orientation == 2){
			_hreadframeLayout.setVisibility(View.VISIBLE);
			APP.GetMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
	}
	
	
	//软解码方法
	public void h264Decoder(byte[] data,int len){
		int[] frameParam = new int[4];
		byte[] bmpBuff = null;
		ByteBuffer bytBuffer = null;
		Bitmap bmp = null;

		long handle = m_decoder;
		byte[] outBytes = m_imageData;
		//SurfaceView surfaceView = m_prevewview;

		m_decoderLock.lock();
		//long t1 = System.currentTimeMillis();
		int nRet = H264Dec.DecoderNal(handle, data, len, frameParam, outBytes);
		//long t2 = System.currentTimeMillis();
		//System.out.println("解码时间.........................："+(t2-t1));
		m_decoderLock.unlock();

		if (nRet > 0) {
			//SurfaceHolder surfaceHolder = surfaceView.getHolder();
			int width_frame = frameParam[2];//352
			int height_frame = frameParam[3];//288
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
				Canvas videoCanvas = null;
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
					m_imageBitmap = bmp;
					if (videoCanvas != null) {
						m_surfaceHolder.unlockCanvasAndPost(videoCanvas);
						//videoCanvas = null;
					}
				}
			}
		}
	}
	
	private void layout(){
		if(pixels[0] > pixels[1]){
			APP.GetMainActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
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
	boolean isCloseChannel = true;//是否关闭通道
	public void stop(){
		if(isStop){
			try {
				isStop = false;
				long t3= System.currentTimeMillis();
				if (_decoderDebugger._isRecording) {
					_decoderDebugger._isRecording = false;
					video.setBackgroundResource(R.drawable.control_icon_small_video_n);
				}
				_sQueue.Stop();
				if(_decoderQueue != null){
					_decoderQueue.Stop();
					_decoderQueue = null;
				}
				_deThead.de_stop();
				if(_decoderDebugger != null){
					_decoderDebugger.close();
					_decoderDebugger = null;
				}
				
				H264Dec.UninitDecoder(m_decoder);
				long t4= System.currentTimeMillis();
				LogUtil.d(TAG, " 退出app .time "+(t4-t3));
				SDK.isInitDecoder = false;
				
				if(isPlay){
					long t1= System.currentTimeMillis();
					if(isCloseChannel){
						SDK.P2PCloseChannel(SDK._sessionId,0);
					} 
					SDK.P2PClose(SDK._sessionId);
					SDK._sessionId = 0;
					isPlay = false;
					long t2= System.currentTimeMillis();
					LogUtil.d(TAG, " 退出SDK.time "+(t2-t1));
				}
				
				//System.gc();
			} catch (Exception e) {
				LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}
	long[] _sessionId = new long[1];
	private void doPlay(){
		try{
			long t1 = System.currentTimeMillis();
			int nRet = SDK.P2PConnect(devSid,_sessionId);
			long t2 = System.currentTimeMillis();
			LogUtil.d(TAG, "SDK.P2PConnect time :"+(t2-t1) +" ret= "+nRet);
			if(nRet == 0){
				SDK._sessionId = _sessionId[0];
				
				long t3 = System.currentTimeMillis();
				_playId = SDK.P2PCreateChannel(SDK._sessionId,0,1,20,10000, 352,288);
				long t4 = System.currentTimeMillis();
				LogUtil.i(TAG,"..调用SDK.P2PCreateChannel返回ret:"+_playId+"---"+_playId+"--"+SDK._sessionId+" time="+(t4-t3));
				if(_playId > 0){
					//startTimer();
					isPlay = true;
					if(_decoderQueue == null){
						_decoderQueue = new DecoderQueue();
						_decoderQueue.Start();
					}
					_deThead.de_start();
					_sQueue.Start();
				}else{
					Message msg = new Message();
					msg.what = XMSG.CREATECHANLL;
					msg.obj = _playId;
					_handler.sendMessage(msg);
					closeWait();
					SDK._sessionId = 0;
				}
				
			}else{
				SDK.SendJsonPck(1,SDK.getJsonString(devSid));
				closeWait();
				APP.ShowToast(SDK.GetErrorStr((int)nRet));
				SDK._sessionId = 0;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void closeWait(){
		if(_dlgWait.isShowing()) _dlgWait.dismiss();
	}
	
	public static java.util.Timer _timer = null;
	//定时器 如果长时间打洞不成功或打洞成功收不到数据 关闭页面
	void startTimer() {
		try {
			synchronized (this) {
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
						if(!SDK.isInitDecoder && _playId > 0){
							APP.ShowToast(SDK.GetErrorStr(_playId));
						}else{
							stopTimer();
						}
					}
				}, 12000, 12000);
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}

	void stopTimer() {
		synchronized (this) {
			if (_timer != null) {
				_timer.cancel();
				_timer = null;
			}
		}
	}
	
	Canvas _canvas;
	int _width, _height;

	private void doDraw(Canvas canvas){
		canvas = m_surfaceHolder.lockCanvas(null);
		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		if(_width < _height){
			canvas.drawRect(new Rect(0, 0, _width, _height/2), paint);
		}else{
			canvas.drawRect(new Rect(0, 0, _width, _height), paint);
		}
		m_surfaceHolder.unlockCanvasAndPost(canvas);
	}
	
	class MyThread implements Runnable{
		Timer timer;
		TimerTask timerTask;
		@Override
		public void run() {
			timer = new Timer();
			timerTask = new TimerTask(){
				@Override
				public void run() {
					doDraw(_canvas);
				}
			};
			timer.schedule(timerTask, 0, 1000);
		}
	}
	
	/**
	 * 隐藏
	 */
	Runnable dis = new Runnable(){
		@Override
		public void run() {
			showOrHide(footer);
		}
	};

	//sdk 收到异常情况关闭
	public void closeNewSurface(int error){
		_handler.sendEmptyMessage(error);
	}
	
	//直接跳到播放页面
//	public void init(){
//		try {
//			_dlgWait.show();
//			_dlgWait.UpdateText(getText(R.string.reopen_stream).toString());
//			_handler.sendEmptyMessage(XMSG.P2PConnect);
//		} catch (Exception e) {
//			LogUtil.e(TAG, e.getMessage());
//		}
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_play_play:
			stop();
			break;
		case R.id.btn_play_fullscreen:
			/*Log.i("debug", "....ocTouch....."+event.getAction());
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				fullscreen.setBackgroundResource(R.drawable.control_icon_full_screen_p);
			} if(event.getAction() == MotionEvent.ACTION_UP){
				orientation();
				fullscreen.setBackgroundResource(R.drawable.control_icon_full_screen_n);
			}*/
			orientation();
			break;
		case R.id.btn_play_cut:
			break;
		case R.id.btn_play_video:
//			if(event.getAction() == MotionEvent.ACTION_DOWN){
//				video.setBackgroundResource(R.drawable.control_icon_small_video_p);
//			}if(event.getAction() == MotionEvent.ACTION_UP){
//				video.setBackgroundResource(R.drawable.control_icon_small_video_n);
//			}

			// 如果正在录像，则停止
			if (_decoderDebugger._isRecording && isPlay) {
				video.setBackgroundResource(R.drawable.control_icon_small_video_n);
				APP.ShowToast(APP.GetMainActivity().getText(R.string.Video_record_end).toString());
				_decoderDebugger._isRecording = false;
			} else {	// 开始录像
				video.setBackgroundResource(R.drawable.control_icon_small_video_p);
				String strDate = DateUtil.getCurrentStringDate(DateUtil.DEFAULT_DATE_TIME_FORMAT);
				String fileName = Fun_AnalogVideo.RecordPath + strDate + devName;
				File dir = new File(Fun_AnalogVideo.RecordPath.substring(0,Fun_AnalogVideo.RecordPath.length()-1));
				if(!dir.exists()) dir.mkdirs();
				if((int)SdCardUtils.getSurplusStorageSize(Fun_AnalogVideo.RecordPath) > 20){
					_decoderDebugger._fileName = fileName + ".h264";
//					Fun_AnalogVideo.instance.h.mCamera.takePicture(null, null, Fun_AnalogVideo.instance.h.BmpCallback);
//					File efile = new File(fileName + ".bmp");
//					if(efile.exists()){
//						Fun_AnalogVideo.instance.h.mPacketizer.recordFile(fileName + ".h264");
//					}
					_decoderDebugger.recordFile(fileName + ".h264");
				}else{
					APP.ShowToast(APP.GetMainActivity().getText(R.string.Video_Storage_space_err).toString());
				}
			}
			break;
		//case R.id.btn_play_back://关闭视频返回
			//stop();
			//break;
		default:
			break;
		}
	}
	
//	@Override 
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
//			stop();
//		}
//		return true;
//	}
	
/*	@Override
	protected void onResume() {
		super.onResume();
		Log.i("debug", ".......onResume........");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i("debug", ".......onPause........");
	}
	
	@Override
	protected void onStop() {
		isStop = true;
		super.onStop();
	}
	@Override
    protected void onDestroy(){
		super.onDestroy();
		instance = null;
		m_imageData = null;
		m_prevewview = null;
		_decoderDebugger = null;
		m_decoderLock = null;
		_handler = null;
		System.gc();
		NewSurfaceTest2.this.finish();
    }*/
	
	@SuppressWarnings("static-access")
	public void isScreenChange() {
		Configuration mConfiguration = APP.GetMainActivity().getResources().getConfiguration(); //获取设置的配置信息
		int ori = mConfiguration.orientation ; //获取屏幕方向
		if(ori == mConfiguration.ORIENTATION_LANDSCAPE){//横屏
			_hreadframeLayout.setVisibility(View.GONE);
		}else if(ori == mConfiguration.ORIENTATION_PORTRAIT){//竖屏
			_hreadframeLayout.setVisibility(View.VISIBLE);
		}
	}
	
	
	class MyOrientationEventListener extends OrientationEventListener{
		public MyOrientationEventListener(Context context, int rate) {
			super(context, rate);
		}

		@Override
		public void onOrientationChanged(int orientation) {			
			if(orientation > 350 || orientation < 10){
				APP.GetMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);				
			}else if(orientation > 80 && orientation < 100){
				APP.GetMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}else if(orientation > 170 && orientation < 190){
				APP.GetMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);				
			}else if(orientation > 260 && orientation < 280){
				APP.GetMainActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}else{}
			
		}
		
	}
	
	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case XMSG.CREATECHANLL:
				APP.ShowToast(SDK.GetErrorStr(_playId));
				break;
			case XMSG.P2PConnect:
				doPlay();
				_dlgWait.UpdateText(APP.GetMainActivity().getText(R.string.reopen_stream).toString());
				break;
			case -5000:
				isCloseChannel = true;
				APP.ShowToast(APP.GetMainActivity().getText(R.string.Video_NetWork_Err).toString());
				stop();
				break;
			case 1:
				isCloseChannel = false;
				APP.ShowToast(APP.GetMainActivity().getText(R.string.Video_Dviece_BUSY).toString());
				stop();
				break;
			}
		}
	}
	
	//解码线程
	public class decoderThead implements Runnable{
		private boolean runFlag;
		Thread _sthread = null;
		private final int MAX_SIZE = 5000;
		public Queue<DevQueueBean> _queue = new LinkedList<DevQueueBean>();
		
		public decoderThead(){
		}
		
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
			synchronized (_queue) {
				if(_queue.size() < MAX_SIZE){
					_queue.offer(new DevQueueBean(data,length));	
				}
			}		
		}
		@Override
		public void run() {
			while(isRunFlag()){
				try {
					synchronized (_queue) {
						if(_queue != null && _queue.size() > 0){
							DevQueueBean bean = _queue.poll();
							h264Decoder(bean.getData(), bean.getLength());
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


	
}