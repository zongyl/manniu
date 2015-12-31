package com.views;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.manniu.manniu.R;
import com.utils.MD5Util;
import com.utils.SetSharePrefer;

public class DevSetNetWorkBaseFragment extends Fragment {

	public final String TAG = "DevSetNetWorkBaseFragment";
	
	private EditText ip, subnet_mask, gateway, dns1;

	private RadioGroup net_type, dns_type;
	
	private RadioButton net_type_static_r, net_type_dhcp_r;
	
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

		net_type = (RadioGroup) view.findViewById(R.id.net_type);
		net_type_static_r = (RadioButton) view.findViewById(R.id.net_type_static);
		net_type_dhcp_r = (RadioButton) view.findViewById(R.id.net_type_dhcp);
		
		net_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == net_type_static_r.getId()){
					net_type_static_r.setChecked(true);
				}else if(checkedId == net_type_dhcp_r.getId()){
					net_type_dhcp_r.setChecked(true);
				}else{
				}
			}
		});
		
//		ip.setText("10.12.6.18");
//		subnet_mask.setText("255.255.255.0");
//		gateway.setText("10.12.6.1");
//		dns1.setText("114.114.114.114");
		String net_type = readInfo("net_type","");
		if("1".equals(net_type)){//dhcp
			net_type_dhcp_r.setChecked(true);
		}else if("2".equals(net_type)){//static
			net_type_static_r.setChecked(true);
		}else{
		}
		ip.setText(readInfo("wifi_ip",""));
		subnet_mask.setText(readInfo("pass_mask",""));
		gateway.setText(readInfo("net_gateway",""));
		dns1.setText(readInfo("net_dns",""));
		return view;
	}
	
	public String getkey(){
		save();
		return "abcdefg";
	}
	
	private void save(){
		if(net_type_static_r.isChecked()){
			writeInt("net_type", 2);
			writeInfo("wifi_ip", ip.getText().toString());
			writeInfo("pass_mask", subnet_mask.getText().toString());
			writeInfo("net_gateway", gateway.getText().toString());
			writeInfo("net_dns", dns1.getText().toString());
		}else if(net_type_dhcp_r.isChecked()){
			writeInt("net_type", 1);
		}else{
			
		} 
	}
	
	private String readInfo(String key, String defValue){
		return SetSharePrefer.read(MD5Util.MD5(deviceId)+NewDeviceSet.FILE, key, defValue);
	}
	
	private void writeInfo(String key, String value){
		SetSharePrefer.write(MD5Util.MD5(deviceId)+NewDeviceSet.FILE, key, value);
	}
	
	private void writeInt(String key, int value){
		SetSharePrefer.write(MD5Util.MD5(deviceId)+NewDeviceSet.FILE, key, value);
	}
	
}
