package com.views;

import com.localmedia.XListViewRewrite;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;

@SuppressLint("Instantiatable")
public class NewSquare extends XViewBasic {
	
	private static final String TAG ="NewSquare";
	
	public static  NewSquare instance = null;
	
	public NewSquare(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		instance = this;
		getFragmentView(0);
	}
	
	@Override
	protected void OnVisibility(int visibility) {
		super.OnVisibility(visibility);
		if (visibility == View.VISIBLE) {
			XListViewRewrite.dismissPopWindow();//切换时关闭本地的删除按钮
		}
	}
	
	@SuppressLint("NewApi")
	public  void getFragmentView(int type){
		try {
			if(type == 0){
				android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	Squ_LiveActivity fragment = new Squ_LiveActivity(); 
	        	frTransaction.replace(R.id.web_container, fragment);// 使用当前Fragment的布局替代id_content的控件
	        	frTransaction.commitAllowingStateLoss();
			}else if(type == 1){
				android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	Squ_ShortActivity fragment = new Squ_ShortActivity(); 
	        	frTransaction.replace(R.id.web_container, fragment);// 使用当前Fragment的布局替代id_content的控件
	        	frTransaction.commitAllowingStateLoss();
			}else{
				android.app.FragmentManager fragmentManager = ACT.getFragmentManager();
	        	android.app.FragmentTransaction frTransaction = fragmentManager.beginTransaction();
	        	Squ_NearActivity fragment = new Squ_NearActivity(); 
	        	frTransaction.replace(R.id.web_container, fragment);// 使用当前Fragment的布局替代id_content的控件
	        	frTransaction.commitAllowingStateLoss();
			}
		} catch (Exception e) {
			LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}
}
