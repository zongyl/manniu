package com.views.bovine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import net.majorkernelpanic.streaming.gl.V_SurfaceView;
import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;
import net.majorkernelpanic.streaming.video.H264Stream;
import net.majorkernelpanic.streaming.video.VideoQuality;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.adapter.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.backprocess.BackLoginThread;
import com.basic.APP;
import com.basic.XMSG;
import com.bean.LiveVideo;
import com.google.zxing.WriterException;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.mining.app.zxing.encodeing.EncodingHandler;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.utils.SdCardUtils;
import com.views.About_MobilephoneActivity;
import com.views.DeviceOnlineShare;
import com.views.Main;
import com.views.NewLogin;
import com.views.OnTaskListener;
import com.views.XViewBasic;
/**
 * @author: li_jianhua Date: 2015-6-11 下午5:00:21
 * To change this template use File | Settings | File Templates.
 * Description： 牛眼 TAB---改成不要TAB 直接显示成牛眼
 */
@SuppressLint("NewApi")
public class Fun_AnalogVideo extends XViewBasic implements OnClickListener, OnTaskListener,OnTouchListener{  
	private String TAG = Fun_AnalogVideo.class.getSimpleName();
	public static Fun_AnalogVideo instance = null;
    private ViewPager viewPager;//页卡内容  
    //private ViewPager local_viewPager;//页卡内容  
    private ImageView imageView;// 动画图片  
    public TextView textView1,textView2,textView3,_qrcodetDevice,_devTite;  
    public LinearLayout _layout;
    private List<View> views;// Tab页面列表  
    private int offset = 0;// 动画图片偏移量  
    private int currIndex = 0;// 当前页卡编号  
    private int bmpW;// 动画图片宽度  
    private View view1,view3;//各个页卡  view2
    public final static String SAVEFILE = "SETTING";
    Context context;
    Button _btnSub, _btnShare;
    EditText _tvPwd;
    public V_SurfaceView   m_prevewview;
    //TextView _btnSub;
    Spinner _rectype;
    public static int stoStep = 0; //存储位置状态 0外置 1手机
	public static int picture = 2; //画质-帧率3高 2 中1低
	public static int resolution = 0; //分辨率 0, 1, 2
    public static int[] NUM_PICTURE_TYPE = null;
    public static String[] STR_PICTURE_TYPE = null;
    RadioGroup _radGroup, _streamType, _isshare;//存储卡
    RadioButton _storageSDK,_storageMobile,_pictureHigh,_pictureIn,_pictureMin;
    
    public static String ImagePath = "";//视频截图
	public static String RecordPath = "";//录像
	public static String temMP4Path = "";//预录MP4文件 取SPS PPS信息
	public static String templatePath = "";
	public static String alarmsnapPath = "";//抓图
	public static String jniPath = "";//抓图
	
	public boolean isDevice = false;
	public int _localIndex = 0;
	
	public String _password = "";
	public TableRow _QRcodeRow;
	public String _sn = "";
	public String _vn = "";
	public String _devSid = "",_devName = "";
//	public int[] _bitrate = {700,400,200};
//	public int[] _framerate = {30,20,15};
	public int _bitrateAndFramerate[][] = {{300,200,100},{25,20,15}};
	/** Default quality of video streams. */
	public VideoQuality videoQuality = new VideoQuality(352,288,20,500000);
	
	public H264Stream h = null;
	//分享状态
	private boolean isshare = false;
	
	private ImageView rqcodeImg;
	
    public Fun_AnalogVideo(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		context = activity;
		instance = this;
	    	
//        InitImageView();  
//        InitTextView();  
//        InitViewPager(); 
		initContext();
    	String tmp = getFilePath();
		setFilePath(tmp);
		h = new H264Stream();
		
		SharedPreferences settings = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		videoQuality = new VideoQuality(
				settings.getInt("video_resX", videoQuality.resX),
				settings.getInt("video_resY", videoQuality.resY), 
				settings.getInt("video_framerate", videoQuality.framerate), 
				settings.getInt("video_bitrate", videoQuality.bitrate/1000)*1000);
		h.setVideoQuality(videoQuality);
		_handler.sendEmptyMessageDelayed(XMSG.CHECK_DEVICE, 2000); //延迟发送检查是否开通
		
		//COPY文件
		copyAssetFile();
    }  
    
    
    public void copyAssetFile(){
    	try {
    		File dir = new File(jniPath);
        	if(!dir.exists()) dir.mkdirs();
        	String fileName = dir + "/config.ini";
    		copyFileToSdcard(fileName);
		} catch (Exception e) {
		}
    }
    
