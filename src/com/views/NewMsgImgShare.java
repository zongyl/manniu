package com.views;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.manniu.manniu.R;

public class NewMsgImgShare extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.new_msgimg_share, null);
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth(cFullFillWidth);
		//this.setCanceledOnTouchOutside(true);
		Window w = this.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = Gravity.BOTTOM;
		this.onWindowAttributesChanged(lp);
		setContentView(layout);
		findViewById(R.id.msg_shareCancel).setOnClickListener(this);
	}
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.msg_shareCancel:
			finish();
			break;
		}
	}

}