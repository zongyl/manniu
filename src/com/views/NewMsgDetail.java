package com.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import P2P.SDK;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.adapter.HttpUtil;
import com.adapter.Message;
import com.alibaba.fastjson.JSON;
import com.basic.APP;
import com.ctrl.XImageBtn;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;
import com.jfeinstein.jazzyviewpager.JazzyViewPager.TransitionEffect;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.utils.BitmapUtils;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.FileUtil;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;

//报警
public class NewMsgDetail extends Activity implements OnClickListener,OnTouchListener{
	private final String TAG ="NewMegDetail";
	private JazzyViewPager _viewPager;
	private PopupWindow _pWindow;
	private ArrayList<Message> _msgs =new ArrayList<Message>();
	private TextView _title;
	private TextView _time;
	private TextView _devName;
	private String _strDevName="";
	private int _curIndex = 0;
	private ImageView _imageview;
	private View _imageLayout;
	private View _spinner;
	private int _curPaNo = 1;
	private Handler handler =new Handler();
	private  InnerPageAdapter _pageAdapter;
	View header,footer;
	Handler _handler;
    boolean firstLoad ;
    boolean firstSend = true;
	private int LEFT =0;
	private int RIGHT = 1;
	private int direction = -1;
	boolean change= false;
	private int UPDATED = 0;
	private int pageCount = 0;
	private DisplayImageOptions options;
	XImageBtn _alarmPlay,_alarmDown,_alarmShare;
	
	String _url;
	private String sid = APP.GetMainActivity().getSharedPreferences("Info_Login", APP.GetMainActivity().MODE_PRIVATE).getString("sid", "");
	
	/**
	 * 手指按下时的x y坐标
	 */
	private int xDown = 0;
	private int yDown = 0;
	/**
	 * 手指移动时的x y 坐标
	 */
	private int xMove = 0;
	private int yMove = 0;
	//触发移动事件的最短距离
	private int touchSlop = 200;
	