    private void copyFileToSdcard(String fileName){  
    	try {
    		File file = new File(fileName);
        	if(!file.exists()){
        		InputStream myInput;  
                OutputStream myOutput = new FileOutputStream(fileName);  
                myInput = APP.GetMainActivity().getAssets().open("config.ini");
                byte[] buffer = new byte[1024];  
                int length = myInput.read(buffer);
                while(length > 0){
                    myOutput.write(buffer, 0, length); 
                    length = myInput.read(buffer);
                }
                myOutput.flush();
                myInput.close();
                myOutput.close();
        	}
        	SDK.DeviceSDKSetConfig(jniPath);
		} catch (Exception e) {
		}
    } 
    
    
    private void initContext(){
    	_rectype = (Spinner) findViewById(R.id.sp_rec_Type);
    	_radGroup = (RadioGroup) findViewById(R.id.rg_storage);
    	
		_storageSDK = (RadioButton) findViewById(R.id.sto_sdk);
		_storageMobile = (RadioButton) findViewById(R.id.sto_mobile);
		//画质先隐掉  帧率目前不能固定
		/*_streamType = (RadioGroup) findViewById(R.id.streamType);
		_pictureHigh = (RadioButton) findViewById(R.id.picture_high);
		_pictureIn = (RadioButton) findViewById(R.id.picture_in);
		_pictureMin = (RadioButton) findViewById(R.id.picture_min);*/
		_devTite = (TextView) findViewById(R.id.device_info);
		_QRcodeRow = (TableRow) findViewById(R.id.qrcode_row);
		//_tvPwd = (EditText) findViewById(R.id.tv_pwd);
    	_btnSub = (Button) findViewById(R.id.btn_sub);
    	_btnShare = (Button) findViewById(R.id.btn_share);
    //	isShare();
    	
    	//录MP4文件VIEW
    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.my_camera_view);
    	m_prevewview = new V_SurfaceView(context);
		layout.addView(m_prevewview);
		
		initSetting();
		
