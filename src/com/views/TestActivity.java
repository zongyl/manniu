package com.views;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import cn.jpush.android.api.JPushInterface;

import com.google.android.gcm.GCMRegistrar;
import com.manniu.manniu.R;
import com.utils.Constants;

public class TestActivity extends Activity {

	private static final String TAG = "TestActivity"; 
	
	private static String LINE = "\n\n";
	
	private String gcmRegId, jpushRegId;
	
	private TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_activity);
		
		gcmRegId = GCMRegistrar.getRegistrationId(this);
		jpushRegId = JPushInterface.getRegistrationID(this);
		
		tv = (TextView) findViewById(R.id.test_tv);
		
		tv.setText("GCMregId:" + gcmRegId + LINE + 
				"jpushRegId:" + jpushRegId + LINE + 
				"Server IP:" + Constants.hostUrl);
		
	}
}
