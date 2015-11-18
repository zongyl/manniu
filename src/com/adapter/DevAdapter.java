package com.adapter;

import java.io.File;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import P2P.SDK;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.basic.APP;
import com.bean.Device;
import com.bean.DeviceParcel;
import com.bean.LiveVideo;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.FileUtil;
import com.utils.LogUtil;
import com.utils.ScreenCache;
import com.views.DeviceOnlineShare;
import com.views.Main;
import com.views.NewDeviceSet;
import com.views.NewLogin;

/**
 * 设备列表
 * @author zongyl
 *
 */
public class DevAdapter extends BaseAdapter{

	public static String TAG = "DevAdapter";
	
	private Context context;
	//设备列表
	private List<?> items;
	
	LayoutInflater inflater;
	//图片缓存目录 
	private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/manniu/devices/";
	//缓存文件
	private File file;
	//设备ID 当缓存文件文件名
	private String sid, url, prompt_text;
	
	RequestQueue requestQueue;
	
	View promptView;
	
	//String url;

	Dialog dlg;
	
	int maxMemory = (int) Runtime.getRuntime().maxMemory();
	
	final LruCache<String, Bitmap> lrnCache = new LruCache<String, Bitmap>(maxMemory/8);
	
	ImageCache imageCache;
	
	public static class ViewHolder{
		public TextView tv;
		public ImageView iv;
		public Button more_btn;
		public Button msg_btn;
		public TextView status;
		public ImageView type_ic;
		public ImageView status_ic;
	}
	
