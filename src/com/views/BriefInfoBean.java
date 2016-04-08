package com.views;
/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-9-5 上午11:50:19
 * To change this template use File | Settings | File Templates.
 * Description： 简单信息 BEAN
 */
public class BriefInfoBean {
	
	public String uuid;
	public String name;
	public float dataValue;
	public String info;
	public String evt_video;
	public int size;
	public String evt_picture;//原图片
	public String thumb_url;//缩略图片
	
	
	public String getEvt_picture() {
		return evt_picture;
	}
	public void setEvt_picture(String evt_picture) {
		this.evt_picture = evt_picture;
	}
	public String getThumb_url() {
		return thumb_url;
	}
	public void setThumb_url(String thumb_url) {
		this.thumb_url = thumb_url;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getEvt_video() {
		return evt_video;
	}
	public void setEvt_video(String evt_video) {
		this.evt_video = evt_video;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public float getDataValue() {
		return dataValue;
	}
	public void setDataValue(float dataValue) {
		this.dataValue = dataValue;
	}
	
	

}
