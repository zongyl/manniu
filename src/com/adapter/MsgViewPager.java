package com.adapter;



import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


public class MsgViewPager extends ViewPager {
	
	

	private static String TAG = "MsgViewPager";
	
	
	
	private boolean scrollble = false;
	

	public MsgViewPager(Context context) {
		super(context);
		
	}


	public MsgViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		if(scrollble){
			return false;
		}
		Log.v(TAG, "onTouchEvent....................");
		return super.onTouchEvent(arg0);
	}
	
	public boolean isScrollble() {
		return scrollble;
	}

	public void setScrollble(boolean scrollble) {
		this.scrollble = scrollble;
	}

//	public void onDisplay(int position) {
//		_imgitem = _msgImgs.get(position);
//	}
}
