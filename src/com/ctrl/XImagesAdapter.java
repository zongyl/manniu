package com.ctrl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import com.utils.Constants;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.view.View.OnClickListener;

public class XImagesAdapter extends BaseAdapter implements OnClickListener{
	final static int N_IMAGE_SIZE	= 8;
	ImageView[] _images = new ImageView[N_IMAGE_SIZE];
	ArrayList<String> _fileList = new ArrayList<String>();
	int _index = 0;
	ImageView _lastImage = null;
	final int SELECTED_ALPHA = 160;
	OnSelectedImageListener _listener;
	Context _context;
	public Bitmap bitmap;
	public Bitmap newBit;
	public XImagesAdapter(Context context) {
		_context = context;
		for (int i = 0; i < N_IMAGE_SIZE; ++i) {
			_images[i] = new ImageView(context);
			_images[i].setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
			_images[i].setAdjustViewBounds(true);
			_images[i].setScaleType(ImageView.ScaleType.FIT_XY); 
			//_images[i].setOnClickListener(this);
		}
		_lastImage = _images[1];
	}
	
	public void SetSelectedListener(OnSelectedImageListener listener){
		_listener = listener;
	}
	
	String GetSelectedImagePath() {
		return (String) _lastImage.getTag();
	}

	Bitmap _defBmp = null;
	public void SetDefultBmp(Bitmap defBmp){
		_defBmp = defBmp;
	}
	
	public boolean DeleteSeletect() {
		String path = GetSelectedImagePath();
		if (path == "") {
			return false;
		}
		File file = new File(path);
		try {
			if (file.isFile() && file.exists()) {
				if (file.delete()) {
					_lastImage.setImageBitmap(null);
					_lastImage.setTag("");
					return true;
				}
			}
		} catch (Exception e) {
		}

		return false;
	}
	
	public int UpdateList(String dir, String type, String query) {
		_fileList.clear();
		String path;
		File baseFile = new File(dir);
		if (baseFile != null && baseFile.exists()) {
			File[] file = baseFile.listFiles();
			if(file != null){
				for(int i = file.length - 1; i > -1; --i){
					path = file[i].getPath();
					if (file[i].isFile() && path.endsWith(type) && (query.equals("") || file[i].getName().contains(query))) {
						_fileList.add(path);
					}
				}
			}
		}
		this.notifyDataSetChanged();
		if(_fileList.size() > 0){
			//加载前清空最后一个选中的
			_lastImage.setPadding(0,0,0,0);
			_lastImage.setBackgroundColor(0);
			_images[0].setTag(_fileList.get(0));
			//OnSelected(_images[0]);
		}
		return _fileList.size();
	}
	
	public static Bitmap LoadImage(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Bitmap LoadImage(String pathName, int reqWidth, int reqHeight) {
		final BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;  
		BitmapFactory.decodeFile(pathName, opts);  
		opts.inSampleSize = GetSampleSize(opts.outWidth, opts.outHeight, reqWidth, reqHeight);
		opts.inJustDecodeBounds = false;  
		return BitmapFactory.decodeFile(pathName, opts);
	}

	public static int GetSampleSize(int srcWidth, int srcHeight, int reqWidth, int reqHeight) {
		int inSampleSize = 1;

		if (srcHeight > reqHeight || srcWidth > reqWidth) {
			if (srcWidth > srcHeight) {
				inSampleSize = Math.round((float) srcHeight / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) srcWidth / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	// get the number
	@Override
	public int getCount() {
		return _fileList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	// get the current selector's id number
	@Override
	public long getItemId(int position) {
		return position;
	}

	// create view method
	@Override
	public View getView(int position, View view, ViewGroup viewgroup) {
		ImageView iv = _images[position % N_IMAGE_SIZE];
		
		Bitmap bmp = null;
		String fileName = _fileList.get(position);
		iv.setTag(fileName);
		if (!(fileName.endsWith(".bmp") || fileName.endsWith(".jpg"))) {
			fileName = fileName.substring(0, fileName.length() - 3) + "bmp";
		}
		File file = new File(fileName);
		if(file.isFile()){
//			bmp = LoadImage(fileName, 200, 200);
			bmp = setViewImage(fileName);
		}else{
			bmp = _defBmp;
		}
		iv.setImageBitmap(bmp);
		iv.setOnClickListener(this);
		return iv;
	}
	
	//生成Bitmap缩量图 
	public Bitmap setViewImage(String filePath) {
		try {
			FileInputStream fis = new FileInputStream(filePath);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bitmap = BitmapFactory.decodeStream(bis);
			newBit = Bitmap.createScaledBitmap(bitmap, 230, 230, false);
			bis.close();
			fis.close();
			return newBit;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	} 

	void OnSelected(ImageView iv){
		if (!iv.equals(_lastImage)) {
			_lastImage.setPadding(0,0,0,0);
			_lastImage.setBackgroundColor(0);
			_lastImage.setAlpha(255);  //设置画笔的透明度  
			_lastImage = iv;
		}
		//iv.setAlpha(SELECTED_ALPHA); //选中的透明度 
		iv.setPadding(4,4,4,4);
		iv.setBackgroundColor(Constants.COLOR_SELECTED);
		if(_listener != null){
			_listener.OnSelectedImage(iv);
		}
	}
	
	//适配器中图片点击事件
	@Override
	public void onClick(View v) {	
		OnSelected((ImageView)v);
	}
}
