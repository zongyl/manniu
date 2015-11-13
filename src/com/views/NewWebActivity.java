package com.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.adapter.HttpUtil;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.AMap.OnMapLoadedListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.basic.APP;
import com.ctrl.PopupMenu;
import com.ctrl.XImageBtn;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.utils.SetSharePrefer;

@SuppressLint({ "SetJavaScriptEnabled", "ResourceAsColor", "NewApi" })
public class NewWebActivity extends Activity implements OnClickListener, OnMapLoadedListener, OnMapClickListener, InfoWindowAdapter{
	
	private static final String TAG = "NewWebActivity";
	private FrameLayout _titleFrm;
	XImageBtn _cancel, _more;
	
	TextView _title;

	LinearLayout squareTools;
	
	LinearLayout comEdit;
	
	ImageView cancelC;
	
	EditTextPreIme editC;
	
	ImageView confrimC;
	
	InputMethodManager imm;
	
	WebView webView;
	String tempTitle;
	
	ProgressBar progressBar; 
	
	Context context;
	
	ImageView collection;
	
	ImageView like;
	
	ImageView squareShare;
	
	ImageView screenshot;
	
	Button comment;
	
	String liveId;
	String liveUrl;
	String liveImg;
	Button refresh;
		
	
	boolean b_like = false;
	boolean b_collection = false;

	//add by zra
	private Bitmap bmp = null;
	private ImageView image = null;
	private FileService fileService = null;
	
	//add by zra
	LinearLayout playLayout;
	boolean bPopKey;
	//add by zra
	WebView  commentView;
	LinearLayout commentTools;
	TextView commentBar;
	TextView informationBar;
	LinearLayout  informationPage;
	int commentNum;
	//add by zra
	TextView commentTitle;
	TextView commentContent;
	TextView commentProfile;
	TextView commentAddress;
	TextView commentPublisher;
	boolean bComment = true;
	int dmHeight;
	int dmWidth;
	//add by zra
	public  MapView mMapView;
	public   AMap mAMap;
	private boolean   bSubView = false;
	private int count =0;
	//add by zra
	private boolean bGetBoardHeight = false;
	private int iBoardHeight;
	private boolean bFinish = true;
	private boolean bRestore = false;
	private boolean bInput = false;
	private int bType;
	private boolean bHave = true;
	private boolean bConfig = true;
	private float devLng;
	private float devLat;
	private JSONArray posArray;
	private Handler  handler;
	private static long oldTime;
	
