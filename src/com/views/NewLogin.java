package com.views;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.alibaba.fastjson.JSON;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.utils.Loger;
import com.utils.MD5Util;
import com.utils.SIMCardInfo;
import com.utils.httpClientUtils;

@SuppressLint("ShowToast")
public class NewLogin extends Activity implements OnClickListener{

	public final static String SAVEFILE = "Info_Login";
//	public static NewLogin instance = null;
	AutoCompleteTextView  _user;
	EditText _pwd;
	Context context = null;
	com.views.CustomScrollView login_main;
	public Dlg_Wait _dlgWait = null;
	Button _btnLogin;
	
	@SuppressLint("SimpleDateFormat")
	public static SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss.SSS");
	
	public static String logPath = "";//日志
	private String TAG = NewLogin.class.getSimpleName();
	public String _actionName = "";
	private MyHandler _handler = new MyHandler();
	SharedPreferences _ipPreferences;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_login);

		login_main =(com.views.CustomScrollView) findViewById(R.id.logindialog_space);
		
		login_main.setOnTouchListener(new OnTouchListener(){  			  
			public boolean onTouch(View arg0, MotionEvent arg1)  
			{  
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);  
				return imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);  
			}  
		});  
		
		
		
		_dlgWait = new Dlg_Wait(this, R.style.dialog);
		BaseApplication.getInstance().addActivity(this);
		//instance = this;
		context = this;
		//解决http请求报错加的二行代码
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
        
		Constants.sessionId = 0;
		//_actionName = "/android/doLogin";
		_actionName = "/logon_v1";
		
		_btnLogin = (Button) findViewById(R.id.login_btn_login);
		_btnLogin.setOnClickListener(this);
		findViewById(R.id.login_a_reg).setOnClickListener(this);
		findViewById(R.id.login_a_retrieve).setOnClickListener(this);
		
		_user = (AutoCompleteTextView) this.findViewById(R.id.tvUser);
		_user.setOnKeyListener(onKeyListener);
		_pwd = (EditText) this.findViewById(R.id.etPassword);
		
		Drawable draw = getResources().getDrawable(R.drawable.login_text3);
		draw.setBounds(0, 0, 50, 50);
		_user.setCompoundDrawables(draw, null, null, null);
		
		Drawable drawpwd = getResources().getDrawable(R.drawable.login_text4);
		drawpwd.setBounds(0, 0, 50, 50);
		_pwd.setCompoundDrawables(drawpwd, null, null, null);
		
		String tmp = getFilePath();
		setFilePath(tmp);
		//打开登录框  关闭显示的输入
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		ReadUserInfo();
		_ipPreferences = getSharedPreferences(SplashScreen.HOSTIP, this.MODE_PRIVATE);
		Constants.hostUrl = _ipPreferences.getString("hostIP", "");
		//连本地服务
