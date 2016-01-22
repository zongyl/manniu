package com.views;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
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
import com.adapter.HttpUtil;
import com.basic.G;
import com.basic.XMSG;
import com.ctrl.XImageBtn;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.analog.camera.encode.DecoderDebugger;

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
    String devName = "",evt_video ="";
    int evt_vsize = 0;
    int evt_ManufacturerType = 0;//设备厂家类型
    Button _btnBack;
    XImageBtn _btnpause,_btnstop;
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
  	
  	
    public InputStream is = null;
    
    public DecoderDebugger _decoderDebugger = null;//视频硬解码 ok
    private MyHandler _handler = null;
    public decoderThead _deThead = null;//软解码线程
    
    private boolean isPause = true;//是否暂停
    private Dlg_WaitForActivity _dlgWait = null;
//    private BufferedOutputStream outputStream;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		setContentView(R.layout.fun_recordplay);
		instance = this;
		devName = getIntent().getExtras().getString("deviceName");//设备名称
		evt_video = getIntent().getExtras().getString("evt_video");
		evt_vsize = getIntent().getExtras().getInt("evt_vsize");
		evt_ManufacturerType = getIntent().getExtras().getInt("evt_ManufacturerType");
		
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
				stop();
			}
		});
		_btnstop = (XImageBtn) this.findViewById(R.id.btnStopVideo);
		_btnpause = (XImageBtn) this.findViewById(R.id.btn_play);
		_btnstop.setOnClickListener(this);
		_btnpause.setOnClickListener(this);
		
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
				if (fromUser == true) {
					seekbar.setEnabled(false);
					isRange = true;
					byteLength = progress;
					if(is != null){
						try {
							//long t1 = System.currentTimeMillis();
							is = null;
							//is.close();
							//long t2 = System.currentTimeMillis();
							//LogUtil.d(TAG, "is.close time="+(t2-t1));
							_dlgWait.show();
							_dlgWait.UpdateText(getText(R.string.openning_stream).toString());
							_handler.sendEmptyMessageDelayed(XMSG.P2PConnect,100);
						} catch (Exception e) {
						}
					}
				}
			}
		});
		
		pixels = getSize();
 		layout();
 		_handler = new MyHandler();
 		show(_layout);
 		if(_dlgWait == null){
			_dlgWait = new Dlg_WaitForActivity(this,R.style.dialog);
			_dlgWait.setCancelable(true);
		}
//		File f = new File(Environment.getExternalStorageDirectory(), "/IPC_recordplay.rbg");
//	    touch (f);
//	    try {
//	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
//	    } catch (Exception e){ 
//	        e.printStackTrace();
//	    }
	}
	
