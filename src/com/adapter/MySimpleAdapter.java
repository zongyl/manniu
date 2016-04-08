package com.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import P2P.SDK;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.FileUtil;
import com.utils.ScreenCache;

public class MySimpleAdapter extends SimpleAdapter {

	private static String TAG = "MySimpleAdapter";
	
	private String deviceId;
	
	private Context context;
	
	List<Map<String, Object>> _data = new ArrayList<Map<String, Object>>(16);
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		final ImageView image = (ImageView) v.findViewById(R.id.ItemImage);
		View refresh = v.findViewById(R.id.ItemRefresh);
		final String channel_s = ((TextView) v.findViewById(R.id.ItemText)).getText().toString();
		
		for (int i = 0; i < _data.size(); i++) {
			if(i == position && _data.get(i).get("logo") != null){
				String tempUrl = _data.get(i).get("logo").toString();
				if(tempUrl.startsWith("http")){
	    			String name = tempUrl.substring(tempUrl.indexOf("aliyuncs.com")+12, tempUrl.length());
	    			//System.out.println("..."+i+" == "+_data.get(i).get("logo"));
					File file = new File(DevAdapter.rootPath + deviceId + name);
					Bitmap bitmap=null;
					if(file.exists()){
						bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
						image.setImageBitmap(bitmap);
					}else{
						byte[] bytes = HttpUtil.executeGetBytes(tempUrl);
	    				//存入SDK文件
	    				FileUtil.toFile(bytes, DevAdapter.rootPath + deviceId + name);
	    				bitmap = BitmapUtils.getBitMap(file.getAbsolutePath());
	    				image.setImageBitmap(bitmap);
					}
				}
				
			}
		}
		
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//APP.ShowToast("refresh..." + deviceId);
				//image.setBackgroundColor(context.getResources().getColor(R.color.blue_menu));
				//image.setVisibility(View.GONE);
				String json = SDK.getJson(deviceId, "".equals(channel_s)?0:Integer.parseInt(channel_s)-1);
				Log.d(TAG, "JSON:"+json);
				SDK.SendJsonPck(0, json);
				ScreenCache.getInstance().addImgView(deviceId, (ImageView)image);
				
				//notifyDataSetChanged();
			}
		});
		return v;
	}
	
	@SuppressWarnings("unchecked")
	public MySimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to, String deviceId) {
		super(context, data, resource, from, to);
		this.deviceId = deviceId;
		this.context = context;
		this._data = (List<Map<String, Object>>) data;
	}

}
