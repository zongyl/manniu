package com.views;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.localmedia.XListViewRewrite;
import com.manniu.manniu.R;
import com.utils.Constants;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-23 上午9:08:30
 * To change this template use File | Settings | File Templates.
 * Description： 本地存储初始化页面 （不能滑动的 tab）
 */
@SuppressWarnings("deprecation")
public class Fun_InitMedia extends TabActivity {
	public static Fun_InitMedia instance = null;
	Context context = null;
	public static int currentTab = 0;
	TabHost tabHost;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		setContentView(R.layout.init_medias);
        tabHost = getTabHost();  // The activity TabHost  
        context = Fun_InitMedia.this;
        instance = this;
        findViewById(R.id.btn_local).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				currentTab = 0;
				/*if(Main.Instance._curIndex == Main.XV_RECORDPLAY){
					APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_FUN_LIST);
				}*/
				Fun_InitMedia.this.finish();
//				Tab_VideoActivity.instance.finish();
//				Tab_SnapActivity.instance.finish();
			}
		});
        
        Intent i1 = new Intent(Intent.ACTION_VIEW);
        i1.setClassName(context, "com.views.Tab_VideoActivity");  
        Intent i2 = new Intent(Intent.ACTION_VIEW);//新建一个Intent用作Tab1显示的内容  
        i2.setClassName(context, "com.views.Tab_SnapActivity"); 
        
        RelativeLayout tabIndicator1 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tabwidget, null);  
		TextView tvTab1 = (TextView)tabIndicator1.findViewById(R.id.tv_title);
		tvTab1.setText("录像");
		tvTab1.setTextColor(Constants.COLOR_BACKGROUD);
		RelativeLayout tabIndicator2 = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.tabwidget,null);  
		TextView tvTab2 = (TextView)tabIndicator2.findViewById(R.id.tv_title);
		tvTab2.setText("截图");
		tvTab2.setTextColor(Constants.COLOR_BACKGROUD);
		
		tabHost.addTab(tabHost.newTabSpec("A").setIndicator(tabIndicator1).setContent(i1));
		tabHost.addTab(tabHost.newTabSpec("B").setIndicator(tabIndicator2).setContent(i2));
        tabHost.setCurrentTab(currentTab);//设置当前的选项卡,这里为Tab1 
        
        tabHost.getTabWidget().getChildAt(1).setOnClickListener(new OnClickListener() { 
	        @Override 
	        public void onClick(View v) {
	        	tabHost.setCurrentTab(1);
	        	XListViewRewrite.instance.dismissPopWindow();
	        } 
	    });
        
       // Main.Instance._curIndex = Main.XV_MEDIA;
	}
	
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//	@Override
//	protected void onStop() {
//		super.onStop();
//	}
//	@Override
//    protected void onDestroy(){
//		tabHost = null;
//        super.onDestroy();
//        System.gc();
//    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
