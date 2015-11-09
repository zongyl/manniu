package com.views.analog.camera.encode;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.basic.APP;
import com.basic.XMSG;
import com.bean.Device;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.BaseApplication;
import com.views.Main;
import com.views.NewMain;
import com.views.NewSurfaceTest;
import com.views.OnTaskListener;
import com.views.XViewBasic;
import com.views.NewSurfaceTest.decoderThead;
import com.views.analog.camera.audio.AudioQueue;
import com.views.analog.camera.encode.DecoderDebugger;
import com.views.analog.camera.encode.VideoSurfaceView;
import com.views.analog.camera.encode.VideoWndCtrl;
import com.views.bovine.Fun_AnalogVideo;
import com.vss.vssmobile.decoder.H264Dec;
import com.vss.vssmobile.decoder.Mp4Enc;

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
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author: li_jianhua Date: 2015-10-26 上午7:58:15
 * To change this template use File | Settings | File Templates.
 * Description： 这种方法 可以解决联想手机第二次打开崩的问题  横竖屏问题未解决
 */
public class Fun_RealPlay extends XViewBasic implements OnClickWnd,OnTaskListener{
	private String TAG = Fun_RealPlay.class.getSimpleName();
	public static Fun_RealPlay instance = null;
	Context context = null;
	SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    public DecoderDebugger _decoderDebugger = null;//视频硬解码 ok
    public DecoderQueue _decoderQueue;//解码队列
    
    Button play, fullscreen, cut, video,_btnBack,_btnGpu;//back, more,
    TextView _devName;
    
    String devName = "",devSid = "";
    public static int _playId = 0; //播放ID
    public static boolean isPlay = false; //视频播放状态
    private RealHandler _handler = null;
	private BaseApplication mAPP = null;
	FrameLayout _hreadframeLayout;
	//宽高自适应......
	private int[] pixels;
    LinearLayout.LayoutParams params;
    FrameLayout framelayout;
    int _width, _height;
    //.............
    