		_qrcodetDevice = (TextView) findViewById(R.id.qrcode_tv);
		_qrcodetDevice.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(ACT, About_MobilephoneActivity.class);
				intent.putExtra("sn", _sn);
				intent.putExtra("vn", _vn);
				ACT.startActivity(intent);
			}
		});
		
    	STR_PICTURE_TYPE = new String[] { "352x288","640x480","1280x720"};
		NUM_PICTURE_TYPE = new int[] { 0, 1, 2};
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(ACT, R.layout.my_spinner_item, STR_PICTURE_TYPE);
		adapter.setDropDownViewResource(R.layout.simple_spinner_item);//android.R.layout.simple_spinner_dropdown_item  R.layout.simple_spinner_item
		_rectype.setAdapter(adapter);
		//分辨率-添加事件Spinner事件监听  
		_rectype.setOnItemSelectedListener(new SpinnerSelectedListener());
		_rectype.setSelection(resolution);
		adapter.notifyDataSetChanged();
		//画质选择
		/*_streamType.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==_pictureHigh.getId()){
                	SaveConfigInfo(1,3);
                }else if(checkedId==_pictureIn.getId()){
                	SaveConfigInfo(1,2);
                }else if(checkedId==_pictureMin.getId()){
                	SaveConfigInfo(1,1);
                }
            }
        });*/
		//存储路径选择
		_radGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==_storageSDK.getId()){
                    String tmp = getExternalPath();
                    if(tmp != null){
                    	setFilePath(tmp);
                		SaveConfigInfo(3,0);
                    }else{
                    	_storageSDK.setChecked(false);
                    	_storageMobile.setChecked(true);
                    	APP.ShowToastLong(context.getString(R.string.nothave_SDCard));
                    	return;
                    }
                }else if(checkedId==_storageMobile.getId()){
                	String tmp = getFilePath();
                	setFilePath(tmp);
                	SaveConfigInfo(3,1);
                }
            }
        });
		
		_btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isshare){
					cancelShare();
				}else{
					share();
				}
			}
		});
		//打开
    	_btnSub.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//修改分辨率之后更新数据
				h.setVideoQuality(videoQuality);
				/*//保存MP4文件(软编码不需要--苹果需要)
				if(resolution == 0){ //352
					h.TESTFILE = Fun_AnalogVideo.temMP4Path+"manniu_352.mp4";
				}else if(resolution == 1){//640
					h.TESTFILE = Fun_AnalogVideo.temMP4Path+"manniu_640.mp4";
				}else if(resolution == 2){//1280
					h.TESTFILE = Fun_AnalogVideo.temMP4Path+"manniu_1280.mp4";
				}
				File file = new File(h.TESTFILE);
				if(!file.exists()){
					//如果分辨率没有变化不生成 MP4
					APP.ShowWaitDlg(Fun_AnalogVideo.this, R.string.set_pwd, 1, 0);
				}else{*/
					initAnalog();
					if(!isDevice){
						addOrUpdateDevice("123456");
					}
					_btnSub.setClickable(false);
				//}
			}
		});

		rqcodeImg = (ImageView) findViewById(R.id.qrcode_img);
		//rqcodeImg.setBackgroundColor(context.getResources().getColor(R.color.red_btn));
		genBitmap("");
		
		//rqcodeImg.setBackgroundResource(R.drawable.default_img);
    }
  
    //string gen rqcode IMG
    private void genBitmap(String str){
    	try {
			rqcodeImg.setImageBitmap(EncodingHandler.createQRCode(str, 350));
		} catch (WriterException e) {
			e.printStackTrace();
		}
    }
  
    private void InitViewPager() {  
        viewPager=(ViewPager) findViewById(R.id.vPager);  
        views=new ArrayList<View>();  
        LayoutInflater inflater = LayoutInflater.from(context);
        view1=inflater.inflate(R.layout.lay_realplay, null);  
        //点击打开预览事件
        view1.setOnTouchListener(this);
        view3=inflater.inflate(R.layout.lay_setting, null);  
        views.add(view1);  
        views.add(view3);  
        viewPager.setAdapter(new XViewPagerAdapter(views));  
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());  
    } 
    
    private MyHandler _handler = new MyHandler();
    @Override
	protected void OnVisibility(int visibility) {
		super.OnVisibility(visibility);
		if (visibility == View.VISIBLE) {
			//initAnalog();
		}
	}
    //初始化牛眼
    private void initAnalog(){
    	if(isDevice){
			APP._dlgWait.show();
        	APP.SetWaitDlgText(context.getText(R.string.openning_stream).toString());
			_handler.sendEmptyMessageDelayed(1002, 1000);
//        	Message msg = new Message();
//			msg.what = 1002;
//			_handler.sendMessageDelayed(msg, 1000);
        }
    }
    //如果手机不支持355 更新到640
    public void updateRectype(){
    	SaveConfigInfo(2,1);
    	h.setVideoQuality(videoQuality);
    	_rectype.setSelection(1);
    }
    
     /** 
      *  初始化头标 
      */  
    private void InitTextView() {  
        textView1 = (TextView) findViewById(R.id.text1);  
        //textView2 = (TextView) findViewById(R.id.text2);  
        textView3 = (TextView) findViewById(R.id.text3);  
  
        textView1.setOnClickListener(this);  
       // textView2.setOnClickListener(this);  
        textView3.setOnClickListener(this);  
    }  
  
    /** 
     2      * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据 
     3 */  
    private void InitImageView() {  
        imageView= (ImageView) findViewById(R.id.cursor);  
        bmpW = BitmapFactory.decodeResource(APP.GetMainActivity().getResources(), R.drawable.tabline).getWidth();// 获取图片宽度  
        DisplayMetrics dm = new DisplayMetrics();  
        APP.GetMainActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);  
        int screenW = dm.widthPixels;// 获取分辨率宽度  
        offset = (screenW / 2 - bmpW) / 2;// 计算偏移量  
        Matrix matrix = new Matrix();  
        matrix.postTranslate(offset, 0);  
        imageView.setImageMatrix(matrix);// 设置动画初始位置  
    }
    
    @Override
	public void OnMessage(Message msg) {
    	switch (msg.what) {
		case XMSG.ANALOG:	//挂断电话后重新打开
			initAnalog();
			break;
    	}
    }
    
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.text1:
			if(!isDevice){
				isDeviceOpen();
        		return;
        	}
			viewPager.setCurrentItem(0, true);
			//startExperience();
			break;
