package com.views.bovine;

import java.util.List;  

import android.support.v4.app.Fragment;  
import android.support.v4.app.FragmentManager;  
import android.support.v4.app.FragmentPagerAdapter;  

/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-8-29 下午1:40:13
 * To change this template use File | Settings | File Templates.
 * Description： FragmentPagerAdapter --fragment 适配器
 */
public class FragAdapter extends FragmentPagerAdapter{  
    
    private List<Fragment> fragments;  
    //private int mCount = 0;
  
    public FragAdapter(FragmentManager fm) {  
        super(fm);  
    }  
      
    public FragAdapter(FragmentManager fm, List<Fragment> fragments) {  
        super(fm);  
        this.fragments = fragments;  
    }  
  
    @Override  
    public Fragment getItem(int position) {  
        return fragments.get(position);  
    }  
  
    @Override  
    public int getCount() {  
        return fragments.size();  
    }

	
}  
