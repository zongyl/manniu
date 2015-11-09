package com.ctrl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.manniu.manniu.R;
import com.views.BriefInfoBean;
/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-9-03 下午09:50:39
 * To change this template use File | Settings | File Templates.
 * Description： 简单数据列表适配器 (报表数据)
 */
public class CommListAdapter extends BaseAdapter{
	
	private Context _context;
	private List<BriefInfoBean> _data = new ArrayList<BriefInfoBean>();
	public CommListAdapter(Context context){
		this._context = context;
		//this._data = data;
	}

	@Override
	public int getCount() {
		return _data.size();
	}
	public void Clear(){
		this._data.clear();
	}
	public void addItem(List<BriefInfoBean> data){
		this._data.addAll(data);
		Collections.reverse(this._data);
	}
	public int updateList(List<BriefInfoBean> data) {
		_data.clear();
		this._data.addAll(data);
		Collections.reverse(this._data);
		this.notifyDataSetChanged();
		return _data.size();
	}
	
	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
	     public TextView tv1;
	     public TextView tv2;
	}
	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;
		if(view == null){
			holder = new ViewHolder();
			view = LayoutInflater.from(_context).inflate(R.layout.list_item_dev, null);
			holder.tv1 = (TextView) view.findViewById(R.id.tv_title);
			holder.tv2 = (TextView) view.findViewById(R.id.tv_title2);
			view.setTag(holder);
			
		}else {
			holder = (ViewHolder) view.getTag();
		}
		BriefInfoBean item = _data.get(position);
		holder.tv1.setText(item.getInfo());
		BigDecimal b = new BigDecimal(item.getDataValue());
		holder.tv2.setText(String.valueOf(b.setScale(2, BigDecimal.ROUND_HALF_UP)));
		return view;
	}

}