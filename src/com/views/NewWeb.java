package com.views;

import java.io.File;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.manniu.manniu.R;

public class NewWeb extends XViewBasic{
	
	private static final String TAG = "NewWeb";
	private static final String APP_CACHE_DIRNAME = "/webcache";
	private static final int ENCODEURL =100;

	String deviceId;
	WebView webView;
	Button refresh;
	WebSettings settings;
	String cacheDirPath;
	
	public NewWeb(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		
		webView = (WebView)findViewById(R.id.webView01);
		refresh = (Button) findViewById(R.id.refresh_web);
		
		
		settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDefaultTextEncodingName("utf-8");
		Log.v(TAG, "webview 缓存模式:"+settings.getCacheMode());
		settings.setCacheMode(settings.LOAD_NO_CACHE);
		//缓存策略，判断是否有网路，有网络使用LOAD_DEAFULT,无网络使用LOAD_CACHE_ELSE_NETWORK
		//settings.setAppCacheEnabled(true);
		//settings.setCacheMode(settings.LOAD_CACHE_ELSE_NETWORK);
		//开启DOM storage API功能
		//settings.setDomStorageEnabled(true);
		//开启database storage功能
		//settings.setDatabaseEnabled(true);
		
	    //cacheDirPath = Environment.getExternalStorageDirectory().getAbsoluteFile()+APP_CACHE_DIRNAME;
		//settings.setAppCachePath(cacheDirPath);
		//设置数据库缓存路径
		//settings.setDatabasePath(cacheDirPath);
		//设置Application caches缓存目录
		//settings.setAppCachePath(cacheDirPath);
		//settings.setAppCacheEnabled(true);
		
		
		//settings.setAppCacheMaxSize(appCacheMaxSize);
		/*DisplayMetrics metrics = new DisplayMetrics();
		ACT.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int mDensity = metrics.densityDpi;
		if(mDensity == 120){
			settings.setDefaultZoom(ZoomDensity.CLOSE);
		}else if(mDensity == 160){
			settings.setDefaultZoom(ZoomDensity.MEDIUM);
		}else if(mDensity == 240){
			settings.setDefaultZoom(ZoomDensity.FAR);
		}*/
		//http://127.0.0.1:8020/ytest/index.html
		
		//jsp页面后缀添加随机整形参数，每次请求刷新jsp页面
		//String webUrl = Constants.hostUrl+"/sharesquare.jsp?dd="+new Random().nextInt(999999);
		//String webUrl = ACT.getString(R.string.server_address)+"/sharesquare.jsp";
	
		String webUrl = "http://10.12.6.107:8080/NineCloud/sharesquare.jsp";
		//String webUrl = Constants.hostUrl+"/mobile.html";
		//String webUrl = "http://10.12.6.130:80/index1.html";
		//String webUrl = Constants.hostUrl+"/mobileLive.jsp";
		//String webUrl = "http://10.12.6.130:80/Q04hAQEAbDAwMDEwYmUxAAAAAAAA.m3u8";
		//webView.loadData(webUrl, "text/html", "utf-8");
		
		webView.loadUrl(webUrl);
		//webView.loadDataWithBaseURL(webUrl, null, "text/html", "utf-8", null);
		//webView.addJavascriptInterface(new transUserId(), "userId");
		//webView.loadUrl("http://square.ys7.com/square/mobile/index.jsp?f=app&version=&v=150623134457");
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.v(TAG, " new web url:"+url);
				
		       /* APP.GetMainActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Intent intent=new Intent(Intent.ACTION_VIEW);
				        intent.setClassName(ACT, "com.views.NewWebActivity"); 
						APP.GetMainActivity().startActivity(intent);
					}
				});*/
				//view.loadUrl("http://m.baidu.com");
				//http://120.26.194.72/NineCloud/LiveAction_toPlays?lc.deviceId=Q04hAQEAbDAwMDEwYmUxAAAAAAAA&lc.livename=IPC(manniu204)
				/*try {
					if(url.contains("LiveAction_toPlays")){
						String[] tempArr = URLDecoder.decode(url).split("=");
						deviceId = tempArr[1].split("&")[0];
						//String str = new String(tempArr[2].getBytes("iso8859-1"),"utf-8");
						Intent intent=new Intent(Intent.ACTION_VIEW);
						intent.putExtra("LiveInfo", new String[]{deviceId,tempArr[2]});
				        intent.setClassName(ACT, "com.nmbb.vlc.ui.VlcVideoActivity"); 
						APP.GetMainActivity().startActivity(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}*/
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
		
		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				Log.v(TAG, "title:"+title);
			}
		});
		
		/*findViewById(R.id.new_web_btn).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(Intent.ACTION_VIEW);
		        intent.setClassName(ACT, "com.views.NewWebActivity");  
		        APP.GetMainActivity().startActivity(intent);
			}
		});*/
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				refresh.setVisibility(View.GONE);
				webView.reload();
			}
		});
	}
	
	public void clearWebViewCache(){
		//清理webview缓存
		File cache  = new File(cacheDirPath);
		if(cache.exists()){
			deleteFile(cache);
		}
	}

	public void deleteFile(File file){
		if(file.exists()){
			if(file.isFile()){
				file.delete();
			}else if(file.isDirectory()){
				File[] files = file.listFiles();
				for(int i = 0;i<files.length;i++){
					deleteFile(files[i]);
				}
			}
			file.delete();
		}else{
			Log.e(TAG, "delete file no exists"+file.getAbsolutePath());
		}
	}
	
	@Override
	protected void onClick(int id) {
		/*super.onClick(id);
		if(id == R.id.refresh_web){
			//refresh.setVisibility(View.GONE);
			//webView.setVisibility(View.VISIBLE);
			webView.reload();
		}*/
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		Log.v("keyCode:", ""+keyCode);
		if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
			webView.goBack();
			return true;
		}
		return ACT.onKeyDown(keyCode, event);
	}
}
