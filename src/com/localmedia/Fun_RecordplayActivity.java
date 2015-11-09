package com.localmedia;

import java.util.TimerTask;
import com.ctrl.XImageBtn;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.vlc.util.VLCInstance;

/**
 * @author: li_jianhua Date: 2015-9-1 上午10:26:49
 * To change this template use File | Settings | File Templates.
 * Description：回放--VLC实现可以实现慢放
 */
@SuppressLint("HandlerLeak")
public class Fun_RecordplayActivity extends Activity implements SurfaceHolder.Callback, IVideoPlayer,OnClickListener{
	
	private final static String TAG = "[Fun_RecordplayActivity]";

	private SurfaceView mSurfaceView;
	private LibVLC mMediaPlayer;
	private SurfaceHolder mSurfaceHolder;
    
    //private View mLoadingView;

	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;
	
	private String fileName = "";//文件名
	TextView _title;
	
	private static long position; //当前进度数
	private long duration; // 总进度数
	// 是否手动拖动播放条标志位
	private boolean isChanging = false;
	private SeekBar seekbar;// 进度条
	private XImageBtn _btnpause,_btnstop,_btnSlow,_btnFast;
	private Button _back;//_upload
	FrameLayout _hreadframeLayout;
	RelativeLayout _layout;
	Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fun_recordplay);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.record_video);
		mSurfaceView.setOnClickListener(this);
       // mLoadingView = findViewById(R.id.video_loading);
		try {
			mMediaPlayer = VLCInstance.getLibVlcInstance();
		} catch (LibVlcException e) {
			e.printStackTrace();
		}
		fileName = getIntent().getExtras().getString("fileName");
		_title = (TextView) this.findViewById(R.id.dev_name);
		setFileName(fileName);
		_back = (Button) this.findViewById(R.id.btn_back_video);
		_back.setOnClickListener(this);
		//_upload = (Button) this.findViewById(R.id.btn_upload_video);
		//_upload.setOnClickListener(this);
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.record_frmheader);
		_layout = (RelativeLayout) this.findViewById(R.id.record_footer);
		_btnstop = (XImageBtn) this.findViewById(R.id.btnStopVideo);
		_btnpause = (XImageBtn) this.findViewById(R.id.btn_play);
		_btnSlow = (XImageBtn) this.findViewById(R.id.btn_slow);
		_btnFast = (XImageBtn) this.findViewById(R.id.btn_fast);
		_btnstop.setOnClickListener(this);
		_btnSlow.setOnClickListener(this);
		_btnpause.setOnClickListener(this);
		_btnFast.setOnClickListener(this);
			
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.addCallback(this);
		mMediaPlayer.eventVideoPlayerActivityCreated(true);

		EventHandler em = EventHandler.getInstance();
		em.addHandler(mVlcHandler);

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mSurfaceView.setKeepScreenOn(true);
		mMediaPlayer.setMediaList();
		fileName = "file:///" + fileName;
		XVideoAdapter._isOpen = true;//点击状态置true
		
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
					mMediaPlayer.setTime(progress);
				}
			}
		});
		show(_layout);
	}
	
	private void show(View v){
		if(!v.isShown()){
			v.setVisibility(View.VISIBLE);
			handler.postDelayed(dis, 3000);
		}
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
	
	private void setFileName(String path){
		try {
			String name = "";
			if(path == null || path == ""){
				return;
			}
			int pathInex = path.lastIndexOf('/');
			if(pathInex == -1){
				return;
			}
			name = path.substring(pathInex + 1);
			int nameLen = name.length();
			_title.setText(name.substring(14, nameLen - 4));
		} catch (Exception e) {
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try {
			// 先判断是否正在播放
			if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
				// 如果正在播放我们就先保存这个播放位置
				position = mMediaPlayer.getTime();
				//mMediaPlayer.stop();
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMediaPlayer != null) {
			mMediaPlayer.eventVideoPlayerActivityCreated(false);
			EventHandler em = EventHandler.getInstance();
			em.removeHandler(mVlcHandler);
			//mMediaPlayer.destroy();
			mMediaPlayer = null;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mMediaPlayer != null) {
			mSurfaceHolder = holder;
			mMediaPlayer.attachSurface(holder.getSurface(), this);
			if (position>0) {
	            try {
	                //开始播放
	            	mHandler.sendEmptyMessage(HANDLER_BUFFER_END);
	                //并直接从指定位置开始播放
	                mMediaPlayer.setTime(position);
	                position=0;                                                
	            } catch (Exception e) {
	            }
	        }else{
	        	//开始播放
	            play();
	        }
		}
	}
	private void play() {
		try {
			System.out.println("fileName:"+fileName);
//			mMediaPlayer.getMediaList().add(new Media(mMediaPlayer, fileName), false);
//			mMediaPlayer.playIndex(0);
			
			mMediaPlayer.playMRL(fileName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static java.util.Timer _timer = null;
	public void startTimer() {
		try {
			Thread.sleep(100);
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
						if(mMediaPlayer.getTime() < duration && mMediaPlayer.getTime() != -1)
							seekbar.setProgress((int) mMediaPlayer.getTime());
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
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		mSurfaceHolder = holder;
//		if (mMediaPlayer != null) {
//			mMediaPlayer.attachSurface(holder.getSurface(), this);//, width, height
//		}
		if (width > 0) {
			mVideoHeight = height;
			mVideoWidth = width;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mMediaPlayer != null) {
			mMediaPlayer.detachSurface();
		}
	}

	@Override
	public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
		mVideoHeight = height;
		mVideoWidth = width;
		mVideoVisibleHeight = visible_height;
		mVideoVisibleWidth = visible_width;
		mSarNum = sar_num;
		mSarDen = sar_den;
		mHandler.removeMessages(HANDLER_SURFACE_SIZE);
		mHandler.sendEmptyMessage(HANDLER_SURFACE_SIZE);
	}

	private static final int HANDLER_BUFFER_START = 1;
	private static final int HANDLER_BUFFER_END = 2;
	private static final int HANDLER_SURFACE_SIZE = 3;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private static final int SURFACE_VERTICAL = 7;
	private int mCurrentSize = SURFACE_BEST_FIT;

	private Handler mVlcHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg == null || msg.getData() == null)
				return;

			switch (msg.getData().getInt("event")) {
			case EventHandler.MediaPlayerTimeChanged:
				break;
			case EventHandler.MediaPlayerPositionChanged:
				break;
			case EventHandler.MediaPlayerPlaying:
				mHandler.removeMessages(HANDLER_BUFFER_END);
				mHandler.sendEmptyMessage(HANDLER_BUFFER_END);
				break;
			case EventHandler.MediaPlayerBuffering:
				break;
			case EventHandler.MediaPlayerLengthChanged:
				break;
			case EventHandler.MediaPlayerEndReached:
				//播放完成
				onClick(_btnstop);
				break;
			case EventHandler.MediaPlayerStopped:                                                                                
//				Toast.makeText(getApplicationContext(), "stream stop or stream offline", Toast.LENGTH_SHORT).show();
//				mMediaPlayer.stop();
//				finish();                                                                                                                                                                                                                                                                                                                                                                              
				break;
			case EventHandler.MediaPlayerEncounteredError:
				Toast.makeText(getApplicationContext(), "MediaPlayer error", Toast.LENGTH_SHORT).show();
				mMediaPlayer.stop();
				finish();
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_BUFFER_START:
               // showLoading();
				break;
			case HANDLER_BUFFER_END:
               // hideLoading();
				duration = mMediaPlayer.getLength();
				seekbar.setMax((int) duration);
    			seekbar.setEnabled(true);
    			startTimer();
				break;
			case HANDLER_SURFACE_SIZE:
				changeSurfaceSize();
				break;
//			case EventHandler.MediaPlayerPaused:
//				_btnpause.SetImages(R.drawable.pause, R.drawable.pause);
//				break;
//			case EventHandler.MediaPlayerPlaying:
//				_btnpause.SetImages(R.drawable.play, R.drawable.play);
//				break;
			}
		}
	};

//	private void showLoading() {
//        mLoadingView.setVisibility(View.VISIBLE);
//	}
//
//	private void hideLoading() {
//        mLoadingView.setVisibility(View.GONE);
//	}
	
	//判断横竖屏
	@SuppressWarnings("static-access")
	public boolean isScreenChange() {
		Configuration cf = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = cf.orientation ; //获取屏幕方向
		if(ori == cf.ORIENTATION_LANDSCAPE){//横屏
			_hreadframeLayout.setVisibility(View.GONE);
			return true;
		}else if(ori == cf.ORIENTATION_PORTRAIT){//竖屏
			_hreadframeLayout.setVisibility(View.VISIBLE);
			return false;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	private void changeSurfaceSize() {
		try {
			// get screen size
			int dw = getWindowManager().getDefaultDisplay().getWidth();
			int dh = getWindowManager().getDefaultDisplay().getHeight();

			// calculate aspect ratio
			double ar = (double) mVideoWidth / (double) mVideoHeight;
			// calculate display aspect ratio
			double dar = (double) dw / (double) dh;
			
			if(isScreenChange()){//横
				mCurrentSize = SURFACE_FILL;
			}else{
				mCurrentSize = SURFACE_VERTICAL;
			}

			switch (mCurrentSize) {
			case SURFACE_BEST_FIT:
				if (dar < ar)
					dh = (int) (dw / ar);
				else
					dw = (int) (dh * ar);
				break;
			case SURFACE_FIT_HORIZONTAL:
				dh = (int) (dw / ar);
				break;
			case SURFACE_FIT_VERTICAL:
				dw = (int) (dh * ar);
				break;
			case SURFACE_FILL:
				break;
			case SURFACE_16_9:
				ar = 16.0 / 9.0;
				if (dar < ar)
					dh = (int) (dw / ar);
				else
					dw = (int) (dh * ar);
				break;
			case SURFACE_4_3:
				ar = 4.0 / 3.0;
				if (dar < ar)
					dh = (int) (dw / ar);
				else
					dw = (int) (dh * ar);
				break;
			case SURFACE_ORIGINAL:
				dh = mVideoHeight;
				dw = mVideoWidth;
				break;
			case SURFACE_VERTICAL:
				dh=dh/2;
				break;
			}

			mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
			ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
			lp.width = dw;
			lp.height = dh;
			mSurfaceView.setLayoutParams(lp);
			mSurfaceView.invalidate();
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_play:
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				//isPause = true;
				//mVlcHandler.sendEmptyMessage(EventHandler.MediaPlayerPaused);
			} else {
				//isPause = false;
				//mVlcHandler.sendEmptyMessage(EventHandler.MediaPlayerPlaying);
				mMediaPlayer.play();
			}
			mSurfaceView.setKeepScreenOn(false);
			OnSelectChange();
			break;

		case R.id.btnStopVideo:
			try {
				stopTimer();
				if (mMediaPlayer.isPlaying()) {
					//stop();
					mMediaPlayer.stop();
				}
				seekbar.setProgress(0);
				Fun_RecordplayActivity.this.finish();
			} catch (Exception e) {
				LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
			}
			break;
		case R.id.btn_back_video:
			onClick(_btnstop);
			break;
		case R.id.btn_slow:
			float slow = mMediaPlayer.getRate();
        	mMediaPlayer.setRate(slow/2);
			break;
		case R.id.btn_fast:
			float fast = mMediaPlayer.getRate();
        	mMediaPlayer.setRate(fast*2);
			break;
		case R.id.record_video:
			show(_layout);
			break;
//		case R.id.btn_upload_video:
//			if(fileName != null){
//				SDK.StartupLoadLocalMedia();
//				FileUtil.readFile(fileName.substring(8, fileName.length()));
//			}
//			break;
		default:
			break;
		}
	}
	
	// 状态更新
	void OnSelectChange() {
		if (mMediaPlayer.isPlaying() == false) {
			_btnpause.SetImages(R.drawable.pause, R.drawable.pause);
		}else{
			_btnpause.SetImages(R.drawable.play, R.drawable.play);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 处理返回按键
			onClick(_btnstop);
		}
		return true;
	}
	

}
