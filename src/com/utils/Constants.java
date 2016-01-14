package com.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.media.AudioFormat;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-16 上午8:42:50
 * To change this template use File | Settings | File Templates.
 * Description：
 */
public class Constants {
	
	public static int childFlge = 0; //子功能标识类型
	public static String childTitle = "图片"; //子功能标题
	//0x|ff|ff00ff，0x是代表颜色整数的标记，ff是表示透明度，ff00ff表示颜色
	public final static int COLOR_WHITE = 0xFFffffff; //白色
	public final static int COLOR_SELECTED = 0xFF00A2FF; //选中背景色
	public final static int COLOR_Transparent = 0x90FFFFFF; //透明背景
	public final static int COLOR_black = 0xFF000000; //白色
	public final static int COLOR_TAB_TEXT = 0xFF207F9F;//#207F9F
	public final static int COLOR_BACKGROUD = 0xFF000000; //黑色
	
	public static int screenwidth = 460;
	public static int screenHeigh = 420;
	
	public static int ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_16BIT; //bit率
	public static int CHANNEL_CONFIGURATION_MONO = 8000;//每秒8K个点
	
	public static int frequency = 44100;//音频采集率
	public static int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;  //2
	public static int audionEncoding = AudioFormat.ENCODING_PCM_16BIT;  //2
	public static long maxInputSamples = 0;//获取编码器最大输入数
	
	public static String userName = "";//全局用户名
	public static String userid = "";//全局用户sid
	public static long sessionId = 0;//记录sessionID
	public static String session_Id = "";//记录ETS返回的sessionID
	//public static String userPassword = "";
	
	//cn
//	public static String hostUrl = "http://120.26.194.72/NineCloud";//www.nmy9.com
//	public static String ETShostUrl = "http://120.26.56.240:9601";

	//us
//	public static String hostUrl = "http://47.88.30.207/NineCloud";
//	public static String ETShostUrl = "http://47.88.30.207:9601";
	
	//ts
	public static String hostUrl = "http://ts.mny9.com";//ts.mny9.com
	public static String ETShostUrl = "http://ts.cms.mny9.com:9601";
	
	//控制视频显示窗口
	public static int viewNum = 1;
	//视频图片大小 
	public static int imgViewWidth = 352;  
	public static int imgViewHeight = 288;
	
	//XML信息
	public final static String SERVERINFO_VIEWNUM = "viewNum";
	
	//网络连接状态
	public static boolean netWakeState = true;
	//0:mobile 1:WIFI
	public static int netWakeType = -1;
	//消息
	public static int smsOnlcikState = 0;
	//录像适配器
	public static ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
	//相册
	public static Bitmap bimap;
	public static String _bucketName;//相册名称
	public static String packageName;//项目名称
	
	

}