package com.ctrl;

import android.view.View;

//on/off 滑动事件接口
public interface  OnChangedListener {
	 abstract void OnChanged(boolean CheckState,View v);
}