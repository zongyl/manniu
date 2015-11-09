package com.ctrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 对讲功能适配器
 * 
 * 列表的显示需要三个元素：
 * 1．ListVeiw 用来展示列表的View。
 * 2．适配器 用来把数据映射到ListView上的中介。
 * 3．数据    具体的将被映射的字符串，图片，或者基本组件。
 */
public class XListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	int layoutId;
	int []itemType;
	int[]itemId;
	ListView listView;
	private List<Map<Integer, Object>> Data = new ArrayList<Map<Integer, Object>>();
	IXList _user;
	// type 0: TextView 1 : ImageView 2 : Button
	public XListAdapter(Context context, IXList user, View listView, int layoutId, int []itemType, int[] itemId) {
		this.mInflater = LayoutInflater.from(context);
		this.listView = (ListView)listView;
		_user = user;
		this.layoutId = layoutId;
		int count = itemType.length;
		this.itemType = new int[count];
		System.arraycopy(itemType, 0, this.itemType, 0, count);
		
		this.itemId = new int[count];
		System.arraycopy(itemId, 0, this.itemId, 0, count);
		
		this.listView.setAdapter(this);
	}
	
	public ListView GetView(){
		return listView;
	}
	public void Clear(){
		this.Data.clear();
	}
	public void AddItem(Map<Integer, Object> map, Object obj){
		map.put(layoutId, obj);
		this.Data.add(map);
	}
	
	public void setAlarmListToShow() {
		this.notifyDataSetInvalidated();
	}
	
	public void AddItem(Map<Integer, Object> map){
		this.Data.add(map);
		if(this.Data.size() > 49){
			this.Data.remove(0);
		}
		Collections.reverse(this.Data);
	}
	
	public void getListData(List<Map<Integer, Object>> list){
		this.Data = list;
	}
	
	public int getCount() {
		return Data.size();
	}

	public Object getItem(int postion) {
		return null;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<Integer, Object> GetItemByObj(Object obj){
		Iterator itr = Data.iterator();
		Map<Integer, Object> item = null;
		Object objItem;
		while (itr.hasNext()) {
			item = (Map<Integer, Object>) itr.next();
			objItem = item.get(layoutId);
			if(objItem != null && objItem.equals(obj)){
				return item;
			}
		}
		
		return null;
	}
	
	public Object getMapItem(int postion, Integer key) {
		if(postion < 0 || postion >= Data.size()){
			return null;
		}
		Map<Integer, Object> map = Data.get(postion);
		return map.get(key);
	}
	
	public Object getItemData(int postion) {
		if(postion < 0 || postion >= Data.size()){
			return null;
		}
		Map<Integer, Object> map = Data.get(postion);
		return map.get(layoutId);
	}

	public long getItemId(int postion) {
		return 0;
	}
//	如果在非activity中如何对控件布局设置操作了，这就需要LayoutInflater动态加载。
	ListItem holder = null;
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(layoutId, null);
			holder = new ListItem(convertView, position);
			convertView.setTag(holder);
			
		} else {
			holder = (ListItem) convertView.getTag();
		}
//		if(this.listView.getId() == 2131034162){
//			convertView.setBackgroundResource(R.anim.list_selector);
//		}
			
		holder.SetPosition(convertView, position);
		return convertView;
	}
	
	class ListItem implements OnClickListener{
		int position = 0;
		ListItem(View convertView, int position){
			this.position = position;
			int count = itemType.length;
			for(int i = 0; i < count; ++i){
				convertView.findViewById(itemId[i]).setOnClickListener(this);
			}
			convertView.setOnClickListener(this);
		}
		
		void SetPosition(View convertView, int position){
			this.position = position;
			int count = itemType.length;
			for(int i = 0; i < count; ++i){
				switch(itemType[i]){
				case 0:
					TextView tv = (TextView) convertView.findViewById(itemId[i]);
					//System.out.println("000;;;"+itemId[i]);
					String sText = (String)Data.get(position).get(itemId[i]);
					//System.out.println("000;;;"+sText);
					if(sText != null){
						tv.setText(sText);
					}
					break;
				case 1:
					ImageView iv = (ImageView) convertView.findViewById(itemId[i]);
					Integer imageId = (Integer) Data.get(position).get(itemId[i]);
					//System.out.println("11;;;"+itemId[i]+"--"+imageId);
					if(imageId != null && imageId != 0){
						iv.setImageResource(imageId);
					}
					break;
				case 2:
					break;
				}
			}
		}

		public void onClick(View v) {
			XListAdapter.this._user.OnClickedItem(XListAdapter.this, v, position, 
					XListAdapter.this.Data.get(position).get(layoutId));
		}
		
	}
}