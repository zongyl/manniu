package com.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.manniu.manniu.R;
import com.mining.app.zxing.encodeing.EncodingHandler;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2015-4-8 下午2:42:07
 * To change this template use File | Settings | File Templates.
 * Description：手机信息
 */

public class About_MobilephoneActivity extends Activity{
	
	TextView _phoneNum;//,_phonePwd,_phoneModel,_phoneName,;
	ImageView _phoneQRcode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		setContentView(R.layout.about_mobilephone);

//		_phoneName = (TextView) this.findViewById(R.id.phone_name);
		_phoneNum = (TextView) this.findViewById(R.id.phone_num);
//		_phonePwd = (TextView) this.findViewById(R.id.phone_pwd);
//		_phoneModel = (TextView) this.findViewById(R.id.phone_model);
		_phoneQRcode = (ImageView) this.findViewById(R.id.phone_QRcode);
		 //sn:11111;vn:2222222;
//		 SIMCardInfo siminfo = new SIMCardInfo(About_MobilephoneActivity.this);
//		 System.out.println(siminfo.getLocalMacAddress());
//		 System.out.println(siminfo.getDeviceUuid().toString());
		
//		_phonePwd.setText("ABCDEF");
//		_phoneName.setText(android.os.Build.MODEL + ","
//				+ android.os.Build.VERSION.SDK + ","
//				+ android.os.Build.VERSION.RELEASE);
//		_phoneModel.setText("WIFI");
		//String str = "http://www.ys7.com/|1001|ABCDEF|WIFI";
		String sn = getIntent().getExtras().getString("sn");
		_phoneNum.setText(sn);
		String vn = getIntent().getExtras().getString("vn");
		if(vn == null || vn.equals("")) vn = "ABCDEF";
		String str = "sn:"+sn+";vn:"+vn;
		try {
			_phoneQRcode.setImageBitmap(EncodingHandler.createQRCode(str, 350));
		} catch (WriterException e) {
			e.printStackTrace();
		}

		// WifiP2pDevice device =
		// this.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

	}

}