package com.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.adapter.HttpUtil;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.AMap.OnMapLoadedListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
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
public class NewWebActivity extends Activity implements OnClickListener, OnMapLoadedListener, OnMapClickListener, InfoWindowAdapter, OnMarkerClickListener{
	
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
	
	Context context, context1;
	
	ImageView collection;
	
	ImageView like;
	
	ImageView squareShare;
	
	ImageView screenshot;
	
	Button comment;
	Button refresh;
	
	private String liveName;
	String liveId;
	String liveUrl;
	String liveImg;

	//add by zra
	LinearLayout playLayout;
	
	//add by zra
	WebView  commentView;
	LinearLayout commentTools;
	TextView commentBar;
	TextView informationBar;
	LinearLayout  informationPage;
	
	//add by zra
	TextView commentTitle;
	TextView commentContent;
	TextView commentProfile;
	TextView commentAddress;
	TextView commentPublisher;
	boolean bComment = true;
	int dmHeight;
	int dmWidth;
	int commentNum;
	boolean b_like = false;
	boolean b_collection = false;
	
	//soft keyboard operation
	private int count =0;
	private boolean bGetBoardHeight = false;
	private int iBoardHeight;
	private boolean bFinish = true;
	private static long oldTime;
	boolean bPopKey;
	
	//map operation
	public  MapView mMapView;
	public   AMap mAMap;
	private boolean   bSubView = false;
	private int bType;
	private boolean bHave = true;
	private boolean bConfig = true;
	private float devLng;
	private float devLat;
	private JSONArray posArray;
	private Handler  handler;
	
	//string resources
	private String sCommentTip ;
	private String sProfileTip;
	private String sDefaultAddr;
	private String sAddressPre ;
	private String sPublisherPre ;
	private String sDefaultPublisher;
	private String sProfilePre ;
	private String sDefaultProfile ;
	private String sDefaultDevTitle ;
	private String sCancelCollectFailure ;
	private String sCancelCollectSuccess;
	private String sCancelLikeSuccess ;
	private String sCancelLikeFailure;
	private String sAddCollectFailure;
	private String sAddCollectSuccess;
	private String sAddLikeSuccess;
	private String sAddLikeFailure;
	//http request count
	private int getLatLngCount = 0;
	private int getCommentNumCount = 0;
	private int getDeviceInfoCount = 0;
	private int savePraiseCount = 0;
	private int saveCollectCount = 0;
	private int cancelPraiseCount = 0;
	private int cancelCollectCount = 0;
	
	private PopupWindow popupWindow;
	View popupWindow_view;
	private BitmapDescriptor bitmapOtherDes;
	private BitmapDescriptor bitmapMeDes;
	private  boolean bCommentClickable = false;
	
	private Marker mMarkerMe;
	private boolean isResume = false;
	private boolean mIsShareFinish = true;
	private int countShare = 0;
	
	@SuppressLint({ "JavascriptInterface", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context1 = this;
		
		globalParamsInit();
		mapViewInit(savedInstanceState);
		commentTitle = (TextView)findViewById(R.id.comment_title);
		commentContent = (TextView)findViewById(R.id.comment_content);
		
		if(bType == 0)
			getDeviceInfo();
		else
			getCommentNum();
		
		topViewInit();
		popupWindowInit();
		commentViewAndSubViewInit();
		praiseAndCollectViewInit();
		playViewInit();
		bindListeners();
		rootViewInit();
	}
	
	private void globalParamsInit()
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		NewMain._isOpen = true;
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		context = this.getApplicationContext();
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		
		dmHeight =dm.heightPixels;
		dmWidth = dm.widthPixels;
		sCommentTip = getResources().getString(R.string.comment_tip);
		sProfileTip = getResources().getString(R.string.profile_tip);
		sDefaultAddr = getResources().getString(R.string.default_address);
		sAddressPre = getResources().getString(R.string.address_pre);
		sPublisherPre = getResources().getString(R.string.publisher_pre);
		sDefaultPublisher = getResources().getString(R.string.default_publisher);
		sProfilePre = getResources().getString(R.string.profile_pre);
		sDefaultProfile = getResources().getString(R.string.default_profile);
		sDefaultDevTitle = getResources().getString(R.string.default_dev_title);
		parseURL();
		setContentView(R.layout.new_web_activity);
		
		if(null == getIntent().getStringExtra("playType"))
		{
			bType = 0;
		}
		
		bType = Integer.parseInt(getIntent().getStringExtra("playType"));
		
		sCancelCollectFailure = getResources().getString(R.string.FAIL_CANCEL_COLLECT);
		sCancelCollectSuccess = getResources().getString(R.string.CANCEL_COLLECT);
		sCancelLikeSuccess = getResources().getString(R.string.CANCEL_LIKE);
		sCancelCollectFailure = getResources().getString(R.string.FAIL_CANCEL_LIKE);
		
		sAddLikeFailure = getResources().getString(R.string.FAIL_ADD_LIKE);
		sAddCollectFailure = getResources().getString(R.string.FAIL_ADD_COLLECT);
		sAddCollectSuccess = getResources().getString(R.string.SUCCESS_COLLECT);
		sAddLikeSuccess = getResources().getString(R.string.SUCCESS_LIKE);
	}
	
