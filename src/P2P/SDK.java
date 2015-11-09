package P2P;

import java.util.Set;

import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.manniu.manniu.R;
import com.nmbb.vlc.ui.VlcVideoActivity;
import com.utils.DevSetHandler;
import com.utils.LogUtil;
import com.utils.ScreenHandler;
import com.views.BaseApplication;
import com.views.Main;
import com.views.NewMain;
import com.views.NewSurfaceTest;
import com.views.analog.camera.audio.AudioQueue;

public class SDK {

	private static String[] strArr;
	private static ScreenHandler screenHandler = new ScreenHandler();
	private static DevSetHandler devSetHandler = new DevSetHandler();
	// 0 是音频 1 是视频 后面三个字节填
//	src:源数组；	srcPos:源数组要复制的起始位置；
//	dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。
	//chnl:0
	//type:类型   0：视频 1：音频 2：语音对讲
	//isIFrame		1：i帧，否则为p帧
	//static byte[] newbuf = new byte[60*1024];
	@SuppressWarnings("static-access")
	public void onData(int chnl,int type,int isIFrame,byte[] data, int length) {
		if(NewSurfaceTest.instance!=null && NewSurfaceTest._playId > 0 && NewSurfaceTest.instance._decoderDebugger != null){
		//if(NewSurfaceTest.instance != null){
			if(NewMain.devType == 1 && data.length > 1){
				//Log.i("SDK", "接收数据.........................."+length);
				//int exHead =  Integer.valueOf(G.byte2hex(data, 22, 1));
//				int exHead = (int)data[22];
//				int realHead = 24 + exHead ;
//				int realLen = length - realHead - 8;
//				byte[] newbuf = new byte[realLen];
//				System.arraycopy(data, realHead, newbuf, 0, realLen);
//				NewSurfaceTest.instance._decoderDebugger.setData(newbuf,realLen);
				
				//硬、软解方法
				if(NewSurfaceTest.instance._decoderQueue != null) {
					NewSurfaceTest.instance._decoderQueue.addData(data,length,isIFrame);
				}
				
				/*if(NewSurfaceTest.instance._decoderQueue != null && NewSurfaceTest.instance._decoderDebugger.isCanDecode()) {
					NewSurfaceTest.instance._decoderQueue.addData(data,length,isIFrame);
				}else{
					//软解直接把数据送到解码器测试
					NewSurfaceTest.instance._deThead.addData(data, length);
				}*/
				
				//NewSurfaceTest.instance._deThead.addData(data, length);
				
			} else{
				//Log.i("SDK", "type:"+type);
				if(NewSurfaceTest.instance._decoderQueue == null) return;
				if(type == 0){//视频
					if(data != null && data.length > 0){
						NewSurfaceTest.instance._decoderQueue.addData(data,length,isIFrame);
						
//						LogUtil.d("SDK", "收视频..."+data.length+" type: "+type+"--"+data[0]+","+data[1]+","+data[2]+","+data[3]+","+data[4]+"------"+data[data.length-1]+","+data[data.length-2]+","+data[data.length-3]+","+data[data.length-4]+","+data[data.length-5]
//								+"------"+data[data.length-6]+","+data[data.length-7]+","+data[data.length-8]+","+data[data.length-9]+","+data[data.length-10]);
					}
				}else if(type == 1){//音频
					if(data != null && data.length > 0 && AudioQueue.runFlag){
						
//						if(data[0]==-1 && (data[1] & -16)==-16){
							AudioQueue.addSound(data);
//						}else{
							LogUtil.d("SDK", "收声音..."+data.length+" type: "+type+"--"+data[0]+","+data[1]+","+data[2]+","+data[3]+","+data[4]+"------"+data[data.length-1]+","+data[data.length-2]+","+data[data.length-3]+","+data[data.length-4]+","+data[data.length-5]
									+"------"+data[data.length-6]+","+data[data.length-7]+","+data[data.length-8]+","+data[data.length-9]+","+data[data.length-10]);
//						}
					}
				}
			}
		}
	}
	
