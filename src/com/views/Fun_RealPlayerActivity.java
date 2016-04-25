package com.views;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.adapter.ChannelPageAdapter;
import com.basic.APP;
import com.basic.OnDoubleClick;
import com.basic.XMSG;
import com.bean.DevCart;
import com.bean.Device;
import com.bean.Monitor;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.DateUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.analog.camera.encode.DecoderDebugger;
import com.views.bovine.Fun_AnalogVideo;
import P2P.SDK;
import P2P.ViESurfaceRenderer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;

/**
 * @author: li_jianhua Date: 2016-3-29 上午10:48:22
 * To change this template use File | Settings | File Templates.
 * Description：多画面 实时视频
 */

@SuppressLint("DefaultLocale")
public class Fun_RealPlayerActivity extends Activity{
	private String TAG = Fun_RealPlayerActivity.class.getSimpleName();
	public static Fun_RealPlayerActivity instance = null;
	private int freamtypeType = -1;// freamtypeType=12:h265 2: h264
	private final int MAX_PLAYVIEWNUM = 4;
	private final int MAX_IMG_BUFFER_SIZE =2560*1600*3;//704*576*3;//
	private final int MIN_IMG_BUFFER_SIZE=1280*720*3;//1280*720*3;
	private final int INIT_SINGLE_CONTROLS = 1;//单画面：打开进度条
	private final int PLAYING_SINGLE_CONTROLS = 2;//单画面：隐藏加载进度条
	private final int STOP_SINGLE_CONTROLS = 3;
//	private final int PLAYING_SINGLE_BUTTONS = 4;
//	private final int FAIL_PLAYING_SINGLE_BUTTONS = 5;
//	private final int STOP_SINGLE_BUTTONS = 6;
	private final int INIT_MULTI_CONTROLS = 11;
	private final int INIT_EACHVIEW_CONTROLS = 12;//多画面：打开进度条
	private final int PLAYING_EACHVIEW_CONTROLS = 13;//多画面：隐藏加载进度条
	private final int STOP_EACHVIEW_CONTROLS = 14;
	private final int CLOSE_EACHVIEW_CONTROLS = 15;
	private final int PLAYING_EACHVIEW_BUTTONS = 16;
	private final int STOP_EACHVIEW_BUTTONS = 17;
	private final int SHOW_LOGINTODEVICE_STATE = 21;
	private final int SHOW_CONNECTSUBTODEVICE_STATE = 22;
	private final int SHOW_CONNECTVIDEOTODEVICE_STATE = 23;
	private final int SHOW_REQUESTVIDEOTODEVICE_STATE = 24;
	private final int ENABLE_PLAYBUTTON = 25;
	private Button _btnBack;
	/** 切换通道的视图 */
	private ViewPager m_channelsPager = null;
	List<View> m_channlespager_viewsList = null;
	/** 码流统计 */
	private TextView m_dataFlowView = null;
	/** 底部按钮 */
	private int m_choseViewIndex = 0;
	private View m_toolView01 = null;
	private View m_toolView02 = null;
	private View m_toolView03 = null;
	private View m_toolView04 = null;
	private View m_toolView05 = null;
	private View m_toolView06 = null;
	/** 单路播放UI */
	View m_singlePlayView = null;
	/** 多路播放UI */
	View m_multiPlayView = null;
	/** 单路绘图视图 */
	SurfaceView m_singleSurface = null;
	int _verticalScreenWidth = 0,_verticalScreenHeigth = 0 ;//竖屏宽
	/** 多路播放子视图 */
	View m_multiPlayView01 = null;
	View m_multiPlayView02 = null;
	View m_multiPlayView03 = null;
	View m_multiPlayView04 = null;
	/** 多路绘图视图 */
	SurfaceView m_surface01 = null;
	SurfaceView m_surface02 = null;
	SurfaceView m_surface03 = null;
	SurfaceView m_surface04 = null;
	
	/** 关闭按钮 */
	ImageButton m_closebButton = null;
	/** 声音按钮 */
	ImageButton m_audioButton = null;
	/** 切换主码流 */
	TextView m_mainStream = null;
	/** 切换辅码流 */
	TextView m_subStream = null;
	/** 状态文字 */
	TextView m_singleDetailText = null;
	/** 刷新设备按钮 */
	ImageButton m_singleRefreshButton = null;
	/** 加载图标 */
	ProgressBar m_singleProgressBar = null;
	/** rec图标 */
	ImageView m_singleRECView = null;
	/** 状态文字 */
	TextView m_multiDetailText01 = null;
	/** 刷新设备按钮 */
	ImageButton m_multiRefreshButton01 = null;
	/** 加载图标 */
	ProgressBar m_multiProgressBar01 = null;
	/** rec图标 */
	ImageView m_multiRECView01 = null;
	/** 状态文字 */
	TextView m_multiDetailText02 = null;
	/** 刷新设备按钮 */
	ImageButton m_multiRefreshButton02 = null;
	/** 加载图标 */
	ProgressBar m_multiProgressBar02 = null;
	/** rec图标 */
	ImageView m_multiRECView02 = null;
	/** 状态文字 */
	TextView m_multiDetailText03 = null;
	/** 刷新设备按钮 */
	ImageButton m_multiRefreshButton03 = null;
	/** 加载图标 */
	ProgressBar m_multiProgressBar03 = null;
	/** rec图标 */
	ImageView m_multiRECView03 = null;
	/** 状态文字 */
	TextView m_multiDetailText04 = null;
	/** 刷新设备按钮 */
	ImageButton m_multiRefreshButton04 = null;
	/** 加载图标 */
	ProgressBar m_multiProgressBar04 = null;
	/** rec图标 */
	ImageView m_multiRECView04 = null;
	/** 新增画面图标 */
	ImageView m_addchannel = null;
	ImageView m_addchannel01 = null;
	ImageView m_addchannel02 = null;
	ImageView m_addchannel03 = null;
	ImageView m_addchannel04 = null;
	/** 是否退出 */
	boolean m_isExit = false;
	boolean m_hadBack = false;
	/** 是否在录像 */
	boolean m_isRecord = false;
	/** 当前的设备路数 */
	int m_devNum = 0;
	/** 当前通道信息 */
	List<DevCart> m_devCartsList = null;
	/**
	 * 单路时的Monitor对象
	 * 
	 * @return 登录：-1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -4:主连接已存在（已登录或者正在登录中）
	 *         -11:登录数据发送失败 -12:登录超时 -21:密码错误 -22:设备锁定 -23:账号不存在 -24:账号已登录
	 *         通道连接：-1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -11:注册数据发送失败 -12:注册回调超时
	 *         1:注册成功 -21:注册失败（已存在） 请求视频请求 -1:主连接不存在 -2:通道连接不存在 -11:请求视频数据发送失败
	 *         -12:请求视频回调超时 1:请求视频成功 -21:请求视频失败
	 */
	Monitor m_singleMonitor = null;
	/**
	 * 多路时的Monitor对象
	 * 
	 * @return 登录：-1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -4:主连接已存在（已登录或者正在登录中）
	 *         -11:登录数据发送失败 -12:登录超时 -21:密码错误 -22:设备锁定 -23:账号不存在 -24:账号已登录
	 *         通道连接：-1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -11:注册数据发送失败 -12:注册回调超时
	 *         1:注册成功 -21:注册失败（已存在） 请求视频请求 -1:主连接不存在 -2:通道连接不存在 -11:请求视频数据发送失败
	 *         -12:请求视频回调超时 1:请求视频成功 -21:请求视频失败
	 */
	public List<Monitor> m_monitorsList = null;
	public Monitor m_chooseMonitor = null;
	/**
	 * 正在录制视频的monitor
	 */
	Monitor record_monitor = null;
	public boolean m_isFullView = false;//是否全屏
	Rect m_fullViewRect = null;
	/**
	 * 正在录制视频的index
	 */
	int record_index = 0;
	public int m_chooseIndex = 0;
	/**是否按下截图按钮*/
	public boolean isShot=false;
	/**截图失败尝试次数*/
	private int screenCount=0;
	
	/** 进读条是否显示 */
	private int progress_show[] = new int[5];
	/** 刷新按钮是否显示 */
	private int refresh_show[] = new int[5];
	/** 状态文字是否显示 */
	private int textview_show[] = new int[5];
	/** 新增通道按钮是否显示 */
	private int addbutton_show[] = new int[5];
	/** 状态文字显示内容 */
	private String text[] = new String[5];
	private boolean isinit = false;
	/**是否已点击新增通道按钮*/
	private boolean isAddChannel=false;
	/* 设置相关 */
	//SettingInfo m_setInfo = null;
	/** 是否开启报警 */
	int m_isOpenAlram;
	/** 是否开启视频声音 */
	int m_isOpenAudio;
	/** 是否开启报警音 */
	int m_isOpenAlarmAudio;
	/** 是否开启遥控器震动 */
	int m_isRemoteShake;
	/** 是否为辅码流 */
	int m_isSubStream;
	/** 是否存入系统相册 */
	int m_isSaveToAlbum;
	/** 是否开启语音对讲 */
	int m_isOpenVoiceTalk;
	/**硬解码器*/
	private DecoderDebugger m_avdecoder;
	/**SDK版本号*/
	private int sdk_int;
	int newWidth;
	int newHeight;
	/** 是否开启语音对讲 */
	private boolean isRecording = false;
	/** 语音对讲Monitor */
	private Monitor talkMonitor;
	/**语音对讲音频解码器*/
	private AudioTrack talk_audioTrack;
	/**开启语音对讲时是否已关闭原有打开的音频*/
	private boolean audioIsChanged=false;
	/** 通道选择适配器 */
	private ChannelPageAdapter channelPageAdapter;
	//private boolean _isGpu = true;//软硬切换 默认走硬解
	FrameLayout _hreadframeLayout,_playview4ui;//标题头
	LinearLayout _bottomMemo,_playview01and02,_playview03and04;//底部菜单
	//LinearLayout _LinearLayout4ui;
	RelativeLayout _middleMenu;//中间菜单
	LinearLayout.LayoutParams params;
	
	/** 码流相关 总流量 */
	long m_totalDataFlow = 0;
	/* 线程开关 */
	/** 是否运行码流线程 */
	boolean m_isRunDataSpeedThread;
	/** 是否暂停码流线程 */
	boolean m_isPauseDataThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		instance = this;
		sdk_int=android.os.Build.VERSION.SDK_INT;
		
//		Configuration mConfiguration = this.getResources().getConfiguration(); // 获取设置的配置信息
//		int ori = mConfiguration.orientation; // 获取屏幕方向
//		if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.activity_realplayer_port);
			// 获取控件
			findViews();
			// 绑定事件
			initListener();
//		} else {
//			getResolution();
//		}
			
		_hreadframeLayout = (FrameLayout) this.findViewById(R.id.hhheader);
		_playview4ui = (FrameLayout) this.findViewById(R.id.frame_playview_4ui);
		params = (LinearLayout.LayoutParams)_playview4ui.getLayoutParams();
		_bottomMemo = (LinearLayout) this.findViewById(R.id.realplayer_bottom_tool);
		_middleMenu = (RelativeLayout) this.findViewById(R.id.center_menu);
		_playview01and02 = (LinearLayout) this.findViewById(R.id.playview01and02);
		_playview03and04 = (LinearLayout) this.findViewById(R.id.playview03and04);
		//_LinearLayout4ui = (LinearLayout) this.findViewById(R.id.LinearLayout_4ui);
		
		
		_btnBack = (Button) this.findViewById(R.id.btn_back_video);
		_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitDeleteDevice();
			}
		});
		
		Serializable lstRealPlayerDevices = getIntent().getSerializableExtra("RealPlayer_devices");
		if(lstRealPlayerDevices != null){
			List<DevCart> devCartsList = (List<DevCart>) getIntent().getSerializableExtra("RealPlayer_devices");
			m_devCartsList = devCartsList;
			m_devNum = m_devCartsList.size();
		}else{
			m_devCartsList = new ArrayList<DevCart>();
			m_devNum = 0;
		}
		// 初始化相关播放控件
		initAllControls();
		isinit = true;
		// 新增通道按钮监听
		addChannelClick();
		
		/* 设置相关 */
//		m_setInfo = SettingDBManager.getItem();
//		m_isOpenAlram = m_setInfo.getAlarmContent();
//		m_isOpenAudio = m_setInfo.getMonitorAudio();
//		m_isOpenAlarmAudio = m_setInfo.getAlarmAudio();
//		m_isRemoteShake = m_setInfo.getRemote();
//		m_isSubStream = m_setInfo.getStream();
//		m_isSaveToAlbum = m_setInfo.getSaveAlbum();
		
