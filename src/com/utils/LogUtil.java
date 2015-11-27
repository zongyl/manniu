package com.utils;

import java.io.BufferedWriter;  
import java.io.File;  
import java.io.FileWriter;  
import java.io.IOException;  
import java.text.ParseException;
import java.text.SimpleDateFormat;  
import java.util.Calendar;  
import java.util.Date;  
import com.views.NewLogin;
import android.annotation.SuppressLint;
import android.util.Log;  
  
/** 
 * 带日志文件输入的，又可控开关的日志调试 
 */  
@SuppressLint("SimpleDateFormat")
public class LogUtil {  
	private static final String TAG = "LogUtil";
    private static Boolean MYLOG_SWITCH=true; // 日志文件总开关  
    private static Boolean MYLOG_WRITE_TO_FILE=true;// 日志写入文件开关  
    private static char MYLOG_TYPE='v';// 输入日志类型，w代表只输出告警信息等，v代表输出所有信息  
    //private static String LOG_PATH = Fun_AnalogVideo.logPath;// 日志文件在sdcard中的路径  
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 3;// sd卡中日志文件的最多保存天数  
    private static String _LogName = ".log";// 本类输出的日志文件名称  
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式  
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式  
  
    public static void w(String tag, Object msg) { // 警告信息  
        log(tag, msg.toString(), 'w');  
    }  
  
    public static void e(String tag, Object msg) { // 错误信息  
        log(tag, msg.toString(), 'e');  
    }  
  
    public static void d(String tag, Object msg) {// 调试信息  
        log(tag, msg.toString(), 'd');  
    }  
  
    public static void i(String tag, Object msg) {//  
        log(tag, msg.toString(), 'i');  
    }  
  
    public static void v(String tag, Object msg) {  
        log(tag, msg.toString(), 'v');  
    }  
  
    public static void w(String tag, String text) {  
        log(tag, text, 'w');  
    }  
  
    public static void e(String tag, String text) {  
        log(tag, text, 'e');  
    }  
  
    public static void d(String tag, String text) {  
        log(tag, text, 'd');  
    }  
  
    public static void i(String tag, String text) {  
        log(tag, text, 'i');  
    }  
  
    public static void v(String tag, String text) {  
        log(tag, text, 'v');  
    }  
  
    /** 
     * 根据tag, msg和等级，输出日志 
     *  
     * @param tag 
     * @param msg 
     * @param level 
     * @return void 
     * @since v 1.0 
     */  
    private static void log(String tag, String msg, char level) {  
    	try {
    		if (MYLOG_SWITCH) {
                if ('e' == level && ('e' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) { // 输出错误信息  
                    Log.e(tag, msg);  
                } else if ('w' == level && ('w' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {  
                    Log.w(tag, msg);  
                } else if ('d' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {  
                    Log.d(tag, msg);  
                } else if ('i' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {  
                    Log.i(tag, msg);  
                } else {  
                    Log.v(tag, msg);  
                }  
                if (MYLOG_WRITE_TO_FILE)  
                    writeLogtoFile(String.valueOf(level), tag, msg);  
            }
		} catch (Exception e) {
		}
    }  
  
    /** 
     * 打开日志文件并写入日志 
     *  
     * @return 
     * **/  
    public static void writeLogtoFile(String mylogtype, String tag, String text) {// 新建或打开日志文件  
        try {  
        	Date nowtime = new Date();  
            //String needWriteFiel = logfile.format(nowtime);  
            String needWriteFiel = DateUtil.getCurrentStringDate();
            String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype  
                    + "    " + tag + "    " + text;  
            
            //File file = new File(NewLogin.logPath, needWriteFiel + _LogName);
            
            File file2 = checkLogFileIsExist();
            if(file2 != null && file2.isFile()){
            	file2 = new File(NewLogin.logPath, needWriteFiel + _LogName); 
                
                FileWriter filerWriter = new FileWriter(file2, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖  
                BufferedWriter bufWriter = new BufferedWriter(filerWriter);  
                bufWriter.write(needWriteMessage);  
                bufWriter.newLine();  
                bufWriter.close();  
                filerWriter.close(); 
            }
             
        } catch (IOException e) {  
        }  
    }  
    
    /**检查日志文件是否存在*/
	public static File checkLogFileIsExist() {
		File file = new File(NewLogin.logPath);
		try {
			if (!file.exists()) {
				file.mkdirs();
			}
			
			String dateStr = DateUtil.getCurrentStringDate();
			file = new File(NewLogin.logPath + dateStr + _LogName);
			if (!isLogExist(file)) {
				try {
					file.createNewFile();
				} catch (IOException e) {
				}
			}
			return file;
		} catch (Exception e) {
			return null;
		}
	}
    
    /**
     * 检查当天日志文件是否存在
     * @param file
     * @return
     */
	public static boolean isLogExist(File file) {
		File tempFile = new File(NewLogin.logPath);
		File[] files = tempFile.listFiles();
		if(files != null){
			for (int i = 0; i < files.length; i++) {
				if (files[i].getName().trim().equalsIgnoreCase(file.getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	 /**
     * 删除内存下过期的日志
     */
	public static void deleteSDcardExpiredLog() {
		try {
			File file = new File(NewLogin.logPath);
			if (file.isDirectory()) {
				File[] allFiles = file.listFiles();
				for (File logFile : allFiles) {
					String fileName = logFile.getName();
					if (_LogName.equals(fileName)) {
						continue;
					}
					String createDateInfo = getFileNameWithoutExtension(fileName);
					if (canDeleteSDLog(createDateInfo)) {
						logFile.delete();
						//LogUtil.d(TAG, "delete expired log success,the log path is:"+ logFile.getAbsolutePath());
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
    /**
     * 判断sdcard上的日志文件是否可以删除
     * @param createDateStr
     * @return
     */
	private static boolean canDeleteSDLog(String createDateStr) {
		boolean canDel = false;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1 * SDCARD_LOG_FILE_SAVE_DAYS);
		Date expiredDate = calendar.getTime();
		try {
			Date createDate = logfile.parse(createDateStr);
			canDel = createDate.before(expiredDate);
		} catch (ParseException e) {
			Log.e(TAG, ExceptionsOperator.getExceptionInfo(e), e);
			canDel = false;
		}
		return canDel;
	}
	/**
     * 去除文件的扩展类型（.log）
     * @param fileName
     * @return
     */
    private static String getFileNameWithoutExtension(String fileName){
        return fileName.substring(0, fileName.indexOf("."));
    }
    
    
  
}