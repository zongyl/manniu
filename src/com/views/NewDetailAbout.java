package com.views;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.adapter.HttpUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ShareContentCustomize;
import com.views.RecommendSheet.OnRecmSheetSelected;

public class NewDetailAbout extends Activity implements OnClickListener,OnRecmSheetSelected,OnCancelListener{
	
	private Context context;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.new_detail_about);
		setListeners();
	}
	
	public void setListeners(){
		findViewById(R.id.about_back).setOnClickListener(this);
		findViewById(R.id.recom_friends).setOnClickListener(this);
		//findViewById(R.id.spre_activity).setOnClickListener(this);
	}
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.about_back:
			finish();
			break;
			case R.id.recom_friends:
				/*//改用新的代码处理 2015.10.14 李德明
				ShareSDK.initSDK(this);
				OnekeyShare oks = new OnekeyShare();
				oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
				oks.setText(getString(R.string.app_name));
				oks.setImageUrl("http://www.9wingo.com/images/qrcode.png");
				oks.show(this);
				*/
				dwz();
				//RecommendSheet.showSheet(this, this, this);
				break;
		default:
			break;
		}
	}
	
	private void dwz(){
		RequestParams params = new RequestParams();
		params.put("url", Constants.hostUrl+ "/downLoadAndroidApp.jsp");
		HttpUtil.post("http://dwz.cn/create.php", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				String short_url = "";
				if(response.has("tinyurl")){
					try {
						short_url = response.getString("tinyurl");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					//String strText = "\t\t"+getString(R.string.famliy_around_withme)+"\r\n"+getString(R.string.allbeautifulview_oftheworld_inmyhand)+"\r\n<a href ='"+Constants.hostUrl+"/downLoadAndroidApp.jsp' >";
					//strText+=getString(R.string.Click_download)+":"+getString(R.string.APP_download_title)+"</a>";
					String strText = "\t\t"+getString(R.string.famliy_around_withme)+"\r\n"+getString(R.string.allbeautifulview_oftheworld_inmyhand)+"\r\n"+getString(R.string.APP_download_title);
					strText+="\r\n请点击:"+short_url;
				String strPYQText = "\t\t"+getString(R.string.famliy_around_withme)+"\r\n"+getString(R.string.allbeautifulview_oftheworld_inmyhand)+"\r\n"+getString(R.string.APP_download_title)+":"+short_url;					

				ShareContentCustomize.showShare( getString(R.string.app_name),context,getString(R.string.famliy_around_withme),
						strText,
						strPYQText,
						Constants.hostUrl+"/images/WechatMomentsqrcode.png",false,null);
					
				}
				
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
			}
		});
		
	}
	
	/**获取当前版本名称*/
	public String getVersionName(){
		/*获取PackageManager的实例*/
		PackageManager packageManager = getPackageManager();
		/*getPackageName()是你当前类的包名，0代表是获取版本信息*/
		PackageInfo packInfo =null;
		try {
			packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
		
			e.printStackTrace();
		}
		return packInfo.versionName;
	}
	
	private void forward(Class<?> target){
		Intent intent = new Intent(this,target);
		startActivity(intent);
	}
	
	public void onClick(int whichButton) {
		
		
	}
	
	public void onCancel(DialogInterface arg0) {
		
		
	}
	
	public void onBackPressed(){	
		finish();
		super.onBackPressed();
	}
}