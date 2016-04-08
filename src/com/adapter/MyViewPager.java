package com.adapter;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {

	private static String TAG = "MyViewPager";
	
	private boolean scrollble = false;
	
	public MyViewPager(Context context) {
		super(context);
	}
	
	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if(scrollble){
			return false;
		}
	//	Log.v(TAG, "onTouchEvent....................");
		return super.onTouchEvent(arg0);
	}
	
	public boolean isScrollble() {
		return scrollble;
	}

	public void setScrollble(boolean scrollble) {
		this.scrollble = scrollble;
	}
}
