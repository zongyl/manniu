package com.localsnap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
/**
 * 浏览相册-- 的适配器类，主要用于加载图片
 *
 */
public class BrowseAlbumAdapter extends BaseAdapter {
	ArrayList<String> _fileList = new ArrayList<String>();
	private Context context;

	public BrowseAlbumAdapter(Context context) {
		this.context = context;
	}
	
	public int UpdateList(String dir) {
		_fileList.clear();
		String path;
		File baseFile = new File(dir);
		if (baseFile != null && baseFile.exists()) {
			File[] file = baseFile.listFiles();
			if(file != null){
				for(int i = file.length - 1; i > -1; --i){
					path = file[i].getPath();
					if (file[i].isFile()) {
						_fileList.add(path);
					}
				}
			}
		}
		this.notifyDataSetChanged();
		return _fileList.size();
	}

	@Override
	public int getCount() {
		return _fileList.size();
	}

	@Override
	public Object getItem(int position) {
		return _fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(_fileList.get(position));
			BufferedInputStream bis = new BufferedInputStream(fis);
			Bitmap bitmap = BitmapFactory.decodeStream(bis);
			BroImageView view = new BroImageView(context);
			view.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			Bitmap newbit = getBitmap(bitmap, BrowseAlbumActivity.screenWidth, BrowseAlbumActivity.screenHeight);
			view.setImageBitmap(newbit);
			fis.close();
			bis.close();
			return view;
		} catch (Exception e) {
			LogUtil.e("BrowseAlbumAdapter",ExceptionsOperator.getExceptionInfo(e));
			return null;
		} catch (Error e) {
			LogUtil.e("BrowseAlbumAdapter",ExceptionsOperator.getErrorInfo(e));
			return null;
		}
	}
	
	/* 
     * 得到图片字节流 数组大小 
     * */  
    public static byte[] readStream(FileInputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();        
        byte[] buffer = new byte[1024 * 1024];        
        int len = 0;        
        while( (len=inStream.read(buffer)) != -1){        
            outStream.write(buffer, 0, len);        
        }        
        outStream.close();        
        inStream.close();        
        return outStream.toByteArray();        
    } 
	@SuppressWarnings("unused")
	public static Bitmap getBitmap(Bitmap bitmap, int screenWidth,
			int screenHight) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scale = (float) screenWidth / w;
		float scale2 = (float) screenHight / h;
		// scale = scale < scale2 ? scale : scale2;
		// 保证图片不变形.
		matrix.postScale(scale, scale);
		// w,h是原图的属性.
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
	}
	
	

}
