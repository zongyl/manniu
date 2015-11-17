package net.majorkernelpanic.streaming.hw;

import java.io.File;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.basic.XMSG;
import com.ctrl.SysPopupMenu;
import com.ctrl.XImageBtn;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.Constants;
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.BaseApplication;
import com.views.Dlg_WaitForActivity;
import com.views.Main;
import com.views.analog.camera.audio.AudioRecorder;
import com.views.bovine.Fun_AnalogVideo;
import P2P.SDK;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLES11Ext;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author: li_jianhua Date: 2015-4-23 上午9:59:44
 * To change this template use File | Settings | File Templates.
 * @Description：  MediaRecorder编码 --- 模拟IPC  只采集、并显示预览
 */
@SuppressLint("NewApi")
public class AnalogvideoActivity extends Activity implements SurfaceHolder.Callback,OnClickListener{
	private String TAG = AnalogvideoActivity.class.getSimpleName();
	public static AnalogvideoActivity instance = null;
	DatagramSocket socket;
	InetAddress address;
	public XImageBtn _btnPic,_btnRecord,_btnStop,_btnScreen;
    public Camera m_camera;  
    public static SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    public static SurfaceTexture surfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);//不显示预览获取到数据帧
    public int _width,_height;
    LinearLayout _layout;
    Handler handler = new Handler();
    TextView _tooltip;
    public AudioRecorder _talkPlayer; //AAC编码
    public EnCoderQueue _encoderQueue;//软编码队列
    public String deviceSid = "",deviceName = "";
    
    
    private AnalogHandler _handler = null;
    public Dlg_WaitForActivity _dlgWait = null;
//    ProgressDialog _myDialog = null;
    public static boolean isOpenAnalog = false;//0 打开牛眼
    
  //检查网络广播接收器
//    PhoneStatReceiver phoReceiver;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
     	// 设置全屏
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		instance = this;
		deviceSid = getIntent().getExtras().getString("deviceSid");
		deviceName = getIntent().getExtras().getString("deviceName");
		//context = this.getApplicationContext();
		BaseApplication.getInstance().addActivity(this);
		_btnPic = (XImageBtn) this.findViewById(R.id.btnPic);
		_btnRecord = (XImageBtn) this.findViewById(R.id.btnRecord);
		_btnStop = (XImageBtn) this.findViewById(R.id.btnStop);
		_btnScreen = (XImageBtn) this.findViewById(R.id.btnScreen);
		_btnPic.setOnClickListener(this);
		_btnRecord.setOnClickListener(this);
		_btnStop.setOnClickListener(this);
		_btnScreen.setOnClickListener(this);
		_layout = (LinearLayout) findViewById(R.id.layout_memo);
		_tooltip = (TextView) this.findViewById(R.id.tooltip);
		
		m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay);
		m_surfaceHolder = m_prevewview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
		// 设置该SurfaceView自己不维护缓冲    
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		m_surfaceHolder.addCallback((Callback) this);
		m_prevewview.setOnClickListener(this);
		
		//initEncoderSetting();
		_talkPlayer = new AudioRecorder();
		_encoderQueue = new EnCoderQueue();
		show(_layout);
		
		_handler = new AnalogHandler();
		//解析MP4文件
		Fun_AnalogVideo.instance.h.setSurfaceView(m_prevewview);
		if(_dlgWait == null){
			_dlgWait = new Dlg_WaitForActivity(this,R.style.dialog);
		}
		isOpenAnalog = true;
		Main.Instance.startLocation();
		registerReceiver(receiver, filter);
		startHeartBeat();
