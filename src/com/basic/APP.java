package com.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.manniu.manniu.R;
import com.utils.Constants;
import com.utils.LogUtil;
import com.utils.Loger;
import com.views.Dlg_Wait;
import com.views.Main;
import com.views.NewLogin;
import com.views.OnTaskListener;

@SuppressLint("UseSparseArrays")
@SuppressWarnings("static-access")
public class APP {
	private static final String ConfigFile = "ConfigFile";	// 存储文件关键字
	private static Map<Integer, Handler> _handlers = null;	// 程序Handler管理
	private static ExecutorService _threadPool = null;		// 线程池
	public static Dlg_Wait _dlgWait = null;
	public static ProgressDialog _progressDialog = null;
	public static int loginRet = -1; //判断注册是否成功 
	
	// 初始化APP
	public static boolean Init(Activity act) {
		Loger.print("APP.Init time:" + NewLogin.sdf.format(new Date()));
		_handlers = new HashMap<Integer, Handler>();
		_threadPool = Executors.newFixedThreadPool(3);
		_dlgWait = new Dlg_Wait(act, R.style.dialog);
		try {
			SDK.Init();
			new P2P.SDK().SetCallback("onData");
			Log.d("APP.INIT:", "##############################################userid:" + Constants.userid);
		} catch (Exception e) {
			Loger.print("APP.Init SDK.Init time:" + NewLogin.sdf.format(new Date()) +" exception msg:"+e.getMessage());
			return false;
		}
		Loger.print("APP.Init return true before time:" + NewLogin.sdf.format(new Date()));
		return true;
	}
	