	@SuppressWarnings("deprecation")
	@SuppressLint({ "JavascriptInterface", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉应用标题
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		NewMain._isOpen = true;
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		context = this.getApplicationContext();
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		bType = Integer.parseInt(getIntent().getStringExtra("playType"));
		parseURL();
		handler = new Handler(){
			@Override
	    	public void handleMessage(Message msg) {
	    		// TODO Auto-generated method stub
	    		super.handleMessage(msg);
	    		switch(msg.what)
	    		{
	    		case 0:
	    			initAMap();
	    			getSurrroundGp();
	    			break;
	    		case 1:
	    			initOverlay();	
	    			break;
	    		default:
	    			break;
	    		}
	    		
	    	}
		};
		
		setContentView(R.layout.new_web_activity);
		playLayout = (LinearLayout)findViewById(R.id.play_layout);
		
		refresh = (Button) findViewById(R.id.refresh_web1);
		refresh.setVisibility(View.GONE);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				webView.loadUrl(liveUrl);
			}
		});
		
		video_fullView = (FrameLayout) findViewById(R.id.video_fullView);
		
		int height = dm.heightPixels ;//高度
		int width = dm.widthPixels;
		dmHeight =dm.heightPixels;
		dmWidth = dm.widthPixels;
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) video_fullView.getLayoutParams(); 
		linearParams.height = width;
		linearParams.width = height;
		video_fullView.setLayoutParams(linearParams);
		
		_titleFrm = (FrameLayout) findViewById(R.id.webTitle);
		height = dm.heightPixels ;//高度
		linearParams = (LinearLayout.LayoutParams) _titleFrm.getLayoutParams(); 
		linearParams.height = height*3/40;
		//webView.getSettings().setDomStorageEnabled(true);//2015.10.09 李德明 增加本地缓存配置，解决广场上的网页，一段时间会没有响应的问题
		_title = (TextView)findViewById(R.id.web_title);
		_cancel = (XImageBtn)findViewById(R.id.web_cancel);
		_more = (XImageBtn)findViewById(R.id.web_more);
		
		videoWebViewInit(height);
		
		getCommentNum();
		//设备评论相关
		commentOperation(height);
		showSmallMap(savedInstanceState);
		
		progressBar = (ProgressBar)findViewById(R.id.progressBar2);
		
		collection = (ImageView)findViewById(R.id.collection);
		like = (ImageView)findViewById(R.id.like);
		squareShare = (ImageView)findViewById(R.id.square_share);
		screenshot = (ImageView)findViewById(R.id.previously);
		
		initPraiseAndCollect();
		webView.loadUrl(liveUrl);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				  if(url.startsWith("http://") && getRespStatus(url)==404) {	
					  
					  	view.stopLoading();
					  	//载入本地assets文件夹下面的错误提示页面404.html   
					  	//view.loadUrl("file:///android_asset/404.html");
					  	//webView.setVisibility(View.GONE);
						//refresh.setVisibility(View.VISIBLE);
						view.clearView();
						String data = "NET DISCONNECT, PAGE NO FOUND";
						view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
				   } else {  
				     	view.loadUrl(url);  
				   }
				  
				   //view.loadUrl(url);
				   Log.v(TAG, "-----直播地址-----------"+url);
				   return true;
			}   
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				view.stopLoading();
				view.clearView();
				String data = "NET DISCONNECT, PAGE NO FOUND";
				view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
			}
		});

		webView.setWebChromeClient(new WebChromeClient(){
			// 播放网络视频时全屏会被调用的方法
			@Override
			public void onShowCustomView(View view, CustomViewCallback callback) {
				
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				_titleFrm.setVisibility(View.GONE);
				
				if(bComment == true)
				{
					commentView.setVisibility(View.GONE);
				}
				else
				{
					informationPage.setVisibility(View.GONE);
				}
				
				commentTools.setVisibility(View.GONE);
				squareTools.setVisibility(View.GONE);
				
				webView.setVisibility(View.GONE);

				if (xCustomView != null) {
					callback.onCustomViewHidden();
					return;
				}
				
				playLayout.setBackgroundColor(Color.parseColor("#000000"));
				video_fullView.addView(view);
				xCustomView = view;
				xCustomViewCallback = callback;
				video_fullView.setVisibility(View.VISIBLE);
			}
			
			// 视频播放退出全屏会被调用的
			@Override
			public void onHideCustomView() {
				
				if (xCustomView == null)// 不是全屏播放状态
				{
					return;
				}
				
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				xCustomView.setVisibility(View.GONE);
				video_fullView.removeView(xCustomView);
				xCustomView = null;
				video_fullView.setVisibility(View.GONE);
				xCustomViewCallback.onCustomViewHidden();
				playLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
				webView.setVisibility(View.VISIBLE);
				_titleFrm.setVisibility(View.VISIBLE);
				
				if(bComment == true)
				{
					commentView.setVisibility(View.VISIBLE);
				}
				else
				{
					informationPage.setVisibility(View.VISIBLE);
				}
				
				commentTools.setVisibility(View.VISIBLE);
				squareTools.setVisibility(View.VISIBLE);
				
				if(bPopKey == true)
				{
					restoreCommentAndInfoView();
				}
				
				webView.stopLoading();
		        webView.clearCache(true);  
		        webView.clearFormData();
		        webView.clearHistory(); 
			}
			
			@Override
			public void onReceivedTitle(WebView view, String title) {
				tempTitle = title;
				if(title.trim()=="")
				{
					_title.setText(tempTitle);
				}
			}
			
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				_title.setText(getString(R.string.loading___));
				//_title.setText("加载中..."+newProgress);
				setTitle("loading...");
				setProgress(newProgress * 100);
				
				if(newProgress == 100){
					progressBar.setVisibility(View.GONE);
					_title.setText(tempTitle);
					setTitle("loaded success!");
				}else{
					if(progressBar.getVisibility() == View.GONE){
						progressBar.setVisibility(View.VISIBLE);
					}else{
						progressBar.setProgress(newProgress);
					}
				}
			}
		});
		
		bindListeners();
		
		//百度地图相关
		playLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	            // TODO Auto-generated method stub
	        	/*if(bInput== true)
	        	{
	        		bInput = false;
	        		return ;
	        	}*/
	        	
	        	Rect r = new Rect();
        		playLayout.getWindowVisibleDisplayFrame(r);
	            int screenHeight = playLayout.getRootView().getHeight();
	            
	        	if(bGetBoardHeight == false)
	            {
	        		bGetBoardHeight = true;
		            iBoardHeight = screenHeight - (r.bottom - r.top);
	            }
	        	
	        	if(bFinish == false)
	        	{
	        		if(count < 1){
	        			oldTime = System.currentTimeMillis();
	        			count ++;
	        			return;
	        		}
	        		else{	
	        			
	        			long curTime = System.currentTimeMillis();
	        			
	        			if( curTime - oldTime <= 500)
	        			{
	        				//APP.ShowToast(Long.toString(curTime - oldTime));
	        				return;
	        			}
	        
	        			restoreCommentAndInfoView();
	        		}
	        	}
	        	
	        }
	    });
		
	}
	
	

	
	private void resetCommentAndInfoView()
	{    
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);	
		squareTools.setVisibility(View.GONE);
		comEdit.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams  linearParams;
		
		if(bComment == true)
		{
			commentView.setVisibility(View.VISIBLE);
			linearParams = (LinearLayout.LayoutParams) commentView.getLayoutParams(); // 取控件webView当前的布局参数		
			linearParams.height = dmHeight/55;//dmHeight/2 -dmHeight/20- dmHeight/40 - dmHeight/320 -dmHeight/640-iBoardHeight;// height - 500;// 当前界面高度-320
			commentView.setLayoutParams(linearParams);
		}
		else
		{
			commentContent.setVisibility(View.GONE);
			if(bType == 0)
				mMapView.setVisibility(View.GONE);
			linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); // 取控件webView当前的布局参数		
			linearParams.height = dmHeight/55;//dmHeight/2 -dmHeight/20-dmHeight/40 - dmHeight/320 -dmHeight/640-iBoardHeight;// height - 500;// 当前界面高度-320
			informationPage.setLayoutParams(linearParams);
		}
		if(bType == 0)
			informationBar.setClickable(false);
		commentBar.setClickable(false);
		
		bPopKey = true;
		bFinish = false;
	}
	
	
	
	private void restoreCommentAndInfoView()
	{
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		comEdit.setVisibility(View.GONE);
		squareTools.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams  linearParams;
		
		if(bComment == true)
		{
			commentView.setVisibility(View.VISIBLE);
			linearParams = (LinearLayout.LayoutParams) commentView.getLayoutParams(); // 取控件webView当前的布局参数		
			linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;// height - 500;// 当前界面高度-320
			commentView.setLayoutParams(linearParams);
		}
		else
		{
			commentContent.setVisibility(View.VISIBLE);
			if(bType == 0)
				mMapView.setVisibility(View.VISIBLE);
			linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); // 取控件webView当前的布局参数		
			linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;// height - 500;// 当前界面高度-320
			informationPage.setLayoutParams(linearParams);
		}
		
		if(bType == 0)
			informationBar.setClickable(true);
		commentBar.setClickable(true);
		
		bPopKey = false;
		count = 0;
		bFinish = true;
	}
	
	private void snapshotOperation()
	{
		destoryBitmap();
		bmp = captureWebViewVisibleSize(webView);

        Log.i(TAG, "comment_snapshot");
        
        String fileName = fileService.saveBitmapToSDCard("" + "commentSnapshot.png", bmp);
        Toast.makeText(getApplicationContext(), getString(R.string.file) + fileName + getString(R.string.SUCCESS_SAVE), Toast.LENGTH_SHORT).show();
	}
	
	private void videoWebViewInit(int height)
	{
		webView = (WebView)findViewById(R.id.webView02);
		if(android.os.Build.VERSION.SDK_INT > 16)
			webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
		WebSettings ws = webView.getSettings();
		ws.setBuiltInZoomControls(true);// 隐藏缩放按钮	
		//ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);// 排版适应屏幕
		ws.setUseWideViewPort(true);// 可任意比例缩放
		ws.setLoadWithOverviewMode(true);// setUseWideViewPort方法设置webview推荐使用的窗口。setLoadWithOverviewMode方法是设置webview加载的页面的模式。
		ws.setSavePassword(true);
		ws.setSaveFormData(true);// 保存表单数据
		ws.setJavaScriptEnabled(true);
		ws.setGeolocationEnabled(true);// 启用地理定位
		ws.setDomStorageEnabled(true);
		ws.setSupportMultipleWindows(true);// 新加
		ws.setSupportZoom(true);
		 
		//2015.10.13 李德明 重新调整webView高度
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) webView.getLayoutParams(); // 取控件webView当前的布局参数		
		linearParams.height = height*7/20-height/80;// height - 500;// 当前界面高度-320
		webView.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件webView
		
		//webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);//2015.10.10 李德明临时屏蔽，会导致广场上视频播放不；了
		//webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		webView.addJavascriptInterface(new Object(){
			@JavascriptInterface
			public void close(){				
				finish();
			}
			@JavascriptInterface
			public void openWithExplorer(){				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setAction("android.intent.action.VIEW");
				intent.setData(Uri.parse(liveUrl));
				startActivity(intent);
				finish();
			}
		}, "callAndroid");
		
		
		webView.setDrawingCacheEnabled(true);
        fileService = new FileService(this);  
	}
	
	private void commentOperation(int height)
	{
		comEdit =(LinearLayout)findViewById(R.id.share_comment);
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) comEdit.getLayoutParams(); 
		linearParams.height = height*3/40+dmHeight/320+dmHeight/50;
		comEdit.setLayoutParams(linearParams); 
		
		
		comment = (Button)findViewById(R.id.square_com);
		squareTools = (LinearLayout)findViewById(R.id.square_tools);
		
		linearParams = (LinearLayout.LayoutParams) squareTools.getLayoutParams(); 
		linearParams.height = height*3/40;
		squareTools.setLayoutParams(linearParams); 
		cancelC = (ImageView) findViewById(R.id.cancel_com);
		editC = (EditTextPreIme) findViewById(R.id.edit_com);
		confrimC = (ImageView) findViewById(R.id.confirm_com);
		
		//height-500
		commentTools = (LinearLayout)findViewById(R.id.comment_tools);
		linearParams = (LinearLayout.LayoutParams) commentTools.getLayoutParams(); // 取控件webView当前的布局参数		
		linearParams.height = height/20;// height - 500;// 当前界面高度-320
		commentTools.setLayoutParams(linearParams);
		
		commentBar = (TextView)findViewById(R.id.comment_bar);
		commentBar.setBackgroundColor(Color.parseColor("#66CCFF"));
		commentBar.setClickable(true);
		
		if(bType == 0)
		{
			informationBar = (TextView)findViewById(R.id.information_bar);
			informationBar.setVisibility(View.VISIBLE);
			informationBar.setClickable(true);
			
		}
		
		commentView = (WebView)findViewById(R.id.webView03);
		commentView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		
		commentView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				
				if(arg0.equals(commentView))
				{
					arg0.getParent().requestDisallowInterceptTouchEvent(true);
					arg0.getParent().getParent().requestDisallowInterceptTouchEvent(true);
				}
				return false;
			}
		});
		
		linearParams = (LinearLayout.LayoutParams) commentView.getLayoutParams(); // 取控件webView当前的布局参数		
		linearParams.height = height/2 -height/20+height/40+height/80;// height - 500;// 当前界面高度-320
		commentView.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件webView
		
		commentView.setVerticalScrollBarEnabled(true);
		commentView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		WebSettings settings = commentView.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		commentView.loadUrl(Constants.hostUrl + "/mobile/commentMsg.jsp");
		//commentView.loadUrl("http://10.12.6.121:8080/NineCloud/mobile/commentMsg.jsp");
		
		commentView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				   view.loadUrl(url);
				   Log.v(TAG, "-----直播地址-----------"+url);
				   return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) 
			{
				if(bType == 0)
					commentView.loadUrl("javascript:setLiveId('" + liveId + "'," + '0' + ")" );
				else
					commentView.loadUrl("javascript:setLiveId('" + liveId + "'," + '1' + ")" );
				
				super.onPageFinished(view, url);
			}
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				view.stopLoading();
				view.clearView();
				String data = "NET DISCONNECT, PAGE NO FOUND";
				view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
			}
		});
		
		commentView.setWebChromeClient(new WebChromeClient(){
			
			@Override
			public void onProgressChanged(WebView view, int newProgress){
				NewWebActivity.this.setProgress(newProgress *100);
			}
		});
		
		informationPage = (LinearLayout)findViewById(R.id.information_page);
		linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); // 取控件webView当前的布局参数		
		linearParams.height = height/2 - height/20 +height/40+height/80;
		informationPage.setLayoutParams(linearParams);
		
		commentTitle = (TextView)findViewById(R.id.comment_title);
		commentContent = (TextView)findViewById(R.id.comment_content);
		
		getDeviceInfo();
		
		
		editC.addTextChangedListener(new TextWatcher(){
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
					bInput = true;
			}
			
			@Override
			public void afterTextChanged(Editable s){
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int before, int count){
				
			}
		});	
		
		
		editC.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					restoreCommentAndInfoView();
				}

				return false;
			}
		});
	}

	private void showSmallMap(Bundle savedInstanceState)
	{	
		try{
				
			mMapView = (MapView) findViewById(R.id.comment_map);
			mMapView.onCreate(savedInstanceState);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void initAMap()
	{
		mAMap = mMapView.getMap();
		mAMap.setOnMapClickListener(this);
		mAMap.setOnMapLoadedListener(this);
		mAMap.setInfoWindowAdapter(this);
		UiSettings uiSettings = mAMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setScaleControlsEnabled(false);
		uiSettings.setAllGesturesEnabled(false);
		
		LatLngBounds bounds= null;
		if(bConfig == false)
		{
			bounds = new LatLngBounds.Builder().include(new LatLng(30.297233, 120.047253)).build();	
		}
		else{
			bounds = new LatLngBounds.Builder().include(new LatLng(devLat, devLng)).build();	
		}
		
		mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
	}
	
	public void initOverlay()
	{
		
		
		try{		
			mAMap.clear();
			MarkerOptions markerOption;
			// add marker overlay
			if(bHave == false)
			{
				LatLng llD = new LatLng(30.297233, 120.047253);
				LatLng llB = new LatLng(30.287233, 120.147253);
				LatLng llC = new LatLng(30.27233, 120.247253);  
				LatLng llA = new LatLng(30.267233, 120.347253);
				
				markerOption = new MarkerOptions();
				markerOption.position(llA);
				markerOption.title("蛮牛云眼1号").snippet("坐标:30.267233, 120.347253");
				markerOption.draggable(true);
				markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_other));
				mAMap.addMarker(markerOption);
				
				markerOption = new MarkerOptions();
				markerOption.position(llB);
				markerOption.title("蛮牛云眼2号").snippet("坐标:30.27233, 120.247253");
				markerOption.draggable(true);
				markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_other));
				mAMap.addMarker(markerOption);
				
				markerOption = new MarkerOptions();
				markerOption.position(llC);
				markerOption.title("蛮牛云眼3号").snippet("坐标:30.287233, 120.147253");
				markerOption.draggable(true);
				markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_other));
				mAMap.addMarker(markerOption);
			}
			else{
				
				for(int i=0; i< posArray.length(); i++)
				{
					JSONObject jpos = posArray.getJSONObject(i);
					
					float devLngT = Float.parseFloat(jpos.get("longitude").toString());
				 	float devLatT = Float.parseFloat(jpos.get("latitude").toString());
				 	
					markerOption = new MarkerOptions();
					markerOption.position(new LatLng(devLatT, devLngT));
					markerOption.title("蛮牛云眼1号");
					markerOption.draggable(true);
					markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera_other));
					mAMap.addMarker(markerOption);
				}
				
			}
			
			if(bConfig == false)
			{
				ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
				giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.camera_other));
				giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.camera_me));
				MarkerOptions ooD = new MarkerOptions().position(new LatLng(30.297233, 120.047253)).title("蛮牛总部云眼").snippet("坐标:30.297233, 120.047253").icons(giflist).draggable(true).period(3);
				mAMap.addMarker(ooD).showInfoWindow();
			}
			else
			{
				ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
				giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.camera_other));
				giflist.add(BitmapDescriptorFactory.fromResource(R.drawable.camera_me));
				MarkerOptions ooD = new MarkerOptions().position(new LatLng(devLat, devLng)).title("蛮牛总部云眼").snippet("坐标:30.297233, 120.047253").icons(giflist).draggable(true).period(3);
				mAMap.addMarker(ooD).showInfoWindow();
			}	
	
		}catch(Exception e){
			
			LogUtil.d("NewWebActivity", "map operation");
			e.printStackTrace();
		}
	}
	
	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public View getInfoWindow(Marker marker){    
		
		TextView textView = new TextView(getApplicationContext());       
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,  15);    
		textView.setTextColor(Color.BLACK);    
		textView.setGravity(Gravity.CENTER_HORIZONTAL);    
		textView.setPadding(20,  10,  20,  20);    
		String title = marker.getTitle();
		textView.setText(title);
		
		return  textView;    
	}
	
	/**
	 * 监听amap地图加载成功事件回调
	 */
	@Override
	public void onMapLoaded() {
		return;
	}
	
	private void resetViewAndMap()
	{
		try{	
			webView.setVisibility(View.GONE);
			commentTools.setVisibility(View.GONE);
			squareTools.setVisibility(View.GONE);
			commentTitle.setVisibility(View.GONE);
			commentContent.setVisibility(View.GONE);
			LinearLayout.LayoutParams  linearParams;
			linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); // 取控件webView当前的布局参数		
			linearParams.height = dmHeight- dmHeight*3/40;// height - 500;// 当前界面高度-320
			informationPage.setLayoutParams(linearParams);
	       
			UiSettings uiSettings = mAMap.getUiSettings();
			uiSettings.setCompassEnabled(true);
			uiSettings.setZoomControlsEnabled(true);
			uiSettings.setScaleControlsEnabled(true);
			uiSettings.setAllGesturesEnabled(true);
			mAMap.setOnMapClickListener(null);
			
		} catch(Exception e){		
			LogUtil.d("NewWebActivity", ExceptionsOperator.getExceptionInfo(e));
		}	
	}
	
	
	private void restoreViewAndMap()
	{
		webView.setVisibility(View.VISIBLE);
		commentTools.setVisibility(View.VISIBLE);
		squareTools.setVisibility(View.VISIBLE);
		commentTitle.setVisibility(View.VISIBLE);
		commentContent.setVisibility(View.VISIBLE);
		_titleFrm.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams  linearParams;
		linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); // 取控件webView当前的布局参数		
		linearParams.height = dmHeight/2 - dmHeight/20 +dmHeight/40+dmHeight/80;// height - 500;// 当前界面高度-320
		informationPage.setLayoutParams(linearParams);

		UiSettings uiSettings = mAMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setScaleControlsEnabled(false);
		uiSettings.setAllGesturesEnabled(false);
		mAMap.setOnMapClickListener(this);
	}
	
	@Override
	public void onMapClick(LatLng point) {
		
		resetViewAndMap();
			bSubView = true;

	}
   
	@Override
	protected void onPause() {
		
		super.onPause();
		if(bType == 0)
			mMapView.onPause();
	}

	@Override
	protected void onResume() {
	
		super.onResume();
		if(bType == 0)
			mMapView.onResume();
	}
	
	private void parseURL()
	{
		liveUrl = getIntent().getStringExtra("url");

//		LogUtil.d(TAG, "liveUrl:" + liveUrl);
//		LogUtil.d(TAG, "Constants.hostUrl:" + Constants.hostUrl);
//		liveUrl = liveUrl.replace(Constants.hostUrl, "http://10.12.6.121:8080/NineCloud");
//		LogUtil.d(TAG, "............. liveUrl:" + liveUrl);
		
		if(liveUrl.indexOf("?")!=-1){
			String str = liveUrl.split("\\?")[1];
			String[] strs = str.split("&");
			for(String s : strs){
				if("lc.deviceId".equals(s.split("=")[0])){
					liveId = s.split("=")[1];
				}else if("lc.img".equals(s.split("=")[0])){
					if(liveUrl.split("\\?").length > 2){
						liveImg = s.split("=")[1]+"?"+liveUrl.split("\\?")[2];
					}else{
						liveImg = s.split("=")[1];
					}
					Log.d(TAG, "img:::"+liveImg);
				}
			}
		}
		
		//String webUrl = "http://192.168.1.61:8080/NineCloud/mobileLive.jsp";
		//String webUrl = Constants.hostUrl+"/sharesquare.jsp";
	}
	
	 private int getRespStatus(String url) {   
		 int status = -1;   
		 try {   
		 	HttpHead head = new HttpHead(url);   
		 	HttpClient client = new DefaultHttpClient();   
		 	HttpResponse resp = client.execute(head);   
		 	status = resp.getStatusLine().getStatusCode();   
		 } catch (IOException e) {
			 
		 }   

		 return status;   
	}   

	//全屏
	private FrameLayout video_fullView;// 全屏时视频加载view
	private CustomViewCallback xCustomViewCallback;
	private View xCustomView;
	
	/**
	 * 判断是否是全屏
	 * 
	 * @return
	 */
	public boolean inCustomView() {
		return (xCustomView != null);
	}
	/**
	 * 全屏时按返加键执行退出全屏方法
	 */
	public void hideCustomView() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(bType == 0)
		mMapView.onSaveInstanceState(outState);
	}	
	
	//2015.10.13 李德明 添加，清理webView缓存(怀疑与webView无响应有关)
	 @Override  
	 protected void onDestroy() {  	 
       	 super.onDestroy();
       	 clearVideoAndComentWebView();
       	 video_fullView.removeAllViews();
       	 playLayout.removeView(webView);
       	 playLayout.removeView(commentView);
       	 webView.destroy();
       	 commentView.destroy();
       	if(bType == 0)
       		mMapView.onDestroy();	       	
	 }
	
	public void bindListeners(){
		comment.setOnClickListener(this);
		cancelC.setOnClickListener(this);
		confrimC.setOnClickListener(this);
		collection.setOnClickListener(this);
		squareShare.setOnClickListener(this);
		like.setOnClickListener(this);
		comment.setOnClickListener(this);
		screenshot.setOnClickListener(this);
		commentBar.setOnClickListener(this);
		
		if(bType == 0)
		{
			informationBar.setOnClickListener(this);
		}
		
	}
	
	private void clearVideoAndComentWebView()
	{
		 webView.goBack();
       	 webView.stopLoading();
         webView.clearCache(true);  
         webView.clearFormData();
         webView.clearHistory();
         webView.loadUrl("about:blank");
         commentView.goBack();
         commentView.stopLoading();
         commentView.clearCache(true);
         commentView.clearFormData();
         commentView.clearHistory();
         commentView.loadUrl("about:blank");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(bPopKey == true)
			{
				restoreCommentAndInfoView();
				return false;
			}
			
			if(bSubView == true)
			{
				restoreViewAndMap();
				bSubView = false;
				return false;
			}
			
			if (inCustomView()) {
				hideCustomView();
			
			}else{
				
				this.finish();	
			}
			
			return true;
		}
		
		return false;
	}
	
	private void createMenu(View v){
		PopupMenu menu = new PopupMenu(context);
		//menu.setHeaderTitle("用户菜单");
		menu.setOnItemSelectedListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		menu.add(0, R.string.web_refresh).setParams(webView);
		menu.add(1, R.string.web_copy_link).setParams(webView.getUrl());
		menu.add(2, R.string.web_on_browser).setParams(webView.getUrl());
		menu.show(v);
	}
	
	public  void  editPop()  {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS); //显示软键盘
		Builder  dialog  =  new  AlertDialog.Builder(this);
		LayoutInflater  inflater  =  (LayoutInflater)  this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout  layout  =  (LinearLayout)inflater.inflate(R.layout.edit_dialog_view,  null);
		
		dialog.setView(layout);
		AlertDialog ad = dialog.create();
		WindowManager.LayoutParams lp = ad.getWindow().getAttributes();
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay();
		lp.x = 0;
		lp.y = dmHeight/2 -280;
		lp.width = dmWidth; // 宽度
		ad.getWindow().setAttributes(lp);
		ad.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		//ad.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM); 
		ad.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		ad.show();
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.web_cancel:
			if(bSubView == true)
			{
				restoreViewAndMap();
				bSubView = false;
			}
			else
			{
				this.finish();
			}
			
			break;
		case R.id.web_more:
			createMenu(v);
			break;
		case R.id.square_com:
			//editPop();
			editC.setText("");
			editC.requestFocus();
			editC.setCursorVisible(true);
			resetCommentAndInfoView();
			break;
		case R.id.cancel_com:
			restoreCommentAndInfoView();
			break;
		case R.id.confirm_com:
			restoreCommentAndInfoView();
			sendComMessage();
			break;
		case R.id.collection:
			
			if(false == b_collection)	
			{
				save(R.id.collection);
			}
			else
			{
				cancel(R.id.collection);
			}
			break;
		case R.id.like:
			if(false == b_like)
			{
				save(R.id.like);
			}
			else
			{
				cancel(R.id.like);
			}
			break;
		case R.id.square_share:
			{
				/*//改用新的代码处理 2015.10.14 李德明
				ShareSDK.initSDK(this);				
				OnekeyShare oks = new OnekeyShare();
				oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
				oks.setTitle("蛮牛云视频");
				oks.setText("快乐生活，分享精彩  "+liveUrl);
				//oks.setViewToShare(webView);
				//oks.setUrl(liveUrl);
				oks.show(this);
				squareShare.setImageDrawable(getResources().getDrawable(R.drawable.square_share));
				*/
				
				dwz();
				/*ShareContentCustomizeDemo.showShare( getString(R.string.app_name),context,"家，与我同行",
						"\t\t家，与我同行\r\n[蛮牛云分享视频]\r\n"+"<a href ='"+liveUrl+"'>请点击:"+_title.getText()+"</a>",
						"\t\t家，与我同行\r\n[蛮牛云分享视频]\r\n"+_title.getText()+"\r\n"+liveUrl,
						liveImg,false,null);*/
			}
			break;
		case R.id.previously:
			//screenshot.setImageDrawable(getResources().getDrawable(R.drawable.previously_sel));
			
			//snapshotOperation();

			//startActivity(new Intent(this.getApplicationContext(), LocMapActivity.class));
			break;
		case R.id.comment_bar:
			bComment = true;
			informationPage.setVisibility(View.GONE);
			commentView.setVisibility(View.VISIBLE);
			
			//if(bPopKey == true)
			{
				commentView.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams  linearParams;
				linearParams = (LinearLayout.LayoutParams) commentView.getLayoutParams(); // 取控件webView当前的布局参数		
				linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;// height - 500;// 当前界面高度-320
				commentView.setLayoutParams(linearParams);
			}
			
			if(bType == 0)
			{
				informationBar.setTextColor(Color.parseColor("#ff888888"));
				informationBar.setBackgroundColor(Color.parseColor("#FFFFFF"));
			}
			
			commentBar.setBackgroundColor(Color.parseColor("#66CCFF"));
			commentBar.setTextColor(Color.parseColor("#FFFFFF"));
			
			break;
		case R.id.information_bar:
			bComment = false;
			commentView.setVisibility(View.GONE);
			informationPage.setVisibility(View.VISIBLE);
			//if(bPopKey == true)
			{
				commentContent.setVisibility(View.VISIBLE);
				if(bType == 0)
					mMapView.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); // 取控件webView当前的布局参数		
				linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;// height - 500;// 当前界面高度-320
				informationPage.setLayoutParams(linearParams);
			}
			
			commentBar.setTextColor(Color.parseColor("#ff888888"));
			commentBar.setBackgroundColor(Color.parseColor("#FFFFFF"));
			informationBar.setBackgroundColor(Color.parseColor("#66CCFF"));
			informationBar.setTextColor(Color.parseColor("#FFFFFF"));
			break;
		default:
			break;
		}
	}
	/*
	 *短网址处理
	 * */
	private void dwz(){
		RequestParams params = new RequestParams();
		params.put("url", liveUrl);
		HttpUtil.post("http://dwz.cn/create.php", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d(TAG, "dwz:" + response.toString());
				String short_url = "";
				if(response.has("tinyurl")){
					try {
						short_url = response.getString("tinyurl");
					} catch (JSONException e) {
						e.printStackTrace();
					}

					//String imgPath = liveImg.replace("man-niu.oss-cn-hangzhou.aliyuncs.com/", "oss.mny9.com/");//设备封面的地址处理
					String imgPath = "http://www.9wingo.com/images/WechatMomentsqrcode.png";//默认分享图地址
					Log.d(TAG, "image path:" + imgPath);
					//String strText= "\t\t"+getString(R.string.famliy_around_withme)+"\r\n["+getString(R.string.app_share_video)+"]\r\n<a href ='"+liveUrl+"'>"+getString(R.string.Click_toplay)+":"+_title.getText()+"</a>";
					String strText= "\t\t"+getString(R.string.famliy_around_withme)+"\r\n["+getString(R.string.app_share_video)+"]\r\n"+_title.getText()+"\r\n请点击:"+short_url;//2015.11.01 李德明修改
					String strPYQText=  "\t\t"+getString(R.string.famliy_around_withme)+"\r\n["+getString(R.string.app_share_video)+"]\r\n"+_title.getText()+"\r\n"+short_url;					
					
					ShareContentCustomizeDemo.showShare( getString(R.string.app_name),context,getString(R.string.famliy_around_withme),
							strText,
							strPYQText,
							imgPath
							,false,null);
				}
				
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
			}
		});
		
	}
	
	
	
	private void getSurrroundGp(){
		RequestParams params = new RequestParams();
		params.put("deviceId", liveId);
		HttpUtil.get(Constants.hostUrl+ "/mobile/getSurrounGp", params, new JsonHttpResponseHandler(){
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						String result = null;
						if(json.has("data"))
						{
							bHave = false;
						}
						else
						{
							 posArray = json.getJSONArray("success");
						}
						
						 Message message=new Message();
						 message.what= 1;
						 handler.sendMessage(message);//发送message信息 
						 
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.GET_C_FAILURE));
			}
		});
	}
	
	
	private void getCommentNum(){
		RequestParams params = new RequestParams();
		params.put("liveid", liveId);
		HttpUtil.get(Constants.hostUrl+ "/LiveAction_getLiveCWNum", params, new JsonHttpResponseHandler(){
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						String result = json.get("commentNum").toString();
						 commentNum = (Integer) json.get("commentNum");
						 commentBar.setText("评论("+commentNum+")");
						 if(json.length() == 3)
						 { 
							 bConfig = false;
						 }
						 else{
							 if(bType == 0){ 
								 devLng = Float.parseFloat(json.get("longitude").toString());
								 devLat = Float.parseFloat(json.get("latitude").toString());
								// APP.ShowToast(Float.toString(devLat));
							 }
						 }
						 
						 Message message=new Message();
						 message.what= 0;
						 handler.sendMessage(message);//发送message信息 
						 
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.GET_C_FAILURE));
			}
		});
	}
	
	private void getDeviceInfo(){
		RequestParams params = new RequestParams();
		params.put("liveid", liveId);
		HttpUtil.get(Constants.hostUrl+ "/LiveAction_vInfomation", params, new JsonHttpResponseHandler(){
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {	
						String title = json.get("livename").toString();
						String content = json.get("liveduction").toString();
						String address = json.get("liveaddress").toString();
						String username = json.get("username").toString();
						String contentText;
						
						if(title == "null")
						 commentTitle.setText("杭州蛮牛云视频");
						else
							commentTitle.setText(title);
						
						if(content == "null")
							contentText ="介绍:" + "好视频，一辈子";
						else
							contentText ="介绍:" + content;

						if(address == "null")
							contentText = contentText+"\n\n地址:" + "杭州荆长大道" ;
						else
							contentText = contentText+"\n\n地址:" + address;
						
						if(username == "null")
							contentText = contentText + "\n\n发布者:" + "李大帅";
						else
							contentText = contentText + "\n\n发布者:" + username;
						
						commentContent.setText(contentText);
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.GET_C_FAILURE));
			}
		});
	}
	
	public void initPraiseAndCollect()
	{
		if(true == SetSharePrefer.read_bool("collect_info", liveId))
		{
			collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
		}
			
		if(true == SetSharePrefer.read_bool("praise_info", liveId))
		{
			like.setImageDrawable(getResources().getDrawable(R.drawable.like_sel));
		}
	}
	
	/**保存收藏信息,成功返回后切换按钮背景图片*/
	public void save(int id){
		final String save_failure = getResources().getString(R.string.Err_CONNET);
		final String collect_success = getResources().getString(R.string.SUCCESS_COLLECT);
		final String like_success = getResources().getString(R.string.SUCCESS_LIKE);
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
		params.put("liveid", liveId);
		
		switch(id)
		{
		case R.id.collection:
			HttpUtil.get(Constants.hostUrl+"/android/saveCollect", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							Log.v(TAG, result);
							if("true".equals(result)){
								APP.ShowToast(collect_success);
								SetSharePrefer.write_bool("collect_info", liveId, true);
								collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
								b_collection = true;	
								//TODO
							}else{
								APP.ShowToast(save_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(save_failure);
				}
			});
			break;
			
		case R.id.like:
			HttpUtil.get(Constants.hostUrl+"/android/savePraise", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							Log.v(TAG, result);
							if("true".equals(result)){
								APP.ShowToast(like_success);
								SetSharePrefer.write_bool("praise_info", liveId, true);
								like.setImageDrawable(getResources().getDrawable(R.drawable.like_sel));
								b_like = true;	
								//TODO
							}else{
								APP.ShowToast("点赞失败!");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(save_failure);
				}
				
			});	
			break;
		default:
			break;
		}
		
	}
	
	public void cancel(int id){
		final String cancel_failure = getResources().getString(R.string.Err_CONNET);
		final String collect_success = getResources().getString(R.string.CANCEL_COLLECT);
		final String like_success = getResources().getString(R.string.CANCEL_LIKE);
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
		params.put("liveid", liveId);
		switch(id)
		{
		case R.id.collection:
			
			HttpUtil.get(Constants.hostUrl+"/android/cancelCollect", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							if("true".equals(result)){
								APP.ShowToast(collect_success);
								SetSharePrefer.write_bool("collect_info", liveId, false);
								collection.setImageDrawable(getResources().getDrawable(R.drawable.collection));
								b_collection = false;
								//TODO
							}else{
								APP.ShowToast(cancel_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(cancel_failure);
				}
			});
			break;
		case R.id.like:
			HttpUtil.get(Constants.hostUrl+"/android/cancelPraise", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							if("true".equals(result)){
								APP.ShowToast(like_success);
								SetSharePrefer.write_bool("praise_info", liveId, false);
								like.setImageDrawable(getResources().getDrawable(R.drawable.like));
								b_like = false;
								//TODO
							}else{
								APP.ShowToast(cancel_failure);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(cancel_failure);
				}
			});
			break;
		default:
			break;
		}
		
	}
	
	public  String getJsonStr(String str){
		try{
			JSONObject obj = new JSONObject();
			
			obj.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
			obj.put("liveId", liveId);
			obj.put("position", "约300米");
			obj.put("content", str);
			if(bType == 0)
			{
				obj.put("topictype", 0);
			}
			else
			{
				obj.put("topictype", 1);
			}
			return obj.toString();
		} catch( JSONException e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void sendComMessage(){
		//评论提交成功之后清空comEdit的内容
		String str = getJsonStr(editC.getText().toString());
		commentView.loadUrl("javascript:InsertComment('" + str + "')" );
		commentNum = commentNum +1;
		commentBar.setText("评论("+commentNum+")");
		
		editC.setText("");
	}
	
	public final class transUserId{
		public transUserId(){
			
		}
		 @JavascriptInterface
		public String getUserId(){
			return APP.GetSharedPreferences("Info_Login", "sid", "");
		}
	}
	
    /**
     * 截取webView可视区域的截图
     * @param webView 前提：WebView要设置webView.setDrawingCacheEnabled(true);
     * @return
     */
	private Bitmap captureWebViewVisibleSize(WebView webView){
		
		webView.buildDrawingCache();
		Bitmap bmp = webView.getDrawingCache();
		
		return bmp;
	}
    
	/**
	 * 回收图片
	 */
	public void destoryBitmap(){
	    if((null != bmp) && (!bmp.isRecycled())){
	    	bmp.recycle();  
	    	System.out.println("回收图片！");
	    }
	    	
	}
	
	public class FileService {
		/**声明上下文*/
		private Context context;
		/**文件夹名字*/
		private static final String FOLDER_NAME = "/manniu/snapshot";
		
		private static final String TAG = "FileService";
		
		// 构造函数
		public FileService(Context context) {
			this.context = context;
		}

		/**
		 * 保存bitmap到文件
		 * @param filename
		 * @param bmp
		 * @return
		 */
		public String saveBitmapToSDCard(String filename, Bitmap bmp) {
			
			// 文件相对路径
			String fileName = null;
			if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				// 文件保存的路径
				String fileDir = Environment.getExternalStorageDirectory() + FOLDER_NAME;
				
				// 如果文件夹不存在，创建文件夹
				if (!createDir(fileDir)) {
					Log.e(TAG, "创建文件夹失败!");
				}
				// 声明文件对象
				File file = null;
				// 声明输出流
				FileOutputStream outStream = null;
				
				try {
					// 如果有目标文件，直接获得文件对象，否则创建一个以filename为名称的文件
					file = new File(fileDir, filename);
					
					// 获得文件相对路径
					fileName = file.toString();
					// 获得输出流，如果文件中有内容，追加内容
					outStream = new FileOutputStream(fileName);
					if(outStream != null)
	                {
	                    bmp.compress(Bitmap.CompressFormat.PNG, 90, outStream);
	                    outStream.close();
	                }
					
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}finally{
					// 关闭流
					try {
						if (outStream != null) {
							outStream.close();
						}
					} catch (IOException e) {
						Log.e(TAG, e.toString());
					}
				}
			}
			return fileName;
		}
		
		/**
		 * 创建指定路径的文件夹，并返回执行情况 ture or false
		 * @param filePath
		 * @return
		 */
		public boolean createDir(String filePath) {
			File fileDir = new File(filePath); // 生成文件流对象
			boolean bRet = true;
			// 如果文件不存在，创建文件
			if (!fileDir.exists()) {
				// 获得文件或文件夹名称
				String[] aDirs = filePath.split("/");
				StringBuffer strDir = new StringBuffer();
				for (int i = 0; i < aDirs.length; i++) {
					// 获得文件上一级文件夹
					fileDir = new File(strDir.append("/").append(aDirs[i]).toString());
					// 是否存在
					if (!fileDir.exists()) {
						// 不存在创建文件失败返回FALSE
						if (!fileDir.mkdir()) {
							bRet = false;
							break;
						}
					}
				}
			}

			return bRet;
		}

	}
}