//	public void touch(File f) {
//		try {
//			if (!f.exists())
//				f.createNewFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int format,int width,
			int height) {
		try {
			_width = width;
			_height = height;
			m_rect = new Rect(0, 0, _width, _height);
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
//		if(_decoderDebugger == null){
//			_decoderDebugger = new DecoderDebugger(holder.getSurface(),Fun_RecordPlay.this);
//		}
		_dlgWait.show();
		_dlgWait.UpdateText(getText(R.string.openning_stream).toString());
		_handler.sendEmptyMessage(XMSG.P2PConnect);
		_deThead = new decoderThead();
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
		if(_dlgWait.isShowing()) _dlgWait.dismiss();
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
    
    @Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
			stop();
		}
		return true;
	}
    
    public void stop(){
    	try {
//    		if(is != null){
//        		is.close();
//        		is = null;
//        	}
    		is = null;
    		stopTimer();
    		_deThead.de_stop();
    		seekbar.setProgress(0);
    		finish();
			System.gc();
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
    }
	
    int temp=0;
    int n = 0;
    byte[] b =new byte[1024];
    byte[] bmpBuff = null;
    int flag  = 0,hk_flag = 0;
    int byteLength = 0;//读取字节长度
    //int updateSeekbar = 0;//10帧更新一次
    boolean isRange = false;//是否从指定的地方加载
	public void doPlay(){
		try {
			//SDK.CurlSetOperation(evt_video, evt_vsize, 0);
			long t3 = System.currentTimeMillis();
			is = HttpUtil.getFileInputStream(evt_video,isRange,byteLength,evt_vsize);
			long t4 = System.currentTimeMillis();
			LogUtil.d(TAG, "HttpUtil.getFileInputStream time="+(t4-t3));
			if(isRange){
				_handler.sendEmptyMessage(1001);
			}
			byte[] deleByte;
	    	int type = 0,ext_len=0;
	    	int naluLength = 0;
	        byte buffer[];
	        boolean isEnd=false;
	        while (true) {
	        	type=0;
	        	ext_len=0;
	        	naluLength=0;
	        	video_width = 0;
	        	video_height = 0;
	        	if(is == null) break;
	        	if(!isPause) continue;
	        	if (evt_ManufacturerType == 1) { //智诺
	        		while ((temp = is.read()) != 'Z'){
		        		if (temp == -1){
		        			isEnd=true;
		        			break;
		        		}
		        	}
		        	if(isEnd){
		        		stop();
		        	}
		        	buffer = new byte[4];
		    		is.read(buffer,0,3);
		    		byteLength += 4;
		    		if (buffer[0] == 'L' && buffer[1] == 'A' && buffer[2] == 'V'){
		    			//System.out.println(new String(buffer,0,3)); 
		        		//1.取头
		        		fill(header,0,20);
		        		byteLength += 20;
		        		type = header[0];
		        		ext_len=header[18];
		        		
		        		naluLength = G.byteToInt(header,8);
		        		
		        		int exHead = (int)header[18];
		        		int realHead = 24 + exHead;
		        		int realLen = naluLength - realHead - 8;
		        		//System.out.println("realLen=:"+realLen+":"+naluLength);
		        		
		        		if(header[0] == -3){
		        			if(ext_len > 0){//ext_len>0时读取b,0,ext_len丢弃
		        				deleByte = new byte[ext_len];
		        				fill(deleByte,0,ext_len);
		        				byteLength += ext_len;
		        				if(deleByte[0]  == -126){
		        					video_width = G.byteToShort(deleByte,4);
		        					video_height = G.byteToShort(deleByte,6);
		        				}
		        				if (deleByte[8] == -127){
		        					freamerate = deleByte[11];
		        				}
		        			}
		        		}
		        		
		        		//2.取数据送解码器
		        		fill(_myBuffer,0,realLen);
		        		byteLength += realLen;
		            	deleByte = new byte[8];
		        		fill(deleByte,0,8);//读到尾部8个字节
		        		byteLength += 8;
		        		if(freamerate > 0){
		        			//System.out.println("byteLength="+byteLength);
		        			if(flag == 0){
		        				flag = 1;
		        				SDK.Ffmpegh264DecoderInit(video_width,video_height,freamerate,0);
		        				bmpBuff = new byte[video_width * video_height * 2];
		        				img_width = video_width;
		        				img_height = video_height;
		        				System.out.println("evt_vsize=="+evt_vsize);
		        				if(evt_vsize > 0){
		        					startTimer();
			        				seekbar.setMax(evt_vsize);//总长度
		        				}
		        				_handler.sendEmptyMessage(1001);
		        				_deThead.de_start();
		        			}
		        			
		        			long t1 = System.currentTimeMillis();
		        			int ret = SDK.AlarmDataPlayBack(_myBuffer,realLen,bmpBuff);
		        			long t2 = System.currentTimeMillis();
		        			//LogUtil.d(TAG, "AlarmDataPlayBack time="+(t2-t1)+" ret:"+ret);
		        			if(ret > 0){
		        				_deThead.addData(bmpBuff);
		        			}
		        			if((t2-t1) < 100){
								Thread.sleep(100-(t2-t1));
							}
		        			//h264Decoder(bmpBuff);
		        			//硬解OK
	        				//_decoderDebugger.decoder(_myBuffer, realLen);
		        		}
		    		}
	        	}else if(evt_ManufacturerType == 2){ //海康
	        		if(flag == 0){
        				flag = 1;
        				buffer = new byte[40];
    		    		is.read(buffer,0,39);
    		    		byteLength += 40;
    		    		int iret = SDK.InputHiKangData(buffer, 40);
    		    		if(iret != 0) break;
	        		}
	        		
	        		int a = fill(b,0,b.length);
        			if(a == -1){
        				byteLength = evt_vsize;
        				isEnd=true;
        				stop();
	        			break;
        			}else{
        				int m = SDK.InputHiKangData(b, a);
    		    		if(m != 0) break;
        			}
        			byteLength += a;
        			//System.out.println("java:"+byteLength+" size:"+evt_vsize+" a:"+a);
    				byte[] data = new byte[1024*1024];
		    		int[] info = new int[3];
		    		while(true){
		    			int ret = SDK.GetHiKangData(data,info);
		    			if(ret == 0){
		    				if(hk_flag == 0){
		    					hk_flag = 1;
		    					img_width = info[1];
		        				img_height = info[2];
		    					bmpBuff = new byte[info[1] * info[2] * 2];
		    					SDK.Ffmpegh264DecoderInit(info[1],info[2],0,0);
		    					if(evt_vsize > 0){
		    						startTimer();
			        				seekbar.setMax(evt_vsize);//总长度
		    					}
		        				_handler.sendEmptyMessage(1001);
		        				_deThead.de_start();
		    				}
		    				long t1 = System.currentTimeMillis();
		    				int ret2 = SDK.AlarmDataPlayBack(data,info[0],bmpBuff);
		    				long t2 = System.currentTimeMillis();
		    				if(ret2 > 0){
		        				_deThead.addData(bmpBuff);
		        			}
		    				if((t2-t1) < 100){
								Thread.sleep(100-(t2-t1));
							}
		    			}else{
		    				break;
		    			}
		    		}
	        	}
			}
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	int video_width =0,video_height=0,freamerate=0;
	int img_width = 0,img_height = 0;
	byte[] _myBuffer = new byte[750*1024]; 
	byte[] header = new byte[20];
	private int fill(byte[] buffer, int offset,int length){
		int sum = 0, len;
		try {
			while (sum<length) {
				len = is.read(buffer, offset+sum, length-sum);
				if (len<0) {
					//throw new IOException("fill...End of stream");
					return -1;
				}
				else sum+=len;
			}
		} catch (Exception e) {
		}
		return sum;
	}
	
	Canvas videoCanvas = null;
	public Rect m_rect = null;
	//软解码方法
	public synchronized void h264Decoder(byte[] outBytes){
		try {
//			if(!_decoderDebugger.canDecode){
				Bitmap bmp = null;
				byte[] bmpBuff = null;
				ByteBuffer bytBuffer = null;
				int width_frame = img_width;//352
				int height_frame = img_height;//288
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
						//m_imageBitmap = bmp;//软解截图时用这个
						if (videoCanvas != null) {
							m_surfaceHolder.unlockCanvasAndPost(videoCanvas);
							videoCanvas = null;
						}
					}
				}
				if (!bmp.isRecycled()) {
					bmp.recycle();
				}
//			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
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
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(is != null){
			stop();
		}
	}
	
	@Override
    protected void onDestroy(){
		super.onDestroy();
		try {
			SDK.Ffmpegh264DecoderUninit();
			m_prevewview = null;
			m_surfaceHolder = null;
			_decoderDebugger = null;
			_handler = null;
			_dlgWait = null;
			instance = null;
			LogUtil.i(TAG, "onDestroy.....end..");
		} catch (Exception e) {
		}
    }
		
	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case XMSG.P2PConnect:
					new Thread(new Runnable() {
						@Override
						public void run() {
							doPlay();
						}
					}).start();
					
					break;
				case 1001:
    				seekbar.setEnabled(true);
    				closeWait();
    				isChanging = false;
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
				OnSelectChange(isPause);
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
	
	// 状态更新
	void OnSelectChange(boolean isPause) {
		if (isPause == false) {
			_btnpause.SetImages(R.drawable.pause, R.drawable.pause);
		}else{
			_btnpause.SetImages(R.drawable.play, R.drawable.play);
		}
	}
	
	
	//解码队列线程
	public class decoderThead implements Runnable{
		private boolean runFlag;
		Thread _sthread = null;
		public final static int MAX_SIZE = 50;
		public int i_flag = 0;//I帧标志位
		private Queue<byte[]> _queue = new LinkedList<byte[]>();
		
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

		public void addData(byte[] data){
			try {
				synchronized (_queue) {
					if(_queue.size() < MAX_SIZE){
						_queue.offer(data);	
					}
				}
			} catch (Exception e) {
			}
		}
		@Override
		public void run() {
			while(runFlag){
				try {
//					synchronized (_queue) {
						if(_queue != null && _queue.size() > 0){
							byte[] data = null;
							synchronized (_queue) {
								data = _queue.poll();
							}
							//byte[] data = _queue.poll();
							h264Decoder(data);
						}
//					}
				} catch (Exception e) {
					LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
				}
			}
		}
		
	}
	
	

}
