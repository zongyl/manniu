package com.basic;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class G {
	public static int ToInt(byte[] bytes, int start) {
		if (bytes == null || start < 0 || start + 4 > bytes.length) {
			return 0;
		}

		int[] ia = new int[4];
		for (int i = 0; i < 4; ++i) {
			ia[i] = bytes[i + start] < 0 ? bytes[i + start] + 256 : bytes[i + start];
		}
		return (ia[3] << 24 | ia[2] << 16 | ia[1] << 8 | ia[0]);
	}
	
	public static int oneByte2Int(byte byteNum) {
		return byteNum > 0 ? byteNum : (128 + (128 + byteNum));  
	}

	public static Date StrToDate(String date) {
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date time = null;
		try {
			time = formatDate.parse(date);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		return time;
	}
	
	public static boolean StrToDate(String strDate, int []time) {
		try {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dt = null;
			dt = formatDate.parse(strDate);
			time[0] = dt.getYear() + 1900;
			time[1] = dt.getMonth() + 1;
			time[2] = dt.getDate();
			time[3] = dt.getHours();
			time[4] = dt.getMinutes();
			time[5] = dt.getSeconds();
			return true;
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}

		return false;
	}
	/** 
	 * 字节转换为字符串
	 * @param pBuffer 字节（至少4个字节） 
	 * @param index 开始位置  length 长度
	 * @return 
	*/ 
	public static String BytesToStr(byte[] pBuffer, int start, int length) {
		if (pBuffer == null) {
			return "";
		}
		int end = start + length;
		if (pBuffer.length < end) {
			end = pBuffer.length;
		}
		int i = start;
		for (; i < end; ++i) {
			if (pBuffer[i] == 0) {
				break;
			}
		}

		return new String(pBuffer, start, i - start);
	}
	/** 
	 * 字节转换为浮点 
	 * @param buf 字节（至少4个字节） 
	 * @param index 开始位置 
	 * @return 
	*/  
	public static float byte2float(byte[] buf, int index) {    
	    int a;                                             
	    a = buf[index + 0];                                  
	    a &= 0xff;                                         
	    a |= ((long) buf[index + 1] << 8);                   
	    a &= 0xffff;                                       
	    a |= ((long) buf[index + 2] << 16);                  
	    a &= 0xffffff;                                     
	    a |= ((long) buf[index + 3] << 24);                  
	    return Float.intBitsToFloat(a);                    
	}  

	public static String byte2hex(byte[] buffer, int start, int len) {
		String h = "";

		for (int i = 0; i < len; i++) {
			String temp = Integer.toHexString(buffer[i + start] & 0xFF);
			if (temp.length() == 1) {
				temp = "0" + temp;
			}
			h = h + temp;
		}

		return h;

	}
	
	public static String ToGB2312(byte[] pBuffer, int start, int length) {
		if (pBuffer == null) {
			return "";
		}
		//System.out.println(byte2hex(pBuffer, start, length));
		
		int end = start + length;
		if (pBuffer.length < end) {
			end = pBuffer.length;
		}
		int i = start;
		for (; i < end; ++i) {
			if (pBuffer[i] == 0) {
				break;
			}
		}

		try {
			ByteArrayInputStream is = new ByteArrayInputStream(pBuffer, start, i - start);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "gb2312"));
			return reader.readLine();
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public static boolean CreatePath(String path) {
		File file = new File(path);
		if (!file.exists()) {// 判断文件夹是否存在,如果不存在则创建文件夹
			return file.mkdirs();
		} else {
			return true;
		}
	}
}