package com.views;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.LogUtil;

public class NewDetailHelp extends Activity implements OnClickListener{
	
	private static final String TAG = "NewDetailHelp";
	
	private EditText feed;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_online_feedback);
		setListeners();
		
	}
	
	public void setListeners(){
		findViewById(R.id.cancel_edit).setOnClickListener(this);
		findViewById(R.id.confirm_edit).setOnClickListener(this);
		findViewById(R.id.cancel_fee).setOnClickListener(this);
		feed = (EditText) findViewById(R.id.feed_content);
		feed.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					findViewById(R.id.cancel_fee).setVisibility(View.GONE);
					// Log.v("隐藏删除键",
					// ""+findViewById(R.id.del_edit).getVisibility());
				} else {
					findViewById(R.id.cancel_fee).setVisibility(View.VISIBLE);
				}

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
			
		});
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.cancel_edit:
			finish();
			break;
		case R.id.cancel_fee:
			feed.setText(null);
			break;
		case R.id.confirm_edit:
			submit();
			finish();
			break;
		}
	}
	
	/**
	 * 意见提交
	 */
	public void submit(){
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
		params.put("content", feed.getText().toString());
		
		LogUtil.d(TAG, "params:" + params.toString());
		LogUtil.d(TAG, "server address:" + Constants.hostUrl + "/android/saveSuggest");
		
		HttpUtil.post(Constants.hostUrl + "/android/saveSuggest", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				APP.ShowToast(getString(R.string.submit_success));
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				LogUtil.d("", "response:" + responseString);
				APP.ShowToast(getString(R.string.E_SER_FAIL));
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				LogUtil.d("", "response:" + errorResponse.toString());
				APP.ShowToast(getString(R.string.E_SER_FAIL));
			}
		});
	}
	
	public void onBackPressed(){
		super.onBackPressed();
		finish();
	}
	
}