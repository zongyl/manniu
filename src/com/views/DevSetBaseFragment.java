package com.views;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.manniu.manniu.R;

/**
 * 
 * @author zongyl
 * 设备设置  基础设置
 *
 */
public class DevSetBaseFragment extends Fragment {

	public static final String TAG = "DevSetBaseFragment";
	
	Spinner resolution, quality;
	
	Context context;
	
	ArrayAdapter<CharSequence> adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.new_device_set_base_fragment, null);
		
		context = this.getActivity();

		resolution = (Spinner) view.findViewById(R.id.resolution);
		quality = (Spinner) view.findViewById(R.id.quality);

		adapter = new ArrayAdapter<CharSequence>(context, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetResolution));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		resolution.setAdapter(adapter);
		resolution.setOnItemSelectedListener(new SelectedListener());
		resolution.setSelection(0);
		adapter.notifyDataSetChanged();

		adapter = new ArrayAdapter<CharSequence>(context, R.layout.my_spinner_item,
				getResources().getStringArray(R.array.devSetQuality));
		adapter.setDropDownViewResource(R.layout.spinner_checked_text);
		quality.setAdapter(adapter);
		quality.setOnItemSelectedListener(new SelectedListener());
		quality.setSelection(0);
		adapter.notifyDataSetChanged();
		
		view.findViewById(R.id.device_set_network).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle data = new Bundle();
				data.putString("deviceId", ((NewDeviceSet)context).getDeviceId());
				forward(NewDeviceSetNetWork.class, data, 1);
			}
		});
		return view;
	}
	
	private void forward(Class clazz, Bundle extras, int requestCode){
		Intent intent = new Intent(context, clazz);
		if(extras != null){
			intent.putExtras(extras);
		}
		startActivityForResult(intent, requestCode);
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

	//private OnClickListener click = s
	
}
