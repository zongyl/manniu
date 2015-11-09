package com.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;

public class TrafficMonitor extends Activity {

	public static boolean isNetConnected(Context context){
		boolean flag = false;
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if(networkInfo.isConnected()){
			flag = true;
		}
		return flag;
	}
	
	public static boolean isWifi(Context context){
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();  
		if(!networkInfo.isConnected()){
			return false;
		}
		String type = networkInfo.getTypeName();
		if(!type.equalsIgnoreCase("WIFI")){
			return false;
		}
		return true;
	}
	
	public  int FlowTip(){
		int flow = 0;
		long  total = 0l;
		try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo("com.manniu.manniu", PackageManager.GET_ACTIVITIES);
            
            if(ai.permission!=null){
            	 total = TrafficStats.getUidTxBytes(ai.uid)+TrafficStats.getUidRxBytes(ai.uid);
            	 flow = (int) total/(1024*1024);
                 return flow;
            }
          
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
//		long total =0l;
//		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
//		Iterator<RunningAppProcessInfo> iter = infos.iterator();
//		while(iter.hasNext()){
//			total+=(TrafficStats.getUidTxBytes(iter.next().pid)+TrafficStats.getUidRxBytes(iter.next().pid));
//		}
		//return  (int) total/(1024*1024);
		//long sent = TrafficStats.getUidTxBytes(Process.myUid());
		return flow;
	}
	
}