package com.utils;

import java.util.HashMap;
import java.util.Map;

import android.widget.ImageView;

public class ScreenCache {

	private Map<String, String> maps;
	private Map<String, ImageView> ivMaps;
	
	private static ScreenCache screenCache;
	
	public ScreenCache(){
		maps = new HashMap<String, String>();
		ivMaps = new HashMap<String, ImageView>();
	}
	
	public static ScreenCache getInstance(){
		if(screenCache == null){
			screenCache = new ScreenCache();
		}
		return screenCache;
	}
	
	public void addScreen(String deviceId, String file_url){
		maps.put(deviceId, file_url);
	}
	
	public void addImgView(String deviceId, ImageView iv){
		ivMaps.put(deviceId, iv);
	}
	
	public String getScreen(String deviceId){
		return maps.get(deviceId);
	}
	
	public ImageView getImgView(String deviceId){
		return ivMaps.get(deviceId);
	}
}
