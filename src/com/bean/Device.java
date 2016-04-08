package com.bean;

import java.io.Serializable;


public class Device implements Serializable{
	private static final long serialVersionUID = 2838790099397564806L;
/*
 [{"createtime":"2014-11-15 03:06:15.0",
 "devicesname":"c1(001237a8)",
 "domainid":"Q04BAQsAAGJmMjFjYmQwMDM3YjEx",
 "sid":"001237a8","state":1,"type":1,"userid":"Q04BAQ0AADAzZWExNWYxMDM4MDEx"},
 
 {"createtime":"2015-05-25 17:23:21.0","devicesname":"12345678901","domainid":"Q04BAQsAAGJmMjFjYmQwMDM3YjEx","sid":"12345678901","state":1,"type":4,"userid":"Q04BAQ0AADAzZWExNWYxMDM4MDEx"},{"createtime":"2015-05-25 17:23:21.0","devicesname":"18158113714",
 "domainid":"Q04BAQsAAGJmMjFjYmQwMDM3YjEx","sid":"18158113714","state":1,"type":1,"userid":"Q04BAQ0AADAzZWExNWYxMDM4MDEx"}
+------------+--------------+------+-----+---------+-------+
| sid        | char(32)     | NO   | PRI | NULL    |       |
| devname    | varchar(50)  | YES  |     | NULL    |       |
| createtime | datetime     | YES  |     | NULL    |       |
| state      | int(11)      | YES  |     | NULL    |       |
| domainid   | char(32)     | YES  |     | NULL    |       |
| userid     | char(32)     | YES  |     | NULL    |       |
| type       | int(11)      | YES  |     | NULL    |       |
| pn         | varchar(30)  | YES  |     | NULL    |       |
| vn         | varchar(30)  | YES  |     | NULL    |       |
| sn         | varchar(30)  | YES  |     | NULL    |       |
| model      | varchar(30)  | YES  |     | NULL    |       |
| ver        | varchar(30)  | YES  |     | NULL    |       |
| logo       | varchar(200) | YES  |     | NULL    |       |
| dpassword  | varchar(50)  | YES  |     | NULL    |       |
+------------+--------------+------+-----+---------+-------+
 */
	
	public String sid;
	public String devname;
	public String dpassword;
	public String createtime;
	public String domainid;
	public Integer state;
	public String userid;
	/** 1.IPC 4.模拟IPC 100.直播收藏 101.短片收藏 */
	public Integer type;
	public String pn;
	public String vn;
	public String sn;
	public String model;
	public String ver;
	public String logo;
	public Integer channels;//通道数 
	public Integer online;
	public Integer isowner;
	public Integer channelNo;//通道号 
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getDevname() {
		return devname;
	}
	public void setDevname(String devname) {
		this.devname = devname;
	}
	public String getDpassword() {
		return dpassword;
	}
	public void setDpassword(String dpassword) {
		this.dpassword = dpassword;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}
	public String getDomainid() {
		return domainid;
	}
	public void setDomainid(String domainid) {
		this.domainid = domainid;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getPn() {
		return pn;
	}
	public void setPn(String pn) {
		this.pn = pn;
	}
	public String getVn() {
		return vn;
	}
	public void setVn(String vn) {
		this.vn = vn;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Integer getChannels() {
		return channels;
	}
	public void setChannels(Integer channels) {
		this.channels = channels;
	}
	public Integer getOnline() {
		return online;
	}
	public void setOnline(Integer online) {
		this.online = online;
	}
	public Integer getIsowner() {
		return isowner;
	}
	public void setIsowner(Integer isowner) {
		this.isowner = isowner;
	}
	public Integer getChannelNo() {
		return channelNo;
	}
	public void setChannelNo(Integer channelNo) {
		this.channelNo = channelNo;
	}
	
	
}
