package com.utils;

import com.bean.Device;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA. User: ljh Date: 2016-4-8 -- 10:45:09 To
 * change this template use File | Settings | File Templates.
 */
public class JsonString {
	
	public static String generateDeviceList(List<Device> devicesnlist) {
		String jsonString = "";
		try {
			JSONObject jsonObject = new JSONObject();
			org.json.JSONArray jsonArray = new org.json.JSONArray();
			for (Device devicesn : devicesnlist) {
				jsonArray.put(devicesn);
			}
			jsonObject.put("devicesnlist", jsonArray);
			jsonString = jsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
	
	public static String getDeviceJsonString(List<Device> list){
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<list.size();i++){
        	Device pbean = list.get(i);
            if(i>0){sb.append(",");};
            sb.append("{");
            if(pbean.dpassword == null){
            	sb.append("\"dpassword\":"+null);
            }else{
            	sb.append("\"dpassword\":\""+pbean.dpassword+"\"");
            }
            sb.append(",\"logo\":\""+pbean.logo+"\"");
            sb.append(",\"model\":\""+pbean.model+"\"");
            sb.append(",\"sn\":\""+pbean.sn+"\"");
            //sb.append(",\"sdkver\":"+"");
            sb.append(",\"userid\":\""+pbean.userid+"\"");
            sb.append(",\"state\":"+pbean.state);
            sb.append(",\"channels\":"+pbean.channels);
            sb.append(",\"devname\":\""+pbean.devname+"\"");
            sb.append(",\"domainid\":\""+pbean.domainid+"\"");
            sb.append(",\"ver\":"+pbean.ver);
            sb.append(",\"type\":"+pbean.type);
            sb.append(",\"ctrl_access\":"+1);
            sb.append(",\"isowner\":"+pbean.isowner);
            sb.append(",\"vn\":\""+pbean.vn+"\"");
            sb.append(",\"online\":"+pbean.online);
            sb.append(",\"createtime\":\""+pbean.createtime+"\"");
            //sb.append(",\"pn\":\""+pbean.pn+"\"");
            sb.append(",\"sid\":\"" + pbean.sid +"\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }
	
	
}
