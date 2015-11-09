package com.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.basic.APP;
import com.basic.XMSG;
import com.bean.LiveVideo;
import com.bean.UsersMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.Constants;
import com.utils.DateTimePickerDialog;
import com.utils.DateTimePickerDialog.OnDateTimeSetListener;
import com.utils.DateUtil;
import com.utils.LogUtil;
import com.utils.Tools;
import com.views.bovine.Fun_AnalogVideo;

public class DeviceOnlineShare extends Activity implements OnClickListener{
	final String TAG = this.getClass().getName();
	String err_con;
	String err_up;
    String suc_up;
	private EditText shareTitle;
	private Spinner spinner;
	//private TextView timeS;
	//private TextView timeE;
	ImageView addveri;
	ImageView verify1;
	ImageView verify2;
	ImageView camera;
	ImageView photo;
	TextView videoType;
	TextView idline;
	TextView veriDeclare;
	EditText shareName;
	EditText shareIdCard;
	EditText shareDeclare;
	boolean firpic;
	boolean secpic;
	boolean isFirstShare;
	boolean flag;
	private Button commit;
	public Dlg_Wait _dlgWait = null;
	String [] items =null;
	String _userId = APP.GetSharedPreferences("Info_Login", "sid", "");
	String sid;
	ArrayAdapter<CharSequence> adapter;
	LayoutInflater inflater;
	LinearLayout userLayout;
	Map<Integer,Boolean> MultiChoiceId = new HashMap<Integer,Boolean>();
	private static final int CAMERA_REQUEST_CODE = 0;//拍照
	private static final int IMAGE_REQUEST_CODE = 1;//相册
	Uri _uri =null;
	String _imagename;
	Bitmap bm;
	BitmapFactory.Options bmFactoryOptions;
	List<Uri> uriList = new ArrayList<Uri>();
	List<File> fileList = new ArrayList<File>();
	
	UsersMessage  userMessage ; 
			
	Map<String,Boolean> flags = new HashMap<String,Boolean>();
	
	RequestParams params ;
	
