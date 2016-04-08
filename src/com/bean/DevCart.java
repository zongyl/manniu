package com.bean;

import java.io.Serializable;

public class DevCart implements Serializable
{
	private static final long serialVersionUID = 4749864185214449263L;

	Device deviceInfo;
	int channelNum;
	int scj_id;
	int dev_id;
	
	

	public int getDev_id() {
		return dev_id;
	}

	public void setDev_id(int dev_id) {
		this.dev_id = dev_id;
	}

	public Device getDeviceInfo()
	{
		return deviceInfo;
	}

	public void setDeviceInfo(Device deviceInfo)
	{
		this.deviceInfo = deviceInfo;
	}

	public int getChannelNum()
	{
		return channelNum;
	}

	public void setChannelNum(int channelNum)
	{
		this.channelNum = channelNum;
	}

	public int getScj_id() {
		return scj_id;
	}

	public void setScj_id(int scj_id) {
		this.scj_id = scj_id;
	}


}
