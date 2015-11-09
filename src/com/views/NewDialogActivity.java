package com.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.manniu.manniu.R;

public class NewDialogActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_main);
		
		BaseApplication.getInstance().addActivity(this);
		
		new Handler().postDelayed(new Runnable(){  
	            @Override  
	            public void run(){  
	            	NewDialogActivity.this.finish();
	                Toast.makeText(getApplicationContext(), getString(R.string.SUCCESS_LOGIN), Toast.LENGTH_SHORT).show();  
	                load();
	            }  
	        }, 1000);
		
	}
	
	private void load(){
		Main main = new Main();
		Intent intent = new Intent(this, main.getClass());
		startActivity(intent);
	}
}