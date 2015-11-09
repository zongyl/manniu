package com.nmbb.vlc.ui;

import java.util.Timer;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;
import org.videolan.vlc.util.VLCInstance;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.bean.live.LiveCommand;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.DateUtil;
import com.utils.Loger;
import com.utils.SetSharePrefer;
import com.views.Dlg_Wait;
import com.views.NewMain;

public class VlcVideoActivity extends Activity  implements SurfaceHolder.Callback, IVideoPlayer ,android.view.View.OnClickListener{
	
	Handler handler = new Handler();
	
	private final static int STOP =101010;
	private final static String TAG = "[VlcVideoActivity]";

	public static final String COLLECT_PRE = "collect_info";
	public static final String PRAISE_PRE = "praise_info";
	
	private SurfaceView mSurfaceView;
	private LibVLC mMediaPlayer;
	private SurfaceHolder mSurfaceHolder;
    
    private View mLoadingView;
    private Button fullScreen;
    
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarNum;
	private int mSarDen;

	public Dlg_Wait _dlgWait = null;
	
	RequestParams params;
	private RelativeLayout footer;
	private FrameLayout liveComm;
	
	private static final int HANDLER_BUFFER_START = 1;
	private static final int HANDLER_BUFFER_END = 2;
	private static final int HANDLER_SURFACE_SIZE = 3;
	private static final int HANDLER_STOP = 4;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private static final int SURFACE_VERTICAL = 7;
	private int mCurrentSize = SURFACE_BEST_FIT;
	
	LiveCommand mLiveCommand;
	Timer _timer;
	FrameLayout header;
	ImageView collection;
	ImageView like;
	Button back;
	TextView liveTitle;
	
	boolean b_like = false;
	boolean b_collection = false;
	String deviceId;
	String liveName;
	
	ProgressBar _bar;
	//宽高自适应......
	private int[] pixels;
    LayoutParams UIparams;
    FrameLayout framelayout;
    
    String hostUrl;
    