    /** 视频解码器 */
   	long m_decoder = -1;
   	Lock m_decoderLock = new ReentrantLock();
   	//MP4录像
   	//long _mp4Enc = -1;
   	private final int MAX_IMG_BUFFER_SIZE = 2560 * 1600 * 3;
   	public Rect m_rect = null;
   	/** Image数据 */
   	byte[] m_imageData = null;
   	/** 绘图源数据 */
   	Bitmap m_imageBitmap = null;
   	/** 视频解码器   end */
    VideoWndCtrl _videWnd = null;	// 视频显示对象
    public boolean _snapImg = false;//软解截图
    public String _fileName = "";//截图文件名
	public String _recordfileName = "";//录像文件名
	private boolean isGpu = false;//软硬切换
	public Fun_RealPlay(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		instance = this;
		context = activity.getApplicationContext();
			
		// 生成视频显示对象,并设置相关属性，添加到此View
		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.SurfaceViewPlay1);
		_videWnd = new VideoWndCtrl(ACT);		
		LinearLayout.LayoutParams LP_FF = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		_videWnd.setLayoutParams(LP_FF);
		layout.addView(_videWnd);
		_videWnd.setVisibility(View.VISIBLE);
		_videWnd.SetOnWndListener(this);
		
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.hhheader);
		framelayout = (FrameLayout)findViewById(R.id.frame);
		params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
		
		_devName = (TextView)findViewById(R.id.dev_name);
		cut = (Button)findViewById(R.id.btn_play_cut);
		video = (Button)findViewById(R.id.btn_play_video);
		_btnGpu = (Button) findViewById(R.id.btn_play_gpu);
		
		// 监听按钮事件
		int btns[] = {R.id.btn_play_play,R.id.btn_play_cut, R.id.btn_play_video,R.id.btn_play_gpu,R.id.btn_play_fullscreen};
		APP.ListenViews(_root, btns, this);
				
		params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
		_btnBack = (Button) this.findViewById(R.id.btn_back_video);
		_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		
		// 解码器
		m_decoder = H264Dec.InitDecoder();
		if(m_imageData == null) m_imageData = new byte[MAX_IMG_BUFFER_SIZE];
		mAPP = (BaseApplication)activity.getApplication();
		_handler = new RealHandler();
		// 设置共享变量
		mAPP.setRedlandler(_handler);
		pixels = getSize();
 		layout();
	}
	
	int _curIndex;					// 当前窗口Index
	// 3.接受VideoWndCtrl视频窗口点击事件
	@Override
	public void OnClickWnd(VideoSurfaceView video, boolean bDouble,int index) {
		_curIndex = index;
		if (bDouble) {
			return;
		}
		if (video.IsPlay()) {
			
		} else {
			
		}
	}
	@Override
	protected void onClick(int id) {
		switch (id) {
		case R.id.btn_play_play:
			stop();
			break;
		case R.id.btn_play_fullscreen:
			orientation();
			break;
		case R.id.btn_play_cut:
			_fileName = getFileName();
			_decoderQueue._startSnap = true;
			_snapImg = true;
			
			break;
		case R.id.btn_play_video:
			// 如果正在录像，则停止
			if (_decoderQueue._isRecording && isPlay) {
				APP.ShowToast(APP.GetString(R.string.Video_record_end));
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
//					_dlgWait.show();
//					_dlgWait.UpdateText(getText(R.string.set_pwd).toString());
					startNotify();
				}else{
					APP.ShowToast(APP.GetString(R.string.Video_Storage_space_err));
				}
			}
			break;
		case R.id.btn_play_gpu://软硬解切换
			_decoderQueue.i_flag = 0;
			if(isGpu){
				_decoderQueue.Stop();
				isGpu = false;
				_decoderDebugger.canDecode = false;
				_decoderDebugger.close();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//关闭解码器
//				m_surfaceHolder.release();
				_btnGpu.setBackgroundResource(R.drawable.gpu_false);
				APP.ShowToast(APP.GetString(R.string.Video_stop_gpu));
				_decoderQueue.Start();
			}else{
				isGpu = true;
				_decoderDebugger.canDecode = true;
//				stopPause();
//				NewSurfaceTest.this.recreate();
				
			}
			break;
		}
	}
	//停止录像
	private void stopRecordingVideo(){
		video.setBackgroundResource(R.drawable.control_icon_small_video_n);
		_decoderQueue._isRecording = false;
		if(NewMain.devType == 1){
			Mp4Enc.stop(Mp4Enc.handle);
		}else{
			_decoderQueue.h264ToMp4();
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
	
	//................
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		APP.GetMainActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
    private void layout(){
		if(pixels[0] > pixels[1]){
			APP.GetMainActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			//show(footer);
			params.height = pixels[1];
			params.width = pixels[0];
			framelayout.setLayoutParams(params);
		}else{
			//show(footer);
			params.height = pixels[1]/2;
			params.width = pixels[0];
			framelayout.setLayoutParams(params);
		}
	}
    //................
    
    
    @Override
   	protected void OnVisibility(int visibility) {
   		super.OnVisibility(visibility);
   		_videWnd.setVisibility(visibility == View.VISIBLE ? View.VISIBLE : View.GONE);
		_videWnd.OnVisibility(visibility); //-->关联_videWnd 初始化入口
   		if (visibility == View.VISIBLE) {
   			//initSV();
   		// 生成视频显示对象,并设置相关属性，添加到此View
//		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.SurfaceViewPlay1);
//		LinearLayout.LayoutParams LP_FF = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//		vsf = new MyView(context);
//		layout.addView(vsf);
   		}
   	}
    
//    public void initSV(){
//    	m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay1);
//		m_surfaceHolder = m_prevewview.getHolder(); 
//		m_surfaceHolder.addCallback((Callback) this);	
//    }
    
    public void initSuface(Surface _surface){
    	if(_decoderDebugger == null){
			_decoderDebugger = new DecoderDebugger(_surface,context);
		}
    	m_surfaceHolder = _videWnd.GetSelected().getHolder();
		_handler.sendEmptyMessage(XMSG.P2PConnect);
    }
    
    
    
    
    
    private int screenCount = 0;
	Canvas videoCanvas = null;
	//软解码方法
	public synchronized void h264Decoder(byte[] data,int len){
		try {
			if(!_decoderDebugger.canDecode){
				int[] frameParam = new int[4];
				byte[] bmpBuff = null;
				ByteBuffer bytBuffer = null;
				Bitmap bmp = null;

				long handle = m_decoder;
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
				if(_snapImg){
					_decoderQueue._startSnap = false;
					_snapImg = false;
					//_handler.sendEmptyMessage(100);
					long fileLong = snapPic(m_imageBitmap, _fileName);
					if(fileLong >= 204800){// || NewMain.devType == 4
						_decoderQueue._startSnap = false;
						_snapImg = false;
					}
				}
			}
		} catch (Exception e) {
		}
	}
    
    
	//截图功能
	@SuppressLint("SimpleDateFormat")
	public long snapPic(Bitmap bitmap,String fileName) {
		BitmapUtils.saveBitmap(bitmap, fileName);
		File file = new File(fileName);
		if(file.isFile() && file.exists() && file.length() >= 204800){
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
		if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
	}
    
	
	long[] _sessionId = new long[1];
	//type 0-初始化要开 1-断线打开
	public synchronized void doPlay(int type){
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
					//模拟目前不要发消息
					if(NewMain.devType ==1)
						_handler.sendEmptyMessageDelayed(XMSG.ON_PLAY,12000);//12秒收不到数据 提示打开视频失败!
					_decoderDebugger.flag = 0;
					stopTimer();
					if(type == 0){
						isPlay = true;
						if(_decoderQueue == null){
							_decoderQueue = new DecoderQueue();
							_decoderQueue.Start();
						}
					}else if(type == 1){
					}
				}else{
					Message msg = new Message();
					msg.what = XMSG.CREATECHANLL;
					msg.obj = _playId;
					_handler.sendMessage(msg);
					closeWait();
					SDK._sessionId = 0;
				}
				
			}else{//连接失败消息处理
				SDK.SendJsonPck(1,SDK.getJsonString(devSid));
				closeWait();
				Message msg = new Message();
				msg.what = XMSG.SMS_P2PConnect;
				msg.obj = nRet;
				if(_handler != null) _handler.sendMessage(msg);
				SDK._sessionId = 0;
				_runFlag = false;
				APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
			}
		}catch(Exception e){
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	boolean isStop = true;//stop 标志位
	//boolean isCloseChannel = true;//是否关闭通道
	public void stop(){
		if(isStop){
			try {
				stopTimer();
				isStop = false;
				long t3= System.currentTimeMillis();
//				if (_decoderQueue != null && _decoderQueue._isRecording) {//如果正在录像关闭录像
//					stopRecordingVideo();
//				}
				
//				_sQueue.Stop();
//				_deThead.de_stop();
				if(_decoderQueue != null){
					_decoderQueue.Stop();
					_decoderQueue = null;
				}
				if(_decoderDebugger != null){
					if(_decoderDebugger.isCanDecode()) _decoderDebugger.close();
					_decoderDebugger = null;
				}
				
				H264Dec.UninitDecoder(m_decoder);
				//Mp4Enc.ReleaseInstance(_mp4Enc);
				long t4= System.currentTimeMillis();
				LogUtil.d(TAG, " 退出app .time "+(t4-t3));
				SDK.isInitDecoder = false;
				
				if(isPlay){
					long t1= System.currentTimeMillis();
					SDK.P2PCloseChannel(SDK._sessionId,0);
					SDK.P2PClose(SDK._sessionId);
					SDK._sessionId = 0;
					isPlay = false;
					long t2= System.currentTimeMillis();
					LogUtil.d(TAG, " 退出SDK.time "+(t2-t1));
				}
				APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
				isStop = true;
				m_prevewview = null;
				m_surfaceHolder = null;
			} catch (Exception e) {
				LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}
	
	public void closeWait(){
		//if(_dlgWait.isShowing()) _dlgWait.dismiss();
	}
	public void showGpu(){
		//判断一下 如果支持硬解把图片换一下
		if(_decoderDebugger.isCanDecode()){
			_handler.sendEmptyMessage(XMSG.PLAY_GPU_OK);
		}else{
			_handler.sendEmptyMessage(XMSG.PLAY_GPU);
		}
	}
	
	
	@Override
	public void OnMessage(Message msg) {
		switch (msg.what) {
		case XMSG.PLAY:	//视频播放
			/*// 发送消息，切换到实时视频界面
			APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_REALPLAY);
			// 开始异步功能操作 （正在打开视频）
			//APP.ShowWaitDlg(this, R.string.openning_stream, 0, msg.obj);
			Device device = (Device) msg.obj;
			devName = device.devname;
			devSid = device.sid;
			doPlay(0);*/
			break;
		case XMSG.ON_PLAY:
			System.out.println(1111);
			break;
		case XMSG.CROSS_SCREEN:
			System.out.println(111);
//			if(_isCross){
//				setFullScreen(true);
//				_isCross = false;
//				_isVertical = true;
//			}
			break;
		case XMSG.VERTICAL_SCREEN:
			System.out.println(22);
//			if(_isVertical){
//				setFullScreen(false);
//				_isVertical = false;
//				_isFillShow = false;
//				_isCross = true;
//			}
			break;
		}
	}
	
	
	/**
	 * 自己实现 Handler 处理消息更新UI
	 * @author mark
	 */
	private int recordCount = 0;
	@SuppressLint("HandlerLeak")
	public class RealHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case XMSG.PLAY:
				//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_REALPLAY);
				NewMain._isOpen = true;//打开视频标志位
				// 开始异步功能操作 （正在打开视频）
				APP.ShowWaitDlg(Fun_RealPlay.this, R.string.openning_stream, 0, msg.obj);
				
				break;
			case XMSG.P2PConnect:
				startTimer(0);
				_runFlag = true;
				break;
			case -5000:
				APP.ShowToast(APP.GetMainActivity().getText(R.string.Video_NetWork_Err).toString());
				if(isPlay){
					//openWait();
					startTimer(1);
					_runFlag = true;
				}
				break;
			case 1://设备忙
				APP.ShowToast(APP.GetString(R.string.Video_Dviece_BUSY));
				stop();
				break;
			case XMSG.ON_PLAY:
				if(!SDK.isInitDecoder && _playId > 0 && isPlay){
					APP.ShowToast(APP.GetString(R.string.video_failopen).toString());
					stop();
				}
				break;
			case XMSG.PLAY_SNAP:
				APP.ShowToast(APP.GetString(R.string.Video_snap_success).toString());
				break;
			case 100:
				APP.ShowToast("截图失败!");
				//btnPhotoFileClick();
				break;
			case XMSG.PLAY_GPU:
				isGpu = false;
				_btnGpu.setBackgroundResource(R.drawable.gpu_false);
				APP.ShowToast(APP.GetString(R.string.Video_err_gpu).toString());
				_btnGpu.setClickable(false);
				break;
			case XMSG.PLAY_GPU_OK:
				isGpu = true;
				_btnGpu.setBackgroundResource(R.drawable.gpu_true);
				break;
			case XMSG.GetNotify://定时判断截图成功
				recordCount ++;
				File efile = new File(_recordfileName + ".bmp");
				if(recordCount < 5){
					if(efile.exists()){
						_decoderQueue.recordFile(_recordfileName);
						stopNotify();
						closeWait();
						recordCount = 0;
					}
				}else{
					recordCount = 0;
					video.setBackgroundResource(R.drawable.control_icon_small_video_n);
					_decoderQueue._isRecording = false;
					efile.delete();
					stopNotify();
					closeWait();
					APP.ShowToast(APP.GetString(R.string.Video_record_error).toString());
					break;
				}
				
				if (_bNotify) {
					_handler.sendEmptyMessageDelayed(XMSG.GetNotify, 500); //延迟发送
				}
				break;
			case XMSG.PLAY_CLOSE_WAIT:
				showGpu();
				break;
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
	

	@Override
	public Object OnDoInBackground(int what, int arg1, int arg2, Object obj) {
		try {
			switch (what) {
			case 0: {	// 打开视频
				Device device = (Device) obj;
				devName = device.devname;
				devSid = device.sid;
				_videWnd.GetSelected().Start();
				_devName.setText(devName);
			}
			case 1: {	// 关闭视频
				break;
			}
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return -1;
	}



	@Override
	public int OnPostExecute(int what, int arg1, int arg2, Object obj,
			Object ret) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,
//			int height) {
//		// TODO Auto-generated method stub
//		System.out.println(22);
//	}
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		System.out.println(111111);
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		System.out.println(33);
//	}
	
	
	
	/*//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case XMSG.CREATECHANLL:
					break;
				case XMSG.SMS_P2PConnect:
					int ret = (Integer) msg.obj;
					APP.ShowToast(SDK.GetErrorStr(ret));
					break;
				case XMSG.P2PConnect:
					doPlay(0);
					break;
				}
			} catch (Exception e) {
			}
		}
	}*/

}
