package com.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manniu.manniu.R;

public class MsgImgPager extends PagerAdapter{

	public ArrayList<Message> msgs = null;
	public Context mContext;
	
	public MsgImgPager(ArrayList<Message> msgs,Context context){
		this.msgs = msgs;  
        this.mContext = context;  
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0==arg1;
	}
	
	public void destroyItem(View container,int position,Object object){
		
	}
	
	public Object instantiateItem(ViewGroup container,int position){
		
		
			View imageLayout = LayoutInflater.from(mContext).inflate(R.layout.item_pager_image, container, false);
					
					
		
		
		return imageLayout;
	}
	
	public void setPrimaryItem(View container,int position,Object object){
		 super.setPrimaryItem(container, position, object);
		 
	}
}
