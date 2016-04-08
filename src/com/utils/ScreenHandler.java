package com.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import com.adapter.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.basic.APP;
import com.bean.Device;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.views.Main;
import com.views.NewMain;

public class ScreenHandler extends Handler{

	public static final String TAG = "ScreenHandler";
	
	private String deviceId, type;
	//图片缓存目录 
	private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/manniu/devices/";
	
	private String fileName;
	
	@Override
	public void handleMessage(Message msg) {
		try {
			Log.d(TAG, "msg:"+msg);
			Bundle bundle = msg.getData();
			deviceId = bundle.getString("deviceId");
			type = bundle.getString("type");
			switch (bundle.getInt("result")) {
			case 0:
				sendScreen(deviceId, bundle.getString("url"));
				if(type == null){
					show(bundle.getString("url"));
				}
				break;
			case 2:
				//APP.ShowToast("设备不在线");
				break;
			case 5:
				APP.ShowToast("P2P操作失败!");
				break;
			case 6:
				APP.ShowToast("IO操作失败!");
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		} catch (Exception e) {
		}
	}
	
	private void display(final String url){
		try {
			ImageView iv = ScreenCache.getInstance().getImgView(deviceId);
			//fileName = rootPath + deviceId + ".png";
			HttpUtil.get(url, new BinaryHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					String name = url.substring(0,url.indexOf("?"));
					//更新缓存对应的封面(退出清空了缓存 所以这里就不需要更新缓存里面的字段了，如果设备状态能通知过来 这段代码要启用)
//					updateCache(deviceId,name);
					name = name.substring(name.indexOf("aliyuncs.com")+12, name.length());
					fileName = rootPath + deviceId + name;
					//先清空之前的文件
    				File baseFile = new File(fileName.substring(0, fileName.lastIndexOf("/")));
    				if (baseFile != null && baseFile.exists()) {
    					File[] f = baseFile.listFiles();
    					if(f != null){
    						for(int i = f.length - 1; i > -1; --i){
    							if (f[i].isFile() && f[i].length() > 0) {
    								f[i].delete();
    							}
    						}
    					}
    				}
    				//fileName = path;
					//原来的fileName = /storage/emulated/0/manniu/devices/VFMhAQEAAGUwNjFiMjAxMGJmOAAA.png
    				//现改为跟缓存目录一至:/storage/emulated/0/manniu/devices/VFMhAQEAAGUwNjFiMjAxMGJmOAAA/bc565120/cam_0/20160314172429_e3519e7e.jpg
    				//解决刷新(刷新封面，提示成功，但双击返回键退出app，再重新打开，封面没有刷新) 如果别的地方用的这个路径改过留意一下
    				FileUtil.toFile(arg2, fileName);
				}
				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				}
			});
			ImageLoader.getInstance().displayImage(url, iv);
		} catch (Exception e) {
		}
	}
	
	public void updateCache(String sid,String logo){
		List<Device> _devList = new ArrayList<Device>();
		String json = NewMain.instance.cache.getAsString(Constants.userid + "_devices");
		if(!json.equals("{}")){
			JSONArray array = JSON.parseArray(json);
			for(int i = 0; i < array.size(); i++){
				Device dev1 = JSON.toJavaObject((JSON)array.get(i), Device.class);
				if(sid.equals(dev1.sid)){
					dev1.logo = logo;
				}
				_devList.add(dev1);
			}
			NewMain.instance.cache.put(Constants.userid + "_devices", JsonString.getDeviceJsonString(_devList));
		}
	}
	
	
	/**
	 * 发送封面到服务器
	 */
	private void sendScreen(String deviceId, String ossUrl){
		try {
			if(ossUrl.equals("")) return;
			int last = ossUrl.indexOf("/cam");
			String str = ossUrl.substring(last+1, ossUrl.length());
			int t = str.lastIndexOf("/");
			//System.out.println(str.substring(0,t).split("_")[1]);
			RequestParams params = new RequestParams();
			params.put("sid", deviceId);
			params.put("screenUrl", ossUrl);
			params.put("channel", Integer.parseInt(str.substring(0,t).split("_")[1]));
			Log.d(TAG, "sendScreen.params:" + params.toString());		
			HttpUtil.post(Constants.hostUrl + "/device/saveScreen", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					Log.d(TAG, "response:" + response.toString());
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					Log.d(TAG,":"+ statusCode);
					if(statusCode!=200)
					{
						APP.ShowToast(APP.GetString(R.string.Err_CONNET));
					}
				}
			});
		} catch (Exception e) {
		}
	}
	
	/**
	 * 转换成授权的图片地址
	 * @param url
	 */
	private void show(String url){
		RequestParams params = new RequestParams();
		params.put("ossUrl", url);
		Log.d(TAG, "params:" + params.toString());
		Log.d("server_address", Main.Instance.getResources().getString(R.string.server_address));
		HttpUtil.get(Main.Instance.getResources().getString(R.string.server_address) + "/android/getUrl", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d(TAG, "onSuccess-response:" + response.toString());
				try {
					display(response.getString("url"));
				} catch (JSONException e) {
					LogUtil.e(TAG, "parse JSON:" + e.getMessage());
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
			}
		});
	}
}
