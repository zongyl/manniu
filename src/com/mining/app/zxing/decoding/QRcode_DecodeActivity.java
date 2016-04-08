package com.mining.app.zxing.decoding;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.mining.app.zxing.camera.CameraManager;
import com.utils.Constants;
import com.utils.LogUtil;
import com.views.Main;
import com.views.NewLogin;

/**
 * @author: li_jianhua Date: 2015-4-13 上午10:52:13
 * To change this template use File | Settings | File Templates.
 * @Description： 扫描二维码界面
 */
public class QRcode_DecodeActivity extends Activity implements Callback {

	public final String TAG = "QRcode_DecodeActivity";
	
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.50f;
	private boolean vibrate;
	private int x = 0;
	private int y = 0;
	private int cropWidth = 0;
	private int cropHeight = 0;
	private RelativeLayout mContainer = null;
	private RelativeLayout mCropLayout = null;

	private String location;
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCropWidth() {
		return cropWidth;
	}

	public void setCropWidth(int cropWidth) {
		this.cropWidth = cropWidth;
	}

	public int getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(int cropHeight) {
		this.cropHeight = cropHeight;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置为无标题格式
		setContentView(R.layout.fun_scan);
		// 初始化 CameraManager
		CameraManager.init(getApplication());
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
		mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);

		Button back = (Button) findViewById(R.id.scan_back);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		
		ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
		ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
		animation.setRepeatCount(-1);
		animation.setRepeatMode(Animation.RESTART);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(1200);
		mQrLineView.startAnimation(animation);
		Main.Instance.startLocation();
		registerReceiver(receiver, filter);
	}
	
	
	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(this);
			Log.d(TAG, "onReceive：。。。");
			location = intent.getExtras().getString("location");
			Log.d(TAG, "json:" + location);
			//com.alibaba.fastjson.JSONObject jObj = JSON.parseObject(location);
			//tv_location.setText(jObj.getString("address"));
		}
	};

	IntentFilter filter = new IntentFilter("com.views.NewMainAddDev");
	
	
	boolean flag = true;

	protected void light() {
		if (flag == true) {
			flag = false;
			// 开闪光灯
			CameraManager.get().openLight();
		} else {
			flag = true;
			// 关闪光灯
			CameraManager.get().offLight();
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		mContainer = null;
		mCropLayout = null;
		mediaPlayer = null;
		super.onDestroy();
	}

	//扫描结果处理
	public void handleDecode(String result) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		
		if (result.equals("")) {
			Toast.makeText(QRcode_DecodeActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		}else {
			Log.d(TAG, "result:"+result);
			
			Map<String, Object> infos = parseCode(result);

			if(infos.containsKey("LTS_devId")){//LTS device
				LogUtil.d(TAG, "LTS device ID:" + infos.get("LTS_devId").toString());
				byte[] b = new byte[28];
				String res = SDK.EncodeUuid(b, "US", 0, 2, 1, 0, 0, infos.get("LTS_devId").toString());
				
				try {
					res = new String(b, "ISO-8859-1");
					LogUtil.d(TAG, "b.tostring:" + res);
					addDevices(res, "ABCDEF", location);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(infos.containsKey("sn")){//zeno device
				if(infos.containsKey("vn")){
					String sn = infos.get("sn").toString();
					String vn = infos.get("vn").toString();
					if("".equals(sn)||sn==null){
						APP.ShowToast(getString(R.string.unrecognized_device));
					}else{
						if("".equals(vn)||vn==null){
							APP.ShowToast(getString(R.string.unrecognized_device));
						}else{
							addDevices(sn, vn, location);
						}
					}
				}else{
					APP.ShowToast(getString(R.string.unrecognized_device));
				}
			}else{
				APP.ShowToast(getString(R.string.unrecognized_device));
			}
		}
		QRcode_DecodeActivity.this.finish();
		
		// 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
		//handler.sendEmptyMessage(R.id.restart_preview);
	}
	
	/**
	 * 解析二维码
	 * @param result
	 * @return
	 */
	public Map<String, Object> parseCode(String result){
		Map<String, Object> infos = new HashMap<String, Object>();
		String[] strs = result.split(";");
		if(strs.length > 1){
			String[] info;
			for(String str : strs){
				info = str.split(":");
				if(info.length > 1){
					infos.put(info[0], info[1]);
				}
			}
		}else{
			strs = result.split("\r");
			if(strs.length > 1){
				if("www.LTSecurityinc.com".equals(strs[0])){
					LogUtil.d(TAG, "LTS device ID:" + strs[1]);
					infos.put("LTS_devId", strs[1]);
				}
			}
		}
		return infos;
	}
	
	public void addDevices(String sn, String vn, String location){
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
		params.put("sn", sn);
		params.put("vn", vn);
		params.put("location", location);
		HttpUtil.get(Constants.hostUrl+"/android/addDevices", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.v(TAG, "response："+response);
				if(statusCode == 200){
					Log.d(TAG, response.toString());
					String msg = "";
					try {
						msg = response.getString("msg");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if("snNotExist".equals(msg)){
						APP.ShowToast(getString(R.string.unregisterad_sn));
					}else if("existOtherUser".equals(msg)){
						APP.ShowToast(getString(R.string.device_havebinduser));
					}else if("vnError".equals(msg)){
						APP.ShowToast(getString(R.string.securitycode_Error));
					}else if("failure".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addFail));
					}else if("success".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addok));
						finish();
						Main.Instance.NewMainreLoad();
					}
					
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.E_SER_FAIL));
			}
		});
	}
	
	//添加到DB
	public void addDevices(){
		String str = "http://www.ys7.com/|1001|ABCDEF|WIFI";
		RequestParams params = new RequestParams();
		
//		params.put("username", user);
//		params.put("password", pwd);
		
		HttpUtil.get(this.getResources().getString(R.string.server_address)+"/android/login", 
				params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					String msg = "";
					String data = "";
					try {
						msg = json.getString("msg");
						data = json.getString("data");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if("success".equals(msg)){
						
					}else{
						APP.ShowToast(getResources().getString(R.string.dev_addFail));
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.E_SER_FAIL));
			}
			});
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);

			Point point = CameraManager.get().getCameraResolution();
			int width = point.y;
			int height = point.x;

			int x = mCropLayout.getLeft() * width / mContainer.getWidth();
			int y = mCropLayout.getTop() * height / mContainer.getHeight();

			int cropWidth = mCropLayout.getWidth() * width
					/ mContainer.getWidth();
			int cropHeight = mCropLayout.getHeight() * height
					/ mContainer.getHeight();

			setX(x);
			setY(y);
			setCropWidth(cropWidth);
			setCropHeight(cropHeight);

		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(QRcode_DecodeActivity.this);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public Handler getHandler() {
		return handler;
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
}
