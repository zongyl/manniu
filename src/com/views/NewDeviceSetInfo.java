package com.views;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;

public class NewDeviceSetInfo extends Activity implements OnClickListener{

	Button cancel, submit;
	
	EditText new_dev_name;
	
	String dev_name, sid; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_device_set_info);

		Bundle extras = getIntent().getExtras();

		dev_name = extras.getString("devicesname");
		sid = extras.getString("sid");
		
		cancel = (Button) findViewById(R.id.dev_set_cancel);
		submit = (Button) findViewById(R.id.dev_set_submit);
		new_dev_name = (EditText) findViewById(R.id.new_dev_name);

		new_dev_name.setText(dev_name);
		
		cancel.setOnClickListener(this);
		submit.setOnClickListener(this);
		
		new_dev_name.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Drawable drawable = new_dev_name.getCompoundDrawables()[2];
						if(drawable != null){
							drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
							if(event.getX() > new_dev_name.getWidth()-new_dev_name.getPaddingRight()-drawable.getIntrinsicWidth()){
								new_dev_name.setText("");
							}
						}
				return false;
			}
		});
	}

	@Override
	public void finish() {
		super.finish();
	}
	
	private String getHttpUrl(){
		return this.getResources().getString(R.string.server_address);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dev_set_cancel:
			this.finish();
			break;
		case R.id.dev_set_submit:
			RequestParams params = new RequestParams();
			dev_name = new_dev_name.getText().toString();
			params.put("sid", sid);
			params.put("devicesname", dev_name);
			HttpUtil.get(getHttpUrl()+"/android/updateDevName", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					Log.d("update Name：", "statusCode："+statusCode);
					if(statusCode == 200){
						Log.d("update Name：", response.toString());
						Intent data = new Intent();
						data.putExtra("deviceName", dev_name);
						setResult(1, data);
						finish();
						APP.ShowToast(getString(R.string.SUCCESS_MODIFY));
						
					}
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers,
						Throwable throwable, JSONObject errorResponse) {
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					Log.d("", "" + responseString);
				}
			});
			break;

		default:
			break;
		}
	}
	
}