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
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.adapter.LineAdapter;
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
	private Canvas canvas;
	private int[] sizes;
	private ListView hroLine;

	private LineAdapter lineAdapter;
	
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
		
		sizes = getSize();
		
		LogUtil.d(TAG, "thread name:"+Thread.currentThread().getName());
		
		grid = (GridView) findViewById(R.id.video_play_back_grid);
		items = new ArrayList<Map<String, Object>>();
		
		sf = (SurfaceView)findViewById(R.id.Line);
		LayoutParams params = new LayoutParams(sizes[0] * 2, 20);
		//sf.setLayoutParams(params);
		holder = sf.getHolder();
		holder.addCallback(new DoThings());
		sf.setOnTouchListener(touch);
		
		hroLine = (ListView)findViewById(R.id.horLine);
		
		Map<String, Object> maps;
		maps = new HashMap<String, Object>();
		maps.put("tag", "");
		maps.put("image", R.drawable.ic_desktop);
		maps.put("text", "text");
		items.add(maps);
		
		lineAdapter = new LineAdapter(context, items, R.layout.line_item,
				new String[]{"tag", "image", "text"},
				new int[]{R.id.tag, R.id.ItemImage, R.id.ItemText});
		hroLine.setAdapter(adapter);
	}
	
	private class DoThings implements SurfaceHolder.Callback{

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			/*new Thread(new Runnable() {
				@Override
				public void run() {
					canvas = holder.lockCanvas();
					Paint paint = new Paint();
					paint.setColor(getResources().getColor(R.color.red));
					canvas.drawLine(1, 130, 1500, 130, paint);
					canvas.drawLine(50, 100, 50, 130, paint);
					canvas.drawLine(60, 100, 60, 130, paint);
					canvas.drawLine(70, 100, 70, 130, paint);
					canvas.drawLine(80, 100, 80, 130, paint);
					canvas.drawLine(90, 100, 90, 130, paint);
					canvas.drawLine(500, 100, 500, 130, paint);
					canvas.drawLine(1000, 100, 1000, 130, paint);
					//canvas.drawText("hello, world!", 1, 50, paint);
					holder.unlockCanvasAndPost(canvas);
				}
			}).start();*/
			new Thread(init).start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			
		}
	}
	
	private int[] getSize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int[] pixels = new int[2];
		pixels[0] = dm.widthPixels;
		pixels[1] = dm.heightPixels;
		return pixels;
	}
	
	private Runnable init = new Runnable() {
		@Override
		public void run() {
			drawLine();
		}
	};
	
	OnTouchListener touch = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				LogUtil.d(TAG, "down:" + event.getX());
				//drawPoint(event.getX(), event.getY());
				drawLine(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				//drawPoint(event.getX(), event.getY());
				drawLine(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				LogUtil.d(TAG, "up:" + event.getX());
				break;
			default:
				break;
			}
			return true;
		}
	};
	
	private void drawLine(){
		canvas = holder.lockCanvas(null);
		if(canvas!=null){
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.red));
			canvas.drawColor(R.color.black);
			//步长  、 
			int step = 20;
			canvas.drawLine(1, 130, 1500, 130, paint);
			for(int i=0;i<100;i++){
				if(i % 5 == 0){
					canvas.drawLine(i*step, 80, i*step, 130, paint);
				}else{
					canvas.drawLine(i*step, 100, i*step, 130, paint);
				}
			}
			
			holder.unlockCanvasAndPost(canvas);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void drawLine(float x, float y){
		canvas = holder.lockCanvas(null);
		if(canvas!=null){
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.red));
			canvas.drawColor(R.color.black);
			canvas.drawLine(x, 0, x, 180, paint);
			holder.unlockCanvasAndPost(canvas);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void drawPoint(float x, float y){
		canvas = holder.lockCanvas(null);
		if(canvas!=null){
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.red));
			canvas.drawColor(R.color.black);
			canvas.drawText(x+","+y, x, y, paint);
			holder.unlockCanvasAndPost(canvas);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
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
