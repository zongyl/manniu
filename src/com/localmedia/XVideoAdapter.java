package com.localmedia;

import java.io.File;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.basic.APP;
import com.basic.G;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.FileUtil;
import com.utils.HttpURLConnectionTools;
import com.utils.LogUtil;
import com.utils.SortUtils;
/**
 * 录像适配器
 * @author jianhua
 *
 */
@SuppressLint("DefaultLocale")
public class XVideoAdapter extends BaseAdapter {
    private Activity activity;
//    public static ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String,String>>();
    private LayoutInflater inflater=null;
    public ImageLoader imageLoader; //用来下载图片的类，后面有介绍
    //IX_MediaList _user;
    // 设备信息
    private final String KEY_ID = "id";
    private final String KEY_TITLE = "title";
    private final String KEY_DATE = "date"; //日期
    private final String KEY_LENGTH = "length";
    private final String KEY_THUMB_URL = "thumb_url";
    public static boolean _isOpen = true;//是否已经打开视频
    private MyHandler _handler = new MyHandler();
    private final int SHARE_SEND_MSG = 100;
    
    public XVideoAdapter(Activity a) {
        activity = a;
        //_user = user;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(imageLoader == null)
        	imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    public void setAlarmListToShow() {
		this.notifyDataSetInvalidated();
	}
    
	public int UpdateList(String path, String type,String query,boolean bQuery) {
		Constants.data.clear();
		File baseFile = new File(path);
		if (baseFile != null && baseFile.exists()) {
			File[] file = baseFile.listFiles();
			SortUtils.sort(file);
			if(file != null){
//				for(int i = file.length - 1; i > -1; --i){
				for(int i = 0; i < file.length; i++){
					HashMap<String, String> map = new HashMap<String, String>();
					if (file[i].isFile() && file[i].getPath().endsWith(type)) {
						if (bQuery) {
							String name = file[i].getName();
							int last = name.lastIndexOf(".");
							String tem = name.substring(0, last).substring(14);
							if(!tem.contains(query)){
								continue;
							}
						} 
						map.put(KEY_ID, R.id.img_play+"");
						map.put(KEY_LENGTH, formetFileSize(file[i].length()));
						setFileName(file[i].getPath(),map);
						map.put(KEY_THUMB_URL, file[i].getPath());
						Constants.data.add(map);
					}
				}
			}
		}
		this.notifyDataSetChanged();
		return Constants.data.size();
	}
	public String formetFileSize(long fileS) {//转换文件大小
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }
	
	public void setFileName(String path,HashMap<String, String> map){
		String name = "";
		if(path == null || path == ""){
			return;
		}
		int pathInex = path.lastIndexOf('/');
		if(pathInex == -1){
			return;
		}
		name = path.substring(pathInex + 1);
		int nameLen = name.length();
		Date dt = ToDate(name.substring(0, 14));
		String strT = String.format("%04d-%02d-%02d %02d:%02d:%02d", dt.getYear() + 1900, dt.getMonth() + 1, 
				dt.getDate(), dt.getHours(), dt.getMinutes(), dt.getSeconds());
		map.put(KEY_TITLE, name.substring(14, nameLen - 4));
		map.put(KEY_DATE, strT);
	}
	
	// 字符串转日期
	@SuppressLint("SimpleDateFormat")
	public static Date ToDate(String date) {
		try {
			SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHHmmss");
			return formatDate.parse(date);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public boolean deleteSeletect(String path) {
		if (path == "") {
			return false;
		}
		File file = new File(path);
		String bmeFile = "";
		try {
			if (file.isFile() && file.exists()) {
				if (path.endsWith(".mp4")) {
					bmeFile = path.substring(0, path.length() - 3) + "bmp";
					File file2 = new File(bmeFile);
					if (file2.isFile() && file2.exists()) {
						file2.delete();
					}
				}
				if (file.delete()) {
					this.notifyDataSetChanged();
					return true;
				}
			}
		} catch (Exception e) {
			LogUtil.e("XVideoAdapter", ExceptionsOperator.getExceptionInfo(e));
		}
		return false;
	}
    
    /*public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_media_row, null);
        TextView title = (TextView)vi.findViewById(R.id.title); // 设备名称
        TextView artist = (TextView)vi.findViewById(R.id.img_time); //保存时间
        TextView length = (TextView)vi.findViewById(R.id.img_length); // 时长
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // 缩略图
        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);
        // 设置ListView的相关值
        title.setText(song.get(Tab_VideoActivity.KEY_TITLE));
        artist.setText(song.get(Tab_VideoActivity.KEY_DATE));
        length.setText(song.get(Tab_VideoActivity.KEY_LENGTH));
        String url = song.get(Tab_VideoActivity.KEY_THUMB_URL);
        vi.setTag(url);
        if (!(url.endsWith(".bmp") || url.endsWith(".jpg"))) {
        	url = url.substring(0, url.length() - 3) + "bmp";
        	imageLoader.DisplayImage(url, thumb_image);
		}
        return vi;
    }*/
    
    public int getCount() {
        return Constants.data.size();
    }

    public Object getItem(int position) {
        //return position;
        return Constants.data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //ListItem holder = null;
    
    public class VideoHolder {
	     public TextView title;
	     public TextView artist;
	     public TextView length;
	     public ImageView thumb_image;
	     public ImageView play_image;
	     public ImageView play_upload;//上传
	}
    
	public View getView(final int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		VideoHolder vh;
		if (vi == null) {
			vh = new VideoHolder();
			//录像列表适配器 
			vi = inflater.inflate(R.layout.list_media_row, null);
			vh.title = (TextView)vi.findViewById(R.id.title); // 设备名称
			vh.artist = (TextView)vi.findViewById(R.id.img_time); //保存时间
			vh.length = (TextView)vi.findViewById(R.id.img_length); // 时长
			vh.thumb_image=(ImageView)vi.findViewById(R.id.list_image); // 缩略图
			vh.play_image = (ImageView)vi.findViewById(R.id.img_play); // 播放
			vh.play_upload = (ImageView)vi.findViewById(R.id.img_upload); // 上传
			vi.setTag(vh);
			//holder = new ListItem(vi, position);
		}else{
			vh = (VideoHolder) vi.getTag();
		}
		HashMap<String, String> song = Constants.data.get(position);
		vh.title.setText(song.get(KEY_TITLE));
        vh.artist.setText(song.get(KEY_DATE));
        vh.length.setText(song.get(KEY_LENGTH));
        String url = song.get(KEY_THUMB_URL);
        if (!(url.endsWith(".bmp") || url.endsWith(".jpg"))) {
        	url = url.substring(0, url.length() - 3) + "bmp";
        	imageLoader.DisplayImage(url, vh.thumb_image);
		}
        
        //视频分享
        vh.play_upload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					final String path = Constants.data.get(position).get(KEY_THUMB_URL);
					File file = new File(path);
					if(file.exists()){
						long fsize = file.length();
						if(fsize < 102400){
							APP.ShowToast(activity.getString(R.string.share_error));
							return;
						}
					}	
					final EditText editText = new EditText(activity);
					editText.setText(Constants.data.get(position).get(KEY_TITLE));
					editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
					editText.setFocusable(true);
					new AlertDialog.Builder(activity).setTitle(APP.GetString(R.string.tip_title)).setMessage(APP.GetString(R.string.video_share)).setIcon(R.drawable.help)
					.setPositiveButton(APP.GetString(R.string.confirm), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							final String title = editText.getText().toString();
							if(!title.trim().equals("")){
								APP.showProgressDialog(activity, APP.GetString(R.string.uploading));
								new Thread(new Runnable() {
									@Override
									public void run() {//上传视频
										byte[] tem = SDK.StartupLoadLocalMedia();
										int ret = 0;
										String str = "";
										if(tem != null){
											ret = FileUtil.readFile(path);//调用上传方法
											str  = ret+","+path.replace(".mp4", ".bmp")+","+new String(tem)+","+title;
										}else{
											ret = -1;
											str  = ret+","+path.replace(".mp4", ".bmp")+","+1+","+title;
										}
										Message msg = new Message();
										msg.what = SHARE_SEND_MSG;
										msg.obj = str;
										_handler.sendMessage(msg);
									}
								}).start();
							}else{
								APP.ShowToastLong(APP.GetString(R.string.title_tip));
							}
						}
					}).setView(editText).setNegativeButton(APP.GetString(R.string.cancel),null).show();
					
				} catch (Exception e) {
					return;
				}
			}
		});
        
        //播放
        vh.play_image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
