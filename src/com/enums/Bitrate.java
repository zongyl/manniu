package com.enums;
//画质  bitrate
public enum Bitrate {
	BIT100(100,"100"),
	ALARMHOST(2,"报警主机"),
	IDEVICE(3,"智能设备"),
	DOORCONTROL(4,"门禁控制器"),
	ENVIRONMENT(5,"环境量设备"),
	INTELLIGENTALARMTYPE(6,"智能报警类型"),
	STATUSDEVICE(7,"有状态设备"),
	TNG_WIRE(101,"无线网关"),
	TNG_WIRED(102,"有线网关"),
	NODE(201,"节点");
	
	private int _type;
	private String _desc;
	/**
	* <p>Title: WSResponseCode</p>
	* <p>Description: 私有构造函数</p>
	* @param type  	int  类型值
	* @param desc  String 说明
	*/
	private Bitrate(int type,String desc){
		_type = type;
		_desc = desc;
	}
	public int get_type() {
		return _type;
	}
	public String get_desc() {
		return _desc;
	}
}