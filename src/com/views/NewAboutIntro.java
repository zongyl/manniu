package com.views;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.manniu.manniu.R;
import com.ctrl.ViewPagerAdapter;

public class NewAboutIntro extends Activity implements OnClickListener{
	
	// 定义ViewPager适配器
		private ViewPagerAdapter _vpAdapter;
		// 定义ViewPager对象
		private ViewPager _viewPager;
		//定义各个界面View对象
		private View view1,view2,view3;
		// 定义底部小点图片
		private ImageView pointImage0, pointImage1, pointImage2,cancelScan;
		// 定义一个ArrayList来存放View
		private ArrayList<View> listViews;
		//定义开始按钮对象
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.guide_main1);
		initView();
		initData();
	}
	
	private void initView(){
		//实例化各个界面的布局对象 
		LayoutInflater inflater = LayoutInflater.from(this);
		view1 = inflater.inflate(R.layout.guide_view1, null);
		view2 = inflater.inflate(R.layout.guide_view2, null);
		view3 = inflater.inflate(R.layout.guide_view3, null);
		// 实例化ViewPager
		_viewPager = (ViewPager) findViewById(R.id.viewpager);
		listViews = new ArrayList<View>();
		// 实例化ViewPager适配器
		_vpAdapter = new ViewPagerAdapter(listViews);
		// 实例化底部小点图片对象
		pointImage0 = (ImageView) findViewById(R.id.page0);
		pointImage1 = (ImageView) findViewById(R.id.page1);
		pointImage2 = (ImageView) findViewById(R.id.page2);
		cancelScan = (ImageView) findViewById(R.id.cancel_scan);
		cancelScan.setOnClickListener(this);
	}
	
	private void initData(){
		// 设置监听
		_viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		// 设置适配器数据
		_viewPager.setAdapter(_vpAdapter);

		//将要分页显示的View装入数组中		
		listViews.add(view1);
		listViews.add(view2);
		listViews.add(view3);			
		_vpAdapter.notifyDataSetChanged();
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case 0:
				pointImage0.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_focused));
				pointImage1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_unfocused));
				break;
			case 1:
				pointImage1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_focused));
				pointImage0.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_unfocused));
				pointImage2.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_unfocused));
				break;
			case 2:
				pointImage2.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_focused));
				pointImage1.setImageDrawable(getResources().getDrawable(R.drawable.page_indicator_unfocused));
				break;
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.cancel_scan:
			finish();
			break;
		}
	}
}