//		Constants.hostUrl = "http://10.12.6.121:8080/NineCloud";
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
	@SuppressLint("SdCardPath")
	public void setFilePath(String path){
		if(path.equals("/data")){
			path = getApplicationContext().getFilesDir().getAbsolutePath();//获取程序安装路径
			//path = "/data/data/com.manniu.manniu"; //manniu的安装路径
		}
		logPath = path +"manniu/logs/";
	}

	@Override
	public void onClick(View v) {
		try{
		switch (v.getId()) {
		case R.id.login_btn_login:
			//login
			final String user = _user.getText().toString();
			final String pwd = _pwd.getText().toString();
			final String time = sdf.format(new Date());
			Loger.print("logining... user:" + user + " password:" + pwd + " time:" + time);
			if("".equals(user.trim())||"".equals(pwd.trim())){
				showToast(getText(R.string.Err_USER_NULL).toString());
			}else{
				String str = getText(R.string.logining).toString();
				if(isNetworkAvailable(this)){
					if(Constants.netWakeType == 0) str = getText(R.string.netWake_mobile).toString();
				}else{
					showToast(getText(R.string.Err_NetConnect).toString());
					return;
				}
				_dlgWait.show();
				_dlgWait.UpdateText(str);
				//final String tempStr = generateLogin(user,pwd,getRegistrationId(),getDeviceInfo(),time);
				new Thread(new Runnable() {
					@Override
					public void run() {
						String tempStr = "?username="+user+"&password="+MD5Util.MD5(MD5Util.MD5(pwd))+"&registrationId="+getRegistrationId()+"&deviceInfo="+getDeviceInfo()+"&time_token="+time;
//						login(Constants.hostUrl+_actionName+tempStr.replaceAll(" ", "%20"));
						
//						HashMap<String, String> params = new HashMap<String, String>();
//						params.put("username", user);
//						params.put("password", MD5Util.MD5(MD5Util.MD5(pwd)));
//						params.put("registrationId", getRegistrationId());
//						params.put("deviceInfo", getDeviceInfo().replaceAll(" ", "%20"));
//						params.put("time_token", time);
						loginForETS(tempStr.replaceAll(" ", "%20"));
						
						/*String tempStr = user+"&"+pwd+"&"+getRegistrationId()+"&"+getDeviceInfo()+"&"+time;
						try {
							byte[] key = DES.initSecretKey();
							Key k = DES.toKey(key);
							byte[] encryptData = DES.encrypt(tempStr.getBytes(), k);
							System.out.println("加密后数据: Str:"+new String(encryptData));
							loginOfGet(Constants.hostUrl+_actionName+"?username="+new String(encryptData).replaceAll(" ", "%20"));
						} catch (Exception e) {
							e.printStackTrace();
						}*/
						
					}
				}).start();
				}
				break;
			case R.id.login_a_reg:
				//Log.v("onClick", "login_a_reg!");
				forward(NewRegActivity.class, "register");
				break;
			case R.id.login_a_retrieve:
				//Log.v("onClick", "login_a_retrieve!");
				forward(NewRegActivity.class, "retrievePwd");
				break;
			}
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}

	
	//使用ETS方式登录
	public void loginForETS(String params){
		try {
			JSONObject json = null;
			Map<String, Object> map = HttpURLConnectionTools.get(Constants.ETShostUrl+_actionName+params);
			if (Integer.parseInt(map.get("code").toString()) != 200) {
				showToast(getResources().getString(R.string.E_SER_FAIL));
			}else{
				json = new JSONObject(map.get("data").toString());
				String msg = "";
				String data = "";
				try {
					msg = json.getString("msg");
					if("success".equals(msg)){
						data = json.getString("data");
						String time_token = "";
						if(json.has("time_token")){
							time_token = json.getString("time_token");
						}else{
							Log.d(TAG, "time_token is null!");
						}
						Constants.session_Id = json.getString("session_id");
//						if(JSON.parseObject(data).getInteger("state") == 0){
//							Message message = new Message();
//							message.what = XMSG.LOGIN_USER_DISABLE;
//							_handler.sendMessage(message);
//						}else{
//						}
						SaveUserInfo(data, time_token);
						load();
						LogUtil.d(TAG, "login success....");
					}else{
						Message message = new Message();
						message.what = XMSG.LOGIN_USER_ERROR;
						_handler.sendMessage(message);
					}
				} catch (JSONException e) {
					LogUtil.e("NewLogin", e);
					Loger.print("login onSuccess exception time:" + sdf.format(new Date()));
				}
				
				
			}
		} catch (Exception e) {
			_handler.sendEmptyMessage(XMSG.LOGIN_NETWORK_ERROR);
			LogUtil.e("NewLogin", ExceptionsOperator.getExceptionInfo(e));
		} finally {
			try {
				if(_dlgWait!= null && _dlgWait.isShowing()) _dlgWait.dismiss();
			} catch (Exception e2) {
			}
		}
	}
	
	/**
	 * 使用get方式登录
	 * @param username
	 * @param password
	 * @return
	 */
	public void login(String url) {
		JSONObject json = null;
		HttpURLConnection conn = null;
		try {
			// 利用string url构建URL对象
			URL mURL = new URL(url);
			conn = (HttpURLConnection) mURL.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(20*1000);
			conn.setConnectTimeout(20*1000);
			conn.connect();
			InputStream is = conn.getInputStream();
			String result = "";
			if (is != null) {
				result = HttpURLConnectionTools.getStringFromInputStream(is);
			}
			int responseCode = conn.getResponseCode();
			LogUtil.d(TAG, "conn.getResponseCode()"+responseCode);
			if (responseCode == 200) {
				json = new JSONObject(result);
				String msg = "";
				String data = "";
				try {
					msg = json.getString("msg");
					if("success".equals(msg)){
						data = json.getString("data");
						String time_token = "";
						if(json.has("time_token")){
							time_token = json.getString("time_token");
						}else{
							Log.d(TAG, "time_token is null!");
						}
						if(JSON.parseObject(data).getInteger("state") == 0){
							Message message = new Message();
							message.what = XMSG.LOGIN_USER_DISABLE;
							_handler.sendMessage(message);
						}else{
							SaveUserInfo(data, time_token);
							load();
							LogUtil.d(TAG, "login success....");
						}
					}else{
						Message message = new Message();
						message.what = XMSG.LOGIN_USER_ERROR;
						_handler.sendMessage(message);
					}
				} catch (JSONException e) {
					LogUtil.e("NewLogin", e);
					Loger.print("login onSuccess exception time:" + sdf.format(new Date()));
				}finally{
					try {  
						is.close();
	                } catch (Exception e) {  
	                }
				}
			} else {
				Message message = new Message();
				message.what = XMSG.LOGIN_NETWORK_ERROR;
				_handler.sendMessage(message);
				//重新解析域名
				updateHostIP();
				LogUtil.i("httpClientUtils", "访问失败" + responseCode);
			}
		} catch (Exception e) {
			Message message = new Message();
			message.what = XMSG.LOGIN_NETWORK_ERROR;
			_handler.sendMessage(message);
		} finally {
			try {
				if(_dlgWait.isShowing()) _dlgWait.dismiss();
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e2) {
			}
		}
	}
	
	public HttpClient getHttpClient(){
		HttpParams mHttpParams=new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(mHttpParams, 20*1000);
		HttpConnectionParams.setSoTimeout(mHttpParams, 20*1000);
		HttpConnectionParams.setSocketBufferSize(mHttpParams, 8*1024);
		HttpClientParams.setRedirecting(mHttpParams, true);
		HttpClient httpClient=new DefaultHttpClient(mHttpParams);
		return httpClient;
	}
	
	@SuppressWarnings("rawtypes")
	private void forward(Class clazz, String type){
		Intent intent = new Intent(this, clazz);
		intent.putExtra("type", type);
		startActivity(intent);
	}
	
	private void showToast(String msg){
		Toast.makeText(this, msg, 2000).show();
	}
	
	private void load(){
		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		closeLogin();
	}
	
	public void closeLogin(){
		try {
			if(_dlgWait.isShowing()) _dlgWait.dismiss();
			//instance = null;
			NewLogin.this.finish();
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	@SuppressWarnings("static-access")
	public void ReadUserInfo() {
		SharedPreferences preferences = getSharedPreferences(SAVEFILE, this.MODE_PRIVATE);
		String strUser = preferences.getString("user0", "");
		String strPwd = preferences.getString("pwd0", "");
		Constants.userName = strUser;
		_user.setText(strUser);
		_pwd.setText(strPwd);
		LogUtil.d(TAG, "ReadUserInfo");
		if(preferences != null){
			if(preferences.getString("user0", "")!=""){
				onClick(_btnLogin);
			}
		}
	}

	// 写 SharedPreferences
	@SuppressWarnings("static-access")
	public int SaveUserInfo(String userData, String token) {
		Loger.print("SaveUserInfo time:" + sdf.format(new Date())+" userData:"+userData + " token:" +token);
		SharedPreferences preferences = getSharedPreferences(SAVEFILE, this.MODE_PRIVATE);
		Editor editor = preferences.edit();
		Constants.userName = _user.getText().toString();
		editor.putString("user0", Constants.userName);
		editor.putString("pwd0", _pwd.getText().toString());
		
		com.alibaba.fastjson.JSONObject  obj = JSON.parseObject(userData);
		for(String key : obj.keySet()){
			if(!("".equals(obj.getString(key))||null == obj.getString(key))){
				editor.putString(key, obj.getString(key));
				if("sid".equals(key)){
					Constants.userid = obj.getString(key);

					//获取是否推送的配置信息，
					String set_push = getSharedPreferences("Info_Set", this.MODE_PRIVATE).getString("push", "");
					String alias = "";
					if(!"off".equals(set_push)){
						alias = MD5Util.MD5(obj.getString(key));
					}
					Loger.print("SaveUserInfo setJpushAlias time:" + sdf.format(new Date())+" token:"+token);
					//setAlias(getRegistrationId(), obj.getString(key), token);
					Message message = new Message();
					message.what = XMSG.Alias;
					message.obj = getRegistrationId()+","+alias+","+token;
					_handler.sendMessageDelayed(message, 5000);
				}
			}
		}
		editor.commit();
		return 0;
	}
	
	public void updateHostIP(){
		final Editor _editor = _ipPreferences.edit();
		new Thread(){
			@Override
			public void run(){
				Constants.hostUrl = httpClientUtils.getServerHostAddress(getResources().getString(R.string.server_address));
				_editor.putString("hostIP", Constants.hostUrl);
				_editor.commit();
			}
		}.start();
	}
	
	/**
	 * 用户登录的时候 给jpush设置别名
	 * @param registrationId
	 * @param alias
	 * @param time_token 
	 */
	public void setAlias(String param) {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("registrationId", param.split(",")[0]);
			params.put("alias", param.split(",")[1]);
			params.put("time_token", param.split(",")[2]);
			params.put("sessionId", Constants.sessionId+"");
			Map<String, Object> map = HttpURLConnectionTools.post(Constants.hostUrl + "/jpush/setAlias",params);
			if (Integer.parseInt(map.get("code").toString()) != 200) {
				showToast(getResources().getString(R.string.E_SER_FAIL));
			}
		} catch (Exception e) {
		}
		/*Map<String, Object> map = HttpURLConnectionTools.get(Constants.hostUrl + "/jpush/setAlias"+params);
		if (Integer.parseInt(map.get("code").toString()) != 200) {
			showToast(this.getResources().getString(R.string.E_SER_FAIL));
		}*/
	}
	
	//2.接受消息
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		// 子类必须重写此方法,接受数据
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case XMSG.LOGIN_USER_ERROR:
				showToast(getText(R.string.Err_USER_ERROR).toString());
				break;
			case XMSG.LOGIN_USER_DISABLE:
				showToast(getText(R.string.Err_USER_DISABLE).toString());
				break;
			case XMSG.LOGIN_NETWORK_ERROR:
				showToast(getText(R.string.Err_CONNET).toString());
				break;
			case XMSG.Alias:
				String param = (String) msg.obj;
				if(param.split(",").length > 0){
					//String tempStr = "?registrationId="+param.split(",")[0]+"&alias="+param.split(",")[1]+"&time_token="+param.split(",")[2]+"&sessionId="+Constants.sessionId;
					setAlias(param);
				}
				break;
			}
		}
	}
	
	/**  
     * 检测网络连接是否可用  
     * @param ctx  
     * @return true 可用; false 不可用  
     * 0:mobile 1:WIFI
     */  
	private boolean isNetworkAvailable(Context context) {
		if (context != null) {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (cm == null) {
				return false;
			}
			NetworkInfo[] netinfo = cm.getAllNetworkInfo();
			if (netinfo == null) {
				return false;
			}
			for (int i = 0; i < netinfo.length; i++) {
				if (netinfo[i].isConnected()) {
					Constants.netWakeType = netinfo[i].getType();
					return true;
				}
			}
		}
		return false;
	}
	
	private OnKeyListener onKeyListener = new OnKeyListener(){
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			Log.d(TAG, "keyCode:" + keyCode);
			if(keyCode == 66){
				_pwd.setFocusable(true);
			}
			if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
				System.exit(0);		// 退出操作
			}
			return false;
		}
	}; 

	public String getRegistrationId(){
		return JPushInterface.getRegistrationID(this);
	}

	public String getDeviceInfo(){
		SIMCardInfo info = new SIMCardInfo(this);
		return info.getDeviceInfo();
	}
	
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}
	
	@Override
    protected void onStop(){
		super.onStop();
    }
	@Override
    protected void onDestroy(){
		_dlgWait = null;
		context = null;
		//instance = null;
		super.onDestroy();
    }
	
}