package com.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**作者 杨明 2015-5-30*/
public class Update implements Serializable{
	private static final long serialVersionUID = -23432523613616l;
	public final static String UTF8 = "UTF-8";
	public final static String NODE_ROOT = "oschina";
	private int  versionCode;
	private String  versionName;
	private String downloadUrl;
	private String updateLog;
	private String appName;
	private String apkFileSize;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getUpdateLog() {
		return updateLog;
	}
	public void setUpdateLog(String updateLog) {
		this.updateLog = updateLog;
	}
	
	
	
	public String getApkFileSize() {
		return apkFileSize;
	}
	public void setApkFileSize(String apkFileSize) {
		this.apkFileSize = apkFileSize;
	}
	public static Update parse(InputStream inputStream) throws IOException,Exception{
		Update update = null;
		//获得XmlPullParser解析器
		XmlPullParser xmlParser = Xml.newPullParser();
		try{
			xmlParser.setInput(null, UTF8);
			//获得解析到的时间类别，这里有开始文档，结束文档,开始标签，结束标签，文本等事件
			int eventType = xmlParser.getEventType();
			//遍历文档
			while(eventType!=xmlParser.END_DOCUMENT){
				String tag = xmlParser.getName();
				switch(eventType){
				case  XmlPullParser.START_TAG:
					//通知消息
					if(tag.equalsIgnoreCase("android")){
						update = new Update();
					}else if(update!=null){
						if(tag.equalsIgnoreCase("versionCode")){
							update.setVersionCode(Integer.parseInt(xmlParser.nextText(), 0));
						}else if(tag.equalsIgnoreCase("versionName")){
							update.setVersionName(xmlParser.nextText());
						}else if(tag.equalsIgnoreCase("downloadUrl")){
							update.setDownloadUrl(xmlParser.nextText());
						}else if(tag.equalsIgnoreCase("updateLog")){
							update.setUpdateLog(xmlParser.nextText());
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				//如果xml没有结束，导航到下一个节点
				eventType = xmlParser.next();
			}
			
		}catch(XmlPullParserException e){
			
		}finally{
			inputStream.close();
		}
		return update;
	}
	
}
