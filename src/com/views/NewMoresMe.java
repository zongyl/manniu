package com.views;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.adapter.Menu;
import com.adapter.UpdateDialog;
import com.basic.APP;
import com.manniu.manniu.R;
import com.utils.BitmapUtils;
import com.utils.Constants;
import com.utils.DateUtil;
import com.utils.SetSharePrefer;
import com.utils.UpdateManager;

import de.hdodenhof.circleimageview.CircleImageView;

//个人
public class NewMoresMe extends XViewBasic  implements OnItemClickListener{
	private String TAG =getClass().getName();
	
	static String SAVEFILE = "Info_Login";
	String _img ="";
	String _userid = "";
	//private final static int CHANGED = 0*0010;
	private final static String PER_DETAIL_EDIT = "com.views.NewDetailEdit";// 个人资料编辑页面
	private final static String PER_DETAIL_SET = "com.views.NewDetailSet";//基本配置
	private final static String PER_DETAIL_HELP = "com.views.NewDetailHelp";//意见反馈
	private final static String PER_DETAIL_ABOUT = "com.views.NewDetailAbout";//关闭
	private final static String PRE_LOG_IN ="com.views.SplashScreen";
	private UpdateDialog tipDialog;
	BaseApplication _bApp = null;
	ImageView _headImage = null;
	ListView listView0, listView1, listView2;
	LogoutDilog _mydilog;
	InnerBroadcastReceiver _broadcast;
	 
	boolean logout,exitApp;
	private List<Menu> getMenuList(Activity act, int titleid, int resid) {
		List<Menu> ret = new ArrayList<Menu>();
		Menu menu;
		
		TypedArray imgs  =  ACT.getResources().obtainTypedArray(resid);
		String[] strs = ACT.getResources().getStringArray(titleid);
		
		for (int i = 0; i<imgs.length(); i++) {
			menu = new Menu();
			menu.setText(strs[i]);
			menu.setIconResid(imgs.getResourceId(i, 0));
			menu.setLink("");
			
			ret.add(menu);
		}
		
		
		return ret;
	}
	
	SharedPreferences preferences = null;
	
	public NewMoresMe(Activity activity, int viewId, String title) {
		super(activity, viewId, title);
		// 加载页面中的ListView，并添加监听
		listView0 = (ListView) findViewById(R.id.list01);//基本配置
		listView1 = (ListView) findViewById(R.id.list02);//意见反馈、版本检测、关于
		listView2 = (ListView) findViewById(R.id.list03);//退出登录、关闭蛮牛

		listView0.setOnItemClickListener(this);
		listView1.setOnItemClickListener(this);
		listView2.setOnItemClickListener(this);
		
		findViewById(R.id.photo).setOnClickListener(this);
		findViewById(R.id.per_edit).setOnClickListener(this);
		listView0.setAdapter(new com.adapter.MenuAdapter(activity, getMenuList(
				activity, R.array.menuList0,R.array.imgGroup0),false));
		listView1.setAdapter(new com.adapter.MenuAdapter(activity, getMenuList(
				activity,R.array.menuList1, R.array.imgGroup1),true));
		listView2.setAdapter(new com.adapter.MenuAdapter(activity, getMenuList(
				activity, R.array.menuList2,R.array.imgGroup2),false));
		//给页面头像和昵称赋值
		ReadUserInfo();
		_bApp =(BaseApplication) ACT.getApplication();
		_headImage =(CircleImageView) findViewById(R.id.photo);
		getHeadImg();
		Log.v(TAG, _userid);
		RegistBroadcast();
	}
	
	public void ReadUserInfo(){
		preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		String username ="";
		if(preferences.contains("username"))//判断该项是否存在
		{
			username= preferences.getString("username", "");
		}
		((TextView)findViewById(R.id.main_hotname)).setText(username);
	}
	 /**注册广播*/
	private void RegistBroadcast() {
		IntentFilter filter = new IntentFilter(NewItemDetailEdit.action); 
		filter.setPriority(20);
		_broadcast = new InnerBroadcastReceiver();
		ACT.registerReceiver(_broadcast, filter);
	}
	
	public void getHeadImg(){
		try {			
			_img = preferences.getString("head_img", "");
			_userid = preferences.getString("sid", "");
			BitmapUtils.loadImage(_img, _userid, _headImage);
		} catch (Exception e) {
		}
	}
	
	protected void OnMessage(Message msg) {
 		if(msg.what == NewDetailEdit.IMG_CHANGED){
			//getHeadImg();
 			//加载本地
 			if(BitmapUtils.path.length()>0 && Constants.userid.length()>0)//防止BitmapUtils.path和Constants.userid为空值
 			{
	 			String path = BitmapUtils.path+Constants.userid +".jpg";	 			
	 			File file = new File(path);
	 			if(file.exists()){
	 				Bitmap bt = BitmapFactory.decodeFile(path);//从Sd中找头像，转换成Bitmap
	 				_headImage.setImageBitmap(bt);
	 			}
 			}
		}
	}
	
