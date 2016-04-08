package com.views;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import P2P.SDK;
import P2P.ViESurfaceRenderer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.adapter.VideoPlaybackSimpleAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.DateTimePickerDialog;
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.analog.camera.audio.AudioQueue;
import com.views.analog.camera.encode.DecoderDebugger;
import com.views.bovine.Fun_AnalogVideo;

@SuppressLint({ "WrongCall", "NewApi" })
public class NewSurfaceTest extends Activity implements SurfaceHolder.Callback,OnClickListener{
	private String TAG = NewSurfaceTest.class.getSimpleName();
	public static NewSurfaceTest instance = null;
	RelativeLayout footer;//, header;
	FrameLayout framelayout;
	FrameLayout _hreadframeLayout;
	LinearLayout.LayoutParams params;
    SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;

    
    public VideoBackHandler videoHandler;
    private BaseApplication baseApp = null;
    
    int ret;
    private int[] pixels;
    Button play, fullscreen, cut, video,_btnBack,_btnGpu, prevday, nextday;//back, more,
    TextView _devName, today;
    public static boolean isPlay = false; //视频播放状态
    public static boolean isGpu = false;//软硬切换
    Handler handler = new Handler();
    private MyHandler _handler = null;
    public static int _playId = 0; //播放ID
    public boolean isResume = false;
    
    private Dlg_WaitForActivity _dlgWait = null;
    /** 视频解码器 */
	long m_decoder = -1;
	private final int MAX_IMG_BUFFER_SIZE = 2560 * 1600 * 3;
	public Rect m_rect = null;
	/** Image数据 */
	byte[] m_imageData = null;
	/** 绘图源数据 */
	Bitmap m_imageBitmap = null;
	public DecoderDebugger _decoderDebugger = null;//视频硬解码 ok
	//public DecoderQueue _decoderQueue;//解码队列(先改一版 不要队列数据来了直接显示的)
	public AudioQueue _sQueue; //音频软解码 AAC  卡
    
    private int channelNo = 0;
    
    //ViESurfaceRenderer vrenderer;//SDK画图方法--数据COPY在JNI里面
    public static int _paly =0;//0 P2P 1.回放
    GridView grid;
 
    private List<Map<String, Object>> items;
	
	private VideoPlaybackSimpleAdapter adapter;
	
	private Bundle data;
    
	private Context context;
    
	private Calendar mDate = Calendar.getInstance();
	
	private static String yyyyMMdd, startTime, endTime;
	
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
		yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		
		LogUtil.d(TAG, "yyyyMMdd:" + yyyyMMdd);
		
