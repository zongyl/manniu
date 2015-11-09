package com.views;

import org.apache.http.Header;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.adapter.HttpUtil;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;

/**
 * 设置密码
 * @author pc
 *
 */
public class NewRegSetPwdActivity extends Activity implements OnClickListener {

	private static final String TAG = "NewRegSetPwdActivity";
	
	Button submit;
	EditText pwd, repwd;
	String password;
	String userId;
	String userName = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_reg_setpwd_activity);
		submit = (Button)findViewById(R.id.sub);
		pwd = (EditText)findViewById(R.id.pwd);
		repwd = (EditText)findViewById(R.id.repwd);
		submit.setOnClickListener(this);
		getParams();
	}

	/**
	 * 验证密码与重复密码 
	 * @return
	 */
	private boolean valid(){
		String _pwd = pwd.getText().toString().trim();
		String _repwd = repwd.getText().toString().trim();
		if(isEmpty(_pwd)){
			alert(getString(R.string.pwd_empty));
			return false;
		}
		if(isEmpty(_repwd)){
			alert(getString(R.string.pwd_reempty));
			return false;
		}
		if(_pwd.equals(_repwd)){
			password = _pwd;
			return true;
		}else{
			alert(getString(R.string.pwd_confrimNoMatch));
			return false;
		}
	}
	
	private boolean isEmpty(String string){
		return "".equals(string) || string == null;
	}
	
	private void alert(String text){
		Toast.makeText(this, text, 0).show();
	}
	
	private void getParams(){
		userId = getIntent().getExtras().getString("userId");
		userName = getIntent().getExtras().getString("userName");
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sub:
			if(valid()){
				requestWeb();
			}
			break;

		default:
			break;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void forward(Class clazz){
		Intent intent = new Intent(this, clazz);
		startActivity(intent);
		NewRegSetPwdActivity.this.finish();
	}
	
	@SuppressWarnings("static-access")
	private void requestWeb(){
		RequestParams params = new RequestParams();
		params.put("userId", userId);
		params.put("password", password);
		Log.v(TAG, "requestWeb userId&password:"+password+" | "+userId);
		HttpUtil.get(Constants.hostUrl+"/android/setPwd", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				alert(getString(R.string.pwd_setSucc));
				SharedPreferences preferences = getSharedPreferences(NewLogin.SAVEFILE, NewRegSetPwdActivity.this.MODE_PRIVATE);
				Editor editor = preferences.edit();
				editor.putString("user0", userName);
				editor.putString("pwd0", password);
				editor.commit();
				forward(NewLogin.class);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				alert(getResources().getString(R.string.E_SER_FAIL));
			}
		});
	}
}