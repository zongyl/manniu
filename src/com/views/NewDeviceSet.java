package com.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.adapter.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.basic.APP;
import com.bean.DeviceParcel;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.MD5Util;
import com.utils.SetSharePrefer;

/**
 * 
 * @author pc
 * 设备设置
 */
@SuppressLint({ "ResourceAsColor", "NewApi" })
public class NewDeviceSet extends Activity {

	public static final String TAG = "NewDeviceSet";
	
	DeviceParcel device;
	
	TextView tv_dev_name, dev_item_version/*, dev_set_base, dev_set_advanced*/;
	
	EditText et_dev_name;
	
	Button btn_update ;
	
	Spinner /*show_position,*/ resolution, /*quality*/frameRate, bitStream;
	
	ImageView switch1, switch2;
	
	ArrayAdapter<CharSequence> adapter;
	
	int[] pixels;
	
	public static final String FILE = "_device_set";

	private static final String SW1_KEY = "dynamic-detection";
	private static final String SW2_KEY = "cloud-storage";
	
	private static NewDeviceSet instance;
	
	public static NewDeviceSet getInstance(){
		if(instance==null){
			instance = new NewDeviceSet();
		}
		return instance;
	}
	
	//String devicesid;
	
    public static String[] STR_PICTURE_TYPE = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_device_set);

		pixels = getSize();
		
		device = getIntent().getParcelableExtra("device");
		
//		tv_dev_name = (TextView) findViewById(R.id.dev_name);
//		tv_dev_name.setText(device.devname); 

		et_dev_name = (EditText) findViewById(R.id.dev_name_et);
		et_dev_name.setText(device.devname);
		
		switch1 = (ImageView) findViewById(R.id.dev_set_item_switch1);
		switch2 = (ImageView) findViewById(R.id.dev_set_item_switch2);
		
//		show_position = (Spinner) findViewById(R.id.show_position);
//		show_position.setDropDownWidth(pixels[0]/2);
//		STR_PICTURE_TYPE = getResources().getStringArray(R.array.devSetPosition);
//		
//		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item, STR_PICTURE_TYPE);
//		adapter.setDropDownViewResource(R.layout.spinner_checked_text);//simple_spinner_item
//		show_position.setAdapter(adapter);
//		show_position.setOnItemSelectedListener(new SelectedListener());
//		show_position.setSelection(0);
//		adapter.notifyDataSetChanged();
		
		resolution = (Spinner) findViewById(R.id.resolution);
		//quality = (Spinner) findViewById(R.id.quality);
		frameRate = (Spinner) findViewById(R.id.frameRate);
		bitStream = (Spinner) findViewById(R.id.bitStream);

		adapter(resolution, R.array.devSetResolution);
		adapter(frameRate, R.array.devSetFrameRate);
		adapter(bitStream, R.array.devSetBitStream);
		
		/*adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetResolution));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		resolution.setAdapter(adapter);
		resolution.setOnItemSelectedListener(new SelectedListener());
		resolution.setSelection(0);
		adapter.notifyDataSetChanged();

		adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetFrameRate));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		frameRate.setAdapter(adapter);
		frameRate.setOnItemSelectedListener(new SelectedListener());
		frameRate.setSelection(0);
		adapter.notifyDataSetChanged();

		adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetBitStream));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		bitStream.setAdapter(adapter);
		bitStream.setOnItemSelectedListener(new SelectedListener());
		bitStream.setSelection(0);
		adapter.notifyDataSetChanged();*/
		
		dev_item_version = (TextView) findViewById(R.id.dev_item_ver_version);
		//dev_item_upver = (TextView) findViewById(R.id.dev_item_ver_upver);
		btn_update = (Button) findViewById(R.id.dev_item_update);
//		dev_set_base = (TextView) findViewById(R.id.dev_set_base);
//		dev_set_advanced = (TextView) findViewById(R.id.dev_set_advanced);
		