	protected void onClick(int id) {
		switch(id){
		case R.id.photo:
			_bApp.setMyhandler(_handler);
			forwardTo(PER_DETAIL_EDIT);//跳转到个人信息编辑页面
			break;
		case R.id.per_edit:
			_bApp.setMyhandler(_handler);
			forwardTo(PER_DETAIL_EDIT);//跳转到个人信息编辑页面
			break;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		TextView tv = (TextView) ((LinearLayout) view).findViewById(R.id.menu_txt);
		if (tv.getText().equals(ACT.getString(R.string.logout))) {//logout
			logout =true;
			_mydilog = new LogoutDilog(ACT,R.style.ActionSheet);
		}
		if (tv.getText().equals(ACT.getString(R.string.base_set))) {//set
			forwardTo(PER_DETAIL_SET);//跳转到基本配置页面
		}
		if (tv.getText().equals(ACT.getString(R.string.suggest))) {//feedback
			forwardTo(PER_DETAIL_HELP);//跳转到意见反馈页面
		}
		if (tv.getText().equals(ACT.getString(R.string.about))) {//about
			forwardTo(PER_DETAIL_ABOUT);//跳转到关于页面
		}
		if(tv.getText().equals(ACT.getString(R.string.ver_check))){//version check
			String timeNow = DateUtil.getCurrentStringDate("yyyyMMdd");
			SetSharePrefer.write("Info_Login", "check_time", timeNow);
			UpdateManager.getUpdateManger().checkAppUpdate(ACT, true,false);
		}
		if(tv.getText().equals(ACT.getString(R.string.exitApp))){//close app
			/*tipDialog = new UpdateDialog(ACT, R.style.UpdateDialog, "关闭后将收不到任何报警和推送", "确定", "取消",
					new UpdateDialog.DialogClickListener() {

				@Override
				public void onRightBtnClick(Dialog dialog) {
					dialog.dismiss();
				}
				@Override
				public void onLeftBtnClick(Dialog dialog) {
					dialog.dismiss();
					APP.GetMainActivity().finish();
					BaseApplication.getInstance().exitApp("close");
				}
			});
			tipDialog.show();*/
			exitApp = true;
			_mydilog = new LogoutDilog(ACT,R.style.ActionSheet);
		}
	}

	/**
	 * 跳转到加载页面
	 */
	private void forwardTo(String target) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClassName(ACT, target);
		APP.GetMainActivity().startActivity(intent);
	}
	
	class LogoutDilog extends Dialog implements OnClickListener{
		
		public LogoutDilog(Context context, int theme) {
			super(context, theme);
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.new_logout_items, null);
			TextView title = (TextView) layout.findViewById(R.id.title);
			if(logout){
				title.setText(getContext().getString(R.string.logout_tip));
			}else{
				title.setText(getContext().getString((R.string.exit_tip)));
			}
			final int cFullFillWidth = 10000;
			layout.setMinimumWidth(cFullFillWidth);
			Window w = this.getWindow();
			WindowManager.LayoutParams lp = w.getAttributes();
			lp.x = 0;
			final int cMakeBottom = -1000;
			lp.y = cMakeBottom;
			lp.gravity = Gravity.BOTTOM;
			this.onWindowAttributesChanged(lp);
			this.setCanceledOnTouchOutside(true);
			setContentView(layout);
			this.show();
			findViewById(R.id.logout_confirm).setOnClickListener(this);
			findViewById(R.id.logout_cancel).setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			 switch(v.getId()){
			 case R.id.logout_confirm:
				 if(logout){//退出登录
					 _mydilog.dismiss();
					//APP.GetMainActivity().finish();
					Main.Instance.ExitApp("exit");
					//BaseApplication.getInstance().exitApp("exit");
					forwardTo(PRE_LOG_IN);
					break;
				 }else{//关闭蛮牛
					 _mydilog.dismiss();
					 APP.GetMainActivity().finish();
					 Main.Instance.ExitApp("close");
					// BaseApplication.getInstance().exitApp("close");
					 break;
				 }
					
			 case R.id.logout_cancel:
				 logout = false;
				 exitApp = false;
				 this.dismiss();
				 break;
			 }
		}
	}
	
	class InnerBroadcastReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			if("dataChanged".equals(intent.getExtras().getString("data"))){
				ReadUserInfo();
				abortBroadcast();
			}
		}
	}
	
	protected void onDestroy() { 
		if(_broadcast!=null){
			ACT.unregisterReceiver(_broadcast); 
		}
	}
}