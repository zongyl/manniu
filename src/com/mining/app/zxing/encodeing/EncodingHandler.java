package com.mining.app.zxing.encodeing;

import java.util.Hashtable;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class EncodingHandler {
	
	/**
	 * 这里调用MultiFormatWriter来返回一个关于QRCode的BitMatrix。
    	然后根据BitMatrix生成一个pixels，并转换为bitmap，此时已生成Bitmap形式的QRCode
	 */
	private static final int BLACK = 0xff000000;
	
	public static Bitmap createQRCode(String str, int widthAndHeight) throws WriterException{
        if (str == null || "".equals(str) || str.length() < 1){
            return null;
        }
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

		//图像数据转换，使用了矩阵转换
		BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if (matrix.get(x, y)){
					pixels[y * width + x] = BLACK;
				}
			}
		}
		//生成二维码图片的格式，使用ARGB_8888
		Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
	}	
}