//		if (m_isOpenAudio == 1) {
//			if(ori == mConfiguration.ORIENTATION_PORTRAIT)
//				selectAudioButton();
//			else
//				selectAudioButtonLand();
//		} else {
//			if(ori == mConfiguration.ORIENTATION_PORTRAIT)
//				disselectAudioButton();
//			else
//				disselectAudioButtonLand();
//		}
		
		// 判断当前为单画面还是多画面
		if (m_devCartsList.size() == 1) {
			m_devNum = 1;
			m_singlePlayView.setVisibility(View.VISIBLE);
			m_multiPlayView.setVisibility(View.GONE);
			// 隐藏REC图标
			hideRECImage(-1);

			/*
			 * if(android.os.Build.VERSION.SDK_INT>=16){ mediaThread.start(); }
			 */
			m_singleMonitor = new Monitor();
			m_singleMonitor.setDevCart(m_devCartsList.get(0));
			m_singleMonitor.setIsSubStream(m_isSubStream);
			m_singleMonitor.setDisplayStream(m_isSubStream);
			m_singleMonitor.setTileView(m_singleSurface);
			//m_singleMonitor.setDecoder(decoder);
			// 分配内存报错
			try {
				m_singleMonitor.setImageData(new byte[MAX_IMG_BUFFER_SIZE]);
			} catch (OutOfMemoryError e) {
				//FLAlertView.showAlertDialog(this, R.string.toast_oom);
				APP.ShowToast(getText(R.string.toast_oom).toString());
				return;
			}

			chooseStreamButton(m_isSubStream);
			m_chooseMonitor = m_singleMonitor;
			if (sdk_int >= 16)
				m_singleSurface.getHolder().addCallback(new MSurfaceCallback(0));
			// 获取画布的尺寸
			m_singleSurface.getViewTreeObserver().addOnPreDrawListener(
					new OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {
							int eventwidth = m_singleSurface.getMeasuredWidth();
							int eventheight = m_singleSurface.getMeasuredHeight();
							newWidth = eventwidth;
							newHeight = eventheight;
							m_singleMonitor.setDrawRect(new Rect(0, 0,eventwidth, eventheight));
							m_singleSurface.getViewTreeObserver().removeOnPreDrawListener(this);
							_verticalScreenWidth = _playview4ui.getWidth();
							_verticalScreenHeigth = _playview4ui.getHeight();
							System.out.println(_playview4ui.getWidth()+"--"+_playview4ui.getHeight());
							return true;
						}
					});
			m_dataFlowView.setText(m_singleMonitor.getDevCart().getDeviceInfo().getDevname());
			if (m_singleMonitor.getDevCart() != null)
				initChannelsView(Integer.valueOf(m_singleMonitor.getDevCart().getDeviceInfo().channels));
			m_channelsPager.setVisibility(View.GONE);
			// 请求视频
			startSingleLoginRequestVideoThread();
		}else{
			m_devNum = m_devCartsList.size();
			m_singlePlayView.setVisibility(View.GONE);
			m_multiPlayView.setVisibility(View.VISIBLE);
			IsSingleViewType = false;
			// 隐藏REC图标
			hideRECImage(-1);
			hideRECImage(1);
			hideRECImage(2);
			hideRECImage(3);
			hideRECImage(4);
			
			for (int i = m_devNum + 1; i <= MAX_PLAYVIEWNUM; i++)
				showAddButton(i);
			
			m_monitorsList = new ArrayList<Monitor>();
			for (int i = 0; i < MAX_PLAYVIEWNUM; i++) {
				Monitor monitor = new Monitor();
				// 分配内存报错
				try {
					monitor.setImageData(new byte[MIN_IMG_BUFFER_SIZE]);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					showAlertDialog(R.string.toast_oom);
					return;
				}
				//多路硬解只有辅码流
				monitor.setIsSubStream(1);
				monitor.setDisplayStream(m_isSubStream);

				switch (i) {
				case 0: {
					monitor.setTileView(m_surface01);
					if (sdk_int >= 16)					
						m_surface01.getHolder().addCallback(
								new MSurfaceCallback(1));									
					// 获取画布的尺寸
					m_surface01.getViewTreeObserver().addOnPreDrawListener(
							new OnSurfacePreDrawListener(monitor, m_surface01));
				}
					break;
				case 1: {
					monitor.setTileView(m_surface02);
					if (sdk_int >= 16)						
						m_surface02.getHolder().addCallback(
								new MSurfaceCallback(2));
					// 获取画布的尺寸
					m_surface02.getViewTreeObserver().addOnPreDrawListener(
							new OnSurfacePreDrawListener(monitor, m_surface02));
				}
					break;
				case 2: {
					monitor.setTileView(m_surface03);
					if (sdk_int >= 16)
						m_surface03.getHolder().addCallback(
								new MSurfaceCallback(3));
					// 获取画布的尺寸
					m_surface03.getViewTreeObserver().addOnPreDrawListener(
							new OnSurfacePreDrawListener(monitor, m_surface03));
				}
					break;
				case 3: {
					monitor.setTileView(m_surface04);
					if (sdk_int >= 16)
						m_surface04.getHolder().addCallback(
								new MSurfaceCallback(4));
					// 获取画布的尺寸
					m_surface04.getViewTreeObserver().addOnPreDrawListener(
							new OnSurfacePreDrawListener(monitor, m_surface04));
				}
					break;

				default:
					break;
				}
				m_monitorsList.add(monitor);
			}
			for (int i = 0; i < m_devCartsList.size(); i++) {
				Monitor monitor = m_monitorsList.get(i);
				// 设置monitor已加载过视频
				monitor.setLoad_status(1);
				// 解码器
//				long decoder = H264Dec.InitDecoder();
				monitor.setDevCart(m_devCartsList.get(i));
//				monitor.setDecoder(decoder);
			}
			
			if (sdk_int >= 16)
				m_singleSurface.getHolder().addCallback(new MSurfaceCallback(0));
			// 获取画布的尺寸
			m_singleSurface.getViewTreeObserver().addOnPreDrawListener(
					new OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {
							int eventwidth = m_singleSurface.getMeasuredWidth();
							newWidth = eventwidth;
							int eventheight = m_singleSurface.getMeasuredHeight();
							newHeight = eventheight;
							m_fullViewRect = new Rect(0, 0, eventwidth,eventheight);
							return true;
						}
					});

			startMultiLoginRequestVideoThread();

			// 默认选择第一个监视器
			defaultChoose();
		}
		startRefreshDataSpeedInfoThread();
		//暂时屏蔽主码流和辅码的选择按钮
		m_mainStream.setVisibility(View.GONE);
		m_subStream.setVisibility(View.GONE);
		
	}
	
	
	/** 定时刷新码流线程 */
	public void startRefreshDataSpeedInfoThread() {
		new Thread() {
			@Override
			public void run() {
				runRefreshDataSpeedInfoThread();
			}
		}.start();
	}
	
	/** 定时刷新码流信息 */
	private void runRefreshDataSpeedInfoThread() {
		if (m_devNum == 1) {
			m_isRunDataSpeedThread = true;
			while (m_isRunDataSpeedThread) {
				// 间隔时间
				int try_count = 0;
				while (true) {
					while (m_isPauseDataThread && m_isRunDataSpeedThread) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (m_isRunDataSpeedThread == false) {
						return;
					}

					if (try_count == 10) {
						break;
					}

					try_count++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// 获取当前的通道的总流量
				long dataFlow = m_singleMonitor.curChnDataFlow;
				// 计算每秒当前通道的流量
				long dataFlowEachSecond = 0;
				if (dataFlow > m_singleMonitor.videoDataFlow) {
					dataFlowEachSecond = dataFlow
							- m_singleMonitor.videoDataFlow;
				}
				// 计算总流量
				m_totalDataFlow += dataFlowEachSecond;

				// 记录前 6 秒的数据流量
				if (m_singleMonitor.dataFlowArray.size() > 5) {
					m_singleMonitor.dataFlowArray.remove(0);
				}
				m_singleMonitor.dataFlowArray.add(dataFlowEachSecond);

				long dataFlowBefore = 0;
				for (int i = 0; i < m_singleMonitor.dataFlowArray.size(); i++) {
					dataFlowBefore += m_singleMonitor.dataFlowArray.get(i);
				}

				dataFlowEachSecond = dataFlowBefore
						/ m_singleMonitor.dataFlowArray.size();

				double dataMB = (double) (m_totalDataFlow / 1024) / 1024;
				double curVideoBPS = dataFlowEachSecond;

				final String dataStr = String.format(
						"Rate: %2.2fKbps,Total:%.2fMB", curVideoBPS * 8 / 1024,
						dataMB);

				Message msg = new Message();
				msg.what = 0;
				Bundle bundle = new Bundle();
				bundle.putString("dataStr", dataStr);
				msg.setData(bundle);
				refreshSpeedHandler.sendMessage(msg);

				m_singleMonitor.videoDataFlow = dataFlow;
			}
		} else {
			m_isRunDataSpeedThread = true;
			while (m_isRunDataSpeedThread) {
				// 间隔时间
				int try_count = 0;
				while (true) {
					while (m_isPauseDataThread && m_isRunDataSpeedThread) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (m_isRunDataSpeedThread == false) {
						return;
					}

					if (try_count == 10) {
						break;
					}

					try_count++;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// 获取当前的通道的总流量
				long dataFlow1 = m_monitorsList.get(0).curChnDataFlow;
				long dataFlow2 = m_monitorsList.get(1).curChnDataFlow;
				long dataFlow3 = m_monitorsList.get(2).curChnDataFlow;
				long dataFlow4 = m_monitorsList.get(3).curChnDataFlow;
				long videoDataFlow1 = m_monitorsList.get(0).videoDataFlow;
				long videoDataFlow2 = m_monitorsList.get(1).videoDataFlow;
				long videoDataFlow3 = m_monitorsList.get(2).videoDataFlow;
				long videoDataFlow4 = m_monitorsList.get(3).videoDataFlow;
				// 计算每秒当前通道的流量
				long dataFlowEachSecond1 = 0;
				long dataFlowEachSecond2 = 0;
				long dataFlowEachSecond3 = 0;
				long dataFlowEachSecond4 = 0;
				if (dataFlow1 > videoDataFlow1) {
					dataFlowEachSecond1 = dataFlow1 - videoDataFlow1;
				}
				if (dataFlow2 > videoDataFlow2) {
					dataFlowEachSecond2 = dataFlow2 - videoDataFlow2;
				}
				if (dataFlow3 > videoDataFlow3) {
					dataFlowEachSecond3 = dataFlow3 - videoDataFlow3;
				}
				if (dataFlow4 > videoDataFlow4) {
					dataFlowEachSecond4 = dataFlow4 - videoDataFlow4;
				}
				// 计算总流量
				m_totalDataFlow += dataFlowEachSecond1;
				m_totalDataFlow += dataFlowEachSecond2;
				m_totalDataFlow += dataFlowEachSecond3;
				m_totalDataFlow += dataFlowEachSecond4;

				for (Monitor monitor : m_monitorsList) {
					// 获取当前的通道的总流量
					long dataFlow = monitor.curChnDataFlow;
					// 计算每秒当前通道的流量
					long dataFlowEachSecond = 0;
					if (dataFlow > monitor.videoDataFlow) {
						dataFlowEachSecond = dataFlow - monitor.videoDataFlow;
					}

					List<Long> dataFlowArray = monitor.dataFlowArray;
					// 记录前 6 秒的数据流量
					if (dataFlowArray.size() > 5) {
						dataFlowArray.remove(0);
					}
					dataFlowArray.add(dataFlowEachSecond);
					
					long dataFlowBefore = 0;
					for (int i = 0; i < dataFlowArray.size(); i++) {
						dataFlowBefore += dataFlowArray.get(i);
					}
					dataFlowEachSecond = dataFlowBefore / dataFlowArray.size();

					monitor.dataFlowEachSecond = dataFlowEachSecond;
				}

				double dataMB = (double) (m_totalDataFlow / 1024) / 1024;
				double curVideoBPS = 0.0f;

				if (m_chooseMonitor != null) {
					curVideoBPS = m_chooseMonitor.dataFlowEachSecond;
				} else {
					curVideoBPS = dataFlowEachSecond1 + dataFlowEachSecond2
							+ dataFlowEachSecond3 + dataFlowEachSecond4;
				}
				String dataStr = String.format("Rate: %2.2fKbps,Total:%.2fMB",
						curVideoBPS * 8 / 1024, dataMB);

				Message msg = new Message();
				msg.what = 0;
				Bundle bundle = new Bundle();
				bundle.putString("dataStr", dataStr);
				msg.setData(bundle);
				refreshSpeedHandler.sendMessage(msg);

				m_monitorsList.get(0).videoDataFlow = dataFlow1;
				m_monitorsList.get(1).videoDataFlow = dataFlow2;
				m_monitorsList.get(2).videoDataFlow = dataFlow3;
				m_monitorsList.get(3).videoDataFlow = dataFlow4;
			}
		}
	}
	
	Handler refreshSpeedHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg != null) {
				if (msg.getData().containsKey("dataStr")) {
					String dataStr = msg.getData().getString("dataStr");
					if (m_dataFlowView != null)
						m_dataFlowView.setText(dataStr);
				}
			}
		}
	};
	
	
	
	/**
	 * 获取控件
	 */
	private void findViews() {
		m_dataFlowView = (TextView) findViewById(R.id.realplayer_dataflow_txt);
		m_toolView01 = findViewById(R.id.realplayer_tool_layout01);
		m_toolView02 = findViewById(R.id.realplayer_tool_layout02);
		m_toolView03 = findViewById(R.id.realplayer_tool_layout03);
		m_toolView04 = findViewById(R.id.realplayer_tool_layout04);
		m_toolView05 = findViewById(R.id.realplayer_tool_layout05);
		//m_toolView06 = findViewById(R.id.realplayer_tool_layout06);
		m_channelsPager = (ViewPager) findViewById(R.id.realplayer_channels_pager);
		m_singlePlayView = (View) findViewById(R.id.realplayer_singleplayview);
		m_multiPlayView = (View) findViewById(R.id.realplayer_multiplayview);
		m_singleSurface = (SurfaceView) findViewById(R.id.realplayer_singlesurface);
		m_multiPlayView01 = (View) findViewById(R.id.realplayer_multiplayview01);
		m_multiPlayView02 = (View) findViewById(R.id.realplayer_multiplayview02);
		m_multiPlayView03 = (View) findViewById(R.id.realplayer_multiplayview03);
		m_multiPlayView04 = (View) findViewById(R.id.realplayer_multiplayview04);
		m_surface01 = (SurfaceView) findViewById(R.id.realplayer_multisurface01);
		m_surface02 = (SurfaceView) findViewById(R.id.realplayer_multisurface02);
		m_surface03 = (SurfaceView) findViewById(R.id.realplayer_multisurface03);
		m_surface04 = (SurfaceView) findViewById(R.id.realplayer_multisurface04);
		m_closebButton = (ImageButton) findViewById(R.id.realplayer_control_close);
		m_audioButton = (ImageButton) findViewById(R.id.realplayer_control_audio);
		m_mainStream = (TextView) findViewById(R.id.realplayer_control_mainstream);
		m_subStream = (TextView) findViewById(R.id.realplayer_control_substream);
		m_singleDetailText = (TextView) findViewById(R.id.realplayer_single_detailtext);
		m_singleRefreshButton = (ImageButton) findViewById(R.id.realplayer_single_refresh);
		m_singleProgressBar = (ProgressBar) findViewById(R.id.realplayer_single_progress);
		m_singleRECView = (ImageView) findViewById(R.id.realplayer_single_rec_image);
		m_multiDetailText01 = (TextView) findViewById(R.id.realplayer_multi_detailtext01);
		m_multiRefreshButton01 = (ImageButton) findViewById(R.id.realplayer_multi_refresh01);
		m_multiProgressBar01 = (ProgressBar) findViewById(R.id.realplayer_multi_progress01);
		m_multiRECView01 = (ImageView) findViewById(R.id.realplayer_multi_rec_image01);
		m_multiDetailText02 = (TextView) findViewById(R.id.realplayer_multi_detailtext02);
		m_multiRefreshButton02 = (ImageButton) findViewById(R.id.realplayer_multi_refresh02);
		m_multiProgressBar02 = (ProgressBar) findViewById(R.id.realplayer_multi_progress02);
		m_multiRECView02 = (ImageView) findViewById(R.id.realplayer_multi_rec_image02);
		m_multiDetailText03 = (TextView) findViewById(R.id.realplayer_multi_detailtext03);
		m_multiRefreshButton03 = (ImageButton) findViewById(R.id.realplayer_multi_refresh03);
		m_multiProgressBar03 = (ProgressBar) findViewById(R.id.realplayer_multi_progress03);
		m_multiRECView03 = (ImageView) findViewById(R.id.realplayer_multi_rec_image03);
		m_multiDetailText04 = (TextView) findViewById(R.id.realplayer_multi_detailtext04);
		m_multiRefreshButton04 = (ImageButton) findViewById(R.id.realplayer_multi_refresh04);
		m_multiProgressBar04 = (ProgressBar) findViewById(R.id.realplayer_multi_progress04);
		m_multiRECView04 = (ImageView) findViewById(R.id.realplayer_multi_rec_image04);

		m_addchannel = (ImageView) findViewById(R.id.addchannel_activity_realplayer);
		m_addchannel01 = (ImageView) findViewById(R.id.addchannel1_activity_realplayer);
		m_addchannel02 = (ImageView) findViewById(R.id.addchannel2_activity_realplayer);
		m_addchannel03 = (ImageView) findViewById(R.id.addchannel3_activity_realplayer);
		m_addchannel04 = (ImageView) findViewById(R.id.addchannel4_activity_realplayer);

		// 八方向的云台控制
//		relative_ptz_layout = (RelativeLayout) findViewById(R.id.relative_ptz_layout);
//		ptz_left_up = (ImageView) findViewById(R.id.ptz_left_up);
//		ptz_up = (ImageView) findViewById(R.id.ptz_up);
//		ptz_right_up = (ImageView) findViewById(R.id.ptz_right_up);
//		ptz_left = (ImageView) findViewById(R.id.ptz_left);
//		ptz_right = (ImageView) findViewById(R.id.ptz_right);
//		ptz_left_down = (ImageView) findViewById(R.id.ptz_left_down);
//		ptz_down = (ImageView) findViewById(R.id.ptz_down);
//		ptz_right_down = (ImageView) findViewById(R.id.ptz_right_down);
	}
	
	/**
	 * 绑定事件
	 */
	private void initListener() {
		OnToolClickListener toolListener = new OnToolClickListener();
		m_toolView01.setOnClickListener(toolListener);
		m_toolView02.setOnClickListener(toolListener);
		m_toolView03.setOnClickListener(toolListener);
		m_toolView04.setOnClickListener(toolListener);
		m_toolView05.setOnClickListener(toolListener);
		//m_toolView06.setOnClickListener(toolListener);
		OnPlayControlsClickListener playListener = new OnPlayControlsClickListener();
		m_closebButton.setOnClickListener(playListener);//关闭
		m_audioButton.setOnClickListener(playListener);//声音
		//m_favorbButton.setOnClickListener(playListener);//收藏 
		m_mainStream.setOnClickListener(playListener);
		m_subStream.setOnClickListener(playListener);
		OnRefreshButtonClickListener refreshListener = new OnRefreshButtonClickListener();
		m_singleRefreshButton.setOnClickListener(refreshListener);
		m_multiRefreshButton01.setOnClickListener(refreshListener);
		m_multiRefreshButton02.setOnClickListener(refreshListener);
		m_multiRefreshButton03.setOnClickListener(refreshListener);
		m_multiRefreshButton04.setOnClickListener(refreshListener);

		// 手势事件
		OnDoubleClickListener onDoubleClickListener = new OnDoubleClickListener();
		OnDoubleClick.registerDoubleClickListener(m_multiPlayView01,
				onDoubleClickListener);
		OnDoubleClick.registerDoubleClickListener(m_multiPlayView02,
				onDoubleClickListener);
		OnDoubleClick.registerDoubleClickListener(m_multiPlayView03,
				onDoubleClickListener);
		OnDoubleClick.registerDoubleClickListener(m_multiPlayView04,
				onDoubleClickListener);
		if (m_devNum > 1) {
			OnDoubleClick.registerDoubleClickListener(m_singlePlayView,
					new OnFullViewDoubleClickListener());
		}
		//注册手势事件
		//m_singleSurface.setOnTouchListener(new MyOnTouchListener());
	}
	
	private class OnDoubleClickListener implements com.basic.OnDoubleClickListener {
		@Override
		public void OnSingleClick(View v) {
			singlePlayViewEvent(v);
		}
		
		@Override
		public void OnDoubleClick(View v) {
			singlePlayViewEvent(v);
			doublePlayViewEvent(v);
			System.out.println("==========================================");
		}
	}
	
	private class OnFullViewDoubleClickListener implements com.basic.OnDoubleClickListener {
		@Override
		public void OnSingleClick(View v) {
		}
		
		@Override
		public void OnDoubleClick(View v) {
			//narrowPlayView();
		}
	}
	
	/** 刷新通道监听接口 */
	private class OnRefreshButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				switch (v.getId()) {
				case R.id.realplayer_single_refresh: {
					btnRefreshClick(-1);
				}
					break;
				case R.id.realplayer_multi_refresh01: {
					btnRefreshClick(1);
				}
					break;
				case R.id.realplayer_multi_refresh02: {
					btnRefreshClick(2);
				}
					break;
				case R.id.realplayer_multi_refresh03: {
					btnRefreshClick(3);
				}
					break;
				case R.id.realplayer_multi_refresh04: {
					btnRefreshClick(4);
				}
					break;

				default:
					break;
				}
			} catch (Exception e) {
			}
		}
	}
	
	//事件监听-停止按键/等
	private class OnPlayControlsClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.realplayer_control_close: {//停止BTN
				btnCloseClick();
			}
				break;
			/*case R.id.realplayer_control_audio: {
				// 打开声音
				if (m_isOpenAudio == 0) {
					selectAudioButton();
					m_isOpenAudio = 1;
				}
				// 关闭声音
				else {
					disselectAudioButton();
					m_isOpenAudio = 0;
				}

				SettingInfo settingInfo = SettingDBManager.getItem();
				settingInfo.setMonitorAudio(m_isOpenAudio);
				SettingDBManager.updateItem(settingInfo);
			}
				break;
			case R.id.realplayer_control_favor: {
				btnFavorClick();
				Common.getInstance().getFavoriteLayout().reloadFavorListView();
			}
				break;
			case R.id.realplayer_control_mainstream: {
				btnSwitchStreamClick(0);
			}
				break;
			case R.id.realplayer_control_substream: {
				btnSwitchStreamClick(1);
			}*/
			default:
				break;
			}
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getFileName(String devName){
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
	
	//截图功能
	@SuppressLint("SimpleDateFormat")
	public long snapPic(Bitmap bitmap,String fileName) {
		BitmapUtils.saveBitmap(bitmap, fileName);
		File file = new File(fileName);
		if(file.isFile() && file.exists() && file.length() >= 102400){// && file.length() >= 204800 夜间抓图会小于200K 
			if(fileName.indexOf("images") != -1){
				controlHandler.sendEmptyMessage(XMSG.PLAY_SNAP);
			}
		}
		return file.length();
	}
	//软解时截图
	public void screenshot(Bitmap bitmap){
		long fileLong = snapPic(bitmap, _fileName);
		if(fileLong >= 51200){// || NewMain.devType == 4
			isShot = false;
			if(_fileName.indexOf("images") != -1){
				SDK._shotContext = -1;
			}
			//((ImageButton) m_toolView02.findViewById(R.id.realplayer_tool_btn02)).setClickable(true);
		}
	}
	//硬解时截图
	public void h264DecoderSnapImg(final byte[] data,byte[]outBytes,final int len,int width,int height,long context){
		try {
			screenCount++;
			if(screenCount < 10){
				Bitmap m_imageBitmap = BitmapUtils.getScreenBitmap(data, outBytes, len,width,height,context);
				if(m_imageBitmap != null){
					long fileLong = snapPic(m_imageBitmap, _fileName);
					if(fileLong >= 51200){
						isShot = false;
						screenCount=0;
						if(_fileName.indexOf("images") != -1){
							SDK._shotContext = -1;
						}
						m_imageBitmap = null;
						//((ImageButton) m_toolView02.findViewById(R.id.realplayer_tool_btn02)).setClickable(true);
					}
				}
			}else{
				isShot = false;
				screenCount=0;
				controlHandler.sendEmptyMessage(100);
				//((ImageButton) m_toolView02.findViewById(R.id.realplayer_tool_btn02)).setClickable(true);
			}
		} catch (Exception e) {
		}
	}
	
	public String _fileName = "";//截图文件名
	public String _recordfileName = "";//录像文件名
	private int recordCount = 0;//录像中的截图次数判断
	
	//事件监听-底部按键
	private class OnToolClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.realplayer_tool_layout01: {//通道
				if (m_choseViewIndex == R.id.realplayer_tool_layout01) {
					resetToobarButton();
					m_choseViewIndex = 0;
				} else {
					resetToobarButton();
					((ImageButton) m_toolView01.findViewById(R.id.realplayer_tool_btn01))
							.setImageResource(R.drawable.toorbar_chn_selected);
					((TextView) m_toolView01.findViewById(R.id.realplayer_tool_tv01))
							.setTextColor(Color.rgb(255, 255, 255));
					m_channelsPager.setVisibility(View.VISIBLE);
					m_choseViewIndex = R.id.realplayer_tool_layout01;
				}
				//如果正在对讲
				if(isRecording){
					((ImageButton) m_toolView03.findViewById(R.id.realplayer_tool_btn03))
							.setImageResource(R.drawable.toorbar_audio_selected);
					((TextView) m_toolView03.findViewById(R.id.realplayer_tool_tv03))
							.setTextColor(Color.rgb(255, 255, 255));
				}
				//如果正在录像
				if(m_isRecord){
					((ImageButton) m_toolView05.findViewById(R.id.realplayer_tool_btn05))
							.setImageResource(R.drawable.toorbar_video_selected);
					((TextView) m_toolView05.findViewById(R.id.realplayer_tool_tv05))
							.setTextColor(Color.rgb(255, 255, 255));
				}
			}
				break;
			case R.id.realplayer_tool_layout02: {// 截图
				Monitor monitor = null;
				if (m_devNum == 1)
					monitor = m_singleMonitor;
				else
					monitor = m_chooseMonitor;

				if (monitor.getPlay_status() != 1) {
					Toast.makeText(Fun_RealPlayerActivity.this,R.string.Video_snap_error, Toast.LENGTH_SHORT).show();
				} else {
					Device devInfo = monitor.getDevCart().getDeviceInfo();
					//((ImageButton) m_toolView02.findViewById(R.id.realplayer_tool_btn02)).setClickable(false);
					SDK._shotContext = SDK.GetChannelPlayContext(devInfo.sid, monitor.getDevCart().getChannelNum()-1);
					_fileName = getFileName(devInfo.devname);
					isShot = true;
				}
			}
				break;
			case R.id.realplayer_tool_layout05: // 录像
				if (m_chooseMonitor.getCanRecord()) {
					if (m_devNum == 1) {
						if (12 == freamtypeType) {
							Toast.makeText(Fun_RealPlayerActivity.this,R.string.alertMsg56, 0).show();
						} else {
							btnRecordClick();
						}

					} else {
						if (m_chooseMonitor.getCodectype() == 12) {
							Toast.makeText(Fun_RealPlayerActivity.this,R.string.alertMsg56, 0).show();
						} else {
							btnRecordClick();
						}
					}
				}
				break;
			/*case R.id.realplayer_tool_layout06: // 软硬解切换
				Monitor monitor = null;
				if(m_devNum==1)
					monitor=m_singleMonitor;
				else 
					monitor=m_chooseMonitor;
				break;*/
			default:
				break;
			}
		}

	}
	boolean _bNotify = false;
	private void startNotify(int type) {
		if (!_bNotify) {
			controlHandler.sendEmptyMessage(type);
			_bNotify = true;
		}
	}
	private void stopNotify() {
		_bNotify = false;
	}
	
	//点击录像
	public void btnRecordClick() {
		if (m_devNum == 1) {
			Monitor monitor = m_singleMonitor;
			Device devInfo = monitor.getDevCart().getDeviceInfo();
			// 开始录制
			if (!m_isRecord) {
				if (monitor.getPlay_status() != 1) {
					Toast.makeText(Fun_RealPlayerActivity.this,R.string.Video_record_error, Toast.LENGTH_SHORT).show();
					return;
				}
				SDK._shotContext = SDK.GetChannelPlayContext(devInfo.sid, monitor.getDevCart().getChannelNum()-1);
				
				String strDate = DateUtil.getCurrentStringDate(DateUtil.DEFAULT_DATE_TIME_FORMAT);
				_recordfileName = Fun_AnalogVideo.RecordPath + strDate + devInfo.devname;
				File dir = new File(Fun_AnalogVideo.RecordPath.substring(0,Fun_AnalogVideo.RecordPath.length()-1));
				if(!dir.exists()) dir.mkdirs();
				if((int)SdCardUtils.getSurplusStorageSize(Fun_AnalogVideo.RecordPath) > 20){
					_fileName = _recordfileName + ".bmp";
					isShot = true;
					startNotify(101);
				}else{
					APP.ShowToast(getText(R.string.Video_Storage_space_err).toString());
				}
				m_isRecord = true;
				m_singleMonitor.setRecord(true);// *
				showRECImage(-1);
			} else if (m_isRecord) {// 停止录制
				SDK.SetFinishVideo(_recordfileName + ".mp4",SDK._shotContext);
				m_isRecord = false;
				SDK._shotContext = -1;
				m_singleMonitor.setRecord(false);
				hideRECImage(-1);
				Toast.makeText(this, R.string.Video_record_end,Toast.LENGTH_SHORT).show();
			}
		} else {
			Monitor monitor = m_chooseMonitor;
			if (monitor.getDevCart() != null) {
				Device devInfo = monitor.getDevCart().getDeviceInfo();
				// 新增
				record_monitor = m_chooseMonitor;
				// 开始录制
				if (!m_isRecord) {
					if (monitor.getPlay_status() != 1) {
						Toast.makeText(Fun_RealPlayerActivity.this,R.string.Video_record_error, Toast.LENGTH_SHORT).show();
						return;
					}
					SDK._shotContext = SDK.GetChannelPlayContext(devInfo.sid, monitor.getDevCart().getChannelNum()-1);
					String strDate = DateUtil.getCurrentStringDate(DateUtil.DEFAULT_DATE_TIME_FORMAT);
					_recordfileName = Fun_AnalogVideo.RecordPath + strDate + devInfo.devname;
					File dir = new File(Fun_AnalogVideo.RecordPath.substring(0,Fun_AnalogVideo.RecordPath.length()-1));
					if(!dir.exists()) dir.mkdirs();
					if((int)SdCardUtils.getSurplusStorageSize(Fun_AnalogVideo.RecordPath) > 20){
						_fileName = _recordfileName + ".bmp";
						isShot = true;
						startNotify(101);
					}else{
						APP.ShowToast(getText(R.string.Video_Storage_space_err).toString());
					}
					m_isRecord = true;
					record_monitor.setRecord(true);
					record_index = m_chooseIndex;
					showRECImage(m_chooseIndex);
					if (m_isFullView) {
						showRECImage(-1);
					}
				}else if (m_isRecord) {// 停止录制
					m_isRecord = false;
					SDK.SetFinishVideo(_recordfileName + ".mp4",SDK._shotContext);
					SDK._shotContext = -1;
					// 新增
					record_monitor.setRecord(false);
					hideRECImage(1);
					hideRECImage(2);
					hideRECImage(3);
					hideRECImage(4);
					hideRECImage(-1);
					Toast.makeText(this, R.string.Video_record_end,Toast.LENGTH_SHORT).show();
				}
			}
		}

		// 新增
		Configuration mConfiguration = this.getResources().getConfiguration(); // 获取设置的配置信息
		int ori = mConfiguration.orientation; // 获取屏幕方向
		// 竖屏情况
		if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
			if (m_isRecord) {
				((ImageButton) m_toolView05
						.findViewById(R.id.realplayer_tool_btn05))
						.setImageResource(R.drawable.toorbar_video_selected);
				((TextView) m_toolView05
						.findViewById(R.id.realplayer_tool_tv05))
						.setTextColor(Color.rgb(255, 255, 255));
			} else {
				((ImageButton) m_toolView05
						.findViewById(R.id.realplayer_tool_btn05))
						.setImageResource(R.drawable.toorbar_video_default);
				((TextView) m_toolView05
						.findViewById(R.id.realplayer_tool_tv05))
						.setTextColor(Color.rgb(190, 190, 190));
			}
		}else {// 横屏情况
//			if (m_isRecord)
//				m_recordButton.setImageResource(R.drawable.extend_record_on);
//			else
//				m_recordButton.setImageResource(R.drawable.extend_record);
		}

	}
	
	public void btnStopClick() {
		if (m_devNum == 1) {
			Monitor monitor = m_singleMonitor;
//			int devID = monitor.getDevID();
//			int chnNum = monitor.getDevCart().getChannelNum();
//			int isSubStream = monitor.getIsSubStream();
			monitor.setPlay_status(0);
			// 播放和暂停按钮
			disableStreamButton();

			// 描述信息
			Message msg = new Message();
			msg.what = STOP_SINGLE_CONTROLS;
			Bundle bundle = new Bundle();
			bundle.putInt("tag", -1);
			msg.setData(bundle);
			controlHandler.sendMessage(msg);
		} else {
			// 若当前设备未加载
			if (m_chooseMonitor.getPlay_status() == -1)
				return;

			if (m_chooseIndex > 0) {
//				int devID = m_chooseMonitor.getDevID();
//				int chnNum = m_chooseMonitor.getDevCart().getChannelNum();
//				int isSubStream = m_chooseMonitor.getIsSubStream();

				// 播放和暂停按钮
				Message msg = new Message();
				msg.what = STOP_EACHVIEW_CONTROLS;
				Bundle bundle = new Bundle();
				bundle.putInt("tag", m_chooseIndex);
				msg.setData(bundle);
				controlHandler.sendMessage(msg);

				// 更改播放状态
				m_chooseMonitor.setPlay_status(0);
				m_chooseMonitor.setLoad_status(-1);
			}
		}
	}
	
	//点击视频停止
	public void btnCloseClick() {
		btnStopClick();
		//释放解码器
		DecoderDebugger avdecoder=m_chooseMonitor.getAVDecoder();
		if(avdecoder!=null){
			avdecoder.release();
			avdecoder=null;
		}
		if(!m_isFullView){
			SurfaceHolder surfaceHolder = m_chooseMonitor.tileView.getHolder();
			try{
				Canvas canvas = surfaceHolder.lockCanvas();
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		

		Message msg = new Message();
		msg.what = CLOSE_EACHVIEW_CONTROLS;
		Bundle bundle = new Bundle();
		bundle.putInt("tag", m_chooseIndex);
		msg.setData(bundle);
		controlHandler.sendMessage(msg);
		// 删除已关闭的通道
		DevCart devCart = m_chooseMonitor.getDevCart();
		for (DevCart _devCart : m_devCartsList) {
			if (_devCart.getDeviceInfo().sid.equals(devCart.getDeviceInfo().sid)
					&& _devCart.getChannelNum() == devCart.getChannelNum()) {
				m_devCartsList.remove(_devCart);
				break;
			}
		}
		m_chooseMonitor.setStop(true);
	}
	
	/** 清除掉所有的选中状态。 */
	private void resetToobarButton() {
		m_channelsPager.setVisibility(View.GONE);
		//m_ptzPager.setVisibility(View.GONE);
		((ImageButton) m_toolView01.findViewById(R.id.realplayer_tool_btn01))
				.setImageResource(R.drawable.toorbar_chn_default);
		((TextView) m_toolView01.findViewById(R.id.realplayer_tool_tv01))
				.setTextColor(Color.rgb(190,190,190));
		((ImageButton) m_toolView02.findViewById(R.id.realplayer_tool_btn02))
				.setImageResource(R.drawable.toorbar_pic_default);
		((TextView) m_toolView02.findViewById(R.id.realplayer_tool_tv02))
				.setTextColor(Color.rgb(190,190,190));
		((ImageButton) m_toolView03.findViewById(R.id.realplayer_tool_btn03))
				.setImageResource(R.drawable.toorbar_audio_default);
		((TextView) m_toolView03.findViewById(R.id.realplayer_tool_tv03))
				.setTextColor(Color.rgb(190,190,190));
		((ImageButton) m_toolView04.findViewById(R.id.realplayer_tool_btn04))
				.setImageResource(R.drawable.toorbar_ptz_default);
		((TextView) m_toolView04.findViewById(R.id.realplayer_tool_tv04))
				.setTextColor(Color.rgb(190,190,190));
		((ImageButton) m_toolView05.findViewById(R.id.realplayer_tool_btn05))
				.setImageResource(R.drawable.toorbar_video_default);
		((TextView) m_toolView05.findViewById(R.id.realplayer_tool_tv05))
				.setTextColor(Color.rgb(190,190,190));
	}
	
	/** 初始化相关播放控件 */
	private void initAllControls() {
		hideAddButton(-1);
		hideAddButton(1);
		hideAddButton(2);
		hideAddButton(3);
		hideAddButton(4);
		hideDetailText(-1);
		hideDetailText(1);
		hideDetailText(2);
		hideDetailText(3);
		hideDetailText(4);
		hideProgressBar(-1);
		hideProgressBar(1);
		hideProgressBar(2);
		hideProgressBar(3);
		hideProgressBar(4);
		hideRECImage(-1);
		hideRECImage(1);
		hideRECImage(2);
		hideRECImage(3);
		hideRECImage(4);
		hideRefreshButton(-1);
		hideRefreshButton(1);
		hideRefreshButton(2);
		hideRefreshButton(3);
		hideRefreshButton(4);
	}
	
	
	/** 单击PlayView的事件 */
	private void singlePlayViewEvent(View v) {
		int index = 0;
		switch (v.getId()) {
		case R.id.realplayer_multiplayview01: {
			index = 1;
		}
			break;
		case R.id.realplayer_multiplayview02: {
			index = 2;
		}
			break;
		case R.id.realplayer_multiplayview03: {
			index = 3;
		}
			break;
		case R.id.realplayer_multiplayview04: {
			index = 4;
		}
			break;

		default:
			break;
		}

		// 第一次选中
		if (m_chooseIndex == 0) {
			m_chooseMonitor = m_monitorsList.get(index - 1);
			m_chooseIndex = index;
			choosePlayView(index);
		} else {
			if (index == m_chooseIndex) {
				cancelPlayView(index);
			} else {
				m_chooseMonitor = m_monitorsList.get(index - 1);
				m_chooseIndex = index;
				choosePlayView(index);
			}
		}
		/*
		 * if (m_chooseMonitor.getPlay_status() != 1) { Intent intent = new
		 * Intent(RealPlayerActivity.this, AddChannelActivity.class);
		 * //如果已经加载过通道视频但未处于播放状态的，则移除列表 DevCart
		 * devCart=m_chooseMonitor.getDevCart(); if(devCart!=null) for (DevCart
		 * _devCart : m_devCartsList) { if (_devCart.getDeviceInfo().getDjLsh()
		 * .equals(devCart.getDeviceInfo().getDjLsh()) &&
		 * _devCart.getChannelNum() == devCart.getChannelNum()) {
		 * m_devCartsList.remove(_devCart); break; } }
		 * intent.putExtra("AddChannelActivity", (Serializable) m_devCartsList);
		 * startActivityForResult(intent, 1); }
		 */
		if (m_chooseMonitor.getDevCart() != null) {
			initChannelsView(Integer.valueOf(m_chooseMonitor.getDevCart().getDeviceInfo().channels));
		}
		EnablePlayViewButtons(m_chooseMonitor);	
	}
	
	/** 双击PlayView的事件 */
	private void doublePlayViewEvent(View v) {
		int index = 0;
		switch (v.getId()) {
		case R.id.realplayer_multiplayview01: {
			index = 1;
		}
			break;
		case R.id.realplayer_multiplayview02: {
			index = 2;
		}
			break;
		case R.id.realplayer_multiplayview03: {
			index = 3;
		}
			break;
		case R.id.realplayer_multiplayview04: {
			index = 4;
		}
			break;

		default:
			break;
		}
		//enlargePlayView(index - 1);
		enlargePlayView2(index - 1);
	}
	
	private void enlargePlayView2(int index){
		SDK.ClearChannelIFrameStatus();
		switch (index) {
		case 0: 
			if(m_isFullView){
				m_isFullView = false;
				m_multiPlayView02.setVisibility(View.VISIBLE);
				m_surface02.setVisibility(View.VISIBLE);
				m_multiPlayView03.setVisibility(View.VISIBLE);
				m_surface03.setVisibility(View.VISIBLE);
				m_multiPlayView04.setVisibility(View.VISIBLE);
				m_surface04.setVisibility(View.VISIBLE);
				_playview03and04.setVisibility(View.VISIBLE);
			}else{
				m_isFullView = true;
				m_multiPlayView02.setVisibility(View.GONE);
				m_surface02.setVisibility(View.GONE);
				m_multiPlayView03.setVisibility(View.GONE);
				m_surface03.setVisibility(View.GONE);
				m_multiPlayView04.setVisibility(View.GONE);
				m_surface04.setVisibility(View.GONE);
				_playview03and04.setVisibility(View.GONE);
			}
			break;
		case 1: 
			if(m_isFullView){
				m_isFullView = false;
				m_multiPlayView01.setVisibility(View.VISIBLE);
				m_surface01.setVisibility(View.VISIBLE);
				m_multiPlayView03.setVisibility(View.VISIBLE);
				m_surface03.setVisibility(View.VISIBLE);
				m_multiPlayView04.setVisibility(View.VISIBLE);
				m_surface04.setVisibility(View.VISIBLE);
				_playview03and04.setVisibility(View.VISIBLE);
			}else{
				m_isFullView = true;
				m_surface01.clearAnimation();
				m_surface01.clearFocus();
				m_surface01.setVisibility(View.GONE);
				m_multiPlayView01.setVisibility(View.GONE);
				m_multiPlayView03.setVisibility(View.GONE);
				m_surface03.setVisibility(View.GONE);
				m_multiPlayView04.setVisibility(View.GONE);
				m_surface04.setVisibility(View.GONE);
				_playview03and04.setVisibility(View.GONE);
			}
			break;
		case 2: 
			if(m_isFullView){
				m_isFullView = false;
				m_multiPlayView01.setVisibility(View.VISIBLE);
				m_surface01.setVisibility(View.VISIBLE);
				m_multiPlayView02.setVisibility(View.VISIBLE);
				m_surface02.setVisibility(View.VISIBLE);
				m_multiPlayView04.setVisibility(View.VISIBLE);
				m_surface04.setVisibility(View.VISIBLE);
				_playview01and02.setVisibility(View.VISIBLE);
			}else{
				m_isFullView = true;
				_playview01and02.setVisibility(View.GONE);
				m_multiPlayView01.setVisibility(View.GONE);
				m_surface01.setVisibility(View.GONE);
				m_multiPlayView02.setVisibility(View.GONE);
				m_surface02.setVisibility(View.GONE);
				m_multiPlayView04.setVisibility(View.GONE);
				m_surface04.setVisibility(View.GONE);
			}
			break;
		case 3: 
			if(m_isFullView){
				m_isFullView = false;
				_playview01and02.setVisibility(View.VISIBLE);
				m_multiPlayView01.setVisibility(View.VISIBLE);
				m_surface01.setVisibility(View.VISIBLE);
				m_multiPlayView02.setVisibility(View.VISIBLE);
				m_surface02.setVisibility(View.VISIBLE);
				m_multiPlayView03.setVisibility(View.VISIBLE);
				m_surface03.setVisibility(View.VISIBLE);
			}else{
				m_isFullView = true;
				_playview01and02.setVisibility(View.GONE);
				m_multiPlayView01.setVisibility(View.GONE);
				m_surface01.setVisibility(View.GONE);
				m_multiPlayView02.setVisibility(View.GONE);
				m_surface02.setVisibility(View.GONE);
				m_multiPlayView03.setVisibility(View.GONE);
				m_surface03.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
		//软解时重置画布
		m_chooseMonitor.clearSoftDecoder();
		
		/*int play_state = m_chooseMonitor.getPlay_status();
		// boolean isRecord = m_chooseMonitor.isRecord();
		if (play_state == -1) {
			disableCloseButton();
		} else if (play_state == 0) {
			enableCloseButton();
		} else if (play_state == 1) {
			enableCloseButton();
		}
		
		try{
			m_chooseMonitor.setImageData(null);
			m_chooseMonitor.setImageData(new byte[MAX_IMG_BUFFER_SIZE]);
		}catch(OutOfMemoryError e){
			showAlertDialog(R.string.toast_oom);
			for(int i=0;i<m_monitorsList.size();i++){
				if(m_monitorsList.get(i).getAVDecoder()!=null){
					m_monitorsList.get(i).getAVDecoder().release();
					m_monitorsList.get(i).setAVDecoder(null);
				}
			}
			return;
		}
		
		disableCloseButton();
		EnablePlayViewButtons(m_chooseMonitor);*/
		
	}
	
	/** 放大选中画面 */
	private void enlargePlayView(int index) {
		m_isFullView = true;
		m_multiPlayView.setVisibility(View.INVISIBLE);
		m_singlePlayView.setVisibility(View.VISIBLE);

		for (Monitor monitor : m_monitorsList) {
			monitor.tileView.setVisibility(View.INVISIBLE);
		}
		m_singleSurface.setVisibility(View.VISIBLE);

		int play_state = m_chooseMonitor.getPlay_status();
		// boolean isRecord = m_chooseMonitor.isRecord();
		if (play_state == -1) {
			disableCloseButton();
		} else if (play_state == 0) {
			enableCloseButton();
		} else if (play_state == 1) {
			enableCloseButton();
		}

		/*
		 * if(android.os.Build.VERSION.SDK_INT>=16) //双击放大后硬解播放
		 * mediaThread.start();
		 */

		if (m_isRecord && (m_chooseIndex == record_index)) {
			showRECImage(-1);
		} else {
			hideRECImage(-1);
		}
		try{
			m_chooseMonitor.setImageData(null);
			m_chooseMonitor.setImageData(new byte[MAX_IMG_BUFFER_SIZE]);
		}catch(OutOfMemoryError e){
			showAlertDialog(R.string.toast_oom);
			for(int i=0;i<m_monitorsList.size();i++){
				if(m_monitorsList.get(i).getAVDecoder()!=null){
					m_monitorsList.get(i).getAVDecoder().release();
					m_monitorsList.get(i).setAVDecoder(null);
				}
			}
			return;
		}
		
		/*if(m_chooseMonitor.getIsSubStream()!=m_chooseMonitor.getDisplayStream()){
			btnStopClick();
			Lock lock = m_chooseMonitor.getDecoder_lock();
			lock.lock();
			if (m_chooseMonitor.getCodectype() == 12) {
				int decPort = m_chooseMonitor.getDecPort();
				H265Sdk.H265Unit(decPort);
				m_chooseMonitor.setCodecinit(0);
				freamtypeType = -1;
			} else {

				H264Dec.UninitDecoder(m_chooseMonitor.getDecoder());
				m_chooseMonitor.setDecoder(H264Dec.InitDecoder());
			}
			lock.unlock();
			m_chooseMonitor.setIsSubStream(m_chooseMonitor.getDisplayStream());
			btnPlayClick();
		}*/
		disableCloseButton();
		EnablePlayViewButtons(m_chooseMonitor);
	}
	
	/** 默认选择第一个监视器 */
	private void defaultChoose() {
		if(m_monitorsList!=null){
			m_chooseMonitor = m_monitorsList.get(0);
			m_chooseIndex = 1;
			choosePlayView(1);
			//显示通道数、点击直接播放
			if (m_chooseMonitor.getDevCart() != null) {
				initChannelsView(Integer.valueOf(m_chooseMonitor.getDevCart().getDeviceInfo().channels));
			}
			if(m_channelsPager!=null)
			m_channelsPager.setVisibility(View.GONE);
		}
	}
	/** 选中指定画面 */
	private void choosePlayView(int index) {
		chooseStreamButton(m_chooseMonitor.getDisplayStream());
		// 判断当前播放状态
		if (m_chooseMonitor.getPlay_status() == -1) {
			disableCloseButton();
			disableStreamButton();
		} else if (m_chooseMonitor.getPlay_status() == 0) {
			disableCloseButton();
			disableStreamButton();
		} else if (m_chooseMonitor.getPlay_status() == 1) {
			enableCloseButton();
			enableStreamButton();
		}
		switch (index) {
		case 1: {
			m_multiPlayView01.setBackgroundResource(R.drawable.monitor_chooseplayview);
			m_multiPlayView02.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView03.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView04.setBackgroundColor(Color.TRANSPARENT);
		}
			break;
		case 2: {
			m_multiPlayView01.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView02.setBackgroundResource(R.drawable.monitor_chooseplayview);
			m_multiPlayView03.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView04.setBackgroundColor(Color.TRANSPARENT);
		}
			break;
		case 3: {
			m_multiPlayView01.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView02.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView03
					.setBackgroundResource(R.drawable.monitor_chooseplayview);
			m_multiPlayView04.setBackgroundColor(Color.TRANSPARENT);
		}
			break;
		case 4: {
			m_multiPlayView01.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView02.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView03.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView04
					.setBackgroundResource(R.drawable.monitor_chooseplayview);
		}
			break;

		default:
			break;
		}

		if (m_chooseMonitor.devID != 0)
			if(m_dataFlowView!=null)
			m_dataFlowView.setText(m_chooseMonitor.getDevCart().getDeviceInfo()
					.getDevname());
	}
	
	/** 取消选中指定画面 */
	private void cancelPlayView(int index) {
		if (index != 100)
			return;
		else {
			m_multiPlayView01.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView02.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView03.setBackgroundColor(Color.TRANSPARENT);
			m_multiPlayView04.setBackgroundColor(Color.TRANSPARENT);
		}

		m_chooseIndex = 0;
		int playBtn = -1;
		int stopBtn = -1;
		for (Monitor monitor : m_monitorsList) {
			if (monitor.getPlay_status() == 1) {
				playBtn = 1;
				break;
			}
		}
		for (Monitor monitor : m_monitorsList) {
			if (monitor.getPlay_status() == 0) {
				stopBtn = 1;
				break;
			}
		}
		// 全部暂停
		if (playBtn == 1) {
			enableCloseButton();
			enableStreamButton();
			return;
		}
		// 全部开始
		if (stopBtn == 1) {
			enableCloseButton();
			disableStreamButton();
			return;
		}
		enableCloseButton();
		disableStreamButton();
	}
	
	/** 正常切换码流按钮 */
	private void enableStreamButton() {
		//防止因横屏没有此控件报空指针异常
		if(m_mainStream!=null){
		m_mainStream.setEnabled(true);
		m_mainStream.setClickable(true);
		m_mainStream.getBackground().setAlpha(255);
		m_mainStream.invalidate();
		}
		if(m_subStream!=null){
		m_subStream.setEnabled(true);
		m_subStream.setClickable(true);
		m_subStream.getBackground().setAlpha(255);
		m_subStream.invalidate();
		}
	}
	
	/** 选择码流 */
	private void chooseStreamButton(int isSub) {
		if(m_mainStream==null||m_subStream==null)
			return;
		// 辅码流
		if (isSub == 1) {
			m_mainStream.setBackgroundColor(Color.WHITE);
			m_mainStream.setTextColor(Color.BLACK);
			m_mainStream.setClickable(false);

			m_subStream.setBackgroundColor(Color.rgb(42,136,183));
			m_subStream.setTextColor(Color.WHITE);
			m_subStream.setClickable(true);
		}else {// 主码流
			m_subStream.setBackgroundColor(Color.WHITE);
			m_subStream.setTextColor(Color.BLACK);
			m_subStream.setClickable(false);

			m_mainStream.setBackgroundColor(Color.rgb(42,136,183));
			m_mainStream.setTextColor(Color.WHITE);
			m_mainStream.setClickable(true);
		}
	}
	
	/** 正常关闭按钮 */
	private void enableCloseButton() {
		m_closebButton.setEnabled(true);
		m_closebButton.getDrawable().setAlpha(255);
		m_closebButton.invalidate();
	}

	/** 禁用关闭按钮 */
	private void disableCloseButton() {
		m_closebButton.setEnabled(false);
		m_closebButton.getDrawable().setAlpha(125);
		m_closebButton.invalidate();
	}
	/** 隐藏状态文字 */
	private void hideDetailText(int tag) {
		TextView detailText = null;
		int i = 0;
		switch (tag) {
		case -1:
			detailText = m_singleDetailText;
			i = 0;
			break;
		case 1:
			detailText = m_multiDetailText01;
			i = 1;
			break;
		case 2:
			detailText = m_multiDetailText02;
			i = 2;
			break;
		case 3:
			detailText = m_multiDetailText03;
			i = 3;
			break;
		case 4:
			detailText = m_multiDetailText04;
			i = 4;
			break;
		default:
			break;
		}
		try{
			detailText.setVisibility(View.GONE);
			textview_show[i] = View.GONE;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/** 显示等待画面 */
	private void showProgressBar(int tag) {
		ProgressBar progress = null;
		int i = tag;
		switch (tag) {
		case -1:
			progress = m_singleProgressBar;
			i = 0;
			break;
		case 1:
			progress = m_multiProgressBar01;
			break;
		case 2:
			progress = m_multiProgressBar02;
			break;
		case 3:
			progress = m_multiProgressBar03;
			break;
		case 4:
			progress = m_multiProgressBar04;
			break;

		default:
			break;
		}

		try
		{
		progress.setVisibility(View.VISIBLE);
		progress_show[i] = View.VISIBLE;
		} catch (Exception e) {
			e.printStackTrace();			
		} 
	}
	/** 隐藏等待画面 */
	private void hideProgressBar(int tag) {
		ProgressBar progress = null;
		int i = tag;
		switch (tag) {
		case -1:
			progress = m_singleProgressBar;
			i = 0;
			break;
		case 1:
			progress = m_multiProgressBar01;
			break;
		case 2:
			progress = m_multiProgressBar02;
			break;
		case 3:
			progress = m_multiProgressBar03;
			break;
		case 4:
			progress = m_multiProgressBar04;
			break;

		default:
			break;
		}
		try{
			progress.setVisibility(View.GONE);
			progress_show[i] = View.GONE;
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/** 显示REC图标 */
	private void showRECImage(int tag) {
		ImageView recView = null;
		switch (tag) {
		case -1:
			recView = m_singleRECView;
			break;
		case 1:
			recView = m_multiRECView01;
			break;
		case 2:
			recView = m_multiRECView02;
			break;
		case 3:
			recView = m_multiRECView03;
			break;
		case 4:
			recView = m_multiRECView04;
			break;

		default:
			break;
		}
		recView.setVisibility(View.VISIBLE);
	}
	
	/** 隐藏REC图标 */
	private void hideRECImage(int tag) {
		ImageView recView = null;
		switch (tag) {
		case -1:
			recView = m_singleRECView;
			break;
		case 1:
			recView = m_multiRECView01;
			break;
		case 2:
			recView = m_multiRECView02;
			break;
		case 3:
			recView = m_multiRECView03;
			break;
		case 4:
			recView = m_multiRECView04;
			break;

		default:
			break;
		}
		recView.setVisibility(View.GONE);
	}
	/** 显示新增画面 */
	private void showAddButton(int tag) {
		ImageView imageView = null;
		int i = tag;
		switch (tag) {
		case -1:
			imageView = m_addchannel;
			i = 0;
			break;
		case 1:
			imageView = m_addchannel01;
			break;
		case 2:
			imageView = m_addchannel02;
			break;
		case 3:
			imageView = m_addchannel03;
			break;
		case 4:
			imageView = m_addchannel04;
			break;
		default:
			break;
		}
		try{
			addbutton_show[i] = View.VISIBLE;
			imageView.setVisibility(View.VISIBLE);
		}catch (Exception e) {
			e.printStackTrace();
		} 
	}
	private void addChannelClick() {
		OnClickListener onClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isAddChannel)
					return;
				//此处需要先停止 蛮牛的视频
				if(m_monitorsList==null){
					return;
				}
				if(m_monitorsList.size()>0){
					for(Monitor mr : m_monitorsList){
						if(mr.getDevCart()!=null && mr.getDevCart().getDeviceInfo()!=null){
							DevCart devCart = mr.getDevCart();							
							System.out.println("20160309   添加通道按钮按下时，要停止己打开的蛮牛的视频");
							StopMNRealPlay(devCart);
							System.out.println("20160309   添加通道按钮按下时，要停止己打开的蛮牛的视频XXXXXXXXXXXXXXX");
						}
					}
				}
				
				switch (v.getId()) {
				case R.id.addchannel1_activity_realplayer:
					m_chooseMonitor = m_monitorsList.get(0);
					break;
				case R.id.addchannel2_activity_realplayer:
					m_chooseMonitor = m_monitorsList.get(1);
					break;
				case R.id.addchannel3_activity_realplayer:
					m_chooseMonitor = m_monitorsList.get(2);
					break;
				case R.id.addchannel4_activity_realplayer:
					m_chooseMonitor = m_monitorsList.get(3);
					break;
				default:
					break;
				}
				// TODO Auto-generated method stub
				/*
				 * Intent intent = new Intent(RealPlayerActivity.this,
				 * AddChannelActivity.class);
				 * intent.putExtra("AddChannelActivity", (Serializable)
				 * m_devCartsList); startActivityForResult(intent, 1);
				 */
				isAddChannel=true;
				Intent intent = new Intent(Fun_RealPlayerActivity.this,Fun_AddChannelActivity.class);
				// 如果已经加载过通道视频但未处于播放状态的，则移除列表
				DevCart devCart = m_chooseMonitor.getDevCart();
				if (devCart != null)
					for (DevCart _devCart : m_devCartsList) {
						if (_devCart.getDeviceInfo().sid.equals(devCart.getDeviceInfo().sid)
								&& _devCart.getChannelNum() == devCart.getChannelNum()) {
							m_devCartsList.remove(_devCart);
							break;
						}
					}
				intent.putExtra("AddChannelActivity",(Serializable) m_devCartsList);
				startActivityForResult(intent, 1);
			}
		};
		m_addchannel01.setOnClickListener(onClick);
		m_addchannel02.setOnClickListener(onClick);
		m_addchannel03.setOnClickListener(onClick);
		m_addchannel04.setOnClickListener(onClick);
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		isAddChannel=false;
		if (resultCode == 1) {
			m_devCartsList.clear();
			m_devCartsList = (List<DevCart>) data.getSerializableExtra("AddChannelActivity");
			List<DevCart> myList = (List<DevCart>) data.getSerializableExtra("AddChannelList");
//			for (DevCart devCart : myList) // 通过循环，将打开的实施播放数据保存
//			{
//				int channelNum = devCart.getChannelNum();
//				//通过Local_intent是否为空，判断将数据保存到直连视图数据表还是云视图数据表
//				if (getIntent().getStringExtra("Local_intent") != null) {
//					String djLsh = devCart.getDeviceInfo().getDjLsh();
//					RealPlayInfoDBManager.updateLocal(djLsh, channelNum);
//				} else {
//					String uuid = devCart.getDeviceInfo().getUuid();
//					RealPlayInfoDBManager.updateCloud(uuid, channelNum);
//					DeviceInfo deviceInfo = devCart.getDeviceInfo();
//					DeviceInfoDBManagerCloud.addItem(deviceInfo);
//				}
//			}
			/*int index = 0;
			for (int i = 0; i < 4; i++) {
				Monitor monitor = m_monitorsList.get(i);
				if (monitor.getLoad_status() == -1) {
					if (index < myList.size()) {
						monitor.setDevCart(myList.get(index));
						monitor.setLoad_status(1);
						monitor.setStop(false);
						index++;
					} else
						break;
				}
			}*/
			
			for (int i = 0; i < m_devCartsList.size(); i++) {
				Monitor monitor = m_monitorsList.get(i);
				// 设置monitor已加载过视频
				monitor.setLoad_status(1);
				monitor.setDevCart(m_devCartsList.get(i));
			}
			
			startMultiLoginRequestVideoThread();
		}else if(resultCode == 2){
			startMultiLoginRequestVideoThread();
			defaultChoose();
		}
	}
	/** 隐藏重载画面 */
	private void hideRefreshButton(int tag) {
		ImageButton refreshButton = null;
		int i = tag;
		switch (tag) {
		case -1:
			refreshButton = m_singleRefreshButton;
			i = 0;
			break;
		case 1:
			refreshButton = m_multiRefreshButton01;
			break;
		case 2:
			refreshButton = m_multiRefreshButton02;
			break;
		case 3:
			refreshButton = m_multiRefreshButton03;
			break;
		case 4:
			refreshButton = m_multiRefreshButton04;
			break;

		default:
			break;
		}
		try{
			refreshButton.setVisibility(View.GONE);
			refresh_show[i] = View.GONE;
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	/** 隐藏新增画面 */
	private void hideAddButton(int tag) {
		ImageView imageView = null;
		int i = tag;
		switch (tag) {
		case -1:
			imageView = m_addchannel;
			i = 0;
			break;
		case 1:
			imageView = m_addchannel01;
			break;
		case 2:
			imageView = m_addchannel02;
			break;
		case 3:
			imageView = m_addchannel03;
			break;
		case 4:
			imageView = m_addchannel04;
			break;
		default:
			break;
		}
		try{
			addbutton_show[i] = View.GONE;		
			imageView.setVisibility(View.GONE);
		}catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	/** 显示状态文字 */
	private void showDetailText(int tag, int stringId) {
		TextView detailText = null;
		int i = 0;
		switch (tag) {
		case -1:
			detailText = m_singleDetailText;
			i = 0;
			break;
		case 1:
			detailText = m_multiDetailText01;
			i = 1;
			break;
		case 2:
			detailText = m_multiDetailText02;
			i = 2;
			break;
		case 3:
			detailText = m_multiDetailText03;
			i = 3;
			break;
		case 4:
			detailText = m_multiDetailText04;
			i = 4;
			break;

		default:
			break;
		}
		detailText.setVisibility(View.VISIBLE);
		detailText.setText(stringId);
		textview_show[i] = View.VISIBLE;
		text[i] = getResources().getString(stringId);
	}
	/**释放解码器 关闭连接...*/
	private void releaseDecoder(){
		if(m_avdecoder!=null){
			if(m_singleMonitor!=null){
				DevCart devInfo = m_singleMonitor.getDevCart();
				if(null!= devInfo && null != devInfo.getDeviceInfo()){
					if(devInfo.getDeviceInfo().type== 1){
						StopMNRealPlay(devInfo.getDeviceInfo().sid,devInfo.getChannelNum());
					}
				}
				m_avdecoder.release();
				m_avdecoder=null;
			}
		}
		if(m_monitorsList!=null)
		for(int i=0;i<m_monitorsList.size();i++){
			DevCart devInfo = m_monitorsList.get(i).getDevCart();
			if(null!= devInfo && null != devInfo.getDeviceInfo()){
				if(devInfo.getDeviceInfo().type== 1){
					StopMNRealPlay(devInfo.getDeviceInfo().sid,devInfo.getChannelNum());
				}
			}
			if(m_monitorsList.get(i).getAVDecoder()!=null){
				m_monitorsList.get(i).getAVDecoder().release();
				m_monitorsList.get(i).setAVDecoder(null);
			}
		}
	}
	//单路 刷新视频
	private void refreshStartMNRealPlay(DevCart devCart,DecoderDebugger avdecoder,Monitor monitor,int nWinIndex,boolean blnSingle){
		if(devCart.getDeviceInfo()!=null){
			//int nGetCount =0;
			//if(HttpCenter.getMNDeviceIsOnLine(devCart.getDeviceInfo().getUuid())){
				//System.out.println("2016.04.08TEST    StartMNRealPlay____在线"+devCart.getDeviceInfo().getUuid()+"===="+devCart.getChannelNum());
				StartMNRealPlay(devCart.getDeviceInfo().sid, devCart.getChannelNum(), devCart.getDeviceInfo().online == 1, avdecoder, monitor, nWinIndex, blnSingle);				
			/*}
			else
			{
				System.out.println("2016.04.08TEST    StartMNRealPlay____不在线"+devCart.getDeviceInfo().getUuid()+"===="+devCart.getChannelNum());
				if(monitor!=null)
				{
					monitor.setPlay_status(0);
				}
				Message msg = new Message();
				msg.what = SHOW_CONNECTSUBTODEVICE_STATE;
				Bundle bundle = new Bundle();
				bundle.putInt("tag", nWinIndex);
				bundle.putInt("state", -2);//-2,-3
				msg.setData(bundle);
				controlHandler.sendMessage(msg);
			}*/
		}
	}
	
	/** 请求视频线程 */
	private void runMultiRequestVideoThread(int sender) {
		Monitor monitor = m_monitorsList.get(sender - 1);
		Message msg = null;
		Bundle bundle = null;
		DevCart devCart = monitor.getDevCart();
		msg = new Message();
		msg.what = INIT_EACHVIEW_CONTROLS;
		bundle = new Bundle();
		bundle.putInt("tag", sender);
		msg.setData(bundle);
		controlHandler.sendMessage(msg);
		monitor.setCanChangeChn(false);
		
		StartMNRealPlay(devCart, monitor.getAVDecoder(), monitor, sender,false);
		// 完成登录操作，无论成功失败
		monitor.setCanChangeChn(true);
	}
	
	//停止蛮牛的实时播放
	private void StopMNRealPlay(DevCart devCart) {
		if (devCart != null && devCart.getDeviceInfo() != null) {
			StopMNRealPlay(devCart.getDeviceInfo().sid,devCart.getChannelNum());
		}
	}

	/** 停止蛮牛的实时播放--关闭通道*/
	private void StopMNRealPlay(final String strUUID, final int nChannelIndex) {
		int nCI = nChannelIndex;
		if (nCI >= 0) {
			if (nCI > 0) {
				nCI--;
			}
			System.out.println("SDK.CloseChannel(" + strUUID + "," + nCI + ")");
			SDK.CloseChannel(strUUID, nCI);
		}
	}
	/** 禁用切换码流按钮 */
	private void disableStreamButton() {
		//防止因横屏没有此控件报空指针异常
		if(m_mainStream!=null){
		m_mainStream.setEnabled(false);
		m_mainStream.setClickable(false);
		m_mainStream.getBackground().setAlpha(125);
		m_mainStream.invalidate();
		}
		
		if(m_subStream!=null){
		m_subStream.setEnabled(false);
		m_subStream.setClickable(false);
		m_subStream.getBackground().setAlpha(125);
		m_subStream.invalidate();
		}
	}
	/** 显示重载画面 */
	private void showRefreshButton(int tag) {
		ImageButton refreshButton = null;
		int i = tag;
		switch (tag) {
		case -1:
			refreshButton = m_singleRefreshButton;
			i = 0;
			break;
		case 1:
			refreshButton = m_multiRefreshButton01;
			break;
		case 2:
			refreshButton = m_multiRefreshButton02;
			break;
		case 3:
			refreshButton = m_multiRefreshButton03;
			break;
		case 4:
			refreshButton = m_multiRefreshButton04;
			break;

		default:
			break;
		}
		try{
			refreshButton.setVisibility(View.VISIBLE);
			refresh_show[i] = View.VISIBLE;
		} catch (Exception e) {
			e.printStackTrace();			
		} 
	}
	/** 初始化通道切换视图 */
	@SuppressLint("InflateParams")
	private void initChannelsView(int channelNum) {
		LayoutInflater inflater = getLayoutInflater();
		View view01 = inflater.inflate(R.layout.viewpager_realplayer_channels01, null);
		View view02 = inflater.inflate(R.layout.viewpager_realplayer_channels02, null);
		View view03 = inflater.inflate(R.layout.viewpager_realplayer_channels03, null);
		View view04 = inflater.inflate(R.layout.viewpager_realplayer_channels04, null);

		View channel01 = view01.findViewById(R.id.realplayer_btn_channel01);
		View channel02 = view01.findViewById(R.id.realplayer_btn_channel02);
		View channel03 = view01.findViewById(R.id.realplayer_btn_channel03);
		View channel04 = view01.findViewById(R.id.realplayer_btn_channel04);
		View channel05 = view01.findViewById(R.id.realplayer_btn_channel05);
		View channel06 = view01.findViewById(R.id.realplayer_btn_channel06);
		View channel07 = view01.findViewById(R.id.realplayer_btn_channel07);
		View channel08 = view01.findViewById(R.id.realplayer_btn_channel08);

		View channel09 = view02.findViewById(R.id.realplayer_btn_channel09);
		View channel10 = view02.findViewById(R.id.realplayer_btn_channel10);
		View channel11 = view02.findViewById(R.id.realplayer_btn_channel11);
		View channel12 = view02.findViewById(R.id.realplayer_btn_channel12);
		View channel13 = view02.findViewById(R.id.realplayer_btn_channel13);
		View channel14 = view02.findViewById(R.id.realplayer_btn_channel14);
		View channel15 = view02.findViewById(R.id.realplayer_btn_channel15);
		View channel16 = view02.findViewById(R.id.realplayer_btn_channel16);

		View channel17 = view03.findViewById(R.id.realplayer_btn_channel17);
		View channel18 = view03.findViewById(R.id.realplayer_btn_channel18);
		View channel19 = view03.findViewById(R.id.realplayer_btn_channel19);
		View channel20 = view03.findViewById(R.id.realplayer_btn_channel20);
		View channel21 = view03.findViewById(R.id.realplayer_btn_channel21);
		View channel22 = view03.findViewById(R.id.realplayer_btn_channel22);
		View channel23 = view03.findViewById(R.id.realplayer_btn_channel23);
		View channel24 = view03.findViewById(R.id.realplayer_btn_channel24);

		View channel25 = view04.findViewById(R.id.realplayer_btn_channel25);
		View channel26 = view04.findViewById(R.id.realplayer_btn_channel26);
		View channel27 = view04.findViewById(R.id.realplayer_btn_channel27);
		View channel28 = view04.findViewById(R.id.realplayer_btn_channel28);
		View channel29 = view04.findViewById(R.id.realplayer_btn_channel29);
		View channel30 = view04.findViewById(R.id.realplayer_btn_channel30);
		View channel31 = view04.findViewById(R.id.realplayer_btn_channel31);
		View channel32 = view04.findViewById(R.id.realplayer_btn_channel32);

		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.realplayer_btn_channel01:
					btnChannelClick(1);
					break;
				case R.id.realplayer_btn_channel02:
					btnChannelClick(2);
					break;
				case R.id.realplayer_btn_channel03:
					btnChannelClick(3);
					break;
				case R.id.realplayer_btn_channel04:
					btnChannelClick(4);
					break;
				case R.id.realplayer_btn_channel05:
					btnChannelClick(5);
					break;
				case R.id.realplayer_btn_channel06:
					btnChannelClick(6);
					break;
				case R.id.realplayer_btn_channel07:
					btnChannelClick(7);
					break;
				case R.id.realplayer_btn_channel08:
					btnChannelClick(8);
					break;
				case R.id.realplayer_btn_channel09:
					btnChannelClick(9);
					break;
				case R.id.realplayer_btn_channel10:
					btnChannelClick(10);
					break;
				case R.id.realplayer_btn_channel11:
					btnChannelClick(11);
					break;
				case R.id.realplayer_btn_channel12:
					btnChannelClick(12);
					break;
				case R.id.realplayer_btn_channel13:
					btnChannelClick(13);
					break;
				case R.id.realplayer_btn_channel14:
					btnChannelClick(14);
					break;
				case R.id.realplayer_btn_channel15:
					btnChannelClick(15);
					break;
				case R.id.realplayer_btn_channel16:
					btnChannelClick(16);
					break;
				case R.id.realplayer_btn_channel17:
					btnChannelClick(17);
					break;
				case R.id.realplayer_btn_channel18:
					btnChannelClick(18);
					break;
				case R.id.realplayer_btn_channel19:
					btnChannelClick(19);
					break;
				case R.id.realplayer_btn_channel20:
					btnChannelClick(20);
					break;
				case R.id.realplayer_btn_channel21:
					btnChannelClick(21);
					break;
				case R.id.realplayer_btn_channel22:
					btnChannelClick(22);
					break;
				case R.id.realplayer_btn_channel23:
					btnChannelClick(23);
					break;
				case R.id.realplayer_btn_channel24:
					btnChannelClick(24);
					break;
				case R.id.realplayer_btn_channel25:
					btnChannelClick(25);
					break;
				case R.id.realplayer_btn_channel26:
					btnChannelClick(26);
					break;
				case R.id.realplayer_btn_channel27:
					btnChannelClick(27);
					break;
				case R.id.realplayer_btn_channel28:
					btnChannelClick(28);
					break;
				case R.id.realplayer_btn_channel29:
					btnChannelClick(29);
					break;
				case R.id.realplayer_btn_channel30:
					btnChannelClick(30);
					break;
				case R.id.realplayer_btn_channel31:
					btnChannelClick(31);
					break;
				case R.id.realplayer_btn_channel32:
					btnChannelClick(32);
					break;

				default:
					break;
				}
			}
		};

		channel01.setClickable(true);
		channel02.setClickable(true);
		channel03.setClickable(true);
		channel04.setClickable(true);
		channel05.setClickable(true);
		channel06.setClickable(true);
		channel07.setClickable(true);
		channel08.setClickable(true);
		channel09.setClickable(true);
		channel10.setClickable(true);
		channel11.setClickable(true);
		channel12.setClickable(true);
		channel13.setClickable(true);
		channel14.setClickable(true);
		channel15.setClickable(true);
		channel16.setClickable(true);
		channel17.setClickable(true);
		channel18.setClickable(true);
		channel19.setClickable(true);
		channel20.setClickable(true);
		channel21.setClickable(true);
		channel22.setClickable(true);
		channel23.setClickable(true);
		channel24.setClickable(true);
		channel25.setClickable(true);
		channel26.setClickable(true);
		channel27.setClickable(true);
		channel29.setClickable(true);
		channel30.setClickable(true);
		channel31.setClickable(true);
		channel32.setClickable(true);

		channel01.setFocusable(true);
		channel02.setFocusable(true);
		channel03.setFocusable(true);
		channel04.setFocusable(true);
		channel05.setFocusable(true);
		channel06.setFocusable(true);
		channel07.setFocusable(true);
		channel08.setFocusable(true);
		channel09.setFocusable(true);
		channel10.setFocusable(true);
		channel11.setFocusable(true);
		channel12.setFocusable(true);
		channel13.setFocusable(true);
		channel14.setFocusable(true);
		channel15.setFocusable(true);
		channel16.setFocusable(true);
		channel17.setFocusable(true);
		channel18.setFocusable(true);
		channel19.setFocusable(true);
		channel20.setFocusable(true);
		channel21.setFocusable(true);
		channel22.setFocusable(true);
		channel23.setFocusable(true);
		channel24.setFocusable(true);
		channel25.setFocusable(true);
		channel26.setFocusable(true);
		channel27.setFocusable(true);
		channel29.setFocusable(true);
		channel30.setFocusable(true);
		channel31.setFocusable(true);
		channel32.setFocusable(true);

		channel01.setOnClickListener(listener);
		channel02.setOnClickListener(listener);
		channel03.setOnClickListener(listener);
		channel04.setOnClickListener(listener);
		channel05.setOnClickListener(listener);
		channel06.setOnClickListener(listener);
		channel07.setOnClickListener(listener);
		channel08.setOnClickListener(listener);
		channel09.setOnClickListener(listener);
		channel10.setOnClickListener(listener);
		channel11.setOnClickListener(listener);
		channel12.setOnClickListener(listener);
		channel13.setOnClickListener(listener);
		channel14.setOnClickListener(listener);
		channel15.setOnClickListener(listener);
		channel16.setOnClickListener(listener);
		channel17.setOnClickListener(listener);
		channel18.setOnClickListener(listener);
		channel19.setOnClickListener(listener);
		channel20.setOnClickListener(listener);
		channel21.setOnClickListener(listener);
		channel22.setOnClickListener(listener);
		channel23.setOnClickListener(listener);
		channel24.setOnClickListener(listener);
		channel25.setOnClickListener(listener);
		channel26.setOnClickListener(listener);
		channel27.setOnClickListener(listener);
		channel28.setOnClickListener(listener);
		channel29.setOnClickListener(listener);
		channel30.setOnClickListener(listener);
		channel31.setOnClickListener(listener);
		channel32.setOnClickListener(listener);

		m_channlespager_viewsList = new ArrayList<View>();
		List<View> channelViewList = new ArrayList<View>();
		channelViewList.add(channel01);
		channelViewList.add(channel02);
		channelViewList.add(channel03);
		channelViewList.add(channel04);
		channelViewList.add(channel05);
		channelViewList.add(channel06);
		channelViewList.add(channel07);
		channelViewList.add(channel08);
		channelViewList.add(channel09);
		channelViewList.add(channel10);
		channelViewList.add(channel11);
		channelViewList.add(channel12);
		channelViewList.add(channel13);
		channelViewList.add(channel14);
		channelViewList.add(channel15);
		channelViewList.add(channel16);
		channelViewList.add(channel17);
		channelViewList.add(channel18);
		channelViewList.add(channel19);
		channelViewList.add(channel20);
		channelViewList.add(channel21);
		channelViewList.add(channel22);
		channelViewList.add(channel23);
		channelViewList.add(channel24);
		channelViewList.add(channel25);
		channelViewList.add(channel26);
		channelViewList.add(channel27);
		channelViewList.add(channel28);
		channelViewList.add(channel29);
		channelViewList.add(channel30);
		channelViewList.add(channel31);
		channelViewList.add(channel32);
		if (channelNum <= 8) {
			m_channlespager_viewsList.add(view01);
			for (int i = channelNum + 1; i <= 8; i++)
				channelViewList.get(i - 1).setVisibility(View.INVISIBLE);
		} else if (channelNum <= 16) {
			m_channlespager_viewsList.add(view01);
			m_channlespager_viewsList.add(view02);
			for (int i = channelNum + 1; i <= 16; i++)
				channelViewList.get(i - 1).setVisibility(View.INVISIBLE);
		} else if (channelNum <= 24) {
			m_channlespager_viewsList.add(view01);
			m_channlespager_viewsList.add(view02);
			m_channlespager_viewsList.add(view03);
			for (int i = channelNum + 1; i <= 24; i++)
				channelViewList.get(i - 1).setVisibility(View.INVISIBLE);
		} else if (channelNum <= 32) {
			m_channlespager_viewsList.add(view01);
			m_channlespager_viewsList.add(view02);
			m_channlespager_viewsList.add(view03);
			m_channlespager_viewsList.add(view04);
			for (int i = channelNum + 1; i <= 32; i++)
				channelViewList.get(i - 1).setVisibility(View.INVISIBLE);
		}
		channelPageAdapter = null;
		channelPageAdapter = new ChannelPageAdapter(m_channlespager_viewsList);
		// m_channlespager_viewsList.add(view01);
		// m_channlespager_viewsList.add(view02);
		// m_channlespager_viewsList.add(view03);
		// m_channlespager_viewsList.add(view04);

		// m_channelsPager.setVisibility(View.GONE);
		if(m_channelsPager!=null)
		m_channelsPager.setAdapter(channelPageAdapter);
	}
	
	public void btnChannelClick(int channel) {
		Monitor monitor = null;
		if (m_devNum == 1) {
			monitor = m_singleMonitor;
		} else {
			if (m_chooseMonitor.devCart == null) {
				return;
			}
			if (m_chooseMonitor.getDevID() == 0) {
				showAlertDialog(R.string.alertMsg17);
				return;
			}
			monitor = m_chooseMonitor;
		}

		boolean isRecord = monitor.isRecord();
		boolean isCanChangeChn = monitor.isCanChangeChn();
		int channelNum = Integer.valueOf(monitor.getDevCart().getDeviceInfo().channelNo);

		// 判断是否在录像
		if (isRecord) {
			showAlertDialog(R.string.alertMsg19);
			return;
		}

		// 不能切换通道
		if (!isCanChangeChn) {
			showAlertDialog(R.string.alertMsg1);
			return;
		}
		// 超过通道数上限
		else if (channel > channelNum) {
			showAlertDialog(R.string.alertMsg10);
			return;
		} else if (m_devNum > 1) {
			boolean isExist = false;
			for(int i=0;i<m_devCartsList.size();i++){
				DevCart devCart=m_devCartsList.get(i);
				if(devCart.getDeviceInfo().sid.equals(monitor.getDevCart().getDeviceInfo().sid)){
					if(devCart.getChannelNum()==channel){
						isExist=true;
						break;
					}
				}			
			}
			if (isExist) {
				showAlertDialog(R.string.alertMsg35);
				return;
			}
		}

		// 画面置黑
		// m_StatusResult.tileView.playImage.image = nil;

		monitor.setPlay_status(-1);

//		int devID = monitor.getDevID();
//		int chnNum = monitor.getDevCart().getChannelNum();
//		int isSubStream = monitor.getIsSubStream();
		// 关闭通道
		/*m_zlvss.devmanager_stopVideoFromDevice_devID(devID, chnNum, isSubStream);
		Lock lock = monitor.getDecoder_lock();
		lock.lock();
		if (monitor.getCodectype() == 12) {
			if (m_devNum == 1) {
				int decPort = m_singleMonitor.getDecPort();
				m_singleMonitor.setCodecinit(0);
				H265Sdk.H265Unit(decPort);
				freamtypeType = -1;
			} else {
				int decPort = m_chooseMonitor.getDecPort();
				m_chooseMonitor.setCodecinit(0);
				H265Sdk.H265Unit(decPort);
				freamtypeType = -1;
			}
		} else {
			H264Dec.UninitDecoder(monitor.getDecoder());
			monitor.setDecoder(H264Dec.InitDecoder());
		}
		lock.unlock();*/

		// 重新连接通道
		monitor.getDevCart().setChannelNum(channel);

		if (m_devNum == 1) {
			startSingleRequestVideoThread();
		} else {
			//startMultiRequestVideoThread(m_chooseIndex);
		}
	}
	
	public void btnRefreshClick(int tag) {
		if (tag == -1) {//间画面
			m_singleMonitor.setPlay_status(0);
			startSingleRequestVideoThread();
		} else {//多路
			Monitor monitor = m_monitorsList.get(tag - 1);
			monitor.setPlay_status(-1);
			startMultiRequestVideoThread(tag);
		}
	}
	public void startSingleLoginRequestVideoThread() {
		if(m_devCartsList.size() == 0){			 
			return ;
		}
		new Thread() {
			@Override
			public void run() {
				try {
					runSingleLoginRequestVideoThread(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	/**
	 * 登录设备并请求视频线程
	 * 
	 * @throws InterruptedException
	 */
	private void runSingleLoginRequestVideoThread(int sender)
			throws InterruptedException {
		// 若sender = 1，则断开连接，重新连接。
//		int tag = sender;
//		boolean isTimeout = false; // 视频数据是否超时
		Message msg = null;
		Bundle bundle = null;

		// 显示加载控件
		controlHandler.sendEmptyMessage(INIT_SINGLE_CONTROLS);
		m_singleMonitor.setCanChangeChn(false);
		// 通过devInfo获取devID
		DevCart devCart = m_singleMonitor.getDevCart();
		Device deviceInfo = devCart.getDeviceInfo();
		if(deviceInfo.type == 1 || deviceInfo.type == 4){
			StartMNRealPlay(devCart,m_avdecoder, m_singleMonitor,-1,true);
		}

		msg = new Message();
		msg.what = ENABLE_PLAYBUTTON;
		bundle = new Bundle();
		bundle.putInt("tag", -1);
		bundle.putInt("state", 0);
		msg.setData(bundle);
		controlHandler.sendMessage(msg);
		// 完成请求码流操作，无论成功失败
		m_singleMonitor.setCanChangeChn(true);
	}
	
	//TODO:蛮牛设备的请求视频处理
	private void StartMNRealPlay(DevCart devCart,DecoderDebugger avdecoder,Monitor monitor,int nWinIndex,boolean blnSingle){
		StartMNRealPlay(devCart.getDeviceInfo().sid, devCart.getChannelNum(), devCart.getDeviceInfo().online == 1, avdecoder, monitor, nWinIndex, blnSingle);
	}

	private void StartMNRealPlay(final String strUUID, final int nChannelIndex,final boolean blnOnLine, final DecoderDebugger avdecoder,final Monitor monitor, final int nWinIndex, final boolean blnSingle) {
		// 蛮牛设备的请求视频处理
		new Thread() {
			@Override
			public void run() {
				//设置界面初始的状态显示
				Message msg = null;
				Bundle bundle = null;						
				msg = new Message();
				if(blnSingle == true){
					msg.what = INIT_SINGLE_CONTROLS;
				}else{
					msg.what = INIT_EACHVIEW_CONTROLS;
				}
				bundle = new Bundle();
				bundle.putInt("tag", nWinIndex);
				msg.setData(bundle);
				controlHandler.sendMessage(msg);
				
				if (blnOnLine) {
					long lRet = SDK.ConnectChannelP2P(strUUID,nChannelIndex - 1, avdecoder, monitor,ConnectChannelP2PHandler);
					// 返回登录状态
					msg = new Message();
					msg.what = SHOW_CONNECTSUBTODEVICE_STATE;
					bundle = new Bundle();
					bundle.putInt("tag", nWinIndex);
					if (lRet == 0) {
						bundle.putInt("state", 1);
					} else {
						bundle.putInt("state", -2);// -2,-3
					}
					msg.setData(bundle);
					controlHandler.sendMessage(msg);
				} else {
					// 不在线提示
					msg = new Message();
					msg.what = SHOW_LOGINTODEVICE_STATE;
					bundle = new Bundle();
					bundle.putInt("tag", nWinIndex);
					bundle.putInt("state", -2);
					msg.setData(bundle);
					controlHandler.sendMessage(msg);
				}
			}
		}.start();
	}
	
	//软件处理.....................
//	public void setBitmap() {
//		_handler.sendEmptyMessage(XMSG.UPDATE_VIEW);
//	}
	
	public void drawBitmap() {
		if (ViESurfaceRenderer.bitmap == null)
			return;
		try {
			ViESurfaceRenderer.lckBitmap.lock();
			Canvas videoCanvas = m_singleSurface.getHolder().lockCanvas();
			if (videoCanvas != null) {
				videoCanvas.drawBitmap(ViESurfaceRenderer.bitmap, ViESurfaceRenderer.srcRect, ViESurfaceRenderer.dstRect, null);
			}
			if (videoCanvas != null) {
				m_singleSurface.getHolder().unlockCanvasAndPost(videoCanvas);
				videoCanvas = null;
			}
			ViESurfaceRenderer.lckBitmap.unlock();
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		} 
	}
	
	//软件处理.....................
	
	//单路刷新处理
	public void startSingleRequestVideoThread() {
		if(m_devCartsList.size() == 0){			 
			return;
		}
		new Thread() {
			@Override
			public void run() {
				try {
					runSingleRequsetVideoThread(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
	}
	//多路刷新处理
	public void startMultiRequestVideoThread(int sender) {
		final int tag = sender;
		if(m_devCartsList == null || m_devCartsList.size() == 0)
		{			 
			return ;
		}
		new Thread() {
			@Override
			public void run() {
				runMultiRequestVideoThread(tag);
			}

		}.start();
	}
	
	/**
	 * 请求视频线程
	 * 
	 * @throws InterruptedException
	 */
	private void runSingleRequsetVideoThread(int sender) throws InterruptedException{
		// 若sender = 1，则断开连接，重新连接。
		/*int tag = sender;
		boolean isTimeout = false; // 视频数据是否超时
		Message msg = null;
		Bundle bundle = null;*/
		// 显示加载控件
		controlHandler.sendEmptyMessage(INIT_SINGLE_CONTROLS);

		m_singleMonitor.setCanChangeChn(false);

		// 通过devInfo获取devID
		DevCart devCart = m_singleMonitor.getDevCart();
		refreshStartMNRealPlay(devCart,m_avdecoder,m_singleMonitor,0,true);	
		
		// 完成请求码流操作，无论成功失败
		m_singleMonitor.setCanChangeChn(true);
		
	}
	
	//多画面打开- 1
	public void startMultiLoginRequestVideoThread() {
		if(m_devCartsList.size() == 0)
		{			 
			return ;
		}
		new Thread() {
			@Override
			public void run() {
				runMultiLoginRequestVideoThread();
			}

		}.start();
	}
	
	/** 登录设备并请求视频线程    多画面打开- 2 */
	private void runMultiLoginRequestVideoThread() {		
		if(m_devCartsList.size() == 0){
			return;
		}
		// 1、判断有哪几台设备设备
		List<Device> devslist = new ArrayList<Device>();
		for (int i = 0; i < m_devCartsList.size(); i++) {
			DevCart devCart = m_devCartsList.get(i);
			if (!devslist.contains(devCart.getDeviceInfo())) {
				devslist.add(devCart.getDeviceInfo());
			}			
		}

		// 2、创建设备
		for (int i = 0; i < devslist.size(); i++) {
			Device devInfo = devslist.get(i);
			if(devInfo.type== 1){
				//增加蛮牛设备的创建处理
				int nIndex = 0;
				for (Monitor monitor : m_monitorsList) {
					nIndex++;
					DevCart devCart = monitor.getDevCart();
					if (devCart== null)
						continue;
	
					if (devCart.getDeviceInfo().sid.equals(devInfo.sid)) {
						System.out.println(devInfo.sid + "--"+devInfo.devname+"--"+nIndex);
						monitor.setStatus(0);
						StartMNRealPlay(devCart, monitor.getAVDecoder(), monitor,nIndex,false);
					}
				}
				
			}else{
				/*int devID = m_zlvss.devmanager_get_devID_fromDeviceInfo(devInfo);
				// 分配ID
				for (Monitor monitor : m_monitorsList) {
					if (monitor.getDevCart() == null)
						continue;
	
					if (monitor.getDevCart().getDeviceInfo().getDjLsh()
							.equals(devInfo.getDjLsh())) {
						monitor.setDevID(devID);
						monitor.setStatus(0);
						monitor.setLogin_state(1);
						monitor.setCanPTZ(true);
						monitor.setCanRecord(true);
						monitor.setCanShot(true);
						monitor.setCanTalk(true);
						monitor.setCanSwitchStream(true);
					}
				}*/
			}
		}
		// 添加moniter所有具有的设备ID（唯一）(以下代码对原有VVNAT和智诺VSS的设备处理有效)
		/*List<Integer> devIDList = new ArrayList<Integer>();
		for (Monitor monitor : m_monitorsList) {
			if (monitor.getDevID() == 0)
				//break;
				continue;//2016.02.18 李德明添加，因为增加了蛮牛设备的混合处理，因此此处需要继续找下去
			boolean isExist = false;
			for (int i = 0; i < devIDList.size(); i++) {
				Integer devID = devIDList.get(i);
				if (devID.intValue() == monitor.getDevID()) {
					isExist = true;
				}
			}
			if (!isExist) {
				devIDList.add(Integer.valueOf(monitor.getDevID()));
			}
		}

		CountDownLatch latch = new CountDownLatch(devIDList.size());
		// 3、开启登录设备并请求视频线程
		for (int i = 0; i < devIDList.size(); i++) {
			Log.d(TAG, i + " - 开始登陆设备");

			int devID = devIDList.get(i).intValue();
			// 登录操作
			new DeviceLoginThread(latch, devID).start();

		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < devIDList.size(); i++) {
			Log.d(TAG, i + " - 开始请求设备");
			int devID = devIDList.get(i).intValue();
			//new RequestVideoThread(devID).start();
		}*/
	}
	
	private int m_OldMonitorDevID= -1;
	private int m_OldMonitorChannelIndex=-1;
	private void EnablePlayViewButtons(Monitor monitor) {
		if (monitor != null) {
			if (monitor.devCart != null) {
				if (monitor.devCart.getDev_id() == m_OldMonitorDevID && monitor.devCart.getChannelNum() == m_OldMonitorChannelIndex) {
					return;
				} else {
					m_OldMonitorDevID = monitor.devCart.getDev_id();
					m_OldMonitorChannelIndex = monitor.devCart.getChannelNum();
				}
				EnablePlayViewButtons(monitor.getCanShot(),
						monitor.getCanTalk(), monitor.getCanPTZ(),
						monitor.getCanRecord(), monitor.getCanSwitchStream());
			} else {
				m_OldMonitorDevID = -1;
				m_OldMonitorChannelIndex = -1;
				EnablePlayViewButtons(false, false, false, false, false);
			}

		}
	}

	private void EnablePlayViewButtons(boolean blnCanShot, boolean blnCanTalk,
			boolean blnCanPTZ, boolean blnCanRecord, boolean blnCanSwithStream) {
		// TODO:根据配置隐藏界面上显示的不能使用按钮
		if (blnCanSwithStream) {
			enableStreamButton();
		} else {
			disableStreamButton();
		}
		m_toolView04.setEnabled(blnCanPTZ);
		/*if (!blnCanPTZ) {
			if (m_ptzPager.getVisibility() == View.VISIBLE) {
				((ImageButton) m_toolView04
						.findViewById(R.id.realplayer_tool_btn04))
						.setImageResource(R.drawable.toorbar_ptz_default);
				((TextView) m_toolView04
						.findViewById(R.id.realplayer_tool_tv04))
						.setTextColor(Color.rgb(190, 190, 190));
				m_toolView04.setEnabled(false);
				m_ptzPager.setVisibility(View.GONE);
				m_choseViewIndex = 0;
			}
			if (relative_ptz_layout.getVisibility() == View.VISIBLE) {
				relative_ptz_layout.setVisibility(View.GONE);
				ptz_isShown = false;
			}
		}*/
		m_toolView02.setEnabled(blnCanShot);
		if (!blnCanShot && isShot == true) {
			isShot = false;
		}
		m_toolView03.setEnabled(blnCanTalk);
		if (!blnCanTalk && isRecording) {
			// TODO:停止语音对讲的处理
		}
		m_toolView05.setEnabled(blnCanRecord);
		if (!blnCanRecord) {
			// TODO:处理正在录像的窗口的状态
		}
	}
	
	//退出activity
	public void exitDeleteDevice() {
		if (m_hadBack) {
			return;
		}
		m_hadBack = true;
		if (m_devNum == 1) {
			if (m_isRecord) {
				APP.ShowToast(getText(R.string.alertMsg13).toString());
				m_hadBack = false;
				return;
			}
		} else {
			if (m_isRecord) {
				APP.ShowToast(getText(R.string.alertMsg13).toString());
				m_hadBack = false;
				return;
			}
		}
		
		m_isExit = true;
		if (m_devNum == 1) {
			int devID = m_singleMonitor.getDevID();
			int chnNum = m_singleMonitor.getDevCart().getChannelNum();
			int isSub = m_singleMonitor.getIsSubStream();
		} else {
			// 停止视频请求
			for (int i = 0; i < m_monitorsList.size(); i++) {
				Monitor monitor = m_monitorsList.get(i);

				if (monitor.getDevCart() == null)
					continue;

				int devID = monitor.getDevID();
				int chnNum = monitor.getDevCart().getChannelNum();
				int isSub = monitor.getIsSubStream();

			}
		}
		
		//释放硬解码器、并关闭连接
		releaseDecoder();
		
		if (m_devNum == 1) {
			m_singleMonitor.imageData = null;
			if (m_singleMonitor.getImageBitmap() != null)
				m_singleMonitor.getImageBitmap().recycle();
		} else {
			for (Monitor monitor : m_monitorsList) {
				monitor.imageData = null;
				if (monitor.getImageBitmap() != null)
					monitor.getImageBitmap().recycle();
			}
		}
		System.gc();
		// 退出当前页面
		finish();
	}
	private int[] pixels;
	private void layout(){
		if(pixels[0] > pixels[1]){
			//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			params.width = pixels[0];
			params.height = pixels[1];
			_playview4ui.setLayoutParams(params);
			//如果是软解 要改变画图的尺寸
			m_chooseMonitor.setDrawRect(new Rect(0, 0, pixels[0], pixels[1]));
		}else{
			params.width = _verticalScreenWidth;
			params.height = 0;
			_playview4ui.setLayoutParams(params);
			m_chooseMonitor.setDrawRect(new Rect(0, 0, _verticalScreenWidth, _verticalScreenHeigth+20));
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//SDK.ClearChannelIFrameStatus();
		isScreenChange(0,0);
		pixels = getSize();
		if(m_devNum == 1){
			layout();
		}
	}
	
	int _flag = 0;
	@SuppressWarnings("static-access")
	public void isScreenChange(int width,int height) {
		Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
		int ori = mConfiguration.orientation ; //获取屏幕方向
		if(ori == mConfiguration.ORIENTATION_LANDSCAPE){//横屏
			if(_flag == 0){
				_flag = 1;
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
				_hreadframeLayout.setVisibility(View.GONE);
				_bottomMemo.setVisibility(View.GONE);
				_middleMenu.setVisibility(View.GONE);
				m_dataFlowView.setVisibility(View.GONE);
			}
		}else if(ori == mConfiguration.ORIENTATION_PORTRAIT){//竖屏
			_flag = 0;
			_hreadframeLayout.setVisibility(View.VISIBLE);
			_bottomMemo.setVisibility(View.VISIBLE);
			_middleMenu.setVisibility(View.VISIBLE);
			m_dataFlowView.setVisibility(View.VISIBLE);
		}

	}
	
	
	/**Surface callback*/
	private class MSurfaceCallback implements SurfaceHolder.Callback{
		private int index;//0：单画面
		public MSurfaceCallback(int index){
			this.index=index;
		}
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			/*switch(index){
			case 0:
				if(m_avdecoder == null){
					m_avdecoder = new DecoderDebugger(holder.getSurface(),352,288);
					DevCart devCart = m_singleMonitor.getDevCart();
					if(devCart!=null && devCart.getDeviceInfo()!=null){
						if(devCart.getDeviceInfo().type == 1){
							String strDevUUID = devCart.getDeviceInfo().sid;
							int nChannelIndex = devCart.getChannelNum()-1;
							m_chooseMonitor.setAVDecoder(m_avdecoder);
							SDK.SetAVDecoder(strDevUUID,nChannelIndex, m_avdecoder);
						}
					}
				}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				if(m_monitorsList.get(index-1).getAVDecoder()==null){
					m_monitorsList.get(index-1).setAVDecoder(new DecoderDebugger(holder.getSurface(),352,288));
					DevCart devCart = m_monitorsList.get(index-1).getDevCart();
					if(devCart!=null && devCart.getDeviceInfo()!=null){
						if(1 == devCart.getDeviceInfo().type){
							String strDevUUID = devCart.getDeviceInfo().sid;
							int nChannelIndex = devCart.getChannelNum()-1;
							SDK.SetAVDecoder(strDevUUID,nChannelIndex, m_monitorsList.get(index-1).getAVDecoder());
						}
					}
				}
				break;
			default:
				break;	
			}*/
			
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			switch(index){
			case 0:
				if(m_avdecoder == null){
					m_avdecoder = new DecoderDebugger(holder.getSurface(),352,288);
					DevCart devCart = m_singleMonitor.getDevCart();
					if(devCart!=null && devCart.getDeviceInfo()!=null){
						if(devCart.getDeviceInfo().type == 1){
							String strDevUUID = devCart.getDeviceInfo().sid;
							int nChannelIndex = devCart.getChannelNum()-1;
							m_chooseMonitor.setAVDecoder(m_avdecoder);
							SDK.SetAVDecoder(strDevUUID,nChannelIndex, m_avdecoder);
						}
					}
				}
				//isScreenChange(width,height);
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				if(m_monitorsList.get(index-1).getAVDecoder() != null){
					m_monitorsList.get(index-1).setDrawRect(new Rect(0, 0, width, height));
				}else if(m_monitorsList.get(index-1).getAVDecoder() == null){
					m_monitorsList.get(index-1).setAVDecoder(new DecoderDebugger(holder.getSurface(),352,288));
					DevCart devCart = m_monitorsList.get(index-1).getDevCart();
					if(devCart!=null && devCart.getDeviceInfo()!=null){
						if(1 == devCart.getDeviceInfo().type){
							String strDevUUID = devCart.getDeviceInfo().sid;
							int nChannelIndex = devCart.getChannelNum()-1;
							SDK.SetAVDecoder(strDevUUID,nChannelIndex, m_monitorsList.get(index-1).getAVDecoder());
						}
					}
				}
				//isScreenChange(width,height);
				
				break;
			default:
				break;	
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			switch(index){
			case 0:
				if(m_avdecoder!=null){
				m_avdecoder.release();
				m_avdecoder=null;
//				m_singleMonitor.setAVDecoder(null);
				}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
				//多画面时 如果放大了 别的窗口不用调画图片方法
				if(m_isFullView && m_monitorsList.size() > 0){
//					android.os.Message msg = new android.os.Message();
//					msg.what = 103;
//					msg.arg1 = index;
//					controlHandler.sendMessage(msg);
					
					new Thread(new Runnable() {
						@Override
						public void run() {
							if(m_monitorsList.get(index-1).getAVDecoder()!=null){
								m_monitorsList.get(index-1).getAVDecoder().release();
								m_monitorsList.get(index-1).setAVDecoder(null);
							}
						}
					}).start();
					
				}else{
					if(m_monitorsList.get(index-1).getAVDecoder()!=null){
						m_monitorsList.get(index-1).getAVDecoder().release();
						m_monitorsList.get(index-1).setAVDecoder(null);
					}
				}
				break;
			default:
				break;	
			}
			
		}
		
	}
	
	// 设置软解--绘图视图尺寸 
	private class OnSurfacePreDrawListener implements OnPreDrawListener {
		Monitor monitor = null;
		SurfaceView surface = null;

		OnSurfacePreDrawListener(Monitor monitor, SurfaceView surface) {
			this.monitor = monitor;
			this.surface = surface;
		}

		@Override
		public boolean onPreDraw() {
			int eventwidth = surface.getMeasuredWidth();
			int eventheight = surface.getMeasuredHeight();
			LogUtil.d(TAG, "eventwidth:"+eventwidth+" eventheight:"+eventheight);
			monitor.setDrawRect(new Rect(0, 0, eventwidth, eventheight));

			surface.getViewTreeObserver().removeOnPreDrawListener(this);

			return true;
		}

	}
	
	private void showAlertDialog(int messageId) {
		AlertDialog.Builder alert = new AlertDialog.Builder(Fun_RealPlayerActivity.this);
		alert.setTitle(R.string.tip_title);
		alert.setMessage(messageId);
		alert.setPositiveButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alert.show();
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isRecording == true) {
				Toast.makeText(Fun_RealPlayerActivity.this, R.string.stop_talk,Toast.LENGTH_SHORT).show();
				return false;
			}
			exitDeleteDevice();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		ViESurfaceRenderer._bufferAndBitmap.clear();
		instance = null;
	}
	
	//............消息处理
	private boolean IsSingleViewType = true;
	//打洞返回值的消息处理
	private Handler ConnectChannelP2PHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle bdData = msg.getData();
			// 创建通道
			String strUUID = bdData.getString("DeviceUUID");
			int nChannelIndex = bdData.getInt("ChannelIndex");
			long lContext = bdData.getLong("Context");
			int nStatus = bdData.getInt("Status");
			int nWinIndex = -1;
			Monitor monitor = null;
			int nCount = m_devCartsList.size();
			Message msgNew = new Message();
			if (IsSingleViewType == true) {
				monitor = m_singleMonitor;
			} else {
				// TODO:需要找到对应的monitor和窗口序号
				for (int n = 0; n < nCount; n++) {
					Monitor mt = m_monitorsList.get(n);
					DevCart devCart = mt.getDevCart();

					if (devCart != null && devCart.getDeviceInfo() != null
							&& devCart.getDeviceInfo().sid.equals(strUUID)
							&& devCart.getChannelNum() == nChannelIndex + 1) {
						msgNew.what = PLAYING_EACHVIEW_CONTROLS;
						monitor = mt;
						nWinIndex = n + 1;
					}
				}
			}
			
			if (msg.what != 0 && msg.what != -1) {//去掉加载框
				if (msg.what == PLAYING_EACHVIEW_CONTROLS) {
					if (IsSingleViewType == true) {
						msgNew.what = PLAYING_SINGLE_CONTROLS;
					} else {
						msgNew.what = PLAYING_EACHVIEW_CONTROLS;
					}
					Bundle bundle = new Bundle();
					bundle.putInt("tag", nWinIndex);
					msgNew.setData(bundle);
					controlHandler.sendMessage(msgNew);
				}
			}else{
				if (nStatus == 0){// p2pConnect成功
					System.out.println("TEST20160321   P2pConnect成功   ChannelIndex:"+ nChannelIndex);
					long lRet = SDK.CreateChannelP2P(strUUID, nChannelIndex,lContext/* , avdecoder, monitor */);
					if (lRet == 0) {
						// 创建通道成功
						if (IsSingleViewType == true) {
							msgNew.what = PLAYING_SINGLE_CONTROLS;//去掉加载框
						} else {
							msgNew.what = PLAYING_EACHVIEW_CONTROLS;
						}
						Bundle bundle = new Bundle();
						bundle.putInt("tag", nWinIndex);
						msgNew.setData(bundle);
						controlHandler.sendMessage(msgNew);
						if (monitor != null) {
							monitor.setPlay_status(1);
						}
					} else {
						// TODO:创建通道失败
						msgNew.what = SHOW_CONNECTSUBTODEVICE_STATE;
						Bundle bundle = new Bundle();
						bundle.putInt("tag", nWinIndex);
						bundle.putInt("state", -2);// -2||-3 连接失败
						msgNew.setData(bundle);
						controlHandler.sendMessage(msgNew);
						if (monitor != null) {
							monitor.setPlay_status(0);
						}
					}
				} else {
					if (nStatus == -5000/* || nStatus == -4999 */) {
						// 调打洞失败的处理
						SDK.CloseP2PConnect(strUUID, nChannelIndex);
					}
					// ConnectP2P失败的处理
					msgNew.what = SHOW_CONNECTSUBTODEVICE_STATE;
					Bundle bundle = new Bundle();
					bundle.putInt("tag", nWinIndex);
					bundle.putInt("state", -2);// -2||-3 连接失败
					msgNew.setData(bundle);
					controlHandler.sendMessage(msgNew);
					if (monitor != null) {
						monitor.setPlay_status(0);
					}
				}
			}
			
		}
	};
	
	private Handler controlHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_SINGLE_CONTROLS: {
				showProgressBar(-1);
				showDetailText(-1, R.string.playerview_detailtext_connecting);
				hideRefreshButton(-1);
				disableCloseButton();
				disableStreamButton();
			}
				break;
			case PLAYING_SINGLE_CONTROLS: {
				hideProgressBar(-1);
				hideRefreshButton(-1);
				hideDetailText(-1);
				enableStreamButton();
			}
				break;
			case STOP_SINGLE_CONTROLS: {
				int tag = msg.getData().getInt("tag");
				showDetailText(tag, R.string.playerview_detailtext_stopped);
			}
				break;
			case INIT_MULTI_CONTROLS: {
				disableCloseButton();
				disableStreamButton();
			}
				break;
			case INIT_EACHVIEW_CONTROLS: {
				int tag = msg.getData().getInt("tag");
				showProgressBar(tag);
				showDetailText(tag, R.string.playerview_detailtext_connecting);
				hideRefreshButton(tag);
				hideAddButton(tag);
			}
				break;
			case PLAYING_EACHVIEW_CONTROLS: {
				int tag = msg.getData().getInt("tag");
				hideProgressBar(tag);
				hideRefreshButton(tag);
				hideAddButton(tag);
				hideDetailText(tag);
			}
				break;
			case STOP_EACHVIEW_CONTROLS: {
				int tag = msg.getData().getInt("tag");
				// 播放和暂停按钮
				if(!m_isFullView)
					enableCloseButton();
				disableStreamButton();

				showDetailText(tag, R.string.playerview_detailtext_stopped);
			}
				break;
			case CLOSE_EACHVIEW_CONTROLS: {//关闭
				int tag = msg.getData().getInt("tag");
				disableCloseButton();
				hideDetailText(tag);
				showAddButton(tag);
			}
				break;
			case PLAYING_EACHVIEW_BUTTONS: {
				if(!m_isFullView)
					enableCloseButton();
				enableStreamButton();
			}
				break;
			case STOP_EACHVIEW_BUTTONS: {
				if(!m_isFullView)
					enableCloseButton();
				disableStreamButton();
			}
				break;
			case SHOW_LOGINTODEVICE_STATE: {
				int tag = msg.getData().getInt("tag");
				int state = msg.getData().getInt("state");
				show_loginToDevice_state(tag, state);
			}
				break;
			case SHOW_CONNECTVIDEOTODEVICE_STATE: {
				int tag = msg.getData().getInt("tag");
				int state = msg.getData().getInt("state");
				show_connectVideoToDevice_state(tag, state);
			}
				break;
			case SHOW_REQUESTVIDEOTODEVICE_STATE: {
				int tag = msg.getData().getInt("tag");
				int state = msg.getData().getInt("state");
				show_requestVideoFromDevice_state(tag, state);
			}
				break;
			case SHOW_CONNECTSUBTODEVICE_STATE: {
				int tag = msg.getData().getInt("tag");
				int state = msg.getData().getInt("state");
				show_connectSubToDevice_state(tag, state);
			}
				break;
			case ENABLE_PLAYBUTTON:
				int tag = msg.getData().getInt("tag");
				int state = msg.getData().getInt("state");
				//show_requestVideoFromDevice_state(tag, state);
				if(tag==-1){
					EnablePlayViewButtons(m_singleMonitor);
				}else{
					EnablePlayViewButtons(m_monitorsList.get(tag - 1));
				}
			break;
			case XMSG.PLAY_SNAP:
				APP.ShowToast(getText(R.string.Video_snap_success).toString());//截图成功
				break;
			case 100:
				APP.ShowToast(getText(R.string.Video_snap_error).toString());//截图失败
				File temFile = new File(_fileName);
				if(temFile.exists()) temFile.delete();
				break;
			case 101://录像功能
				recordCount ++;
				File efile = new File(_recordfileName + ".bmp");
				if(recordCount < 10){
					if(efile.exists() && efile.length() >= 51200){
						SDK.SetVideoPath(_recordfileName + ".h264",_recordfileName + ".aac",SDK._shotContext);
						m_isRecord = true;
						stopNotify();
						recordCount = 0;
					}
				}else{
					recordCount = 0;
					efile.delete();
					SDK._shotContext = -1;
					stopNotify();
					APP.ShowToast(getText(R.string.Video_record_error).toString());
					break;
				}
				if (_bNotify) {
					controlHandler.sendEmptyMessageDelayed(101, 500); //延迟发送
				}
				break;
//			case 102:
//				doublePlayViewEvent(m_multiPlayView01);
//				doublePlayViewEvent(m_multiPlayView01);
//				break;
//			case 103:
//				int index = msg.arg1;
//				if(m_monitorsList.get(index-1).getAVDecoder()!=null){
//					m_monitorsList.get(index-1).getAVDecoder().release2();
//					m_monitorsList.get(index-1).setAVDecoder(null);
//				}
//				break;
			default:
				break;
			}
		}
	};
	
	/** 返回登录状态 */
	private void show_loginToDevice_state(int tag, int state) {
		Log.d(TAG, "TAG = " + tag + ",登录状态 = " + state);
		/*
		 * 登录：-1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -11:登录数据发送失败 -12:登录超时 -21:密码错误
		 * -22:设备锁定 -23:账号不存在 -24:账号已登录
		 */
		if (state < 0) {
			hideProgressBar(tag);
			hideAddButton(tag);
			// -1:P2P服务器未连上
			if (state == -1) {
				showDetailText(tag, R.string.playerview_detailtext_P2PError);
			}
			// -2||-3:连接失败
			else if (state == -2 || state == -3) {
				showDetailText(tag, R.string.playerview_detailtext_devFailed);
			}
			// -11:登录数据发送失败
			else if (state == -11) {
				showDetailText(tag, R.string.playerview_detailtext_loginFailed);
			}
			// -12:登录超时
			else if (state == -12) {
				showDetailText(tag, R.string.playerview_detailtext_devTimeout);
			}
			// -21:密码错误
			else if (state == -21) {
				showDetailText(tag, R.string.playerview_detailtext_pwdError);
			}
			// -22:设备锁定
			else if (state == -22) {
				showDetailText(tag, R.string.playerview_detailtext_devLock);
			}
			// -23:账号不存在
			else if (state == -23) {
				showDetailText(tag, R.string.playerview_detailtext_devNoUser);
			}
			// -24:账号已登录
			else if (state == -24) {
				showDetailText(tag, R.string.playerview_detailtext_devLogged);
			} else {
				showDetailText(tag, R.string.playerview_detailtext_error01);
			}
			showRefreshButton(tag);
			if (tag == -1) {
				disableCloseButton();
			} else {
				enableCloseButton();
			}
			return;
		}

	}
	
	/** 返回辅连接请求状态 */
	private void show_connectSubToDevice_state(int tag, int state) {
		LogUtil.d(TAG, "TAG = " + tag + ",辅连接请求状态 = " + state);
		/*
		 * 创建并注册辅连接 -1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -11:注册数据发送失败 -12:注册回调超时
		 * 1:注册成功 -21:注册失败（已存在）
		 */
		if (state < 0) {
			hideProgressBar(tag);
			hideAddButton(tag);
			// -1:P2P服务器未连上
			if (state == -1) {
				showDetailText(tag, R.string.playerview_detailtext_P2PError);
			}
			// -2||-3:连接失败
			else if (state == -2 || state == -3) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			}
			// -11:注册数据发送失败
			else if (state == -11) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			}
			// -12:注册回调超时
			else if (state == -12) {
				showDetailText(tag, R.string.playerview_detailtext_chnTimeout);
			}
			// -21:注册失败（已存在）
			else if (state == -21) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			} else {
				showDetailText(tag, R.string.playerview_detailtext_error02);
			}

			showRefreshButton(tag);

			return;
		}
	}
	/** 返回请求视频数据状态 */
	private void show_requestVideoFromDevice_state(int tag, int state) {
		LogUtil.d(TAG, "TAG = " + tag + "请求视频数据状态 = " + state);
		/*
		 * 请求视频请求 -1:主连接不存在 -2:通道连接不存在 -11:请求视频数据发送失败 -12:请求视频回调超时 1:请求视频成功
		 * -21:请求视频失败
		 */
		hideProgressBar(tag);
		hideAddButton(tag);
		if (state < 0) {
			// -1 -2 -11:请求视频数据发送失败
			if (state == -1 || state == -2 || state == -11) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			}
			// -12:请求视频回调超时
			else if (state == -12) {
				showDetailText(tag, R.string.playerview_detailtext_chnTimeout);
			}
			// -21:请求视频失败
			else if (state == -21) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			} else {
				showDetailText(tag, R.string.playerview_detailtext_error04);
			}

			showRefreshButton(tag);

			return;
		}
	}
	/** 返回通道请求状态 */
	private void show_connectVideoToDevice_state(int tag, int state) {
		LogUtil.d(TAG, "TAG = " + tag + ",通道请求状态 = " + state);
		/*
		 * 通道连接：-1:P2P服务器未连上 -2:P2P连接失败 -3:IP连接失败 -11:注册数据发送失败 -12:注册回调超时 1:注册成功
		 * -21:注册失败（已存在）
		 */
		if (state < 0) {
			hideProgressBar(tag);
			hideAddButton(tag);
			// -1:P2P服务器未连上
			if (state == -1) {
				showDetailText(tag, R.string.playerview_detailtext_P2PError);
			}
			// -2||-3:连接失败
			else if (state == -2 || state == -3) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			}
			// -11:注册数据发送失败
			else if (state == -11) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			}
			// -12:注册回调超时
			else if (state == -12) {
				showDetailText(tag, R.string.playerview_detailtext_chnTimeout);
			}
			// -21:注册失败（已存在）
			else if (state == -21) {
				showDetailText(tag, R.string.playerview_detailtext_chnFailed);
			} else {
				showDetailText(tag, R.string.playerview_detailtext_error03);
			}
			showRefreshButton(tag);

			return;
		}
	}
	
	/**截图*/
	private Handler screenHandler=new Handler(){
		public void handleMessage(Message msg) {			
			//btnPhotoClick();
		};
	};
	
}
