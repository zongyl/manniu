package com.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.util.Log;

import com.views.bovine.Fun_AnalogVideo;

public class FileUtil {

	public static String TAG = "FileUtil";
	
	private static File FILE = null;
	private static FileOutputStream FOS = null;
	private static BufferedOutputStream BOS = null;
	private static FileInputStream FIS = null;
	private static ByteArrayOutputStream BAOS = null;
	
	/**
	 * 获取byte[] 
	 * @param fileName 文件名称(带路径 全名)
	 * @return
	 */
	public static byte[] getBytes(String fileName){
		byte[] buffer =  null;
		try {
			FIS = new FileInputStream(fileName);
			BAOS = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = FIS.read(b)) != -1) {
				BAOS.write(b, 0, n);
			}
			FIS.close();
			BAOS.close();
			buffer = BAOS.toByteArray();
		} catch (Exception e) {
			LogUtil.e(TAG, "getBytes exception!");
		}
		return buffer;
	}
	
	public static void merge(String toFile, List<File> fromFiles){
		Log.d(TAG, "File.getFile fileName:" + toFile);
		try {
			FILE = new File(toFile);
			if(!FILE.getParentFile().exists()){
				FILE.getParentFile().mkdirs();
			}
			FOS = new FileOutputStream(FILE);
			BOS = new BufferedOutputStream(FOS);
			for(File file : fromFiles){
				BOS.write(getBytes(file.getAbsolutePath()));
			}
		} catch (Exception e) {
			LogUtil.e(TAG, "exception:" +e.getMessage());
		} finally {
			if(BOS != null){
				try {
					BOS.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "getFile bos.close exception!" + e.getMessage());
				}
			}
			if(FOS != null){
				try {
					FOS.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "getFile fos.close exception!" + e.getMessage());
				}
			}
		}
	}
	
	//文件上传至广场
	public static long fileLong;
	public static int readFile(String fileName){
		int nRet = 0;
		try {
			File file = new File(fileName);
			FIS = new FileInputStream(fileName);
			fileLong = file.length();
			byte[] b = new byte[1024*1024];
			int n;
			int nRetLength = 0;
			while ((n = FIS.read(b)) != -1) {
				nRetLength += n;
				if(fileLong == nRetLength){
					nRet = SDK.upLoadLocalMedia(b,n,true);
					//如果发送失败在重发一次
					if(nRet != 0){
						SDK.upLoadLocalMedia(b,n,true);
					}
				}else{
					nRet = SDK.upLoadLocalMedia(b,n,false);
					//如果发送失败在重发一次
					if(nRet != 0){
						SDK.upLoadLocalMedia(b,n,false);
					}
				}
			}
			FIS.close();
		} catch (Exception e) {
			LogUtil.e(TAG, "getBytes exception!");
		}
		return nRet;
	}
	
	/**
	 * create file
	 * @param bytes
	 * @param fileName 文件名
	 * @return
	 */
	public static File toFile(byte[] bytes, String fileName){
		Log.d(TAG, "File.getFile fileName:" + fileName);
		try {
			FILE = new File(fileName);
			if(!FILE.getParentFile().exists()){
				FILE.getParentFile().mkdirs();
			}
			FOS = new FileOutputStream(FILE);
			BOS = new BufferedOutputStream(FOS);
			BOS.write(bytes);
		} catch (Exception e) {
			LogUtil.e(TAG, "exception:" +e.getMessage());
		} finally {
			if(BOS != null){
				try {
					BOS.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "getFile bos.close exception!" + e.getMessage());
				}
			}
			if(FOS != null){
				try {
					FOS.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "getFile fos.close exception!" + e.getMessage());
				}
			}
		}
		return FILE;
	}
	
	@SuppressLint("SimpleDateFormat")
	public static String getFileName(String devName){
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
	//解析图片 url  返回  20160112130313_8a205bb0 作为缓存图片的KEY
	public static String getUrlKey(String str){
		str = str.substring(str.lastIndexOf("/")+1,str.length());
		str = str.substring(0, str.indexOf("."));
		return str;
	}
	
	 /**
	  * 将字符串转成long型
	  * @param strValue:值的字符串
	  * @param nDefaultValue:默认值
	  * @return
	  */
	public static long ConvertStringToLong(String strValue, long nDefaultValue) {
		long nRet = nDefaultValue;
		if (strValue != null && strValue.length() > 0) {
			try {
				nRet = Long.parseLong(strValue);
			} catch (Throwable e) {

			}
		}
		return nRet;
	}
	 /**将字符串转成int型**/
	public static int ConvertStringToInt(String strValue, int nDefaultValue) {
		int nRet = nDefaultValue;
		if (strValue != null && strValue.length() > 0) {
			try {
				nRet = Integer.parseInt(strValue);
			} catch (Throwable e) {

			}
		}
		return nRet;
	}

	public static String ConvertObjectToString(Object objValue,String strDefauleValue) {
		String strRet = strDefauleValue;
		if (objValue != null) {
			strRet = objValue.toString();
		}
		return strRet;
	}
	
	public static boolean ConvertToBoolean(Object objValue, boolean defaultValue) {
		return ConvertStringToBoolean(ConvertObjectToString(objValue, "true"),defaultValue);
	}

	public static boolean ConvertStringToBoolean(String strValue,boolean defaultValue) {
		boolean blnRet = defaultValue;
		blnRet = Boolean.parseBoolean(strValue);
		return blnRet;
	}
	
}
