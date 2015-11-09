package com.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class HelpAdapter extends FragmentPagerAdapter{
	private final static String TAG="helpadatper";
	private List<Fragment> list;
	private List<String> titleList;
	public HelpAdapter(FragmentManager fm,List<Fragment> list,List<String> titles) {
		super(fm);
		this.list = list;
		this.titleList = titles;
	}
	
	@Override  
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }  
	@Override
	public Fragment getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Log.v(TAG, "instantiateitem");
		super.instantiateItem(container, position);
		return list.get(position);
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titleList.get(position);
	}
}
