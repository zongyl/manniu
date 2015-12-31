package com.views;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.basic.APP;
import com.manniu.manniu.R;
import com.utils.Constants;

@SuppressLint("NewApi")
public class Squ_LiveActivity extends Fragment{
	
	private static final String TAG = "Squ_LiveActivity";
	
	String deviceId;
	WebView webView;
	Button refresh;
	Context context;
	public static boolean bLiveStart = false;//打开视频标识位
	/**SDK版本号*/
	private int sdk_int;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		sdk_int=android.os.Build.VERSION.SDK_INT;
		
		context = this.getActivity();
		View view = inflater.inflate(R.layout.squ_fragment, null);
		webView = (WebView) view.findViewById(R.id.webView01);
		refresh = (Button) view.findViewById(R.id.refresh_web);
		
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				LoadURL();
			}
		});
		
		
		try {
			WebSettings settings = webView.getSettings();
			settings.setJavaScriptEnabled(true);
			settings.setDefaultTextEncodingName("utf-8");
			settings.setJavaScriptEnabled(true);
			settings.setAllowFileAccess(true);
			settings.setDomStorageEnabled(true);
			if(sdk_int >= 17)
				settings.setMediaPlaybackRequiresUserGesture(false);
		} catch (Exception e) {
		}
		
		
		LoadURL();//2015.10.19 李德明，改为LoadURL()加载URL处理
		refresh.setVisibility(View.GONE);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.v(TAG, " new web url:"+url);
				try {
					if(url.contains("mobileSearch.jsp")){
						Intent intent = new Intent(getActivity(),SearchTip.class);
						APP.GetMainActivity().startActivity(intent);
					}else{
						
						if(!bLiveStart){
							bLiveStart = true;
							Intent intent = new Intent(Intent.ACTION_VIEW);
					        intent.setClassName(context, "com.views.NewWebActivity"); 
					        intent.putExtra("url", url);
					        intent.putExtra("playType", "0");
							APP.GetMainActivity().startActivity(intent);
							return true;
						}
	
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				webView.setVisibility(View.GONE);
				refresh.setVisibility(View.VISIBLE);
			}			 

		});
		
		return view;
	}

	private void LoadURL()
	{
		//String webUrl = "http://10.12.6.121:8080/NineCloud/sharesquare.jsp";
		String webUrl = Constants.hostUrl1+"/sharesquare.jsp";
		//String webUrl = Constants.hostUrl+"/LiveAction_toPlays?lc.deviceId=Q04hAQEAbDAwMjkzZjBkAAAAAAAA";
		webView.loadUrl(webUrl);
	};
	public boolean onKeyDown(int keyCode, KeyEvent event){
		Log.v("keyCode:", ""+keyCode);
		if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
			webView.goBack();
			return true;
		}
		return getActivity().onKeyDown(keyCode, event);
	} 
}
