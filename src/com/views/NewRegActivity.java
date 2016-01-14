package com.views;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adapter.HttpUtil;
import com.adapter.RandomUtil;
import com.adapter.Utils;
import com.alibaba.fastjson.JSON;
import com.exinxi.Sms;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.LanguageUtil;
import com.utils.LogUtil;
import com.utils.Loger;
import com.utils.TwilioUtils;
/**
 * 用户注册  、 找回密码
 * @author pc
 *
 */
public class NewRegActivity extends Activity implements OnClickListener, OnTaskListener{

	private static final String TAG = "NewRegActivity";

	String pattern = "^1[3|4|5|7|8][0-9]\\d{8}$";
	
	String pattern_en = "^\\d{10}$";
	
	//美国/北美号码（包括手机和座机）为十位数
	//(\d{10})|(\d{3}[-\.\s]\d{3}[-\.\s]\d{4})|(\d{3}-\d{3}-\d{4}\s(x|(ext))\d{3,5})|(\(\d{3}\)-\d{3}-\d{4})
	
	public enum Type{
		register, retrievePwd
	}
	
    AutoCompleteTextView tv_mobile;
    EditText vaildCode;
    
    Button btn_reg, btn_vaildCode;
    
    Handler handler;
    //注册/找回
    String type;
    
    Map<String, String> maps = new HashMap<String, String>();
    
    String mobile, code;
    
