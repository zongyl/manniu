package com.ctrl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
public class RealAlarmAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	int layoutId;
	int []itemType;
	int[]itemId;
	ListView listView;
	private List<Map<Integer, Object>> Data = new ArrayList<Map<Integer, Object>>();
	IXList _user;
	// type 0: TextView 1 : ImageView 2 : Button
	public RealAlarmAdapter(Context context, IXList user, View listView, int layoutId, int []itemType, int[] itemId) {
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
//		this.nodeListToShow.clear();
//		establishNodeListToShow(this.root);
		this.notifyDataSetInvalidated();
	}
	
	public void AddItem(Map<Integer, Object> map){
		this.Data.add(map);
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

	ListItem2 holder = null;
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(layoutId, null);
			holder = new ListItem2(convertView, position);
			convertView.setTag(holder);
		} else {
			holder = (ListItem2) convertView.getTag();
		}

		holder.SetPosition(convertView, position);
//		convertView.invalidate();
		return convertView;
	}
	
	class ListItem2 implements OnClickListener{
		int position = 0;
		ListItem2(View convertView, int position){
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
					String sText = (String)Data.get(position).get(itemId[i]);
					if(sText != null){
						tv.setText(sText);
					}
					break;
				case 1:
					break;
				case 2:
					break;
				}
			}
		}

		public void onClick(View v) {
//			RealAlarmAdapter.this._user.OnClickedItem(RealAlarmAdapter.this, v, position, 
//					RealAlarmAdapter.this.Data.get(position).get(layoutId));
		}
		
	}
}