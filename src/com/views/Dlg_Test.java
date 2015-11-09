package com.views;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Dlg_Test extends Dialog implements OnClickListener, OnTaskListener,OnCheckedChangeListener  {

	public Dlg_Test(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	protected Dlg_Test(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public Dlg_Test(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object OnDoInBackground(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int OnPostExecute(int what, int arg1, int arg2, Object obj,
			Object ret) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}