    private Dlg_WaitForActivity _dlgWait = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_reg_activity);
		
		btn_reg = (Button)findViewById(R.id.reg_btn_reg);
		
		if("en".equals(LanguageUtil.getLanguageEnv())){
			pattern = pattern_en;
		}else if("zh_CN".equals(LanguageUtil.getLanguageEnv())){
			//
		}else{
			//other
		}
		
		getParams();
		
		tv_mobile = (AutoCompleteTextView)findViewById(R.id.regtvUser);
		vaildCode = (EditText)findViewById(R.id.regetPassword);
		if("retrievePwd".equals(type)){
			btn_reg.setText(getString(R.string.pwd_refind));
		}
		btn_reg.setOnClickListener(this);
		btn_vaildCode = (Button)findViewById(R.id.validCode);
		btn_vaildCode.setOnClickListener(this);
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				String time = msg.getData().getString("time");
				if("0".equals(time)){
					offThread();
					btn_vaildCode.setEnabled(true);
					btn_vaildCode.setText(getString(R.string.register_getcode));
				}else{
					btn_vaildCode.setText(time);
				}
			}
		};
		_dlgWait = new Dlg_WaitForActivity(this,R.style.dialog);
		
		tv_mobile.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					vaildMobile();
				}
			}
		});
		
		Drawable draw = getResources().getDrawable(R.drawable.login_text3);
		draw.setBounds(0, 0, 50, 50);
		tv_mobile.setCompoundDrawables(draw, null, null, null);
		
		Drawable drawpwd = getResources().getDrawable(R.drawable.login_text4);
		drawpwd.setBounds(0, 0, 50, 50);
		vaildCode.setCompoundDrawables(drawpwd, null, null, null);
		
	}
	
	private void getParams(){
		type = getIntent().getExtras().getString("type");
	}
	
	private void forward(Class clazz, Bundle extras){
		Intent intent = new Intent(this, clazz);
		intent.putExtras(extras);
		startActivity(intent);
		NewRegActivity.this.finish();
		_dlgWait.dismiss();
	}
	
	private void offThread(){
		handler.removeCallbacks(runnable);
	}
	
	/**
	 * 手机号码失去焦点验证
	 * @return
	 */
	private boolean vaildMobile(){
		mobile = tv_mobile.getText().toString();
		code = vaildCode.getText().toString();
		if(!isEmpty(mobile)){
			if(Pattern.matches(pattern, mobile.trim())){
					return true;
			}else{
				alert(getString(R.string.phnum_wrong));
				return false;
			}
		}else{
			alert(getString(R.string.phnum_empty));
			return false;
		}
	}
	
	/**
	 * 注册按钮验证
	 * @return
	 */
	private boolean vaild(){
		mobile = tv_mobile.getText().toString();
		code = vaildCode.getText().toString();
		if(!isEmpty(mobile)){
			Log.v(TAG, ""+mobile);
			if(Pattern.matches(pattern, mobile.trim())){
				Log.d(TAG, "session:" + maps.get(mobile));
				Log.d(TAG, "code:" + Utils.MD5(code).toUpperCase());
					if(Utils.MD5(code).toUpperCase().equals(maps.get(mobile))){
						return true;
					}else{
						alert(getString(R.string.register_wrongcode));
						return false;
					}
			}else{
				alert(getString(R.string.phnum_wrong));
				return false;
			}
		}else{
			alert(getString(R.string.phnum_empty));
			return false;
		}
	}
	
	/**
	 * 验证码按钮验证
	 * @return
	 */
	private boolean codeVaild(){
		mobile = tv_mobile.getText().toString();
		code = vaildCode.getText().toString();
		if(!isEmpty(mobile)){
			Log.d(TAG, "mobile:" + mobile);
			if(Pattern.matches(pattern, mobile)){
				return true;
			}else{
				alert(getString(R.string.phnum_wrong));
				return false;
			}
		}else{
			alert(getString(R.string.phnum_empty));
			return false;
		}
	}
	
	/**
	 * 注册按钮提交
	 * @param url
	 * @param _mobile
	 */
	private void regSubmit(String url, String _mobile){
		Log.d(TAG, "regSubmit.url:" + url);
		Log.d(TAG, "regSubmit.mobile:" + _mobile);
		
		RequestParams params = new RequestParams();
		params.put("mobile", _mobile);
		Log.d(TAG, "regSubmit.params:" + params.toString());
		HttpUtil.get(Constants.hostUrl+url, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					Log.d(TAG, "regSubmit.onSuccess:" + json.toString());
					String msg = "";
					try {
						msg = json.getString("msg");
						if("success".equals(msg)){
							//get user info  forward
							String sid = json.getJSONObject("data").getString("sid");
							//Log.v(TAG, "—————————————服务器端返回的用户ID———————————————："+sid);
							Bundle extras = new Bundle();
							extras.putString("userId", sid);
							extras.putString("userName", tv_mobile.getText().toString());
							forward(NewRegSetPwdActivity.class, extras);
						}else if("exist".equals(msg)){
							_dlgWait.dismiss();
							alert(getString(R.string.phnum_repeat));
						}else if("none".equals(msg)){
							_dlgWait.dismiss();
							alert(getString(R.string.phnum_none));
						}else if("many".equals(msg)){
							_dlgWait.dismiss();
							alert(getString(R.string.phnum_error));
						}else if("exception".equals(msg)){
							_dlgWait.dismiss();
							alert(getString(R.string.register_serverErr));
						}
					} catch (JSONException e) {
						LogUtil.e(TAG, "register exception:" + e.getMessage());
					}
				}
			}
			
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json){
				_dlgWait.dismiss();
				alert(getResources().getString(R.string.E_SER_FAIL));
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String errorString, Throwable throwable) {
				_dlgWait.dismiss();
				alert(getResources().getString(R.string.E_SER_FAIL));
			}
			});
	}
	
	/**
	 *  通过服务器端 发送验证码
	 * @param mobile
	 */
	@Deprecated
	private void codeSubmit(String _mobile){
		RequestParams params = new RequestParams();
		params.put("mobile", _mobile);
		HttpUtil.get(Constants.hostUrl+"/mobile/getCode", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				if(statusCode == 200){
						 com.alibaba.fastjson.JSONObject obj = JSON.parseObject(response.toString());
								 Log.d(TAG, obj.toJSONString());
						 obj = JSON.parseArray(obj.getString("result")).getJSONObject(0);
						 String smsCode = obj.getString("smscode");
						// alert(smsCode);
						// alert(obj.getString("respcode"));
						 maps.put(mobile, smsCode);
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				alert(getResources().getString(R.string.E_SER_FAIL));
			}
		});
		
	} 
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reg_btn_reg:
			Loger.print("mobile register. mobile:" + mobile);
			if(vaild()){
				String url;
				String text = "";
				if("register".equals(type)){
					url = "/android/register";
					text = getText(R.string.openning_Registered).toString();
				}else{
					url = "/android/retrievePwd";
					text = getText(R.string.set_pwd).toString();
				}
				_dlgWait.show();
				_dlgWait.UpdateText(text);
				regSubmit(url, mobile);
			}
			break;
		case R.id.validCode:
			mobile = tv_mobile.getText().toString();
			code = vaildCode.getText().toString();
			if(codeVaild()){
				String _code = RandomUtil.generateNumString(4);
				maps.put(mobile, Utils.MD5(_code).toUpperCase());
				LogUtil.d(TAG, "language:" + LanguageUtil.getLanguageEnv());
				if(LanguageUtil.getLanguageEnv().contains("en")){
					TwilioUtils.sendSms(mobile, _code);
				}else if("zh_CN".equals(LanguageUtil.getLanguageEnv())){
					new smsTask().execute(mobile, _code);
				}else {
					//
				}
				btn_vaildCode.setEnabled(false);
				handler.post(runnable);
			}
			break;
		default:
			break;
		}
		
	}

	private boolean isEmpty(String string ){
		return "".equals(string) || string == null;
	}
	
	private void alert(String text){
		Toast.makeText(this, text, 0).show();
	}
	
	/**
	 * 短信发送 
	 */
	private class smsTask extends AsyncTask<String, String, String>{
		@Override
		protected String doInBackground(String... params) {
			String ret = "";
			try {
					ret = Sms.SendCode(params[0], params[1]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ret;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(result != null){
//				String respcode = JSON.parseObject(result).getJSONObject("resp").getString("respCode");
//				Loger.print("验证码短信发送:"+respcode);
				Loger.print("" + result);
				if(result.startsWith("0")){
					Loger.print("sms send success!");
				}
			}
			super.onPostExecute(result);
		}
	}
	
	/**
	 * 注册时判断用户名是否存在
	 * @param username
	 * @return
	 */
	public boolean exist(String username){
		HttpUtil.get(Constants.hostUrl+"/LoginAction_executename.action?username="+username,
				null, new AsyncHttpResponseHandler(){
					@Override
					public void onFailure(int statusCode, Header[] headers, byte[] response,
							Throwable throwable ) {
					}
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] response) {
						String msg = "";
						try {
							msg = new String(response, "UTF-8");
						} catch (UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
						Log.v("response:", msg);
						alert("toast:"+msg);
					}
		});
		return false;
	}
	
	Runnable runnable = new Runnable(){
		Timer timer;
		TimerTask timerTask;
		int count;
		@Override
		public void run() {
			count = 30;
			timer = new Timer();
			timerTask = new TimerTask(){
				@Override
				public void run() {
					String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				//	int i = Integer.parseInt(btn_vaildCode.getText().toString());
				//	btn_vaildCode.setText(date);
					Log.i("debug", "count:"+ count +" | date:"+ date +" | ThreadName:"+Thread.currentThread().getName());
					if(count >= 0){
						Message msg = new Message();
						Bundle data = new Bundle();
						data.putString("time", String.valueOf(count));
						msg.setData(data);
						handler.sendMessage(msg);
					}else{
						timer.cancel();
					}
					count--;
				}
			};
			timer.schedule(timerTask, 0, 1000);
		}
	};
	
	@Override
	public Object OnDoInBackground(int what, int arg1, int arg2, Object obj) {
		Toast.makeText(this, "register OnDoInBackground", 10).show();
		return null;
	}

	@Override
	public int OnPostExecute(int what, int arg1, int arg2, Object obj,
			Object ret) {
		Toast.makeText(this, "register OnPostExecute", 10).show();
		return 0;
	}
	
}