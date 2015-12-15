package com.adapter;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 推送消息
 * @author pc
 *
 */
@SuppressLint("ParcelCreator")
public class Message implements Parcelable{
	/** 移动侦测报警 */
	public String title;
	/** 时间戳 */
	public Long time;
	/** 设备名称  example:C2mini(502478205) */
	public String devName;
	/** 报警封面图片 */
	public String picPath;
	/** 报警录像  */
	public String videoPath;

	public String devicename;
	
	public String evt_picture;
	
	public int evt_state;

	public int evt_type;
	
	public String evt_video;
	public int evt_vsize;
	
	public String kid;

	public String logtime;
	
	public String uuid;
	
	public String evt_time;
	
	/*
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public String getDevName() {
		return devName;
	}
	public void setDevName(String devName) {
		this.devName = devName;
	}
	public String getPicPath() {
		return picPath;
	}
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
	public String getVideoPath() {
		return videoPath;
	}
	public void setVideoPath(String videoPath) {
		this.videoPath = videoPath;
	}
	public int getEvt_vsize() {
		return evt_vsize;
	}
	public void setEvt_vsize(int evt_vsize) {
		this.evt_vsize = evt_vsize;
	}
	public String getDevicename() {
		return devicename;
	}
	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}
	public String getEvt_picture() {
		return evt_picture;
	}
	public void setEvt_picture(String evt_picture) {
		this.evt_picture = evt_picture;
	}
	public int getEvt_state() {
		return evt_state;
	}
	public void setEvt_state(int evt_state) {
		this.evt_state = evt_state;
	}
	public int getEvt_type() {
		return evt_type;
	}
	public void setEvt_type(int evt_type) {
		this.evt_type = evt_type;
	}
	public String getEvt_video() {
		return evt_video;
	}
	public void setEvt_video(String evt_video) {
		this.evt_video = evt_video;
	}
	public String getKid() {
		return kid;
	}
	public void setKid(String kid) {
		this.kid = kid;
	}
	public String getLogtime() {
		return logtime;
	}
	public void setLogtime(String logtime) {
		this.logtime = logtime;
	}*/
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<Message> CREATOR = new Creator<Message>(){

		@Override
		public Message createFromParcel(Parcel source) {
			Message msg = new Message();
			msg.title = source.readString();
			msg.time = source.readLong();
			msg.devName = source.readString();
			msg.picPath = source.readString();
			msg.videoPath = source.readString();
			msg.devicename =source.readString();
			msg.evt_picture= source.readString();
			msg.logtime = source.readString();
			msg.uuid = source.readString();
			msg.evt_time = source.readString();
			msg.evt_vsize = source.readInt();
			msg.evt_video = source.readString();
			return msg;
		}

		@Override
		public Message[] newArray(int arg0) {
			return new Message[arg0];
		}
		
	};
	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		if(time==null){
			time =0l;
		}
		if(title==null){
			title="";
		}
		if(devName==null){
			devName="";
		}
		if(picPath==null){
			picPath="";
		}
		if(videoPath==null){
			videoPath="";
		}
		if(devicename==null){
			devicename="";
		}
		if(evt_picture==null){
			evt_picture="";
		}
		if(logtime==null){
			logtime="";
		}
		dest.writeString(title);
		dest.writeLong(time);
		dest.writeString(devName);
		dest.writeString(picPath);
		dest.writeString(videoPath);
		dest.writeString(devicename);
		dest.writeString(evt_picture);
		dest.writeString(logtime);
		dest.writeString(uuid);
		dest.writeString(evt_time);
		dest.writeInt(evt_vsize);
		dest.writeString(evt_video);
	}
}