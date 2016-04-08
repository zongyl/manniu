package com.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import com.bean.DevCart;
import com.bean.Device;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.FileUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter{
	/** 设备列表数据 */
	private List<Device> m_devicesList = new ArrayList<Device>();
	/** 选中的设备数据 */
	public List<DevCart> m_devCartsList = new ArrayList<DevCart>();
	
	private Context m_context;
	
	public ExpandableListViewAdapter(Context _context){
		this.m_context = _context;
	}
	
	public void addItem(List<Device> data){
		this.m_devicesList.addAll(data);
	}
	
	public int updateList(List<Device> data) {
		m_devicesList.clear();
    	this.m_devicesList.addAll(data);
		this.notifyDataSetChanged();
		return m_devicesList.size();
	}
	
	@Override
	public int getGroupCount()
	{
		return m_devicesList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		int chnlNums = (int)m_devicesList.get(groupPosition).channels;
		return chnlNums;
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		return m_devicesList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View contentView = LayoutInflater.from(m_context).inflate(R.layout.layout_home_deviceslist_cell, null);
		Device deviceInfo = m_devicesList.get(groupPosition);
		// 判断数组中是否存在当前的DeviceInfo
		boolean isSelected = false;
		for (int i = 0; i < m_devCartsList.size(); i++) {
			DevCart _devCart = m_devCartsList.get(i);
			if (_devCart.getDeviceInfo().getSid().equals(deviceInfo.sid)) {
				isSelected = true;
			}
		}
		// 获取左侧图片
		ImageView imgDevImage = (ImageView) contentView.findViewById(R.id.home_devslist_cell_img);
		// 在线状态
		if (deviceInfo.online == 1) {
			// IPC设备
			if (deviceInfo.channels == 1) {
				if (isSelected) {
					imgDevImage.setImageResource(R.drawable.home_ipc_online_selected);
				} else {
					imgDevImage.setImageResource(R.drawable.home_ipc_online);
				}
			}else {// DVR、NVR设备
				if (isSelected) {
					imgDevImage.setImageResource(R.drawable.home_dvr_online_selected);
				} else {
					imgDevImage.setImageResource(R.drawable.home_dvr_online);
				}
			}
		}else {// 离线状态
			// IPC设备
			if (deviceInfo.channels == 1) {
				imgDevImage.setImageResource(R.drawable.home_ipc_offline);
			}else {// DVR、NVR设备
				imgDevImage.setImageResource(R.drawable.home_dvr_offline);
			}
		}

		// 获取文字信息
		TextView tvDevName = (TextView) contentView.findViewById(R.id.home_devslist_cell_txtlabel);
		TextView tvDetailName = (TextView) contentView.findViewById(R.id.home_devslist_cell_detaillabel);
		tvDevName.setText(deviceInfo.getDevname());
		// 1-IP设备、4-牛眼
		if (deviceInfo.type == 1 || deviceInfo.type == 4) {
			tvDetailName.setText(deviceInfo.sid);
		}
		// 设置more按钮事件
		View btnMore = contentView.findViewById(R.id.home_devslist_cell_morelayout);
		btnMore.setTag(groupPosition);
		// btnMore.setOnClickListener(new OnGroupMoreClickListener());

		return contentView;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// 当前选中的devCart
		DevCart devCart = new DevCart();
		devCart.setDeviceInfo(m_devicesList.get(groupPosition));
		devCart.setChannelNum(childPosition + 1);

		View contentView = LayoutInflater.from(m_context).inflate(R.layout.layout_home_deviceslist_channels_cell, null);
		TextView tvChName = (TextView) contentView.findViewById(R.id.home_devslist_channel_cell_txtlabel);
		tvChName.setText(m_context.getString(R.string.home_chn) + " " + (childPosition + 1));
		try {
			// 通道缩略图
			Device devInfo = devCart.getDeviceInfo();
			if(devInfo.logo != null && !devInfo.logo.equals("")){
				String name = devInfo.logo.substring(devInfo.logo.indexOf("aliyuncs.com") + 12,devInfo.logo.length());
				File file = new File(DevAdapter.rootPath + devInfo.sid + name);
				if (file.exists()) {
					Bitmap bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
					ImageView leftView = (ImageView) contentView.findViewById(R.id.home_devslist_channel_cell_img);
					leftView.setImageBitmap(bitmap);
				}
			}
			// 判断数组中是否存在当前的devCart
			boolean isSelected = false;
			for (int i = 0; i < m_devCartsList.size(); i++) {
				DevCart _devCart = m_devCartsList.get(i);
				if (_devCart.getDeviceInfo().getSid()
						.equals(devCart.getDeviceInfo().getSid())
						&& devCart.getChannelNum() == _devCart.getChannelNum()) {
					isSelected = true;
				}
			}
			// 当前通道已选中
			if (isSelected) {
				ImageView selectImgView = (ImageView) contentView.findViewById(R.id.home_devslist_channel_cell_select_img);
				selectImgView.setImageResource(R.drawable.home_channel_checked);
			} else {
				ImageView selectImgView = (ImageView) contentView.findViewById(R.id.home_devslist_channel_cell_select_img);
				selectImgView.setImageResource(R.drawable.home_channel_unchecked);
			}
		} catch (Exception e) {
		}
		return contentView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

}
