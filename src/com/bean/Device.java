package com.bean;


public class Device {

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
	
}