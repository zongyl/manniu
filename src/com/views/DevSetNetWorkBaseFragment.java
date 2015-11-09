package com.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.manniu.manniu.R;
import com.utils.MD5Util;
import com.utils.SetSharePrefer;

public class DevSetNetWorkBaseFragment extends Fragment {

	public static final String TAG = "DevSetNetWorkBaseFragment";
	
	private EditText ip, subnet_mask, gateway, dns1;
	
	private String deviceId;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.new_device_set_network_base_fragment, null);
		
		deviceId = getArguments().getString("deviceId");
		
		ip = (EditText) view.findViewById(R.id.dev_set_base_network_ip);
		subnet_mask = (EditText) view.findViewById(R.id.dev_set_base_network_subnet_mask);
		gateway = (EditText) view.findViewById(R.id.dev_set_base_network_gateway);
		dns1 = (EditText) view.findViewById(R.id.dev_set_base_network_dns1);
		
		
		ip.setText("1111111111111");
		subnet_mask.setText("22222222222");
		gateway.setText("333333333");
		dns1.setText("444444444444" + readInfo("net_dns","net_dns_defValue"));
		return view;
	}
	
	private String readInfo(String key, String defValue){
		return SetSharePrefer.read(MD5Util.MD5(deviceId)+NewDeviceSet.FILE, key, defValue);
	}
	
}