	/*
	 * C_MSG_CREATE_CHANNEL = 10,		//创建通道(通道信息在 C_Channel_T* param)
		C_MSG_CLOSE_CHANNEL,			//关闭通道(通道信息在 C_Channel_T* param)
		C_MSG_SET_CONFIG,				//设置IPC(value=1, json格式，value=2, xml格式。数据在str里，长度在len里)
		C_MSG_GET_CONFIG,				//获取IPC配置（返回码<0失败，1返回json格式，2返回xml格式，数据在str里，长度在len里）
		C_MSG_SUBSCRIBE_ALARM,			//订阅报警（value=类型，0表示所有类型，〉0表示订阅对应类型报警。参见C_Alarm_T类型）
		C_MSG_DISSUBCRIBE_ALARM,		//取消订阅报警（参数参见订阅类型）
		C_MSG_UPGRADE,					//升级（value=1时，str表示升级文件位置及文件名，len表示长度；value=2时，str表示升级文件地址串（TFTP模式））
		C_MSG_SNAPSHOT,					//抓图（value=1，表示使用当前分辨率抓图，图片内容在str里，长度在返回值里。）
		C_MSG_SYNC_TIME,				//时间同步（value=1，表示全球相对时间，值在str里）
		C_MSG_ANNOUNCEMENT,				//公告（value=1表示短信，值在str里，长度在len里。value=2表示是广告，value=3表示是个人信息）
		C_MSG_RESTART,					//重启设备（value＝1表示用户请求，value=2表示系统维护）
		C_MSG_START_UPLOAD,				//开始上传（通过sendData传数据中间件，如果已经在P2P或上传视频了，中间件是不会调用的）
		C_MSG_STOP_UPLOAD,				//停止上传（结束start打开的请求）
		C_MSG_ASK_REAL_VIDEO,			//请求实时视频（报警后由服务端来获取实时视频）
		C_MSG_STOP_REAL_VIDEO,			//结束实时视频（服务端发起）
		C_MSG_PLAYBACK,					//请求录像
		C_MSG_DEV_STATUS,				//设备状态，打洞成功创建通道时返回，如果设备忙，则需要关闭打洞链接
		C_MSG_JSON_PCK,					//json数据包，用户自定义的json包
	 * 回调函数：
	 * 返回值 0 成功		<0 失败
	 * param3帧率 param4 宽 param5 高 param6 码率
	 *
	 * 返回：0表示成功，<0参照错误码
	* */
	protected static VideoQuality mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
	//protected VideoQuality mQuality = mRequestedQuality.clone(); 
	//public static List<Long> lessionList = new ArrayList<Long>();
	public static boolean isInitDecoder = false;//收到宽高标识位判断通道是否创建成功
	public static int _createChnlFlag = -1;//_createChnlFlag 标志位 0成功
	public static boolean analogPic = false;//模拟抓图
	public static int nFrameRate = 20;//帧率
	public void OnCommand(int cmd, long param1, int param2, int param3,
			int param4,int param5 ,int param6,int param7, String str1, String str2, String str3) {
		LogUtil.d("SDK", cmd + " -- " + param1 + "-- " + param2 + "--" + param3+ "--" + param4 + "--" + str1 + "--" + str2 + "--" + str3);
		if(cmd == 8){
			LogUtil.d("SDK", "被踢掉了");
		} else if(cmd == 27){
//			LogUtil.d("SDK", "data:" + str1);
//			LogUtil.d("SDK", "长度：" + param1);
			strArr = str1.split("\\|");
			JSONObject jsonData = JSON.parseObject(strArr[1]);
			if(jsonData.containsKey("action")&&"104".equals(jsonData.getString("action"))){//抓图
				if(jsonData.containsKey("result")){
					Message msg = new Message();
					Bundle data = new Bundle();
					data.putString("deviceId", strArr[0]);
					data.putInt("result", jsonData.getIntValue("result"));
					switch (jsonData.getIntValue("result")) {
					case 0:
						data.putString("url", jsonData.getString("file_url"));
						break;
					default:
						break;
					}
					msg.setData(data);
					screenHandler.sendMessage(msg);
				}
			}else if(jsonData.containsKey("action")&&"101".equals(jsonData.getString("action"))){//配置
				
				Set<String> keys = jsonData.keySet();
				for(String key : keys){
					LogUtil.d( "SDK", "key:" + key);
					if(jsonData.get(key) instanceof JSONObject){
						LogUtil.d( "SDK", key + " is JSONObject instance!");
					}else if(jsonData.get(key) instanceof JSONArray){
						LogUtil.d( "SDK", key + " is JSONArray instance!");
					}else{
						LogUtil.d( "SDK", key + " value is other instance! value " + jsonData.get(key));
					} 
				}
				Message msg = new Message();
				msg.what = 1;
				Bundle data = new Bundle();
				data.putString("deviceId", strArr[0]);
				data.putString("setting", jsonData.toJSONString());
				msg.setData(data);
				devSetHandler.sendMessage(msg);
				
				//Intent intent = new Intent("com.views.NewMainAddDev");
				//intent.putExtras(msg.getData());
				//sendBroadcast(intent);
				//CFHandler cfhandler = new NewDeviceSet.CFHandler();
				
			}
		}else if(cmd == 903){ //ETS会自动下线原有链接
			
		}
		if(cmd == 17){//模拟IPC抓图
			if(AnalogvideoActivity.instance != null){
				analogPic = true;
				AnalogvideoActivity.instance.onClick(AnalogvideoActivity.instance._btnPic);
			}
		}
		
		if(Main.Instance._curIndex == Main.XV_NEW_MSG){//牛眼
			if (cmd == 10) {
				LogUtil.d("SDK", "发送宽高。。="+VideoStream.mRequestedQuality.framerate+"--"+VideoStream.mRequestedQuality.bitrate*0.8+"--"+VideoStream.mRequestedQuality.resX+"--"+VideoStream.mRequestedQuality.resY);
				int ret = SendMediaInfo(param1, 0,1,0,VideoStream.mRequestedQuality.framerate,5,(int)(VideoStream.mRequestedQuality.bitrate*0.8),2,VideoStream.mRequestedQuality.resX,VideoStream.mRequestedQuality.resY);
				if(ret == 0 && AnalogvideoActivity.instance != null){
					_createChnlFlag = 0;
					LogUtil.d("SDK", "发数据。。。。。");
					AnalogvideoActivity.instance.startEncode();
					AnalogvideoActivity.instance.changeTextValue();
				}
			}else if(cmd == 11){//关闭通通-停止编码发送数据
				_createChnlFlag = -1;
				_sessionId = 0;
				AnalogvideoActivity.instance.stopEncode();
				AnalogvideoActivity.instance.changeTextValue();
			}
		}else if(Main.Instance._curIndex == Main.XV_NEW_MAIN){//云端
			if(cmd == 10){//获取宽高  第3  4  5 个参数分别是帧率  宽 高
				LogUtil.d("SDK","收到宽高：    "+param2+"--"+param3 + "--" + param4+ "--" + param5+"--"+param6+"--"+param7);
				if(param3 != 0) nFrameRate = param3;
				//NewSurfaceTest.instance._decoderDebugger.configureDecoder(param3,param4,param5,param6);
//				start();
				isInitDecoder = true;
			}else if(cmd == 26){//设备状态 == 1设备不在线
				if(param2 == 1 && NewSurfaceTest.instance != null) NewSurfaceTest.instance.closeNewSurface(param2);
			}
		}
	}
	
