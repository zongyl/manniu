package com.views;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cn.jpush.android.api.JPushInterface;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.MD5Util;
import com.utils.SetSharePrefer;

public class NewDetailSet extends Activity implements OnClickListener {
	private ImageView /*switch1,switch2,*/switch3,switch4,switch5;
	//private LinearLayout _flow;
	private String _tag;
	//private TextView _flowtipSize;
	private View _line;
	FlowBroadcastReceiver _broadcast;
	private final String SetInfo ="Info_Set";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_detail_set);
		iniViewDatas();
		setListeners();
		RegistBroadcast();
	}
	
	private  void iniViewDatas(){
	//	switch1 = (ImageView) findViewById(R.id.flow_switch);
	//	switch2 = (ImageView) findViewById(R.id.devi_switch);
		switch3 = (ImageView) findViewById(R.id.push_switch);
		switch4 = (ImageView) findViewById(R.id.zddl_switch);
		switch5 = (ImageView) findViewById(R.id.bcmm_switch);
		
		//switch4 = (ImageView) findViewById(R.id.check_switch);
		//_flow = (LinearLayout) findViewById(R.id.flow_limit);
		//_flowtipSize =(TextView) findViewById(R.id.flow_tipSize);
		_line = (View) findViewById(R.id.set_line);
		//_flowtipSize.setText(getIntent().getStringExtra("tipsize")==null? getString(R.string.non_set):getIntent().getStringExtra("tipsize"));
		readSetInfos();
	}
	
	public void readSetInfos(){
		SharedPreferences pre = APP.GetMainActivity().getSharedPreferences(SetInfo, APP.GetMainActivity().MODE_PRIVATE);
		if(pre!=null){
			if(!"".equals(pre.getString("push", ""))){
				switch3.setTag(pre.getString("push", ""));
			}
			if(!"".equals(pre.getString("zddl", ""))){
				switch4.setTag(pre.getString("zddl", ""));
			}
			if(!"".equals(pre.getString("bcmm", ""))){
				switch5.setTag(pre.getString("bcmm", ""));
			}
		}
		//switch4.setTag(APP.GetMainActivity().getSharedPreferences("Info_Login", APP.GetMainActivity().MODE_PRIVATE).getString("auto_check", "on"));
		//setSwitch(switch1);
		//setSwitch(switch2);
		setSwitch(switch3);
		setSwitch(switch4);
		setSwitch(switch5);
		//setSwitch(switch4);
	}
	
	public void setSwitch(View v){
		String value = v.getTag().toString();
		if("on".equals(value)||value==null||"".equals(value)){
			v.setBackgroundResource(R.drawable.my_switch_on);
		}else{
			v.setBackgroundResource(R.drawable.my_switch_off);
		}
	}
	
	private void setListeners(){
		//switch1.setOnClickListener(this);// 通过监听设置tag属性值,然后根据tag值加载开关切换
		//switch2.setOnClickListener(this);
		switch3.setOnClickListener(this);
		switch4.setOnClickListener(this);
		switch5.setOnClickListener(this);
		//switch4.setOnClickListener(this);
		// 绑定按钮监听
		findViewById(R.id.set_back).setOnClickListener(this);
	//	findViewById(R.id.flow_limit).setOnClickListener(this);
		//findViewById(R.id.flow_count).setOnClickListener(this);
		//findViewById(R.id.rece_login).setOnClickListener(this);
	}
	
	private void RegistBroadcast() {
		_broadcast = new FlowBroadcastReceiver();
		IntentFilter filter = new IntentFilter(NewFlowTipSet.action); 
        registerReceiver(_broadcast, filter);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.set_back:
			APP.GetMainActivity().ShowXView(Main.XV_NEW_MORE);
			finish();
			break;
		/*case R.id.flow_switch:
			_tag=switch1.getTag().toString();
			switch1.setTag("on".equals(_tag)?"off":"on");
			if ("on".equals(switch1.getTag().toString())) {
				switch1.setImageResource(R.drawable.my_switch_on);
				_flow.setVisibility(View.VISIBLE);
				_line.setVisibility(View.VISIBLE);
				SetSharePrefer.write(SetInfo, "traffictip", switch1.getTag().toString());
				break;
			} else {
				switch1.setImageResource(R.drawable.my_switch_off);
				_line.setVisibility(View.GONE);
				_flow.setVisibility(View.GONE);
				SetSharePrefer.write(SetInfo, "traffictip", switch1.getTag().toString());
				break;
			}
		case R.id.flow_limit:
			forward("tipSize",(String) _flowtipSize.getText(), NewFlowTipSet.class);
			break;*/
		/*case R.id.devi_switch:
			_tag=switch2.getTag().toString();
			switch2.setTag("on".equals(_tag)?"off":"on");
			if ("on".equals(switch2.getTag().toString())) {
				switch2.setImageResource(R.drawable.my_switch_on);
				SetSharePrefer.write(SetInfo, "recmsgauto", switch2.getTag().toString());
				break;
			} else {
				switch2.setImageResource(R.drawable.my_switch_off);
				SetSharePrefer.write(SetInfo, "recmsgauto", switch2.getTag().toString());
				break;
			}*/
		case R.id.push_switch:
			_tag=switch3.getTag().toString();
			switch3.setTag("on".equals(_tag)?"off":"on");
			if ("on".equals(switch3.getTag().toString())) {
				switch3.setImageResource(R.drawable.my_switch_on);
				setAlias(true);
				SetSharePrefer.write(SetInfo, "push", switch3.getTag().toString());
				break;
			} else {
				switch3.setImageResource(R.drawable.my_switch_off);
				setAlias(false);
				SetSharePrefer.write(SetInfo, "push", switch3.getTag().toString());
				break;
			}
			case R.id.zddl_switch:
				_tag=switch4.getTag().toString();
				switch4.setTag("on".equals(_tag)?"off":"on");
				if ("on".equals(switch4.getTag().toString())) {
					switch4.setImageResource(R.drawable.my_switch_on);
					SetSharePrefer.write(SetInfo, "zddl", switch4.getTag().toString());
					break;
				} else {
					switch4.setImageResource(R.drawable.my_switch_off);
					SetSharePrefer.write(SetInfo, "zddl", switch4.getTag().toString());
					break;
				}
			case R.id.bcmm_switch:
				_tag=switch5.getTag().toString();
				switch5.setTag("on".equals(_tag)?"off":"on");
				if ("on".equals(switch5.getTag().toString())) {
					switch5.setImageResource(R.drawable.my_switch_on);
					SetSharePrefer.write(SetInfo, "bcmm", switch5.getTag().toString());
					break;
				} else {
					switch5.setImageResource(R.drawable.my_switch_off);
					SetSharePrefer.write(SetInfo, "bcmm", switch5.getTag().toString());
					break;
				}
		/*case R.id.check_switch:
			_tag=switch4.getTag().toString();
			switch4.setTag("on".equals(_tag)?"off":"on");
			if ("on".equals(switch4.getTag().toString())) {
				switch4.setImageResource(R.drawable.my_switch_on);
				SetSharePrefer.write("Info_Login", "auto_check", switch4.getTag().toString());
				break;
			} else {
				switch4.setImageResource(R.drawable.my_switch_off);
				SetSharePrefer.write("Info_Login", "auto_check", switch4.getTag().toString());
				break;
			}*/
		/*case R.id.flow_count:
			forward("","",NewFlowCount.class);
			break;
		case R.id.rece_login:
			getLoginInfo();
			break;*/
		default :
			break;
		}
	}
	
	/**
	 * 设置是否接收推送消息 
	 * @param on
	 */
	private void setAlias(boolean on){
		RequestParams params = new RequestParams();
		if(on){
			String sid = APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", "");
			params.put("alias", MD5Util.MD5(sid));
		}else{
			params.put("alias", "");
		}
		params.put("registrationId", JPushInterface.getRegistrationID(this));
		Log.d("push set", "params:" + params.toString());
		HttpUtil.post(Constants.hostUrl + "/jpush/setAlias", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				Log.d("","response:" + response.toString());
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
			}
		});
	}
	
	private void getLoginInfo() {
		
	}

	public void onBackPressed(){
		APP.GetMainActivity().ShowXView(Main.XV_NEW_MORE);
		finish();
		super.onBackPressed();
	}
	
	public void forward(String name,String value, Class<?> target){
		Intent intent =new Intent(this,target);
		intent.putExtra(name, value);
		startActivity(intent);
	}
	
	class FlowBroadcastReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			//_flowtipSize.setText(intent.getStringExtra("flow"));
			readSetInfos();
		}
	}
	
	protected void onDestroy() { 
	    unregisterReceiver(_broadcast); 
	    super.onDestroy();
	}
}