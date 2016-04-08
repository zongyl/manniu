package com.localmedia;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MemoryCache {
	public Bitmap bitmap;
	public Bitmap newBit;
    private Map<String, SoftReference<Bitmap>> cache=Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());//杞紩鐢?
    
    public Bitmap get(String id){
        if(!cache.containsKey(id))
            return null;
        SoftReference<Bitmap> ref=cache.get(id);
        return ref.get();
    }
    
  //生成Bitmap缩量图 
  	public Bitmap setViewImage(String filePath) {
  		try {
  			FileInputStream fis = new FileInputStream(filePath);
  			BufferedInputStream bis = new BufferedInputStream(fis);
  			bitmap = BitmapFactory.decodeStream(bis);
  			newBit = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
  			bis.close();
  			fis.close();
  			return newBit;
  		} catch (Exception e) {
  			e.printStackTrace();
  			return null;
  		}
  	} 
  	
  	public Bitmap revitionImageSize(String path) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(
				new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			if ((options.outWidth >> i <= 256)
					&& (options.outHeight >> i <= 256)) {
				in = new BufferedInputStream(
						new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				in.close();//关闭流
				break;
			}
			i += 1;
		}
		return bitmap;
	}
    
    public void put(String id, Bitmap bitmap){
        cache.put(id, new SoftReference<Bitmap>(bitmap));
    }

    public void clear() {
        cache.clear();
    }
}