		framelayout = (FrameLayout)findViewById(R.id.frame);
		//_hreadframeLayout = (LinearLayout) this.findViewById(R.id.hhheader);
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.hhheader);
		params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
		m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay1);
		m_surfaceHolder = m_prevewview.getHolder(); 
		m_surfaceHolder.addCallback((Callback) this);	
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		ViESurfaceRenderer.ResetSurfaceHolder(this);
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
		
		baseApp = (BaseApplication)getApplication();
		videoHandler = new VideoBackHandler();
		grid = (GridView)findViewById(R.id.new_surface_grid);
		context = this.getApplicationContext();
		items = new ArrayList<Map<String, Object>>();
		grid.setOnItemClickListener(listener);
		init();

		prevday = (Button)findViewById(R.id.prevday);
		nextday = (Button)findViewById(R.id.nextday);
		today = (TextView)findViewById(R.id.taday);

		today.setText(yyyyMMdd);
		
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

		prevday.setOnClickListener(this);
		nextday.setOnClickListener(this);
		today.setOnClickListener(this);
		
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
		if(_dlgWait == null){
			_dlgWait = new Dlg_WaitForActivity(this,R.style.dialog);
			_dlgWait.setCancelable(true);
		}
		
		NewMain._isOpen = true;//打开视频标志位
		_handler = new MyHandler();
		_btnGpu.setClickable(true);
		cut.setClickable(true);
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
			System.out.println(_paly+"  "+startTime+"  "+endTime);
			if(_paly == 1){
				_dlgWait.show();
				_dlgWait.UpdateText(getText(R.string.P2PConnect).toString());
				if(isGpu) _decoderDebugger.canDecode = true;
				_handler.sendEmptyMessage(1002);
			}else{
				_dlgWait.show();
				_dlgWait.UpdateText(getText(R.string.P2PConnect).toString());
				_handler.sendEmptyMessage(XMSG.P2PConnect);
			}
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
			changeDestRect(width, height);
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("debug", "....surfaceDestroyed....");
	}
	
	private float dstRightScale = 1;
	private float dstBottomScale = 1;
	private void changeDestRect(int dstWidth, int dstHeight) {
		ViESurfaceRenderer.dstRect.right = (int)(ViESurfaceRenderer.dstRect.left + dstRightScale * dstWidth);
		ViESurfaceRenderer.dstRect.bottom = (int)(ViESurfaceRenderer.dstRect.top + dstBottomScale * dstHeight);
    }
	
	private int screenCount = 0; //截图失败总次数
	private int i_flag = 0;//I帧标志位
	//硬解时截图
	public void h264DecoderSnapImg(final byte[] data,final int len){
		try {
			if(_snapImg){
				
				/*if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
				m_imageBitmap = BitmapUtils.getScreenBitmap(data, m_imageData, len);
				if(m_imageBitmap != null){
					long fileLong = snapPic(m_imageBitmap, _fileName);
					if(fileLong >= 51200){
						_snapImg = false;
						screenCount=0;
						m_imageBitmap = null;
						cut.setClickable(true);
					}else{
						_snapImg = false;
						screenCount=0;
						_handler.sendEmptyMessage(100);
						cut.setClickable(true);
					}
				}*/
				
				screenCount++;
				if(screenCount < 10){
					if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
					m_imageBitmap = BitmapUtils.getScreenBitmap(data, m_imageData, len,SDK._width,SDK._height,SDK._sessionIdContext);
					if(m_imageBitmap != null){
						long fileLong = snapPic(m_imageBitmap, _fileName);
						if(fileLong >= 51200){
							_snapImg = false;
							screenCount=0;
							m_imageBitmap = null;
							cut.setClickable(true);
						}
					}
				}else{
					_snapImg = false;
					screenCount=0;
					_handler.sendEmptyMessage(100);
					cut.setClickable(true);
				}
			}
		} catch (Exception e) {
		}
	}
	
	public void setBitmap() {
		_handler.sendEmptyMessage(XMSG.UPDATE_VIEW);
	}
	
	public void drawBitmap() {
		if (ViESurfaceRenderer.bitmap == null)
			return;
		try {
			ViESurfaceRenderer.lckBitmap.lock();
			Canvas videoCanvas = m_surfaceHolder.lockCanvas();
			if (videoCanvas != null) {
				videoCanvas.drawBitmap(ViESurfaceRenderer.bitmap, ViESurfaceRenderer.srcRect, ViESurfaceRenderer.dstRect, null);
			}
			if (videoCanvas != null) {
				m_surfaceHolder.unlockCanvasAndPost(videoCanvas);
				videoCanvas = null;
			}
			ViESurfaceRenderer.lckBitmap.unlock();
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		} 
	}
	
	//软解码方法
	public void h264Decoder(byte[] data,int len,int isIFrame){
		try {
				if(_decoderDebugger.canDecode){//硬解
					if(i_flag == 0 && isIFrame == 1){
						i_flag = 1;
						closeWait();
					}
					if(i_flag == 1){//硬解第一帧保证I帧
						//截图
						if(_snapImg && isIFrame == 1){
							h264DecoderSnapImg(data, len);
						}
						if(NewMain.devType == 1 && SDK._manufactorType == 0){//IPC,智诺设备 去头
							int exHead = (int)data[22];
							if(exHead < 0) return;
							int realHead = 24 + exHead;
							int realLen = len - realHead - 8;
							byte[] newbuf = new byte[realLen];
							System.arraycopy(data, realHead, newbuf, 0, realLen);
							_decoderDebugger.decoder(newbuf, realLen);
						}else{ //模拟、海康设备不用去头
							if(_decoderDebugger.decoder(data, len) == -1) i_flag = 0;
						}
					}
				}/*else{//软解 
					//默认走软解
					if(i_flag == 0){
						i_flag = 1;
						CreateByteBuffer(SDK._width,SDK._height);
						closeWait();
					}
					if (len > 0) {
						int width_frame = SDK._width;//352
						int height_frame = SDK._height;//288
						if (width_frame > 0 && height_frame > 0) {
							if(byteBuffer == null)
					            return;
							long t1 = System.currentTimeMillis();
							byteBuffer = ByteBuffer.wrap(data);
					        byteBuffer.rewind();
					        bitmap.copyPixelsFromBuffer(byteBuffer);
					        long t2 = System.currentTimeMillis();
					        System.out.println("==========222=="+(t2-t1));
							try {
								videoCanvas = m_surfaceHolder.lockCanvas();
								if (videoCanvas != null) {
									videoCanvas.drawColor(Color.BLACK);
									Rect rect = m_rect;
									videoCanvas.drawBitmap(bitmap, null, rect, null);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								m_imageBitmap = bitmap;//软解截图时用这个
								if (videoCanvas != null) {
									m_surfaceHolder.unlockCanvasAndPost(videoCanvas);
									videoCanvas = null;
								}
							}
						}
					}
					if(_snapImg){
						long fileLong = snapPic(m_imageBitmap, _fileName);
						if(fileLong >= 102400){// || NewMain.devType == 4
							//_decoderQueue._startSnap = false;
							_snapImg = false;
							cut.setClickable(true);
						}
					}
				}*/
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
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
	
	public boolean isStop = true;//stop 标志位
	public void stop(){
		if(isStop){
			try {
				isGpu = false;//置成软解
				LogUtil.d(TAG, "start...stop...");
				isStop = false;
				_paly = 0;
				long t3= System.currentTimeMillis();
				if (_isRecording) {//如果正在录像关闭录像  _decoderQueue != null && _decoderQueue.
					stopRecordingVideo();
				}
				//_deThead.de_stop();
//				if(_decoderQueue != null){//视频队列
//					_decoderQueue.Stop();
//					_decoderQueue = null;
//				}
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
				LogUtil.d(TAG, " stop  app .time= "+(t4-t3));
				SDK.isInitDecoder = false;
				if(isPlay){
					isPlay = false;
					long t1= System.currentTimeMillis();
//					if(!isGpu){
//						SDK.SetDecoderModel(0,SDK._sessionId);
//					}
					if(_paly == 0)
						SDK.P2PCloseChannel(SDK._sessionIdContext, channelNo);
					LogUtil.i(TAG, "P2PCloseChannel  sessionId:"+SDK._sessionIdContext+"  devSid:"+devSid);
					SDK.P2PClose(SDK._sessionIdContext);
					LogUtil.i(TAG, "P2PClose  sessionId:"+SDK._sessionIdContext+" devSid:"+devSid);
					SDK._sessionIdContext = 0;
					long t2= System.currentTimeMillis();
					LogUtil.d(TAG, "end p2pclose stop SDK.time "+(t2-t1));
				}
				//SDK.CleanPool();
				finish();
				//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
				System.gc();
			} catch (Exception e) {
				LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}
	long[] _sessionId = new long[1];
	private int p2p_Count= 0;//P2P连接失败次数标识
	private int deviceChannelID = 1;
	//type 0-初始化要开 1-断线打开
	public synchronized void doPlay(int type){
		try{
			SDK.DataSourceDeviceType(NewMain.devType);
			long t1 = System.currentTimeMillis();
			int nRet = SDK.connectChannelP2P_device(devSid,deviceChannelID,connectChannelP2PHandler);
			long t2 = System.currentTimeMillis();
			LogUtil.d(TAG, "SDK.P2PConnect return= "+nRet+" time= "+(t2-t1)+" devSid = "+devSid +" devName = "+devName +" channelNo = "+channelNo);
			if(nRet == 0){
				//获取onstata 里面的状度 然后合建通道
				
				/*long t3 = System.currentTimeMillis();
				_playId = SDK.P2PCreateChannel(SDK._sessionId, channelNo,1,20,10000, 352,288);
				long t4 = System.currentTimeMillis();
				LogUtil.i(TAG,"SDK.P2PCreateChannel return="+_playId+" time="+(t4-t3)+" sessionID="+SDK._sessionId);
				stopTimer();
				if(_playId > 0){
					//模拟目前不要发消息
					if(NewMain.devType ==1)
						_handler.sendEmptyMessageDelayed(XMSG.ON_PLAY,12000);//12秒收不到数据 提示打开视频失败!
					if(_decoderDebugger != null) _decoderDebugger.flag = 0;
					if(type == 0){
						isPlay = true;
						if(!isGpu){
							_decoderDebugger.release();
							closeWait();
						}else{
							_decoderDebugger.canDecode = true;
						}
						if(_sQueue == null){//模拟打开音频队列，IPC目前没有音频 
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
				}*/
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
	
	//打洞返回值的消息处理
	@SuppressLint("HandlerLeak")
	private Handler connectChannelP2PHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bdData = msg.getData();
			// 创建通道
			long lContext = bdData.getLong("Context");
			int nStatus = bdData.getInt("Status");
			if (nStatus == 0){// p2pConnect成功
				SDK._sessionIdContext = lContext;
				if(!isGpu) SDK.SetDecoderModel(1,SDK._sessionIdContext);
				_playId = SDK.P2PCreateChannel(SDK._sessionIdContext, channelNo,1,20,10000, 352,288);
				LogUtil.i(TAG,"SDK.P2PCreateChannel return="+_playId+" time="+1+" sessionID="+SDK._sessionIdContext);
				if(_playId >= 0){
					isPlay = true;
					//模拟目前不要发消息
					if(NewMain.devType ==1)
						_handler.sendEmptyMessageDelayed(XMSG.ON_PLAY,12000);//12秒收不到数据 提示打开视频失败!
					if(_sQueue == null){//模拟打开音频队列，IPC目前没有音频 
						_sQueue = new AudioQueue();
						_sQueue.Start();
					}
					if(_decoderDebugger != null) _decoderDebugger.flag = 0;
					if(!isGpu){
						_decoderDebugger.release();
						closeWait();
						talkAudio();
					}else{
						_decoderDebugger.canDecode = true;
					}
				}else{//创建通道失败不用重试直接退出
					Message err_msg = new Message();
					err_msg.what = XMSG.CREATECHANLL;
					err_msg.obj = _playId;
					_handler.sendMessage(err_msg);
				}
			} else {
				//通过消息发送 重试三次打洞处理
				_handler.sendEmptyMessage(XMSG.P2PConnect);
				//连接3次不成功关闭 不需要关闭连接
				p2p_Count ++;
				if(p2p_Count == 3){
					errorP2PConnect(nStatus);
				}
				//收到 -5000 要关闭连接
				if (nStatus == -5000) {
					SDK.P2PClose(lContext);
				}
			}
		}
	};
	
	
	
	//type 0-初始化要开 1-断线打开  录像回放
	public synchronized void doBackPlay(String startTime, String endTime){
		try{
			SDK.DataSourceDeviceType(NewMain.devType);
			long t1 = System.currentTimeMillis();
			int nRet = SDK.connectChannelP2P_device(devSid,deviceChannelID,alarmConnectChannelP2PHandler);
			long t2 = System.currentTimeMillis();
			LogUtil.d(TAG, "SDK.P2PConnect return= "+nRet+" time= "+(t2-t1)+" devSid = "+devSid +" devName = "+devName +" channelNo = "+channelNo+" sessionID="+SDK._sessionIdContext);
			if(nRet == 0){
				/*long t3 = System.currentTimeMillis();
				_playId = SDK.PlaybackCtrl(SDK._sessionIdContext, 0, "", startTime, endTime, channelNo, 1);
				long t4 = System.currentTimeMillis();
				LogUtil.i(TAG,"SDK.P2PCreateChannel return="+_playId+" time="+(t4-t3)+" sessionID="+SDK._sessionIdContext);
				if(_playId >= 0){
					isPlay = true;
					//closeWait();
				}else{//创建通道失败不用重试直接退出
					Message msg = new Message();
					msg.what = XMSG.CREATECHANLL;
					msg.obj = _playId;
					_handler.sendMessage(msg);
				}*/
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
	
	//远程回放打洞返回值的消息处理
	@SuppressLint("HandlerLeak")
	private Handler alarmConnectChannelP2PHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bdData = msg.getData();
			// 创建通道
			long lContext = bdData.getLong("Context");
			int nStatus = bdData.getInt("Status");
			if (nStatus == 0){// p2pConnect成功
				SDK._sessionIdContext = lContext;
				if(!isGpu) SDK.SetDecoderModel(1,SDK._sessionIdContext);
				_playId = SDK.PlaybackCtrl(SDK._sessionIdContext, 0, "", startTime, endTime, channelNo, 1);
				LogUtil.i(TAG,"SDK.P2PCreateChannel return="+_playId+" time="+1+" sessionID="+SDK._sessionIdContext);
				if(_playId >= 0){
					isPlay = true;
				}else{//创建通道失败不用重试直接退出
					Message msg1 = new Message();
					msg1.what = XMSG.CREATECHANLL;
					msg1.obj = _playId;
					_handler.sendMessage(msg1);
				}
			} else {
				//通过消息发送 重试三次打洞处理
//				_handler.sendEmptyMessage(XMSG.P2PConnect);
				//连接3次不成功关闭 不需要关闭连接
				p2p_Count ++;
				if(p2p_Count == 3){
					errorP2PConnect(nStatus);
				}
			}
		}
	};
	
	
	private void errorP2PConnect(int nRet){
		SDK.SendJsonPck(1,SDK.getJsonString(devSid));
		closeWait();
		Message msg = new Message();
		msg.what = XMSG.SMS_P2PConnect;
		msg.obj = nRet;
		if(_handler != null) _handler.sendMessage(msg);
		SDK._sessionIdContext = 0;
		_runFlag = false;
		p2p_Count = 0;
		this.finish();
		APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
	}
	
	public void closeWait(){
		if(_dlgWait != null && _dlgWait.isShowing()) _dlgWait.dismiss();
	}
	public void showGpu(){
		//判断一下 如果支持硬解把图片换一下
		if(_decoderDebugger.isCanDecode()){
			_handler.sendEmptyMessage(XMSG.PLAY_GPU_OK);
		}else{
			_handler.sendEmptyMessage(XMSG.PLAY_GPU);
		}
	}
	//开户音频
	public void talkAudio(){
		//_handler.sendEmptyMessageAtTime(XMSG.PLAY_AUDIO, 500);
		startNotify(XMSG.PLAY_AUDIO);
	}
	
	//网络断开打开提示框
	public void openWait(){
//		if(_decoderQueue != null){
//			_decoderQueue.clearQueue();
//		}
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
		if(file.isFile() && file.exists() && file.length() >= 102400){// && file.length() >= 204800 夜间抓图会小于200K 
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
	
	public void screenshot(Bitmap bitmap){
		long fileLong = snapPic(bitmap, _fileName);
		if(fileLong >= 102400){// || NewMain.devType == 4
			//_decoderQueue._startSnap = false;
			_snapImg = false;
			cut.setClickable(true);
		}
	}
	
	public String _fileName = "";//截图文件名
	public String _recordfileName = "";//录像文件名
	public boolean _snapImg = false;//软解截图
	private boolean _isRecording = false;//录像
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
				//_decoderQueue._startSnap = true;
				_snapImg = true;
				
				break;
			case R.id.btn_play_video:
				// 如果正在录像，则停止
				if (_isRecording && isPlay) {
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
						//_decoderQueue._startSnap = true;
						_snapImg = true;
						_dlgWait.show();
						_dlgWait.UpdateText(getText(R.string.set_pwd).toString());
						startNotify(XMSG.GetNotify);
						/*if(_decoderDebugger.isCanDecode()){
							startNotify();
						}else{
							SDK.SetVideoPath(_recordfileName + ".h264",_recordfileName + ".aac");
							_decoderQueue._isRecording = true;
						}*/
					}else{
						video.setBackgroundResource(R.drawable.control_icon_small_video_n);
						APP.ShowToast(getText(R.string.Video_Storage_space_err).toString());
					}
				}
				break;
			case R.id.btn_play_gpu://软硬解切换
				if(isGpu){
					SDK.SetDecoderModel(1,SDK._sessionIdContext);
					_btnGpu.setClickable(false);
					i_flag = -1;
					//_decoderQueue.Stop();
					isGpu = false;
					_decoderDebugger.canDecode = false;
					_decoderDebugger.close();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					_btnGpu.setBackgroundResource(R.drawable.gpu_false);
					//_decoderQueue.Start();
					i_flag = 1;
					APP.ShowToast(getText(R.string.Video_stop_gpu).toString());
					_btnGpu.setClickable(true);
				}else{
					i_flag = -1;
					//SDK.SetDecoderModel(0,SDK._sessionIdContext);
					_btnGpu.setClickable(false);
					isGpu = true;
					if(_decoderDebugger != null) _decoderDebugger.canDecode = true;
					stopPause();
					NewSurfaceTest.this.recreate();
				}
				break;
			case R.id.prevday:	
				mDate = prevDay(mDate);
				break;
			case R.id.taday:	
				DateTimePickerDialog date = new DateTimePickerDialog(NewSurfaceTest.instance, System.currentTimeMillis());
				date.show();
				break;
			case R.id.nextday:	
				mDate = nextDay(mDate);
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}
	}
	
	private Calendar prevDay(Calendar mDate){
		Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mDate.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, -1);
        today.setText(DateFormat.format("yyyy-MM-dd", cal));
        return cal;
	}
	
	private Calendar nextDay(Calendar mDate){
		Calendar cal = Calendar.getInstance();
        if(getDay(mDate) >= getDay(cal)){
        	APP.ShowToast("date error!");
        }else{
        	cal.setTimeInMillis(mDate.getTimeInMillis());
            cal.add(Calendar.DAY_OF_YEAR, 1);
            today.setText(DateFormat.format("yyyy-MM-dd", cal));
        }
        return cal;
	}

	private long getDay(Calendar cal){
		return cal.getTimeInMillis()/(1000*60*60*24);
	}
	
	//停止录像
	private void stopRecordingVideo(){
		video.setBackgroundResource(R.drawable.control_icon_small_video_n);
		_isRecording = false;
		SDK.SetFinishVideo(_recordfileName + ".mp4",SDK._sessionIdContext);
		/*if(_decoderDebugger.isCanDecode()){
			_decoderQueue.h264ToMp4();
		}else{
			SDK.SetFinishVideo(_recordfileName + ".mp4");
		}*/
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
			if (_isRecording) {//如果正在录像关闭录像 _decoderQueue != null && _decoderQueue.
				stopRecordingVideo();
			}
			if(_sQueue != null){
				_sQueue.Stop();
				_sQueue = null;
			} 
			if(_decoderDebugger != null){
				_decoderDebugger.close();
				_decoderDebugger = null;
			}
			SDK.isInitDecoder = false;
			if(isPlay){
//				if(!isGpu){
//					SDK.SetDecoderModel(0);
//				}
				if(_paly == 0)
					SDK.P2PCloseChannel(SDK._sessionIdContext, channelNo);
				SDK.P2PClose(SDK._sessionIdContext);
				SDK._sessionIdContext = 0;
				isPlay = false;
			}
		} catch (Exception e) {
			LogUtil.i(TAG, "stopPause.."+ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	//暂停/恢复
	public void playPause(int bPause){
		if(bPause == 0){
			NewSurfaceTest.this.recreate();
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
			LogUtil.i(TAG, "onStop.....start..");
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
			LogUtil.i(TAG, "onDestroy.....start..");
			devSid = "";
			instance = null;
			m_imageData = null;
			ViESurfaceRenderer.bitmap = null;
			//vrenderer = null;
			LogUtil.d(TAG, "2..DrawBitMapDestoryCallback....");
			m_imageBitmap = null;
			m_prevewview = null;
			_decoderDebugger = null;
			_handler = null;
			_sessionId = null;
			_dlgWait = null;
//			_paly = 0;
			LogUtil.i(TAG, "onDestroy.....end..");
		} catch (Exception e) {
			System.out.println(1);
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
					SDK.P2PCloseChannel(SDK._sessionIdContext, channelNo);
					SDK.P2PClose(SDK._sessionIdContext);
					SDK._sessionIdContext = 0;
					NewSurfaceTest.this.finish();
					APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
					break;
				case XMSG.SMS_P2PConnect:
					int ret = (Integer) msg.obj;
					APP.ShowToast(SDK.GetErrorStr(ret));
					break;
				case XMSG.P2PConnect:
					//startTimer(0);
					if(_handler != null) _handler.removeMessages(XMSG.ON_PLAY);
					doPlay(0);
					_runFlag = true;
					_dlgWait.UpdateText(getText(R.string.reopen_stream).toString());
					LogUtil.d(TAG, "收到 打洞消息。...");
					break;
				case 1002:
					doBackPlay(startTime, endTime);
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
				case 0://设备忙
					APP.ShowToast(getText(R.string.Video_Dviece_BUSY).toString());
					stop();
					break;
				case 1://设备忙
					APP.ShowToast(getText(R.string.Video_Dviece_BUSY).toString());
					stop();
					break;
				case XMSG.PLAY_SNAP:
					APP.ShowToast(getText(R.string.Video_snap_success).toString());
					break;
				case XMSG.ON_PLAY:
					if(!SDK.isInitDecoder && _playId > 0 && isPlay){
						APP.ShowToast(getText(R.string.video_failopen).toString());
						LogUtil.d(TAG, "12秒未收到数据调 stop()....");
						stop();
					}
					break;
				case 100:
					APP.ShowToast(getText(R.string.Video_snap_error).toString());//截图失败
					File temFile = new File(_fileName);
					if(temFile.exists()) temFile.delete();
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
						if(efile.exists() && efile.length() >= 51200){
							//_decoderQueue.recordFile(_recordfileName);
							SDK.SetVideoPath(_recordfileName + ".h264",_recordfileName + ".aac",SDK._sessionIdContext);
							_isRecording = true;
							stopNotify();
							closeWait();
							recordCount = 0;
						}
					}else{
						recordCount = 0;
						video.setBackgroundResource(R.drawable.control_icon_small_video_n);
						//_decoderQueue._isRecording = false;
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
				case XMSG.PLAY_AUDIO:
					recordCount ++;
					if(recordCount < 3){
						if(AudioQueue._talkAudio != null){
							stopNotify();
							AudioQueue._talkAudio.play();//音频开始
						}
					}else{
						recordCount = 0;
						stopNotify();
					}
					if (_bNotify) {
						_handler.sendEmptyMessageDelayed(XMSG.PLAY_AUDIO, 400); //延迟发送
					}
					break;
				case XMSG.UPDATE_VIEW:
					drawBitmap();
					break;
				}
			} catch (Exception e) {
				LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}
	
	boolean _bNotify = false;
	private void startNotify(int type) {
		if (!_bNotify) {
			//_handler.sendEmptyMessage(XMSG.GetNotify);
			_handler.sendEmptyMessage(type);
			_bNotify = true;
		}
	}
	private void stopNotify() {
		_bNotify = false;
	}
	
	OnItemClickListener listener = new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			startTime = items.get(arg2).get("start_time").toString();
			endTime = items.get(arg2).get("end_time").toString();
		//	APP.ShowToast(arg2+"|"+startTime+"|"+endTime);
			LogUtil.d(TAG, arg2+"|"+startTime+"|"+endTime);
			_dlgWait.show();
			_dlgWait.UpdateText(getText(R.string.P2PConnect).toString());
			if(isGpu && _paly == 1){//如果当前是硬解，停止并切换图片默认走软解
				SDK.SetDecoderModel(1);
				_btnGpu.setBackgroundResource(R.drawable.gpu_false);
				isGpu = false;
			}
			ViESurfaceRenderer._flag = 0;
			stopPause();
//			SDK.CleanPool();
			_paly = 1;
			ViESurfaceRenderer.bitmap = null;
			_handler.sendEmptyMessage(1002);
		}
	};
	
	public final class VideoBackHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				data = msg.getData();
				LogUtil.d(TAG, "噢拉拉!");
				LogUtil.d(TAG, data.getString("file_list"));
				
				JSONArray array = JSONArray.parseArray(data.getString("file_list"));
				Map<String, Object> maps;
				JSONObject obj = null;
				for(Iterator<Object> iter = array.listIterator(); iter.hasNext();){
					obj = (JSONObject)iter.next();
					//obj.getString("start_time").substring(11);
					
					maps = new HashMap<String, Object>();
					maps.put("tag", "");
					maps.put("image", R.drawable.lock_bg1);
					maps.put("text",obj.getString("start_time").substring(11));
					maps.put("start_time",obj.getString("start_time"));
					maps.put("end_time",obj.getString("stop_time"));
					items.add(maps);
				}
				adapter = new VideoPlaybackSimpleAdapter(context, items, R.layout.gridview_item,
						new String[]{"tag", "image", "text"},
						new int[]{R.id.tag, R.id.ItemImage, R.id.ItemText});
				grid.setAdapter(adapter);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	void init(){
		baseApp.setVideoHandler(videoHandler);
		items.clear();
		//VFMhAQEAADAwMTk0MjBhAAAAAAAA
		//VFMhAQEAADAwMTRjZDk1AAAAAAAA 00201
		//VFMhAQEAAGUwNjFiMjE5NDIwYQAA 楼下的
		//String sid = "VFMhAQEAAGUwNjFiMjE5NDIwYQAA";
		String sid = devSid;
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("type", 1);
		maps.put("action", 106);
		maps.put("channel", channelNo);
		maps.put("sid", sid);
		maps.put("start_time", yyyyMMdd + " 00:00:00");
		maps.put("stop_time", yyyyMMdd + " 23:59:59");
		maps.put("video_type", 0);
		
		String jsonString = new JSONObject(maps).toString();
		LogUtil.d(TAG, "jsonString:" + jsonString);
		int result = SDK.SendJsonPck(0, sid+"|"+jsonString);
		LogUtil.d(TAG, "return value:" + result);
	}

	
}