	/*
	 * 收到消息，参见下面枚举
		// value取值
		typedef enum
		{
			GW_LINK_IDM		= 0,		// IDM服务链接
			GW_LINK_SVR,				// ETS服务链接
			GW_LINK_P2P,				// P2P打洞链接
			GW_DEV_REMOVED,				// 设备被剔除（同一个账号在别的设备上登陆）
		}GW_LINK_CHANNEL;
		// status取值
		typedef enum
		{
			C_STATUS_P2P_CLIENT_OFFLINE = -5000,		// P2P对端掉线，对于IPC：需要停止发送通道数据并自动关闭通道
			// 对于手机、OCX客户端：停止播放音视频，并提示链接断线
			C_STATUS_SVR_DISCONNECTED,					// 服务器掉线，需要先执行登出操作（调用p2p_logout），再执行登陆（调用p2p_login），一直到登陆成功
		}C_CALLBACK_STATUS;
	 *	value						dst_uuid		sessionId		status
	 * GW_LINK_IDM:					NULL			0				C_STATUS_SVR_DISCONNECTED
	 * GW_LINK_P2P:					uuid			sessionId		0、成功  -5000 C_STATUS_P2P_CLIENT_OFFLINE
	 * GW_LINK_SVR:					NULL			0				C_STATUS_SVR_DISCONNECTED
	 * GW_DEV_REMOVED:				NULL			0				0
	 * 返回值 0 成功		<0 失败
	 * param3帧率 param4 宽 param5 高 param6 码率
	 * 返回：0表示成功，<0参照错误码
	 * C_STATUS_P2P_CLIENT_OFFLINE = -5000,	
	// 对于手机、OCX客户端：停止播放音视频，并提示链接断线
	C_STATUS_SVR_DISCONNECTED,		
	 * */
	public static boolean _isRun = true; //是否发送登录IDM
	public static long _sessionId = 0;
	public static boolean _isLogout = true;
	public void OnStatus(int value, long sessionId, int status, String dst_uuid){
		switch (value) {
		case 0:
			if(status == -4999){//牛眼-停止发送视频  云端 -关闭连接
				LogUtil.d("SDK", "value = 0 IDM  onStatus:"+status);
				if(_isRun) {
					_isRun = false;
					BackLoginThread.state = 1;
					Main.Instance._loginThead.start();
				}
			}
			break;
		case 1:
			if(status == -4999){//牛眼-停止发送视频 //云端 -关闭连接
				LogUtil.d("SDK", "value = 1 ETS onStatus:"+status);
				if(_isRun) {
					_isRun = false;
					BackLoginThread.state = 1;
					Main.Instance._loginThead.start();
				}
			}
			break;
		case 2:
			if(Main.Instance._curIndex == 1){//牛眼
				if(status == 0){
					LogUtil.d("SDK", "有人连。。lession ID ="+sessionId);
					_sessionId = sessionId;
				}else if(status == -5000){
					LogUtil.d("SDK", "关闭连接。。lession  ID ="+sessionId);
				}
			}else if(Main.Instance._curIndex == 0){//云端
				LogUtil.d("SDK", "OnStatus方法。。value ="+value+"  status="+status+"--sessionid "+sessionId);
				if(status == -5000){
					_sessionId = 0;
					LogUtil.d("SDK", "出现异常，连接关闭。。lession="+sessionId);
					if(NewSurfaceTest.isPlay && NewSurfaceTest.instance != null) NewSurfaceTest.instance.closeNewSurface(-5000);
				}
			}
			break;
		case 3:
			if(_isLogout){
				_isLogout = false;
				LogUtil.d("SDK", "同一个账号在别的设备上登陆....");
				Main.Instance._loginThead.stop();
				//判断如果正在播放视频关闭
				if(NewSurfaceTest.isPlay){
					NewSurfaceTest.instance.stop();
				}
				//关闭牛眼
				if(Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog){
					AnalogvideoActivity.instance.clearAnalog();
					AnalogvideoActivity.instance.finish();
				}
				//判断如果正在播放广场视频关闭
				if(VlcVideoActivity.instance != null && VlcVideoActivity.instance.isVlcPlaying()){
					VlcVideoActivity.instance.release();
				}
				
				Intent intent = new Intent();
				intent.setAction("com.service.EXIT_SERVICE");
				intent.putExtra("type", "1");
				intent.setPackage("com.manniu.manniu");
				BaseApplication.getInstance().startService(intent);
			}
			break;
		default:
			break;
		}
	}

