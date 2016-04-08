package com.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.adapter.ExpandableListViewAdapter;
import com.adapter.PullToRefreshExpandableListView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.basic.APP;
import com.bean.DevCart;
import com.bean.Device;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.views.NewMain.OnClickDevicesChildListListener;
import com.views.NewMain.OnPullableRefreshListener;

import P2P.SDK;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;

/**
 * @author: li_jianhua Date: 2016-4-6 上午10:52:27
 * To change this template use File | Settings | File Templates.
 * Description：添加通道
 */

public class Fun_AddChannelActivity extends Activity{
	
	
	/** 多画面设备列表的Adapter */
	private ExpandableListViewAdapter m_expandableListViewAdapter = null;
	/** 下拉刷新设备列表 */
	private ExpandableListView m_pullToRefreshExpandableListView = null;//多画面
	/** 选中的设备数据 */
	private List<DevCart> _devCartsList = null;
	/** 多画面设备列表数据 */
	List<Device> m_devicesList;
	/** 播放按钮 */
	private View m_playView = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_addchannel);
		
		m_pullToRefreshExpandableListView = (ExpandableListView) findViewById(R.id.lv_activity_addchannel);
		m_pullToRefreshExpandableListView.setOnChildClickListener(new OnClickDevicesChildListListener());
		m_playView = findViewById(R.id.play_addchannel_activity);
		m_playView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (_devCartsList.size() > 0){
						/*Intent intent = new Intent(ACT, Fun_RealPlayerActivity.class);
						//intent.setClass(ACT, Fun_RealPlayerActivity.class);
						if(_devCartsList!=null && _devCartsList.size()>0){
							intent.putExtra("RealPlayer_devices", (Serializable) _devCartsList);
						}
						ACT.startActivity(intent);*/
						Intent intent=new Intent();
						List<DevCart> myList=new ArrayList<DevCart>();
						myList.addAll(_devCartsList);
						myList.removeAll(m_devicesList);
						if(myList.size()>0){
							intent.putExtra("AddChannelList", (Serializable)myList);
							intent.putExtra("AddChannelActivity", (Serializable)_devCartsList);
							setResult(1, intent);
							finish();
						}else{
							setResult(2);
							finish();
						}
					}else{
						APP.ShowToast(APP.GetString(R.string.home_select_chn));
					}
				} catch (Exception e) {
					System.out.println(111);
				}
			}
		});
		if(getIntent().getSerializableExtra("AddChannelActivity") != null){
			_devCartsList=(List<DevCart>)getIntent().getSerializableExtra("AddChannelActivity");
		}else{
			_devCartsList=new ArrayList<DevCart>();
		}
		initData();
	}
	
	public void initData(){
		String json = NewMain.instance.cache.getAsString(Constants.userid + "_devices");
		if(m_devicesList == null){
			m_devicesList = new ArrayList<Device>();
		}
		m_devicesList.clear();
		if(!json.equals("{}")){
			JSONArray array = JSON.parseArray(json);
			for(int i = 0; i < array.size(); i++){
				Device dev1 = JSON.toJavaObject((JSON)array.get(i), Device.class);
				m_devicesList.add(dev1);
			}
		}
		if(m_expandableListViewAdapter == null){
			m_expandableListViewAdapter = new ExpandableListViewAdapter(Fun_AddChannelActivity.this);
			m_expandableListViewAdapter.addItem(m_devicesList);
			m_expandableListViewAdapter.m_devCartsList.addAll(_devCartsList);
			m_pullToRefreshExpandableListView.setAdapter(m_expandableListViewAdapter);
			m_pullToRefreshExpandableListView.setGroupIndicator(null);
		}else{
			m_expandableListViewAdapter.updateList(m_devicesList);
		}
	}
	
	// Child点击的监听器
	class OnClickDevicesChildListListener implements ExpandableListView.OnChildClickListener{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id){
			if(m_devicesList.get(groupPosition).online == 0){
				APP.ShowToast(SDK.GetErrorStr(-896));//设备不在线
				return false;
			}
			// 当前选中的devCart
			DevCart devCart = new DevCart();
			devCart.setDeviceInfo(m_devicesList.get(groupPosition));
			devCart.setChannelNum(childPosition + 1);
			
			// 判断数组中是否存在当前的devCart
			boolean isSelected = false;
			int select_index = -1;
			for (int i = 0; i < _devCartsList.size(); i++){
				DevCart _devCart = _devCartsList.get(i);
				if (_devCart.getDeviceInfo().getSid().equals(devCart.getDeviceInfo().getSid()) && devCart.getChannelNum() == _devCart.getChannelNum()){
					isSelected = true;
					select_index = i;
				}
			}

			// 当前通道已选中
			if (isSelected){
				_devCartsList.remove(select_index);
			}else{// 当前通道未选中
				if (_devCartsList.size() >= 4){
					Toast.makeText(Fun_AddChannelActivity.this, R.string.alertMsg23, Toast.LENGTH_SHORT).show();
				}else{
					_devCartsList.add(devCart);
				}
			}
			m_expandableListViewAdapter.m_devCartsList.clear();
			m_expandableListViewAdapter.m_devCartsList.addAll(_devCartsList);
			m_expandableListViewAdapter.notifyDataSetChanged();
			return true;
		}

	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			setResult(2);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
