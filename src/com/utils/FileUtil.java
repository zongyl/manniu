package com.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import P2P.SDK;
import android.util.Log;

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
	
	//文件上传至广场
	public static long fileLong;
	public static int readFile(String fileName){
		int nRet = 0;
		try {
			//Thread.sleep(5000);
			File file = new File(fileName);
			FIS = new FileInputStream(fileName);
			BAOS = new ByteArrayOutputStream(1024*1024);
			fileLong = file.length();
			//System.out.println(file.length()+"--");
			byte[] b = new byte[1024*1024];
			int n;
			while ((n = FIS.read(b)) != -1) {
				BAOS.write(b, 0, n);
				if(fileLong == BAOS.toByteArray().length){
					nRet = SDK.upLoadLocalMedia(BAOS.toByteArray(),n,true);
				}else{
					nRet = SDK.upLoadLocalMedia(BAOS.toByteArray(),n,false);
				}
				//System.out.println(".........."+BAOS.toByteArray().length+"---"+n);
			}
			FIS.close();
			BAOS.close();
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
	
}
