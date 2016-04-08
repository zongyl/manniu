package com.views.analog.camera.encode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basic.APP;
import com.basic.XMSG;
import com.ctrl.SysPopupMenu;
import com.ctrl.XImageBtn;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.BaseApplication;
import com.views.analog.camera.audio.AudioRecorder;
import com.views.bovine.Fun_AnalogVideo;

/**
 * @author: li_jianhua Date: 2015-4-23 上午9:59:44
 * To change this template use File | Settings | File Templates.
 * @Description：  模拟IPC  只采集、并显示预览 软编码方式
 */
@SuppressLint("NewApi")
public class AnalogvideoActivity extends Activity implements SurfaceHolder.Callback,OnClickListener{
	private String TAG = AnalogvideoActivity.class.getSimpleName();
	public static AnalogvideoActivity instance = null;
	DatagramSocket socket;
	InetAddress address;
	XImageBtn _btnPic,_btnRecord,_btnStop;
	EncoderDebugger avcCodec;//视频编码
    public Camera m_camera;  
    SurfaceView   m_prevewview;
    SurfaceHolder m_surfaceHolder;
    public static SurfaceTexture surfaceTexture = new SurfaceTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);//不显示预览获取到数据帧
    public int _width,_height;
    LinearLayout _layout;
    Handler handler = new Handler();
    TextView _tooltip;
    //private Context context;
//    public TalkPlayer _talkPlayer;		// 音频硬编码
    public AudioRecorder _talkPlayer; //AAC编码
    
    
    private AnalogHandler _handler = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置全屏
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		instance = this;
		//context = this.getApplicationContext();
		BaseApplication.getInstance().addActivity(this);
		_btnPic = (XImageBtn) this.findViewById(R.id.btnPic);
		_btnRecord = (XImageBtn) this.findViewById(R.id.btnRecord);
		_btnStop = (XImageBtn) this.findViewById(R.id.btnStop);
		_btnPic.setOnClickListener(this);
		_btnRecord.setOnClickListener(this);
		_btnStop.setOnClickListener(this);
		
		_layout = (LinearLayout) findViewById(R.id.layout_memo);
		_tooltip = (TextView) this.findViewById(R.id.tooltip);
		
		m_prevewview = (SurfaceView) findViewById(R.id.SurfaceViewPlay);
		m_surfaceHolder = m_prevewview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
		// 设置该SurfaceView自己不维护缓冲    
		m_surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		m_surfaceHolder.addCallback((Callback) this);
		m_prevewview.setOnClickListener(this);
		
//		decodeView = (SurfaceView) findViewById(R.id.decode_sv);
//		_holderImg = decodeView.getHolder();
//		_holderImg.addCallback((Callback) this);
		
//		_width = 352;
//		_height = 288;
		avcCodec = new EncoderDebugger(m_prevewview,_width,_height);
		_talkPlayer = new AudioRecorder();
//		_talkPlayer = new TalkPlayer();
		
		show(_layout);
		_handler = new AnalogHandler();
		
	}
	
	//截图功能
	@SuppressLint("SimpleDateFormat")
	public void snapPic(Bitmap bitmap,String fileName) {
		BitmapUtils.CompressToFile(bitmap, fileName,100);
		File file = new File(avcCodec.fileName);
		if(file.isFile() && file.exists()){
			Toast.makeText(this,getString(R.string.Video_snap_success), Toast.LENGTH_SHORT).show();
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
		String fileName = path + strDate + getString(R.string.analog_Video) + ".bmp";
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
		if(SDK._sessionIdContext == 0){
			new AlertDialog.Builder(this).setTitle(getString(R.string.prompt_title))
			.setMessage(getString(R.string.areyousure_tocloseanalogIPC))
			.setIcon(R.drawable.help)
			.setPositiveButton(getString(R.string.btn_OK_caption), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					LogUtil.d(TAG, "模拟端点击关闭视频。..start..");
					avcCodec.destroyCamera();
					//先把模拟的登出
			    	if(APP.loginRet == 0){
						int ret = SDK.LogoutIdm();
						if(ret != 0)
							APP.ShowToast(SDK.GetErrorStr(ret));
					}
			    	LogUtil.d(TAG, "SDK.Logout()... success");
					//退出之前 在登录IPC的
					APP.ipcLogin();
					AnalogvideoActivity.this.finish();
				}
			}).setNegativeButton(getString(R.string.btn_Cancel_caption), null).show();
		}else{
			APP.ShowToast(getString(R.string.analogvideocaptureing_CantExit));
		}
	}
    
    //注销编码器
    public void stopEncode(){
    	if(avcCodec != null){
    		avcCodec.close();
    	}
    	if(_talkPlayer != null) _talkPlayer.Stop();
    	//changeTextValue();
    }
    //开始编码
    public void startEncode(){
    	try {
			avcCodec.encodeWithMediaCodecMethod1();
			_talkPlayer.Start();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	protected void onStop() {
		super.onStop();
	}
	
	protected void onDestroy() {
		avcCodec.destroyCamera();
		super.onDestroy();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		System.out.println("surfaceChanged");
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			avcCodec.createCamera();
			int ret = avcCodec.updateCamera();
			if(ret != 0){
				APP.ShowToast(getString(R.string.openCamera_error));
				this.finish();
			}
			avcCodec.measureFramerate();
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
	}
	
	public void changeTextValue(){
		Message msg = new Message();
		msg.what = XMSG.CHANGE_TEXTVALUE;
		if(SDK._sessionIdContext != 0){
			msg.obj = "  "+getString(R.string.captureing);
			_handler.sendMessageDelayed(msg, 300);
		}else{
			msg.obj = "  "+getString(R.string.connectclosed_preview);
			_handler.sendMessageDelayed(msg, 300);
			
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPic:
			avcCodec.startSnap = true;
			avcCodec.fileName = getFileName();
			
			//_talkPlayer.play();
			
		    
			break;
		case R.id.btnRecord:
//			try {
//				recorder.start();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			/*//用MediaRecorder 录像
			RecordThread thread = new RecordThread(m_prevewview,m_surfaceHolder);
			// 如果正在录像，则停止
			if (thread._isRecording) {
				APP.ShowToast("录像结束!");
				thread.stopRecord();
			} else {	// 开始录像
				String strDate = DateUtil.getCurrentStringDate(DateUtil.DEFAULT_DATE_TIME_FORMAT);
				String fileName = Fun_Setting.RecordPath + strDate + "模拟IPC";
				File dir = new File(Fun_Setting.RecordPath.substring(0,Fun_Setting.RecordPath.length()-1));
				if(!dir.exists()) dir.mkdirs();
				if((int)SdCardUtils.getSurplusStorageSize(Fun_Setting.RecordPath) > 20){
//					avcCodec.fileName = fileName + ".bmp";
//					avcCodec.startSnap = true;
//					File efile = new File(fileName + ".bmp");
//					if(efile.exists()){
						thread._recordFileName = fileName + ".mp4";
						thread.start();
//					}else
//						break;
				}else{
					APP.ShowToast("剩余存储空间不足!");
				}
			}*/
			
			
			break;
		case R.id.btnStop:
			//创建下拉菜单
			closeVideo();
			//createMenu(v);
			break;
			
		case R.id.SurfaceViewPlay:
			show(_layout);
			break;

		default:
			break;
		}
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
			}
		}
	}



}