//					if(!Main.reconnect){
//						APP.ShowToastLong("服务器断开连接！");
//						return;
//					}
					String path = Constants.data.get(position).get(KEY_THUMB_URL);
					File file = new File(path);
					if(file.exists()){
						long fsize = file.length();
						if(fsize < 102400){
							APP.ShowToast(activity.getString(R.string.smalfile_tip));
							return;
						}
						if(path.length() > 4 && _isOpen){
							_isOpen = false;
							Intent intent = new Intent(activity.getApplicationContext(), Fun_RecordplayActivity_MediaPlayer.class);
							intent.putExtra("fileName", path);
							activity.startActivity(intent);
						}else{
							_isOpen = true;
							APP.ShowToast(activity.getString(R.string.openVideo_error));
						}
					}else{
						APP.ShowToast(activity.getString(R.string.nofile));
					}
				} catch (Exception e) {
					_isOpen = true;
					APP.ShowToast(activity.getString(R.string.video_failopen));
					LogUtil.d("XVideoAdapter", ExceptionsOperator.getExceptionInfo(e));
					return;
				}
			}
		});
		return vi;
	}
	
	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SHARE_SEND_MSG:
				APP.dismissProgressDialog();
				String retStr = (String) msg.obj;
				if(Integer.parseInt(retStr.split(",")[0]) == 0){
					APP.ShowToastLong(APP.GetString(R.string.share_completed));
					//发送到服务器
					uploadVideo(retStr);
				}else{
					APP.ShowToastLong(APP.GetString(R.string.share_failed));
				}
				break;
			}
		}
	}
	//发送方法
	public void uploadVideo(String param) {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("imgUrl", param.split(",")[1]);
			params.put("videoUrl", param.split(",")[2]);
			params.put("videoName", param.split(",")[3]);//URLEncoder.encode(param.split(",")[3],"utf-8")
			params.put("videoInfo", param.split(",")[3]);
			params.put("userId", Constants.userid);
			Map<String, Object> map = HttpURLConnectionTools.post(Constants.hostUrl + "/android/uploadVideo",params);
			if (Integer.parseInt(map.get("code").toString()) != 200) {
				APP.ShowToastLong(APP.GetString(R.string.Err_Error_Unknow));
			}else{
				JSONObject json = new JSONObject(map.get("data").toString());
				String msg = "";
				//{"success":"true","mid":1022}
				msg = json.getString("success");
				if("true".equals(msg)){
					//上传封面
					byte[] temData = FileUtil.getBytes(param.split(",")[1]);
		        	byte[] imgByte = new byte[210]; 
		        	int nRet = SDK.uploadlocalsnapshot(imgByte, 0, temData, temData.length);
		        	String imgUrl = "";
		        	if(nRet > 0){
		        		imgUrl = G.BytesToStr(imgByte,0,nRet);
		        		HashMap<String, String> params2 = new HashMap<String, String>();
						params2.put("userId", Constants.userid);
						params2.put("mid", json.getString("mid"));
						params2.put("imgUrl", imgUrl);
						Map<String, Object> map2 = HttpURLConnectionTools.post(Constants.hostUrl + "/android/saveVideoCover",params2);
						if (Integer.parseInt(map2.get("code").toString()) != 200) {
							APP.ShowToastLong(APP.GetString(R.string.Err_Error_Unknow));
						}
		        	}
				}
			}
		} catch (Exception e) {
		}
		
	}
    
    /*class ListItem implements OnClickListener{
		int position = 0;
		ListItem(View convertView, int position){
			this.position = position;
			convertView.findViewById(R.id.img_play).setOnClickListener(this);
			convertView.setOnClickListener(this);
		}
		void SetPosition(View vi, int position){
			this.position = position;
			TextView title = (TextView)vi.findViewById(R.id.title); // 设备名称
	        TextView artist = (TextView)vi.findViewById(R.id.img_time); //保存时间
	        TextView length = (TextView)vi.findViewById(R.id.img_length); // 时长
	        //TextView play = (TextView)vi.findViewById(R.id.img_play); // 播放
	        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // 缩略图
	        HashMap<String, String> song = new HashMap<String, String>();
	        song = Constants.data.get(position);
	        // 设置ListView的相关值
	        title.setText(song.get(KEY_TITLE));
	        artist.setText(song.get(KEY_DATE));
	        length.setText(song.get(KEY_LENGTH));
	        String url = song.get(KEY_THUMB_URL);
	        vi.setTag(url);
	        if (!(url.endsWith(".bmp") || url.endsWith(".jpg"))) {
	        	url = url.substring(0, url.length() - 3) + "bmp";
	        	imageLoader.DisplayImage(url, thumb_image);
			}
//	        ImageView img = (ImageView) vi.findViewById(R.id.img_play);
//			Integer imageId = R.id.img_play;
//			if(imageId != null && imageId != 0){
//				img.setImageResource(imageId);
//			}
	        vi.findViewById(R.id.img_play).setOnClickListener(this);
		}

		public void onClick(View v) {
			XVideoAdapter.this._user.OnClickedItem(XVideoAdapter.this, v, position, 
					Constants.data.get(position).get(KEY_THUMB_URL));
		}
	}*/
    
    
     
}
