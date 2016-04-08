package com.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceParcel extends Device implements Parcelable{

	public DeviceParcel(Device device){
		devname = device.devname;
		sid = device.sid;
		dpassword = device.dpassword;
		createtime = device.createtime;
		domainid = device.domainid;
		userid = device.userid;
		pn = device.pn;
		vn = device.vn;
		sn = device.sn;
		model = device.model;
		ver = device.ver;
		logo = device.logo;
		state = device.state;
		type = device.type;
		isowner = device.isowner;
		online = device.online;
		channels = device.channels;
		channelNo = device.channelNo;
	}
	
	public DeviceParcel(Parcel in){
		//DeviceParcel dp = new DeviceParcel();
		devname = in.readString();
		sid = in.readString();
		dpassword = in.readString();
		createtime = in.readString();
		domainid = in.readString();
		userid = in.readString();
		pn = in.readString();
		vn = in.readString();
		sn = in.readString();
		model = in.readString();
		ver = in.readString();
		logo = in.readString();
		state = in.readInt();
		type = in.readInt();
		channels = in.readInt();
		channelNo = in.readInt();
		online = in.readInt();
		isowner = in.readInt();
		//return dp;
	}
	
	public static final Parcelable.Creator<DeviceParcel> CREATOR = new Parcelable.Creator<DeviceParcel>() {

		@Override
		public DeviceParcel createFromParcel(Parcel in) {
			return new DeviceParcel(in);
		}

		@Override
		public DeviceParcel[] newArray(int size) {
			return new DeviceParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(devname);
		out.writeString(sid);
		out.writeString(dpassword);
		out.writeString(createtime);
		out.writeString(domainid);
		out.writeString(userid);
		out.writeString(pn);
		out.writeString(vn);
		out.writeString(sn);
		out.writeString(model);
		out.writeString(ver);
		out.writeString(logo);
		out.writeInt(state);
		out.writeInt(type);
		out.writeInt(isowner);
		out.writeInt(online);
		if(channelNo != null){
			out.writeInt(channelNo);
		}
		if(channels != null){
			out.writeInt(channels);
		}
	}

}
