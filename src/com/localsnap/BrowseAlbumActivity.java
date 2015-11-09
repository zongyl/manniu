package com.localsnap;

import com.manniu.manniu.R;
import com.views.bovine.Fun_AnalogVideo;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-9-1 下午3:54:27
 * To change this template use File | Settings | File Templates.
 * Description：浏览图片--双击一个图片 进入到图片浏览模式
 */
public class BrowseAlbumActivity extends Activity{
	//public static BrowseAlbumActivity instance = null;	
	public static String _path = "";
	//Context context = null;
	//屏幕的宽度
	public static int screenWidth;
	//屏幕的高度
	public static int screenHeight;
	private BroGallery gallery;
	BrowseAlbumAdapter _gadAdapter;//图片浏览适配器
		
	
//	private int window_width, window_height;// 控件宽度
//	private DragImageView dragImageView;// 自定义控件
//	private int state_height;// 状态栏的高度
//	private ViewTreeObserver viewTreeObserver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		setContentView(R.layout.bro_imgview);
		//instance = this;
		//context = BrowseAlbumActivity.this;
		 //获取屏幕的大小
        screenWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
		gallery = (BroGallery) findViewById(R.id.bro_gallery);
        gallery.setVerticalFadingEdgeEnabled(false);	
        gallery.setHorizontalFadingEdgeEnabled(false);//);// 设置view在水平滚动时，水平边不淡出。
        _gadAdapter = new BrowseAlbumAdapter(this);
        
        _gadAdapter.UpdateList(Fun_AnalogVideo.ImagePath+_path);
        gallery.setAdapter(_gadAdapter);
        
        int position = getIntent().getIntExtra("position", 0);
        //设置显示选中的图片的ID号
        gallery.setSelection(position);
		
		
		/** 获取可見区域高度 **/
		/*WindowManager manager = getWindowManager();
		window_width = manager.getDefaultDisplay().getWidth();
		window_height = manager.getDefaultDisplay().getHeight();
		dragImageView = (DragImageView) findViewById(R.id.iv01);
		Bitmap bmp = BitmapUtil.readLoacalBitmap(this, _path,window_width, window_height);
		// 设置图片
		dragImageView.setImageBitmap(bmp);
		dragImageView.setmActivity(this);//注入Activity.
		*//** 测量状态栏高度 **//*
		viewTreeObserver = dragImageView.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (state_height == 0) {
							// 获取状况栏高度
							Rect frame = new Rect();
							getWindow().getDecorView()
									.getWindowVisibleDisplayFrame(frame);
							state_height = frame.top;
							dragImageView.setScreen_H(window_height-state_height);
							dragImageView.setScreen_W(window_width);
						}

					}
				});*/
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		BrowseAlbumActivity.this.finish();
		//instance = null;
	}

}