	static {
		//System.loadLibrary("GwMiddleSDK");
		System.loadLibrary("P2PTransfor");
		//System.loadLibrary("zbar");
	}
	
	/*
	 * 登陆ETS
	 * jstring   ETS IP地址或者域名
	 * jint      登陆ETS端口
	 * jint      设备类型
	 * jstring   设备uuid
	 * jstring   用户名
	 * jstring   密码
	 *
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int LoginEts(String ip, int port, int type, String userid,String mnId, String password);
	public static native int LogoutEts();
	
	/*
	 * 登陆ETS
	 * jstring   ETS IP地址或者域名
	 * jint      登陆ETS端口
	 * jint      设备类型
	 * jstring   设备uuid
	 * jstring   用户名
	 * jstring   密码
	 *
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int Login(String ip, int port, int type, String userid,String mnId, String password);

	/*
	 * 登出ETS
	 *
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int Logout();
	
	/**
	 * 抓图
	 * @param jiami 是否加密  0不加密  1加密 
	 * @param data 
	 * @param length
	 * @return
	 */                      
	public static native int SendJsonPck(int jiami, String json);

	public static String getJsonString(String sid){
		return sid +"|{\"type\":1,\"action\":109,\"sid\":\""+sid+"\"}";
	}
	
	public static String getJson(String sid){
		return sid +"|{\"type\":1,\"action\":104,\"sid\":\""+sid+"\",\"channel\":0}";
	}
	
	//public static native String decode(byte[] data, int width, int height, boolean isCrop, int x, int y, int cwidth, int cheight);
	
	public static native int Init();

	public static native void UnInit();