//		phoReceiver = new PhoneStatReceiver();
//		_dlgWait.start();
//		_dlgWait.show();
//		_dlgWait.UpdateTextReal("正在查找MPEG4头");
//		APP.showProgressDialog(AnalogvideoActivity.this, "正在查找 ....");
	}
	
	//截图功能
	@SuppressLint("SimpleDateFormat")
	public void snapPic(Bitmap bitmap,String fileName) {
		BitmapUtils.CompressToFile(bitmap, fileName,100);
		File file = new File(Fun_AnalogVideo.instance.h.mPacketizer._fileName);
		if(file.isFile() && file.exists()){
			Toast.makeText(this, getString(R.string.Video_snap_success), Toast.LENGTH_SHORT).show();
		}
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
		String fileName = path + strDate + deviceName + ".bmp";
		fileName = fileName.replace(" ", "");
		fileName = fileName.replace("-", "");
		fileName = fileName.replace(":", "");
		return fileName;
	}

    long _lLastBack = 0;
    public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
			closeVideo();
		}
		return true;
	}
    
    public void closeVideo(){
		//如果正在采集提示 并不跳转
    	try {
//    		if(SDK._sessionId == 0){
    			new AlertDialog.Builder(this).setTitle(getString(R.string.prompt_title))
    			.setMessage(getString(R.string.areyousure_tocloseanalogIPC))
    			.setIcon(R.drawable.help)
    			.setPositiveButton(getString(R.string.btn_OK_caption), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int which) {
    					clearAnalog();
    					//先把模拟的登出
    					//SDK.P2PClose(SDK._sessionId);//2015.09.28 李德明添加，解决视频源播放时退出视频，接收视频端第二次无法打开的处理，一直提示设备忙
    					LogUtil.d(TAG, "logout...start...");
    			    	SDK.Logout();
    					//退出之前 在登录IPC的
    			    	BackLoginThread.state = 2;
    			    	Main.Instance._loginThead.start();
    			    	LogUtil.d(TAG, "Main.Instance._loginThead.start()..ok.."); 
    			    	AnalogvideoActivity.this.finish();
    				}
    			}).setNegativeButton(getString(R.string.btn_Cancel_caption), null).show();
