package com.views;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.manniu.manniu.R;

public class NewMsgWatch extends Activity implements OnClickListener{

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_msg_watch);
		findViewById(R.id.msg_watch_back).setOnClickListener(this);
	}
	
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.msg_watch_back:
			finish();
			break;
		}
	}
}