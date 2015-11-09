package com.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.UUID;

import com.basic.APP;
import com.manniu.manniu.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2015-4-8 下午2:59:07
 * To change this template use File | Settings | File Templates.
 * Description：
 */

public class SIMCardInfo {
	
	public static final String TAG = "SIMCardInfo";
	
	/**
     * TelephonyManager提供设备上获取通讯服务信息的入口。 应用程序可以使用这个类方法确定的电信服务商和国家 以及某些类型的用户访问信息。
     * 应用程序也可以注册一个监听器到电话收状态的变化。不需要直接实例化这个类
     * 使用Context.getSystemService(Context.TELEPHONY_SERVICE)来获取这个类的实例。
     */
    private TelephonyManager telephonyManager;
    /**
     * 国际移动用户识别码
     */
    private String IMSI;
    private Context context;
    protected String PREFS_FILE = "device_id.xml";
    protected String PREFS_DEVICE_ID = "device_id";
    protected UUID uuid;
 
    public SIMCardInfo(Context context) {
    	this.context = context;
        telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
    }
 
    /**
     * 获取设备IMEI
     * @return
     */
    public String getDeviceId(){
    	return telephonyManager.getDeviceId();
    }
 
    /**
     *  获取设备信息
     * @return
     */
    public String getDeviceInfo(){
    	return Build.MANUFACTURER +" "+ Build.PRODUCT +" "+ Build.MODEL;
    }
    
	public UUID getDeviceUuid() {
		if (uuid == null) {
			SharedPreferences prefs = context.getSharedPreferences(
					PREFS_FILE, 0);
			String id = prefs.getString(PREFS_DEVICE_ID, null);
			if (id != null) {
				uuid = UUID.fromString(id);
			} else {
				String androidId = Secure.getString(
						context.getContentResolver(), Secure.ANDROID_ID);
				try {
					if (!"9774d56d682e549c".equals(androidId)) {
						uuid = UUID.nameUUIDFromBytes(androidId
								.getBytes("utf8"));
					} else {
						String deviceId = ((TelephonyManager) context
								.getSystemService(Context.TELEPHONY_SERVICE))
								.getDeviceId();
						uuid = deviceId != null ? UUID
								.nameUUIDFromBytes(deviceId
										.getBytes("utf8")) : UUID
								.randomUUID();
					}
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				// Write the value out to the prefs file
				prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
			}
		}
		return uuid;
	}

    /**
     * Role:获取当前设置的电话号码
     * <BR>Date:2012-3-12
     */
    public String getNativePhoneNumber() {
        String NativePhoneNumber=null;
        NativePhoneNumber=telephonyManager.getLine1Number();
        return NativePhoneNumber;
    }
    
    //MAC 地址
    public String getLocalMacAddress() { 
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE); 
        WifiInfo info = wifi.getConnectionInfo(); 
        return info.getMacAddress(); 

   }  
 
    /**
     * Role:Telecom service providers获取手机服务商信息 <BR>
     * 需要加入权限<uses-permission
     * android:name="android.permission.READ_PHONE_STATE"/> <BR>
     *
     */
    public String getProvidersName() {
        String ProvidersName = null;
        // 返回唯一的用户ID;就是这张卡的编号神马的
        IMSI = telephonyManager.getSubscriberId();
        // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
        System.out.println(IMSI);
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
            ProvidersName = APP.GetString(R.string.yidong);
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = APP.GetString(R.string.liantong);
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = APP.GetString(R.string.dianxin);
        }
        return ProvidersName;
    }

    public String getBuildFields(){
    	StringBuffer sb = new StringBuffer();
    	Field[] fields = Build.class.getDeclaredFields();
    	for(Field field : fields){
    		field.setAccessible(true);
    		try {
    			sb.append(field.getName()+":"+field.get(null).toString());
				Log.d(TAG, field.getName()+":"+ field.get(null).toString());
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "getBuildFields " + e.getMessage());
			} 
    	}
    	return sb.toString();
    }
    
}