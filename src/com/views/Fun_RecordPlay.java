package com.views;

import java.util.TimerTask;
import P2P.SDK;
import P2P.ViESurfaceRenderer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.adapter.MsgAdapter2;
import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import android.view.SurfaceHolder.Callback;

/**
 * @author: li_jianhua Date: 2015-11-27 下午5:05:41
 * To change this template use File | Settings | File Templates.
 * Description： 报警录像回放
 */
public class Fun_RecordPlay extends Activity implements SurfaceHolder.Callback,OnClickListener{
	private String TAG = Fun_RecordPlay.class.getSimpleName();
	public static Fun_RecordPlay instance = null;
	SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    TextView _devName;
    public String devName = "",evt_video ="";
    public int evt_vsize = 0;
    public int evt_ManufacturerType = 0;//设备厂家类型
    Button _btnBack;
    //XImageBtn _btnpause,_btnstop;
    // 是否手动拖动播放条标志位
 	private boolean isChanging = false;
    private SeekBar seekbar;// 进度条
    
    //宽高自适应......
  	private int[] pixels;
  	LinearLayout.LayoutParams params;
  	FrameLayout framelayout;
  	int _width, _height;
  	//.............
  	RelativeLayout _layout;//显示隐藏工具条
    Handler handler = new Handler();
    FrameLayout _hreadframeLayout;//标题栏
  	
    //public InputStream is = null;
    private RecordHandler _handlerRecord = null;
    
    private boolean isPause = true;//是否暂停
    private Dlg_WaitForActivity _dlgWait = null;
    public long _alarmContext;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		//getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE); 
		setContentView(R.layout.fun_recordplay);
		//VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE); //设置最小heap内存为6MB大小。
		instance = this;
//		devName = getIntent().getExtras().getString("deviceName");//设备名称
//		evt_video = getIntent().getExtras().getString("evt_video");
//		evt_vsize = getIntent().getExtras().getInt("evt_vsize");
//		evt_ManufacturerType = getIntent().getExtras().getInt("evt_ManufacturerType");
		
		devName = Constants.devName;//设备名称
		evt_video = Constants.evt_video;
		evt_vsize = Constants.evt_vsize;
		evt_ManufacturerType = Constants.evt_ManufacturerType;
		
		framelayout = (FrameLayout)findViewById(R.id.frame);
        params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
        _layout = (RelativeLayout) this.findViewById(R.id.record_footer);
		m_prevewview = (SurfaceView) findViewById(R.id.record_video);
		m_surfaceHolder = m_prevewview.getHolder(); 
		m_surfaceHolder.addCallback((Callback) this);	
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		m_prevewview.setOnClickListener(this);
		
		_devName = (TextView)findViewById(R.id.dev_name);
		_devName.setText(devName);
		_btnBack = (Button) this.findViewById(R.id.btn_back_video);
		_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(BackLoginThread.state != 200){
					APP.ShowToast(getText(R.string.downloading).toString());
					return;
				}
				stop();
			}
		});
		seekbar = (SeekBar) findViewById(R.id.seekBar1);
		seekbar.setEnabled(false);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				isChanging = false;
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				isChanging = true;
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser == true && BackLoginThread.state == 200) {
					SDK.CleanPool(_alarmContext);
					seekbar.setEnabled(false);
					isRange = true;
					byteLength = progress;
					thresholdpricelen = progress;
					_dlgWait.show();
					_dlgWait.UpdateText(getText(R.string.openning_stream).toString());
					_handlerRecord.sendEmptyMessageDelayed(XMSG.P2PConnect,100);
				}
			}
		});
		
		pixels = getSize();
 		layout();
 		
 		_handlerRecord = new RecordHandler();
		ViESurfaceRenderer.ResetSurfaceHolder(this);
 		show(_layout);
 		if(_dlgWait == null){
			_dlgWait = new Dlg_WaitForActivity(this,R.style.dialog);
			_dlgWait.setCancelable(true);
		}
 		_dlgWait.show();
		_dlgWait.UpdateText(getText(R.string.openning_stream).toString());
		LogUtil.d(TAG, "0.._dlgWait.UpdateTex....");
 		MsgAdapter2._isOpenAlarm = true;
//		RealAlarmListAdapter._isOpenAlarm = true;
 		APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MORE);
		LogUtil.d(TAG, "1..new ViESurfaceRenderer....");
	}
	