//		case R.id.text2:
//			viewPager.setCurrentItem(1, true);
//			break;
		case R.id.text3:
			viewPager.setCurrentItem(1, true);
			break;
		default:
			break;
		}
	}
	//设备未开通，请先开通
	public void isDeviceOpen(){
		new AlertDialog.Builder(APP.GetMainActivity()).setTitle(context.getString(R.string.tip_title)).setMessage(context.getString(R.string.analog_video_no_open))
		.setIcon(R.drawable.help)
		.setPositiveButton(context.getString(R.string.confirm), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(viewPager.getCurrentItem() == 0)
					Fun_AnalogVideo.instance.onClick(textView3);
			}
		}).show();
	}
	//判断是否分享
	private void isShare(){
		RequestParams params = new RequestParams();
		params.put("deviceId", _devSid);
		
		LogUtil.d(TAG, "isShare:" + Constants.hostUrl+"/device/isShare?"+params.toString());
		
		HttpUtil.get(Constants.hostUrl + "/device/isShare", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d(TAG, "isShare.onSuccess response:" + response.toString());
				boolean status = false;
				try {
					status = response.getBoolean("result");
					if(status){
						Log.d(TAG, "true");
						isshare = true;
						_btnShare.setText(context.getString(R.string.analog_video_cancel_share));
						_btnShare.setEnabled(true);
					}else{
						Log.d(TAG, "false");
						isshare = false;
						_btnShare.setText(context.getString(R.string.analog_video_open_ok_share));
						_btnShare.setEnabled(true);
					}
				} catch (JSONException e) {
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}
	
	/**
	 * 取消分享
	 */
	private void cancelShare(){
		RequestParams params = new RequestParams();
		params.put("deviceId", _devSid);
		HttpUtil.get(Constants.hostUrl+"/device/cancelShare", params, new JsonHttpResponseHandler() {
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200 ){
					try{
						if(json.getBoolean("result")){
							APP.ShowToast(context.getString(R.string.SUCCESS_CANCEL));
							isShare();
						//	checkDevice(0);
						}else{
							APP.ShowToast(context.getString(R.string.FAIL_CANCEL));
						}
					}catch(JSONException e){
						e.printStackTrace();
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if(errorResponse!=null){
					Log.v(TAG, "error1:"+errorResponse.toString());
				}
				APP.ShowToast(context.getString(R.string.E_SER_FAIL));
			}
		});
		
	}
	
	/**
	 * 分享
	 */
	private void share(){
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
		params.put("deviceId", _devSid);
		params.put("sessionId", Constants.sessionId);
		Log.d(TAG, "niuyan share params:" + params.toString());
		HttpUtil.get(Constants.hostUrl+"/android/isUserS", params, new JsonHttpResponseHandler() {
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200 ){
					try{
						String result = json.getString("msg");
						if("nologin".equals(result)){
							LogUtil.d(TAG, "查询用户认证信息超时");
							//BaseApplication.getInstance().relogin();
						}else{
							context.registerReceiver(receiver, intentFilter);
							Intent intent = new Intent(context, DeviceOnlineShare.class);
							Bundle b = new Bundle();
							b.putStringArray("sharemsg", new String[]{_devSid, result});
							String tempStr = json.getString("data").toString();
							if(tempStr.length()>0){
								com.alibaba.fastjson.JSONObject job = (com.alibaba.fastjson.JSONObject) (JSON.parse(json.getString("data")));
								LiveVideo liveVideo = JSON.toJavaObject(job, LiveVideo.class);
								b.putParcelable("shareinfo", liveVideo);
							}
							intent.putExtras(b);
							context.startActivity(intent);
						}
					}catch(JSONException e){
						e.printStackTrace();
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if(errorResponse!=null){
					Log.v(TAG, "error1:"+errorResponse.toString());
				}
				APP.ShowToast(context.getString(R.string.E_SER_FAIL));
			}
			/*public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				if(responseString!=null){
					Log.v(TAG, "error2:"+responseString);
				}
				APP.ShowToast("请稍后再试!");
			};*/
		});
	
	}
	
	
	/**
     * 通过activity获取视图
     * @param id
     * @param intent
     * @return
     */
//    @SuppressWarnings("deprecation")
//	private View getView(String id, Intent intent) {
//        return manager.startActivity(id, intent).getDecorView();
//    }
	
	public void getFragmentView(int type){
		if(type == 0){
			android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
        	Fra_VideoActivity fragment = new Fra_VideoActivity(); 
        	frTransaction.replace(R.id.ui_container, fragment);// 使用当前Fragment的布局替代id_content的控件
        	frTransaction.commit();
		}else{
			android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
        	Fra_SnapActivity fragment = new Fra_SnapActivity(); 
        	frTransaction.replace(R.id.ui_container, fragment);// 使用当前Fragment的布局替代id_content的控件
        	frTransaction.commit();
		}
	}
	
    public class MyOnPageChangeListener implements OnPageChangeListener{  
//        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量  
//        int two = one * 2;// 页卡1 -> 页卡3 偏移量  
        public void onPageScrollStateChanged(int state) {  
        	//scorllState = state;
        }  
  
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {  
        }  
  
        public void onPageSelected(int arg0) {  
            switch (arg0) { 
            case 0: 
            	if(!isDevice){
            		isDeviceOpen();
            		return;
            	}
//            	_layout = (LinearLayout) findViewById(R.id.main_tab);
//            	_layout.setOnTouchListener(new OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						switch (v.getId()) {
//						case R.id.main_tab:
//							if(event.getAction() == MotionEvent.ACTION_UP){
//								System.out.println(111);
//							}
//							break;
//
//						default:
//							break;
//						}
//						return false;
//					}
//				});
            	textView1.setTextColor(context.getResources().getColor(R.color.blue_menu));
            	textView3.setTextColor(context.getResources().getColor(R.color.text_color));
            	
            	APP.ShowWaitDlg(Fun_AnalogVideo.this, R.string.openning_stream, 0, 0);
                break; 
            case 1:
            	textView3.setTextColor(context.getResources().getColor(R.color.blue_menu));
            	textView1.setTextColor(context.getResources().getColor(R.color.text_color));
            	
            	//_tvPwd.setText(_password);
                break; 
            } 
            changeView(arg0);
        }  
    } 
    
    public void changeView(int arg0){
    	int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量  
        int two = one * 2;// 页卡1 -> 页卡3 偏移量  
    	Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);//显然这个比较简洁，只有一行代码。  
        currIndex = arg0;  
        animation.setFillAfter(true);// True:图片停在动画结束位置  
        animation.setDuration(200);  
        imageView.startAnimation(animation);  
    }
    
    public void addOrUpdateDevice(String pwd){
    	//添加到设备列表
		RequestParams params = new RequestParams();
		params.put("devicesname", Constants.userName);
		params.put("userId", Constants.userid);
		params.put("password", pwd);
		
		APP._dlgWait.show();
    	APP.SetWaitDlgText(context.getText(R.string.set_pwd).toString());
		
		String _url = "";
		if(isDevice)
			_url = "/android/updateAnalogDevice";
		else
			_url = "/android/addDevice";
		
		HttpUtil.post(Constants.hostUrl + _url, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					//Log.d(TAG, "onSuccess:" + json.toString());
					String msg = "";
					try {
						msg = json.getString("msg");
						if("success".equals(msg)){
							APP.ShowToast(ACT.getResources().getString(R.string.SUCCESS_Operation));
							checkDevice(1);
							_QRcodeRow.setVisibility(View.VISIBLE);
							_devTite.setVisibility(View.VISIBLE);
						}else if("nologin".equals(msg)){
							//BaseApplication.getInstance().relogin();
						}else{
							_btnSub.setClickable(true);
							APP.ShowToast(ACT.getResources().getString(R.string.Err_Error_Unknow));
						}
					} catch (JSONException e) {
						_btnSub.setClickable(true);
						LogUtil.e(TAG, "" + e.getMessage());
					}
					if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
				APP.ShowToast(ACT.getResources().getString(R.string.Err_CONNET));
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
				if(responseString == null){
					Log.d(TAG, "responseString is null!");
				}else{
					Log.d(TAG, "response:" + responseString);
				}
				
				if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
				APP.ShowToast(ACT.getResources().getString(R.string.Err_CONNET));
			}
			@Override  
            public void onFinish() {  
                super.onFinish();  
            }
		});
    }
    
    @SuppressWarnings("static-access")
	public void initSetting(){
    	SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		picture = preferences.getInt("picture", 2);
		stoStep = preferences.getInt("stoStep", 1);
		resolution = preferences.getInt("resolution", 0);
		if(stoStep == 0){
			_storageSDK.setChecked(true);
			String tmp = getExternalPath();
			if(tmp != null){
				setFilePath(tmp);
			}
		}else if(stoStep == 1){
			_storageMobile.setChecked(true);
			String tmp = getFilePath();
			setFilePath(tmp);
		}
		
		/*if(picture == 3){
			_pictureHigh.setChecked(true);
		}else if(picture == 2){
			_pictureIn.setChecked(true);
		}else if(picture == 1){
			_pictureMin.setChecked(true);
		}*/
    }
    
    @SuppressLint("SdCardPath")
	public void setFilePath(String path){
		if(path.equals("/data")){
			path = "/data/data/com.manniu.manniu"; //manniu的安装路径
		}
		ImagePath = path + "manniu/images/";
		RecordPath = path + "manniu/records/";
		templatePath = path+"manniu/template/";
		alarmsnapPath = path+"manniu/alarmsnap/";
		temMP4Path = path + "manniu/mp4/";
		jniPath = path + "manniu/jni/";
	}
 // 写 SharedPreferences  1.画质 2.分辩率bitrate  3 存储位置
    private String newValue = "";
 	@SuppressWarnings("static-access")
	public int SaveConfigInfo(int type, int value) {
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE,APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		if (type == 1) {
			editor.putInt("picture", value);
			picture = value;
			setBitrateAndFramerate(editor);
		} else if (type == 2) {
			if (value == 0) {
				_bitrateAndFramerate[0][0] = 300;
				_bitrateAndFramerate[0][1] = 200;
				_bitrateAndFramerate[0][2] = 100;
				_bitrateAndFramerate[1][0] = 25;
				_bitrateAndFramerate[1][1] = 20;
				_bitrateAndFramerate[1][2] = 15;
				newValue = "352*288";
			}else if (value == 1) {
				_bitrateAndFramerate[0][0] = 700;
				_bitrateAndFramerate[0][1] = 400;
				_bitrateAndFramerate[0][2] = 200;
				_bitrateAndFramerate[1][0] = 25;
				_bitrateAndFramerate[1][1] = 20;
				_bitrateAndFramerate[1][2] = 15;
				newValue = "640*480";
			}else if (value == 2) {
				_bitrateAndFramerate[0][0] = 2000;
				_bitrateAndFramerate[0][1] = 1000;
				_bitrateAndFramerate[0][2] = 700;
				_bitrateAndFramerate[1][0] = 20;
				_bitrateAndFramerate[1][1] = 15;
				_bitrateAndFramerate[1][2] = 5;
				newValue = "1280*720";
			}

			editor.putInt("video_resX", Integer.parseInt(newValue.split("\\*")[0]));
			editor.putInt("video_resY", Integer.parseInt(newValue.split("\\*")[1]));
			videoQuality.resX = Integer.parseInt(newValue.split("\\*")[0]);
			videoQuality.resY = Integer.parseInt(newValue.split("\\*")[1]);
//			System.out.println("init..."+videoQuality.resX+"---"+videoQuality.resY);
			
			setBitrateAndFramerate(editor);
			editor.putInt("resolution", value);
			resolution = value;
		} else if (type == 3) {
			editor.putInt("stoStep", value);
		}
		editor.commit();
		return 0;
	}
 	
 	//添加码率和帧率
 	public void setBitrateAndFramerate(Editor editor){
 		switch (picture) {
		case 1:
			editor.putInt("video_bitrate", _bitrateAndFramerate[0][2]);
			editor.putInt("video_framerate", _bitrateAndFramerate[1][2]);
			videoQuality.bitrate = _bitrateAndFramerate[0][2]*1000;
			videoQuality.framerate = _bitrateAndFramerate[1][2];
			break;
		case 2:
			editor.putInt("video_bitrate", _bitrateAndFramerate[0][1]);
			editor.putInt("video_framerate", _bitrateAndFramerate[1][1]);
			videoQuality.bitrate = _bitrateAndFramerate[0][1]*1000;
			videoQuality.framerate = _bitrateAndFramerate[1][1];
			break;
		case 3:
			editor.putInt("video_bitrate", _bitrateAndFramerate[0][0]);
			editor.putInt("video_framerate", _bitrateAndFramerate[1][0]);
			videoQuality.bitrate = _bitrateAndFramerate[0][0]*1000;
			videoQuality.framerate = _bitrateAndFramerate[1][0];
			break;
		default:
			break;
		}
 	}
 	
 	
 	//外置SD卡
	private String getExternalPath(){
		String tmpPath = "";
		tmpPath = SdCardUtils.getExternalSDcardPath();
		return tmpPath;
	}
	
	//内置SD卡
	private String getFilePath() {
		String tmpPath = "";
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			tmpPath = Environment.getExternalStorageDirectory()+"/";
		} else{
			tmpPath = Environment.getDataDirectory()+"/";
		}
		return tmpPath;
	}
    
  //使用数组形式操作
    class SpinnerSelectedListener implements OnItemSelectedListener{
        public void onItemSelected(AdapterView<?> arg0, View view, int arg2,
                long arg3) {
//        	if(view != null){
//        		TextView tv = (TextView)view;  
//                tv.setTextSize(15.0f);    //设置大小
//        	}
        	//参考 package net.majorkernelpanic.spydroid.ui; 这两个类修改画质 OptionsActivity SpydroidApplication
        	SaveConfigInfo(2,arg2);
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
    //public boolean isOpenAnalog = false;//判断是否是打开牛眼用于收到IDM掉线通知后 登录牛眼
	public static java.util.Timer _timer = null;
	//定时器 如果长时间打洞不成功或打洞成功收不到数据 关闭页面
	public void startTimer() {
		try {
			if(BackLoginThread.state == 200){
				//isOpenAnalog = true;
				//先把IPC的登出
		    	SDK.Logout();
		    	BackLoginThread.state = 3;
		    	Main.Instance._loginThead.start();
		    	Main.Instance._loginThead.error_Count = 0;
				if (_timer != null) {
					_timer.cancel();
					_timer = null;
				}
				if(_timer == null){
					_timer = new java.util.Timer();
				}
				_timer.schedule(new TimerTask() {
					@Override
					public void run() {
						startExperience();
					}
				}, 100, 1000);
			}else{
				_btnSub.setClickable(true);
				_handler.sendEmptyMessage(XMSG.ANALOG_IS_LOGIN);
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}

	public void stopTimer() {
		if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
	}
    //模拟TYPE 传4
    public void startExperience() {
//    	//在登录模拟的
		//int status = SDK.Login("cms.9wingo.com", 9511, 4, _devSid, Constants.userName, "pwd");
		if(BackLoginThread.state == 200){
			stopTimer();
			Intent intent = new Intent(ACT, AnalogvideoActivity.class);
			intent.putExtra("deviceSid", _devSid);
			intent.putExtra("deviceName", _devName);
			ACT.startActivity(intent);
			if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
			_btnSub.setClickable(true);
			//isOpenAnalog = false;
		}
		
//    	stopTimer();
//    	Intent intent = new Intent(ACT, AnalogvideoActivity.class);
//		ACT.startActivity(intent);
//		if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
//		return 0;
	}
    
	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg); //这句什么都没有操作  可以不要
			switch (msg.what) {
			case 1002:
				if(APP.IsWaitDlgShow()){
					startTimer();
	        	}
				break;
			case XMSG.CHECK_DEVICE:
				backgroundExecution();
				break;
			case XMSG.CHECK_DEVICE_SHARE:
				isShare();
				break;
			case XMSG.ANALOG_IS_LOGIN:
				APP.ShowToastLong(APP.GetString(R.string.Video_Dviece_login));
				if(APP.IsWaitDlgShow()) APP._dlgWait.dismiss();
				break;
			case 1003:
				initAnalog();
				if(!isDevice){
					addOrUpdateDevice("123456");
				}
				_btnSub.setClickable(false);
				break;
			}
		}
	}
	
	//这个方法在主UI线程上调用
	private void backgroundExecution(){
		//将耗时的操作移到子线程。
		Thread thread = new Thread(null,doBackgroundThreadProcessing,"Background");
		thread.start();
	}
	//执行后台处理方法的run.
	private Runnable doBackgroundThreadProcessing = new Runnable() {
		@Override
		public void run() {
			//判断设备有没有开通
			checkDevice(0);
		}
	};
    //在后台执行一些处理的方法。
//	private void backgroundThreadProcessing(){
//		//耗时操作
//	}
	
	//判断设备是否存在
	/*private void checkDevice2(){
		try {
			JSONObject json = null;
//			String params = "?sid="+Constants.userid;
//			Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/isExist"+params);
			String params = "?sid="+Constants.userid+"&cid="+Constants.session_Id;
			Map<String, Object> map = HttpURLConnectionTools.get(Constants.ETShostUrl + "/query_sm_ipc" + params);
			if (Integer.parseInt(map.get("code").toString()) == 200) {
				json = new JSONObject(map.get("data").toString());
				String msg = "";
				try {
					msg = json.getString("msg");
					if("success".equals(msg)){
						if(!json.getString("data").equals("{}")){
							JSONArray array = JSON.parseArray(json.getString("data"));
							for(int i = 0; i < array.size(); i++){
								Device dev1 = JSON.toJavaObject((JSON)array.get(i), Device.class);
								isDevice = true;
								textView3.setText("修改");
								_sn = dev1.sn;
								_vn = dev1.vn;
								_devSid = dev1.sid;
								Log.d(TAG, "已开通!");
							}
						}
					}else{
						isDevice = false;
						textView3.setText("开通");
						Log.d(TAG, "未开通!");
					}
				} catch (JSONException e) {
				}
			}else{
				APP.ShowToast(ACT.getResources().getString(R.string.E_SER_FAIL));
				_handler.sendEmptyMessage(XMSG.CHECK_DEVICE);//如果网络不好一直判断
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}*/
	//检查设备是否开通  typt 0:程序加载工 1：添加判断
	private void checkDevice(int type){
		try {
			JSONObject json = null;
			String params = "?sid="+Constants.userid;
			Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl+"/android/isExist"+params);
			
			LogUtil.d(TAG, "checkDevice:" + Constants.hostUrl+"/android/isExist"+params);
			
			if (Integer.parseInt(map.get("code").toString()) == 200) {
				json = new JSONObject(map.get("data").toString());
				String msg = "";
				try {
					msg = json.getString("msg");
						if("true".equals(msg)){
						JSONObject data = json.getJSONObject("data");
						isDevice = true;
						_sn = data.getString("sn").toString();
						_vn = data.getString("vn").toString();
						_devSid = data.getString("sid").toString();
						_devName = data.getString("devicesname").toString();
						if(type == 1){//添加成功后 直接加打开牛眼
							APP.ShowWaitDlg(Fun_AnalogVideo.this, R.string.openning_stream, 0, 0);
						}
						Log.d(TAG, "已开通!");
						genBitmap("sn:"+_sn+";vn:"+_vn);
						_handler.sendEmptyMessage(XMSG.CHECK_DEVICE_SHARE);
					}else{
						isDevice = false;
						_QRcodeRow.setVisibility(View.GONE);
						_devTite.setVisibility(View.GONE);
						Log.d(TAG, "未开通!");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}else{
				APP.ShowToast(ACT.getResources().getString(R.string.E_SER_FAIL));
				_handler.sendEmptyMessage(XMSG.CHECK_DEVICE);//如果网络不好一直判断
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}

	//这种异步的方法要等前一个运行完成之后 才运行
	@Override
	public Object OnDoInBackground(int what, int arg1, int arg2, Object obj) {
		try {
			switch (what) {
			case 0:
				startTimer();
				//return startExperience();
				return 0;
			case 1: 	// MP4文件
				try {
					h.setSurfaceView(m_prevewview);
					int ret = h.testMediaRecorderAPI();
					return ret;
				} catch (RuntimeException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		return -1;
	}

	@Override
	public int OnPostExecute(int what, int arg1, int arg2, Object obj,
			Object ret) {
		switch (what) {
		case 0: 	
			Integer nRet = (Integer) ret;
			if (nRet != 0) {	// 打开视频失败
				APP.ShowDialog(SDK.GetErrorStr(nRet));
			}
			break;
		case 1:
			Integer nRet1 = (Integer) ret;
			if (nRet1 != 0) {	// 录像失败
				APP.ShowDialog(SDK.GetErrorStr(nRet1));
			}else{
				_handler.sendEmptyMessageDelayed(1003, 500);
			}
			break;
		case -1:					// 异常
			break;
		}
		return 0;
	}

	//点击打开预览
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			APP._dlgWait.show();
        	APP.SetWaitDlgText(context.getText(R.string.openning_stream).toString());
        	Message msg = new Message();
			msg.what = 1002;
			_handler.sendMessageDelayed(msg, 1000);
			break;
		}
		return true;
	}
    
	/**
	 * 广播接收器
	 */
	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			context.unregisterReceiver(this);
			Log.d(TAG, "Fun_AnalogVideo onReceive：...");
			isShare();
		}
	};
	IntentFilter intentFilter = new IntentFilter("com.views.bovine.Fun_AnalogVideo");
}  
