package com.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manniu.manniu.R;

/**
 * 设备设置 -> 网络 -> wifi
 * @author pc
 *
 */
public class DevSetNetWorkWifiFragment extends Fragment {

	public static final String TAG = "DevSetNetWorkWifiFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.new_device_set_network_wifi_fragment, null);
		
		return view;
	}
}