	ArrayList<View> _imageLayouts ;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init(R.layout.new_msg_detail2);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		switch(newConfig.orientation){
		case Configuration.ORIENTATION_PORTRAIT:
			init(R.layout.new_msg_detail2);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			init(R.layout.new_msg_detail1);
			break;
		}
	}
	
	public void init(int resid){
		setContentView(resid);
		findViews();
		setListeners();
		firstLoad = true;
		
	}
	
	/**加载视图数据*/
	public void findViews(){
		try {
			_title = (TextView) findViewById(R.id.msg_title);
			_time = (TextView) findViewById(R.id.msg_time);
			_devName = (TextView) findViewById(R.id.msg_devName);
			_curIndex = getIntent().getIntExtra("position", 0);
			_viewPager = (JazzyViewPager) findViewById(R.id.new_pager_list);
			_alarmPlay = (XImageBtn) findViewById(R.id.alarm_play);
			_alarmDown = (XImageBtn) findViewById(R.id.alarm_down);
			_alarmShare = (XImageBtn) findViewById(R.id.alarm_share);
			getMessageList();
			/*_imageLayout =LayoutInflater.from(this).inflate(R.layout.item_pager_image,null);
			_imageview = (ImageView) _imageLayout.findViewById(R.id.msg_image);
			_spinner = _imageLayout.findViewById(R.id.loading);*/
			initImageList();
			//getCurrentMsg(_curIndex);
			//getImageViews();
			//transforUrl(imgUrls);
			bindingAdpater();
			header =(View)findViewById(R.id.msg_detail_header);
			footer =(View)findViewById(R.id.msg_detail_footer);
			//show(header);
			//show(footer);
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
		
	}
	public void initImageList(){
		_imageLayouts = new ArrayList<View>();
		for(int i = 0;i<15;i++){
			View view = LayoutInflater.from(this).inflate(R.layout.item_pager_image,null);
			_imageLayouts.add(view);
		}
		options =  new DisplayImageOptions.Builder()
		//.showImageOnLoading(R.drawable.progress_msg_loading)
		.showImageForEmptyUri(R.drawable.images_nophoto_bg)
		.showImageOnFail(R.drawable.event_list_fail_pic)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build(); 
	}
	
	public void bindingAdpater(){
		RequestParams params = new RequestParams();
		params.put("userId",sid);
		params.put("sessionId", Constants.sessionId);
		HttpUtil.get(Constants.hostUrl+"/android/getMsgCount", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						Log.v(TAG, json.toString());
						if("nologin".equals(json.getString("result"))){
							LogUtil.d(TAG, "查询报警消息总数超时");
							//BaseApplication.getInstance().relogin();
						}else{
							pageCount = json.getInt("result");
							initAdapter();
						}
						
					} catch (JSONException e1) {
						e1.printStackTrace();
					}		
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(getResources().getString(R.string.E_SER_FAIL));
			}
		});
	}
	
	public  void initAdapter() {
		_pageAdapter = new InnerPageAdapter(this,_msgs);
		_viewPager.setOnPageChangeListener(new InnerPageListener());
		_viewPager.setAdapter(_pageAdapter);
		_viewPager.setOnTouchListener(this);
		/*显示报警消息点击条目*/
		_viewPager.setCurrentItem(_curIndex);
		/*设置动画类型*/
		_viewPager.setTransitionEffect(TransitionEffect.ZoomIn);
	}
	
	/**添加页面点击事件监听*/
	public void setListeners(){
		//findViewById(R.id.new_msg_video).setOnClickListener(this);
		findViewById(R.id.new_msg_back).setOnClickListener(this);
		findViewById(R.id.new_msg_menu).setOnClickListener(this);
		//_imageLayout.setOnClickListener(this);
		//_imageLayout.setOnTouchListener(this);
		//_imageview.setOnClickListener(this);
		//_spinner.setOnClickListener(this);
		if(_alarmPlay != null)
			_alarmPlay.setOnClickListener(this);
		if(_alarmDown != null)
			_alarmDown.setOnClickListener(this);
		if(_alarmShare != null)
			_alarmShare.setOnClickListener(this);
	}
	
	
	public void freshMessage(String userId, int  pageNo){	
		try {
			if(pageNo<1){
				return;
			}
			//_viewPager.setAdapter(null);
			_msgs.clear();
			Log.v("--页数--", ""+pageNo);
			RequestParams params = new RequestParams();
			params.put("userId",userId);
			params.put("pageNo", pageNo);
			params.put("sessionId", Constants.sessionId);
			HttpUtil.get(getResources().getString(R.string.server_address)+"/android/getMessage", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
					if(statusCode == 200){
						try {
							String result = json.getString("data");
							if("nologin".equals(result)){
								LogUtil.v(TAG, "查询报警消息超时");
								//BaseApplication.getInstance().relogin();
							}else{
								com.alibaba.fastjson.JSONArray array = JSON.parseArray(result);
								for(int i = 0; i < array.size(); i++){
									Message msg = JSON.toJavaObject((JSON)array.get(i), Message.class);
									_msgs.add(msg);	
									Log.v("fresh", "logtime:"+msg.logtime);
								}
								if(direction==LEFT){
									Log.v(TAG, "fist item of next page");
									_curPaNo++;
									displayMessage(0);
								}
								if(direction==RIGHT){
									Log.v(TAG, "last item of pre page");
									_curPaNo--;
									displayMessage(_msgs.size()-1);
								}
							}
						} catch (JSONException e1) {
							e1.printStackTrace();
						}		
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
					APP.ShowToast(getResources().getString(R.string.E_SER_FAIL));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<Message> getMessageList() {
		//dataList = (List<ImageItem>) getIntent().getSerializableExtra(EXTRA_IMAGE_LIST);
		ArrayList<Message> serializable = (ArrayList<Message>)getIntent().getExtras().getSerializable("msgList");	
		_msgs = serializable;
		return _msgs;
	}
	
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		/*case R.id.new_msg_video:
			forward(NewMsgWatch.class);
			break;*/
		case R.id.new_msg_menu:
			showPopwindow();
			break;
		case R.id.new_msg_back:
			APP.GetMainActivity().ShowXView(Main.XV_NEW_MAIN);
			finish();
			break;
//		case R.id.ontime_video:
//			//TODO:实现报警录像回放
//			_pWindow.dismiss();
//			break;
		case R.id.msg_save:
			//APP.ShowToast("click on pop:save msgImage to local");
			//TODO:实现报警图片下载			
			String filePath = FileUtil.getFileName(_strDevName);
			Bitmap bitmap = ImageLoader.getInstance().loadImageSync(_url);
			BitmapUtils.saveBitmap(bitmap, filePath);
			_pWindow.dismiss();
			break;
		case R.id.msg_shareImg:
			_pWindow.dismiss();
			//forward(NewMsgImgShare.class);
			String strAlarmInfo = "";
//			strAlarmInfo += getString(R.string.famliy_around_withme)+"\r\n";
//			strAlarmInfo += "[";
			strAlarmInfo += _devName.getText()+" ";			
			strAlarmInfo += _title.getText()+" ";
			strAlarmInfo += _time.getText();	
//			strAlarmInfo += "]";
			//2015.11.17 李德明修改 			
			ShareContentCustomizeDemo.showShare( getString(R.string.app_name),this,getString(R.string.famliy_around_withme),
					"",
					strAlarmInfo,
					_url
					,false,null);
			break;
		case R.id.msg_image:
			show(header);
			show(footer);
			break;
		case R.id.loading:
			show(header);
			show(footer);
			break;
		case R.layout.item_pager_image:
			show(header);
			show(footer);
		case R.id.alarm_play://播放
//			System.out.println(_curIndex);
//			System.out.println(_msgs.get(_curIndex).devicename+"--"+_msgs.get(_curIndex).logtime);
//			System.out.println(_msgs.get(_curIndex).evt_video);
			JSONObject json = null;
			String params = "?ossUrl="+_msgs.get(_curIndex).evt_video+"&timeMillis=0";
			try {
				Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/getUrl"+params);
				if (Integer.parseInt(map.get("code").toString()) == 200) {
					json = new JSONObject(map.get("data").toString());
					String str = json.getString("url");
					if(str.equals("NoSuchKey")){//地址错误
						APP.ShowToast(SDK.GetErrorStr(-1));
					}else{
						//播放
						Intent intent = new Intent(this, Fun_RecordPlay.class);
						intent.putExtra("evt_vsize", _msgs.get(_curIndex).evt_vsize);
						intent.putExtra("evt_video", str);
						intent.putExtra("deviceName", _msgs.get(_curIndex).devicename);
						startActivity(intent);
					}
				}
			} catch (Exception e) {
			}
			break;
		case R.id.alarm_down:
			imgDown();
			break;
		case R.id.alarm_share:
			imgShare();
			break;
		}
	}
	//保存本地
	private void imgDown(){
		try {
			String filePath = FileUtil.getFileName(_strDevName);
			Bitmap bitmap = ImageLoader.getInstance().loadImageSync(_url);
			BitmapUtils.saveBitmap(bitmap, filePath);
			File file = new File(filePath);
			if(file.isFile() && file.exists()){
				Toast.makeText(this, getText(R.string.SUCCESS_SAVE).toString(), Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
		}
	}
	//分享
	private void imgShare(){
		try {
			String strAlarmInfo = "";
			strAlarmInfo += _devName.getText()+" ";			
			strAlarmInfo += _title.getText()+" ";
			strAlarmInfo += _time.getText();	
			ShareContentCustomizeDemo.showShare( getString(R.string.app_name),this,getString(R.string.famliy_around_withme),
					"",
					strAlarmInfo,
					_url
					,false,null);
		} catch (Exception e) {
		}
	}
	
	 /**创建PopupWindow对话框*/
	private void showPopwindow() {
		 LayoutInflater inflater = (LayoutInflater) getSystemService(NewMsgDetail.LAYOUT_INFLATER_SERVICE);       
		 final View vPopupWindow=inflater.inflate(R.layout.new_msg_pop, null, false);
		 _pWindow= new PopupWindow(this);  
		 _pWindow.setContentView(vPopupWindow);
		 _pWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
		 _pWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		 _pWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_down_bg));
		 _pWindow.setFocusable(true);
		 _pWindow.setOutsideTouchable(true);
		 /*设置PopupWindow显示位置*/
		 _pWindow.showAtLocation(_viewPager, Gravity.TOP|Gravity.RIGHT, 0, 120);
		 vPopupWindow.setOnKeyListener(new OnKeyListener() {
				public boolean onKey(View view, int keycode, KeyEvent arg2) {
					if(keycode == KeyEvent.KEYCODE_BACK){
						_pWindow.dismiss();
					}
					return true;
				}
		 });
		 //vPopupWindow.findViewById(R.id.ontime_video).setOnClickListener(this);
		 vPopupWindow.findViewById(R.id.msg_save).setOnClickListener(this);
		 vPopupWindow.findViewById(R.id.msg_shareImg).setOnClickListener(this);
	}
	/**更新Message显示*/
	public void loadMessage(int position){
		
		if(direction==LEFT && position%15==0){
			//change = true;
			Log.v(TAG, " next page------");
			freshMessage(sid, _curPaNo+1);
		} 
		else if(direction==RIGHT && position%15==14){
			Log.v(TAG, " pre page--------");
			//change = true;
			freshMessage(sid, _curPaNo-1);
		}else{
			Log.v(TAG, "messagelist no chage");
			displayMessage(position);
			//change = false;
		}
	}
	
	public void displayMessage(int position){
		Log.v(TAG, "show message include  title  and img");
		int location = position%15;
		View view = _imageLayouts.get(position%15);
		_imageview = (ImageView) view.findViewById(R.id.msg_image);
		_spinner = view.findViewById(R.id.loading);
		_spinner.setVisibility(View.GONE);
		_imageview.setVisibility(View.VISIBLE);
		if(_msgs.size()==0){
			_spinner.setVisibility(View.VISIBLE);
			_imageview.setVisibility(View.GONE);
		}else if(location>=_msgs.size()&&_msgs.size()<15){
			return;
		}
		else{
			_url = _msgs.get(location).evt_picture;
			if("NoSuchKey".equals(_url)){
				_url="";
			}
			ImageLoader.getInstance().displayImage(_url, _imageview, options,null);
			 _title.setText(getString(R.string.move_alert));
			_time.setText(getString(R.string.time).concat(_msgs.get(location).logtime));
			_devName.setText(getString(R.string.from).concat(_msgs.get(location).devicename));
			_strDevName = _msgs.get(location).devicename;
			
		}
		
	}
	
	class InnerPageAdapter extends PagerAdapter {
		private LayoutInflater inflater;
		private ArrayList<Message> msgList;
		
		public ArrayList<Message> getMsgList() {
			return msgList;
		}
		public void setMsgList(ArrayList<Message> msgList) {
			this.msgList = msgList;
		}
		
		public InnerPageAdapter(Context context,ArrayList<Message> list){
			inflater = LayoutInflater.from(context);	
			msgList = list;
		}
		public int getCount() {
			return pageCount;
		}
		
		public int getItemPositin(){
			return POSITION_NONE;
		}
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.v(TAG, "destroyItem:"+position);
			container.removeView((View) object); 
		}
		
		public Object instantiateItem(ViewGroup container, int position) {
			Log.v(TAG, "instantiateItem:"+position);
			//_imageLayout = inflater.inflate(R.layout.item_pager_image, container, false);
			//_imageLayout.setOnTouchListener(new MyTouchListener());
			//_imageview = (ImageView) _imageLayout.findViewById(R.id.msg_image);
			//_spinner = (View) _imageLayout.findViewById(R.id.loading);
			//_imageview.setVisibility(View.INVISIBLE);
			//_spinner.setVisibility(View.VISIBLE);
			//loadImage(position);
			/*ViewGroup parent = (ViewGroup) _imageLayout.getParent();
			if(parent!=null){
				parent.removeAllViews();
			}*/
			//loadImage(position);
			//View view  = _imageLayouts.get(position%_msgs.size());
			//loadMessage(position);
			/*if(position%15==0 && direction ==LEFT){
				loadImage(position);
				freshMessage(sid, _curPaNo+1);
			}else if(position%15==14 &&direction == RIGHT){
				loadImage(position);
				freshMessage(sid, _curPaNo-1);
			}else{
				showImg(position);
			}*/
			if(position==0){
				View view = _imageLayouts.get(0);
				_imageview = (ImageView) view.findViewById(R.id.msg_image);
				_spinner = view.findViewById(R.id.loading);
				_spinner.setVisibility(View.GONE);
				_imageview.setVisibility(View.VISIBLE);
				String url = _msgs.get(0).evt_picture;
				if("NoSuchKey".equals(url)){
					url="";
				}
				displayMessage(0);
				//ImageLoader.getInstance().displayImage(url, _imageview, options, null);
			}
			container.addView(_imageLayouts.get(position%15));
			_viewPager.setObjectForPosition(_imageLayouts.get(position%15), position);
			return _imageLayouts.get(position%15);
		}
	}
	public void loadImage(final int position){
		_handler = new Handler(getMainLooper()){
			public void handleMessage(android.os.Message message){
				if(message.what==UPDATED){
					int location = position%_msgs.size();
					String url = _msgs.get(location).evt_picture;
					if("NoSuchKey".equals(url)){
						url="";
					}
					ImageLoader.getInstance().displayImage(url, _imageview, options, null/*new SimpleImageLoadingListener(),new ImageLoadingProgressListener() {
						@Override
						public void onProgressUpdate(String imageUri, View view, int current,
								int total) {
							if(total-current>0){
								_spinner.setVisibility(View.VISIBLE);
							}else{
								_spinner.setVisibility(View.GONE);
							}
						}
					}*/);
				}
			}
		};
	}
	
	public void showImg(int position){
		int location = position%_msgs.size();
		ImageLoader.getInstance().displayImage(_msgs.get(location).evt_picture, _imageview, options, new SimpleImageLoadingListener(),new ImageLoadingProgressListener() {
			@Override
			public void onProgressUpdate(String imageUri, View view, int current,
					int total) {
				if(total-current>0){
					_spinner.setVisibility(View.VISIBLE);
				}else{
					_spinner.setVisibility(View.GONE);
				}
			}
		});
	}

	class InnerPageListener implements OnPageChangeListener{
		
		public InnerPageListener(){
			
		}
		
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		public void onPageScrollStateChanged(int status) {
			
		}
		public void onPageSelected(int position) {
			//Log.v(TAG, "onPageSelected");
			Log.v("当前显示位置--> loadMessage",""+position);
			
			//getCurrentMsg(position);
			loadMessage(position);
			_curIndex = position;
			//displayMessage(position);
		}
	}
	
	Runnable dis = new Runnable(){
		@Override
		public void run() {
			Hide(header);
			Hide(footer);
		}
	};
	
	private void Hide(View v){
		if(v.isShown()){
			v.setVisibility(View.INVISIBLE);
			handler.removeCallbacks(dis);
		}else{
			v.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 控件延时隐藏  
	 * @param v
	 */
	private void show(View v){
		if(v.isShown()){
			handler.postDelayed(dis,3000);
		}else{
			v.setVisibility(View.VISIBLE);
			handler.postDelayed(dis,3000);
		}
	}
	
	public void forward(Class<?> target){
		Intent intent = new Intent(this,target);
		startActivity(intent);
	}
	
	public void onBackPress(){
		finish();
	}
	
	public boolean onTouch(View v, MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//Log.v(TAG, "---点击---");
			xDown = (int) ev.getX();
			yDown = (int) ev.getY();
			//show(header);
			//show(footer);
			break;
		case MotionEvent.ACTION_MOVE:
			//Log.v(TAG, "---移动---");
			xMove = (int) ev.getX();
			yMove = (int) ev.getY();
			int dx = xMove - xDown;
			int dy = yMove - yDown;
			//判断是否是从右到左的滑动
			//System.out.println(Math.abs(dx)+" -- "+Math.abs(dy));
			if (xMove < xDown && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop) {
				direction = LEFT;//左滑
				break;
			}else if(xMove > xDown && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop){
				direction = RIGHT;//右滑
				break;
			}
			break;
		}
		return false;
	}
}