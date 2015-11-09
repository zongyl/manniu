package com.views;
import java.io.File;
import java.io.FileNotFoundException;
import org.apache.http.Header;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adapter.HttpUtil;
import com.basic.APP;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.manniu.manniu.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.utils.BitmapUtils;
import com.utils.Constants;
import com.utils.Tools;
import com.utils.URITOOL;
import com.views.PhotoSheet.OnActionSheetSelected;
import com.views.XViewBasic.MyHandler;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewDetailEdit extends Activity implements OnClickListener,OnActionSheetSelected,OnCancelListener {
	private static final String TAG ="NewDetailEdit";
	private  String IMAGE_FILE_NAME = ".jpg";
	private static final int CHANGED = 0;
	private static final int CAMERA_REQUEST_CODE = 0;//拍照
	private static final int IMAGE_REQUEST_CODE = 1;//相册
	private static final int WEIXIN_REQUEST_CODE = 2;//微信
    private static final int RESULT_REQUEST_CODE = 3; 
    final static String SAVEFILE = "Info_Login";
    private MyHandler _mhandler = null;
    private BaseApplication _bApp  = null;
    CircleImageView _faceimage = null;
    TextView _userName = null;
    TextView _perSigner = null;
    TextView _phoneNum = null;
    TextView _email = null;
    TextView _password =null;
    String [] _tempArr =new String[3];
    String _img;
    String _UserId ="";
    Bitmap _bitmap = null;
    Uri _uri =null;
    String _imageFilepath="";
    InnerBroadcastReceiver _broadcast;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_detail_edit);
		loadViews();
		ReadUserInfo();
		RegistBroadcast();
		setListener();
	}
	
    public void setListener() {
    	findViewById(R.id.pers_back).setOnClickListener(this);
		findViewById(R.id.photo_upload).setOnClickListener(this);
		findViewById(R.id.faceimage).setOnClickListener(this);
		findViewById(R.id.hotN).setOnClickListener(this);
		//findViewById(R.id.pSign).setOnClickListener(this);
		//findViewById(R.id.phoN).setOnClickListener(this);
		findViewById(R.id.mailN).setOnClickListener(this);
		findViewById(R.id.pwdChg).setOnClickListener(this);
	}

	public void loadViews(){
    	_faceimage = (CircleImageView) findViewById(R.id.faceimage);
		_userName = (TextView)findViewById(R.id.hot_name) ;
		//_perSigner = (TextView)findViewById(R.id.per_signer) ;
		_phoneNum = (TextView)findViewById(R.id.pho_number) ;
		_email = (TextView)findViewById(R.id.e_mail) ;
		_bApp = (BaseApplication) getApplication();
		_mhandler = _bApp.getMyhandler();
    }
    
    /**注册广播*/
	private void RegistBroadcast() {
		IntentFilter filter = new IntentFilter(NewItemDetailEdit.action); 
		filter.setPriority(100);//优先级大于NewMoresMe,先接受消息更新UI
		_broadcast = new InnerBroadcastReceiver();
        registerReceiver(_broadcast, filter);
	}
	
	/**获取用户资料*/
	public void ReadUserInfo() {
		APP.GetMainActivity();
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, Context.MODE_PRIVATE);
		_userName.setText(preferences.getString("username", ""));
		//_perSigner.setText(preferences.getString("signer", ""));
		_phoneNum.setText(preferences.getString("phonenumber", ""));
		_email.setText(preferences.getString("email", ""));
		_UserId =preferences.getString("sid","");
		_img =preferences.getString("img", "");
		IMAGE_FILE_NAME = "temp"+IMAGE_FILE_NAME;
		BitmapUtils.loadImage(_img, _UserId, _faceimage);
	}

	@Override
	public void onClick(View view) {
		View clickView = (View) findViewById(view.getId());
		String t_clicked ="";
		if(clickView instanceof LinearLayout){
			t_clicked = new String((String) ((TextView) ((LinearLayout)findViewById(view.getId())).getChildAt(0)).getText());
			if(R.id.photo_upload == view.getId()){
				PhotoSheet.showSheet(this, this, this);
			}
			else if(R.id.pwdChg == view.getId()){
				forward("title",t_clicked,NewPwdDetailEdit.class);
			}else{
				String value = (String) ((TextView) ((LinearLayout)findViewById(view.getId())).getChildAt(1)).getText();
				this._tempArr[0] = t_clicked;
				this._tempArr[1] = value;
				this._tempArr[2] = _UserId;
				forward("title", _tempArr,NewItemDetailEdit.class);
			}
		}
		if(view.getId()==R.id.faceimage){
			PhotoSheet.showSheet(this, this, this);
		}
		if(R.id.pers_back == view.getId()){
			APP.GetMainActivity().ShowXView(Main.XV_NEW_MORE);
			this.finish();
		}
	}
	//转发到编辑页
	public void forward(String name,String[] value,Class<?> target){
		Bundle b=new Bundle();
		b.putStringArray(name, value);
		Intent intent=new Intent (this,target); 
		intent.putExtras(b);
		startActivity(intent);
	}
	
	public void forward(String name,String value,Class<?> target){
		Intent intent=new Intent (this,target); 
		intent.putExtra(name, value);
		startActivity(intent);
	}

	@Override
	public void onCancel(DialogInterface arg0) {
		
	}

	@Override
	public void onClick(int whichButton) {
		switch(whichButton){
		//0拍照、 1 相册选取、 2使用微信头像
		case  0:
			_imageFilepath =  BitmapUtils.getPath()+IMAGE_FILE_NAME;
			// File imageFile = new File(imageFilepath);
			 Intent intentFromCapture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			 ContentValues values = new ContentValues();
			 values.put(Media.TITLE, _imageFilepath);
			 _uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		     // 判断存储卡是否可以用，可用进行存储
		     if (Tools.hasSdcard()) {
		    	Log.v("相机拍照", "-----------");
	            intentFromCapture.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,_uri);
	            intentFromCapture.putExtra("return-data", false);
	            startActivityForResult(intentFromCapture,CAMERA_REQUEST_CODE);
		     }
		     break;
		case  1:
			  Intent intentFromGallery =  new Intent(Intent.ACTION_PICK, null);;
			  intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			  startActivityForResult(intentFromGallery,IMAGE_REQUEST_CODE);
              break;
		/*case  2:
			break;*/
		}
		
	}
     
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (resultCode != RESULT_CANCELED) {
		 switch (requestCode) {
	        case IMAGE_REQUEST_CODE:
	        	if(null!=data){
	        		_uri = data.getData();
	        		startPhotoZoom( _uri);
	        	}
	        	
	             break;
	        case CAMERA_REQUEST_CODE:
                if (Tools.hasSdcard()) {
                Uri uri =_uri;
                startPhotoZoom(uri);   
                } else {
                        APP.ShowToast(getString(R.string.nosdcard));
                }

                break;
	        case RESULT_REQUEST_CODE:
        		if(_uri == null){
        			break;
        		}
        		//Log.v("剪裁后的图片路径", ""+_uri.toString());
        		Log.v("上传文件路径", _imageFilepath);
        		_bitmap = BitmapUtils.getBitmapFromUri(this, _uri);
        		uploadPhoto();
                break;
	        }
	        super.onActivityResult(requestCode, resultCode, data);
		            
		 }
	}
	  
	private void uploadPhoto() {
		final Dlg_Wait dialogW = new Dlg_Wait(this, R.style.UpdateDialog);
		dialogW.setCancelable(false);
		dialogW.show();
		dialogW.UpdateTextNoDelay(getResources().getString(R.string.IN_UPLOAD));
		File photofile = BitmapUtils.CompressToFile(_bitmap, IMAGE_FILE_NAME,50);//剪裁后的照片按50%比例压缩后上传
		Log.v("处理后文件", ""+photofile.getName());
		RequestParams params = new RequestParams();
		params.put("userId", _UserId);
		//params.put("sessionId", Constants.sessionId);
		try {
			params.put("photo", photofile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String url = Constants.hostUrl+"/android/uploadPhoto";
		AsyncHttpClient client = new AsyncHttpClient();
		client.setTimeout(1000*15);
		client.post(url, params, new AsyncHttpResponseHandler() {
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				dialogW.dismiss();
				_faceimage.setImageBitmap(_bitmap);
				BitmapUtils.rename(IMAGE_FILE_NAME,_UserId);//成功后将文件改名
				Log.v(TAG, "userId:"+_UserId);
				UpdateUserInfo();//更新本地缓存(更新)
				_mhandler.sendEmptyMessage(CHANGED);
				APP.ShowToast(getResources().getString(R.string.SUCCESS_UPLOAD));
			}
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				dialogW.dismiss();
				APP.ShowToast(getResources().getString(R.string.Err_CONNET));
				BitmapUtils.clearCache(IMAGE_FILE_NAME);//销毁超时上传的本地头像
			}
		});
	}
	
	private void UpdateUserInfo() {
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("img","images/users/"+_UserId.concat(".jpg"));
		editor.commit();
	}
	//剪裁图片
	public void startPhotoZoom(Uri uri) {
	   if (uri == null) {  
	      Log.v(TAG, "The uri is not exist.");  
	      return;  
	   }  
	   Intent intent = new Intent("com.android.camera.action.CROP");  
	   if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {  
	      String url=URITOOL.getPath(this,uri);  
	      intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");  
	   }else{  
	      intent.setDataAndType(uri, "image/*");  
	   }
	   intent.putExtra("crop", "true");// 设置裁剪
	   intent.putExtra("aspectX", 1); // aspectX aspectY 是宽高的比例
	   intent.putExtra("aspectY", 1);
	   intent.putExtra("outputX", 320);// outputX outputY 是裁剪图片宽高
	   intent.putExtra("outputY", 320);
	   intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
	   intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
	   intent.putExtra("return-data", false);
	   startActivityForResult(intent, RESULT_REQUEST_CODE);
	}
	
	class InnerBroadcastReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			if("dataChanged".equals(intent.getExtras().getString("data"))){
				ReadUserInfo();
			}
		}
	}
	
	public void onBackPressed(){
		APP.GetMainActivity().ShowXView(Main.XV_NEW_MORE);
		finish();
		super.onBackPressed();
	}
	
	protected void onDestroy() { 
	    unregisterReceiver(_broadcast); 
	    super.onDestroy();
	}
}