	private void topViewInit()
	{
		_titleFrm = (FrameLayout) findViewById(R.id.webTitle);
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) _titleFrm.getLayoutParams(); 
		linearParams.height = dmHeight*3/40;
		_title = (TextView)findViewById(R.id.web_title);
		_cancel = (XImageBtn)findViewById(R.id.web_cancel);
		_more = (XImageBtn)findViewById(R.id.web_more);
		progressBar = (ProgressBar)findViewById(R.id.progressBar2);
	}
	
	/*
	 * 功能:根布局 root view初始化:
	 * 1.支持google键盘取消键事件监听
	 * 2.支持用户输入后，再取消事件监听
	 * 3.支持android弹出软键盘，重新布局事件过滤
	 */
	private void rootViewInit()
	{
		playLayout = (LinearLayout)findViewById(R.id.play_layout);
		playLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	        	
	        	Rect r = new Rect();
        		playLayout.getWindowVisibleDisplayFrame(r);
	            int screenHeight = playLayout.getRootView().getHeight();
	            
	            if(mIsShareFinish == false)
	            {
	            	if(countShare == 0)
	            	{
	            		countShare ++;
	            	}
	            	else
	            	{
	            		countShare =0;
	            		mIsShareFinish = true;
	            	}
	            	
	            }
	            
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
	
	/*功能:播放页面webView ，全屏view初始化:
	 * 1.支持全屏功能，
	 * 2.支持进度显示
	 * 3.支持刷新加载
	 * 4.支持断网错误页面加载
	*/
	private void playViewInit()
	{
		video_fullView = (FrameLayout) findViewById(R.id.video_fullView);
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) video_fullView.getLayoutParams(); 
		linearParams.height = dmWidth;
		linearParams.width = dmHeight;
		video_fullView.setLayoutParams(linearParams);
		
		videoWebViewInit(dmHeight);
		
		refresh = (Button) findViewById(R.id.refresh_web1);
		refresh.setVisibility(View.GONE);
		
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				webView.loadUrl(liveUrl);
			}
		});
		
		webView.loadUrl(liveUrl);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				  if(url.startsWith("http://") && getRespStatus(url)==404) {	
					  
					  	view.stopLoading();
						view.clearView();
						String data = "NET DISCONNECT, PAGE NO FOUND";
						view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
						NewWebActivity.this.finish();
				   } else {  
				     	view.loadUrl(url);  
				   }
				  
				   return true;
			}   
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				view.stopLoading();
				view.clearView();
				String data = "NET DISCONNECT, PAGE NO FOUND";
				view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
				NewWebActivity.this.finish();
			}
			
		/*	@Override
			public void onPageFinished(WebView view, String url) { 			
				view.loadUrl("javascript:(function() { " +
						"var videos = document.getElementsByTagName('video'); " +
						"for(var i=0;i<videos.length;i++){" +
							"videos[i].play();" +
						"}" +
				"})()"); 
			}*/
		});

		webView.setWebChromeClient(new WebChromeClient(){
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
			
			@Override
			public void onHideCustomView() {
				
				if (xCustomView == null)
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
	}
	
	/*
	 * 功能:创建popupwindow,帮助显示软键盘。
	 * */
	protected void popupWindowInit() {
		popupWindow_view = getLayoutInflater().inflate(R.layout.pop_up_window, null, false);
		popupWindow_view.setVisibility(View.GONE);
		cancelC = (ImageView) popupWindow_view.findViewById(R.id.cancel_com);
		editC = (EditTextPreIme) popupWindow_view.findViewById(R.id.edit_com);
		confrimC = (ImageView) popupWindow_view.findViewById(R.id.confirm_com);
		comEdit =(LinearLayout)popupWindow_view.findViewById(R.id.share_comment);	
		
		popupWindow = new PopupWindow(popupWindow_view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true); 
		popupWindow.setFocusable(true);
		popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
		popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		popupWindow_view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (popupWindow != null && popupWindow.isShowing()) {
					//popupWindow.dismiss();
				}
				return false;
			}
		});
	}
	
	/*
	 * 功能:软键盘弹出底层view重新布局。
	 */
	private void resetCommentAndInfoView(View v)
	{  
		popupWindow_view.setVisibility(View.VISIBLE);
		editC.setText("");
		editC.requestFocus();
		editC.setCursorVisible(true); 
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);	
		popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
		
		if(bType == 0)
			informationBar.setClickable(false);
		commentBar.setClickable(false);
		
		bPopKey = true;
		bFinish = false;
	}

	/*
	 * 功能:软键盘弹出底层view布局恢复*/
	private void restoreCommentAndInfoView()
	{
		popupWindow_view.setVisibility(View.GONE);
		imm.hideSoftInputFromWindow(comEdit.getWindowToken(), 0);
		popupWindow.dismiss();
		if(bType == 0)
			informationBar.setClickable(true);
		commentBar.setClickable(true);
		bPopKey = false;
		count = 0;
		bFinish = true;
	}
	
	private void videoWebViewInit(int height)
	{
		webView = (WebView)findViewById(R.id.webView02);
		if(android.os.Build.VERSION.SDK_INT > 16)
			webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
		WebSettings ws = webView.getSettings();
		ws.setBuiltInZoomControls(true);
		ws.setUseWideViewPort(true);
		ws.setLoadWithOverviewMode(true);
		ws.setSaveFormData(true);
		ws.setJavaScriptEnabled(true);
		ws.setGeolocationEnabled(true);
		ws.setDomStorageEnabled(true);
		ws.setSupportMultipleWindows(true);
		ws.setSupportZoom(true);
		 
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) webView.getLayoutParams();	
		linearParams.height = height*7/20-height/80;
		webView.setLayoutParams(linearParams); 
		
		
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
	}
	
	/*
	 * 功能:评论view和相关子view初始化：
	 * 1.评论和地图切换工具条初始化
	 * 2.评论工具条初始化
	 * 3.评论页面初始化
	 * 4.软键盘done事件监听
	 * */
	private void commentViewAndSubViewInit()
	{		
		squareTools = (LinearLayout)findViewById(R.id.square_tools);
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) squareTools.getLayoutParams(); 
		linearParams.height = dmHeight*3/40;
		squareTools.setLayoutParams(linearParams); 
		comment = (Button)findViewById(R.id.square_com);

		commentTools = (LinearLayout)findViewById(R.id.comment_tools);
		linearParams = (LinearLayout.LayoutParams) commentTools.getLayoutParams();
		linearParams.height = dmHeight/20;
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
		
		linearParams = (LinearLayout.LayoutParams) commentView.getLayoutParams();
		linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;
		commentView.setLayoutParams(linearParams); 
		
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
				   return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) 
			{	
				super.onPageFinished(view, url);
				
				if(bType == 0)
					commentView.loadUrl("javascript:setLiveId('" + liveId + "'," + 0 + ")" );
				else
					commentView.loadUrl("javascript:setLiveId('" + liveId + "'," + 1 + ")" );
				
				bCommentClickable = true;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				view.stopLoading();
				view.clearView();
				String data = "NET DISCONNECT, PAGE NO FOUND";
				view.loadUrl("javascript:document.body.innerHTML=\"" + data + "\"");
				NewWebActivity.this.finish();
			}
		});
		
		commentView.setWebChromeClient(new WebChromeClient(){
			
			@Override
			public void onProgressChanged(WebView view, int newProgress){
				NewWebActivity.this.setProgress(newProgress *100);
			}
		});
		
		informationPage = (LinearLayout)findViewById(R.id.information_page);
		linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams();
		linearParams.height = dmHeight/2 - dmHeight/20 +dmHeight/40+dmHeight/80;
		informationPage.setLayoutParams(linearParams);
		
		editC.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					restoreCommentAndInfoView();
					sendComMessage();
				}
				
				return false;
			}
		});
	}


	@SuppressLint("HandlerLeak")
	private void mapViewInit(Bundle savedInstanceState)
	{	
		handler = new Handler(){
			@Override
	    	public void handleMessage(Message msg) {
	    		// TODO Auto-generated method stub
	    		super.handleMessage(msg);
	    		switch(msg.what)
	    		{
	    		case 0:
	    			initAMapAndMovePosition();
	    			getSurrroundDeviceLatLng();
	    			break;
	    		case 1:
	    			initDevLatLngAndShowDevMarker();	
	    			break;
	    		case 2:
	    			getCommentNum();
	    			break;
	    		case 3:
	    			getSurrroundDeviceLatLng();
	    			break;
	    		default:
	    			break;
	    		}
	    		
	    	}
		};
		
		try{
				
			mMapView = (MapView) findViewById(R.id.comment_map);
			mMapView.onCreate(savedInstanceState);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void initAMapAndMovePosition()
	{
		mAMap = mMapView.getMap();
		mAMap.setOnMapClickListener(this);
		mAMap.setOnMapLoadedListener(this);
		mAMap.setInfoWindowAdapter(this);
		mAMap.setOnMarkerClickListener(this);
		UiSettings uiSettings = mAMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setScaleControlsEnabled(false);
		uiSettings.setAllGesturesEnabled(false);
		uiSettings.setZoomGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		
		bitmapOtherDes = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
		bitmapMeDes = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
		
		LatLngBounds bounds= null;
		if(bConfig == false)
		{
			bounds = new LatLngBounds.Builder().include(new LatLng(30.297233, 120.047253)).build();	
		}
		else{
			bounds = new LatLngBounds.Builder().include(new LatLng(devLat, devLng)).build();	
		}
		
		if(bConfig == false)
		{
			ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
			giflist.add(bitmapOtherDes);
			giflist.add(bitmapMeDes);
			MarkerOptions ooD = new MarkerOptions().position(new LatLng(30.297233, 120.047253)).title(getResources().getString(R.string.default_dev_title)).icons(giflist).draggable(true).period(3);
			mMarkerMe = mAMap.addMarker(ooD);
			mMarkerMe.showInfoWindow();
		}
		else 
		{
			ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
			giflist.add(bitmapOtherDes);
			giflist.add(bitmapMeDes);
			MarkerOptions ooD = new MarkerOptions().position(new LatLng(devLat, devLng)).title(liveName).icons(giflist).draggable(true).period(3);
			mMarkerMe = mAMap.addMarker(ooD);
			mMarkerMe.showInfoWindow();
		}	
		
		mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
		mAMap.moveCamera(CameraUpdateFactory.zoomTo(10));
	}
	
	public void initDevLatLngAndShowDevMarker()
	{
		try{	
			MarkerOptions markerOption;
			
			// add marker overlay
			if(bHave == false)
			{
				LatLng llB = new LatLng(30.287233, 120.147253);
				LatLng llC = new LatLng(30.27233, 120.247253);  
				LatLng llA = new LatLng(30.267233, 120.347253);
				
				markerOption = new MarkerOptions();
				markerOption.position(llA);
				markerOption.title(getResources().getString(R.string.default_dev_title));
				markerOption.icon(bitmapOtherDes);
				mAMap.addMarker(markerOption);
				
				
				markerOption = new MarkerOptions();
				markerOption.position(llB);
				markerOption.title(getResources().getString(R.string.default_dev_title));
				markerOption.icon(bitmapOtherDes);
				mAMap.addMarker(markerOption);
				
				markerOption = new MarkerOptions();
				markerOption.position(llC);
				markerOption.title(getResources().getString(R.string.default_dev_title));
				markerOption.icon(bitmapOtherDes);
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
					markerOption.title(jpos.get("devname").toString());
					markerOption.icon(bitmapOtherDes);
					mAMap.addMarker(markerOption);
				}
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
	
	
	@Override
	public void onMapLoaded() {
		return;
	}
	
	/*功能:点击小地图，重新布局view弹出大地图，并设置大地图相关属性*/
	private void resetViewAndMap()
	{
		try{	
			webView.setVisibility(View.GONE);
			commentTools.setVisibility(View.GONE);
			squareTools.setVisibility(View.GONE);
			commentTitle.setVisibility(View.GONE);
			commentContent.setVisibility(View.GONE);
			LinearLayout.LayoutParams  linearParams;
			linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); 	
			linearParams.height = dmHeight- dmHeight*3/40;
			informationPage.setLayoutParams(linearParams);
	       
			mAMap.moveCamera(CameraUpdateFactory.zoomTo(10));
			UiSettings uiSettings = mAMap.getUiSettings();
			uiSettings.setCompassEnabled(true);
			uiSettings.setZoomControlsEnabled(true);
			uiSettings.setScaleControlsEnabled(true);
			uiSettings.setAllGesturesEnabled(true);
			uiSettings.setZoomGesturesEnabled(true);
			uiSettings.setZoomControlsEnabled(true);
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
		LinearLayout.LayoutParams  linearParams;
		linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams(); 	
		linearParams.height = dmHeight/2 - dmHeight/20 +dmHeight/40+dmHeight/80;
		informationPage.setLayoutParams(linearParams);

		mAMap.moveCamera(CameraUpdateFactory.zoomTo(10));
		UiSettings uiSettings = mAMap.getUiSettings();
		uiSettings.setCompassEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		uiSettings.setScaleControlsEnabled(false);
		uiSettings.setAllGesturesEnabled(false);
		uiSettings.setZoomGesturesEnabled(false);
		uiSettings.setZoomControlsEnabled(false);
		mAMap.setOnMapClickListener(this);
		
		List<Marker> saveMarkerList = mAMap.getMapScreenMarkers();
		if (saveMarkerList == null || saveMarkerList.size() <= 0)
		{
			return;
		}
		
		for (Marker marker : saveMarkerList) {
			if(marker != mMarkerMe)
			{
				marker.hideInfoWindow();
			}
		}
		
		mMarkerMe.showInfoWindow();
	}
	
	@Override
    public boolean onMarkerClick(final Marker marker) {
            
		if(bSubView == false)
		{
			return true;
		}
			
		return false;
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
		playPause(1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(isResume){
			isResume = false;
		}
		if(bType == 0)
			mMapView.onResume();
	}
	
	//暂停/恢复
	public void playPause(int bPause){
		if(bPause == 0){
		}
		if(bPause == 1){
			if(bPopKey == true){
				restoreCommentAndInfoView();
			}
			if(bSubView == true){
				restoreViewAndMap();
				bSubView = false;
			}
			if (inCustomView()) {
				hideCustomView();
			}else{
				if(mIsShareFinish == true)
				{
					this.finish();
				}
			}
			isResume = true;
		}
	}
	
	private void parseURL()
	{
		
		liveUrl = getIntent().getStringExtra("url");
		//liveUrl = "http://10.12.6.116:80/live_video.html";
		//liveUrl = "http://10.12.6.121:8020/Video/video.html";
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

	private FrameLayout video_fullView;
	private CustomViewCallback xCustomViewCallback;
	private View xCustomView;
	
	public boolean inCustomView() {
		return (xCustomView != null);
	}

	public void hideCustomView() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(bType == 0)
		mMapView.onSaveInstanceState(outState);
	}	
	
	@Override
	protected void onStop() {
		Squ_LiveActivity.bLiveStart = false;//广场视频点击标志位
		super.onStop();
	}
	
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
       	 {
        	 mMapView.onDestroy();	
        	 if(bitmapOtherDes != null)
        		 bitmapOtherDes.recycle();
        	 if(bitmapMeDes != null)
        		 bitmapMeDes.recycle();
       	 }
       	 	
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
				webView.loadUrl("about:blank");
				this.finish();	
			}
			
			return true;
		}
		
		return false;
	}
	
	private void createMenu(View v){
		PopupMenu menu = new PopupMenu(context);
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
				webView.loadUrl("about:blank");
				this.finish();
			}
			
			break;
		case R.id.web_more:
			createMenu(v);
			break;
		case R.id.square_com:
			
			if(bCommentClickable == false)
			{
				return;
			}
			
			resetCommentAndInfoView(v);
			break;
		case R.id.cancel_com:
			restoreCommentAndInfoView();
			break;
		case R.id.confirm_com:
			restoreCommentAndInfoView();
			sendComMessage();
			break;
		case R.id.collection://点击收藏
			if(true == SetSharePrefer.read_bool("collect_info", liveId)){
				APP.ShowToast(getString(R.string.ALREADY_COLLECT));
				return;
			} 
			if(false == b_collection)	
			{
				LogUtil.d(TAG, "collection button action save()!");
				save(R.id.collection);
			}
			else
			{
				LogUtil.d(TAG, "collection button action cancel()!");
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
				dwz();
			}
			break;
		case R.id.previously:
			//screenshot.setImageDrawable(getResources().getDrawable(R.drawable.previously_sel));
			break;
		case R.id.comment_bar:
			bComment = true;
			informationPage.setVisibility(View.GONE);
			commentView.setVisibility(View.VISIBLE);
			
			//if(bPopKey == true)
			{
				commentView.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams  linearParams;
				linearParams = (LinearLayout.LayoutParams) commentView.getLayoutParams(); 	
				linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;
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
				{
					mMapView.setVisibility(View.VISIBLE);
				}
				
				LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) informationPage.getLayoutParams();
				linearParams.height = dmHeight/2 -dmHeight/20+dmHeight/40+dmHeight/80;
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

	//分享到朋友圈
	private void dwz(){
		if(mIsShareFinish == false){
			return;
		}
		mIsShareFinish = false;
		
		RequestParams params = new RequestParams();
		params.put("url", liveUrl);
		

		String imgPath = "http://www.9wingo.com/images/WechatMomentsqrcode.png";
		if(liveImg != null && liveImg.isEmpty()==false && liveImg.length()>0)
		{
			imgPath = liveImg.replace("man-niu.oss-cn-hangzhou.aliyuncs.com/", "oss.mny9.com/");
		}
		Log.d(TAG, "image path:" + imgPath);
		String strText= _title.getText()+"";					
		String strPYQText=  _title.getText()+"";
		String strURL= liveUrl+"&lc.type=2";
		
		String strTitle = "【"+getString(R.string.app_name)+"】";
		
		ShareContentCustomizeDemo.showShare( getString(R.string.app_name),context1,strTitle,
				strText,
				strPYQText,
				imgPath,
				strURL,
				false,null);
	}

	private void getSurrroundDeviceLatLng(){
		
		RequestParams params = new RequestParams();
		params.put("deviceId", liveId);
		//Constants.hostUrl = "http://10.12.6.121:8080/NineCloud/";
		HttpUtil.get( Constants.hostUrl+ "/mobile/getSurrounGp", params, new JsonHttpResponseHandler(){
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						
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
						 handler.sendMessage(message);
						 
						 getLatLngCount = 0;
						 
					} catch (JSONException e) {
						e.printStackTrace();
						getSurrroundDeviceLatLngFailAndExceptionProc();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				getSurrroundDeviceLatLngFailAndExceptionProc();
				
			}
		});
	}
	
	public void getSurrroundDeviceLatLngFailAndExceptionProc()
	{
		if(getLatLngCount <3)
		{
			getSurrroundDeviceLatLng();
			getLatLngCount ++;
		}else{
			getLatLngCount = 0;
			APP.ShowToast(getResources().getString(R.string.GET_P_FAILURE));
			NewWebActivity.this.finish();
		}
	}
	
	private void getCommentNum(){
		
		RequestParams params = new RequestParams();
		params.put("liveid", liveId);
		
		if(bType == 0)
			params.put("type", 0);
		else
			params.put("type", 1);
		
		HttpUtil.get(Constants.hostUrl+ "/LiveAction_getLiveCWNum", params, new JsonHttpResponseHandler(){
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						 commentNum = (Integer) json.get("commentNum");
						 commentBar.setText( sCommentTip +"("+commentNum+")");
						 
						 if(json.length() == 3)
						 { 
							 bConfig = false;
							 
						 }else{
							 
							 if(bType == 0){ 
								 devLng = Float.parseFloat(json.get("longitude").toString());
								 devLat = Float.parseFloat(json.get("latitude").toString());
							 }
						 }
						 
						if(bType == 0)
						{
							 Message message=new Message();
							 message.what= 0;
							 handler.sendMessage(message);
						}
						
						getCommentNumCount = 0;
						
					} catch (JSONException e) {
						e.printStackTrace();
						getCommentNumFailAndExceptionProc();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				getCommentNumFailAndExceptionProc();
			}
		});
	}
	
	private void getCommentNumFailAndExceptionProc()
	{
		if(getCommentNumCount <3)
		{
			getCommentNum();
			getCommentNumCount ++;
		}else{
			getCommentNumCount = 0;
			if(bType == 0)
				APP.ShowToast(getResources().getString(R.string.GET_C_FAILURE));
			else
				APP.ShowToast(getResources().getString(R.string.Err_NetConnect));
			NewWebActivity.this.finish();
		}
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
						liveName = title;
						String content = json.get("liveduction").toString();
						String address = json.get("liveaddress").toString();
						String username = json.get("username").toString();
						String contentText;
						
						if(title == "null")
							 commentTitle.setText(sDefaultDevTitle);
						else
							commentTitle.setText(title);
						
						if(content == "null")
							contentText = "\n"+sProfilePre +":" + sDefaultProfile ;
						else
							contentText = "\n"+sProfilePre +":" + content ;

						if(address == "null")
							contentText = contentText+"\n\n"+sAddressPre+":" + sDefaultAddr;
						else
							contentText = contentText+"\n\n"+ sAddressPre + ":"+ address;
						
						if(username == "null")
							contentText = contentText + "\n\n"+ sPublisherPre+ ":"+ sDefaultPublisher;
						else
							contentText = contentText + "\n\n"+ sPublisherPre + ":"+ username;
							
							commentContent.setText(contentText);
						
						 Message message=new Message();
						 message.what= 2;
						 handler.sendMessage(message);
						 
						 getDeviceInfoCount = 0;
						
					} catch (JSONException e) {
						e.printStackTrace();
						getDeviceInfoFailAndExceptionProc();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				
				getDeviceInfoFailAndExceptionProc();
			}
		});
	}
	
	
	public void getDeviceInfoFailAndExceptionProc()
	{

		if(getDeviceInfoCount <3)
		{
			getDeviceInfoCount ++;
			getDeviceInfo();
			
		}else{
			getDeviceInfoCount = 0;
			//APP.ShowToast(getResources().getString(R.string.GET_C_FAILURE));
			APP.ShowToast(getResources().getString(R.string.Err_NetConnect));
			NewWebActivity.this.finish();
		}
	}
	
	public void praiseAndCollectViewInit()
	{
		collection = (ImageView)findViewById(R.id.collection);
		like = (ImageView)findViewById(R.id.like);
		squareShare = (ImageView)findViewById(R.id.square_share);
		screenshot = (ImageView)findViewById(R.id.previously);
		if(true == SetSharePrefer.read_bool("collect_info", liveId))
		{
			collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
		}
			
		if(true == SetSharePrefer.read_bool("praise_info", liveId))
		{
			like.setImageDrawable(getResources().getDrawable(R.drawable.like_sel));
		}
	}

	public void save(int id){
//		if(collection.getd){
//			
//		}
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
		params.put("liveid", liveId);
		if(bType == 0)
			params.put("type", 0);
		else
			params.put("type", 1);
		//Constants.hostUrl = "http://10.12.6.121:8080/NineCloud/";
		
		switch(id)
		{
		case R.id.collection:
			LogUtil.d(TAG, "collection params:" + params);
			HttpUtil.get(Constants.hostUrl+"/android/saveCollect", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							Log.v(TAG, result);
							if("true".equals(result)){
								APP.ShowToast(sAddCollectSuccess);
								SetSharePrefer.write_bool("collect_info", liveId, true);
								collection.setImageDrawable(getResources().getDrawable(R.drawable.collection_sel));
								b_collection = true;
								saveCollectCount = 0;
							}else{
								saveCollectFailAndExceptionProc();
							}
							
						} catch (JSONException e) {
							e.printStackTrace();
							saveCollectFailAndExceptionProc();
						}
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					
					saveCollectFailAndExceptionProc();
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
								APP.ShowToast(sAddLikeSuccess);
								SetSharePrefer.write_bool("praise_info", liveId, true);
								like.setImageDrawable(getResources().getDrawable(R.drawable.like_sel));
								b_like = true;
								savePraiseCount = 0;
								//TODO
							}else{
								 savePraiseFailAndExceptionProc();
							}
						} catch (JSONException e) {
							e.printStackTrace();
							 savePraiseFailAndExceptionProc();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					
					 savePraiseFailAndExceptionProc();
				}
				
			});	
			break;
		default:
			break;
		}
		
	}
	
	public void saveCollectFailAndExceptionProc()
	{
		if(saveCollectCount < 3)
		{
			saveCollectCount ++;
			save(R.id.collection);
		}
		else
		{
			saveCollectCount = 0;
			APP.ShowToast(sAddCollectFailure);
			NewWebActivity.this.finish();
		}
	}
	
	public void savePraiseFailAndExceptionProc()
	{
		if(savePraiseCount < 3)
		{
			savePraiseCount ++;
			save(R.id.like);
		}
		else
		{
			savePraiseCount = 0;
			APP.ShowToast(sAddLikeFailure);
			NewWebActivity.this.finish();
		}
	}
	
	public void cancel(int id){
		
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
		params.put("liveid", liveId);
		if(bType == 0)
			params.put("type", 0);
		else
			params.put("type", 1);
		//Constants.hostUrl = "http://10.12.6.121:8080/NineCloud/";
		
		switch(id)
		{
		case R.id.collection://取消收藏
			
			HttpUtil.get(Constants.hostUrl+"/android/cancelCollect", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							if("true".equals(result)){
								APP.ShowToast(sCancelCollectSuccess);
								SetSharePrefer.write_bool("collect_info", liveId, false);
								collection.setImageDrawable(getResources().getDrawable(R.drawable.collection));
								b_collection = false;
								cancelCollectCount = 0;
								//TODO
							}else{
								cancelCollectFailAndExceptionProc();
							}
						} catch (JSONException e) {
							e.printStackTrace();
							cancelCollectFailAndExceptionProc();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					
					cancelCollectFailAndExceptionProc();
				}
			});
			break;
		case R.id.like://取消点赞
			HttpUtil.get(Constants.hostUrl+"/android/cancelPraise", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					
					if(statusCode == 200){
						try {
							String result = json.getString("result");
							if("true".equals(result)){
								APP.ShowToast(sCancelLikeSuccess);
								SetSharePrefer.write_bool("praise_info", liveId, false);
								like.setImageDrawable(getResources().getDrawable(R.drawable.like));
								b_like = false;
								cancelPraiseCount = 0;
								//TODO
							}else{
								cancelPraiseFailAndExceptionProc();
							}
						} catch (JSONException e) {
							e.printStackTrace();
							cancelPraiseFailAndExceptionProc();
						}
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					cancelPraiseFailAndExceptionProc();
				}
			});
			break;
		default:
			break;
		}
		
	}
	
	public void cancelCollectFailAndExceptionProc()
	{
		if(cancelCollectCount<3)
		{
			cancelCollectCount ++;
			cancel(R.id.collection);
		}else{
			
			cancelCollectCount = 0;
			APP.ShowToast(sCancelCollectFailure);
			NewWebActivity.this.finish();
			
		}
	}
	public void cancelPraiseFailAndExceptionProc()
	{
		if(cancelPraiseCount < 3)
		{
			cancelPraiseCount ++;
			cancel(R.id.like);
		}else{
			
			cancelPraiseCount = 0;
			APP.ShowToast(sCancelLikeFailure);
			NewWebActivity.this.finish();
		}
	}
	
	public  String getJsonStr(String str){
		
		try{
			
			JSONObject obj = new JSONObject();
			obj.put("userId", APP.GetSharedPreferences("Info_Login", "sid", ""));
			obj.put("liveId", liveId);
			obj.put("position", sDefaultAddr);
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
	
	//提交评论
	public void sendComMessage(){
		String editStr = editC.getText().toString();
		if(editStr.length() == 0 || editStr.trim().equals("")){
			return;
		}
		
		String str = getJsonStr(editStr);		
		commentView.loadUrl("javascript:InsertComment('" + str + "')" );
		commentNum = commentNum +1;
		commentBar.setText(sCommentTip+"("+commentNum+")");
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
	
}
