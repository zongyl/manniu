package com.basic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-14 上午11:57:52
 * To change this template use File | Settings | File Templates.
 * Description：通道类型
 */
public class ChannelType {
	//通道类型z：1-普通摄像机，2-高速球机，3-半球机  4-门禁，
	public final static int VIDEO = 1;
	public final static int HIGHSPEED = 2;
	public final static int HALFSPEED = 3;
	
	public final static int S_TYPE1 = 1;
	public final static int S_TYPE2 = 2;
	public final static int S_TYPE3 = 3;
	public final static int S_TYPE4 = 4;
	public final static int S_TYPE5 = 5;
	public final static int S_TYPE6 = 6;
	public final static int S_TYPE7 = 7;
	public final static int S_TYPE8 = 8;
	public final static int S_TYPE9 = 9;
	public final static int S_TYPE10 = 10;
	public final static int S_TYPE11 = 11;
	public final static int S_TYPE12 = 12;
	public final static int S_TYPE13 = 13;
	public final static int S_TYPE14 = 14;
	public final static int S_TYPE15 = 15;
	public final static int S_TYPE16 = 16;
	public final static int S_TYPE17 = 17;
	public final static int S_TYPE18 = 18;
	public final static int S_TYPE60000 = 60000;
	
	private static Map<String,String> chnl_map = new HashMap<String,String>();
	private static String chnl_value = "未订义类型";
	static {
		chnl_map.put("1", "温度");
		chnl_map.put("2", "湿度");
		chnl_map.put("4", "水浸");
		chnl_map.put("5", "烟感");
		chnl_map.put("6", "门磁");
		chnl_map.put("7", "开关量");
		chnl_map.put("8", "模拟量");
		chnl_map.put("10", "继电器输出");
	}
	
	public static String getChnlResponseCode(int code){
		String temp = chnl_map.get(Integer.toString(code));
		if(temp == null || temp.equals("")){
			return chnl_value;
		}else {
			return temp;
		}		
	}
	
	
	
}