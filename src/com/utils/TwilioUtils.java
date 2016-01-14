package com.utils;

import org.apache.http.Header;

import com.adapter.HttpUtil;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.manniu.manniu.R;
import com.views.BaseApplication;

/**
 * Twilio 
 * @author pc
 *
 */
public class TwilioUtils {

	public static final String ACCOUNT_SID = "ACdae6456d60b257e7df50fbfbfe7fb19c";
	public static final String AUTH_TOKEN = "d6209c9f3e8d478e229fb3c4a2d7af26";
	
	public static final String TAG = "TwilioUtils";
	
	public static void sendSms(String toNubmer, String text){
		String url = "https://api.twilio.com/2010-04-01/Accounts/"+ACCOUNT_SID+"/Messages";
		RequestParams params = new RequestParams();
		params.put("To", "+1"+toNubmer);
		params.put("From", "16503979760");
		params.put("Body", BaseApplication.getInstance().getString(R.string.register_success_tip) + text + ".");
		HttpUtil.postussms(ACCOUNT_SID+":"+AUTH_TOKEN, url, params, new TextHttpResponseHandler(){
			@Override
			public void onFailure(int arg0, Header[] arg1, String arg2,
					Throwable arg3) {
				LogUtil.d(TAG, "failure" + arg2);
			}
			@Override
			public void onSuccess(int arg0, Header[] arg1, String arg2) {
				LogUtil.d(TAG, "success" + arg2);
			}
		});
	}
}
