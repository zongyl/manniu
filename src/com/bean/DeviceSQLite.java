package com.bean;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.utils.SQLiteHelper;

public class DeviceSQLite {

	private SQLiteHelper sqlite;
	
	private SQLiteDatabase db;
	
	public DeviceSQLite(Context context){
		sqlite = new SQLiteHelper(context, "manniu.db", null, 1);
	}
	
	public void createTable(){
		db = sqlite.getWritableDatabase();
		db.execSQL("create table if not exists devices(id integer primary key autoincrement, " +
				"sid varchar, " +
				"devicesname varchar, " +
				"dpassword varchar, " +
				"createtime varchar, " +
				"domainid varchar, " +
				"state smallint, " +
				"userid varchar, " +
				"type smallint, " +
				"pn varchar, " +
				"vn varchar, " +
				"sn varchar, " +
				"model varchar, " +
				"ver varchar, " +
				"logo varchar, " +
				"line smallint)");
	}
	
	public void insert(Device device){
		ContentValues values = new ContentValues();
		values.put("devicesname", device.devname);
		values.put("sid", device.sid);
		values.put("dpassword", device.dpassword);
		values.put("createtime", device.createtime);
		values.put("domainid", device.domainid);
		values.put("state", device.state);
		values.put("userid", device.userid);
		values.put("type", device.type);
		values.put("pn", device.pn);
		values.put("vn", device.vn);
		values.put("sn", device.sn);
		values.put("model", device.model);
		values.put("ver", device.ver);
		values.put("logo", device.logo);
		values.put("line", device.online);
		sqlite.insert("devices", values);
	}
	
	public void close(){
		sqlite.close();
	}
}
