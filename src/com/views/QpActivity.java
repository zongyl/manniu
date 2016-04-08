package com.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * 全屏  感应  横屏
 * @author pc
 *
 */
public class QpActivity extends Activity implements OnClickListener, OnDoubleTapListener{

	private int[] pixels;
	/**
	 * 以数组形式返回 尺寸 [0] = width, [1] = height
	 * @return
	 */
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Log.i("debug", "width:"+dm.widthPixels);
		Log.i("debug", "height:"+dm.heightPixels);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.qp_main);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new MyView(this));
		//findViewById(R.id.btn_qp_01).setOnClickListener(this);
		Log.i("debug", "onCreate!");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i("debug", "onConfigurationChanged");
		pixels = getSize();

		if(pixels[0] > pixels[1]){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			Toast.makeText(getApplicationContext(), "横屏", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(getApplicationContext(), "竖屏", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.i("debug:", "onDoubleTap");
		return false;
	}
	
	@Override
	public void onClick(View v) {/*
		if(R.id.btn_qp_01 == v.getId()){
			getSize();
		}
		
	*/}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	
	

	/**
	 * 
	 * @author pc
	 *
	 */
	class MyView extends SurfaceView implements SurfaceHolder.Callback{

		SurfaceHolder holder;
		Canvas _canvas;
		int _width, _height;
		
		public MyView(Context context) {
			super(context);
			this.setLongClickable(true);
			holder = this.getHolder();
			holder.addCallback(this);
		}

		public MyView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.i("debug", ".........surfaceCreated.............");
			new MyThread().run();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.i("debug", ".........surfaceChanged.............");
			Log.i("debug", ".........surfaceChanged.width:"+width);
			Log.i("debug", ".........surfaceChanged.height:"+height);
			_width = width;
			_height = height;
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.i("debug", ".........surfaceDestroyed.............");
		}
		
		@SuppressLint("SimpleDateFormat")
		class MyThread implements Runnable{
			Timer timer;
			TimerTask timerTask;
			@Override
			public void run() {
				timer = new Timer();
				timerTask = new TimerTask(){
					@Override
					public void run() {
						String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
						Log.i("debug", ""+date);
						_canvas = holder.lockCanvas(null);
						Paint paint = new Paint();
						paint.setColor(Color.YELLOW);
						if(_width < _height){
							_canvas.drawRect(new Rect(0, 0, _width, _height/2), paint);
						}else{
							_canvas.drawRect(new Rect(0, 0, _width, _height), paint);
						}
						holder.unlockCanvasAndPost(_canvas);
					}
					
				};
				timer.schedule(timerTask, 0, 1000);
				
			}
			
		}
		
	}
	
	
	
}
