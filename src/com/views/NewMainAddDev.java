package com.views;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.LogUtil;

/**
 * 
 * @author pc
 *
 */
public class NewMainAddDev extends Activity {

	public static String TAG = "NewMainAddDev";
	
	private EditText sn, pn;
	
	private TextView tv_location;
	
	private String location, address;
	
	JSONObject locationJson;
	
	Double longitude, latitude;
	
	Button cancel, submit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_main_add_dev);

		sn = (EditText) findViewById(R.id.new_dev_sn);
		pn = (EditText) findViewById(R.id.new_dev_pn);
		tv_location = (TextView) findViewById(R.id.location_tv);
		
		cancel = (Button) findViewById(R.id.new_main_adddev_cancel);
		submit = (Button) findViewById(R.id.new_main_adddev_submit);
		cancel.setOnClickListener(new Click());
		submit.setOnClickListener(new Click());

		sn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Drawable drawable = sn.getCompoundDrawables()[2];
						if(drawable != null){
							drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
							if(event.getX() > sn.getWidth()-sn.getPaddingRight()-drawable.getIntrinsicWidth()){
								sn.setText("");
							}
						}
				return false;
			}
		});
		pn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Drawable drawable = pn.getCompoundDrawables()[2];
						if(drawable != null){
							drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
							if(event.getX() > pn.getWidth()-pn.getPaddingRight()-drawable.getIntrinsicWidth()){
								pn.setText("");
							}
						}
				return false;
			}
		});
		
		Main.Instance.startLocation();
		registerReceiver(receiver, intentFilter);
	}
	
	class Click implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.new_main_adddev_cancel:
				finish();
				break;
			case R.id.new_main_adddev_submit:
				//addDevices(sn.getText().toString(), pn.getText().toString(), location);
				
				try {
					longitude = locationJson.getDouble("longitude");// 获取经度信息
					latitude = locationJson.getDouble("latitude");// 获取纬度信息
					address = locationJson.getString("address");// 地址
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				addDevice(sn.getText().toString(), pn.getText().toString(), longitude, latitude, address);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public void finish() {
		Log.d(TAG, "finish!......");
		APP.GetMainActivity().ShowXView(Main.XV_NEW_MAIN);
		Main.Instance.NewMainreLoad();
		super.finish();
	} 
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	};
	
	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(this);
			Log.d(TAG, "onReceive：...");
			location = intent.getExtras().getString("location");
			Log.d(TAG, "json:" + location);
			try {
				locationJson = new JSONObject(location);
				address = locationJson.getString("address");
				tv_location.setText(address);
			} catch (JSONException e) {
				LogUtil.d(TAG, "parse location :" + e.getMessage());
			}
		}
	};
	IntentFilter intentFilter = new IntentFilter("com.views.NewMainAddDev");
	
	public void addDevice(String sn, String vn, Double longitude, Double latitude, String address){
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
		params.put("sn", sn);
		params.put("vn", vn);
		params.put("longitude", longitude);
		params.put("latitude", latitude);
		params.put("address", address);
		HttpUtil.get(Constants.hostUrl+"/android/deviceIntoUsers", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d(TAG, "deviceIntoUsers response:" +  response.toString());
				if(statusCode == 200 && response.has("msg")){
					String msg = "";
					msg = getStringByJson(response, "msg");
					if("snNotExist".equals(msg)){
						APP.ShowToast(getString(R.string.dev_noserial));
					}else if("vnError".equals(msg)){
						APP.ShowToast(getString(R.string.register_wrongcode));
					}else if("failure".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addFail));
					}else if("success".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addok));
						finish();
					}else if("repeat".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addrepeat));
						finish();
					}else if("existOtherUser".equals(msg)){
						if(response.has("usmsg")){
							msg = getStringByJson(response, "usmsg");
							if("maximum".equals(msg)){
								APP.ShowToast(getString(R.string.dev_add_maximum));
							}else if("success".equals(msg)){
								APP.ShowToast(getString(R.string.dev_addok));
								finish();
							}else if("failure".equals(msg)){
								APP.ShowToast(getString(R.string.dev_addFail));
							}else {
								
							} 
						}
					}else {
						
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.E_SER_FAIL));
			}
		});
	}
	
	/**
	 * 添加设备
	 * @param sn
	 * @param vn
	 * @param location
	 */
	public void addDevices1(String sn, String vn, String location){
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
		params.put("sn", sn);
		params.put("vn", vn);
		params.put("location", location);
		HttpUtil.get(Constants.hostUrl+"/android/addDevices", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.v(TAG, "response："+response);
				if(statusCode == 200){
					Log.d(TAG, response.toString());
					String msg = "";
					try {
						msg = response.getString("msg");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if("snNotExist".equals(msg)){
						APP.ShowToast(getString(R.string.dev_noserial));
					}else if("existOtherUser".equals(msg)){
						APP.ShowToast(getString(R.string.dev_binded));
					}else if("vnError".equals(msg)){
						APP.ShowToast(getString(R.string.register_wrongcode));
					}else if("failure".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addFail));
					}else if("success".equals(msg)){
						APP.ShowToast(getString(R.string.dev_addok));
						finish();
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.E_SER_FAIL));
			}
		});
	}
	
	private String getStringByJson(JSONObject json, String key){
		String ret = "";
		try {
			ret = json.getString(key);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}
}
