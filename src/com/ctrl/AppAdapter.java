package com.ctrl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.manniu.manniu.R;

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-9 下午4:25:04 To change
 * this template use File | Settings | File Templates. Description：基础数据适配器
 * 
 */
public class AppAdapter extends BaseAdapter {
	private List<Object> mList;
	private Context mContext;
	public static final int APP_PAGE_SIZE = 8;

	public AppAdapter(Context context, List<Object> list, int page) {
		mContext = context;
		mList = new ArrayList<Object>();
		int i = page * APP_PAGE_SIZE;
		int iEnd = i + APP_PAGE_SIZE;
		while ((i < list.size()) && (i < iEnd)) {
			mList.add(list.get(i));
			i++;
		}
	}

	public int getCount() {
		return mList.size();
	}

	public Object getItem(int position) {
		return mList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// Map appInfo = mList.get(position);
		AppItem appItem;
		if (convertView == null) {
			View v = LayoutInflater.from(mContext).inflate(R.layout.app_item,
					null);

			appItem = new AppItem();
			appItem.mAppIcon = (ImageView) v.findViewById(R.id.imgdetail);

			v.setTag(appItem);
			convertView = v;
		} else {
			appItem = (AppItem) convertView.getTag();
		}
		// set the icon
		appItem.mAppIcon.setImageResource((Integer) mList.get(position));
		// set the app name
		return convertView;
	}

	/**
	 * 每个应用显示的内容，包括图标和名称
	 */
	class AppItem {
		ImageView mAppIcon;
	}
}