package com.views;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.LogUtil;

public class NewItemDetailEdit extends Activity implements OnClickListener{
	final String TAG =getClass().getName();
	String _UserId = "";
	String _updateItem ="";
	String updateItem="";
	String [] _tempArr = new String[3];
	EditText _ex;
	TextView _desc;
	int inputNum =15;//签名字数限制
	final static String SAVEFILE = "Info_Login";
	public static final String action = "detail.broadcast.action";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_item_detail_edit);
		Bundle bundle = this.getIntent().getExtras();
		_tempArr = bundle.getStringArray("title");
		String title = _tempArr[0];
		String value = _tempArr[1];		
		_UserId = _tempArr[2];
		TextView tx =(TextView)findViewById(R.id.hotN_title);
		_ex =(EditText)findViewById(R.id.hotN_write);
		_desc =(TextView)findViewById(R.id.detailItem_desc);
		tx.setText(title);
		_ex.setText(value);
		loadPersonMsg(title);
		setListeners();
	}
	public void setListeners(){
		findViewById(R.id.cancel_edit).setOnClickListener(this);
		findViewById(R.id.confirm_edit).setOnClickListener(this);
		findViewById(R.id.del_edit).setOnClickListener(this);
	}
	public void loadPersonMsg(String title){
		if(title.equals(getString(R.string.e_mail))){
			_ex.setHint(getString(R.string.mail_hint));
			_desc.setText(getString(R.string.mail_desc));
			_updateItem = "mail";
			updateItem= "email";
		}
		if(title.equals(getString(R.string.hot_name))){
			_ex.setHint(getString(R.string.hot_hint));
			_desc.setText(getString(R.string.hot_desc));
			_updateItem = "username";
			updateItem= "username";
		}
		if(title.equals(getString(R.string.pho_number))){
			_ex.setHint(getString(R.string.pho_hint));
			_ex.setInputType(InputType.TYPE_CLASS_PHONE);
			_updateItem = "mobile";
			updateItem= "phonenumber";
		}
	/*	if(title.equals("个性签名")){
			_ex.setHint("请编辑15个字以内的签名");
			_updateItem = "signer";
			updateItem= "signer";
			_ex.addTextChangedListener(new MyTextWatcher());
		}*/
		//设置光标位置
		CharSequence text = _ex.getText();
		if(text instanceof Spannable){
			 Spannable spanText = (Spannable)text;
			 Selection.setSelection(spanText, text.length());
		}
	}
	
	@Override
	public void onClick(View view) {
		if(R.id.cancel_edit == view.getId()){
			this.finish();
		}
		if(R.id.del_edit == view.getId()){
			EditText et= (EditText) findViewById(R.id.hotN_write);
			et.setText(null);
		}
		if(R.id.confirm_edit == view.getId()){
			if(_ex.getText().toString().trim().length() < 2){
				APP.ShowToast(getString(R.string.no_empty)+","+getString(R.string.hot_hint));
			}else{
				if( "mail".equals(_updateItem)){
					if(!isEmail(_ex.getText().toString())){
						APP.ShowToast(getString(R.string.mail_tip));
					}else{
						updateMsg();
					}
				}else{
					updateMsg();
				}
			}
		}
	}
	public void updateMsg(){
		final String err_con = getResources().getString(R.string.Err_CONNET);
		RequestParams params = new RequestParams();
		params.put("userId", _UserId);
		params.put(_updateItem, _ex.getText().toString());
		params.put("sessionId", Constants.sessionId);
		HttpUtil.get(getResources().getString(R.string.server_address)+"/android/updateUser", params, new JsonHttpResponseHandler() {
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					try {
						if(json.getString("result").equals("nologin")){
							LogUtil.v(TAG, "更新用户信息超时");
							//BaseApplication.getInstance().relogin();
						}else{
							UpdateUserInfo();
							notifyChanged();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(err_con);
			}
		});
	}
	
	private void notifyChanged() {
		Intent intent = new Intent(action);
		intent.putExtra("data","dataChanged");
		sendOrderedBroadcast(intent, null);
		this.finish();
	} 
	
	private void UpdateUserInfo() {
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(updateItem, _ex.getText().toString());
		editor.commit();
	}
	
	public void onBackPressed(){
		this.finish();
	}
	
	
	public boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}
	
	class MyTextWatcher implements TextWatcher{

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