//		dev_set_base.setWidth(pixels[0]/2);
//		dev_set_advanced.setWidth(pixels[0]/2);
//		
//		dev_set_base.setOnClickListener(new Click());
//		dev_set_advanced.setOnClickListener(new Click());
		btn_update.setOnClickListener(new Click());

		switch1.setOnClickListener(new Click());
		switch2.setOnClickListener(new Click());
		
		//设备信息
		findViewById(R.id.dev_info).setOnClickListener(new Click());
		//返回按钮
		findViewById(R.id.dev_set_back).setOnClickListener(new Click());
		findViewById(R.id.dev_set_save).setOnClickListener(new Click());
		findViewById(R.id.dev_set_btn_sub).setOnClickListener(new Click());
		findViewById(R.id.dev_set_btn_cancel).setOnClickListener(new Click());
		
		getVersion(device.sid);
		
		//getFragmentView(0);
		
		//Q04hAQEAbDAwMTIzN2E4AAAAAAAA ...37a8
		//Q04hAQEAbDAwMDEwYmMzAAAAAAAA manniu202
		//Q04hAQEAbDAwMDEwYmY4AAAAAAAA manniu203
		//devicesid = "Q04hAQEAbDAwMTIzN2E4AAAAAAAA";
		
		registerReceiver(receiver, intentFilter);
		
		String str1 = getSetting(device.sid);//device.sid 
		Log.d(TAG, "device sets:"+str1);
		SDK.SendJsonPck(0, str1);
		//readSetInfos();
		findViewById(R.id.device_set_network).setOnClickListener(new Click());
	}
	
	/**
	 * 以数组形式返回 尺寸 [0] = width, [1] = height
	 * @return
	 */
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
	
	private void close(){
		this.finish();
		//Main.Instance.NewMainreLoad();
	}

	private void save(){
		if(!device.devname.equals(et_dev_name.getText().toString())){
			updateDevName();
		} 
		writeInfo("method", 0);
		writeInfo("type", 1);
		set(0, "overlay_text", et_dev_name.getText().toString());
		
		String sets = getSets();
		LogUtil.d(TAG, "save config json:"+sets);
		send(sets);
	}
	
	//获取配置 发送请求的json
	private String getSetting(String sid){
		return sid + "|{\"type\":1,\"action\":101,\"sid\":\""+sid+"\",\"method\":1}";
	}
	
	private void forward(Class clazz, Bundle extras, int requestCode){
		Intent intent = new Intent(this, clazz);
		if(extras != null){
			intent.putExtras(extras);
		}
		startActivityForResult(intent, requestCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//目前只有修改设备名称的时候用到  所以没有判断requestCode、 resultCode
		Log.d(TAG, "requestCode:" + requestCode + " resultCode:"+resultCode);
		if(resultCode == 1){
			Bundle bundle = data.getExtras();
			tv_dev_name.setText(bundle.getString("deviceName"));
			device.devname = bundle.getString("deviceName");
		}
	}
	
	private String getServerAddress(){
		return Constants.hostUrl;
	}
	
	private String paramStr;
	
	/**
	 * 将设置信息发送到web
	 */
	private void send(String configJson){
		RequestParams params = new RequestParams();
		
		com.alibaba.fastjson.JSONObject obj = JSON.parseObject(configJson);
		
		//printJson(obj);
		paramStr = cvtJson(obj);
		params.put("configJson", device.sid + "|" + paramStr);// configJson
		LogUtil.d(TAG, "set Confiog params:" + params.toString());
		HttpUtil.get(getServerAddress()+"/device/saveDesConfig", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				LogUtil.d(TAG, "config setting response:"+response.toString());
				//APP.ShowToast(getString(R.string.dev_upgradeOk));
				try {
					if("true".equals(response.getString("return"))){
						LogUtil.d(TAG, "QWE:"+paramStr);
						Map<String, Object> maps = new HashMap<String, Object>();
						maps.put("type", 1);
						maps.put("method", 0);
						maps.put("sid", device.sid);
						maps.put("action", 101);
						
						String str = new JSONObject(maps).toString();
						//JSON.parseObject(maps.toString()).toJSONString()
						LogUtil.d(TAG, "QWE:"+str);
						SDK.SendJsonPck(0, device.sid + "|" + str);
						close();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}

	private void updateDevName(){
		RequestParams params = new RequestParams();
		///dev_name = et_dev_name.getText().toString();
		params.put("sid", device.sid);
		params.put("devicesname", et_dev_name.getText().toString().trim());
		HttpUtil.get(getServerAddress()+"/android/updateDevName", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d("update Name：", "statusCode："+statusCode);
				if(statusCode == 200){
					Log.d("update Name：", response.toString());
//					Intent data = new Intent();
//					data.putExtra("deviceName", dev_name);
//					setResult(1, data);
//					finish();
//					APP.ShowToast(getString(R.string.SUCCESS_MODIFY));
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				Log.d("", "" + responseString);
			}
		});
	}

	/**
	 * 
	 * @param jsonData
	 * @return
	 */
	private String cvtJson(com.alibaba.fastjson.JSONObject jsonData){
		Set<String> keys = jsonData.keySet();
		Map<String, Object> map = new HashMap<String, Object>();
		for(String key : keys){
			if("cam_conf".equals(key)){
				map.put(key, JSON.parseArray(jsonData.get(key).toString()));
			}else{
				map.put(key, jsonData.get(key));
			}
		}
		return JSON.toJSONString(map);
	}
	
	private void printJson(com.alibaba.fastjson.JSONObject jsonData){
		Set<String> keys = jsonData.keySet();
		for(String key : keys){
			LogUtil.d(TAG, "key:" + key);
			if(jsonData.get(key) instanceof JSONObject){
				LogUtil.d( TAG, key + " is JSONObject instance!");
			}else if(jsonData.get(key) instanceof JSONArray){
				LogUtil.d( TAG, key + " is JSONArray instance!");
			}else{
				LogUtil.d( TAG, key + " value is other instance! value " + jsonData.get(key));
			} 
		}
	}
	
	/**
	 * IPC升级
	 */
	private void update(String deviceId){
		RequestParams params = new RequestParams();
		params.put("sid", deviceId);
		HttpUtil.get(getServerAddress()+"/device/update", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				APP.ShowToast(getString(R.string.dev_upgradeOk));
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}
	
	/**
	 * 获取版本
	 */
	private void getVersion(String deviceId){
		RequestParams params = new RequestParams();
		params.put("sid", deviceId);
		HttpUtil.get(getServerAddress()+"/device/getVersion", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d(TAG, "response:" + response);
				JSONObject jsonObj;
				try {
					jsonObj = response.getJSONObject("result");
					if(jsonObj.has("ver")){
						if("notonline".equals(jsonObj.getString("ver"))){
							//不在线
							dev_item_version.setText(getString(R.string.dev_offline));
						}else if("notupver".equals(jsonObj.getString("ver"))){
							//没有可升级的版本
							dev_item_version.setText("");
						}else if("error".equals(jsonObj.getString("ver"))){
							//错误
							dev_item_version.setText(getString(R.string.Err_Error));
						}else{
							dev_item_version.setText(jsonObj.getString("ver"));
						}
					}
					if(jsonObj.has("upver")){
						if("notonline".equals(jsonObj.getString("upver"))){
							//不在线
							//dev_item_upver.setText(getString(R.string.dev_offline));
						}else if("notupver".equals(jsonObj.getString("upver"))){
							//没有可升级的版本
							//dev_item_upver.setText("");
						}else if("error".equals(jsonObj.getString("upver"))){
							//错误
							//dev_item_upver.setText(getString(R.string.Err_Error));
						}else{
							//升级按钮 
						//	dev_item_upver.setText(jsonObj.getString("upver"));
							btn_update.setText(jsonObj.getString("upver"));
							btn_update.setVisibility(View.VISIBLE);
						}
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				Log.d(TAG, "failure~~");
			}
		}); 
	}
	
	private String getSets(){
		SharedPreferences pre = APP.GetMainActivity().getSharedPreferences(MD5Util.MD5(device.sid) + FILE, APP.GetMainActivity().MODE_PRIVATE);
		return JSON.toJSONString(pre.getAll());
	}
	
	/**
	 * 设置属性 
	 * @param cannelNo 通道号  
	 * @param key  属性
	 * @param value 值
	 */
	private void set(int channelNo, String key, Object value){
		String cam_conf = readInfo("cam_conf");
		com.alibaba.fastjson.JSONArray array = JSON.parseArray(cam_conf);
		com.alibaba.fastjson.JSONObject obj = array.getJSONObject(channelNo);
		obj.put(key, value);
		//array.remove(channelNo);
		array.set(channelNo, obj);
		SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, "cam_conf", array.toJSONString());
	}
	
	/**
	 * 修改属性值
	 * @param key
	 * @param value
	 */
	private void writeInfo(String key, String value){
		SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, key, value);
	}
	
	private void writeInfo(String key, Object value){
		SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, key, value);
	}
	
	private String readInfo(String key){
		SharedPreferences pre = APP.GetMainActivity().getSharedPreferences(MD5Util.MD5(device.sid) + FILE, APP.GetMainActivity().MODE_PRIVATE);
		return pre.getString(key, "");
	}
	
	public void readSetInfos(){
		LogUtil.d(TAG, "readSetInfos!");
		SharedPreferences pre = APP.GetMainActivity().getSharedPreferences(MD5Util.MD5(device.sid) + FILE, APP.GetMainActivity().MODE_PRIVATE);
		if(pre!=null){
			//switch1.setTag(pre.getString(SW1_KEY, ""));//动检
			//switch2.setTag(pre.getString(SW2_KEY, ""));//遮挡
			
			String cam_conf = pre.getString("cam_conf", "");
			JSONArray sets = JSON.parseArray(cam_conf);
			com.alibaba.fastjson.JSONObject set;
			if((sets !=null) && sets.size() > 0){
				for(int i = 0;i < sets.size();i++){
					set = sets.getJSONObject(i);
					LogUtil.d(TAG, "toJSONString:" + set.toJSONString());
					LogUtil.d(TAG, "toString:" + set.toString());
				}
			}
			
			set = sets.getJSONObject(0);

			String bps,fps,width;
			bps = set.getString("bps");
			fps = set.getString("fps");
			width = set.getString("width");
			
			if("1".equals(fps)){
				frameRate.setSelection(0);
			}else if("5".equals(fps)){
				frameRate.setSelection(1);
			}else if("10".equals(fps)){
				frameRate.setSelection(2);
			}else if("15".equals(fps)){
				frameRate.setSelection(3);
			}else {
			}
			
			if("352".equals(width)){
				resolution.setSelection(0);
				if("48".equals(bps)){
					bitStream.setSelection(0);
				}else if("96".equals(bps)){
					bitStream.setSelection(1);
				}else if("160".equals(bps)){
					bitStream.setSelection(2);
				}else if("224".equals(bps)){
					bitStream.setSelection(3);
				}else{
					bitStream.setSelection(0);
				}
			}else if("704".equals(width)){
				resolution.setSelection(1);
				if("128".equals(bps)){
					bitStream.setSelection(0);
				}else if("256".equals(bps)){
					bitStream.setSelection(1);
				}else if("384".equals(bps)){
					bitStream.setSelection(2);
				}else if("512".equals(bps)){
					bitStream.setSelection(3);
				}else if("640".equals(bps)){
					bitStream.setSelection(4);
				}else{
					bitStream.setSelection(0);
				} 
			}else if("1280".equals(width)){
				resolution.setSelection(2);
				if("224".equals(bps)){
					bitStream.setSelection(0);
				}else if("384".equals(bps)){
					bitStream.setSelection(1);
				}else if("640".equals(bps)){
					bitStream.setSelection(2);
				}else if("768".equals(bps)){
					bitStream.setSelection(3);
				}else if("896".equals(bps)){
					bitStream.setSelection(4);
				}else if("1280".equals(bps)){
					bitStream.setSelection(5);
				}else{
					bitStream.setSelection(0);
				}
			}else{ 
			} 
			
			if("0".equals(set.getString("alert_type"))){
				LogUtil.d(TAG, "报警未开启!");
				switchTag(switch1, "alert_type");
			}
		}else{
			LogUtil.d(TAG, "readSetInfos perperences is null!");
		}
	}
	
	public void switchTag(View iv_switch, String key){
		if("on".equals(iv_switch.getTag())){
			iv_switch.setTag("off");
			iv_switch.setBackgroundResource(R.drawable.my_switch_off);
			write(iv_switch, key);
		}else{
		iv_switch.setTag("on");
		iv_switch.setBackgroundResource(R.drawable.my_switch_on);
		write(iv_switch, key);
		}
	}
	public void write(View iv_switch, String key){
		if("alert_type".equals(key)){
			if("on".equals(iv_switch.getTag().toString())){
				String cam_conf = readInfo("cam_conf");
				com.alibaba.fastjson.JSONArray array = JSON.parseArray(cam_conf);
				com.alibaba.fastjson.JSONObject obj = array.getJSONObject(0);
				obj.put("alert_type", 9);
				array.set(0, obj);
				SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, "cam_conf", array.toJSONString());
			}else if("off".equals(iv_switch.getTag().toString())){
				String cam_conf = readInfo("cam_conf");
				com.alibaba.fastjson.JSONArray array = JSON.parseArray(cam_conf);
				com.alibaba.fastjson.JSONObject obj = array.getJSONObject(0);
				obj.put("alert_type", 0);
				array.set(0, obj);
				SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, "cam_conf", array.toJSONString());
			}
		}
		
		SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, key, iv_switch.getTag().toString());
	}
	
	/**
	 * 为Fragment提供的 
	 * @return
	 */
	public String getDeviceId(){
		return device.sid;
	}
	
	private void adapter(Spinner spinner, int resId){
		adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item,
				getResources().getStringArray(resId));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SelectedListener());
		spinner.setSelection(0);
		adapter.notifyDataSetChanged();
	}
	
	class SelectedListener implements OnItemSelectedListener{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			
			switch (parent.getId()) {
			case R.id.resolution:
				switch (position) {
				case 0:
					adapter(bitStream, R.array.devSetBitStream);
					if(view != null){
						set(0, "width", 325);
						set(0, "height", 288);
					}
					break;
				case 1:
					adapter(bitStream, R.array.devSetBitStream1);
					if(view != null){
						set(0, "width", 704);
						set(0, "height", 576);
					}
					break;
				case 2:
					adapter(bitStream, R.array.devSetBitStream2);
					if(view != null){
						set(0, "width", 1280);
						set(0, "height", 720);
					}
					break;
				default:
					break;
				}
				break;
			case R.id.frameRate:
				LogUtil.d(TAG, "...frameRate...");
				if(view != null){
					switch (position) {
					case 0:
						set(0, "fps", 1);
						break;
					case 1:
						set(0, "fps", 5);
						break;
					case 2:
						set(0, "fps", 10);
						break;
					case 3:
						set(0, "fps", 15);
						break;
					default:
						break;
					}
				}
				break;
			case R.id.bitStream:
				LogUtil.d(TAG, "...bitStream...");
				if(view != null){
					String bps = bitStream.getSelectedItem().toString();
					LogUtil.d(TAG, "当前码流:" + bps);
					set(0, "bps", Integer.parseInt(bps.substring(0, bps.indexOf(" kbps")).trim()));
				}
				break;
			default:
				break;
			}

			if(view == null){
				Log.d(TAG, "view is null!");
			}else{
				Log.d(TAG, "viewId:" + view.getId());
			}
			Log.d(TAG, "position:"+position+" id:"+id);
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}
	
	@SuppressLint("NewApi")
	public void getFragmentView(int type){
		try {
			if(type == 0){
				android.app.FragmentManager fragmentManager = this.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	DevSetBaseFragment fragment = new DevSetBaseFragment(); 
	        	frTransaction.replace(R.id.ui_container, fragment);
	        	frTransaction.commitAllowingStateLoss();
			}else{
				android.app.FragmentManager fragmentManager = this.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	DevSetAdvancedFragment fragment = new DevSetAdvancedFragment(); 
	        	frTransaction.replace(R.id.ui_container, fragment);
	        	frTransaction.commitAllowingStateLoss();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	/**
	 * 点击事件
	 * @author pc
	 *
	 */
	class Click implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dev_item_update:
				update(device.sid);
				break;
//			case R.id.dev_set_base: //base setting and advanced setting tab  
//				getFragmentView(0);
//				dev_set_base.setTextColor(getResources().getColor(R.color.blue_menu));
//				dev_set_advanced.setTextColor(getResources().getColor(R.color.black));
//				break;
//			case R.id.dev_set_advanced:
//				getFragmentView(1);
//				dev_set_base.setTextColor(getResources().getColor(R.color.black));
//				dev_set_advanced.setTextColor(getResources().getColor(R.color.blue_menu));
//				break;
			case R.id.dev_info://设备改名不能点进去
				/*Bundle extras = new Bundle();
				extras.putString("devicesname", device.devname);
				extras.putString("sid", device.sid);
				forward(NewDeviceSetInfo.class, extras, 1);*/
				break;
			case R.id.dev_set_back:
				close();
				break;
			case R.id.dev_set_save:
				save();
				break;
			case R.id.dev_set_btn_cancel:
				close();
				break;
			case R.id.dev_set_btn_sub:
				save();
				break;
			case R.id.device_set_network:
				Bundle data = new Bundle();
				data.putString("deviceId", device.sid);
				forward(NewDeviceSetNetWork.class, data, 1);
				break;
			case R.id.dev_set_item_switch1:
				switchTag(v, "alert_type");
				break;
			case R.id.dev_set_item_switch2:
				switchTag(v, "default");
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 广播接收器
	 */
	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			unregisterReceiver(this);
			LogUtil.d(TAG, "接收到广播!");
			readSetInfos();
		}
	};
	
	IntentFilter intentFilter = new IntentFilter("com.views.NewDeviceSet");
	
}