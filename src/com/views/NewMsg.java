package com.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ScrollView;

import com.adapter.HttpUtil;
import com.adapter.Message;
import com.adapter.MsgAdapter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.basic.APP;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;

public class NewMsg extends XViewBasic implements OnItemClickListener {

	private final static String TAG = "NewMsg";
	
	Context context;
	
	SurfaceView surfaceView;
	
	ListView listView;
	
	PullToRefreshScrollView scrollView;
	
	ArrayList<Message> msgList;
	
	MsgAdapter adapter;
	
	public CheckBox cb;
	
	HashMap<String, Object> isSelected = new HashMap<String, Object>();

	public NewMsg(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		context = activity;
		
		listView = (ListView)findViewById(R.id.msg_list);
		
		listView.setOnItemClickListener(this);
		
		scrollView = (PullToRefreshScrollView)findViewById(R.id.pull_refresh_msg);
		
		scrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				new GetDataTask().execute(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())),
						ACT.getResources().getString(R.string.server_address)+"/android/getDevices");
			}
		});
		
		load();

		cb = (CheckBox)findViewById(R.id.msg_ck_all);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				selectAll(isChecked);
			}
		});
		
		/*findViewById(R.id.btn_edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				edit();
			}
		});*/
	}

	/**
	 * 全选
	 * @param sel
	 */
	private void selectAll(boolean sel){
		isSelected.put("移动侦测报警", sel);
		isSelected.put("移动侦测报警1", sel);
		isSelected.put("移动侦测报警2", sel);
		isSelected.put("移动侦测报警3", sel);
		adapter.isSelected = isSelected;
		adapter.notifyDataSetChanged();	
		APP.GetMainActivity().setValue(adapter.sumByChecked(adapter.isSelected, true));
	}
	
	/**
	 * 编辑消息列表
	 */
	public void edit(){
		if(adapter.show){
			adapter.show = false;
			adapter.notifyDataSetChanged();
			findViewById(R.id.msg_frame).setVisibility(View.GONE);
			//findViewById(R.id.msg_frame_bom).setVisibility(View.GONE);
		}else{
			adapter.show = true;
			adapter.notifyDataSetChanged();
			findViewById(R.id.msg_frame).setVisibility(View.VISIBLE);
			//findViewById(R.id.msg_frame_bom).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Message msg = msgList.get(position);
		//APP.ShowToast(""+msg.title);
		Log.v(TAG, msg.toString());
		Intent intent = new Intent(ACT,NewMsgDetail.class);
		Bundle bd = new Bundle();
	
		bd.putParcelableArrayList("msgList", msgList);
		intent.putExtras(bd);
		intent.putExtra("position", position);
		APP.GetMainActivity().startActivity(intent);
		
	}

	private class GetDataTask extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {
			return getMsg();
		}
		
		@Override
		protected void onPostExecute(String result) {
			//tv.setText(result);
			//APP.ShowToast("......"+result+".......");
			if("failure".equals(result)){
				APP.ShowToast(ACT.getResources().getString(R.string.E_SER_FAIL));
			}else{
				render(result, "pulltorefsh");
			}
			// Call onRefreshComplete when the list has been refreshed.
			scrollView.onRefreshComplete();
			super.onPostExecute(result);
		}
		
	}

	private void load(){
		msgList = new ArrayList<Message>();
		RequestParams params = new RequestParams();
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "id", ""));
		HttpUtil.get(Constants.hostUrl+"/android/getDevices", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
				if(statusCode == 200){
					render(getMsg(), "first");
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(ACT.getResources().getString(R.string.E_SER_FAIL));
			}
		});
	}
	
	/**
	 * 模拟服务器端的消息推送数据
	 */
	private String getMsg(){
		Message msg = new Message();
		/*msg.title = "移动侦测报警";
		msg.time = System.currentTimeMillis();
		msg.devName = "C2mini(xxxxxxxxx)";
		Message msg1 = new Message();
		msg1.title = "移动侦测报警1";
		msg1.time = System.currentTimeMillis();
		msg1.devName = "C2mini(xxxxxxxxx)";
		Message msg2 = new Message();
		msg2.title = "移动侦测报警2";
		msg2.time = System.currentTimeMillis();
		msg2.devName = "C2mini(xxxxxxxxx)";
		Message msg3 = new Message();
		msg3.title = "移动侦测报警3";
		msg3.time = System.currentTimeMillis();
		msg3.devName = "C2mini(xxxxxxxxx)";*/
		
		List<Message> list = new ArrayList<Message>();
		list.add(msg);
	/*	list.add(msg1);
		list.add(msg2);
		list.add(msg3);*/
		
		return JSON.toJSON(list).toString();
	}
	
	private void render(String json, String flag){
		msgList.clear();
			Log.v(TAG,flag+":"+json);
			JSONArray array = JSON.parseArray(json);
			for(int i = 0; i < array.size(); i++){
				Message msg = JSON.toJavaObject((JSON)array.get(i), Message.class);
				msgList.add(msg);
			/*	Log.v(TAG, ""+msg.title);
				Log.v(TAG, ""+msg.time);
				Log.v(TAG, ""+msg.devName);*/
			}
			if(adapter!=null&&adapter.show){
				Log.v(TAG, "isSelected："+isSelected);
				Log.v(TAG, "adapter.isSelected："+adapter.isSelected);
				HashMap<String, Object> maps = new HashMap<String, Object>();
				if(adapter.isSelected!=null){
					//isSelected = adapter.isSelected;
					maps = adapter.isSelected;
				}
				adapter = new MsgAdapter(context, msgList);
				listView.setAdapter(adapter);
				adapter.show = true;
				adapter.notifyDataSetChanged();
				//adapter.isSelected = isSelected;
				adapter.isSelected = maps;
				adapter.notifyDataSetChanged();
			}else{
				adapter = new MsgAdapter(context, msgList);
				listView.setAdapter(adapter);
			}
	}
	
}
