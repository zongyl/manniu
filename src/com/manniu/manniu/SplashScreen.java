package com.manniu.manniu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Window;

import com.utils.Loger;
import com.utils.SIMCardInfo;
import com.views.NewLogin;

public class SplashScreen extends Activity {

//	private TasksProgressView mTasksView;
//	private int mTotalProgress;
//	private int mCurrentProgress;
//	Handler handler = new Handler();
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splashscreen);
		
		SIMCardInfo info = new SIMCardInfo(this);
		Loger.openPrint(info.getDeviceId(), getResources().getString(R.string.server_address));
		
//		mTotalProgress = 100;
//		mCurrentProgress = 0;
//		mTasksView = (TasksProgressView) findViewById(R.id.tasks_view);
		
		
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//getWindow().setFormat(PixelFormat.RGBA_8888);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		// Display the current version number
		/*PackageManager pm = getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo("org.wordpress.android", 0);
			TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
			versionNumber.setText("Version " + pi.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}*/
//		new Handler().postDelayed(new Runnable() {
//			public void run() {
//				/* Create an Intent that will start the Main WordPress Activity. */
//				Main _main = new Main();
//				Intent mainIntent = new Intent(SplashScreen.this, _main.getClass());
//				SplashScreen.this.startActivity(mainIntent);
//				SplashScreen.this.finish();
//			}
//		}, 1000);  
		
		
		/*handler.postDelayed(new Runnable() {  
            public void run() {  
            	Main _main = new Main();
				Intent mainIntent = new Intent(SplashScreen.this, _main.getClass());
				SplashScreen.this.startActivity(mainIntent);
				SplashScreen.this.finish();
				handler.removeCallbacks(this);
            }  
        }, 1000);*/

		new Handler().postDelayed(new Runnable() {
			public void run() {
				// Create an Intent that will start the Main WordPress Activity. 
//				Main _main = new Main();
//				Intent mainIntent = new Intent(SplashScreen.this, _main.getClass());
				Intent mainIntent = new Intent(SplashScreen.this, NewLogin.class);
				SplashScreen.this.startActivity(mainIntent);
				SplashScreen.this.finish();
			}
		}, 1000);
		
		//new Thread(new ProgressRunable()).start();
		String tmp = getFilePath();
		setFilePath(tmp);
	}
	
	public static String logPath = "";
	//SD卡是否存在
	private String getFilePath() {
		String tmpPath = "";
		//SDK卡
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
//				System.out.println("sdk:"+Environment.getExternalStorageDirectory());
//				System.out.println("内部存储："+Environment.getDataDirectory());
			tmpPath = Environment.getExternalStorageDirectory()+"";
		} else{
			tmpPath = Environment.getDataDirectory()+"";
//				System.out.println("  getRootDirectory:-"+Environment.getRootDirectory());
//				System.out.println("内部存储："+Environment.getDataDirectory());
		}
		return tmpPath;
	}
	@SuppressLint("SdCardPath")
	public void setFilePath(String path){
		if(path.equals("/data")){
			path = "/data/data/com.client.xgps"; //TDM的安装路径
		}
		logPath = path +"/tdm/logs/";
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
    protected void onDestroy(){
		super.onDestroy();
    }
	
	/*class ProgressRunable implements Runnable {
		@Override
		public void run() {
			while (mCurrentProgress < mTotalProgress) {
				mCurrentProgress += 5;
				//System.out.println(mCurrentProgress);
				mTasksView.setProgress(mCurrentProgress);
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}*/
	public void onBackPressed(){
		this.finish();
		super.onBackPressed();
	}
	
}
