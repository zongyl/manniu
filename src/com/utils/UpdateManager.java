package com.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adapter.UpdateDialog;
import com.basic.APP;
import com.bean.Update;
import com.manniu.manniu.R;
import com.views.BaseApplication;
import com.views.Main;
import com.views.NewLogin;

public class UpdateManager {
	private static final String TAG ="UpadteManager";
	private static final int DOWN_NOSDCARD = 0;
	private static final int DOWN_UPDATE = 1;
	private static final int DOWN_OVER = 2;
	
	private static final int DIALOG_TYPE_LATEST = 0;
	private static final int DIALOG_TYPE_FAIL = 1;
	
	private Context mContext;
	//下载线程
	private Thread downloadThread;
	//通知对话框
	private UpdateDialog noticeDialog;
	
	private Dialog notice;
	
	//更新下载对话框
	private Dialog downloadDialog;
	//已经是最新 或下载失败对话框
	private UpdateDialog latestOrFailDialog;
	//进度条
	private ProgressBar mProgrss;
	//进度值
	private int progress;
	//显示下载数值
	private TextView mProgressText;
	//查询动画
	private ProgressDialog mProDialog;
	//显示已下载量
	private String tempFileSize;
	//显示文件大小
	private String apkFileSize;
	//终止标记
	private boolean stopFlag;
	//提示语
	private String updateMsg="";
	//返回的安装包url
	private String apkUrl="";
	//下载包保存路径
	private String savePath="";
	//完整安装包路径
	private String apkFilePath="";
	//临时下载文件路径
	private String tmpFilePath="";
	private String curVersionName="";
	private int curVersionCode;
	private Update mUpdate;
	private UpdateClient mUpdateClient;
	private boolean isAuto;
	private static UpdateManager  updateManager;
	
	public static UpdateManager getUpdateManger(){
		if(updateManager == null){
			updateManager = new UpdateManager();
		}
		updateManager.stopFlag = false;
		return updateManager;
	}
	