	AsyncHttpClient client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.device_online_share, null);
		setContentView(layout);
		initViews();
		setListeners();
	}

	public void initViews(){
		_dlgWait = new Dlg_Wait(this, R.style.UpdateDialog);
		shareTitle = (EditText) findViewById(R.id.share_title);
		//timeS = (TextView) findViewById(R.id.time_start);
		//timeE = (TextView) findViewById(R.id.time_end);
		commit = (Button) findViewById(R.id.share_commit);
		addveri = (ImageView) findViewById(R.id.add_verify);
		verify1 = (ImageView) findViewById(R.id.verify_1);
		verify2 = (ImageView) findViewById(R.id.verify_2);
		camera = (ImageView) findViewById(R.id.take_idcard);
		photo = (ImageView) findViewById(R.id.local_idcard);
		videoType = (TextView) findViewById(R.id.share_type);
		idline = (TextView) findViewById(R.id.idline);
		veriDeclare = (TextView) findViewById(R.id.veriDeclare);
		shareDeclare  = (EditText) findViewById(R.id.share_declare);
		shareName = (EditText) findViewById(R.id.share_relN);
		shareIdCard = (EditText) findViewById(R.id.share_idCard);
		userLayout = (LinearLayout) findViewById(R.id.userMessage);
		
		err_con = getResources().getString(R.string.Err_CONNET);
		err_up = getResources().getString(R.string.Err_UPLOAD);
		suc_up = getResources().getString(R.string.SUCCESS_UPLOAD);
				
		FirstTimeOrNot();
	}
	
	public void setListeners(){
		shareTitle.setOnClickListener(this);
		//spinner.setOnClickListener(this);
		//timeS.setOnClickListener(this);
		//timeE.setOnClickListener(this);
		commit.setOnClickListener(this);
		addveri.setOnClickListener(this);
		verify1.setOnClickListener(this);
		verify2.setOnClickListener(this);
		camera.setOnClickListener(this);
		photo.setOnClickListener(this);
		videoType.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		/*case R.id.time_start:
			//finish();
			//showDialog(timeS);
			mLinearLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.time_end:
			//showDialog(timeE);
			mLinearLayout.setVisibility(View.VISIBLE);
			break;*/
		case R.id.share_commit:
			LogUtil.d(TAG, "click commit button!!");
			sendInfo();
			break;
		case R.id.add_verify:
			showImageButton(true);
		case R.id.verify_1:
			/*if(firpic){
				return;
			}else{
				showImageButton(true);
			}*/
			break;
		case R.id.verify_2:
			/*if(secpic){
				return;
			}else{
				showImageButton(true);
			}*/
			break;
		case R.id.take_idcard:
			takepcamera();
			break;
		case R.id.local_idcard:
			localphotos();
			break;
		case R.id.share_type:
			items = getResources().getStringArray(R.array.share_group);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			MultiChoiceId.clear();
			builder.setMultiChoiceItems(items, new boolean[]{false,false,false}, new OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					MultiChoiceId.put(which,isChecked);
				}
			});
			builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int size = MultiChoiceId.size();
					StringBuilder temSb = new StringBuilder();
					if(size>0){
						Iterator<Entry<Integer, Boolean>> it = MultiChoiceId.entrySet().iterator();
						while(it.hasNext()){
							Entry<Integer, Boolean> entry =it.next();
							if((Boolean) entry.getValue()){
								temSb.append(items[(Integer) entry.getKey()]+",");
							}
						}
						videoType.setText(temSb.deleteCharAt(temSb.length()-1));
						MultiChoiceId.clear();
					}
				}
			});
			builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MultiChoiceId.clear();
				}
			});
			builder.create().show();
			break;
		}
			
	}
	/**是否首次分享*/
	public void FirstTimeOrNot(){
		Bundle bundle = this.getIntent().getExtras();
		String[]_tempArr = bundle.getStringArray("sharemsg");
		sid = _tempArr[0];
		Log.v(TAG, "sid:"+sid);
		String value = _tempArr[1];
		if("status".equals(value)){
			//隐藏个人信息输入框
			userLayout.setVisibility(View.GONE);
			/**如果已经分享过,将之前的分享信息默认显示在分享页面*/
			//TODO
			Log.v(TAG, ""+bundle.containsKey("shareinfo"));
			if(bundle.containsKey("shareinfo")){
				LiveVideo livevideo= (LiveVideo)bundle.getParcelable("shareinfo");
				shareTitle.setText(livevideo.getLivename());
				shareDeclare.setText(livevideo.getIntroduction());
			}
		}else{
			isFirstShare = true;
		}
	}
	/**设置上传按钮显示或隐藏*/
	public void showImageButton(boolean isShow){
		if(isShow){
			camera.setVisibility(View.VISIBLE);
			photo.setVisibility(View.VISIBLE);
		}else{
			camera.setVisibility(View.GONE);
			photo.setVisibility(View.GONE);
		}
	}
	/**拍照*/
	public void takepcamera(){
		if(firpic && secpic){
			return;
		}
		_uri = BitmapUtils.getImageFromCamer(this, generateName(), CAMERA_REQUEST_CODE);
		Log.v(TAG, "_uri:"+_uri.toString());
	}
	/**相册选取*/
	public void  localphotos(){
		if(firpic && secpic){
			return;
		}
		BitmapUtils.getImageFromPhoto(this, IMAGE_REQUEST_CODE);
	}
	
	public String generateName(){
		if(!firpic){
			_imagename = BitmapUtils.getPath()+"firpic.jpg";
		}else{
			_imagename = BitmapUtils.getPath()+"secpic.jpg";
		}
		return _imagename;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		 if (resultCode != RESULT_CANCELED) {
			 switch (requestCode) {
		        case IMAGE_REQUEST_CODE:
		        	if(null!=data){
		        		_uri = data.getData();
		        		showImage();
		        	}
		             break;
		        case CAMERA_REQUEST_CODE:
	                if (Tools.hasSdcard()) {
	                	showImage();
	                } else {
	                        APP.ShowToast(getString(R.string.nosdcard));
	                }
	                break;
		       }
	        super.onActivityResult(requestCode, resultCode, data);       
		 }
	}
	
	@SuppressLint("NewApi")
	public void showImage(){
		InputStream in = null;
	    bmFactoryOptions  = new BitmapFactory.Options();
		bmFactoryOptions.inTempStorage=new byte[100*1024];
		bmFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		bmFactoryOptions.inPurgeable = true;
		bmFactoryOptions.inSampleSize = 50;//缩小50倍
		bmFactoryOptions.inInputShareable = true;
		try {
			//bmFactoryOptions.inJustDecodeBounds = false;
			in= getContentResolver().openInputStream(_uri);
			bm = BitmapFactory.decodeStream(in,null,bmFactoryOptions);
			uriList.add(_uri);
			Log.v(TAG, "bm:"+bm.getByteCount());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!firpic){
			verify1.setVisibility(View.VISIBLE);
			verify1.setImageBitmap(bm);
			firpic = true;
		}else if(!secpic){
			verify2.setVisibility(View.VISIBLE);
			verify2.setImageBitmap(bm);
			secpic = true;
		}
		if(firpic && secpic){
			showImageButton(false);
			addveri.setVisibility(View.GONE);
		}
	}
	/**提交分享申请*/
	public void sendInfo(){
		LogUtil.d(TAG, "sendInfo method firstLine!");
		InputStream in = null;
		_dlgWait.show();
		_dlgWait.UpdateTextNoDelay(getString(R.string.checking_msg));
		if(isVerfied(isFirstShare)){
			if(isFirstShare){
				/**从图片压缩开始 产生耗时操作*/
				_dlgWait.UpdateTextNoDelay(getString(R.string.up_relinfo));
				bmFactoryOptions.inSampleSize =3;
				for(int i=1;i<uriList.size()+1;i++){
					File f;
					try {
						in = getContentResolver().openInputStream(uriList.get(i-1));
						f = BitmapUtils.CompressToFile(BitmapFactory.decodeStream(in,null,bmFactoryOptions), _userId.concat(String.valueOf(i)).concat(".png"), 50);
						Log.v(TAG, "filename"+f.getName());
						fileList.add(f);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}finally{
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			uploadShareMsg(isFirstShare);
		}
	}
	
	public void uploadShareMsg(boolean isFirst){
	    client = new AsyncHttpClient();
	    client.setTimeout(1000*60);//整个上传过程设置超时时间为1分钟
		if(isFirst){
			params = new RequestParams();
			params.put("userId", _userId);
			String url = Constants.hostUrl+"/mobile/upload";
			params.put("path", "verify");
			//第一张
			try {
				params.put("file", fileList.get(0));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			client.post(url, params, new AsyncHttpResponseHandler() {
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					sendToHandler("first",true);
				}
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					sendToHandler("first",false);
				}
			});
			//第二张
			try {
				params.put("file", fileList.get(1));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			client.post(url, params, new AsyncHttpResponseHandler() {
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
					sendToHandler("second",true);
				}
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					sendToHandler("second",false);
				}
			});
		}else{
			sendShareMsg();
		}
	}
	/**提交用户真实信息*/
	public void sendRelUser(){
		client = new AsyncHttpClient();
		client.setTimeout(1000*15);
		params = new RequestParams();
		Log.v(TAG, "sessionid:"+Constants.sessionId);
		params.put("userId", _userId);
		params.put("realName", shareName.getText().toString());
		params.put("idcard", shareIdCard.getText().toString());
		params.put("acard", "verify/".concat(_userId).concat("1.png")); 
		params.put("bcard", "verify/".concat(_userId).concat("2.png"));
		params.put("sessionId", Constants.sessionId);
		Log.v(TAG, "request url:"+Constants.hostUrl+"/android/saveUserMessage"+";and pararam:"+params.toString());
		client.post(Constants.hostUrl+"/android/saveUserMessage", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						String result = json.getString("result");
						Log.v(TAG, "提交用户真实信息结果:"+result);
						if("nologin".equals(result)){
							_dlgWait.dismiss();
							//BaseApplication.getInstance().relogin();
						}
						if("true".equals(result)){
							//保存成功,如果是首次分享，继续提交分享信息(其实是100%成立)
							if(isFirstShare){
								sendShareMsg();
							}
							
						}
						if("false".equals(result)){
							//数据库操作失败
							_dlgWait.dismiss();
							//APP.ShowToast("上传失败(上传用户真实信息)");
							APP.ShowToast(err_up);
							finish();
						}
						if("exception".equals(result)){
							//数据库异常
							_dlgWait.dismiss();
							//APP.ShowToast("上传失败sql异常)");
							APP.ShowToast(err_up);
							finish();
						}
					} catch(JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				_dlgWait.dismiss();
				//APP.ShowToast("网络连接失败(上传用户真实信息)");
				APP.ShowToast(err_con);
				finish();
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				Log.v(TAG, "responseString:"+responseString);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONArray errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Log.v(TAG, "JSONArray errorResponse:"+errorResponse.toString());
			}
		});
	}
	/**提交分享信息*/
	public void sendShareMsg(){
		if(_dlgWait.isShowing()){
			_dlgWait.UpdateTextNoDelay(getString(R.string.up_shareinfo));
		}
		client = new AsyncHttpClient();
		client.setTimeout(1000*15);
		params = new RequestParams();
		params.put("sessionId", Constants.sessionId);
		params.put("userId", _userId);
		params.put("deviceId", sid);
		params.put("livename", shareTitle.getText().toString());
		params.put("introduction", shareDeclare.getText().toString());
		params.put("starttime", DateUtil.getCurrentStringDate("yy-dd HH:mm"));
		params.put("type", 0);
		client.post(Constants.hostUrl+"/android/saveShareMsg", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						String result = json.getString("result");
						if("nologin".equals(result)){
							_dlgWait.dismiss();
							//BaseApplication.getInstance().relogin();
						}
						if("true".equals(result)){
							_dlgWait.dismiss();
							//APP.ShowToast("上传成功(上传分享信息)");
							APP.ShowToast(suc_up);
							
							//发送广播
							Intent intent = new Intent("com.views.bovine.Fun_AnalogVideo");
							sendBroadcast(intent);
							finish();
						}
						if("false".equals(result)){
							_dlgWait.dismiss();
							//APP.ShowToast("网络连接失败(上传分享信息)");
							APP.ShowToast(err_up);
							finish();
						}
					} catch(JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				_dlgWait.dismiss();
				//APP.ShowToast("网络连接失败(上传分享信息)");
				APP.ShowToast(err_con);
				finish();
			}
		});
	}
	
	public void sendToHandler(String which,boolean status){
		if(flags!=null){
			flags.put(which, status);
			final Message msg = new Message();
			msg.obj = flags;
			handler.sendMessage(msg);
		}
	}
	
	/**页面申请信息是否通过验证
	 * @param isFirst是否首次分享*/
	public boolean isVerfied(boolean isFirst){
		flag = false;
		if(proofSimple()){
			if(isFirst){
				proofUsers();
			}
		}
		return flag;
	}
	/**验证实名信息*/
	public boolean proofUsers(){
		if(!isRelName(shareName.getText().toString())){
			flag = false;
		}else if(!isIdCard(shareIdCard.getText().toString())){
			flag = false;
		}else if(uriList.size()!=2){
			hideDilg();
			APP.ShowToast(getString(R.string.photos_tip));
			flag = false;
		}else{
			flag = true;
		}
		return flag;
	}
	/**验证分享信息*/
	public boolean proofSimple(){
		if("".equals(shareTitle.getText())||shareTitle.getText()==null||shareTitle.getText().length()==0){
			hideDilg();
			APP.ShowToast(getString(R.string.title_tip));
			return flag;
		}else if("".equals(videoType.getText())||videoType.getText()==null){
			hideDilg();
			APP.ShowToast(getString(R.string.group_tip));
			return flag;
		}else{
			flag = true;
		}
		return flag;
	}
	
	public boolean isRelName(String name){
		boolean tempFlag = false;
		if("".equals(name)||name==null){
			hideDilg();
			APP.ShowToast(getString(R.string.rename_tip));
			return tempFlag;
		}else{
			String str = "[\\u4e00-\\u9fa5]+";
			Pattern p = Pattern.compile(str);
			Matcher m = p.matcher(name.trim());
			tempFlag = m.matches();
			if(!tempFlag){
				hideDilg();
				APP.ShowToast(getString(R.string.ch_tip));
				return tempFlag;
			}
		}
		return tempFlag;
	}
	
	public boolean isIdCard(String idNumber){
		boolean tempFlag = false;
		if("".equals(idNumber)||idNumber==null){
			hideDilg();
			APP.ShowToast(getString(R.string.idcard_tip));
			return tempFlag;
		}else{
			String str = "\\d{15}|\\d{17}[0-9Xx]";
			Pattern p = Pattern.compile(str);
			Matcher m = p.matcher(idNumber);
			tempFlag = m.matches();
			if(!tempFlag){
				hideDilg();
				APP.ShowToast(getString(R.string.idfit_tip));
				return tempFlag;
			}
		}
		return tempFlag;
	}
	
	public void hideDilg(){
		if(_dlgWait.isShowing()){
			_dlgWait.dismiss();
		}
	}
	
	public void showDialog(final TextView v)
	{
		DateTimePickerDialog dialog  = new DateTimePickerDialog(this, System.currentTimeMillis());
		dialog.setOnDateTimeSetListener(new OnDateTimeSetListener()
	      {
			public void OnDateTimeSet(AlertDialog dialog, long date)
			{
				
				//Toast.makeText(MainActivity.this, "您输入的日期是："+getStringDate(date), Toast.LENGTH_LONG).show();
				//v.setText(getStringDate(date));
			}
		});
		dialog.show();
	}
	/**
	* 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
	*
	*/
	public static String getStringDate(Long date) 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String dateString = formatter.format(date);
		return dateString;
	}
	
	public void onBackPressed(){
		super.onBackPressed();
		finish();
	}

	
	final Handler handler  = new Handler(){
		public void handleMessage(Message message){
			Log.v(TAG,"dlgWait:"+_dlgWait);
			if(!_dlgWait.isShowing()){
				return;
			}else{
				flags = (Map<String,Boolean>) message.obj;
				int count=0;
				for(Entry<String, Boolean> entry:flags.entrySet()){
				    if(!entry.getValue()){
				    	_dlgWait.dismiss();
				    	//APP.ShowToast("网络连接失败(上传图片)");
				    	APP.ShowToast(err_con);
				    	flags = null;
				    	finish();
				    	break;
				    }
				    count++;
				}
				if(count==2){
					Log.v(TAG, "count:"+count);
					sendRelUser();//照片上传成功之后再提交真实信息
				}
				
			}
		}
	};
}
