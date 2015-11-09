package com.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-23 下午1:38:32
 * To change this template use File | Settings | File Templates.
 * Description： 验证类
 */
public class Validate {

	public static boolean isIpAddress(String ipAddress){   
		 String  ip="(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";   
		 Pattern pattern = Pattern.compile(ip);   
		 Matcher matcher = pattern.matcher(ipAddress);   
		 return matcher.matches();   
	 }
	 
	 
	//去list中重复的
	public static List<String> removeRepeat(List<String> list) {
		Set<String> set = new HashSet<String>();
		List<String> newList = new ArrayList<String>();
		for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (set.add((String) element))
				newList.add((String) element);
		}
		return newList;
	}
	
	//判断第一个时间是否比较小
	public static boolean isFirstTimelower(String beginTime,String endTime) { 
		long _beginTime = DateUtil.parseTime2Long(beginTime) / 1000;
		long _endTime = DateUtil.parseTime2Long(endTime) / 1000;
		return _beginTime < _endTime;
	}

	 

}