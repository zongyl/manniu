package com.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2008-6-11
 * Time: 9:24:01
 * 时间转换公共类
 */
public class DateUtil {
    /**
     * 取得当前日期
     * @return Date 当前日期
     */
    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 返回当前日期对应的默认格式的字符串
     * @return String 当前日期对应的字符串
     */
    public static String getCurrentStringDate() {
        return convertDate2String(getCurrentDate(), DEFAULT_DATE_FORMAT);
    }

    /**
     * 返回当前日期对应的指定格式的字符串
     * @param dateFormat - 日期格式
     * @return String 当前日期对应的字符串`
     */
    public static String getCurrentStringDate(String dateFormat) {
        return convertDate2String(getCurrentDate(), dateFormat);
    }

    /**
     * 将日期转换成指定格式的字符串
     * @param date - 要转换的日期
     * @param dateFormat - 日期格式
     * @return String 日期对应的字符串
     */
    public static String convertDate2String(Date date, String dateFormat) {
        SimpleDateFormat sdf;
        if(dateFormat != null && !dateFormat.equals("")) {
            try {
                sdf = new SimpleDateFormat(dateFormat);
            } catch(Exception e) {
                sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            }
        } else {
            sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }
        return sdf.format(date);
    }

    /**
     * 将字符串转换成日期
     * @param stringDate - 要转换的字符串格式的日期
     * @return Date 字符串对应的日期
     */
    public static Date convertString2Date(String stringDate) {
        return convertString2Date(stringDate, DEFAULT_DATE_FORMAT);
    }

    /**
     * 将时间从毫秒数转化为字符串格式
     * @param time
     * @return String 日期时间的字符串形式
     */
    public static String getStringDateByLong(long time){
    	if(time==0){
    		return "";
    	}
        return convertDate2String(new Date(time),"");
    }
    public static String getStringDateByLong(long time,String dateFormat){
    	if(time == 0)
    		return "";
        return convertDate2String(new Date(time),dateFormat);
    }
    
    
    /**
     * 将字符串转换成日期
     * @param stringDate - 要转换的字符串格式的日期
     * @param dateFormat - 要转换的字符串对应的日期格式
     * @return Date 字符串对应的日期
     */
    public static Date convertString2Date(String stringDate, String dateFormat) {
        SimpleDateFormat sdf;
        if(dateFormat != null && !dateFormat.equals("")) {
            try {
                sdf = new SimpleDateFormat(dateFormat);
            } catch(Exception e) {
                sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            }
        } else {
            sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }
        try {
            return sdf.parse(stringDate);
        } catch(ParseException pe) {
            return new Date();
        }
    }

    /**
     * 将一种格式的日期字符串转换成默认格式的日期字符串
     * @param oldStringDate - 要格式化的日期字符串
     * @param oldFormat - 要格式化的日期的格式
     * @return String 格式化后的日期字符串
     */
    public static String formatStringDate(String oldStringDate, String oldFormat) {
        return convertDate2String(convertString2Date(oldStringDate, oldFormat), DEFAULT_DATE_FORMAT);
    }

    /**
     * 将一种格式的日期字符串转换成另一种格式的日期字符串
     * @param oldStringDate - 要格式化的日期字符串
     * @param oldFormat - 要格式化的日期的格式
     * @param newFormat - 格式化后的日期的格式
     * @return String 格式化后的日期字符串
     */
    public static String formatStringDate(String oldStringDate, String oldFormat, String newFormat) {
        return convertDate2String(convertString2Date(oldStringDate, oldFormat), newFormat);
    }
    /**
     * 从easyUI获取到时间转换成秒数进行显示
     * @param date
     * @param time
     * @return
     */
    public static long parseTime2Long(String date,String time) {
    	
    	if (date !=null && !"".equals(date.trim())) {
			if ("".equals(time)) {
				time = "00:00:00";
			}
			return convertString2Date(date + " " + time,"yyyy-MM-dd HH:mm:ss").getTime();
			 
		}
		return 0;
    	
    	
    }
    /**
     * 从mydate97获取到时间转换成秒数进行显示
     * @param date
     * @param time
     * @return
     */
    public static long parseTime2Long(String time) {
    	
    	if (time !=null && !"".equals(time.trim())) {
			if ("".equals(time)) {
				time = "00:00:00";
			}
			return convertString2Date(time,"yyyy-MM-dd HH:mm:ss").getTime();
			 
		}
		return 0;
    	
    	
    }
    /**
     * 默认的日期格式
     */
    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static String DEFAULT_DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    public static int getDaysByMonth(int year,int month){
        Calendar c = new GregorianCalendar(year,month,1);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getNowyear(){
        return Integer.valueOf(convertDate2String(getCurrentDate(),"yyyy"));
    }

    public static int compDate(Date date1,Date date2){
        Calendar c = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c.clear();
        c.setTime(date1);
        c2.clear();
        c2.setTime(date2);
        return c.compareTo(c2);
    }
    //系统时间转long 
    public static long getSysTolong(){
    	Date dt=new Date();
    	long longtime = dt.getTime()/1000; 
		return longtime;    	
    }
  //天数加1
    public static Date getDateAddDays(Date date,int day){
    	//Date  newDate2 = new Date(date1.getTime() + 1000 * 60 * 60 * 24);   
        Calendar c  = Calendar.getInstance();   
        c.setTime(date);   
        c.add(Calendar.DATE, day);   
        Date  newDate  =  c.getTime();   
        return newDate;
    }
}
