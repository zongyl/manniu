package com.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bean.Menu;
import com.manniu.manniu.R;

public class FlowAdapter extends BaseAdapter{

	private Context context;
	
	private List<Menu> items;
	
	LayoutInflater inflater;
	
	Boolean ishidden;
	String tipSet;
	public FlowAdapter(Context _context, List<Menu> _items, Boolean _ishidden,String tipSet){
		this.context = _context;
		this.items = _items;
		this.ishidden = _ishidden;
		this.tipSet = tipSet;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = inflater.inflate(R.layout.new_flow_item, null);	
		TextView tv = (TextView)rowView.findViewById(R.id.flow_text);
		TextView tvf = (TextView)rowView.findViewById(R.id.flow_itemcount);
		if(!ishidden){
			tvf.setVisibility(View.VISIBLE);
		}
		ImageView iv = (ImageView)rowView.findViewById(R.id.flow_checked);
		tv.setText(items.get(position).text);
		//iv.setBackgroundResource(items.get(position).iconResid);
		if(tv.getText().equals(tipSet)){
			iv.setVisibility(View.VISIBLE);
		}
		return rowView;
	}

}
