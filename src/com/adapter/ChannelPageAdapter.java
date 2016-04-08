package com.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class ChannelPageAdapter extends PagerAdapter {
	private List<View> m_channlespager_viewsList;
	public ChannelPageAdapter(List<View> m_channelspager_viewsList){
		this.m_channlespager_viewsList=m_channelspager_viewsList;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public int getCount() {
		return m_channlespager_viewsList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position,
			Object object) {
		container.removeView(m_channlespager_viewsList.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(m_channlespager_viewsList.get(position));
		return m_channlespager_viewsList.get(position);
	}

}
