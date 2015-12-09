package com.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

/**
 * 设备设置  网络设置
 * @author zongyl
 *
 */
public class NewDeviceSetNetWork extends Activity{

	public static final String TAG = "NewDeviceSetNetWork";
	
	Button submit, cancel;
	
	TextView dev_set_net_base, dev_set_net_wifi;
	
	String deviceId;
	
	int[] pixels;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_device_set_network);

		deviceId = getIntent().getStringExtra("deviceId");
		
		pixels = getSize();
		cancel = (Button) findViewById(R.id.dev_set_network_cancel);
		submit = (Button) findViewById(R.id.dev_set_network_submit);

		dev_set_net_base = (TextView) findViewById(R.id.dev_set_net_base);
		dev_set_net_wifi = (TextView) findViewById(R.id.dev_set_net_wifi);

		dev_set_net_base.setWidth(pixels[0]/2);
		dev_set_net_wifi.setWidth(pixels[0]/2);
		
		cancel.setOnClickListener(click);
		submit.setOnClickListener(click);
		dev_set_net_base.setOnClickListener(click);
		dev_set_net_wifi.setOnClickListener(click);
		
		getFragmentView(0);
		
	}
	
	@Override
	public void finish() {
		super.finish();
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
	
	@SuppressLint("NewApi")
	public void getFragmentView(int type){
		try {
			if(type == 0){
				android.app.FragmentManager fragmentManager = this.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	DevSetNetWorkBaseFragment fragment = new DevSetNetWorkBaseFragment();
	        	Bundle data = new Bundle();
	        	data.putString("deviceId", deviceId);
	        	fragment.setArguments(data);
	        	frTransaction.replace(R.id.ui_container, fragment);
	        	frTransaction.commitAllowingStateLoss();
			}else{
				android.app.FragmentManager fragmentManager = this.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	DevSetNetWorkWifiFragment fragment = new DevSetNetWorkWifiFragment(); 
	        	Bundle data = new Bundle();
	        	data.putString("deviceId", deviceId);
	        	fragment.setArguments(data);
	        	frTransaction.replace(R.id.ui_container, fragment);
	        	frTransaction.commitAllowingStateLoss();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	private void save(){
		Fragment fragment = getFragmentManager().findFragmentById(R.id.ui_container);
		if(fragment instanceof DevSetNetWorkBaseFragment){
			LogUtil.d(TAG, "base fragment!");
			DevSetNetWorkBaseFragment fmt = (DevSetNetWorkBaseFragment)fragment;
			fmt.getString(R.id.dev_set_base_network_ip);
			
			LogUtil.d(TAG, fmt.getkey());
			LogUtil.d(TAG, fmt.getText(R.id.dev_set_base_network_ip));
			LogUtil.d(TAG, fmt.getString(R.id.dev_set_base_network_subnet_mask));
			LogUtil.d(TAG, fmt.getString(R.id.dev_set_base_network_gateway));
			LogUtil.d(TAG, fmt.getString(R.id.dev_set_base_network_dns1));
		}else if(fragment instanceof DevSetNetWorkWifiFragment){
			LogUtil.d(TAG, "wifi fragment!");
		}
	}
	
	private OnClickListener click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dev_set_network_cancel:
				finish();
				break;
			case R.id.dev_set_network_submit:
				save();finish();
				break;
			case R.id.dev_set_net_base:
				getFragmentView(0);
				dev_set_net_base.setTextColor(getResources().getColor(R.color.blue_menu));
				dev_set_net_wifi.setTextColor(getResources().getColor(R.color.black));
				break;
			case R.id.dev_set_net_wifi:
				getFragmentView(1);
				dev_set_net_base.setTextColor(getResources().getColor(R.color.black));
				dev_set_net_wifi.setTextColor(getResources().getColor(R.color.blue_menu));
				break;
			default:
				break;
			}
		}
	};
	
}
