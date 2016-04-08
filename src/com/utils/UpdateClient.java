package com.utils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import android.content.Context;

import com.adapter.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.bean.Update;
import com.manniu.manniu.R;
import com.views.BaseApplication;

public class UpdateClient {
	/**检查版本更新*/
	private Context mcotnext;
	public UpdateClient(Context context){
		this.mcotnext = context;
	}
	Update update =null;
	public  Update checkVersion(BaseApplication  context) throws Exception {
		String url =mcotnext.getString(R.string.server_address)+File.separator+"update";
		try{
			
			String result = HttpUtil.executeHttpGet(url+File.separator+"version.json");
			if(result.length()>0){
				update=new Update();
				update.setVersionCode(JSON.parseObject(result).getIntValue("verCode"));
				update.setVersionName(JSON.parseObject(result).getString("verName"));
				update.setDownloadUrl(url+File.separator+JSON.parseObject(result).getString("apkname"));
				update.setUpdateLog(JSON.parseObject(result).getString("updateLog"));
				update.setAppName(JSON.parseObject(result).getString("appname"));
				try{
					URL url1 = new URL(update.getDownloadUrl());
					HttpURLConnection conn = (HttpURLConnection)url1.openConnection();
					conn.connect();
					int length = conn.getContentLength();
					//显示文件大小格式:2个小数点显示
					DecimalFormat df= new DecimalFormat("0.00");
					//进度条下面显示总文件大小
					update.setApkFileSize(df.format((float)length/1024/1024)+"MB");
				}catch(Exception e){
					e.printStackTrace();
					//APP.ShowToast("请检查网络是否正常连接");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			//APP.ShowToast("请检查网络是否正常连接");
		}
		return update;
	}
} 
