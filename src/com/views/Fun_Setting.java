package com.views;
import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.ctrl.OnChangedListener;
import com.ctrl.XSlipButton;
import com.ctrl.XSlipSoundButton;
import com.utils.Constants;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Fun_Setting extends XViewBasic implements OnTouchListener,OnSeekBarChangeListener,OnChangedListener{	
	public static Fun_Setting instance = null;	
//	public static String ImagePath = "";
//	public static String RecordPath = "";
//	public static String logPath = "";
	public static int PtzStep = 3;
//	public static int bDecIOnly = 0;
	TextView _tvStep;
//	SeekBar _skStep;
	TextView _tvImagePath;
	TextView _tvRecPath;
	//CheckBox _decIFOnly;
	boolean _bInit = false;
	public final static String SAVEFILE = "SETTING";
	TextView _pwd;
	TableRow _pwdRow;
	static private int openfileDialogId = 0;
	public static final int MODE_WORLD_READABLE = 0x0001;
	private XSlipButton _OnButton;
	private XSlipSoundButton _OffButton;
	RadioGroup _radGroup;
	RadioButton _storageSDK,_storageMobile;
	public static int stoStep = 0; //存储位置状态
	public static int smsStep = 1; //自动布撤防状态
	public static int soundStep = 1; //报警提示音状态
	
	@SuppressWarnings("static-access")
	public Fun_Setting(Activity activity, int viewId, String title) {
		super(activity, viewId, title);	
		instance = this;
		//存储路径显示
		//_tvImagePath = (TextView)this.findViewById(R.id.tvImgPath);
		
		_pwd = (TextView) this.findViewById(R.id.pwd_channge);
		_pwdRow = (TableRow) this.findViewById(R.id.pwd_row);
		
		_OnButton = (XSlipButton) this.findViewById(R.id.btn_autoSMS);
		_OnButton.SetOnChangedListener(this);//设置事件监听
		_OffButton = (XSlipSoundButton) this.findViewById(R.id.btn_autoSound);
		_OffButton.SetOnChangedListener(this);//设置事件监听
		
		_radGroup = (RadioGroup) this.findViewById(R.id.rg_storage);
		_storageSDK = (RadioButton) this.findViewById(R.id.sto_sdk);
		_storageMobile = (RadioButton) this.findViewById(R.id.sto_mobile);

		_radGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId==_storageSDK.getId()){
                    //System.out.println("sdk:"+Environment.getExternalStorageDirectory());
                    String tmp = getFilePath();
            		//setFilePath(tmp);
            		//stoStep = 0;
            		SaveConfigInfo(3,0);
                }else if(checkedId==_storageMobile.getId()){
                	//System.out.println("内部存储："+Environment.getDataDirectory());
                	//setFilePath("/data");
                	//stoStep = 1;
                	SaveConfigInfo(3,1);
                }
            }
        });
		
		_bInit = true;
		SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		PtzStep = preferences.getInt("PTZStep", 3);
		stoStep = preferences.getInt("stoStep", 0);
		if(stoStep == 0){
			_storageSDK.setChecked(true);
			String tmp = getFilePath();
			//setFilePath(tmp);
		}else if(stoStep == 1){
			_storageMobile.setChecked(true);
			//setFilePath("/data");
		}
		
		int ids[] = {R.id.pwd_channge};
		for (int j = 0; j < ids.length; j++) {
			this.findViewById(ids[j]).setOnTouchListener(this);
		}
		
		
	}
	
	/*@SuppressLint("SdCardPath")
	public void setFilePath(String path){
		if(path.equals("/data")){
			path = "/data/data/com.manniu.manniu"; //TDM的安装路径
		}
		ImagePath = path + "/manniu/images/";
		RecordPath = path + "/manniu/records/";
		logPath = path +"/manniu/logs/";
		//_tvImagePath.setText(path); //将存储路径显示在页面上
	}*/
	
	//SD卡是否存在
	private String getFilePath() {
		String tmpPath = "";
		//SDK卡
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
//			System.out.println("sdk:"+Environment.getExternalStorageDirectory());
//			System.out.println("内部存储："+Environment.getDataDirectory());
			tmpPath = Environment.getExternalStorageDirectory()+"";
		} else{
			tmpPath = Environment.getDataDirectory()+"";
//			System.out.println("  getRootDirectory:-"+Environment.getRootDirectory());
//			System.out.println("内部存储："+Environment.getDataDirectory());
		}
		return tmpPath;
	}
	
	//这里为开或者关时自己所需要做的动作或实现的内容处理
	public void OnChanged(boolean CheckState,View v) {
		switch (v.getId()) {
		case R.id.btn_autoSMS:
			if (CheckState) {
				//APP.ShowToast("打开");
				SaveConfigInfo(1,0);
			} else {
				//APP.ShowToast("关闭。。");
				SaveConfigInfo(1,1);
//				if(Main.Instance._notificationManager != null)
//					Main.Instance._notificationManager.cancel(1);//清空状态栏通知
			}
			break;
		case R.id.btn_autoSound:
			if (CheckState) {
				SaveConfigInfo(2,0);
			} else {
				SaveConfigInfo(2,1);
			}
			break;
		default:
			break;
		}
				
	}
	
	// 写 SharedPreferences
	@SuppressWarnings("static-access")
	public int SaveConfigInfo(int type,int value) {
		 SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
		 Editor editor = preferences.edit();
		 if(type == 1){
			 editor.putInt("smsStep", value);
		 }else if(type == 2){
			 editor.putInt("soundStep", value);
		 }else if(type == 3){
			 editor.putInt("stoStep", value);
		 }
		
		 editor.commit();
		return 0;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void setVisibility(int visibility){
		if(visibility == View.INVISIBLE){
			 SharedPreferences preferences = APP.GetMainActivity().getSharedPreferences(SAVEFILE, APP.GetMainActivity().MODE_PRIVATE);
			 Editor editor = preferences.edit();
			 editor.putInt("PTZStep", PtzStep);
//			 bDecIOnly = _decIFOnly.isChecked() ? 1 : 0;
			 editor.putInt("DecIFOnly", 0);
			 editor.commit();
		}
		super.setVisibility(visibility);
	}
	 
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		if(_bInit){
			PtzStep = arg1;
			_tvStep.setText("" + (arg1 + 1));
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//0- 按下  1-抬起
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			v.setBackgroundColor(Constants.COLOR_SELECTED);
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			//APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_PassWord);
			onEventHandle(v);
			v.setBackgroundColor(Constants.COLOR_WHITE);
			break;
		}
		return true;
	}
	
	
	//处理事件
	public void onEventHandle(View v){
		switch (v.getId()) {
		case R.id.pwd_channge:
			APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_WEB);//XV_PassWord
			break;
//		case R.id.tvImgPath:
//			//打开文件浏览功能 
//			//showDialog(openfileDialogId).show();
//			break;
		default:
			break;
		}
		
	}
	public Dialog _dialog = null;
	public void dissDialog(int id) {
		_dialog.cancel();
		APP.GetMainActivity().removeDialog(id);
	}
	
	/*public Dialog showDialog(int id) {
		//AlertDialog.Builder builder = new AlertDialog.Builder(APP.GetMainActivity());
		if(id==openfileDialogId){
			Map<String, Integer> images = new HashMap<String, Integer>();
			// 下面几句设置各文件类型的图标， 需要你先把图标添加到资源文件夹
			images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);	// 根目录图标
			images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);	//返回上一层的图标
			images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);	//文件夹图标
			images.put("wav", R.drawable.filedialog_wavfile);	//wav文件图标
			images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
			OpenFileDialog ofd = new OpenFileDialog();
			Dialog dialog = ofd.createDialog(id, APP.GetMainActivity(), "浏览文件夹", new CallbackBundle() {
				@Override
				public void callback(Bundle bundle) {
//					String filepath = bundle.getString("path");
					//setTitle(filepath); // 把文件路径显示在标题上
				}
			}, 
			".wav;",
			images);
			_dialog = dialog;
			return dialog;
		}
		//builder.show();
		return null;
	}*/
	
	
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	public void onStopTrackingTouch(SeekBar arg0) {
	}
}