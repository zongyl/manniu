package com.views;


import java.util.ArrayList;

import com.manniu.manniu.R;
import com.ctrl.ViewPagerAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-9-15 下午3:41:32
 * To change this template use File | Settings | File Templates.
 * Description： 引导页面 
 */
@SuppressWarnings("static-access")
public class GuideViewActivity extends Activity implements OnTouchListener{
	
	private SharedPreferences _preferences;
	private Editor _editor; 
	
	// 定义ViewPager适配器
	private ViewPagerAdapter _vpAdapter;
	// 定义ViewPager对象
	private ViewPager _viewPager;
	//定义各个界面View对象
	private View view1,view2,view3;
	// 定义底部小点图片
	private ImageView pointImage0, pointImage1, pointImage2;
	// 定义一个ArrayList来存放View
	private ArrayList<View> listViews;
	//定义开始按钮对象
	private Button _btnStart;
//	private ImageView _imgStart;
	
	// 当前的位置索引值
	@SuppressWarnings("unused")
	private int currIndex = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.guide_main);
		_preferences = getSharedPreferences("firstLogin", this.MODE_PRIVATE);
		//触发移动事件的最短距离，如果小于这个距离就不触发移动控件 一般用于ViewPager控制是否翻页
		//touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
//		initView();
//		initData();
		//防止按home键之后退回到桌面，再次点击程序重新启动
		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {  
            finish();
            return;
		}
		// 判断是不是首次登录，
		if (_preferences.getBoolean("firstLogin", true)) {
			_editor = _preferences.edit();
			// 将登录标志位设置为false，下次登录时不在显示首次登录界面
			_editor.putBoolean("firstLogin", false);
			_editor.commit();
			initView();
			initData();
		}else{
			startExperience();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	private void initView(){
		//实例化各个界面的布局对象 
		LayoutInflater inflater = LayoutInflater.from(this);
		view1 = inflater.inflate(R.layout.guide_view1, null);
		view2 = inflater.inflate(R.layout.guide_view2, null);
		view3 = inflater.inflate(R.layout.guide_view3, null);
		view3.setOnTouchListener(this); 
		// 实例化ViewPager
		_viewPager = (ViewPager) findViewById(R.id.viewpager);
		listViews = new ArrayList<View>();
		// 实例化ViewPager适配器
		_vpAdapter = new ViewPagerAdapter(listViews);
		// 实例化底部小点图片对象
		pointImage0 = (ImageView) findViewById(R.id.page0);
		pointImage1 = (ImageView) findViewById(R.id.page1);
		pointImage2 = (ImageView) findViewById(R.id.page2);
		
		//实例化开始按钮
		_btnStart = (Button) view3.findViewById(R.id.btn_start);
		//_imgStart = (ImageView) view3.findViewById(R.id.img_start);
		
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
		// 给开始按钮设置监听
		_btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startExperience();
			}
		});
//		_imgStart.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startExperience();
//			}
//		});
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
			currIndex = position;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			//System.out.println("aa");
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			//System.out.println("bb");
		}
	}
	
	private void startExperience() {  
      	Intent intent = new Intent();
		intent.setClass(this,SplashScreen.class);
		startActivity(intent);
		GuideViewActivity.this.finish();
	}
	
	/**
	 * 手指按下时的x y坐标
	 */
	private int xDown = 0;
	private int yDown = 0;
	/**
	 * 手指移动时的x y 坐标
	 */
	private int xMove = 0;
	private int yMove = 0;
	//触发移动事件的最短距离
	private int touchSlop = 5;
	/**
	 * 相应按钮点击事件
	 */
	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = (int) ev.getX();
			yDown = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = (int) ev.getX();
			yMove = (int) ev.getY();
			int dx = xMove - xDown;
			int dy = yMove - yDown;
			//判断是否是从右到左的滑动
			//System.out.println(Math.abs(dx)+" -- "+Math.abs(dy));
			if (xMove < xDown && Math.abs(dx) > touchSlop && Math.abs(dy) < touchSlop) {
				startExperience();
				break;
			}
			break;
		}
		return true;
	}
	

}