//    		}else{
//    			APP.ShowToast("正在采集视频不能退出...");
//    		}
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
    
    public void clearAnalog(){
    	isOpenAnalog = false;
    	if (_encoderQueue._isRecording) {//如果正在录像关闭录像
			stopRecordingVideo();
		}
		if(_encoderQueue != null) _encoderQueue.Stop();//停止编码线程
		if(_talkPlayer != null) _talkPlayer.Stop();//停止音频线程
		//LogUtil.d(TAG, "停止编码线程");
		stopEncode();
    	SDK.Ffmpegh264EncoderUninit();			    	
    	//LogUtil.d(TAG, "stopEncode...ok.. SDK._sessionId=="+SDK._sessionId); 
    }
    
    //断线关闭连接
    public void sendCheageData(){
    	_handler.sendEmptyMessage(1002);
    }
    
    //注销编码器
    public void stopEncode(){
    	//if(_talkPlayer != null) _talkPlayer.Stop();
    	//如果正在录像编码标志位不变
    	if(!_encoderQueue._isRecording) _encoderQueue._isEncord = false;
    	_talkPlayer._startEncodeNow = false;
    	
    }
    //开始编码
    public void startEncode(){
    	try {
    		_encoderQueue._isEncord = true;
    		_talkPlayer._startEncodeNow = true;
			//_talkPlayer.Start();
		} catch (Exception e) {
			LogUtil.i(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
    }
    
    //暂停/恢复
  	public boolean isResume = false;
    @Override
	protected void onResume() {
		super.onResume();
		if(isResume){
			show(_layout);
			isResume = false;
		}
		//LogUtil.i(TAG, ".......onResume........");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		isResume = true;
		//LogUtil.i(TAG, ".......onPause........");
	}
	
	public void onStart(){
		super.onStart();
        //注册广播接收器
//		IntentFilter intentFilter = new IntentFilter(); 
//		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
//		intentFilter.setPriority(1000); 
//		registerReceiver(phoReceiver, intentFilter);
    }
	
	protected void onStop() {
		//LogUtil.i(TAG,"onstop");
		stopHeartBeat();
		//取消广播接收器
//        if (phoReceiver != null) {
//        	unregisterReceiver(phoReceiver);
//    	}
		super.onStop();
	}
	
	protected void onDestroy() {
		if(!AnalogvideoActivity.isOpenAnalog){
			receiver = null;
			instance = null;
		}
		//LogUtil.i(TAG,"onDestroy");
		super.onDestroy();
	}



	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		//System.out.println("surfaceChanged");
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//_dlgWait.start();
		Fun_AnalogVideo.instance.h.setSurfaceView(m_prevewview);
		_handler.sendEmptyMessage(-1002);
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		Fun_AnalogVideo.instance.h.destroyCamera();
	}
	
	public void changeTextValue(){
		Message msg = new Message();
		msg.what = XMSG.CHANGE_TEXTVALUE;
		if(SDK._sessionId != 0){
			msg.obj = "  正在采集视频数据...";
			_handler.sendMessageDelayed(msg, 300);
		}else{
			Fun_AnalogVideo.instance.h.mPacketizer.iFrameFlag = 0;
			Fun_AnalogVideo.instance.h.mPacketizer.dataFlag = 0;
			msg.obj = "  连接关闭，正在预览...";
			_handler.sendMessageDelayed(msg, 300);
			
		}
	}
	
	//public boolean _recordShow = false;		// 录像显示状态
	public String _recordfileName = "";//录像文件名
	private boolean safeToTakePicture = true;//截图片标志
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPic:
			Fun_AnalogVideo.instance.h.mPacketizer._fileName = getFileName();
			Fun_AnalogVideo.instance.h._startSnap = true;
			
			/*Fun_AnalogVideo.instance.h.mPacketizer._fileName = getFileName();
			if (safeToTakePicture) {
				safeToTakePicture = false;
				Fun_AnalogVideo.instance.h.mCamera.takePicture(null, null, Fun_AnalogVideo.instance.h.BmpCallback);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			    safeToTakePicture = true;
			}*/
			
			break;
		case R.id.btnRecord:
			// 如果正在录像，则停止
			if (_encoderQueue._isRecording) {
				APP.ShowToast(getText(R.string.Video_record_end).toString());
				stopRecordingVideo();
			} else {	// 开始录像
				//_recordShow = true;
				_btnRecord.setClickable(false);
				_btnRecord.SetImages(R.drawable.btn_record2, R.drawable.btn_record2);
				String strDate = DateUtil.getCurrentStringDate(DateUtil.DEFAULT_DATE_TIME_FORMAT);
				_recordfileName = Fun_AnalogVideo.RecordPath + strDate + deviceName;
				File dir = new File(Fun_AnalogVideo.RecordPath.substring(0,Fun_AnalogVideo.RecordPath.length()-1));
				if(!dir.exists()) dir.mkdirs();
				if((int)SdCardUtils.getSurplusStorageSize(Fun_AnalogVideo.RecordPath) > 20){
					//Fun_AnalogVideo.instance.h.mPacketizer._fileName = fileName + ".mp4";
					Fun_AnalogVideo.instance.h.mPacketizer._fileName = _recordfileName + ".bmp";
					//Fun_AnalogVideo.instance.h.mCamera.takePicture(null, null, Fun_AnalogVideo.instance.h.BmpCallback);
					Fun_AnalogVideo.instance.h._startSnap = true;
					_handler.sendEmptyMessageDelayed(XMSG.GetNotify, 500); //延迟发送
				}else{
					APP.ShowToast("剩余存储空间不足!");
				}
			}
			break;
		case R.id.btnStop:
			//创建下拉菜单
			closeVideo();
			break;
		case R.id.SurfaceViewPlay:
			show(_layout);
			break;
		case R.id.btnScreen://设置封面
			Fun_AnalogVideo.instance.h.mPacketizer._fileName = getFileName();
			Fun_AnalogVideo.instance.h._startSnap = true;
			Fun_AnalogVideo.instance.h._startScreen = true;
			break;
		default:
			break;
		}
	}
	
	//停止录像
	private void stopRecordingVideo(){
		_btnRecord.SetImages(R.drawable.btn_record0, R.drawable.btn_record0);
		//如果正在采集标志位不变
		_encoderQueue._isRecording = false;
		if(SDK._sessionId == 0 && SDK._createChnlFlag == -1){
			_encoderQueue._isEncord = false;
		}
		_encoderQueue.h264ToMp4();
	}
	
	//创建更多下拉菜单
	private final static int SYS_SETTING = 0;//系统设置
	public void createMenu(View v) {
		try {
			/*if (Constants.netWakeState == false){
				Toast.makeText(context,
						APP.GetString(R.string.Err_NetConnect), Toast.LENGTH_LONG).show();
				return;
			}*/
			SysPopupMenu menu = new SysPopupMenu(this);
			//menu.setHeaderTitle("用户菜单");
			menu.setOnItemSelectedListener(this);
			menu.add(SYS_SETTING, R.string.stop_stream).setIcon(
					getResources().getDrawable(R.drawable.ic_menu_setting));
			menu.show(v);
		} catch (Exception e) {
			LogUtil.e(TAG, e);
		}
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
	
	
	public void closeWait(){
		if(_dlgWait.isShowing()) _dlgWait.dismiss();
//		_dlgWait.stop();
	}
	
	@SuppressLint("HandlerLeak")
	public class AnalogHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case XMSG.CHANGE_TEXTVALUE:
				_tooltip.setText(msg.obj.toString());
				show(_layout);
				break;
			case -1002:
				try {
					Fun_AnalogVideo.instance.h.start();
					_encoderQueue.Start();
					_talkPlayer.Start();
				} catch (Exception e) {
					//APP.ShowToast("该手机不支持硬编码，请等下一版!");
					//closeVideo();
					LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
				}
				break;
			case XMSG.GetNotify://定时判断截图成功后录像
				File efile = new File(_recordfileName + ".bmp");
				if(efile.exists()){
					_encoderQueue.recordFile(_recordfileName + ".h264");
				}else{
					_btnRecord.SetImages(R.drawable.btn_record0, R.drawable.btn_record0);
					_encoderQueue._isRecording = false;
					efile.delete();
				}
				_btnRecord.setClickable(true);
				break;
			case 1002:
				stopEncode();
				changeTextValue();
				break;
			case XMSG.HeartBeat://牛眼心跳 30秒发一次
				checkTimes(AnalogvideoActivity.this);
				if (_bNotify) {
					_handler.sendEmptyMessageDelayed(XMSG.HeartBeat, 30000); //延迟发送
				}
				break;
			}
		}
	}
	
	private boolean _bNotify = false;
	private void startHeartBeat() {
		if (!_bNotify) {
			_handler.sendEmptyMessage(XMSG.HeartBeat);
			_bNotify = true;
		}
	}
	private void stopHeartBeat() {
		try {
			_handler.removeMessages(XMSG.HeartBeat);
			_bNotify = false;
		} catch (Exception e) {
		}
	}
	
	//获取WIFI  SSID 和信号强度
	@SuppressWarnings("static-access")
	public void checkTimes(Activity act) {
		try {
			WifiManager wifiMg = (WifiManager) act.getSystemService(act.WIFI_SERVICE);
			List<ScanResult> list = wifiMg.getScanResults();
			if (list != null) {
				//LogUtil.d(TAG, "list size : " + list.size());
//				if (list != null) {
//					for (ScanResult scanResult : list) {
//						int nSigLevel = WifiManager.calculateSignalLevel(scanResult.level, 100);
//						LogUtil.d(TAG, "SSID:" + scanResult.SSID + "    强度:"+ scanResult.level + "-" + nSigLevel);
//					}
//				}
				WifiInfo wifiInfo = wifiMg.getConnectionInfo();
				int nWSig = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100);
//				LogUtil.d(TAG, "new SSID : " + wifiInfo.getSSID()+ "   signal strength : " + wifiInfo.getRssi() + "   强度:" + nWSig);
				String ssid = wifiInfo.getSSID();
				if(Constants.netWakeType != 1){
					ssid = "";
					nWSig = 0;
				}
				SDK.HeartBeat(Constants.netWakeType, ssid, nWSig, nlatitude, nlongitude);
			} else {
				LogUtil.d(TAG, "list is null");
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}

	} 
	
	private String location,nlatitude="",nlongitude="";//返回字符串，纬度，经度
	private JSONObject locationJson;
	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(this);
			location = intent.getExtras().getString("location");
			//Log.d(TAG, "json:" + location);
			try {
				//{"address":"中国浙江省杭州市余杭区荆长路646","longitude":120.040663,"latitude":30.291106}
				locationJson = new JSONObject(location);
				nlongitude = locationJson.getString("longitude");
				nlatitude = locationJson.getString("latitude");
			} catch (JSONException e) {
				LogUtil.d(TAG, "parse location :" + e.getMessage());
			}
		}
	};
	IntentFilter filter = new IntentFilter("com.views.NewMainAddDev");
	



}
