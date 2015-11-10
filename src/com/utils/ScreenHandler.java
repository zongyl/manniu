package com.utils;

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
import com.basic.APP;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.views.Main;

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
	
	private void display(String url){
		try {
			ImageView iv = ScreenCache.getInstance().getImgView(deviceId);
			fileName = rootPath + deviceId + ".png";
			
			HttpUtil.get(url, new BinaryHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
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
	
	/**
	 * 发送封面到服务器
	 */
	private void sendScreen(String deviceId, String ossUrl){
		RequestParams params = new RequestParams();
		params.put("sid", deviceId);
		params.put("screenUrl", ossUrl);
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
