package com.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.manniu.manniu.R;

/**
 * 设备报警配置
 * @author zongyl
 *
 */
public class NewDeviceSetAlarm extends Activity{

	private String TAG = "NewDeviceSetAlarm";
	
	Button submit, cancel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_device_set_alarm);

		cancel = (Button) findViewById(R.id.dev_set_alarm_cancel);
		submit = (Button) findViewById(R.id.dev_set_alarm_submit);

		cancel.setOnClickListener(click);
		submit.setOnClickListener(click);
		//findViewById(R.id.device_set_alarm_week).setOnClickListener(click);
	}
	
	@Override
	public void finish() {
		super.finish();
	}
	
	private OnClickListener click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.dev_set_alarm_cancel:
				finish();
				break;
			case R.id.dev_set_alarm_submit:
				
				break;
			/*case R.id.device_set_alarm_week:
				
				break;*/

			default:
				break;
			}
		}
	};
	
}