	public DevAdapter(Context _context, List<?> _items){
		this.context = _context;
		this.items = _items;
		this.requestQueue = Volley.newRequestQueue(context);
		this.imageCache = new ImageCache(){
			@Override
			public Bitmap getBitmap(String url) {
				return lrnCache.get(url);
			}
			@Override
			public void putBitmap(String url, Bitmap bitmap) {
				lrnCache.put(url, bitmap);
			}};
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}                                                                                              

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final ViewHolder holder;
		if(convertView == null){
			
			final Device device = (Device)items.get(position);
			holder = new ViewHolder();
		//	if(device.channelNo == null){//单通道
				convertView = inflater.inflate(R.layout.new_main_item, null);
				holder.tv = (TextView)convertView.findViewById(R.id.device_txt);
				holder.iv = (ImageView)convertView.findViewById(R.id.device_img);
				//holder.msg_btn = (Button)convertView.findViewById(R.id.device_msg);
				holder.more_btn = (Button)convertView.findViewById(R.id.device_btn);
				//holder.status = (TextView) convertView.findViewById(R.id.device_status);
				holder.status_ic = (ImageView)convertView.findViewById(R.id.device_status_ic);
				holder.type_ic = (ImageView)convertView.findViewById(R.id.device_type_ic);
				convertView.setTag(holder);
//			}else{
//				convertView = inflater.inflate(R.layout.new_main_grid_item, null);
//			}
			
			holder.tv.setText(device.devname + (device.channelNo==null?"":" 通道:"+(device.channelNo+1)));
			
			switch (device.type) {
			case 1:
				holder.type_ic.setImageDrawable(context.getResources().getDrawable(R.drawable.ipc));
				break;
			case 4:
				holder.type_ic.setImageDrawable(context.getResources().getDrawable(R.drawable.common_bar_eye_2));
				break;
			case 100:
				holder.type_ic.setImageDrawable(context.getResources().getDrawable(R.drawable.collection));
				break;
			default:
				break;
			}
			
			if(device.online==0){
				holder.status_ic.setImageDrawable(context.getResources().getDrawable(R.drawable.ipc_noline1));
			}else{
				holder.status_ic.setImageDrawable(context.getResources().getDrawable(R.drawable.ipc_online1));
			}
			
			if(device.type == 100){
				holder.status_ic.setVisibility(View.GONE);
			}else{
				holder.status_ic.setVisibility(View.VISIBLE);
			}
			
			holder.more_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(device.type == 1 && device.isowner == 1){
						dlg = Sheet(device, holder.iv);
					}else if(device.type == 1 && device.isowner == 0){
						dlg = DelSheet(device, holder.iv);
					}else if(device.type == 4){
						dlg = AnalogSheet(device, holder.iv);
					}else if(device.type == 100){
						dlg = CollectSheet(device, holder.iv);
					}else{
						
					}
				}
			});
			
			RequestParams params = new RequestParams();
			params.put("sid", device.sid);//"Q04hAQEAbDAwMjk0MTYwAAAAAAAA"
			try {
				HttpUtil.get(Constants.hostUrl + "/mobile/getScreen", params, new JsonHttpResponseHandler(){
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						//Log.d(TAG, response.toString());
						String result = "";
						try {
							result = response.getString("result");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						if(result.startsWith("http")){
							Log.d(TAG, device.devname + "的封面:" + result);
							String name = result.substring(result.indexOf("aliyuncs.com")+12, result.length());
							
							file = new File(rootPath+device.sid+name);
							if(file.exists()){
								Log.d(TAG, device.devname + "的封面本地已存在!");
								holder.iv.setImageBitmap(getBitMap(file.getAbsolutePath()));
							}else{
								Log.d(TAG, device.devname + "的封面本地不存在,正在下载...");
								byte[] bytes = HttpUtil.executeGetBytes(result);
								FileUtil.toFile(bytes, rootPath+device.sid+name);
								holder.iv.setImageBitmap(getBitMap(file.getAbsolutePath()));
							}
							
							//volley image load
							/*ImageLoader imageLoader = new ImageLoader(requestQueue, imageCache);
							ImageListener listener = ImageLoader.getImageListener(holder.iv, R.drawable.lock_bg1, R.drawable.ic_launcher);
							imageLoader.get(result, listener);*/
							
							/*DisplayImageOptions options = new DisplayImageOptions.Builder()
							.cacheOnDisk(true)
							.showImageOnLoading(context.getResources().getDrawable(R.drawable.lock_bg1))
							.build();
							ImageLoader.getInstance().displayImage(result, holder.iv, options);*/
						}
					}
					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						super.onFailure(statusCode, headers, responseString, throwable);
					}
				});
				holder.iv.setImageDrawable(context.getResources().getDrawable(R.drawable.lock_bg1));
			} catch (Exception e) {
			}
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
	
	/**
	 * IPC弹出dialog
	 * @param device
	 * @param imv
	 * @return
	 */
	private Dialog Sheet(final Device device, final ImageView imv){
		return ActionSheet.showSheet(context, device, new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				TextView tag = (TextView) view.findViewById(R.id.tag);
				ImageView img = (ImageView) view.findViewById(R.id.ItemImage);
				TextView text= (TextView)view.findViewById(R.id.ItemText);
				if("dialog1".equals(tag.getText())){
					APP.ShowConfirmDialog(context.getString(R.string.tip_title), context.getString(R.string.delete_ask), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							delDevice(device.sid, device.userid);
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							dlg.dismiss();
						}
					},null,null,context);
					
				}else if("dialog2".equals(tag.getText())){
					dlg.dismiss();
					Intent intent = new Intent(context, NewDeviceSet.class);
					DeviceParcel deviceParcel = new DeviceParcel(device);
					intent.putExtra("device", deviceParcel);
					context.startActivity(intent);
				}else if("dialog3".equals(tag.getText())){
					text.setText(context.getString(R.string.dev_coverfresh));
					String json = SDK.getJson(device.sid);
					Log.d(TAG, "JSON:"+json);
					SDK.SendJsonPck(0, json);
					ScreenCache.getInstance().addImgView(device.sid, imv);
					dlg.dismiss();
				}else if("dialog4".equals(tag.getText())){
					dlg.dismiss();

					LogUtil.d(TAG, "text:" + text.getText());
					LogUtil.d(TAG, "text:" + context.getString(R.string.share));
					LogUtil.d(TAG, "text:" + context.getString(R.string.cancelcollect));
					
					if(context.getString(R.string.share).equals(text.getText())){
						RequestParams params = new RequestParams();
						params.put("userId", device.userid);
						params.put("deviceId", device.sid);
						params.put("sessionId", Constants.sessionId);
						HttpUtil.get(Constants.hostUrl+"/android/isUserS", params, new JsonHttpResponseHandler() {
							public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
								if(statusCode == 200 ){
									try{
										String result = json.getString("msg");
										if("nologin".equals(result)){
											LogUtil.d(TAG, "查询用户认证信息超时");
											//BaseApplication.getInstance().relogin();
										}else{
											Intent intent = new Intent(context, DeviceOnlineShare.class);
											Bundle b = new Bundle();
											b.putStringArray("sharemsg", new String[]{device.sid,result});
											String tempStr = json.getString("data").toString();
											if(tempStr.length()>0){
												com.alibaba.fastjson.JSONObject job = (com.alibaba.fastjson.JSONObject) (JSON.parse(json.getString("data")));
												LiveVideo liveVideo = JSON.toJavaObject(job, LiveVideo.class);
												b.putParcelable("shareinfo", liveVideo);
											}
											intent.putExtras(b);
											context.startActivity(intent);
										}
									}catch(JSONException e){
										e.printStackTrace();
									}
								}
							}
							@Override
							public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
								if(errorResponse!=null){
									Log.v(TAG, "error1:"+errorResponse.toString());
								}
								APP.ShowToast(context.getString(R.string.E_SER_FAIL));
							}
							/*public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
								if(responseString!=null){
									Log.v(TAG, "error2:"+responseString);
								}
								APP.ShowToast("请稍后再试!");
							};*/
						});
						
					}else if(context.getString(R.string.cel_share).equals(text.getText())){

						APP.ShowConfirmDialog(context.getString(R.string.tip_title), context.getString(R.string.cancelShare_ask), 
								new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								RequestParams params = new RequestParams();
								params.put("deviceId", device.sid);
								HttpUtil.get(Constants.hostUrl+"/device/cancelShare", params, new JsonHttpResponseHandler() {
									public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
										if(statusCode == 200 ){
											try{
												if(json.getBoolean("result")){
													APP.ShowToast(context.getString(R.string.SUCCESS_CANCEL));
												}else{
													APP.ShowToast(context.getString(R.string.FAIL_CANCEL));
												}
											}catch(JSONException e){
												e.printStackTrace();
											}
										}
									}
									@Override
									public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
										if(errorResponse!=null){
											Log.v(TAG, "error1:"+errorResponse.toString());
										}
										APP.ShowToast(context.getString(R.string.E_SER_FAIL));
									}
								});
							}
						}, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								dlg.dismiss();
							}
						},null,null,context);
						
					}
				}else if("dialog5".equals(tag.getText())){
					dlg.dismiss();
					ShowPromptDialog("添加辅用户", "请输入用户名", 
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									final EditText userInput = (EditText) promptView.findViewById(R.id.editTextDialog);
									prompt_text = userInput.getText().toString();
									if("".equals(prompt_text.trim())||prompt_text==null){
										APP.ShowToast("用户名不能为空!");
									}else{
										appointUser(device, prompt_text);
									}
								}
					}, 
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}}, null, null, context);
				}
			}
		
		}, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		});
	}
	
	/**
	 * 模拟弹出dialog
	 * @param device
	 * @param imv
	 * @return
	 */
	private Dialog AnalogSheet(final Device device, final ImageView imv){// holder.iv
		return AnalogSheet.showSheet(context, device, new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				TextView tag = (TextView) view.findViewById(R.id.tag);
				ImageView img = (ImageView) view.findViewById(R.id.ItemImage);
				TextView text= (TextView)view.findViewById(R.id.ItemText);
				if("dialog1".equals(tag.getText())){
					APP.ShowConfirmDialog(context.getString(R.string.tip_title), context.getString(R.string.delete_ask), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							delDevice(device.sid, device.userid);
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							dlg.dismiss();
						}
					},null,null,context);
					
				}/*else if("dialog2".equals(tag.getText())){
					dlg.dismiss();
					Intent intent = new Intent(context, NewDeviceSet.class);
					DeviceParcel deviceParcel = new DeviceParcel(device);
					intent.putExtra("device", deviceParcel);
					context.startActivity(intent);
				}*/else if("dialog3".equals(tag.getText())){
					text.setText(context.getString(R.string.dev_coverfresh));
					String json = SDK.getJson(device.sid);
					Log.d(TAG, "JSON:"+json);
					SDK.SendJsonPck(0, json);
					ScreenCache.getInstance().addImgView(device.sid, imv);
					dlg.dismiss();
				}
			}
		
		}, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		});
	}

	/**
	 * 收藏设备弹出dialog
	 * @param device
	 * @param imv
	 * @return
	 */
	private Dialog CollectSheet(final Device device, final ImageView imv){
		return CollectSheet.showSheet(context, device, new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tag = (TextView) view.findViewById(R.id.tag);
				ImageView img = (ImageView) view.findViewById(R.id.ItemImage);
				TextView text= (TextView)view.findViewById(R.id.ItemText);
				if("dialog1".equals(tag.getText())){
					APP.ShowConfirmDialog(context.getString(R.string.tip_title), context.getString(R.string.cancelColle_ask), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancelCollect(APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""), device.sid);
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							dlg.dismiss();
						}
					},null,null,context);
				}
			}
		
		}, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		});
	}
	
	/**
	 * 辅用户 弹出dialog
	 * @param device
	 * @param imv
	 * @return
	 */
	private Dialog DelSheet(final Device device, final ImageView imv){
		return CollectSheet.showSheet(context, device, new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tag = (TextView) view.findViewById(R.id.tag);
				ImageView img = (ImageView) view.findViewById(R.id.ItemImage);
				TextView text= (TextView)view.findViewById(R.id.ItemText);
				if("dialog1".equals(tag.getText())){
					APP.ShowConfirmDialog(context.getString(R.string.tip_title), context.getString(R.string.delDev_ask), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							delDev(APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""), device.sid);
						}
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							dlg.dismiss();
						}
					},null,null,context);
				}
			}
		
		}, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				
			}
		});
	}
	
	/**
	 * 辅用户删除设备
	 * @param userId 辅用户ID 
	 * @param devId 设备ID
	 */
	private void delDev(String userId, String devId){
		RequestParams params =  new RequestParams();
		params.put("userId", userId);
		params.put("sid", devId);
		HttpUtil.get(Constants.hostUrl + "/android/appointUserDelDevice", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				if(response.has("msg")){
					
					String msg = "";
					try {
						msg = response.getString("msg");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if("success".equals(msg)){
						dlg.dismiss();
						Main.Instance.NewMainreLoad();
					}else if("failure".equals(msg)){
						dlg.dismiss();
						Main.Instance.NewMainreLoad();
					}
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}
	
	/**
	 * 取消收藏
	 */
	private void cancelCollect(String userId, String liveId){
		RequestParams params = new RequestParams();
		params.put("userId", userId);
		params.put("liveid", liveId);
		Log.d(TAG, "cancelCollect params:" + params.toString());
		HttpUtil.get(context.getResources().getString(R.string.server_address)+"/android/cancelCollect", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				dlg.dismiss();
				Main.Instance.NewMainreLoad();
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}
	
	/**
	 * 添加辅用户
	 * appointUser?username="指定用户的名称或者电话"&userId="主用户ID"&sid=“设备ID”
	 * msg : nodev--没有该设备
			msg : failure--添加失败
			msg : maximum--超过最大绑定数
			msg : success--成功

	 * 
	 */
	private void appointUser(Device device, String username){
		RequestParams params = new RequestParams();
		params.put("username", username);
		params.put("userId", APP.GetSharedPreferences(NewLogin.SAVEFILE, "sid", ""));
		params.put("sid", device.sid);
		Log.d(TAG, "appointUser params:" + params.toString());
		HttpUtil.get(Constants.hostUrl+"/android/appointUser", params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				String msg = "";
				if(response.has("msg")){
					try {
						msg = response.getString("msg");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if("nodev".equals(msg)){
						APP.ShowToast("没有该设备!");
					}else if("failure".equals(msg)){
						APP.ShowToast("添加失败!");
					}else if("maximum".equals(msg)){
						APP.ShowToast("超过最大绑定数!");
					}else if("success".equals(msg)){
						APP.ShowToast("添加成功!");
					}else if("norit".equals(msg)){
						APP.ShowToast("没有权限!");
					}else if("nouser".equals(msg)){
						APP.ShowToast("没有找到该用户!");
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
			}
		});
	}
	
	/**
	 * 删除设备
	 */
	private void delDevice(String sid, String userId){
		RequestParams params = new RequestParams();
		 params.put("sid", sid);
		 params.put("userId", userId);
		 Log.d(TAG, "delDevice#params:" + params.toString());
		 HttpUtil.get(context.getResources().getString(R.string.server_address)+"/android/delDevice", params, new JsonHttpResponseHandler(){
			 @Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				 Log.d(TAG, "response:"+response);
				 if(statusCode == 200){
					 try {
						if(response.getInt("msg") == 0){
							dlg.dismiss();
							Main.Instance.NewMainreLoad();
						 }else{
							 //TODO 测试分支
							dlg.dismiss();
						 }
					} catch (JSONException e) {
						LogUtil.e("delete device", e.getMessage());
					}
				 }
			}
			 @Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				APP.ShowToast(context.getString(R.string.Err_CONNET));
			}
			 
			 @Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				Log.d(TAG, "delDevice#onFailure:" + responseString);
			}
		 });
	}
	
	/**
	 *以最省内存的方式读取本地资源的图片
	 * @return
	 */
	private Bitmap getBitMap(String path){
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		return BitmapFactory.decodeFile(path, opt);
	}
	
	/*class BmpHandler extends Handler{
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			byte[] bytes = msg.getData().getByteArray("bytes");
			Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			iv.setImageBitmap(bmp);
		}
	}*/
	
	Runnable saveScreenRunnable = new Runnable(){
		@Override
		public void run() {
			Log.d(TAG, "save screen thread:" + url);
			byte[] bytes = HttpUtil.executeGetBytes(url);
			FileUtil.toFile(bytes, rootPath+"sid_"+sid+".png");
		}
	};
	
	private void saveScreen(String sid, String url, ImageView iv){
		Log.d(TAG, "save screen thread:" + url + "\nsid:" + sid);
		
		String name = url.substring(url.indexOf("aliyuncs.com")+12, url.indexOf("jpg?")+3);
		Log.d(TAG, "file name:" + name);
		
		byte[] bytes = HttpUtil.executeGetBytes(url);
		if(new File(rootPath+sid+name).exists()){
			iv.setImageBitmap(getBitMap(file.getAbsolutePath()));
		}else{
			FileUtil.toFile(bytes, rootPath+sid+name);
		}
		
	}
	
	private Bitmap getBitMap(){
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		int heightRatio = (int)Math.ceil(opts.outHeight/(float)7);
		int widthRatio = (int)Math.ceil(opts.outWidth/(float)10);
		
		if(widthRatio > 1 && heightRatio > 1){
			if(heightRatio > widthRatio){
				opts.inSampleSize = heightRatio;
			}else{
				opts.inSampleSize = widthRatio;
			}
		}
		opts.inJustDecodeBounds =false;
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.sc, opts);
	}
	
	private void ShowPromptDialog(String strTitle, String strMsg, DialogInterface.OnClickListener yes, DialogInterface.OnClickListener no, String yesText, String noText,Context context) {
		LayoutInflater li = LayoutInflater.from(context);
		promptView = li.inflate(R.layout.prompts, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(promptView);
		TextView text = (TextView) promptView.findViewById(R.id.textPrompt);
		text.setText(strMsg);
		builder.setTitle(strTitle);
		builder.setPositiveButton(yesText==null?context.getString(R.string.confirm):yesText, yes);
		builder.setNegativeButton(noText==null?context.getString(R.string.cancel):noText, no);
		builder.create().show();
	}
	
}