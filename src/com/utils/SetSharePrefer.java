package com.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.basic.APP;

public class SetSharePrefer {

	public static void  write(String file,String key, Object value){
		@SuppressWarnings("static-access")
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(file, APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		if("java.lang.String".equals(value.getClass().getName())){
			editor.putString(key, value.toString());
		}
		if("java.lang.Integer".equals(value.getClass().getName())){
			editor.putInt(key, (Integer)value);
		}
		editor.commit();
	}
	public static void  write(String file,String param, String value){
		@SuppressWarnings("static-access")
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(file, APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(param,value);
		editor.commit();
	}
	
	@SuppressWarnings("static-access")
	public static String read(String file, String key, String defValue){
		String tem = "";
		try {
			SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(file, APP.GetMainActivity().MODE_PRIVATE);
			tem = preferences.getString(key, defValue);
		} catch (Exception e) {
		}
		return tem;
	}
	
	public static void write_bool(String file, String param,  boolean value)
	{
		@SuppressWarnings("static-access")
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(file, APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putBoolean(param,value);
		editor.commit();
	}
	
	public static boolean read_bool(String file, String param)
	{
		@SuppressWarnings("static-access")
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(file, APP.GetMainActivity().MODE_PRIVATE);
		boolean bvalue = preferences.getBoolean(param, false);
		return bvalue;
	}
}