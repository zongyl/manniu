package com.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.manniu.manniu.R;
import com.utils.Loger;
import com.utils.SIMCardInfo;
import com.utils.httpClientUtils;

public class SplashScreen extends Activity {

	private SharedPreferences _preferences;
	private Editor _editor; 
	public final static String HOSTIP = "HOSTIP";
	
	@SuppressWarnings("static-access")
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.advert);
		
		try {
			SIMCardInfo info = new SIMCardInfo(this);
			
			if(info.getDeviceId() == null)
			{
				Loger.openPrint(info.getDeviceUuid().toString(), getResources().getString(R.string.server_address));
			}
			else
			{
				Loger.openPrint(info.getDeviceId(), getResources().getString(R.string.server_address));
			}
		} catch (Exception e) {
		}
		
		
		_preferences = getSharedPreferences(HOSTIP, this.MODE_PRIVATE);
		if (_preferences.getString("hostIP", "").equals("")) {
			_editor = _preferences.edit();
			new Thread(){
				@Override
				public void run(){
					//Constants.hostUrl = httpClientUtils.getServerHostAddress(getResources().getString(R.string.server_address));
					_editor.putString("hostIP", httpClientUtils.getServerHostAddress(getResources().getString(R.string.server_address)));
					_editor.commit();
				}
			}.start();
		}
		

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
		
	}
	
	/** * 获取网络状态，wifi,wap,2g,3g. * * @param context 上下文 * @return int 网络状态 {@link #NETWORKTYPE_2G},{@link #NETWORKTYPE_3G}, *{@link #NETWORKTYPE_INVALID},{@link #NETWORKTYPE_WAP}*
	{@link #NETWORKTYPE_WIFI} */ 
	/*static int mNetWorkType = 0;
	public static int getNetWorkType(Context context) { 
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) { 
			String type = networkInfo.getTypeName(); 
			if (type.equalsIgnoreCase("WIFI")) { 
				mNetWorkType = NETWORKTYPE_WIFI; 
				} else if (type.equalsIgnoreCase("MOBILE")) { 
					String proxyHost = android.net.Proxy.getDefaultHost(); 
					mNetWorkType = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORKTYPE_3G : NETWORKTYPE_2G) : NETWORKTYPE_WAP; 
					} } else { 
						mNetWorkType = NETWORKTYPE_INVALID; 
						} 
		return mNetWorkType; 
						}*/
	
	
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
    protected void onDestroy(){
		super.onDestroy();
    }
	
	public void onBackPressed(){
		this.finish();
		super.onBackPressed();
	}
	
}