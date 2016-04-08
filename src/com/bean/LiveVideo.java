package com.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class LiveVideo implements Parcelable{
	private String sid;//设备ID
	private String livename;//直播标题
	private String introduction;//直播介绍
	private String userid;//分享人ID
	private String starttime;//分享创建时间
	private String lasttime;//开始分享时间
	private int type;//直播类型
	private int state;//直播状态
	private String img;//封面
	
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getLivename() {
		return livename;
	}
	public void setLivename(String livename) {
		this.livename = livename;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getLasttime() {
		return lasttime;
	}
	public void setLasttime(String lasttime) {
		this.lasttime = lasttime;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toString() {
		return "LiveVideo [did=" + sid + ", livename=" + livename
				+ ", introduction=" + introduction + ", userid=" + userid
				+ ", starttime=" + starttime + ", lasttime=" + lasttime
				+ ", type=" + type + ", state=" + state + ", img=" + img + "]";
	}

	public static final Parcelable.Creator<LiveVideo> CREATOR = new Creator<LiveVideo>(){
		@Override
		public LiveVideo createFromParcel(Parcel source) {
			LiveVideo video = new LiveVideo();
			video.sid = source.readString();
			video.userid = source.readString();
			video.livename = source.readString();
			video.introduction = source.readString();
			video.lasttime = source.readString();
			video.starttime = source.readString();
			video.state = source.readInt();
			video.type = source.readInt();
			video.img = source.readString();
			return video;
		}

		@Override
		public LiveVideo[] newArray(int arg0) {
			return new LiveVideo[arg0];
		}
	};
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(sid);
		dest.writeString(userid);
		dest.writeString(livename);
		dest.writeString(introduction);
		dest.writeString(lasttime);
		dest.writeString(starttime);
		dest.writeInt(state);
		dest.writeInt(type);
		dest.writeString(img);
	}
}	
