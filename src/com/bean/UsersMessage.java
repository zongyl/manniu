package com.bean;



/**
 * @用户真实信息
 * @author Administrator
 */

public class UsersMessage {
	
	private String uid;//用户ID
	private String uname;//用户真实姓名
	private String address;//用户住址
	private String idcard;//身份证
	private int sex;//性别
	private String fcardimg;//正面身份证
	private String bcardimg;//背面
	private int state;
	
	public String getUid() {
	return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUname() {
		return uname;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getIdcard() {
		return idcard;
	}
	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}
	
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	
	public String getFcardimg() {
		return fcardimg;
	}
	public void setFcardimg(String fcardimg) {
		this.fcardimg = fcardimg;
	}
	
	public String getBcardimg() {
		return bcardimg;
	}
	public void setBcardimg(String bcardimg) {
		this.bcardimg = bcardimg;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
}