	private Handler mHandler = new Handler(APP.GetMainActivity().getMainLooper()){
		public void handleMessage(Message message){
			switch(message.what){
			case DOWN_UPDATE:
				mProgrss.setProgress(progress);
				//mProgressText.setText(tempFileSize+"/"+apkFileSize);
				mProgressText.setText(""+progress+"%");
				break;
			case DOWN_OVER:
				downloadDialog.dismiss();
				installApk();
				break;
			case DOWN_NOSDCARD:
				downloadDialog.dismiss();
				APP.ShowToast(mContext.getString(R.string.nosdcard));
				break;
			}
		}
	};
	/**获取当前版本信息*/
	public void getCurrentVersion(){
		try {
			PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			curVersionCode = info.versionCode;
			curVersionName = info.versionName;
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
	/**检查app更新
	 * @param context
	 * @parem isShowMsg 是否显示提示消息
	 * */
	public void checkAppUpdate(Context context,final boolean isShowMsg,boolean isAuto){
		this.mContext =context;
		this.isAuto = isAuto;
		getCurrentVersion();
		if(isShowMsg){
			if(mProDialog == null){
				mProDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.checking), true, true);
			}/*else if(mProDialog.isShowing()||(latestOrFailDialog!=null&&latestOrFailDialog.isShowing())){
				Log.v(TAG, "不显示对话框   1");
				return;
			}*/else if(!mProDialog.isShowing()){
				mProDialog.show();
			}
		}
		final Handler handler = new Handler(APP.GetMainActivity().getMainLooper()){
			public void handleMessage(Message msg){
				//进度条对话框不显示，检测结果也不显示
				/*if(mProDialog!=null || !mProDialog.isShowing()){
					Log.v(TAG, "不显示对话框    2");
					return;
				}*/
				//关闭并释放检查更新进度条对话框
				/*if(isShowMsg && mProDialog!=null){
					mProDialog.dismiss();
					mProDialog=null;
				}*/
				/**如果查询进度框不显示，直接返回*/
				if(mProDialog!=null &&!mProDialog.isShowing()){
					return;
				}
				//显示检测结果
				if(msg.what == 1){
					/**收到消息之后隐藏进度框，显示检查结果*/
					if(mProDialog!=null && mProDialog.isShowing()){
						mProDialog.dismiss();
					}
					mUpdate =(Update) msg.obj;
					if(mUpdate!=null){
						if(curVersionCode<mUpdate.getVersionCode()){
							apkUrl = mUpdate.getDownloadUrl();
							updateMsg =mUpdate.getUpdateLog();
							curVersionName = mUpdate.getVersionName();
							showNoticeDialog();
						}else if(isShowMsg){
							showLatestOrFailDialog(DIALOG_TYPE_LATEST);//已经是最新版本
						}
					}
				}else if(isShowMsg){
					showLatestOrFailDialog(DIALOG_TYPE_FAIL);
				}
			}
		};
		new Thread(){
			public void run(){
				Message msg = new Message();
				try{
					mUpdateClient = new UpdateClient(mContext);
					Update update = mUpdateClient.checkVersion((BaseApplication)mContext.getApplicationContext());
					msg.what =1;
					msg.obj =update;
				}catch(Exception e){
					e.printStackTrace();
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	/**显示版本更新通知对话框*/
	private void showNoticeDialog(){
		/*if(isAuto){
			noticeDialog =new UpdateDialog(mContext, R.style.UpdateDialog, "发现新版本"+curVersionName, "立即更新", "不再提醒",
					new UpdateDialog.DialogClickListener() {

				@Override
				public void onRightBtnClick(Dialog dialog) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}

				@Override
				public void onLeftBtnClick(Dialog dialog) {
					dialog.dismiss();
					showDownloadDialog();
				}
			});
		}else{
			noticeDialog =new UpdateDialog(mContext, R.style.UpdateDialog, "发现新版本"+curVersionName, "立即更新", "以后再说",
					new UpdateDialog.DialogClickListener() {

				@Override
				public void onRightBtnClick(Dialog dialog) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}

				@Override
				public void onLeftBtnClick(Dialog dialog) {
					dialog.dismiss();
					showDownloadDialog();
				}
			});
		}
		noticeDialog.show();*/	
		try {
			if(mContext != null){
				final LayoutInflater inflater = LayoutInflater.from(mContext);
				View v = inflater.inflate(R.layout.update_notice,null);
				AlertDialog.Builder builder = new Builder(mContext);
				notice = builder.create();
				notice.setCancelable(true);
				LogUtil.d("tag", "shwo start...");
				notice.show();
				LogUtil.d("tag", "shwo end...");
				notice.getWindow().setContentView(v);
				WindowManager windowManager= notice.getWindow().getWindowManager();
				//WindowManager windowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
				Display display = windowManager.getDefaultDisplay();
				WindowManager.LayoutParams lp = notice.getWindow().getAttributes();
				int size = 0;
				if(display.getWidth()>display.getHeight()){
					size = (int) display.getHeight()/8*7;
				}else{
					size = (int) display.getWidth()/8*7;
				}
				lp.width = size;
				lp.height = size;
				//lp.width = (int) (display.getWidth() / 6 * 5); //设置宽度
				//lp.height = (int) (display.getHeight()/5*3);   //设置高度
				notice.getWindow().setAttributes(lp);
				TextView title = (TextView) v.findViewById(R.id.notice_title);
				title.setText(mContext.getString(R.string.new_version)+curVersionName);
				TextView tCancel = (TextView) v.findViewById(R.id.button1);
				TextView tConfirm = (TextView)v.findViewById(R.id.button2);
				TextView tLog = (TextView) v.findViewById(R.id.update_log);
				if(mUpdate.getUpdateLog() != null){
					String [] temp = mUpdate.getUpdateLog().split("-");
					StringBuffer sbuf =new StringBuffer();
					for(String str:temp){
						sbuf.append(" - ".concat(str)).append("\n");
					}
					if(sbuf.length()>0){
						sbuf.append(mContext.getString(R.string.file_count)+mUpdate.getApkFileSize());
					}
					tLog.setText(sbuf.toString());
				}
				tCancel.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						notice.dismiss();
					}
				});
				tConfirm.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						notice.dismiss();
						Main.Instance._loginThead.stop();
						showDownloadDialog();
					}
				});
			}
		} catch (Exception e) {
			LogUtil.e("UpdateManager", ExceptionsOperator.getExceptionInfo(e));
		}
	}
	

	
	
	/**显示下载对话框*/
	private void showDownloadDialog(){
		AlertDialog.Builder builder = new Builder(mContext);
		//builder.setTitle("正在下载");
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v= inflater.inflate(R.layout.update_progress,null);
		final int cFullFillWidth = 10000;
		v.setMinimumWidth(cFullFillWidth);//设置对话框尽可能的宽
		mProgrss =(ProgressBar) v.findViewById(R.id.update_progress);
		mProgressText =(TextView) v.findViewById(R.id.update_progress_text);
		builder.setView(v);
		builder.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				stopFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.show();
		downloadApk();
	}
	
	/**显示'已经是最新' 或'无法获取版本信息'对话框*/
	private void showLatestOrFailDialog(int dialogType){
		
		if(latestOrFailDialog!=null){
			//关闭并释放之前的对话框
			latestOrFailDialog.dismiss();
			latestOrFailDialog =null;
		}
		if(dialogType == DIALOG_TYPE_LATEST){
			latestOrFailDialog =new UpdateDialog(mContext, R.style.UpdateDialog, mContext.getString(R.string.mostnew), mContext.getString(R.string.confirm), "",
					new UpdateDialog.DialogClickListener() {

				@Override
				public void onRightBtnClick(Dialog dialog) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}

				@Override
				public void onLeftBtnClick(Dialog dialog) {
					dialog.dismiss();
				
				}
			});
			
		}else if(dialogType ==DIALOG_TYPE_FAIL){
			latestOrFailDialog =new UpdateDialog(mContext, R.style.UpdateDialog, mContext.getString(R.string.E_SER_FAIL), mContext.getString(R.string.confirm), "",
					new UpdateDialog.DialogClickListener() {

				@Override
				public void onRightBtnClick(Dialog dialog) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}

				@Override
				public void onLeftBtnClick(Dialog dialog) {
					dialog.dismiss();
				
				}
			});
		}
		latestOrFailDialog.show();
	}
	
	private Runnable mdownApkRunnable = new Runnable(){
		@Override
		public void run() {
			try{
				String apkName=mUpdate.getAppName()+mUpdate.getVersionName()+".apk";
				String temApk =mUpdate.getAppName()+mUpdate.getVersionName()+".tmp";
				//判断是否挂载SD卡
				String storageState =Environment.getExternalStorageState();
				if(storageState.equals(Environment.MEDIA_MOUNTED)){
					savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/download/";
					File file = new File(savePath);
					if(!file.exists()){
						file.mkdirs();
					}
					apkFilePath = savePath+apkName;
					tmpFilePath = savePath+temApk;
				}
				//没有挂载SD卡,无法下载
				if(apkFilePath == null || apkFilePath ==""){
					mHandler.sendEmptyMessage(DIALOG_TYPE_FAIL);
					return;
				}
				File apkFile = new File(apkFilePath);
				if(apkFile.exists()){
					downloadDialog.dismiss();
					installApk();
					return;
				}
				//输出临时下载文件
				File temFile= new File(tmpFilePath);
				FileOutputStream fos = new FileOutputStream(temFile);
				URL url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();
				//显示文件大小格式:2个小数点显示
				DecimalFormat df= new DecimalFormat("0.00");
				//进度条下面显示总文件大小
				apkFileSize = df.format((float)length/1024/1024)+"MB";
				int count =0;
				byte[] buf = new byte[1024];
				do{
					int numread = is.read(buf);
					count+=numread;
					//进度条显示的 当前下载文件大小
					tempFileSize =df.format((float)count/1024/1024)+"MB";
					//当前进度值
					progress =(int)(((float)count/length)*100);
					//更新进度
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if(numread<=0){
						//下载完成-将临时下载文件 转成APK文件
						if(temFile.renameTo(apkFile)){
							//通知安装
							mHandler.sendEmptyMessage(DOWN_OVER);
						}
						break;
					}
					fos.write(buf, 0, numread);
				}while(!stopFlag);//点击取消就停止下载
				fos.close();
				is.close();
			}catch(MalformedURLException e){
				LogUtil.d(TAG, ExceptionsOperator.getExceptionInfo(e));
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	};
	
	/**下载apk*/
	private void downloadApk(){
		if(!TrafficMonitor.isWifi(mContext)){
			new UpdateDialog(mContext, R.style.UpdateDialog, mContext.getString(R.string.net_tip), mContext.getString(R.string.go_on), mContext.getString(R.string.cancel),
					new UpdateDialog.DialogClickListener() {

				@Override
				public void onRightBtnClick(Dialog dialog) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}

				@Override
				public void onLeftBtnClick(Dialog dialog) {
					dialog.dismiss();
					doDownload();
				}
			}).show();
		}
		doDownload();
	}
	
	public void doDownload(){
		downloadThread = new Thread(mdownApkRunnable);
		downloadThread.start();
	}
	/**安装apk*/
	private void installApk() {
		File apkFile = new File(apkFilePath);
		if(!apkFile.exists()){
			return;
		}
		APP.GetMainActivity().getSharedPreferences(NewLogin.SAVEFILE,APP.GetMainActivity().MODE_PRIVATE).edit().clear();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://"+apkFile.toString()), "application/vnd.android.package-archive");
		mContext.startActivity(i);
	}
}