package com.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.manniu.manniu.R;

public class NewIdea extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_idea);
		
	}
	
}