	// ip:10.12.6.102 port:6000 id:001 (本地ID) type:设备类型 1-IPC 3-手机设备  context：目前没用传0 
	/*
	 * 登陆IDM
	 * jint      设备类型
	 * jstring   设备uuid
	 * jstring   用户名
	 * jstring   密码
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int LoginIdm(int type,String uuid,String user,String password);
	/**
	 * 返回值      0     //  成功  
	   -100  //  SDK对象为空
	 */
	public static native int LogoutIdm();

	public static native int ModifyPassword(String oldPass, String newPass);

	/**
	 * @param dstId 设备ID
	 * @param streamType 0主码流 1辅码流
	 * @param channel 通道号 1
	 * @param netType 分辩率 
	 * @return
	 */
	public static native int RealPlay(String dstId, int streamType,int channel, int netType,long sessionId);
	public static native void StopPlay(int playId);
	
	/**
	 * @param jint 设备网络类型。1 wifi, 2 Lan, 3 3G, 4 4G
	 * @param jstring  NetType=1时，为SSID， 2时为IP
	 * @param NetType=1时，为SSID信号强度
	 * @param jstring 设备纬度值
	 * @param jstring 设备经度值
	 * @return
	 */
	public static native int HeartBeat(int devNetType,String str1,int rssi,String param1,String param2);
	
	/*
	 * P2P打洞
	 * jstring   链接对象目标ID
	 * 返回：=0表示成功，<0参照错误码
	 * */
	public static native int P2PConnect(String dId,long[] sessionId);
	/*
	 * P2P转发
	 * jstring   链接对象目标ID
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int P2PTransfor(String dId,long[] sessionId);
	
	/*
	 * class:      p2p_SDK
	 * jlong       会话id
	 * jintArray: send buf data
	 * jint :     data len
	 * jint :     type  1:mix 2:video 3:voice
	 * jint :     channel
	 * jint :     bIFrame 1 为I帧 0 为P帧
	 *  返回值 >0 成功
	 * */
	//public static native int EnCodeAndSendData(long sessionId,byte[] buf, int len,int type,int channel,int bIFrame);
	/*
	 * class:      p2p_SDK
	 * jintArray: send buf data
	 * jint :     data len
	 * framerate 帧类型
	*  返回值 >0 成功
	 * */
	public static native int Ffmpegh264EnCoder(byte[] buf, int len,byte[] data,int[] bIFrame);
	/*
	 * jint :   帧率
	 * jint ：       码率
	 * jint :   宽
	 * jint ：     高
	 * */
	public static native int Ffmpegh264Init(int rate, int bitrate, int nWidth, int nHeigh);
	public static native int Ffmpegh264Uninit();
	
	/*
	 * jstring  h264文件绝对路径
	 * jstring  mp4文件绝对路径
	 * */
	public static native int Ffmpegh264ToMp4(String h246Path,String mp4Path);
	
	/*
	 * jint  截图数据格式 1 nv12 2nv21
	 * jbyteArray     数据源
	 * jstring        图片路径
	 * jint           宽
	 * jint           高
	 * */
	public static native int ScreenShotsNv21ToRgb24(byte[] buf, String fileName,int nWidth, int nHeigh);
	/*
	 * 发送抓图数据
	 * jint  通道
	 * jbyteArray 抓图数据
	 * jint   长度
	 * */
	public static native int SendSnapshotData(int chnl, byte[] data, int length);
	
	/*
	 * 输出参数 ----牛眼主动截图
	 * jbyteArray url
	 * 输入参数
	 * jint  channel
	 * jbyteArray data 图片数据
	 * jint 图片数据长度
	 * */
	public static native int uploadlocalsnapshot(byte[] outData,int channel,byte[] imgData ,int imgLength);
	/*
	 * jstring devicesdk 的配置文件路径
	 * */
	public static native int DeviceSDKSetConfig(String path);
	
	/*
	 * type  0:初始化 1:ipc 4:手机模拟
	 * */
	public static native int DataSourceDeviceType(int type);
	
