package com.views;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.adapter.FlowAdapter;
import com.bean.Menu;
import com.manniu.manniu.R;
import com.utils.SetSharePrefer;

public class NewFlowTipSet extends Activity implements OnClickListener ,OnItemClickListener{
	final static int CHANGED =0X1233;
	private ListView _listview;
	private TextView _newTxt;
	private FlowAdapter _flowadpater;
	public static final String action = "flow.broadcast.action";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_flow_tip_set);
		_listview =(ListView) findViewById(R.id.flow_list);
		_flowadpater = new FlowAdapter(this,getMenuList(this,R.array.flow),true,getIntent().getStringExtra("tipSize"));
		_listview.setAdapter(_flowadpater);
		findViewById(R.id.cancel_flowtip).setOnClickListener(this);
		_listview.setOnItemClickListener(this);
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
		case R.id.cancel_flowtip:
			finish();
		break;
		}
		
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ImageView checked = (ImageView) ((LinearLayout) view).findViewById(R.id.flow_checked);
		_newTxt =(TextView) ((LinearLayout) view).findViewById(R.id.flow_text);
		checked.setVisibility(View.VISIBLE);
		/*boolean flag = false;
		for(int i=0;i<_listview.getCount();i++){
			LinearLayout lineralayout = (LinearLayout) _listview.getAdapter().getView(i,null, null);
			TextView oldTxt = (TextView) lineralayout.getChildAt(0);
			ImageView oldImg = (ImageView) lineralayout.getChildAt(2);
			Log.v("未选中图标", ""+oldImg.getVisibility());
			if(!oldTxt.getText().equals(newTxt.getText())){
				oldImg.setVisibility(View.GONE);
				flag = true;
				if(flag){
					break;
				}
			}
			if(flag){
				break;
			}
		}*/
		SetSharePrefer.write("Info_Set", "tipsize", _newTxt.getText().toString());
		notifyChanged();
		
	}
	
	private void notifyChanged() {
		Intent intent = new Intent(action);
		//intent.putExtra("flow", _newTxt.getText());
		sendBroadcast(intent);
		finish();
	}

	public void onBackPressed(){
		finish();
		super.onBackPressed();
	}
	
	public void forward(String name,String value,Class<?>  target){
		Intent intent = new Intent(this,target);
		intent.putExtra(name, value);
		startActivity(intent);
	}
}