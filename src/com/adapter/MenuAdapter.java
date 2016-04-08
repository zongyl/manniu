package com.adapter;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.manniu.manniu.R;

public class MenuAdapter extends BaseAdapter{
	
	private Context context;
	private List<Menu> items;
	LayoutInflater inflater;
	private boolean showV;
	public MenuAdapter(Context _context, List<Menu> _items,boolean showV){
		this.context = _context;
		this.items = _items;
		this.showV = showV;
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
		View rowView = inflater.inflate(R.layout.new_more_item, null);
		ImageView iv = (ImageView)rowView.findViewById(R.id.menu_img);
		TextView tv = (TextView)rowView.findViewById(R.id.menu_txt);
		TextView bor = (TextView) rowView.findViewById(R.id.divider);
		TextView desc =(TextView) rowView.findViewById(R.id.menu_desc);
		if(position == items.size()-1){
			bor.setVisibility(View.INVISIBLE);
		}
		if(showV && position==1){
			try {
				desc.setText("V"+context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		iv.setBackgroundResource(items.get(position).getIconResid());
		tv.setText(items.get(position).getText());
		return rowView;
	}

}