	/*
	 * 创建通道
	 * jint 通道号（从0开始，IPC只有通道0）
	 * jint 0:主码流，1：副码流
	 * jint 帧率
	 * jint 码流
	 * jint 分辨率宽
	 * jint 分辨率高
	 * 返回：>0表示成功，<0参照错误码
	 * */
	public static native int P2PCreateChannel(long sessionId,int channel,int stream, int rate, int bitrate, int nWidth, int nHeigh);
	/*
	 * 关闭通道
	 * jint 通道号（从0开始，IPC只有通道0）
	 * 返回：>0表示成功，<0参照错误码
	 * */
	public static native int P2PCloseChannel(long sessionId,int channel);
	/*
	 * P2P关闭
	 * sessionId   会话ID
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int P2PClose(long sessionId);
	

	// int[] pInfo Width, Height, FrameCount, Rate

	//public static native int GetBmp32Frame(int nId, int nWidth, int nHeight,int[] pBmpBuffer, int[] pInfo);
	
	public static native int GetBmp32Frame(int playId, int nWide, int nHeight, int[] pBmpBuffer, int[] pInfo);

	public static native int GetBmp32Framebyte(int playId, int nWide, int nHeight, byte[] pOutBuffer, int[] pFInfo);
	
	public static native int GetH264(int[] buffer, int len);

	public native void SetCallback(String cb);
	
	/*	 jlong  sessionId     //链接ID
	 jint channel;		// 从0开始
	 jint stream;		// 0 主码流，1 辅码流
	 jint iFrame;		// I帧间隔
	 jint rate;			// 帧率 1~25
	 jint quality;		// 画质（1~6，取值5）
	 jint bitrate;		// 码流，单位kbps
	 jint encode;		// 编码。1.MPEG4, 2.H.264, 3. AVC, 4. H.265
	 jint nWidth;			// 分辨率
	 jint nHeigh;
	 */
	public static native int SendMediaInfo(long sessionId, int channel,int stream,int iFrame,int rate,int quality,int bitrate,int encode,int nWidth,int nHeigh);
	/*
	 * class:      p2p_SDK
	 * jintArray: send buf data
	 * jint :     data len
	 * type:类型   0：视频 1：音频 2：语音对讲
	 * jint :     channel
	 * jint :     bIFrame 1 为I帧 0 为P帧
	*  返回值 >0 成功
	 * */
	public static native int SendData(byte[] data,int lenght,int type,int channel,int bIFrame);
	
	public static native byte[] StartupLoadLocalMedia();
	/*
	 * jbyteArray   数据
	 * len          数据长度
	 * jboolean     标志是否为最后一帧数据  是最后一个数据包 true 否则为false
	 * ret 0 成功
	 * */
	public static native int upLoadLocalMedia(byte[] data, int length, boolean flag);
	
	/*
	 * jstring 解密前的数据
	 * jint    解密前的数据长度
	 * jstring 密钥
	 * jint    密钥长度
	 * jstring 解密后的数据
	 * jint    解密后的数据长度
	 * */
	//public static native int NgxDecodeTea(String data, int length, String str, int length, String str2, int length);

	/*
	 * jstring 加密前的数据
	 * jint    加密前的数据长度
	 * jstring 密钥
	 * jint    密钥长度
	 * jstring 加密后的数据
	 * jint    加密后的数据长度
	 * */

	//public static native int  NgxEncodeTea(JNIEnv *, jclass,jstring, jint, jstring, jint, jstring, jint);
	
	/*
	 * jint  1 是软解  我们自己的软解 暂时不用
	 * */
	public static native int SetDecoderModel(int type);
	
	//之前的错误码 全部作废  阮少说的
    
    //2015-7-20 最新版
    public static final int GW_ERRCODE_SUCESS = 0;
    //自定论错误码 从-100开始
    public static final int Error = -1;
    public static final int Err_refresh = -100;
    public static final int Err_SER_FAIL = -101;
    
    public static final int GW_SESSION_UNLINK = -2000;
    public static final int GW_ERRCODE_UNLOGIN = -1999;
    
    public static final int GW_ERRCODE_LOGIN_FAILED = -1000;
    public static final int GW_ERRCODE_LOGIN_TIMEOUT = -999;
    public static final int GW_ERRCODE_LOGIN_IDM_TIMEOUT = -998;
    public static final int GW_ERRCODE_IDM_INIT_FAILD = -997;
    public static final int GW_ERRCODE_IDM_REGISTER_CONFLICT = -996;
    
    public static final int GW_ERRCODE_P2P_FAILD = -900;
    public static final int GW_ERRCODE_P2P_UNSUPPORT_TYPE = -899;
    public static final int GW_ERRCODE_P2P_INIT_FAILD = -898;
    public static final int GW_ERRCODE_P2P_DISCONNECT = -897;
    public static final int GW_ERRCODE_P2P_DEST_DISCONNECT = -896;
    public static final int GW_ERRCODE_P2P_LINK_FAILD = -895;
    public static final int GW_ERRCODE_P2P_TIMEOUT = -894;

