package com.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import P2P.SDK;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;

import com.adapter.VideoPlaybackSimpleAdapter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.manniu.manniu.R;
import com.utils.LogUtil;

/**
 * 录像回放
 * @author pc
 *
 */
public class VideoPlayback extends Activity {

	public static String TAG = "VideoPlayback";
	
	public VideoBackHandler handler;
	
	private Button query;
	
	private Context context;
	
	private BaseApplication baseApp = null;
	
	private GridView grid;
	
	private List<Map<String, Object>> items;
	
	private VideoPlaybackSimpleAdapter adapter;
	
	private static Bundle data;
	
	private SurfaceView sf;
	private SurfaceHolder holder;
	
	public final class VideoBackHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				data = msg.getData();
				LogUtil.d(TAG, data.getString("file_list"));
				
				JSONArray array = JSONArray.parseArray(data.getString("file_list")) ;
				Map<String, Object> maps;
				JSONObject obj = null;
				for(Iterator<Object> iter = array.listIterator(); iter.hasNext();){
					obj = (JSONObject)iter.next();
					maps = new HashMap<String, Object>();
					maps.put("tag", "");
					maps.put("image", R.drawable.ic_desktop);
					maps.put("text",obj.getString("start_time").substring(11));
					items.add(maps);
				}
				
				adapter = new VideoPlaybackSimpleAdapter(context, items, R.layout.gridview_item,
						new String[]{"tag", "image", "text"},
						new int[]{R.id.tag, R.id.ItemImage, R.id.ItemText});
				grid.setAdapter(adapter);
				
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_play_back);
		this.context = this;
		query = (Button) findViewById(R.id.playback_btn);
		query.setOnClickListener(clickListener);
		
		baseApp = (BaseApplication)getApplication();
		handler = new VideoBackHandler();
		
		LogUtil.d(TAG, "thread name:"+Thread.currentThread().getName());
		
		grid = (GridView) findViewById(R.id.video_play_back_grid);
		items = new ArrayList<Map<String, Object>>();
		
		sf = (SurfaceView)findViewById(R.id.Line);
		holder = sf.getHolder();
		holder.addCallback(new DoThings());
	}
	
	private class DoThings implements SurfaceHolder.Callback{

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(true){
						Canvas c = holder.lockCanvas();
						Paint paint = new Paint();
						paint.setColor(getResources().getColor(R.color.red));
						c.drawLine(1, 130, 1500, 130, paint);

						c.drawLine(50, 100, 50, 130, paint);
						c.drawLine(500, 100, 500, 130, paint);
						c.drawLine(1000, 100, 1000, 130, paint);
						holder.unlockCanvasAndPost(c);
					}
				}
			}).start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			
		}
	}
	
	OnTouchListener touch = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				break;
			case MotionEvent.ACTION_MOVE:
				
				break;
			default:
				break;
			}
			return false;
		}
	};
	
	OnClickListener clickListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.playback_btn:
				items.clear();
				//VFMhAQEAADAwMTk0MjBhAAAAAAAA
				//VFMhAQEAADAwMTRjZDk1AAAAAAAA 00201
				//VFMhAQEAAGUwNjFiMjE5NDIwYQAA
				String sid = "VFMhAQEAAGUwNjFiMjE5NDIwYQAA";
				Map<String, Object> maps = new HashMap<String, Object>();
				maps.put("type", 1);
				maps.put("action", 106);
				maps.put("channel", 1);
				maps.put("sid", sid);
				maps.put("start_time", "2016-3-7 00:00:00");
				maps.put("stop_time", "2016-3-7 23:59:59");
				maps.put("video_type", 0);
				
				String jsonString = new JSONObject(maps).toString();
				LogUtil.d(TAG, "jsonString:" + jsonString);
				int result = SDK.SendJsonPck(0, sid+"|"+jsonString);
				LogUtil.d(TAG, "return value:" + result);
				break;
			default:
				break;
			}
			
		}
	};
}
