/**
 * Program  : T1Activity.java
 * Author   : qianj
 * Create   : 2012-5-31 下午4:24:32
 *
 * Copyright 2012 by newyulong Technologies Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of newyulong Technologies Ltd.("Confidential Information").  
 * You shall not disclose such Confidential Information and shall 
 * use it only in accordance with the terms of the license agreement 
 * you entered into with newyulong Technologies Ltd.
 *
 */

package com.views.bovine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.localsnap.AlbumBucket;
import com.localsnap.AlbumBucketAdapter;
import com.localsnap.AlbumHelper;
import com.localsnap.BrowseAlbumActivity;
import com.localsnap.Fun_ImgGridActivity;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.views.Fun_Setting;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @version  1.0.0
 */
@SuppressLint("NewApi")
public class Fra_SnapActivity extends Fragment{
	
	List<AlbumBucket> dataList;
	GridView gridView;
	AlbumBucketAdapter adapter;// 自定义的适配器
	AlbumHelper helper;
	View _view;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_view = inflater.inflate(R.layout.media_tab_snap, null);
		
		helper = AlbumHelper.getHelper();
		helper.init(this.getActivity().getApplicationContext());
		initData();
		initView();
		
		return _view;
	}
	
	/**
	 * 初始化数据
	 */
	public void initData() {
		dataList = helper.getImagesBucketList(Fun_AnalogVideo.ImagePath);	
		Collections.sort(dataList);
		Constants.bimap=BitmapFactory.decodeResource(
				getResources(),
				R.drawable.icon_addpic_unfocused);
		Constants.bimap.recycle(); //释放Bitmap资源
	}
	/**
	 * 初始化view视图
	 */
	public void initView() {
		gridView = (GridView) _view.findViewById(R.id.gridview);
		adapter = new AlbumBucketAdapter(this.getActivity(), dataList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/**
				 * 根据position参数，可以获得跟GridView的子View相绑定的实体类，然后根据它的isSelected状态，
				 * 来判断是否显示选中效果。 至于选中效果的规则，下面适配器的代码中会有说明
				 * 通知适配器，绑定的数据发生了改变，应当刷新视图
				 */
				AlbumBucket imageBucket = dataList.get(position);
				Constants._bucketName = imageBucket.bucketName;
				BrowseAlbumActivity._path = Constants._bucketName;
				adapter.notifyDataSetChanged();
				Intent intent = new Intent(Fra_SnapActivity.this.getActivity(),Fun_ImgGridActivity.class);
				intent.putExtra("imagelist",
						(Serializable) dataList.get(position).imageList);
				startActivity(intent);
			}
		});
	}
	
	
	
}

