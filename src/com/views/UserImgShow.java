package com.views;

import java.io.File;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class UserImgShow extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_img_show);
		Bundle bundle = this.getIntent().getExtras();
		String[] trans = bundle.getStringArray("imgs");
		String url = getResources().getString(R.string.server_address)+File.separator+trans[0];
		ImageView img = (ImageView) findViewById(R.id.user_img);
		
		DisplayImageOptions options  = new DisplayImageOptions.Builder()
		//.showImageForEmptyUri(R.drawable.images_nophoto_bg)
		.showImageOnFail(R.drawable.event_list_fail_pic)
		//.showImageOnLoading(R.drawable.progress_msg_loading)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build(); 
		ImageLoader.getInstance().displayImage(url, img, options, null);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		finish();
		return true;
	}
	
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}
}