    public static  VlcVideoActivity instance = null;
    private static int nFirstInit =1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.live_video);
		_dlgWait = new Dlg_Wait(this, R.style.dialog);
		framelayout = (FrameLayout) findViewById(R.id.frame);
		UIparams = (RelativeLayout.LayoutParams)framelayout.getLayoutParams();
		
		mSurfaceView = (SurfaceView) findViewById(R.id.video);
        mLoadingView = findViewById(R.id.video_loading);
	
		try {
			mMediaPlayer = VLCInstance.getLibVlcInstance();
		} catch (LibVlcException e) {
			e.printStackTrace();
		}
		
		
		header = (FrameLayout) findViewById(R.id.hhheader);
		footer = (RelativeLayout) findViewById(R.id.pagefooter);
		fullScreen = (Button)findViewById(R.id.btn_play_fullscreen);
		liveComm = (FrameLayout) findViewById(R.id.live_com);
		
		collection = (ImageView)findViewById(R.id.collection);
		like = (ImageView)findViewById(R.id.like);
		back = (Button) findViewById(R.id.btn_back_video);
		liveTitle = (TextView) findViewById(R.id.liv_name);
		
		bindListener();
		
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
		mSurfaceHolder.addCallback(this);

		mMediaPlayer.eventVideoPlayerActivityCreated(true);

		EventHandler em = EventHandler.getInstance();
		em.addHandler(mVlcHandler);

		//this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//mSurfaceView.setKeepScreenOn(true);
	
		mMediaPlayer.setMediaList();
		//String url = "http://10.12.6.107:8080/NineCloud/download/20150827143437IPC(manniu225).mp4";
		//String url = Environment.getExternalStorageDirectory()+"/manniu/records/20150827143437IPC(manniu225).mp4";
		//String url ="http://hzhls01.ys7.com:7888/hcnp/524074371_1_1_1_0_183.136.184.7_6500.m3u8";
		//String url ="http://192.168.1.61:80/live.m3u8";
		//String url ="http://120.26.56.240:7001/live/Q04hAQEAbDAwMDEwYmUxAAAAAAAA.m3u8"; 
		//mMediaPlayer.getMediaList().add(new Media(mMediaPlayer, "http://10.12.6.130:80/Q04hAQEAbDAwMjAxNTQ5AAAAAAAA.m3u8"), false);
		//doPlay();
		pixels = getSize();
 		layout();
 		//hostUrl = "http://10.12.6.107:8080/NineCloud";
 		hostUrl =Constants.hostUrl;
 		
 		liveOrShort();
		
		NewMain._isOpen = true;//打开视频标志位
		//mMediaPlayer.getMediaList().add(new Media(mMediaPlayer, url), false);
		//mMediaPlayer.playIndex(0);
		//mMediaPlayer.playMRL(url);
		
	}
	/**根据视频类型 播放数据*/
	public void liveOrShort(){
		
		if("short".equals(getIntent().getStringArrayExtra("LiveInfo")[0])){
			doShortPlay();
		}else{
			String[] args = getIntent().getStringArrayExtra("LiveInfo");
			deviceId = args[0];
			liveName = args[1];
			if(args.length>2){
				if("fromList".equals(args[2])){
					collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
				}
			}
			liveTitle.setText(liveName);
			initPraiseAndCollect();			
			doLivePlay();
		}
	}
	
	/**点赞收藏状态*/
	public void initPraiseAndCollect()
	{
		if(true == SetSharePrefer.read_bool("collect_info", deviceId))
		{
			collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
		}
			
		if(true == SetSharePrefer.read_bool("praise_info", deviceId))
		{
			like.setImageDrawable(getResources().getDrawable(R.drawable.like_sel));
		}
	}
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
			show(footer);
			UIparams.height = pixels[1];
			UIparams.width = pixels[0];
			framelayout.setLayoutParams(UIparams);
		}else{
			show(footer);
			UIparams.height = pixels[1]/2;
			UIparams.width = pixels[0];
			framelayout.setLayoutParams(UIparams);
		}
	}
	
	public void bindListener(){
		mSurfaceView.setOnClickListener(this);
		
		footer.setOnClickListener(this);
		fullScreen.setOnClickListener(this);
		
		collection.setOnClickListener(this);
		like.setOnClickListener(this);
		back.setOnClickListener(this);
	}
	
	public void doShortPlay(){
		if(mMediaPlayer.getMediaList()!=null){
			mMediaPlayer.getMediaList().clear();
		}
		
		//mMediaPlayer.getMediaList().clear();
		
		mMediaPlayer.getMediaList().add(new Media(mMediaPlayer, getIntent().getStringArrayExtra("LiveInfo")[1]), false);
		mMediaPlayer.playIndex(0);
	}
	
	public void doLivePlay(){
		
		params = new RequestParams();
		params.put("deviceId", deviceId);
		HttpUtil.get(hostUrl+"/device/getLiveServer", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				try {

					if(response.has("data")){
						String result = response.getString("data");
						Log.v(TAG, "live url:"+result);
						if("no".equals(result)){
							APP.ShowToast(APP.GetString(R.string.Video_play_fail));
							finish();
						}else{
							
							if(mMediaPlayer.getMediaList()!=null){
								mMediaPlayer.getMediaList().clear();
							}
							try{
								//去掉首次请到到的live地址参数
								/*if(result.contains("?")){
									String[] temp = result.split("\\?");
									result = temp[0];
								}*/
							}catch(Exception e){
								e.printStackTrace();
							}
							mMediaPlayer.getMediaList().add(new Media(mMediaPlayer, result), false);
							
							MediaList ms = mMediaPlayer.getMediaList();
							Log.d(TAG, ""+ms.size());
							TextView v = (TextView) findViewById(R.id.video_loading_text);
							v.setText(R.string.video_layout_starting);
							Loger.print("live deviceid:"+deviceId+"; m3u8-url:"+result+"  time: "+DateUtil.getStringDateByLong(System.currentTimeMillis()));
							mMediaPlayer.playIndex(0);	
							
						}
					}else{
						APP.ShowToast(APP.GetString(R.string.Video_play_fail));						
						finish();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub				
				super.onFailure(statusCode, headers, throwable, errorResponse);
				APP.ShowToast(APP.GetString(R.string.E_SER_FAIL));
				finish();
			}
		});
	}
	

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		//mMediaPlayer.stopDebugBuffer();
		//mMediaPlayer.stop();
		finish();
		//mHandler.sendEmptyMessage(STOP);
		return true;
	}
	*/
	@Override
	public void onPause() {
		super.onPause();
		/*if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mSurfaceView.setKeepScreenOn(false);
		}*/
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		instance = null;
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}
		
		//mMediaPlayer = null;
	/*if (mMediaPlayer != null) {
			Log.v(TAG, "media player is playiing or not "+mMediaPlayer.isPlaying());
			//mMediaPlayer.destroy();
			mMediaPlayer.stop();
			mMediaPlayer.eventVideoPlayerActivityCreated(false);
			EventHandler em = EventHandler.getInstance();
			em.removeHandler(mVlcHandler);
		}*/
	}

	@Override
	public void onBackPressed() {
		this.onDestroy();
		finish();

	}
	
	/*Runnable closeVideo = new Runnable(){

		@Override
		public void run() {
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.eventVideoPlayerActivityCreated(false);
				EventHandler em = EventHandler.getInstance();
				em.removeHandler(mVlcHandler);
			}
			mHandler.removeCallbacks(closeVideo);
		}
		
	};*/
	
	public boolean isScreenChange() {
		Configuration cf = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = cf.orientation ; //获取屏幕方向
		if(ori == cf.ORIENTATION_LANDSCAPE){//横屏
			header.setVisibility(View.GONE);
			liveComm.setVisibility(View.GONE);
			return true;
		}else if(ori == cf.ORIENTATION_PORTRAIT){//竖屏
			header.setVisibility(View.VISIBLE);
			liveComm.setVisibility(View.VISIBLE);
			return false;
		}
		return false;
	}
	
	private void orientation(){
		Configuration cfg = getResources().getConfiguration();	
		if(cfg.orientation == 1){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			liveComm.setVisibility(View.GONE);
			//mSurfaceView.invalidate();
		}else if(cfg.orientation == 2){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			liveComm.setVisibility(View.VISIBLE);
			//mSurfaceView.invalidate();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
		super.onConfigurationChanged(newConfig);
		pixels = getSize();
		layout();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mMediaPlayer != null) {
			mSurfaceHolder = holder;
			mMediaPlayer.attachSurface(holder.getSurface(), this);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		mSurfaceHolder = holder;
		if (mMediaPlayer != null) {
			mMediaPlayer.attachSurface(holder.getSurface(), this);//, width, height
		}
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
				//2015.09.23 李德明修改，去除Video_play_stop提示，增加nFirstInit变量处理保证只调用一次mMediaPlayer.clearBuffer();mMediaPlayer.stop();
				if(mMediaPlayer!=null){

				}
				break;
			case EventHandler.MediaPlayerPaused:				
				
				break;
			case EventHandler.MediaPlayerStopped:

				//2015.09.23 李德明修改，去除Video_play_stop提示，增加nFirstInit变量处理保证只调用一次mMediaPlayer.clearBuffer();mMediaPlayer.stop();				
				if(mMediaPlayer!=null){
					mHandler.removeMessages(HANDLER_BUFFER_END);
					mHandler.sendEmptyMessage(HANDLER_BUFFER_END);
				}
                                                                                                                                                                                                                                                                                                                                                                             
				break;
			case EventHandler.MediaPlayerEncounteredError:
				//2015.09.23 李德明修改，去除Video_play_stop提示，增加nFirstInit变量处理保证只调用一次mMediaPlayer.clearBuffer();mMediaPlayer.stop();

				if(mMediaPlayer!=null){

				}
				break;
			case EventHandler.HardwareAccelerationError:
				//2015.09.23 李德明修改，去除Video_play_stop提示，增加nFirstInit变量处理保证只调用一次mMediaPlayer.clearBuffer();mMediaPlayer.stop();
				if(mMediaPlayer!=null){
				}
				break;
			}
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_BUFFER_START:
                showLoading();
				break;
			case HANDLER_BUFFER_END:
                hideLoading();
                show(footer);
				break;
			case HANDLER_SURFACE_SIZE:
				changeSurfaceSize();
				break;
			case HANDLER_STOP:
				showLoading();
				break;
			case STOP:				
				release();
				break;
			}
		}
	};

	public void release(){
		this.onDestroy();
		finish();
	}
	
	public boolean isVlcPlaying(){
		return mMediaPlayer != null ? mMediaPlayer.isPlaying():false;
	}
	
	private void showLoading() {
        mLoadingView.setVisibility(View.VISIBLE);
		//_dlgWait.show();
		//_dlgWait.UpdateTextNoDelay(getString(R.string.video_layout_loading));
		//_bar.setVisibility(View.VISIBLE);
	}

	private void hideLoading() {
        mLoadingView.setVisibility(View.GONE);
		//_dlgWait.dismiss();
		//_bar.setVisibility(View.GONE);
	}

	private void changeSurfaceSize() {
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.video:
			show(footer);
			break;
		case R.id.btn_play_fullscreen:
			orientation();
			break;
		case R.id.collection:
			if(false == b_collection)	
			{
				save(R.id.collection);
			}
			else
			{
				cancel(R.id.collection);
			}
			break;
		case R.id.like:
			if(false == b_like)
			{
				save(R.id.like);
			}
			else
			{
				cancel(R.id.like);
			}
			break;
		case R.id.btn_back_video:
			showLoading();
			this.onDestroy();
			finish();
			break;
		default:
			break;
		}
		
	}
	
	/**保存收藏信息,成功返回后切换按钮背景图片*/
	public void save(int id){
		final String save_failure = getResources().getString(R.string.Err_CONNET);
		final String collect_success = getResources().getString(R.string.SUCCESS_COLLECT);
		final String like_success = getResources().getString(R.string.SUCCESS_LIKE);
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
		params.put("liveid", deviceId);
		
		switch(id)
		{
		case R.id.collection:
			HttpUtil.get(hostUrl+"/android/saveCollect", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							Log.v(TAG, result);
							if("true".equals(result)){
								APP.ShowToast(collect_success);
								SetSharePrefer.write_bool("collect_info", deviceId, true);
								collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
								b_collection = true;	
								//TODO
							}else{
								APP.ShowToast(save_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(save_failure);
				}
			});
			break;
			
		case R.id.like:
			HttpUtil.get(hostUrl+"/android/savePraise", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							Log.v(TAG, result);
							if("true".equals(result)){
								APP.ShowToast(like_success);
								SetSharePrefer.write_bool("praise_info", deviceId, true);
								like.setImageDrawable(getResources().getDrawable(R.drawable.like_sel));
								b_like = true;	
								//TODO
							}else{
								APP.ShowToast(save_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(save_failure);
				}
				
			});	
			break;
		default:
			break;
		}
		
	}
	
	public void cancel(int id){
		final String cancel_failure = getResources().getString(R.string.Err_CONNET);
		final String collect_success = getResources().getString(R.string.CANCEL_COLLECT);
		final String like_success = getResources().getString(R.string.CANCEL_LIKE);
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
		params.put("liveid", deviceId);
		switch(id)
		{
		case R.id.collection:
			
			HttpUtil.get(hostUrl+"/android/cancelCollect", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							if("true".equals(result)){
								APP.ShowToast(collect_success);
								SetSharePrefer.write_bool("collect_info", deviceId, false);
								collection.setImageDrawable(getResources().getDrawable(R.drawable.collection));
								b_collection = false;
								//TODO
							}else{
								APP.ShowToast(cancel_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(cancel_failure);
				}
			});
			break;
		case R.id.like:
			HttpUtil.get(hostUrl+"/android/cancelPraise", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							if("true".equals(result)){
								APP.ShowToast(like_success);
								SetSharePrefer.write_bool("praise_info", deviceId, false);
								like.setImageDrawable(getResources().getDrawable(R.drawable.like));
								b_like = false;
								//TODO
							}else{
								APP.ShowToast(cancel_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(cancel_failure);
				}
			});
			break;
		default:
			break;
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
	
	private void show(View v) {
		if(!v.isShown()){
			v.setVisibility(View.VISIBLE);
			handler.postDelayed(dis, 2000);
		}
	}
	
	private ProgressBar createProgressBar(Activity activity,Drawable custoD){
		FrameLayout container = (FrameLayout) activity.findViewById(R.id.frame);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL;
		ProgressBar bar = new ProgressBar(activity);
		bar.setLayoutParams(lp);
		if(custoD!=null){
			bar.setIndeterminateDrawable(custoD);
		}
		container.addView(bar);
		return bar;
	}
}