    public static final int GW_ERRCODE_TRANS_FAILD = -893;
    public static final int GW_ERRCODE_TRANS_UNSUPPORT_TYPE = -892;
    public static final int GW_ERRCODE_TRANS_INIT_FAILD = -891;
    public static final int GW_ERRCODE_TRANS_DISCONNECT = -890;
    public static final int GW_ERRCODE_TRANS_LINK_FAILD = -889;
    public static final int GW_ERRCODE_TRANS_TIMEOUT = -888;

    public static final int GW_ERRCODE_P2P_SEND_MSG_FAILD = -887;
    public static final int GW_ERRCODE_P2P_SEND_MSG_DISCONNECT = -886;
    
    public static final int GW_ERRCODE_P2P_SEND_DATA_FAILD = -885;
    public static final int GW_ERRCODE_P2P_SEND_DATA_DISCONNECT = -884;
    public static final int GW_ERRCODE_P2P_SEND_DATA_UNSUPORT_TYPE = -883;
    
    public static final int GW_ERRCODE_ALARM_FAILD = -882;
    public static final int GW_ERRCODE_ALARM_WAIT = -881;
    public static final int GW_ERRCODE_ALARM_UPLOAD_FAILD = -880;
    public static final int GW_ERRCODE_ALARM_TIMEOUT = -879;
    
    public static final int GW_ERRCODE_MEDIA_UPLOAD_FAILD = -878;
    
    public static final int GW_ERRCODE_SNAPSHOT_FAILD = -877;
    public static final int GW_ERRCODE_SNAPSHOT_UPLOAD_FAILD = -876;
    
	// gw_heartbeat错误码
    public static final int GW_ERRCODE_HEARTBEAT_FAILD = -875;					// 心跳失败（pdu创建失败等，只在测试阶段会出现）
    public static final int GW_ERRCODE_HEARTBEAT_SEND_FAILD = -874;			// 心跳发送失败

	// gw_send_json_pkt错误码
    public static final int GW_ERRCODE_SEND_JSON_PKT_FAID = -873;				// 发送json数据包失败

	// gw_start_upgrade_download错误码
    public static final int GW_ERRCODE_START_UPGRADE_FAILD = -872;				// 启动升级下载失败
    public static final int GW_ERRCODE_START_UPGRADE_WAIT = -871;				// 正在升级中
	
