package com.views;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.adapter.FlowAdapter;
import com.bean.Menu;
import com.manniu.manniu.R;

public class NewFlowCount extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_flow_count);
		ListView listview =(ListView) findViewById(R.id.flowcount_list);
		listview.setAdapter(new FlowAdapter(this,getMenuList(this,R.array.flow_count),false,""));
		findViewById(R.id.flc_cancel).setOnClickListener(this);
	}
	
	public List<Menu> getMenuList(Activity act, int resid){
		List<Menu> ret = new ArrayList<Menu>();
		Menu menu;
		for(String str : act.getResources().getStringArray(resid)){
			menu = new Menu();
			menu.text = str;
			//menu.iconResid = R.drawable.common_title_confirm_sel;
			ret.add(menu);
		}
		return ret;
	}
	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.flc_cancel:
			finish();
			break;
		}
		
	}
	
	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}
}