package com.basic;

public class XMSG {
	public final static int GET_CAMCHN = 1;
	public final static int UPDATE_VIEW = 2;
	public final static int SHOW_LOGINDLG = 3;
	public final static int QUERYRECORD = 4;
	public final static int LOGIN = 5;
	public final static int LOGOUT = 6;
	public final static int SELECTED_FUN = 7;
	public final static int SELECTED_SUB_FUN = 8;
	public final static int PLAY = 9; //视频播放
	public final static int GPS_UPDATE = 10;
	public final static int UPDATE_SOUND = 11;
	public final static int PLAY_FILE = 12;
	public final static int GetNotify = 13;
	public final static int UPDATE_TIME_RATE = 14;
	public final static int PLAY_ALARM_FILE = 15;
	public final static int ON_CLICKED_PTZ = 16;
	//public final static int REAL_GetNotify = 17; //实时报警
	public final static int UPDATE_REAL_ALARM = 18;//更新实时报警
	public final static int REALDATA = 19;//传感器实时数据
	public final static int UPDATEA_APP = 20;
	public final static int CROSS_SCREEN = 22;//横屏
	public final static int VERTICAL_SCREEN = 23;//竖屏
	public final static int HeartBeat = 24;//牛眼发送心跳
	public final static int CHANGE_TEXTVALUE = 100;//改变采集视频上面的文字
	public final static int CHECK_DEVICE = 101;//检查模拟设备有没有开通
	
	
	public final static int CREATECHANLL = 101;
	public final static int Alias = 102; //jpush设置别名
	public final static int P2PConnect = 103;//创建连接

	public final static int SMS_P2PConnect = 104;//创建连接错误消息
	public final static int ON_PLAY = 105;//不能播放视频
	public final static int PLAY_SNAP = 106;//截图成功
	public final static int PLAY_GPU = 107;//软硬切换
	
	public final static int LOCATION = 108;//定位
	//模拟
	public final static int ANALOG_IS_LOGIN = 200;//判断IDM有没有登录成功
	public final static int PLAY_GPU_OK = 201;//硬解-GPU
	//云端
	public final static int DEVICE_LIST_LOAD = 300;
	public final static int PLAY_CLOSE_WAIT = 301;
	public final static int DEVICE_LIST_ISREFRESH = 302;
	
	public final static int LOGIN_USER_ERROR = -1;
	public final static int LOGIN_USER_DISABLE = -2;
	public final static int LOGIN_NETWORK_ERROR = -3;
	
	
}