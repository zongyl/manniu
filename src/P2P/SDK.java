package P2P;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.bean.Monitor;
import com.manniu.manniu.R;
import com.utils.DevSetHandler;
import com.utils.ExceptionsOperator;
import com.utils.FileUtil;
import com.utils.LogUtil;
import com.utils.ScreenHandler;
import com.views.BaseApplication;
import com.views.Fun_RealPlayerActivity;
import com.views.Fun_RecordPlay;
import com.views.Main;
import com.views.NewSurfaceTest;
import com.views.analog.camera.audio.AudioQueue;
import com.views.analog.camera.encode.DecoderDebugger;

public class SDK {

	private static String[] strArr;
	//截图 handler
	private static ScreenHandler screenHandler = new ScreenHandler();
	//设备设置 handler
	private static DevSetHandler devSetHandler = new DevSetHandler();
	//录像回放 handler
	//private static VideoBackHandler videoBackHandler = new VideoBackHandler();
	// 0 是音频 1 是视频 后面三个字节填
//	src:源数组；	srcPos:源数组要复制的起始位置；
//	dest:目的数组；	destPos:目的数组放置的起始位置；	length:复制的长度。
	//chnl:0
	//type:类型   0：视频 1：音频 2：语音对讲
	//isIFrame		1：i帧，否则为p帧
	//pcmType 音频类型
	//static byte[] newbuf = new byte[60*1024];
	public void onData(int width,int heigth,int pcmType,int chnl,int devType,int type,int isIFrame,byte[] data, int length,long nSessionID, long lDeviceChannelID,int encoderType) {
		/*if(data.length > 0 && NewMain.instance != null && NewMain.instance.viewPager.getCurrentItem() == 1){//报警视频
			if(_width != width || _height != heigth){
				_width = width;
				_height = heigth;
			}
		}else if(data != null && data.length > 0 && NewSurfaceTest.instance != null){//实时视频
			//硬、软解方法
			if(type == 0){//视频
				if(_width != width || _height != heigth){
					_manufactorType = devType;
					_width = width;
					_height = heigth;
				}
				if(NewSurfaceTest.instance.isStop && NewSurfaceTest.isGpu)
					NewSurfaceTest.instance.h264Decoder(data, length,isIFrame);
			}else if(type == 1 && AudioQueue.runFlag){//音频
				AudioQueue.addSound(data,pcmType);
			}
		}*/
		if(data.length > 0){
			if(NewSurfaceTest.instance != null && NewSurfaceTest.isPlay){//实时视频
				//硬、软解方法
				if(type == 0){//视频
					if(_width != width || _height != heigth){
						_manufactorType = devType;
						_width = width;
						_height = heigth;
					}
					if(NewSurfaceTest.instance.isStop && NewSurfaceTest.isGpu)
						NewSurfaceTest.instance.h264Decoder(data, length,isIFrame);
				}else if(type == 1 && AudioQueue.runFlag){//音频
					AudioQueue.addSound(data,pcmType);
				}
				return;
			}
			//多画面--- 0:视频
			if(type == 0){
				String strKey = GetChannelKeyByID(lDeviceChannelID);
				String[] strDevChlInfo = ParseDeviceChannelKey(strKey);
				String strUUID="";
				int nChannelIndex= 0;
				if(strDevChlInfo.length>=2){
					strUUID= strDevChlInfo[0];
					nChannelIndex= FileUtil.ConvertStringToInt(strDevChlInfo[1], 0);
				}
				boolean blnFirstIFrame = false;
				
				Monitor monitor = GetChannelMonitor(lDeviceChannelID);
				if (monitor == null) {		
					//System.out.println("2016.04.05TEST onData  monitor == null");
					return;
				}
				
				// 硬解尝试等到IFrame
				if (GetChannelIFrameStatus(lDeviceChannelID) == false) {
					if (isIFrame == 1) {
						if (GetChannelHaveDataStatus(lDeviceChannelID) == false) {
							SetChannelHaveDataStatus(lDeviceChannelID, true);
							Bundle bdData = new Bundle();				
							
							bdData.putString("DeviceUUID", strUUID);
							bdData.putInt("ChannelIndex", nChannelIndex);				
							bdData.putInt("Status", 1);
							SendMessage(_ConnectChannelP2PHandler,PLAYING_EACHVIEW_CONTROLS, bdData);				
						}
						SetChannelIFrameStatus(lDeviceChannelID, true);
						System.out.println("2016.04.05TEST onData  SetChannelIFrameStatus");
						blnFirstIFrame = true;
					} else {
						System.out.println("2016.04.05TEST onData  iS IFreame:False");
						return;
					}
				}
				
				DecoderDebugger decoder= GetRealPlayDecoder(lDeviceChannelID);
				boolean blnHaveReSetDecoder= false;
				if(decoder==null){
					blnHaveReSetDecoder=true;
				}else{
					if(GetChannelDecoderType(lDeviceChannelID)==false && !decoder.isCanDecode()){
						//走软解处理
						SetChannelDecodeType(lDeviceChannelID,true);
						SetDecoderModel(1,GetChannelPlayContext(lDeviceChannelID));
						return;
					}	
				}
				
				if (blnHaveReSetDecoder) {
					if (_RealPlaySurface.containsKey("" + lDeviceChannelID)) {
						_InformationLock.lock();
						if (monitor != null && monitor.getAVDecoder() != null) {
							monitor.getAVDecoder().release();
							monitor.setAVDecoder(null);
						}
						monitor.setAVDecoder(new DecoderDebugger((Surface)_RealPlaySurface.get(""+lDeviceChannelID),176,144));
						_InformationLock.unlock();
						SetAVDecoder(lDeviceChannelID, monitor.getAVDecoder());
						System.out.println("2016.04.05TEST onData  ReceiveData   ReSetDecoder   --------------");
					} else {
						ThreadSleep(1);
						return;
					}
				}
				
				if(decoder.isCanDecode()){
					if(monitor != null){
						// 计算流量
						monitor.setCurChnDataFlow(length);
						if (monitor.getPlay_status() == 1){//视频
							if(monitor.getCodecwidth() != width || monitor.getCodecheight() != heigth){
								monitor.setCodecwidth(width);
								monitor.setCodecheight(heigth);
							}
							//多画面时 如果放大了 别的窗口不用调画图片方法
							if(Fun_RealPlayerActivity.instance.m_isFullView && Fun_RealPlayerActivity.instance.m_monitorsList.size() > 0){
								if(monitor != Fun_RealPlayerActivity.instance.m_chooseMonitor){
									//System.out.println("........................................................................."+Fun_RealPlayerActivity.instance.m_isFullView+"::"+monitor.getDevCart().getDeviceInfo().sid);
									return;
								}
							}
							Lock lock = monitor.getDecoder_lock();
							lock.lock();
							int realLen = length;
							if(devType == 0){		//厂家设备类型为智诺的，需要去头处理
								if(length>22){//去头处理
									int exHead = (int)data[22];
									int realHead = 24 + exHead;
									realLen = length - realHead - 8;
									byte[] newbuf = new byte[realLen];
									System.arraycopy(data, realHead, newbuf, 0, realLen);
									try{
										if(blnFirstIFrame == true){
											decoder.decoder(newbuf,realLen);
											decoder.decoder(newbuf,realLen);
											decoder.decoder(newbuf,realLen);
											decoder.decoder(newbuf,realLen);
											decoder.decoder(newbuf,realLen);
											decoder.decoder(newbuf,realLen);
										}
										decoder.decoder(newbuf,realLen);
										//截图
					        			if(isIFrame == 1 && Fun_RealPlayerActivity.instance.isShot && SDK._shotContext == SDK.GetChannelPlayContext(lDeviceChannelID)){
					        				Fun_RealPlayerActivity.instance.h264DecoderSnapImg(newbuf,monitor.getImageData(),realLen,monitor.getCodecwidth(),monitor.getCodecheight(),SDK._shotContext);
					                	}
									}catch (Exception e) {
									}
								}
							}else{//模拟、海康设备不用去头
								try{
									// 计算流量
									monitor.setCurChnDataFlow(length);
									if(blnFirstIFrame == true){
										decoder.decoder(data,length);
										decoder.decoder(data,length);
										decoder.decoder(data,length);
										decoder.decoder(data,length);
										decoder.decoder(data,length);
									}
									decoder.decoder(data,length);
									//截图
				        			if(isIFrame == 1 && Fun_RealPlayerActivity.instance.isShot && SDK._shotContext == SDK.GetChannelPlayContext(lDeviceChannelID)){
				        				Fun_RealPlayerActivity.instance.h264DecoderSnapImg(data,monitor.getImageData(),length,monitor.getCodecwidth(),monitor.getCodecheight(),SDK._shotContext);
				                	}
								}catch (Exception e) {
									LogUtil.e("SDK", ExceptionsOperator.getExceptionInfo(e));
								}
							}
							lock.unlock();
						}
					}
					
				}
				
			}else if(type == 1){//音频
				System.out.println(111111);
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
	public static boolean isInitDecoder = false;//收到宽高标识位判断通道是否创建成功/以这个来判断要不要送数据到解码器
	public static int _createChnlFlag = -1;//_createChnlFlag 标志位 0成功
	public static boolean analogPic = false;//模拟抓图
	public static int nFrameRate = 20;//帧率
	public static int _bitrate = 80000000;//码率
	public static int _width = 352;//ondata返回的宽高
	public static int _height = 288;//
	public static int _manufactorType = 0;//厂家类型：0-智诺 1-海康
	public static int _flag = 0;//收到宽高只处理一次
	public void OnCommand(int cmd, long param1, int param2, int param3,
			int param4,int param5 ,int param6,int param7, String str1, String str2, String str3) {
		LogUtil.d("SDK", "OnCommand:"+cmd + " -- " + param1 + "-- " + param2 + "--" + param3+ "--" + param4 + "--" + str1 + "--" + str2 + "--" + str3);
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
				printJson(jsonData);
				if("0".equals(jsonData.getString("result"))){
					Message msg = new Message();
					if(jsonData.containsKey("cam_conf")){//用此属性判断 是获取的时候返回的 还是设置的时候返回的
						msg.what = 1;
						Bundle data = new Bundle();
						data.putString("deviceId", strArr[0]);
						data.putString("setting", jsonData.toJSONString());
						msg.setData(data);
					}else{
						msg.what = 2;
					}
					devSetHandler.sendMessage(msg);
				}else if("2".equals(jsonData.getString("result"))){
					LogUtil.d("SDK", "/设备不在线");
					Message msg = new Message();
					msg.what = 3;
					devSetHandler.sendMessage(msg);
				}
			}else if(jsonData.containsKey("action")&&"106".equals(jsonData.getString("action"))){
				if("7".equals(jsonData.getString("result"))||"0".equals(jsonData.getString("result"))){
					if(jsonData.containsKey("file_list")){
						Message msg = new Message();
						msg.what = 1;
						Bundle data = new Bundle();
						data.putString("file_list", jsonData.getJSONArray("file_list").toJSONString());
						msg.setData(data);
					//	VideoPlayback.mhandler.sendMessage(msg);
						BaseApplication.getInstance().getVideoHandler().sendMessage(msg);
						
					}
				}
			}
		}else if(cmd == 903){ //ETS会自动下线原有链接
			
		}else if(cmd == 100){//关闭报警回放
			//if(Fun_RecordPlay.instance != null) Fun_RecordPlay.instance.stop();
			devSetHandler.sendEmptyMessage(4);
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
//				int ret = SendMediaInfo(param1, 0,1,0,10,5,(int)(VideoStream.mRequestedQuality.bitrate*0.8),2,VideoStream.mRequestedQuality.resX,VideoStream.mRequestedQuality.resY);
//				if(ret == 0 && AnalogvideoActivity.instance != null){
					_createChnlFlag = 0;
					AnalogvideoActivity.instance.startEncode();
					AnalogvideoActivity.instance.changeTextValue();
//				}
			}else if(cmd == 11){//关闭通通-停止编码发送数据
				_createChnlFlag = -1;
				_sessionIdContext = 0;
				AnalogvideoActivity.instance.stopEncode();
				AnalogvideoActivity.instance.changeTextValue();
			}
		}else if(NewSurfaceTest.instance != null && NewSurfaceTest.isPlay){//云端
			if(cmd == 10){//获取宽高  第3  4  5 个参数分别是帧率  宽 高
//				LogUtil.d("SDK","收到宽高：    "+param2+"--"+param3 + "--" + param4+ "--" + param5+"--"+param6+"--"+param7);
//				if(param3 != 0) nFrameRate = param3;
//				_width = param5;
//				_height = param6;
				//NewSurfaceTest.instance._decoderDebugger.configureDecoder(param3,param4,param5,param6);
				//isInitDecoder = true;
//				if(NewSurfaceTest.isPlay && _flag == 0){
//					_flag = 1;
//					BackLoginThread.state = 4;
//					Main.Instance._loginThead.start();
//				}
			}else if(cmd == 26){//设备状态 == 0.未知状态，初始化为该状态 1. 设备忙 2.设备空闲
				isInitDecoder = true;
				if(param2 != 2 && NewSurfaceTest.instance != null) NewSurfaceTest.instance.closeNewSurface(param2);
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
	public static boolean _isRun = true; //是否发送登录IDM或ETS 保证只执行一次
	public static long _sessionIdContext = 0;//这里主要用于设备里面播放视频。 多画面的时候不用这个参数  跟4画面的context 是一个意思。都是打洞成功后通过onstatus 里面返回的
	public static boolean _isLogout = true;
	public void OnStatus(int value, long sessionId, int status, String dst_uuid,long context,long deviceChannelID){
		switch (value) {
		case 0:
			if(status == -4999){//牛眼-停止发送视频  云端 -关闭连接(IDM掉线 移到后台管理)
				LogUtil.d("SDK", "value = 0 IDM  onStatus:"+status);
				if(_isRun) {
					_isRun = false;
					BackLoginThread.state = 1;
					if((Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog) || (Main.Instance._curIndex == Main.XV_NEW_MAIN && NewSurfaceTest._playId > 0))
						Main.Instance._loginThead.start();
				}
			}
			break;
		case 1:
			if(status == -4999){//牛眼-停止发送视频 //云端 -关闭连接
				LogUtil.d("SDK", "value = 1 ETS onStatus:"+status);
				//1........要先关闭 p2pclose()..P2PCloseChannel
				if(_isRun) {
					_isRun = false;
					BackLoginThread.state = 1;
					if((Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog) || (Main.Instance._curIndex == Main.XV_NEW_MAIN && NewSurfaceTest._playId > 0))
						Main.Instance._loginThead.start();
				}
			}
			break;
		case 2://打洞返回
			if(Main.Instance._curIndex == 1){//牛眼
				if(status == 0){
					LogUtil.d("SDK", "有人连。。lession ID ="+sessionId);
					_sessionIdContext = sessionId;
				}else if(status == -5000){
					LogUtil.d("SDK", "关闭连接。。lession  ID ="+sessionId);
				}
			}else if(Main.Instance._curIndex == 0){//云端
				LogUtil.d("SDK", "OnStatus方法。。value ="+value+"  status="+status+"--sessionid "+sessionId);
				if(status == -5000){
					_sessionIdContext = 0;
					LogUtil.d("SDK", "出现异常，连接关闭。。lession="+sessionId);
					//2..........要先关闭 p2pclose()..P2PCloseChannel
					if(NewSurfaceTest.isPlay && NewSurfaceTest.instance != null) NewSurfaceTest.instance.closeNewSurface(-5000);
				}
			}
			//contxt返回  --- 新增多画面 start
			if(NewSurfaceTest.instance != null){
				Bundle bdData = new Bundle();
				bdData.putLong("Context", context);
				bdData.putInt("Status",status);	
				SendMessage(_ConnectChannelP2PHandler, bdData);
			}else{
				String strKey = GetChannelKeyByID(deviceChannelID);
				if(strKey.trim().length() > 0){
					if(status == 0 ){
						_PlayChannelContextLock.lock();
						_ChannelPlayContext.put(""+deviceChannelID, context);//添加ChannelPlayContext对应关系
						_PlayChannelContextLock.unlock();
					}
					Bundle bdData = new Bundle();
					String[] strDevChannelInfo = ParseDeviceChannelKey(strKey);
					if(strDevChannelInfo!=null && strDevChannelInfo.length>=2){
						bdData.putString("DeviceUUID", strDevChannelInfo[0]);
						bdData.putInt("ChannelIndex", FileUtil.ConvertStringToInt(strDevChannelInfo[1],-1));
						bdData.putLong("Context", context);
						bdData.putInt("Status",status);					
						SendMessage(_ConnectChannelP2PHandler, bdData);
						//System.out.println("20160309P2P     OnStatus strDevChannelInfo[0]:"+strDevChannelInfo[0] + ";strDevChannelInfo[1]:"+strDevChannelInfo[1]+";Context:"+context+";status:"+status);
					}
				}
			}
			//contxt返回  --- 新增多画面 end
			break;
		case 3:
			if(_isLogout){
				try {
					_isLogout = false;
					LogUtil.d("SDK", "同一个账号在别的设备上登陆....");
					if(Main.Instance!= null && Main.Instance._loginThead != null){
						Main.Instance.stopUpdateCheck();
						Main.Instance._loginThead.stop();//停止IDM线程
					}
					//判断如果正在播放视频关闭
					if(NewSurfaceTest.isPlay){
						NewSurfaceTest.instance.stop();
					}
					//关闭多画面
					if(Fun_RealPlayerActivity.instance != null){
						Fun_RealPlayerActivity.instance.exitDeleteDevice();
					}
					//关闭牛眼
					if(Main.Instance._curIndex == Main.XV_NEW_MSG && AnalogvideoActivity.isOpenAnalog){
						AnalogvideoActivity.instance.clearAnalog();
						AnalogvideoActivity.instance.finish();
					}
					//关闭报警回放
					if(Fun_RecordPlay.instance != null/* && Fun_RecordPlay.instance.is != null*/){
						Fun_RecordPlay.instance.stop();
					}
					//判断如果正在播放广场视频关闭
//					if(VlcVideoActivity.instance != null && VlcVideoActivity.instance.isVlcPlaying()){
//						VlcVideoActivity.instance.release();
//					}
					
					Intent intent = new Intent();
					intent.setAction("com.service.EXIT_SERVICE");
					intent.putExtra("type", "1");
					intent.setPackage("com.manniu.manniu");
					BaseApplication.getInstance().startService(intent);
				} catch (Exception e) {
					LogUtil.e("SDK", ExceptionsOperator.getExceptionInfo(e));
				}
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
	
	//.............................多画面处理
	
	/**
	 * 打开通道视频
	 * @param dId:设备UUID
	 * @param nOpenChannelIndex:通道索引
	 * @param avdecoder:解码器
	 * @param monitor:展示容器
	 * @return
	 */
	private static int _MaxChannelID = 0;
	private static Lock _Lock = new ReentrantLock();
	private static Lock _PlayChannelContextLock = new ReentrantLock();
	private static Handler _ConnectChannelP2PHandler = null;
	private static Map<String,String> _ChannelKeyID = new HashMap<String,String>();//_ChannelID对应关系 Key:设备UUID+通道索引;Object:对应的通道ID(long型)
	private static Map<String,String> _ChannelIDKey = new HashMap<String,String>();//ChannelState对应关系 Key:对应的通道ID(long型);Object:设备UUID+通道索引
	private static Map<String,Object> _ChannelPlayContext = new HashMap<String,Object>();//_ChannelContext对应关系 Key:对应的通道ID(long型);Object:对应的Context
	private static Map<String,Object> _RealPlayDecoder=new HashMap<String,Object>();//AVDecoder对应关系  Key:设备UUID+通道索引;Object:对应的AVDecoder 
	private static Map<String,Object> _RealPlaySurface=new HashMap<String,Object>();//Surface对应关系  Key:设备UUID+通道索引;Object:对应的Surface
	private static Map<String,Object> _RealPlayMonitor=new HashMap<String,Object>();//Monitor对应关系  Key:设备UUID+通道索引;Object:对应的Monitor
	private static Map<String,Object> _CreateChannelState = new HashMap<String,Object>();//_ChannelContext对应关系 Key:对应的通道ID(long型);Object:对应的CreateChannel的状态
	private static Map<String,Object> _ChannelDecodeType = new HashMap<String,Object>();//_ChannelContext对应关系 Key:对应的通道ID(long型);Object:对应的解码类型 false:硬解;true:软解
	private static Map<String,Object> _ChannelIFrameStatus = new HashMap<String,Object>();//_ChannelContext对应关系 Key:对应的通道ID(long型);Object:对应的解码类型 false:硬解;true:软解
	private static Map<String, Object> _ChannelHaveDataStatus = new HashMap<String, Object>();// _ChannelContext对应关系// Key:对应的通道ID(long型);Object:对应的解码类型 // false:硬解;true:软解
	private static int PLAYING_EACHVIEW_CONTROLS = 13;
	public static long _shotContext = -1;//截图context
	public static long ConnectChannelP2P(String dId, int nOpenChannelIndex,
			DecoderDebugger avdecoder, Monitor monitor, Handler hdlConnectStateHandler) {
		long nRet = 0;
		_Lock.lock();
		long lChannelID = GetChannelIDByKey(dId, nOpenChannelIndex);
		//System.out.println("20160309P2P     ConnectChannelP2PlChannelID:"+ lChannelID+" sid = "+dId);
		if (GetChannelPlayContext(lChannelID) == 0){// 如果当前设备通道正在播放，则不重新打通p2p
			//System.out.println("20160309P2P     ConnectChannelP2P  【Start: " + lChannelID + "】");
			nRet = P2PConnect(dId,lChannelID);
			//System.out.println("20160309P2P     ConnectChannelP2P  【End: " + lChannelID + "】");
			if (nRet == 0) {
				_ConnectChannelP2PHandler = hdlConnectStateHandler;
				// 支持多窗口处理
				SetAVDecoder(lChannelID, avdecoder);
				SetMonitor(lChannelID, monitor);
			}
		} else {
			nRet = 0;
		}
		_Lock.unlock();
		return nRet;
	}
	
	public static void SetMonitor(long lChannelID,Monitor monitor){
		_RealPlayMonitor.put(""+lChannelID, monitor);
		monitor.setStatus(0);
		monitor.setLogin_state(1);
		if(monitor != null){
			monitor.setPlay_status(1);;
			monitor.setCanPTZ(false);
			monitor.setCanTalk(false);
			monitor.setCanSwitchStream(false);
		}
	}
	
	public static void SetAVDecoder(long lChannelID,DecoderDebugger avdecoder){
		String strKey = GetChannelKeyByID(lChannelID);
		String[] strArr = ParseDeviceChannelKey(strKey);				
		String strDevUUID = strArr[0];
		int nChannelIndex = FileUtil.ConvertStringToInt(strArr[1], 0)  ;
		SetAVDecoder(strDevUUID,nChannelIndex,avdecoder);
	}
	/**
	 * 设置设备通道对应的AVDecoder
	 * @param dId
	 * @param nOpenChannelIndex
	 * @param avdecoder
	 */
	public static void SetAVDecoder(String dId,int nOpenChannelIndex,DecoderDebugger avdecoder){
		if(avdecoder != null){
			avdecoder.setCanDecode(true);			
			long lChannelID = GetChannelIDByKey(dId,nOpenChannelIndex);
			_RealPlayDecoder.put(""+lChannelID, avdecoder);
			_RealPlaySurface.put(""+lChannelID, avdecoder.GetSurface());
		}
	}
	
	public static String GetChannelKeyByID(long lID) {
		String strRet = "";
		if (_ChannelIDKey.containsKey("" + lID)) {
			strRet = _ChannelIDKey.get("" + lID);
		}
		return strRet;
	}
	
	private static String[] ParseDeviceChannelKey(String strKey){
		String[] strArr= strKey.split("_");
		return strArr;
	}
	
	public static long GetChannelPlayContext(long lChannelID) {
		long lRet = 0;
		_PlayChannelContextLock.lock();
		if (_ChannelPlayContext.containsKey("" + lChannelID)) {
			lRet = FileUtil.ConvertStringToLong(_ChannelPlayContext.get("" + lChannelID).toString(), 0);
		}
		_PlayChannelContextLock.unlock();
		return lRet;
	}
	
	public static long GetChannelPlayContext(String strUUID,int nChannelIndex){
		return GetChannelPlayContext(GetChannelIDByKey(strUUID, nChannelIndex));
	}
	
	public static long GetChannelIDByKey(String strUUID,int nChannelIndex){
		return   GetChannelIDByKey(CreateDeviceChannelKey(strUUID,nChannelIndex));
	}

	public static long GetChannelIDByKey(String strKey) {
		long lRet = -1;
		if (_ChannelKeyID.containsKey(strKey)) {
			lRet = FileUtil.ConvertStringToLong(_ChannelKeyID.get(strKey),-1);
		} else {
			_MaxChannelID += 1;
			// Channel Key和ID之间建立对应关系，删除时需要一并删除
			_ChannelKeyID.put(strKey, "" + _MaxChannelID);
			_ChannelIDKey.put("" + _MaxChannelID, strKey);
			lRet = _MaxChannelID;
		}
		return lRet;
	}
	/**
	 * 生成设备通道对应的Key
	 * @param strUUID
	 * @param nChannelIndex
	 * @return
	 */
	private static String CreateDeviceChannelKey(String strUUID,int nChannelIndex){
		return strUUID+"_"+nChannelIndex;
	}
	//创建通道
	public static long CreateChannelP2P(String dId,int nOpenChannelIndex,long lContext/*,AVDecoder avdecoder,Monitor monitor*/){
		//System.out.println("TEST20160321     CreateChannelP2P:"+dId+"   " + nOpenChannelIndex);
		long lRet = 0;
		boolean blnCreateChannel = false;
		int nCCTryCount = 0;
		long lChannelID= GetChannelIDByContext(lContext);
		blnCreateChannel = ChannelHaveCreate(lChannelID);
		//System.out.println("TEST20160321     CreateChannelP2P HaveCreateChannel="+(blnCreateChannel==true?"是":"否")+"___"+lChannelID+";lContext:"+lContext);
		if(lChannelID != -1){
			//System.out.println("TEST20160321     CreateChannel______________Context:"+lContext);
			int nplayId = P2PCreateChannel(lContext,nOpenChannelIndex,1,20,10000, 352,288);
			try {
				Thread.sleep(10, 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//System.out.println("TEST20160321     CreateChannelXXXXXXXXXXXXXXXXX  P2PCreadChannel Ret:" + nplayId);
			if(nplayId >= 0){	
				blnCreateChannel = true;				
			}else{
				lRet = -1;
			}
			_CreateChannelState.put(""+lChannelID, blnCreateChannel);
			nCCTryCount++;
		}	
		return lRet;
			
	}
	
	public static long GetChannelIDByContext(long lContext) {
		long lRet = -1;
		_PlayChannelContextLock.lock();
		//System.out.println("20160309    GetChannelIDByContext:" + lContext);
		if (_ChannelPlayContext.containsValue(lContext)) {
			//System.out.println("20160309    GetChannelIDByContext______________");
			for (Map.Entry<String, Object> entry : _ChannelPlayContext.entrySet()) {
				String strValue = entry.getValue().toString();
				String strKey = entry.getKey();
				if (lContext == FileUtil.ConvertStringToLong(strValue, -1)) {
					lRet = FileUtil.ConvertStringToLong(strKey, -1);
					//System.out.println("20160309    GetChannelIDByContextXXXXXXXXXXXX");
					break;
				}
			}
		}
		_PlayChannelContextLock.unlock();
		return lRet;
	}
	
	public static boolean ChannelHaveCreate(long lChannelID) {
		boolean blnRet = false;
		if (_CreateChannelState.containsKey("" + lChannelID)) {
			//System.out.println("20160309P2P    _CreateChannelState   _   lChannelID:"+ _CreateChannelState.get("" + lChannelID).toString());
			blnRet = FileUtil.ConvertToBoolean(_CreateChannelState.get("" + lChannelID), false);
			//System.out.println("20160309P2P    _CreateChannelState   _   blnRet:"+ blnRet);
		} else {
			//System.out.println("20160325P2P    _CreateChannelState   _   Not Find lChannelID:"+ lChannelID);
		}
		return blnRet;
	}
	//关闭连接....
	public static void CloseP2PConnect(String strUUID,int nOpenChannelIndex){
		long lChannelID = GetChannelIDByKey(strUUID,nOpenChannelIndex);	
		long lContext = GetChannelPlayContext(lChannelID);
		if(lContext != 0){
			//System.out.println("20160309P2P     CloseChannel7777777____Context:"+lContext);
			int nRet = -1;
			int nTryCount = 0;
			//System.out.println("20160309P2P     CloseChannel88888888____Context:"+lContext);
			nRet = P2PClose(lContext);
			//System.out.println("20160309P2P     CloseChannel999999999____Context:"+lContext);
			nTryCount++;
		}			
		RemoveChannelPlayContext(lChannelID);
		RemoveChannelIDByKey(strUUID, nOpenChannelIndex);	
		RemoveCreateChannelState(lChannelID);
	}

	public static void RemoveChannelPlayContext(long lChannelID) {
		_PlayChannelContextLock.lock();
		if (_ChannelPlayContext.containsKey("" + lChannelID)) {
			_ChannelPlayContext.remove("" + lChannelID);
		}
		_PlayChannelContextLock.unlock();
		if (_ChannelDecodeType.containsKey("" + lChannelID)) {
			_ChannelDecodeType.remove("" + lChannelID);
		}
		if (_ChannelIFrameStatus.containsKey("" + lChannelID)) {
			_ChannelIFrameStatus.remove("" + lChannelID);
		}
	}
	public static void RemoveChannelIDByKey(String strUUID,int nChannelIndex){
		String strKey = CreateDeviceChannelKey(strUUID,nChannelIndex);
		RemoveChannelIDByKey(strKey);
	}

	public static void RemoveChannelIDByKey(String strKey) {
		long lChannelID = GetChannelIDByKey(strKey);
		if (_ChannelKeyID.containsKey(strKey)) {
			_ChannelKeyID.remove(strKey);
		}
		if (_ChannelIDKey.containsKey("" + lChannelID)) {
			_ChannelIDKey.remove("" + lChannelID);
		}
		if (_ChannelDecodeType.containsKey("" + lChannelID)) {
			_ChannelDecodeType.remove("" + lChannelID);
		}
		if (_ChannelIFrameStatus.containsKey("" + lChannelID)) {
			_ChannelIFrameStatus.remove("" + lChannelID);
		}
	}

	public static void RemoveCreateChannelState(long lChannelID) {
		if (_CreateChannelState.containsKey("" + lChannelID)) {
			_CreateChannelState.remove("" + lChannelID);
		}
		if (_ChannelDecodeType.containsKey("" + lChannelID)) {
			_ChannelDecodeType.remove("" + lChannelID);
		}
		if (_ChannelIFrameStatus.containsKey("" + lChannelID)) {
			_ChannelIFrameStatus.remove("" + lChannelID);
		}
	}
	
	
	/**
	 * 发送消息
	 * @param strKey:区分消息体的关键区分值
	 * @param bdData:消息内容
	 */
	private static Lock m_SendMessageLock = new ReentrantLock();
	public static void SendMessage(Handler handler,Bundle bdData){
		SendMessage(handler,-1,bdData);	
	}
	/**
	 * 发送消息
	 * @param strKey:区分消息体的关键区分值
	 * @param nWhat:What
	 * @param bdData:消息内容
	 */
	public static void SendMessage(Handler handler, int nWhat, Bundle bdData) {
		m_SendMessageLock.lock();
		if (handler != null) {
			boolean blnHaveMessage = false;
			Message msg = new Message();
			if (nWhat != -1) {
				msg.what = nWhat;
			}
			if (bdData != null) {
				msg.setData(bdData);
				blnHaveMessage = true;
			}
			if (blnHaveMessage) {
				handler.sendMessage(msg);
			} else {
				handler.sendEmptyMessage(nWhat);
			}
		}
		m_SendMessageLock.unlock();
	}
	
	
	/**
	 * 关闭通道视频
	 * @param strUUID:设备UUID
	 * @param nOpenChannelIndex:播放设备通道
	 */
	public static void CloseChannel(String strUUID, int nOpenChannelIndex) {
		_Lock.lock();
		//System.out.println("20160309P2P     CloseChannel11111");
		long lChannelID = GetChannelIDByKey(strUUID, nOpenChannelIndex);
		long lContext = GetChannelPlayContext(lChannelID);
		if (lContext != 0) {
			String strKey = CreateDeviceChannelKey(strUUID, nOpenChannelIndex);
			if (lContext != 0) {
				//System.out.println("20160309P2P     CloseChannel222222____Context:"+ lContext);
				// Test_WriteFileNewLine(lChannelID,"20160309P2P     CloseChannel222222____Context:"+lContext);
				int nRet = -1;
				int nTryCount = 0;
				boolean blnCreateChannel = ChannelHaveCreate(lChannelID);
				// while( blnCreateChannel == true && nRet != 0 && nTryCount<5)
				{
					//System.out.println("20160309P2P     CloseChannel3333333____Context:"+ lContext);
					// Test_WriteFileNewLine(lChannelID,"20160309P2P     CloseChannel3333333____Context:"+lContext);
					nRet = P2PCloseChannel(lContext, nOpenChannelIndex);
					try {
						Thread.sleep(10, 10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("20160309P2P     CloseChannel4444444____Context:"+ lContext);
					// Test_WriteFileNewLine(lChannelID,"20160309P2P     CloseChannel4444444____Context:"+lContext);
					nTryCount++;
				}
				nRet = -1;
				nTryCount = 0;
				// while(nRet != 0 && nTryCount <5)
				{
					//System.out.println("20160309P2P     CloseChannel5555555____Context:"+ lContext);
					// Test_WriteFileNewLine(lChannelID,"20160309P2P     CloseChannel5555555____Context:"+lContext);
					nRet = P2PClose(lContext);
					try {
						Thread.sleep(10, 10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//System.out.println("20160309P2P     CloseChannel666666____Context:"+ lContext);
					nTryCount++;
				}

			}
			DecoderDebugger decoder = GetChannelDecoder(strUUID,nOpenChannelIndex);
			if (decoder != null) {
				if (_RealPlayDecoder.containsKey(strKey)) {
					_RealPlayDecoder.remove(strKey);
				}
				if (_RealPlaySurface.containsKey(strKey)) {
					_RealPlaySurface.remove(strKey);
				}
			}
			Monitor monitor = GetChannelMonitor(strUUID, nOpenChannelIndex);
			if (monitor != null) {
				// monitor.stopSoftDecoder(); //尝试关闭软件的处理
				if (_RealPlayMonitor.containsKey(strKey)) {
					_RealPlayMonitor.remove(strKey);
				}
			}
			//System.out.println("20160309P2P     CloseChannel" + strKey + " "+ lContext);
			RemoveChannelPlayContext(lChannelID);
			RemoveChannelIDByKey(strUUID, nOpenChannelIndex);
			RemoveCreateChannelState(lChannelID);
		}
		_Lock.unlock();
	}
	
	/**
	 * 获取通道对应的AVDecoder
	 * @param strUUID
	 * @param nChannelIndex
	 * @return
	 */
	public static DecoderDebugger GetChannelDecoder(String strUUID,
			int nChannelIndex) {
		DecoderDebugger dRet = null;
		String strKey = CreateDeviceChannelKey(strUUID, nChannelIndex);
		if (_RealPlayDecoder.containsKey(strKey)) {
			try {
				dRet = (DecoderDebugger) _RealPlayDecoder.get(strKey);
			} catch (Exception e) {
				// LogUtil.e("SDK", ExceptionsOperator.getExceptionInfo(e));
			}
		}
		return dRet;
	}
	/**
	 * 获取通道对应的Monitor
	 * @param strUUID
	 * @param nChannelIndex
	 * @return
	 */
	public static Monitor GetChannelMonitor(String strUUID, int nChannelIndex) {
		Monitor dRet = null;
		// String strKey = CreateDeviceChannelKey(strUUID,nChannelIndex);
		long lChannelID = GetChannelIDByKey(strUUID, nChannelIndex);
		if (_RealPlayMonitor.containsKey(lChannelID)) {
			try {
				dRet = (Monitor) _RealPlayMonitor.get(lChannelID);
			} catch (Exception e) {
				LogUtil.e("SDK", ExceptionsOperator.getExceptionInfo(e));
			}
		}
		return dRet;
	}
	
	/**
	 * 通过SessionID查询对应的AVDecoder
	 * @param lSession
	 * @return
	 */
	public static DecoderDebugger GetRealPlayDecoder(long lDeviceChannelID){
		DecoderDebugger decoder = null;
		if(_RealPlayDecoder.containsKey(""+lDeviceChannelID)){
			decoder=(DecoderDebugger)_RealPlayDecoder.get(""+lDeviceChannelID);
		}
		return decoder;
	}
	
	/**
	 * 通过SessionID查询对应的Monitor
	 * @param lSession
	 * @return
	 */
	public static Monitor GetRealPlayMonitor(long lChannelID) {
		Monitor decoder = null;
		if (_RealPlayMonitor.containsKey("" + lChannelID)) {
			decoder = (Monitor) _RealPlayMonitor.get("" + lChannelID);
		}
		return decoder;
	}
	
	public static Monitor GetChannelMonitor(long lChannelID) {
		Monitor monitor = null;
		if (_RealPlayMonitor.containsKey("" + lChannelID)) {
			monitor = (Monitor) _RealPlayMonitor.get("" + lChannelID);
		}
		return monitor;
	}
	/**
	 * 设置软解
	 * @param lSession
	 * @return
	 */
	public static void SetChannelDecodeType(long lChannelID,boolean blnDecodeType){	
		_ChannelDecodeType.put(""+lChannelID, blnDecodeType);	
		//System.out.println("2016.03.29TEST SetChannelDecodeType:"+lChannelID+";blnDecodeType:"+blnDecodeType);
	}
	public static boolean GetChannelDecoderType(long lChannelID){
		boolean blnRet = false;
		//System.out.println("2016.03.29TEST GetChannelDecoderType:"+lChannelID);
		if(_ChannelDecodeType.containsKey(""+lChannelID)){
			//System.out.println("2016.03.29TEST GetChannelDecoderType:"+lChannelID+"      ------          Have");
			blnRet = FileUtil.ConvertToBoolean(_ChannelDecodeType.get(""+lChannelID), false);
		}
		return blnRet;
	}
	
	public static int connectChannelP2P_device(String dId, int nOpenChannelIndex,Handler hdlConnectStateHandler) {
		int nRet = 0;
		nRet = P2PConnect(dId,nOpenChannelIndex);
		if (nRet == 0) {
			_ConnectChannelP2PHandler = hdlConnectStateHandler;
		}
		return nRet;
	}
	
	private static Lock _InformationLock = new ReentrantLock();	
	public static boolean GetChannelIFrameStatus(long lChannelID) {
		boolean blnRet = false;
		_InformationLock.lock();
//		System.out.println("2016.03.29TEST GetChannelIFrameStatus:"
//				+ lChannelID);
		if (_ChannelIFrameStatus.containsKey("" + lChannelID)) {
//			System.out.println("2016.03.29TEST GetChannelIFrameStatus:"
//					+ lChannelID + "      ------          Have");
			blnRet = FileUtil.ConvertToBoolean(_ChannelIFrameStatus.get("" + lChannelID), false);
		}
		_InformationLock.unlock();
		return blnRet;
	}
	
	public static boolean GetChannelHaveDataStatus(long lChannelID) {
		boolean blnRet = false;
		_InformationLock.lock();
//		System.out.println("2016.03.29TEST GetChannelIFrameStatus:"
//				+ lChannelID);
		if (_ChannelHaveDataStatus.containsKey("" + lChannelID)) {
//			System.out.println("2016.03.29TEST GetChannelIFrameStatus:"
//					+ lChannelID + "      ------          Have");
			blnRet = FileUtil.ConvertToBoolean(_ChannelHaveDataStatus.get("" + lChannelID), false);
		}
		_InformationLock.unlock();
		return blnRet;
	}
	
	public static void SetChannelHaveDataStatus(long lChannelID,
			boolean blnHaveStatus) {
		_InformationLock.lock();
		_ChannelHaveDataStatus.put("" + lChannelID, blnHaveStatus);
//		System.out.println("2016.03.29TEST SetChannelIFrameStatus:"
//				+ lChannelID + ";blnIFrameStatus:" + blnIFrameStatus);
		_InformationLock.unlock();
	}
	
	public static void SetChannelIFrameStatus(long lChannelID,
			boolean blnIFrameStatus) {
		_InformationLock.lock();
		_ChannelIFrameStatus.put("" + lChannelID, blnIFrameStatus);
//		System.out.println("2016.03.29TEST SetChannelIFrameStatus:"
//				+ lChannelID + ";blnIFrameStatus:" + blnIFrameStatus);
		_InformationLock.unlock();
	}
	
	public static void ClearChannelIFrameStatus(){
		_InformationLock.lock();
		_ChannelIFrameStatus.clear();
		_InformationLock.unlock();
	}
	
	private static void ThreadSleep(int n) {
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//.............................多画面处理
	
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

	/**
	 * print jsonObject 
	 * @param jsonData
	 */
	private void printJson(JSONObject jsonData){
		Set<String> keys = jsonData.keySet();
		for(String key : keys){
			LogUtil.d( "SDK", "key:" + key + "; type:"+jsonData.get(key).getClass().getName() + "; value:" +jsonData.get(key));
			
			/*if(jsonData.get(key) instanceof JSONObject){
				LogUtil.d( "SDK", key + " is JSONObject instance!");
			}else if(jsonData.get(key) instanceof JSONArray){
				LogUtil.d( "SDK", key + " is JSONArray instance!");
			}else{
				LogUtil.d( "SDK", "type:"+jsonData.get(key).getClass().getName());
				LogUtil.d( "SDK", key + " value is other instance! value " + jsonData.get(key));
			}*/ 
			
		}
	}
	
	public static String getJsonString(String sid){
		return sid +"|{\"type\":1,\"action\":109,\"sid\":\""+sid+"\"}";
	}
	
	public static String getJson(String sid, int channel){
		return sid +"|{\"type\":1,\"action\":104,\"sid\":\""+sid+"\",\"channel\":"+channel+"}";
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
	 * 接口返回值：=0表示成功，<0参照错误码
	 * 打洞返回值在onstate 里面处理
	 * */
	public static native int P2PConnect(String dId,long deviceChannelID);
	/*
	 * P2P转发
	 * jstring   链接对象目标ID
	 * 返回：0表示成功，<0参照错误码
	 * */
	public static native int P2PTransfor(String dId,long[] sessionId);
	
	/*
	 *  deviceid设备id
	 *  返回值 1 智诺 2 海康
	 * */
	public static native int AnalysisFactoryType(String deviceid);
	
	public static native int DecodeUuid(String uuid,byte[] buf);
	
	/*海康告警回放*/
	/*
	 * jbyteArray  输入数据
	 * jint        输入长度
	 * */
	public static native int InputHiKangData(byte[] inputdata,int inlen);

	/*
	  * jbyteArray  输出数据
	 * 	jint        输出长度
	 * 	返回值                    0 成功  1需要input数据
	 * 	jintArray   数据信息 目前有数据长度 宽 高信息
	 * 	注意                       input一次可能需要get多次 直到返回1时继续input数据
	 * */
	public static native int GetHiKangData(byte[] outputdata,int[] info);
	
	
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
	 * jint   :   channel
	 *	jint   :   发送标志
	*  返回值 >0 成功
	 * */
	public static native int Ffmpegh264EnCoder(byte[] buf, int len,byte[] data,int[] bIFrame,int channel,int isSend);
	/*
	 * jint :   帧率
	 * jint ：       码率
	 * jint :   宽
	 * jint ：     高
	 * */
	public static native int Ffmpegh264EncoderInit(int rate, int bitrate, int nWidth, int nHeigh);
	public static native int Ffmpegh264EncoderUninit();
	
	/*
	 * jstring  h264文件绝对路径
	 * jstring  mp4文件绝对路径
	 * jint     是否存在音频1存在 0不存在
	 * */
	public static native int Ffmpegh264ToMp4(String h246Path,String aacPath,String mp4Path,int flag);
	
	/*
	 * 设置软解录像文件路径
	 * videopath 视频文件路径
	 * audiopath 音频文件路径
	 * audioflag 音频标志 0没有音频  1 有音频
	 * */
	public static native int SetVideoPath(String videopath,String audiopath,long context);
	/*
	 * 设置软解录像文件路径
	 * jstring mp4 file
	 * */
	public static native int SetFinishVideo(String mp4file,long context);
	
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
	 * url 地址
	 * len 拖放时从多少字节开始播放
	 * rate 快放时的帧率
	 * */
	public static native int CurlSetOperation(String url,int len,int rate);
	
	/*硬解码 截图
	 *
	 *jbyteArray 输入数据
	 *jint       输入数据的长度
	 *jbyteArray 输出数据
	 *jint  	 返回值 输出数据的长度
	 * */
	public static native int AlarmDataPlayBack(byte[] indata,int len,byte[] outdata);
	
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
	 * jint  1 是软解  0 硬解 我们自己的软解 暂时不用
	 * long context 上下文
	 * */
	public static native int SetDecoderModel(int type,long deviceChannelID);
	public static native int SetDecoderModel(int type);//此接口不要了 
	/*
	 * jint  宽
	 * jint  高
	 * jint  帧率
	 * jint  码率
	 * ret = 0 初始化成功
	 * */
	public static native int Ffmpegh264DecoderInit(int width,int heigth,int framerate,int bitrate);
	public static native int Ffmpegh264DecoderUninit();
	/*硬解码 截图
	 *jbyteArray 输入数据
	 *jint       输入数据的长度
	 *jbyteArray 输出数据
	 *jint  	 返回值 输出数据的长度
	 * */
	public static native int ScreenShots(byte[] indata,int len,byte[] outdata,long context);
	
	//报警视频
	public static native int CurlSetOperation(String url,int len,int factorytype,int thresholdpricelen,long context);
	public static native long CurlSet();
	public static native int CurldownloadFinish(long context);
	public static native int CleanPool(long context);
	

	/**
	 *  * jlong 链接id
 * jint 索引
 * jstring 文件路径
 * jstring 开始时间
 * jstring 停止时间
 * jint    通道
 * jint    命令字
	 * @return
	 */
	public static native int PlaybackCtrl(long sessionId, int index, String filePath, String startTime, String endTime, int channel, int cmd);
	
	/**
	 * UUID编码
	 * @param nc
	 * @param ac
	 * @param fc
	 * @param tc
	 * @param mc
	 * @param vc
	 * @param sn
	 * @return
	 */
	public static native String EncodeUuid(byte[] uuid, String nc, int ac, int fc, int tc, int mc, int vc, String sn);
	
    //2015-7-20 最新版
    public static final int GW_ERRCODE_SUCESS = 0;
    //自定论错误码 从-100开始
    public static final int Error = -1;
    public static final int Err_refresh = -100;
    public static final int Err_SER_FAIL = -101;
    public static final int Err_Last_page = -102;//最后一页
    
    public static final int GW_SESSION_UNLINK = -2000;// P2P会话链接断开
    public static final int GW_ERRCODE_UNLOGIN = -1999;// 设备未登陆
    public static final int GW_ERRCODE_UPGRADE_PROGRAM = -1998;	// APP需要升级
    public static final int GW_ERRCODE_DEV_UNSUPORT = -1997;// 设备不支持该功能，需要升级
    
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
		case GW_ERRCODE_UPGRADE_PROGRAM:
			strId = R.string.GW_ERRCODE_UPGRADE_PROGRAM; break;
		case GW_ERRCODE_DEV_UNSUPORT:
			strId = R.string.GW_ERRCODE_DEV_UNSUPORT; break;
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
		case Err_Last_page:
			 strId = R.string.Err_Last_page; break;	 
			 
		}
		if(strId != 0){
			return APP.GetString(strId);
		}
		return APP.GetString(R.string.Err_Error) + " code = " + error;
	}
	
}
