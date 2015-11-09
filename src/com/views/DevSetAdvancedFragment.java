package com.views;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.manniu.manniu.R;

/**
 * 
 * @author zongyl
 * 设备设置  高级配置
 *
 */
public class DevSetAdvancedFragment extends Fragment {

	public static final String TAG = "DevSetAdvancedFragment";
	
	Spinner frameRate, bitStream;
	
	Context context;
	
	ArrayAdapter<CharSequence> adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.new_device_set_advanced_fragment, null);
		context = this.getActivity();
		frameRate = (Spinner) view.findViewById(R.id.frameRate);
		bitStream = (Spinner) view.findViewById(R.id.bitStream);
		

		adapter = new ArrayAdapter<CharSequence>(context, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetFrameRate));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		frameRate.setAdapter(adapter);
		frameRate.setOnItemSelectedListener(new SelectedListener());
		frameRate.setSelection(0);
		adapter.notifyDataSetChanged();
		
		adapter = new ArrayAdapter<CharSequence>(context, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetBitStream));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		bitStream.setAdapter(adapter);
		bitStream.setOnItemSelectedListener(new SelectedListener());
		bitStream.setSelection(0);
		adapter.notifyDataSetChanged();
		return view;
	}
	
	class SelectedListener implements OnItemSelectedListener{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Log.d(TAG, "position:"+position+" id:"+id);
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	}
}
