package com.jpush;

import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Notification;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import com.manniu.manniu.R;


public class PushSetActivity extends InstrumentedActivity {
    private static final String TAG = "JPush";
    
	Button mSetTag;
	Button mSetAlias;
	Button mStyleBasic;
	Button mStyleCustom;
	Button mSetPushTime;

	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		/*setContentView(R.layout.push_set_dialog);
		init();
		initListener();*/
	}
	
	/*private void init(){
	    mSetTag = (Button)findViewById(R.id.bt_tag);
	    mSetAlias = (Button)findViewById(R.id.bt_alias);
	    mStyleBasic = (Button)findViewById(R.id.setStyle1);
	    mStyleCustom = (Button)findViewById(R.id.setStyle2);
	    mSetPushTime = (Button)findViewById(R.id.bu_setTime);
	}
	
	private void initListener(){
		mSetTag.setOnClickListener(this);
		mSetAlias.setOnClickListener(this);
		mStyleBasic.setOnClickListener(this);
		mStyleCustom.setOnClickListener(this);
		mSetPushTime.setOnClickListener(this);
	}*/
	
	/*@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_tag:
			setTag();
			break;
		case R.id.bt_alias:
			setAlias();
			break;
		case R.id.setStyle1:
			setStyleBasic();
			break;
		case R.id.setStyle2:
			setStyleCustom();
			break;
		case R.id.bu_setTime:
			Intent intent = new Intent(PushSetActivity.this, SettingActivity.class);
			startActivity(intent);
			break;
		}
	}*/
	
	private void setTag(){
		/*EditText tagEdit = (EditText) findViewById(R.id.et_tag);
		String tag = tagEdit.getText().toString().trim();
		
        // 妫?煡 tag 鐨勬湁鏁堟?
		if (TextUtils.isEmpty(tag)) {
			Toast.makeText(PushSetActivity.this,R.string.error_tag_empty, Toast.LENGTH_SHORT).show();
			return;
		}*/
		
		// ","闅斿紑鐨勫涓?杞崲鎴?Set
		String[] sArray = new String().split(",");
		Set<String> tagSet = new LinkedHashSet<String>();
		for (String sTagItme : sArray) {
			/*if (!ExampleUtil.isValidTagAndAlias(sTagItme)) {
				Toast.makeText(PushSetActivity.this,R.string.error_tag_gs_empty, Toast.LENGTH_SHORT).show();
				return;
			}*/
			tagSet.add(sTagItme);
		}
		
		//璋冪敤JPush API璁剧疆Tag
		mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TAGS, tagSet));

	} 
	
	private void setAlias(){
		//EditText aliasEdit = (EditText) findViewById(R.id.et_alias);
		String alias = "";//aliasEdit.getText().toString().trim();
		/*if (TextUtils.isEmpty(alias)) {
			Toast.makeText(PushSetActivity.this,R.string.error_alias_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		if (!ExampleUtil.isValidTagAndAlias(alias)) {
			Toast.makeText(PushSetActivity.this,R.string.error_tag_gs_empty, Toast.LENGTH_SHORT).show();
			return;
		}*/
		
		//璋冪敤JPush API璁剧疆Alias
		mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, alias));
	}
	
	/**
	 *璁剧疆閫氱煡鎻愮ず鏂瑰紡 - 鍩虹灞炴?
	 */
	private void setStyleBasic(){
		BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(PushSetActivity.this);
		builder.statusBarDrawable = R.drawable.ic_launcher;
		builder.notificationFlags = Notification.FLAG_AUTO_CANCEL;  //璁剧疆涓虹偣鍑诲悗鑷姩娑堝け
		builder.notificationDefaults = Notification.DEFAULT_SOUND;  //璁剧疆涓洪搩澹帮紙 Notification.DEFAULT_SOUND锛夋垨鑰呴渿鍔紙 Notification.DEFAULT_VIBRATE锛? 
		JPushInterface.setPushNotificationBuilder(1, builder);
		Toast.makeText(PushSetActivity.this, "Basic Builder - 1", Toast.LENGTH_SHORT).show();
	}
	
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private final TagAliasCallback mAliasCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
            case 0:
                logs = "Set tag and alias success";
                Log.i(TAG, logs);
                break;
                
            case 6002:
                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                Log.i(TAG, logs);
                if (ExampleUtil.isConnected(getApplicationContext())) {
                	mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                } else {
                	Log.i(TAG, "No network");
                }
                break;
            
            default:
                logs = "Failed with errorCode = " + code;
                Log.e(TAG, logs);
            }
            
            ExampleUtil.showToast(logs, getApplicationContext());
        }
	    
	};
	
    private final TagAliasCallback mTagsCallback = new TagAliasCallback() {

        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs ;
            switch (code) {
            case 0:
                logs = "Set tag and alias success";
                Log.i(TAG, logs);
                break;
                
            case 6002:
                logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                Log.i(TAG, logs);
                if (ExampleUtil.isConnected(getApplicationContext())) {
                	mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_TAGS, tags), 1000 * 60);
                } else {
                	Log.i(TAG, "No network");
                }
                break;
            
            default:
                logs = "Failed with errorCode = " + code;
                Log.e(TAG, logs);
            }
            
            ExampleUtil.showToast(logs, getApplicationContext());
        }
        
    };
    
	private static final int MSG_SET_ALIAS = 1001;
	private static final int MSG_SET_TAGS = 1002;
	private static final int MSG_GET_REGISTRATIONID = 1003;
	
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case MSG_SET_ALIAS:
                Log.d(TAG, "Set alias in handler.");
                JPushInterface.setAliasAndTags(getApplicationContext(), (String) msg.obj, null, mAliasCallback);
                break;
                
            case MSG_SET_TAGS:
                Log.d(TAG, "Set tags in handler.");
                JPushInterface.setAliasAndTags(getApplicationContext(), null, (Set<String>) msg.obj, mTagsCallback);
                break;
                
            case MSG_GET_REGISTRATIONID:
                Log.d(TAG, "get RegistrationId in handler.");
                String registrationid = JPushInterface.getRegistrationID(getApplicationContext());
                Log.d(TAG, "RegistrationId:"+registrationid);
                break;
                
            default:
                Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };
	
}
