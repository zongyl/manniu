package com.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.bean.Device;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;

public class ActionSheet {

	public interface OnActionSheetSelected {
		void onClick(int whichButton);
	}

	private ActionSheet() {
	}
	
	static List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
	
	@SuppressLint("Recycle")
	public static Dialog showSheet(final Context context, Device device, OnItemClickListener itemClickListener,
			OnCancelListener cancelListener) {
		
		items.clear();
		
		final Dialog dlg = new Dialog(context, R.style.dialog1);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.actionsheet, null);
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth(cFullFillWidth);

		GridView grid = (GridView) layout.findViewById(R.id.gridView);
		//List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

		String[] textArr = context.getResources().getStringArray(R.array.dialog_text);
		String[] tagArr = context.getResources().getStringArray(R.array.dialog_tag);
		TypedArray imgs = context.getResources().obtainTypedArray(R.array.dialog_ic);
		
		boolean online = (device.online == 1);
		
		Map<String, Object> map = null;
		for(int i = 0; i < textArr.length-2; i++){
			map = new HashMap<String, Object>();
			if(!online&&(i==1 || i==2)){
				continue;
			}else{
				map.put("tag", tagArr[i]);
				map.put("text", textArr[i]);
				map.put("image", imgs.getResourceId(i, 0));
				items.add(map);
			}
		}
		
		if(device.type == 1){
			RequestParams params = new RequestParams();
			params.put("deviceId", device.sid);
			HttpUtil.get(Constants.hostUrl + "/device/isShare", params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers,
						JSONObject response) {
					Log.d("isShare.onSuccess", "response:" + response.toString());
					boolean status = false;
					try {
						status = response.getBoolean("result");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if(status){
						/*for(Map<String, Object> map : items){
							if("dialog4".equals(map.get("tag"))){
								items.remove(map);
								Map<String, Object> newmap = new HashMap<String, Object>(); 
								newmap.put("tag", "dialog4");
								newmap.put("image", R.drawable.my_share_dis);
								newmap.put("text", context.getString(R.string.cel_share));
								items.add(newmap);
							}
						}*/
						Map<String, Object> newmap = new HashMap<String, Object>(); 
						newmap.put("tag", "dialog4");
						newmap.put("image", R.drawable.my_share_dis);
						newmap.put("text", context.getString(R.string.cel_share));
						items.add(newmap);
					}else{
						Map<String, Object> newmap = new HashMap<String, Object>(); 
						newmap.put("tag", "dialog4");
						newmap.put("image", R.drawable.my_share);
						newmap.put("text", context.getString(R.string.share));
						items.add(newmap);
					}
				}
				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
				}
				
			}, false);
		}
		
		//items
		map = new HashMap<String, Object>();
		map.put("tag", tagArr[4]);
		map.put("text", textArr[4]);
		map.put("image", imgs.getResourceId(4, 0));
		items.add(map);
		
		SimpleAdapter saImageItems = new SimpleAdapter(context, items, R.layout.actionsheet_item, 
				new String[]{"tag", "image", "text"}, new int[]{R.id.tag, R.id.ItemImage, R.id.ItemText});

		grid.setAdapter(saImageItems);
		
		grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		grid.setOnItemClickListener(itemClickListener);
		
		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		//lp.x = 0;
		//final int cMakeBottom = -1000;
		//lp.y = cMakeBottom;
		lp.gravity = Gravity.CENTER_VERTICAL;
		dlg.onWindowAttributesChanged(lp);
		//dlg.setCanceledOnTouchOutside(false);
		if (cancelListener != null)
			dlg.setOnCancelListener(cancelListener);

		dlg.setContentView(layout);
		dlg.show();

		return dlg;
	}

}
