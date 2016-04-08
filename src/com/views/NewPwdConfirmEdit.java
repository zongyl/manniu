package com.views;

import java.util.ArrayList;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.views.NewDetailEdit.InnerBroadcastReceiver;

public class NewPwdConfirmEdit extends Activity implements OnClickListener{
	final static String SAVEFILE = "Info_Login";
	String  _firstInputPwd ="";
	String _confirmPwd = "";
	String _oldPwd = "";
	String _UserId = "";
	ArrayList<String> pwd = new ArrayList<String>();
	InnerBroadcastReceiver _broadcast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_pwd_confirm_edit);
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		_UserId = preferences.getString("sid", "");
		//_oldPwd = preferences.getString("password", "");
		
		_firstInputPwd = this.getIntent().getStringExtra("newpassword");
        //监听文本输入状态
        EditText et = (EditText) findViewById(R.id.pwd_write);
        et.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				if(s.length() == 0){
						findViewById(R.id.del_edit).setVisibility(View.GONE);
					}else{
						findViewById(R.id.del_edit).setVisibility(View.VISIBLE);
					}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				
			}
        	
        });
        
        findViewById(R.id.cancel_edit).setOnClickListener(this);
        findViewById(R.id.confirm_edit).setOnClickListener(this);
        findViewById(R.id.del_edit).setOnClickListener(this);
       
        getPwd();
	}
	
	public void getPwd(){
		Bundle b = getIntent().getExtras();
		pwd = b.getStringArrayList("pwd");
		_oldPwd = pwd.get(0);
		_firstInputPwd = pwd.get(1);
	}
	
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.del_edit:
			((EditText)findViewById(R.id.pwd_write)).setText("");
			break;
		case R.id.confirm_edit:
			_confirmPwd = ((EditText)findViewById(R.id.pwd_write)).getText().toString();
			Log.v("secondInput",_confirmPwd);
			if("".equals(_confirmPwd)){
				APP.ShowToast(getString(R.string.pwd_confrimEmpty));
			}else if(!_confirmPwd.equals(_firstInputPwd)){
				APP.ShowToast(getString(R.string.pwd_confrimNoMatch));
			}else{
				RequestParams params = new RequestParams();
				params.put("userId", _UserId);
				params.put("password", _oldPwd);
				params.put("newPwd", _confirmPwd);
				params.put("sessionId", Constants.sessionId);
				HttpUtil.get(Constants.hostUrl+"/android/updatePwd", params, new JsonHttpResponseHandler() {
					public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
						if(statusCode == 200){
							try {
								String msg = json.getString("result");
								if(msg.equals("pwd_error")){
									APP.ShowToast(getString(R.string.pwd_modiFail));
								}else{
									//UpdateUserInfo();
									//forward(NewDetailEdit.class);
									finish();
									APP.ShowToast(getString(R.string.pwd_mofiSucc));
								}
							} catch (JSONException e) {
								
								e.printStackTrace();
							}
							
						}
					}
					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
						forward(NewDetailEdit.class);
						APP.ShowToast(getString(R.string.E_SER_FAIL));
					}
				});
			}
			break;
			
		case R.id.cancel_edit:
			finish();
			break;
		}
	}
	
	private void forward(Class<?> target) {
		Intent intent =new Intent(this,target);
		startActivity(intent);
		
	}
	private void UpdateUserInfo() {
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("password",_confirmPwd);
		editor.commit();
	}
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}

}
