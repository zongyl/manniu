package com.views;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.basic.APP;
import com.manniu.manniu.R;
import com.utils.Constants;

public class Squ_ShortActivity extends Fragment{
	
	WebView webView;
	Button refresh;
	boolean bShortStart=false;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.squ_fragment, null);
		webView = (WebView) view.findViewById(R.id.webView01);
		refresh = (Button) view.findViewById(R.id.refresh_web);
		
		webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("utf-8");
		//String webUrl = "http://10.12.6.121:8080/NineCloud/squareShort.jsp";
		String webUrl = Constants.hostUrl+"/squareShort.jsp";
		webView.loadUrl(webUrl);
		
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				/*try {
					if(url.contains("mobileSearch.jsp")){
						Intent intent = new Intent(getActivity(),SearchTip.class);
						APP.GetMainActivity().startActivity(intent);
					}
					if(url.contains("mp4")){
						String tempStr = URLDecoder.decode(url);
						Intent intent = new Intent(getActivity(),VlcVideoActivity.class);
						intent.putExtra("LiveInfo", new String[]{"short",tempStr});
						APP.GetMainActivity().startActivity(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}*/
				if(false == bShortStart)
				{	
					bShortStart = true;
					Intent intent = new Intent(Intent.ACTION_VIEW);
			        intent.setClassName(getActivity(), "com.views.NewWebActivity"); 
			        intent.putExtra("url", url);
			        intent.putExtra("playType", "1");
					APP.GetMainActivity().startActivity(intent);
					return true;
				}
				
				bShortStart = false;
				return true;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				webView.setVisibility(View.GONE);
				refresh.setVisibility(View.VISIBLE);
			}
			
		});
		return view;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		Log.v("keyCode:", ""+keyCode);
		if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
			webView.goBack();
			return true;
		}
		return getActivity().onKeyDown(keyCode, event);
	}
}