	public static int etsLogin(){
		Loger.print("etsLogin time:" + NewLogin.sdf.format(new Date()));
		int status = SDK.LoginEts("cms.9wingo.com", 9511, 3, Constants.userid, Constants.userName, "pwd");
		LogUtil.d("APP", "etsLogin:"+status);
		if(status != 0){
			// 注册失败
			Builder builder = new AlertDialog.Builder(APP.GetMainActivity()).setTitle(GetString(R.string.tip_title)).setMessage(SDK.GetErrorStr(status))
			.setIcon(R.drawable.help)
			.setPositiveButton(GetString(R.string.confirm), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Main.Instance.ExitApp("close");//设备注册失败退出APP
				}
			});
			builder.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_SEARCH){
						return true;
				    }else{
				    	return false; //默认返回 false
				    }
				}
			});
			builder.setCancelable(false);
			builder.show();
		}
		return status;
	}
	
	public static void ipcLogin(){
		Loger.print("ipcLogin time:" + NewLogin.sdf.format(new Date()));
		//int nRet = SDK.Login("120.26.56.240", 6001, 3,0,"12345678902","12345678902", "pwd");
		int nRet = SDK.LoginIdm(3,Constants.userid,Constants.userid, "pwd");//登录同一个服务器  只用登录账号和密码不一样就可以
		LogUtil.d("APP.ipcLogin:", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>userid:" + Constants.userid+"-ret:-"+nRet);
		loginRet = nRet;
		if (nRet != 0) {	// 注册失败
			new AlertDialog.Builder(APP.GetMainActivity()).setTitle(GetString(R.string.tip_title)).setMessage(SDK.GetErrorStr(nRet))
			.setIcon(R.drawable.help)
			.setPositiveButton(GetString(R.string.confirm), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Main.Instance.ExitApp("close");//设备注册失败退出APP
				}
			}).show();
			return;
		}
	}

	// 向viewId发送Message（事先使用RegHandler函数注册viewId--Handler关联）
	public static boolean SendMsg(int viewId, Message msg) {
		if (_handlers.containsKey(viewId)) {
			Handler handler = _handlers.get(viewId);
			//发送消息到 Handler ，通过 handleMessage 方法接收
			return handler.sendMessage(msg);
		}
		return false;
	}

	public static boolean SendMsg(int viewId, int what, int arg1) {
		return SendMsg(viewId, what, arg1, 0, null);
	}
	public static boolean SendMsg(int viewId, int what, Object obj) {
		return SendMsg(viewId, what, 0, 0, obj);
	}
	public static boolean SendMsg(int viewId, int what, int arg1, int arg2) {
		return SendMsg(viewId, what, arg1, arg2, null);
	}
	public static boolean SendMsg(int viewId, int what, int arg1, Object obj) {
		return SendMsg(viewId, what, arg1, 0, obj);
	}
	public static boolean SendMsg(int viewId, int what, int arg1, int arg2, Object obj) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		return SendMsg(viewId, msg);
	}

	// 通过APP线程池驱动Runable
	public static void Execute(Runnable run) {
		_threadPool.execute(run);
	}

	// 事先使用RegHandler函数注册viewId--Handler关联
	public static boolean RegHandler(int id, Handler handler) {
		//System.out.println("main:  "+id+"--"+handler);
		_handlers.put(id, handler);
		return true;
	}

	// 显示Dialog
	static public void ShowDialog(String strTitle, String strMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(GetMainActivity());
		builder.setTitle(strTitle);
		builder.setMessage(strMsg);
		builder.setNegativeButton(GetString(R.string.confirm), null);
		builder.show();
	}
	
	// 显示ConfirmDialog(踢出账号号、删除设备用)
	static public void ShowConfirmDialog(String strTitle, String strMsg, OnClickListener yes, OnClickListener no, String yesText, String noText,Context context ) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(strTitle);
		builder.setMessage(strMsg);
		builder.setPositiveButton(yesText==null?GetString(R.string.confirm):yesText, yes);
		builder.setNegativeButton(noText==null?GetString(R.string.cancel):noText, no);
		AlertDialog mDialog = builder.create();
		mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设定为系统级警告，关键
		mDialog.show();
	}
	
	//同一个账号在别的设备上登陆(退出要求屏闭返回键、点击别的区域不消息)
	static public void ShowConfirmDialog2(String strTitle, String strMsg, OnClickListener no, String noText,Context context ) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(strTitle);
		builder.setMessage(strMsg);
		//builder.setPositiveButton(yesText==null?GetString(R.string.confirm):yesText, yes);
		builder.setNegativeButton(noText==null?GetString(R.string.cancel):noText, no);
		AlertDialog mDialog = builder.create();
		mDialog.setCanceledOnTouchOutside(false);//设置点击进度对话框外的区域对话框不消失
		mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设定为系统级警告，关键
		mDialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK){
					return true;
			    }else{
			    	return false; //默认返回 false
			    }
			}
		});
		mDialog.show();
	}
	
	static public void ShowDialog(String strMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(GetMainActivity());
		builder.setMessage(strMsg);
		builder.setNegativeButton(GetString(R.string.cancel), null);
		builder.show();
	}
	
	static public void ShowPromptDialog(String strTitle, String strMsg, OnClickListener yes, OnClickListener no, String yesText, String noText,Context context) {
		LayoutInflater li = LayoutInflater.from(GetMainActivity());
		View promptView = li.inflate(R.layout.prompts, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(GetMainActivity());
		builder.setView(promptView);
		final EditText userInput = (EditText) promptView.findViewById(R.id.editTextDialog);
		//PROMPT_TEXT = userInput.getText().toString();
		TextView text = (TextView) promptView.findViewById(R.id.textPrompt);
		text.setText(strMsg);
		builder.setTitle(strTitle);
		//builder.setMessage(strMsg);
		builder.setPositiveButton(yesText==null?GetString(R.string.confirm):yesText, yes);
		builder.setNegativeButton(noText==null?GetString(R.string.cancel):noText, no);
		builder.create().show();
	}
	
	//后台线程登录IDM 提示信息
	public static void showProgressDialog(Context context,String str){
		try {
			_progressDialog = new ProgressDialog(context);
			_progressDialog.setCancelable(false);//设置进度条是否可以按退回键取消
			_progressDialog.setCanceledOnTouchOutside(false);//设置点击进度对话框外的区域对话框不消失
			_progressDialog.setIndeterminate(true);
			_progressDialog.setMessage(str);
			_progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
	            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
	                if (keyCode == KeyEvent.KEYCODE_BACK) {
	                	_progressDialog.dismiss();
	                }
	                return false;
	            }
	        });
			_progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设定为系统级警告，关键
			_progressDialog.show();
		} catch (Exception e) {
		}
	}
	
	public static void dismissProgressDialog(){
		try {
			if(_progressDialog != null && _progressDialog.isShowing()){
				_progressDialog.dismiss();
				_progressDialog = null;
				//LogUtil.d("BackLoginThread",222);
			}
		} catch (Exception e) {
		}
	}
	

	// 显示错误Dialog
	static public void ShowError(String strError) {
		ShowDialog(GetString(R.string.err_tip), strError);
	}
	static public void ShowError(int strId) {
		ShowError(APP.GetString(strId));
	}
	
	// 显示提示(不带确认按键　２秒自动关闭)
	static public void ShowToast(String str) {
		Toast.makeText(GetMainActivity(), str, Toast.LENGTH_SHORT).show();
	}
	
	// 显示提示(不带确认按键　3.5秒自动关闭)
	static public void ShowToastLong(String str) {
		Toast.makeText(GetMainActivity(), str, Toast.LENGTH_LONG).show();
	}

	// 开始一次异步操作
	public static void ShowWaitDlg(OnTaskListener user, int text) {
		ShowWaitDlg(user, text, 0, 0, 0, null);
	}

	public static void ShowWaitDlg(OnTaskListener user, int text, int what) {
		ShowWaitDlg(user, text, what, 0, 0, null);
	}

	public static void ShowWaitDlg(OnTaskListener user, int text, int what, Object obj) {
		ShowWaitDlg(user, text, what, 0, 0, obj);
	}

	public static void ShowWaitDlg(OnTaskListener user, int text, int what, int arg1) {
		ShowWaitDlg(user, text, what, arg1, 0, null);
	}
	
	public static boolean IsWaitDlgShow() {
		return _dlgWait.isShowing();
	}

	public static void ShowWaitDlg(OnTaskListener user, int text, int what, int arg1, int arg2, Object obj) {
		String strText = "";
		if(text != 0){
			strText = APP.GetString(text);
		}
		_dlgWait.Show(user, strText, what, arg1, arg2, obj);
	}

	public static void SetWaitDlgText(String text) {
		_dlgWait.UpdateText(text);
	}
	
	public static void SetWaitDlgText(int textId) {
		_dlgWait.UpdateText(APP.GetString(textId));
	}

	// 获取字符串值
	public static String GetString(int id) {
		return Main.Instance.getText(id).toString();
	}

	// 遍历ViewGroup,并监听Button对象
	public static void ListenAllBtns(ViewGroup layout, android.view.View.OnClickListener listener) {
		int count = layout.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = layout.getChildAt(i);
			if (v instanceof Button) {
				//System.out.println(v.toString());
				v.setOnClickListener(listener);
			} else if (v instanceof ViewGroup) {
				ListenAllBtns((ViewGroup) v, listener);
			}
		}
	}

	// 监听views对象
	static public void ListenViews(ViewGroup layout, int views[], android.view.View.OnClickListener listener) {
		for (int i = 0; i < views.length; i++) {
			layout.findViewById(views[i]).setOnClickListener(listener);
		}
	}

	static public void ListenViews(Activity activity, int views[], android.view.View.OnClickListener listener) {
		for (int i = 0; i < views.length; i++) {
			activity.findViewById(views[i]).setOnClickListener(listener);
		}
	}
	
	static public Main GetMainActivity() {
		return Main.Instance;
	}

	// 创建目录/sdcard/test/hello.txt"
	static boolean MakeDir(String fileName) {
		File file = new File(fileName);
		if (!file.exists())// 是否存在
		{
			if (!file.mkdirs()) {
				return false;
			}
		}
		try {
			return file.createNewFile();
		} catch (IOException e) {
		}
		return false;
	}

	// 获取APP配置
	public static String GetAppConfig_Str(String key, String defalutValue) {
		return GetSharedPreferences(ConfigFile, key, defalutValue);
	}

	public static int GetAppConfig_Int(String strKey, int defalutValue) {
		SharedPreferences preferences = GetMainActivity().getSharedPreferences(ConfigFile, GetMainActivity().MODE_PRIVATE);
		return preferences.getInt(strKey, defalutValue);
	}

	// 配置APP配置
	public static int SetAppConfig_Str(String strKey, String strValue) {
		return SetSharedPreferences(ConfigFile, strKey, strValue);
	}

	public static int SetAppConfig_Int(String strKey, int value) {
		return SetAppConfig_Str(strKey, String.valueOf(value));
	}

	// 锟斤拷 SharedPreferences
	public static String GetSharedPreferences(String name, String strKey, String defaultValue) {
		SharedPreferences preferences = GetMainActivity().getSharedPreferences(name, GetMainActivity().MODE_PRIVATE);
		return preferences.getString(strKey, defaultValue);
	}

	// 写 SharedPreferences
	public static int SetSharedPreferences(String name, String strKey, String strValue) {
		SharedPreferences preferences = GetMainActivity().getSharedPreferences(name, GetMainActivity().MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(strKey, strValue);
		editor.commit();
		return 0;
	}

	public static int ClearSharedPreferences(String name, String strKey, String strValue) {
		SharedPreferences preferences = GetMainActivity().getSharedPreferences(name, GetMainActivity().MODE_PRIVATE);
		if (preferences != null) {
			Editor editor = preferences.edit();
			editor.clear();
			editor.commit();
		}
		return 0;
	}

	@SuppressWarnings("resource")
	public static Object ReadObject(File f) throws Exception {
		InputStream is = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(is);
		return ois.readObject();
	}

}