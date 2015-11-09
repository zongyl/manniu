package com.localsnap;
/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-8-7 上午8:50:45
 * To change this template use File | Settings | File Templates.
 * Description：打开相册  activity
 */

import java.io.File;
import java.util.List;
import java.util.Map;

import com.basic.APP;
import com.localsnap.ImageGridAdapter.Holder;
import com.localsnap.ImageGridAdapter.TextCallback;
import com.manniu.manniu.R;
import com.utils.Constants;
import com.views.Main;
import com.views.bovine.Fun_AnalogVideo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

@SuppressWarnings("unchecked")
public class Fun_ImgGridActivity extends Activity implements OnClickListener{
	private final String EXTRA_IMAGE_LIST = "imagelist";
	Context context = null;
	List<ImageItem> dataList;
	GridView gridView;
	GridView gridView2;
	ImageGridAdapter adapter;
	AlbumHelper helper;
	Button _btnBack,_btnDel,_btnAllSelect,_btnClose,_btnSelectDel;
	ViewFlipper _vf;		// View切换
	
	public static Fun_ImgGridActivity instance = null;	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置为无标题格式
		instance = this;
		setContentView(R.layout.fun_image_grid);
		
		_vf = (ViewFlipper) this.findViewById(R.id.viewflipper);
		_btnAllSelect = (Button) findViewById(R.id.btn_all_select);
		context = Fun_ImgGridActivity.this;
		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());
		TextView tv = (TextView)this.findViewById(R.id.picTitle);
		tv.setText(Constants._bucketName);
		//long startTime = System.currentTimeMillis();
		dataList = (List<ImageItem>) getIntent().getSerializableExtra(EXTRA_IMAGE_LIST);
		//long endTime = System.currentTimeMillis();
		//System.out.println("取dataList用时 ："+(startTime-endTime));
		initView();
