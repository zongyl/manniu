package com.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.bean.Device;
import com.manniu.manniu.R;

public class AnalogSheet {

	public interface OnActionSheetSelected {
		void onClick(int whichButton);
	}

	private AnalogSheet() {
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

		String[] textArr = context.getResources().getStringArray(R.array.dialog_text);
		String[] tagArr = context.getResources().getStringArray(R.array.dialog_tag);
		TypedArray imgs = context.getResources().obtainTypedArray(R.array.dialog_ic);
		
		Map<String, Object> map = null;
		for(int i = 0; i < textArr.length-2; i++){
			if(i!=1){
				map = new HashMap<String, Object>();
				map.put("tag", tagArr[i]);
				map.put("text", textArr[i]);
				map.put("image", imgs.getResourceId(i, 0));
				items.add(map);
			}
		}
		
		SimpleAdapter saImageItems = new SimpleAdapter(context, items, R.layout.actionsheet_item, 
				new String[]{"tag", "image", "text"}, new int[]{R.id.tag, R.id.ItemImage, R.id.ItemText});

		grid.setAdapter(saImageItems);
		
		grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		grid.setOnItemClickListener(itemClickListener);
		
		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.gravity = Gravity.CENTER_VERTICAL;
		dlg.onWindowAttributesChanged(lp);
		if (cancelListener != null)
			dlg.setOnCancelListener(cancelListener);
		dlg.setContentView(layout);
		dlg.show();

		return dlg;
	}

}
