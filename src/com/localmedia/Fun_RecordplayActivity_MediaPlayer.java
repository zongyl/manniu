package com.localmedia;

import java.util.Timer;
import java.util.TimerTask;
import com.ctrl.XImageBtn;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import android.app.Activity;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


/**
 * @author: li_jianhua Date: 2015-9-1 上午10:26:49
 * To change this template use File | Settings | File Templates.
 * Description：录像回放--mediaplayer 方式不能实现慢放
 */
public class Fun_RecordplayActivity_MediaPlayer extends Activity implements SurfaceHolder.Callback,OnClickListener{
	
	private final static String TAG = "[Fun_RecordplayActivity]";

	XImageBtn _btnpause,_btnstop,_btnSlow,_btnFast;
	SurfaceView surfaceView;
	SurfaceHolder m_surfaceHolder;
	MediaPlayer mediaPlayer;
	static int position; //当前进度数
	// 是否手动拖动播放条标志位
	private boolean isChanging = false;
	private SeekBar seekbar;// 进度条
	private String fileName = "";//文件名
	//private boolean isPause = false;//是否暂停
	Button _back;
	TextView _title;
	//宽高自适应......
	private int[] pixels;
    LinearLayout.LayoutParams params;
    FrameLayout framelayout;
    int _width, _height;
    //.............
    RelativeLayout _layout;//显示隐藏工具条
    Handler handler = new Handler();
    FrameLayout _hreadframeLayout;//标题栏

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		setContentView(R.layout.fun_recordplay_back);
		
		framelayout = (FrameLayout)findViewById(R.id.frame);
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.record_frmheader);
		_layout = (RelativeLayout) this.findViewById(R.id.record_footer);
        params = (LinearLayout.LayoutParams)framelayout.getLayoutParams();
		fileName = getIntent().getExtras().getString("fileName");
		 _title = (TextView) this.findViewById(R.id.dev_name);
		 setFileName(fileName);
		_back = (Button) this.findViewById(R.id.btn_back_video);
		_back.setOnClickListener(this);
		// btnplay=(XImageBtn)this.findViewById(R.id.btn_pick_file);
		_btnstop = (XImageBtn) this.findViewById(R.id.btnStopVideo);
		_btnpause = (XImageBtn) this.findViewById(R.id.btn_play);
//		_btnSlow = (XImageBtn) this.findViewById(R.id.btn_slow);
//		_btnFast = (XImageBtn) this.findViewById(R.id.btn_fast);

		_btnstop.setOnClickListener(this);
//		_btnSlow.setOnClickListener(this);
		_btnpause.setOnClickListener(this);
//		_btnFast.setOnClickListener(this);
		
		if(mediaPlayer == null) mediaPlayer = new MediaPlayer();
		surfaceView = (SurfaceView) this.findViewById(R.id.record_video);
		surfaceView.setOnClickListener(this);
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
					mediaPlayer.seekTo(progress);
				}
			}
		});

		// 设置SurfaceView自己不管理的缓冲区
		m_surfaceHolder = surfaceView.getHolder();
        m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);                
        m_surfaceHolder.addCallback(this);
		//当视频文件播放完时触发事件 
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				onClick(_btnstop);
			}
		});
		pixels = getSize();
 		layout();
 		XVideoAdapter._isOpen = true;//点击状态置true
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
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (position>0) {
            try {
                //开始播放
                play();
                //并直接从指定位置开始播放
                mediaPlayer.seekTo(position);
                //mediaPlayer.getCurrentPosition();
                position=0;                                                
            } catch (Exception e) {
            }
        }else{
        	//开始播放
            play();
        }
        
    }                        
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                    int height) {
    	_width = width;
		_height = height;
		isScreenChange();
    }
    
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_play:
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.pause();
				//isPause = true;
			} else {
				//isPause = false;
				mediaPlayer.start();
			}
			OnSelectChange();
			break;

		case R.id.btnStopVideo:
			try {
				stopFastTimer();
				stopTimer();
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				seekbar.setProgress(0);
				Fun_RecordplayActivity_MediaPlayer.this.finish();
			} catch (Exception e) {
				LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
			}
			break;
		case R.id.btn_back_video:
			onClick(_btnstop);
			break;
//		case R.id.btn_slow:
//			addback();
//			break;
//		case R.id.btn_fast:
//			startFastTimer();
//			break;
		case R.id.record_video:
			show(_layout);
			break;
		default:
			break;
		}

	}
	
	private Timer _fastTimer;
	public void startFastTimer() {
		try {
			if (_fastTimer != null) {
				_fastTimer.cancel();
				_fastTimer = null;
			}
			if (_fastTimer == null) {
				_fastTimer = new java.util.Timer();
			}
			_fastTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						if(mediaPlayer.isPlaying()){
							if(mediaPlayer.getCurrentPosition()+10 < mediaPlayer.getDuration())
								mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10);
				        }
					} catch (Exception e) {
					}
				}
			}, 0,5);
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}

	public void stopFastTimer() {
		if (_fastTimer != null) {
			_fastTimer.cancel();
			_fastTimer = null;
		}
	}
	
	private void addgo(){
        if(mediaPlayer.isPlaying()){
        	mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+100);
        	mediaPlayer.start();
        }
    }
   
    private void addback(){
        if(mediaPlayer.isPlaying()){
        	mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-100);
        	mediaPlayer.start();
        }
    }
    
	
	// 状态更新
	void OnSelectChange() {
		if (mediaPlayer.isPlaying() == false) {
			_btnpause.SetImages(R.drawable.pause, R.drawable.pause);
		}else{
			_btnpause.SetImages(R.drawable.play, R.drawable.play);
		}
	}

	@Override
	protected void onPause() {
		try {
			// 先判断是否正在播放
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				// 如果正在播放我们就先保存这个播放位置
				position = mediaPlayer.getCurrentPosition();
				mediaPlayer.stop();
			}
		} catch (Exception e) {
		}
		super.onPause();
	}
	@Override
	public void onDestroy(){
	    if(mediaPlayer != null) {
	    	mediaPlayer.release();
	    	mediaPlayer = null;
	    }
	    super.onDestroy();
	}

	private void play() {
		try {
			mediaPlayer.reset();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// 设置需要播放的视频
			mediaPlayer.setDataSource(fileName);
			// 把视频画面输出到SurfaceView
			mediaPlayer.setDisplay(surfaceView.getHolder());
			
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) { 
						Toast.makeText(Fun_RecordplayActivity_MediaPlayer.this, getText(R.string.Video_play_fail).toString(), Toast.LENGTH_SHORT).show();
						mediaPlayer.reset();// 可调用此方法重置 
						return false;
				    } else if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) { 
				      LogUtil.v(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK"); 
				    } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) { 
				      LogUtil.v(TAG, "MEDIA_ERROR_UNKNOWN"); 
				    } 
					return true;
				}
			});
			mediaPlayer.prepare();
			startTimer();
			// 播放
			mediaPlayer.start();
			seekbar.setMax(mediaPlayer.getDuration());
			seekbar.setEnabled(true);
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) { // 处理返回按键
			onClick(_btnstop);
		}
		return true;
	}

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
						seekbar.setProgress(mediaPlayer.getCurrentPosition());
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
	

}