	//30是预登录失败
	public static String GetErrorStr(int error){
		int strId = 0;
		switch(error){
		case Error:
			strId = R.string.Err_Error_Unknow; break;
		case GW_ERRCODE_SUCESS:
			strId = R.string.GW_ERRCODE_SUCESS; break;
		case GW_SESSION_UNLINK:
			strId = R.string.GW_SESSION_UNLINK; break;
		case GW_ERRCODE_UNLOGIN:
			strId = R.string.GW_ERRCODE_UNLOGIN; break;
		case GW_ERRCODE_LOGIN_FAILED:
			strId = R.string.GW_ERRCODE_LOGIN_FAILED; break;
		case GW_ERRCODE_LOGIN_TIMEOUT:
			strId = R.string.GW_ERRCODE_LOGIN_TIMEOUT; break;
		case GW_ERRCODE_LOGIN_IDM_TIMEOUT:
			strId = R.string.GW_ERRCODE_LOGIN_IDM_TIMEOUT; break;
		case GW_ERRCODE_IDM_INIT_FAILD:
			strId = R.string.GW_ERRCODE_IDM_INIT_FAILD; break;
		case GW_ERRCODE_IDM_REGISTER_CONFLICT:
			strId = R.string.GW_ERRCODE_IDM_REGISTER_CONFLICT; break;
		case GW_ERRCODE_P2P_FAILD:
			strId = R.string.GW_ERRCODE_P2P_FAILD; break;
		case GW_ERRCODE_P2P_UNSUPPORT_TYPE:
			 strId = R.string.GW_ERRCODE_P2P_UNSUPPORT_TYPE; break;
		case  GW_ERRCODE_P2P_INIT_FAILD: 
			strId = R.string.GW_ERRCODE_P2P_INIT_FAILD; break;
		case GW_ERRCODE_P2P_DISCONNECT:
			 strId = R.string.GW_ERRCODE_P2P_DISCONNECT; break;
		case GW_ERRCODE_P2P_DEST_DISCONNECT:
			 strId = R.string.GW_ERRCODE_P2P_DEST_DISCONNECT; break;
		case GW_ERRCODE_P2P_LINK_FAILD:
			 strId = R.string.GW_ERRCODE_P2P_LINK_FAILD; break;
		case GW_ERRCODE_P2P_TIMEOUT:
			 strId = R.string.GW_ERRCODE_P2P_TIMEOUT; break;
		case GW_ERRCODE_TRANS_FAILD: 
			 strId = R.string.GW_ERRCODE_TRANS_FAILD; break;
		case GW_ERRCODE_TRANS_UNSUPPORT_TYPE:
			 strId = R.string.GW_ERRCODE_TRANS_UNSUPPORT_TYPE; break;
		case GW_ERRCODE_TRANS_INIT_FAILD:
			 strId = R.string.GW_ERRCODE_TRANS_INIT_FAILD; break;
		case GW_ERRCODE_TRANS_DISCONNECT:
			 strId = R.string.GW_ERRCODE_TRANS_DISCONNECT; break;
		case GW_ERRCODE_TRANS_LINK_FAILD:
			 strId = R.string.GW_ERRCODE_TRANS_LINK_FAILD; break;
		case GW_ERRCODE_TRANS_TIMEOUT:
			 strId = R.string.GW_ERRCODE_TRANS_TIMEOUT; break;
		case GW_ERRCODE_P2P_SEND_MSG_FAILD:
			 strId = R.string.GW_ERRCODE_P2P_SEND_MSG_FAILD; break;
		case GW_ERRCODE_P2P_SEND_MSG_DISCONNECT:
			 strId = R.string.GW_ERRCODE_P2P_SEND_MSG_DISCONNECT; break;
		case GW_ERRCODE_P2P_SEND_DATA_FAILD:
			 strId = R.string.GW_ERRCODE_P2P_SEND_DATA_FAILD; break;
		case GW_ERRCODE_P2P_SEND_DATA_DISCONNECT:
			 strId = R.string.GW_ERRCODE_P2P_SEND_DATA_DISCONNECT; break;
		case GW_ERRCODE_ALARM_FAILD:
			 strId = R.string.GW_ERRCODE_ALARM_FAILD; break;
		case GW_ERRCODE_ALARM_WAIT:
			 strId = R.string.GW_ERRCODE_ALARM_WAIT; break;
		case GW_ERRCODE_ALARM_UPLOAD_FAILD:
			 strId = R.string.GW_ERRCODE_ALARM_UPLOAD_FAILD; break;
		case GW_ERRCODE_ALARM_TIMEOUT:
			 strId = R.string.GW_ERRCODE_ALARM_TIMEOUT; break;
		case GW_ERRCODE_MEDIA_UPLOAD_FAILD:
			 strId = R.string.GW_ERRCODE_MEDIA_UPLOAD_FAILD; break;
		case GW_ERRCODE_SNAPSHOT_FAILD:
			 strId = R.string.GW_ERRCODE_SNAPSHOT_FAILD; break;
		case GW_ERRCODE_SNAPSHOT_UPLOAD_FAILD:
			 strId = R.string.GW_ERRCODE_SNAPSHOT_UPLOAD_FAILD; break;
			 
		case GW_ERRCODE_HEARTBEAT_FAILD:
			 strId = R.string.GW_ERRCODE_HEARTBEAT_FAILD; break;
		case GW_ERRCODE_HEARTBEAT_SEND_FAILD:
			 strId = R.string.GW_ERRCODE_HEARTBEAT_SEND_FAILD; break;
		case GW_ERRCODE_SEND_JSON_PKT_FAID:
			 strId = R.string.GW_ERRCODE_SEND_JSON_PKT_FAID; break;
		case GW_ERRCODE_START_UPGRADE_FAILD:
			 strId = R.string.GW_ERRCODE_START_UPGRADE_FAILD; break;
		case GW_ERRCODE_START_UPGRADE_WAIT:
			 strId = R.string.GW_ERRCODE_START_UPGRADE_WAIT; break;
			 
		case Err_refresh:
			 strId = R.string.fail_refresh; break;
		case Err_SER_FAIL:
			 strId = R.string.E_SER_FAIL; break;
			 
			 
		}
		if(strId != 0){
			return APP.GetString(strId);
		}
		return APP.GetString(R.string.Err_Error) + " code = " + error;
	}
	
}