package com.views;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class NewPwdDetailEdit extends Activity implements OnClickListener {
	private String TAG = getClass().getName();
	final static String SAVEFILE = "Info_Login";
	public static final String action = "pwd.broadcast.action";
	String _password ="";
	String _userId = "";
	EditText _editText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_pwd_detail_edit);
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		_password = preferences.getString("password", "");
		_userId = preferences.getString("sid","");
		Log.v(TAG, _userId);
		_editText = (EditText) findViewById(R.id.pwd_write);
        _editText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					findViewById(R.id.del_edit).setVisibility(View.GONE);
					// Log.v("隐藏删除键",
					// ""+findViewById(R.id.del_edit).getVisibility());
				} else {
					findViewById(R.id.del_edit).setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}
		});

		findViewById(R.id.cancel_edit).setOnClickListener(this);
		findViewById(R.id.next_edit).setOnClickListener(this);
		findViewById(R.id.del_edit).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.del_edit:
			((EditText) findViewById(R.id.pwd_write)).setText("");
			break;
		case R.id.next_edit:
			if("".equals(_editText.getText().toString())){
				APP.ShowToast(getString(R.string.pwd_oldEmpty));
				
			}else{
				RequestParams params = new RequestParams();
				params.put("userId", _userId);
				params.put("password", _editText.getText().toString());
				params.put("sessionId", Constants.sessionId);
				HttpUtil.get(Constants.hostUrl+"/android/vaildOldPwd", params, new JsonHttpResponseHandler() {
					public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
						if(statusCode == 200 ){
							Boolean result =null;
							try{
								result = json.getBoolean("result");
								if(result){
									forward(NewPwdWriteEdit.class,"oldPwd",_editText.getText().toString());
									finish();
								}else{
									APP.ShowToast(getString(R.string.pwd_error));
								}
							}catch(JSONException e){
								e.printStackTrace();
							}
						}
					}
					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
						APP.ShowToast(getString(R.string.pwd_veriFail));
					}
					
				});
			}
			
			break;
		case R.id.cancel_edit:
			finish();
			break;
		}
	}
	
	private void forward(Class<?> target,String name,String value) {
		Intent intent =new Intent(this,target);
		intent.putExtra(name, value);
		startActivity(intent);
		
	}
	
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}
}