//	public void touch(File f) {
//		try {
//			if (!f.exists())
//				f.createNewFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	private float dstRightScale = 1;
	private float dstBottomScale = 1;
	private void changeDestRect(int dstWidth, int dstHeight) {
		ViESurfaceRenderer.dstRect.right = (int)(ViESurfaceRenderer.dstRect.left + dstRightScale * dstWidth);
		ViESurfaceRenderer.dstRect.bottom = (int)(ViESurfaceRenderer.dstRect.top + dstBottomScale * dstHeight);
    }
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int format,int width,
			int height) {
//		try {
//			_width = width;
//			_height = height;
//			m_rect = new Rect(0, 0, _width, _height);
//		} catch (Exception e) {
//		}
		changeDestRect(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
//		if(_decoderDebugger == null){
//			_decoderDebugger = new DecoderDebugger(holder.getSurface(),Fun_RecordPlay.this);
//		}
		_alarmContext = SDK.CurlSet();
		LogUtil.d(TAG, "create  _alarmContext = "+_alarmContext);
		_handlerRecord.sendEmptyMessage(XMSG.P2PConnect);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}

	private void show(View v){
		if(!v.isShown()){
			v.setVisibility(View.VISIBLE);
			handler.postDelayed(dis, 3000);
		}
	}
	
	private void closeWait(){
		if(_dlgWait.isShowing() && _dlgWait != null) _dlgWait.dismiss();
	}
	
	public void closeFirst(){
		_handlerRecord.sendEmptyMessage(1001);//关闭等待框.打开进度条
	}
	/**
	 * 隐藏
	 */
	Runnable dis = new Runnable(){
		@Override
		public void run() {
			showOrHide(_layout);
		}
	};
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
	
	//................
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
    private void layout(){
		if(pixels[0] > pixels[1]){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
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
    
    public void setBitmap() {
    	_handlerRecord.sendEmptyMessage(XMSG.UPDATE_VIEW);
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
    
    @Override 
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
			if(BackLoginThread.state != 200){
				APP.ShowToast(getText(R.string.downloading).toString());
				return false;
			}
			stop();
		}
		return true;
	}
    public boolean isClose = false;//是否关闭
    public void stop(){
    	try {
    		if(!isClose){
    			LogUtil.d(TAG, "2..stop start...."+" _alarmContext = "+_alarmContext);
    			isClose = true;
        		SDK.CurldownloadFinish(_alarmContext);
        		//LogUtil.d(TAG, "2..stop start..1.1..");
        		stopTimer();
        		seekbar.setProgress(0);
    			Fun_RecordPlay.this.finish();
        		APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_MAIN);
    			//System.gc();
    			LogUtil.d(TAG, "2..stop end....");
    		}
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
    }
	
    public int byteLength = 0,thresholdpricelen = 0;//读取字节长度..进度条的长度/  加载文件的长度
    boolean isRange = false;//是否从指定的地方加载
    public int flag  = 0;//第一次打开视频关闭弹出框
	public void doPlay2(){
		//每次打开定时器之前清空消息队列
		if(_handlerRecord != null) _handlerRecord.removeMessages(XMSG.ON_PLAY);
		if(evt_vsize > 0){
			startTimer();
			seekbar.setMax(evt_vsize);//总长度
			_handlerRecord.sendEmptyMessageDelayed(XMSG.ON_PLAY,12000);//12秒收不到数据 提示打开视频失败!
		}
		if(isRange){
			_handlerRecord.sendEmptyMessage(1001);
		}
		BackLoginThread.state = 203;
    	Main.Instance._loginThead.start();
//		long t1 = System.currentTimeMillis();
//		LogUtil.d(TAG,"SDK.CurlSetOperation.1.1...");
//		SDK.CurlSetOperation(evt_video,evt_vsize,evt_ManufacturerType,thresholdpricelen);
//		long t2 = System.currentTimeMillis();
//		LogUtil.d(TAG,"SDK.CurlSetOperation..1.2.."+(t2-t1));
	}
	
	//更新进度条
	public static java.util.Timer _timer = null;
	public void startTimer() {
		try {
			if (_timer != null) {
				_timer.cancel();
				_timer = null;
			}
			if (_timer == null) {
				_timer = new java.util.Timer();
			}
			_timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if (isChanging == true) {
							return;
						}
						seekbar.setProgress(byteLength);
					} catch (Exception e) {
					}
				}
			}, 0, 10);
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}

	void stopTimer() {
		if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
	}
	
	private boolean isResume = false;//是否主页
	//暂停/恢复
	public void playPause(int bPause){
		if(bPause == 0){
		}
		if(bPause == 1 && !isClose){
			stop();
			isResume = true;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(isResume){
			playPause(0);
			isResume = false;
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		playPause(1);
	}
	
	@Override
	protected void onStop() {
		try {
			LogUtil.i(TAG, "onStop.....start..");
			closeWait();
			_handlerRecord.removeMessages(XMSG.ON_PLAY);
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
			m_prevewview = null;
//			m_surfaceHolder = null;
			if(ViESurfaceRenderer.bitmap != null && !ViESurfaceRenderer.bitmap.isRecycled()){
				ViESurfaceRenderer.bitmap.recycle();
				ViESurfaceRenderer.bitmap = null;
			}
//			vrenderer.DrawBitMapDestoryCallback();
			//ViESurfaceRenderer.DestroySurfaceHolder();
			LogUtil.d(TAG, "2..DrawBitMapDestoryCallback....");
			_handlerRecord = null;
			_dlgWait = null;
			instance = null;
			LogUtil.i(TAG, "onDestroy.....end..");
		} catch (Exception e) {
		}
    }
		
	//2.接受消息
	@SuppressLint("HandlerLeak")
	public class RecordHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case XMSG.P2PConnect:
					new Thread(new Runnable() {
						@Override
						public void run() {
							//doPlay();
							doPlay2();
						}
					}).start();
					//doPlay2();
					break;
				case 1001:
    				seekbar.setEnabled(true);
    				closeWait();
    				isChanging = false;
					break;
				case XMSG.ON_PLAY:
					if(_dlgWait.isShowing() && _dlgWait != null){
						APP.ShowToast(getText(R.string.video_failopen).toString());
						LogUtil.d(TAG, "12秒未收到数据调 stop()....");
						stop();
					}
					break;
				case XMSG.UPDATE_VIEW:
					drawBitmap();
//					s_threads.execute(_sound);
					break;
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.record_video:
				show(_layout);
				break;
			case R.id.btn_play:
				if (isPause) {
					isPause = false;
				} else {
					isPause = true;
				}
				//OnSelectChange(isPause);
				break;
			case R.id.btnStopVideo:
				stop();
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}
	}
	
	

}
