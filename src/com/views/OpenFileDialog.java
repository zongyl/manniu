// filename: OpenFileDialog.java
package com.views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.basic.APP;
import com.basic.XMSG;
import com.manniu.manniu.R;
import com.views.bovine.Fun_AnalogVideo;

// 参数说明
// context:上下文
// dialogid:对话框ID
// title:对话框标题
// callback:一个传递Bundle参数的回调接口
// suffix:需要选择的文件后缀，比如需要选择wav、mp3文件的时候设置为".wav;.mp3;"，注意最后需要一个分号(;)
// images:用来根据后缀显示的图标资源ID。
//	根目录图标的索引为sRoot;
//	父目录的索引为sParent;
//	文件夹的索引为sFolder;
//	默认图标的索引为sEmpty;
//	其他的直接根据后缀进行索引，比如.wav文件图标的索引为"wav"
///system 寄存的是rom的信息； 
///system/app 寄存rom本身附带的件软即系统件软； 
///system/data 寄存/system/app 中核心系统件软的数据件文信息。
///data 寄存的是户用的件软信息（非自带rom装安的件软）； 
///data/app 寄存户用装安的件软； 
///data/data 寄存有所件软（包含/system/app 和 /data/app 和 /mnt/asec中装的件软）的一些lib和xml件文等数据信息； 
///data/dalvik-cache 寄存程序的缓存件文，这里的件文都是可以删除的。 
@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class OpenFileDialog {
	public static String tag = "OpenFileDialog";
	static final public String sRoot = "/"; 
	static final public String sParent = "..";
	static final public String sFolder = ".";
	static final public String sEmpty = "";
	static final private String sOnErrorMsg = "No rights to access!";
	
	public  Dialog createDialog(int id, Context context, String title, CallbackBundle callback, String suffix, Map<String, Integer> images){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(new FileSelectView(context, id, callback, suffix, images));
		Dialog dialog = builder.create();
		//dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setTitle(title);
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();  
		params.width = 100;  
		params.height = 100;  
		dialog.getWindow().setAttributes(params); 
		return dialog;
	}
	
	@SuppressLint("DefaultLocale")
	public class FileSelectView extends ListView implements OnItemClickListener{
		public CallbackBundle callback = null;
		public  String path = sRoot;
		public  List<Map<String, Object>> list = null;
		public  int dialogid = 0;
		public String suffix = null;
		public Map<String, Integer> imagemap = null;
		
		public FileSelectView(Context context, int dialogid, CallbackBundle callback, String suffix, Map<String, Integer> images) {
			super(context);
			this.imagemap = images;
			this.suffix = suffix==null?"":suffix.toLowerCase();
			this.callback = callback;
			this.dialogid = dialogid;
			this.setOnItemClickListener(this);
			refreshFileList();
		}
		
		private String getSuffix(String filename){
			int dix = filename.lastIndexOf('.');
			if(dix<0){
				return "";
			}
			else{
				return filename.substring(dix+1);
			}
		}
		
		private int getImageId(String s){
			if(imagemap == null){
				return 0;
			}else if(imagemap.containsKey(s)){
				return imagemap.get(s);
			}else if(imagemap.containsKey(sEmpty)){
				return imagemap.get(sEmpty);
			}else {
				return 0;
			}
		}
		
		public int refreshFileList(){
			// 刷新文件列表
			File[] files = null;
			try{
				files = new File(path).listFiles();
			}
			catch(Exception e){
				files = null;
			}
			if(files==null){
				// 访问出错
				Toast.makeText(getContext(), sOnErrorMsg,Toast.LENGTH_SHORT).show();
				return -1;
			}
			if(list != null){
				list.clear();
			}
			else{
				list = new ArrayList<Map<String, Object>>(files.length);
			}
			
			// 用来先保存文件夹和文件夹的两个列表
			ArrayList<Map<String, Object>> lfolders = new ArrayList<Map<String, Object>>();
			ArrayList<Map<String, Object>> lfiles = new ArrayList<Map<String, Object>>();
			
			if(!this.path.equals(sRoot)){
				// 添加根目录 和 上一层目录
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", sRoot);
				map.put("path", sRoot);
				map.put("img", getImageId(sRoot));
				list.add(map);
				
				map = new HashMap<String, Object>();
				map.put("name", sParent);
				map.put("path", path);
				map.put("img", getImageId(sParent));
				list.add(map);
			}
			
			for(File file: files){
				if(file.isDirectory() && file.listFiles()!=null){
//					1、mnt                挂载点目录
//					2、etc                系统主要配置文件
//					3、system            Android 系统文件
//					4、sys                Linux 内核文件
//					5、proc                运行时文件
//					6、init.rc            启动脚本
//					7、default.prop        系统属性配置文件
//					8、data                用户程序目录
//					9、dev                设备文件
					// 添加文件夹
					if(!file.getPath().startsWith("/system") && !file.getPath().startsWith("/sys")){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("name", file.getName());
						map.put("path", file.getPath());
						map.put("img", getImageId(sFolder));
						lfolders.add(map);
					}
				}else if(file.isFile()){
					// 添加文件
					String sf = getSuffix(file.getName()).toLowerCase();
					if(suffix == null || suffix.length()==0 || (sf.length()>0 && suffix.indexOf("."+sf+";")>=0)){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("name", file.getName());
						map.put("path", file.getPath());
						map.put("img", getImageId(sf));
						lfiles.add(map);
					}
				}  
			}
			
			list.addAll(lfolders); // 先添加文件夹，确保文件夹显示在上面
			list.addAll(lfiles);	//再添加文件
			
			SimpleAdapter adapter = new SimpleAdapter(getContext(), list, R.layout.file_dialog_item, new String[]{"img", "name", "path"}, new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
			this.setAdapter(adapter);
			return files.length;
		}
		public int _nClickedCount = 0;
		public View _lastClieckView = null;
		public MyHandler _handler = new MyHandler();
		public final static int DOUBLE_CLICKED = 2;
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
			v.setBackgroundColor(222);
			if (v.equals(_lastClieckView)) {
				_nClickedCount++;
			} else {
				_nClickedCount = 1;
				Message msg = new Message();
				msg.what = DOUBLE_CLICKED;
				msg.obj = v;
				msg.arg1 = position;
				_handler.sendMessageDelayed(msg, 300);
			}
			_lastClieckView = v;
		}
		
		
		//2.接受消息
		class MyHandler extends Handler {
			// 子类必须重写此方法,接受数据
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case DOUBLE_CLICKED:
					onSelected(msg,_nClickedCount,msg.arg1);
					_nClickedCount = 0;
					_lastClieckView = null;
					break;
				}
			}
		}
		
		
		// 处理选择事件
		public  void onSelected(Message msg,int nClickedCount,int position) {
//			View v = (View) msg.obj;
			// 处理双击事件
			if(2 == nClickedCount){
				String filePath = (String) list.get(position).get("path");
				if(isFilePath(filePath)){
					path = null;
					//System.out.println("文件路径 ："+(String) list.get(position).get("path"));
					Fun_Setting.instance._tvImagePath.setText((String) list.get(position).get("path"));
					Fun_AnalogVideo.instance.setFilePath((String) list.get(position).get("path"));
					//APP.GetMainActivity().dismissDialog(dialogid); //android会在后台保存其状态
					//APP.GetMainActivity().removeDialog(dialogid); //关闭之后彻底的清除对象
					Fun_Setting.instance.dissDialog(dialogid);
					APP.SendMsg(R.layout.main, XMSG.SELECTED_FUN, Main.XV_NEW_WEB);
				}else{
					Toast.makeText(APP.GetMainActivity(), APP.GetString(R.string.file_tip), Toast.LENGTH_SHORT).show();
					this.refreshFileList();
				}
				
			}else{
				// 条目选择
				String pt = (String) list.get(position).get("path");
				String fn = (String) list.get(position).get("name");
				if(fn.equals(sRoot) || fn.equals(sParent)){
					// 如果是更目录或者上一层
					File fl = new File(pt);
					String ppt = fl.getParent();
					if(ppt != null){
						// 返回上一层
						path = ppt;
					}
					else{
						// 返回更目录
						path = sRoot;
					}
				}else{
					File fl = new File(pt);
					/*if(fl.isFile()){
						// 如果是文件
						((Activity)getContext()).dismissDialog(this.dialogid); // 让文件夹对话框消失
						
						// 设置回调的返回值
						Bundle bundle = new Bundle();
						bundle.putString("path", pt);
						bundle.putString("name", fn);
						// 调用事先设置的回调函数
						this.callback.callback(bundle);
						return;
					}*/
					if(fl.isDirectory()){
						// 如果是文件夹
						// 那么进入选中的文件夹
						path = pt;
					}
				}
				this.refreshFileList();
				
			}
		}
		
		//判断路径是否合法
		public boolean isFilePath(String path){
			boolean flag = false;
			if(path.equals("/storage")){
				flag = false;
			}else{
				flag = true;
			}
			return flag;
		}
		
		
		
		
	}
	
	
		
	
	
	
	
}