//		findViewById(R.id.btn_img_back).setOnClickListener(this);
		_btnBack = (Button) this.findViewById(R.id.btn_img_back);
		_btnBack.setOnClickListener(this);
		_btnDel = (Button) findViewById(R.id.btn_img_del);
		_btnDel.setOnClickListener(this);
		
	}
	
	/**
	 * 初始化列表数据
	 */
	private void initView() {
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(Fun_ImgGridActivity.this, dataList,0);
		gridView.setAdapter(adapter);
		_btnAllSelect.setVisibility(View.GONE);
		//处理点击选择图片回调事件  取到选中的PATH集合 然后删除
		adapter.setTextCallback(new TextCallback() {
			public void onListen(int count,String path) {
				//System.out.println("back:"+path);
				//bt.setText("完成" + "(" + count + ")");
			}
		});
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//System.out.println("选择图片："+id+"-"+position);
				adapter.notifyDataSetChanged();
			}

		});
		//释放Bitmap资源
		
		
		

	}
	private void initView2() {
//		ImageGridAdapter.map.clear();
//		ImageGridAdapter.holderList.clear();
		//去掉选中的
		List<ImageItem> list = adapter.dataList;
		for (int i = 0; i < list.size(); i++) {
			ImageItem item = list.get(i);
			item.isSelected = false;
		}
		gridView2 = (GridView) findViewById(R.id.gridview2);
		gridView2.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(Fun_ImgGridActivity.this, dataList,1);
		gridView2.setAdapter(adapter);
		_btnClose = (Button) findViewById(R.id.btn_close);
		_btnSelectDel = (Button) findViewById(R.id.btn_delete);
		_btnAllSelect.setOnClickListener(this);
		_btnClose.setOnClickListener(this);
		_btnSelectDel.setOnClickListener(this);
		
		//处理点击选择图片回调事件  取到选中的PATH集合 然后删除
		adapter.setTextCallback(new TextCallback() {
			public void onListen(int count,String path) {
			}
		});
		gridView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//System.out.println("选择图片："+id+"-"+position);
				adapter.notifyDataSetChanged();
			}

		});
	}
	
	// 查询结果与查询条件View切换
	public int _index = 0;
	void ShowPage(int index){
		Animation rInAnim = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
		Animation rOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
		_vf.setInAnimation(rInAnim);
		_vf.setOutAnimation(rOutAnim);
		_vf.setDisplayedChild(index);
		if(index == 1){
			_btnDel.setVisibility(View.GONE);
			_btnAllSelect.setVisibility(View.VISIBLE);
		}else{
			_btnDel.setVisibility(View.VISIBLE);
			_btnAllSelect.setVisibility(View.GONE);
		}
		_index = index;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_img_back:
			//返回到相册页面
			Main.Instance.NewMainreLoadImg();
            Fun_ImgGridActivity.this.finish();
			break;
		case R.id.btn_img_del:
			initView2();
			ShowPage(1);
			break;
		case R.id.btn_all_select:
			
			List<ImageItem> list = adapter.dataList;
			for (int i = 0; i < list.size(); i++) {
				ImageItem item = list.get(i);
				item.isSelected = true;
				adapter.map.put(item.imagePath, item.imagePath);
			}
			List<Holder> hList = adapter.holderList;
			for (int i = 0; i < hList.size(); i++) {
				Holder holder = hList.get(i);
				holder.getSelected().setImageResource(R.drawable.icon_data_select);
				holder.getText().setBackgroundResource(R.drawable.bgd_relatly_line);
			}
			break;
		case R.id.btn_close:
			ShowPage(0);
			break;
		case R.id.btn_delete:
			deleteImgs();
			break;
		default:
			break;
		}
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {	// 处理返回按键
			onClick(_btnBack);
		}
		return true;
	}
	
	
	public void deleteImgs() {
		if (adapter.map.size() == 0) {
			Toast.makeText(this, getString(R.string.choosepic_alert), Toast.LENGTH_SHORT).show();
			return;
		}
		new AlertDialog.Builder(this).setTitle(getString(R.string.tip_title)).setMessage(getString(R.string.del_chose_ask))
				.setIcon(R.drawable.help)
				.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Map<String, String> map = adapter.map;
						for (Map.Entry<String, String> entry : map.entrySet()) {
							if (deleteSeletect(entry.getValue())) {
								for (int i = 0; i < dataList.size(); i++) {
									if (dataList.get(i).imagePath.equals(entry
											.getValue()))
										dataList.remove(i);
								}
							}
						}
						initView2();
						adapter.notifyDataSetChanged();
						adapter.map.clear();
						
						String path = Fun_AnalogVideo.ImagePath+""+BrowseAlbumActivity._path;
						File file = new File(path);
						File[] files = file.listFiles();
						if (files.length == 0) {//空文件夹
							APP.GetMainActivity().ShowXView(Main.XV_NEW_MAIN);
							Main.Instance.NewMainreLoadImg();
				            Fun_ImgGridActivity.this.finish();
							instance = null;
							context = null;
						}
						
					}
				}).setNegativeButton(getString(R.string.cancel), null).show();

	}
	
	public boolean deleteSeletect(String path) {
		if (path == "") {
			return false;
		}
		File file = new File(path);
		try {
			if (file.isFile() && file.exists()) {
				if (file.delete()) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	/*@Override
	protected void onStop() {
		super.onStop();
		instance = null;
		context = null;
	}*/
	
	@Override
    protected void onDestroy(){
		super.onDestroy();
		instance = null;
		context = null;
		if(dataList != null)
			dataList.clear();
		dataList = null;
		if(helper.bucketList != null)
			helper.bucketList.clear();
		helper.bucketList = null;
		if(adapter.map != null)
			adapter.map.clear();
		adapter.map = null;
		if(adapter.holderList != null)
			adapter.holderList.clear();
		adapter.holderList = null;
		for (int i = 0; i < adapter.grid_listbit.size(); i++) {
			if(!adapter.grid_listbit.get(i).isRecycled()){
				adapter.grid_listbit.get(i).recycle();  
			}
		}
		adapter.grid_listbit.clear();
		adapter.imageLoader.clearCache();
		adapter.imageLoader = null;
		adapter = null;
		helper = null;
		System.gc();
    }
	
}
