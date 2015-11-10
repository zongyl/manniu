package com.views;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	
	Button btn_update;
	
	Spinner /*show_position,*/ resolution, quality;
	
	ImageView switch1, switch2;
	
	ArrayAdapter<CharSequence> adapter;
	
	int[] pixels;
	
	public static final String FILE = "_device_set";

	private static final String SW1_KEY = "dynamic-detection";
	private static final String SW2_KEY = "cloud-storage";
	
	String devicesid;
	
    public static String[] STR_PICTURE_TYPE = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_device_set);

		pixels = getSize();
		
		device = getIntent().getParcelableExtra("device");
		
		tv_dev_name = (TextView) findViewById(R.id.dev_name);
		tv_dev_name.setText(device.devname); 

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
		quality = (Spinner) findViewById(R.id.quality);

		adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetResolution));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		resolution.setAdapter(adapter);
		resolution.setOnItemSelectedListener(new SelectedListener());
		resolution.setSelection(0);
		adapter.notifyDataSetChanged();

		adapter = new ArrayAdapter<CharSequence>(this, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetQuality));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		quality.setAdapter(adapter);
		quality.setOnItemSelectedListener(new SelectedListener());
		quality.setSelection(0);
		adapter.notifyDataSetChanged();
		
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
		
		getVersion(device.sid);
		
		//getFragmentView(0);
		
		devicesid = "Q04hAQEAbDAwMDEwYmMzAAAAAAAA";
		
		String str1 = getSetting(devicesid);//device.sid 
		Log.d(TAG, "device sets:"+str1);
		SDK.SendJsonPck(0, str1);
		
		readSetInfos();
		
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
	
	private String getSets(){
		SharedPreferences pre = APP.GetMainActivity().getSharedPreferences(MD5Util.MD5(device.sid) + FILE, APP.GetMainActivity().MODE_PRIVATE);
		return JSON.toJSONString(pre.getAll());
	}
	
	private void close(){
		this.finish();
		String str = getSets();
		Log.d(TAG, "device sets:"+str);
		Main.Instance.NewMainreLoad();
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
	
	public void readSetInfos(){
		SharedPreferences pre = APP.GetMainActivity().getSharedPreferences(MD5Util.MD5(devicesid) + FILE, APP.GetMainActivity().MODE_PRIVATE);
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
		}
		//setSwitch(switch1);
		//setSwitch(switch2);
	}
	public void setSwitch(View v){
		String value = v.getTag().toString();
		if("on".equals(value)){
			v.setBackgroundResource(R.drawable.my_switch_on);
		}else{
			v.setBackgroundResource(R.drawable.my_switch_off);
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
		SetSharePrefer.write(MD5Util.MD5(device.sid) + FILE, key, iv_switch.getTag().toString());
	}
	
	/**
	 * 为Fragment提供的 
	 * @return
	 */
	public String getDeviceId(){
		return devicesid;
	}
	
	class SelectedListener implements OnItemSelectedListener{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
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
			case R.id.device_set_network:
				Bundle data = new Bundle();
				data.putString("deviceId", devicesid);
				forward(NewDeviceSetNetWork.class, data, 1);
				break;
			case R.id.dev_set_item_switch1:
				switchTag(v, "default");
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
			LogUtil.d(TAG, "接收到广播!");
		}
	};
	
	
	public class CFHandler extends Handler{
		
	}
	
	public Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {};
	};
	
}