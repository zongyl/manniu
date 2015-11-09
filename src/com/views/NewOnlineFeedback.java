package com.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.manniu.manniu.R;


public class NewOnlineFeedback extends Activity implements OnClickListener{
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_online_feedback);
		findViewById(R.id.cancel_edit).setOnClickListener(this);
	}

	
	
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}


	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.cancel_edit:
			finish();
			break;
		}
		
	}
}