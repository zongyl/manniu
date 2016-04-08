package com.adapter;

import java.util.List;
import java.util.Map;

import P2P.SDK;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.manniu.manniu.R;
import com.utils.ScreenCache;

/**
 * Video Back 报警回放
 * @author pc
 *
 */
public class VideoPlaybackSimpleAdapter extends SimpleAdapter {

	private static String TAG = "VideoPlaybackSimpleAdapter";
	
	private String deviceId;
	
	private Context context;
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		final View image = v.findViewById(R.id.ItemImage);
		View refresh = v.findViewById(R.id.ItemRefresh);
		final String channel_s = ((TextView) v.findViewById(R.id.ItemText)).getText().toString();
		
		/*refresh.setOnClickListener(new OnClickListener() {
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
		});*/
		return v;
	}
	
	public VideoPlaybackSimpleAdapter(Context context,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
	}

}
