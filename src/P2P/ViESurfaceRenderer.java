/*
 *  Copyright (c) 2012 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package P2P;

// The following four imports are needed saveBitmapToJPEG which
// is for debug only
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.*;

import com.bean.Monitor;
import com.utils.LogUtil;
import com.views.Fun_RealPlayerActivity;
import com.views.Fun_RecordPlay;
import com.views.NewMain;
import com.views.NewSurfaceTest;
import com.views.analog.camera.encode.DecoderDebugger;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.widget.TextView;

public class ViESurfaceRenderer{

    private final static String TAG = "WEBRTC";
   // public static Activity activity;
    // the bitmap used for drawing.
    public static Bitmap bitmap = null;
    public static Lock lckBitmap = new ReentrantLock();
    public ByteBuffer byteBuffer = null;
    public int _witdh = 0;
    public int _height = 0;
    public static SurfaceHolder surfaceHolder = null;
    // Rect of the source bitmap to draw
    public static Rect srcRect = new Rect();
    // Rect of the destination canvas to draw to
    public static Rect dstRect = new Rect();
    private float dstTopScale = 0;
    public static float dstBottomScale = 1;
    private float dstLeftScale = 0;
    public static float dstRightScale = 1;
    
    /*public ViESurfaceRenderer() {
    	File f = new File(Environment.getExternalStorageDirectory(), "/1.rgb");
        File f2 = new File(Environment.getExternalStorageDirectory(), "/2.rgb");
	    touch (f);
	    touch (f2);
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
	        outputStream2 = new BufferedOutputStream(new FileOutputStream(f2));
	        Log.i("AvcEncoder", "outputStream initialized");
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
    }*/

    // surfaceChanged and surfaceCreated share this function
    public static void changeDestRect(int dstWidth, int dstHeight) {
    	   	
        dstRect.right = (int)(dstRect.left + dstRightScale * dstWidth);
        dstRect.bottom = (int)(dstRect.top + dstBottomScale * dstHeight);
        
        Log.d("change dst rect ","dstRect.right :"+ dstRect.right+ 
        					" dstRect.left:"+dstRect.left+" dstRightScale"+ dstRightScale+
        					" dstwidth:"+dstWidth+" dstRect.bottom"+dstRect.bottom+ "dstRect.top"+
        					" dstBottomScale:"+dstBottomScale+" dstHeight "+dstHeight);
       
    }
    /*private static BufferedOutputStream outputStream,outputStream2;
     * public static void touch(File f) {
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

/*    @Override
	public void surfaceChanged(SurfaceHolder holder, int format,
            int in_width, int in_height) {
     
    	Canvas canvas = surfaceHolder.lockCanvas();
    	
        changeDestRect(in_width, in_height);
        Log.d(TAG, "ViESurfaceRender::surfaceChanged" +
                " in_width:" + in_width + " in_height:" + in_height +
                " srcRect.left:" + srcRect.left +
                " srcRect.top:" + srcRect.top +
                " srcRect.right:" + srcRect.right +
                " srcRect.bottom:" + srcRect.bottom +
                " dstRect.left:" + dstRect.left +
                " dstRect.top:" + dstRect.top +
                " dstRect.right:" + dstRect.right +
                " dstRect.bottom:" + dstRect.bottom);
        
		 if(canvas != null) {
		    surfaceHolder.unlockCanvasAndPost(canvas);
		 }
		 
		 if(NewSurfaceTest.instance != null){
	        NewSurfaceTest.instance.isScreenChange();
		 }
    }

    @Override
	public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = surfaceHolder.lockCanvas();
        if(canvas != null) {
            Rect dst = surfaceHolder.getSurfaceFrame();
            if(dst != null) {
                changeDestRect(dst.right - dst.left, dst.bottom - dst.top);
                Log.d(TAG, "ViESurfaceRender::surfaceCreated" +
                        " dst.left:" + dst.left +
                        " dst.top:" + dst.top +
                        " dst.right:" + dst.right +
                        " dst.bottom:" + dst.bottom +
                        " srcRect.left:" + srcRect.left +
                        " srcRect.top:" + srcRect.top +
                        " srcRect.right:" + srcRect.right +
                        " srcRect.bottom:" + srcRect.bottom +
                        " dstRect.left:" + dstRect.left +
                        " dstRect.top:" + dstRect.top +
                        " dstRect.right:" + dstRect.right +
                        " dstRect.bottom:" + dstRect.bottom);
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }*/

    public static void ResetSurfaceHolder(Activity activity) {
//		if (view == null) {
//			surfaceHolder = null;
//			return;
//		}
		
//		if(_recordHandler == null){
//			mAPP = (BaseApplication) activity.getApplication();
//			// 获得该共享变量实例
//			_recordHandler = mAPP.getRecordHandler();
//			System.out.println(_recordHandler);
//		}
		
		// if(! surfaceHolder.isCreating()){
//			surfaceHolder = view.getHolder();
//
//			Log.d(TAG, "ViESurfaceRenderer::ResetSurfaceHolder");
//
//			if (surfaceHolder == null) {
//				return;
//			}
//			Log.d(TAG, "ViESurfaceRenderer::ResetSurfaceHolder end");
//			surfaceHolder.addCallback(this);
//			surfaceCreated(view.getHolder());
		//}
	}
    
    public static void DestroySurfaceHolder(){
    	surfaceHolder = null;
    }
    
  /*  @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "ViESurfaceRenderer::surfaceDestroyed");
    }*/

    public Bitmap CreateBitmap(int width, int height) {
        LogUtil.d(TAG, "CreateByteBitmap " + width + ":" + height);
        if (bitmap == null) {
            try {
                android.os.Process.setThreadPriority(
                    android.os.Process.THREAD_PRIORITY_DISPLAY);
            }
            catch (Exception e) {
            }
        }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        srcRect.left = 0;
        srcRect.top = 0;
        srcRect.bottom = height;
        srcRect.right = width;
        
        //changeDestRect(width,height);
      
        return bitmap;
    }

    public static Map<Long,Videobitmap> _bufferAndBitmap = new HashMap<Long,Videobitmap>();//通道号ID和 bitmap、bytebuffer对应关系 MAP
    
    public ByteBuffer CreateByteBuffer(int width, int height,long deviceChannelID) {
    	LogUtil.d(TAG, "CreateByteBuffer " + width + ":" + height+"--"+deviceChannelID);
        //if (bitmap == null) {
            byteBuffer = ByteBuffer.allocateDirect(width * height * 2);
        //}
        _witdh = width;
        _height = height;
        if(NewMain.instance != null && NewMain.instance.viewPager.getCurrentItem() == 2){//多画面
        	Bitmap bitmap = CreateBitmap(_witdh, _height);
            Videobitmap videobitmap =  _bufferAndBitmap.get(deviceChannelID);
            if(videobitmap == null){
            	_bufferAndBitmap.put(deviceChannelID, new Videobitmap(byteBuffer,bitmap));
            }
        }
        return byteBuffer;
    }

    public class Videobitmap {
    	ByteBuffer _byteBuffer;
    	Bitmap _bitmap;
    	Videobitmap(ByteBuffer byteBuffer,Bitmap bitmap){
    		this._byteBuffer = byteBuffer;
    		this._bitmap = bitmap;
    	}
	}
    
    public native void DrawBitMapCallback();
    public native void DrawBitMapDestoryCallback();

    public void SetCoordinates(float left, float top,
            float right, float bottom) {
        Log.e(TAG, "Enter SetCoordinates " + left + "," + top + ":" + right + "," + bottom);
        
        dstLeftScale = left;
        dstTopScale = top;
        dstRightScale = right;
        dstBottomScale = bottom;
        
        changeDestRect(surfaceHolder.getSurfaceFrame().width(),surfaceHolder.getSurfaceFrame().height());
      
    }

    // It saves bitmap data to a JPEG picture, this function is for debug only.
    private void saveBitmapToJPEG(int width, int height) {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteOutStream);
        try{
            FileOutputStream output = new FileOutputStream(String.format("/sdcard/render_%d.jpg", System.currentTimeMillis()));
            output.write(byteOutStream.toByteArray());
            output.flush();
            output.close();
        }
        catch (FileNotFoundException e) {
        }
        catch (IOException e) {
        }
    }

    public static int _flag = 0;
    /**
     * m_encoderType 12:H265 其它是0
     * deviceid: P2PConnect传入的标识ID
     * realLen 报警回放的 实时长度
     */
    public void DrawByteBuffer(int realLen,long deviceChannelID,int encoderType) {
    	try {
    		if(NewMain.instance != null && NewMain.instance.viewPager.getCurrentItem() == 2){//多画面
    			Videobitmap videobitmap =  _bufferAndBitmap.get(deviceChannelID);
        		if(videobitmap != null){
        			if(encoderType == 12 || SDK.GetChannelDecoderType(deviceChannelID) == true){//H265的解码处理,只能走软解,需要考虑每个窗口走的解码方式会不一样		
            			Monitor monitor = SDK.GetChannelMonitor(deviceChannelID);	
            			//System.out.println("TEST DrawByteBuffer  ReceiveData 1111111111111111"+" deviceChannelID = "+deviceChannelID+" sid = "+monitor.getDevCart().getDeviceInfo().sid+" devName= "+monitor.getDevCart().getDeviceInfo().devname);	
            			if(monitor == null) return;
            			// 计算流量
    					monitor.setCurChnDataFlow(realLen);
    					
    					//多画面时 如果放大了 别的窗口不用调画图片方法
						if(Fun_RealPlayerActivity.instance.m_isFullView && Fun_RealPlayerActivity.instance.m_monitorsList.size() > 0){
							if(monitor != Fun_RealPlayerActivity.instance.m_chooseMonitor){
								return;
							}
						}
            			Lock lock = monitor.getDecoder_lock();
        				lock.lock();
            			if(videobitmap._byteBuffer == null)
                            return;
            			videobitmap._byteBuffer.rewind();
            			videobitmap._bitmap.copyPixelsFromBuffer(videobitmap._byteBuffer);
            			
            			//截图
            			if(Fun_RealPlayerActivity.instance.isShot && SDK._shotContext == SDK.GetChannelPlayContext(deviceChannelID)){
            				Fun_RealPlayerActivity.instance.screenshot(videobitmap._bitmap);
                    	}
            			
            			if(monitor.getAVDecoder()!=null){
            				if(monitor.getAVDecoder().isCanDecode() == true){
            					monitor.getAVDecoder().close();
            				}
            				//monitor.getAVDecoder().release();
            			}
            			if(monitor.getSoftDecoder()!=null){
            				monitor.getSoftDecoder().AddDecoderData(videobitmap._bitmap);
            			}
            			lock.unlock();
            			return;
                    }
        		}
    		}else{//设备、报警 视频
    			if(byteBuffer == null)
                    return;
        		byteBuffer.rewind();
        		lckBitmap.lock();
        		if(bitmap == null){
        			bitmap = CreateBitmap(_witdh, _height);
        		}
                bitmap.copyPixelsFromBuffer(byteBuffer);
                lckBitmap.unlock();
                
                if(Fun_RecordPlay.instance != null && !Fun_RecordPlay.instance.isClose){
                	Fun_RecordPlay.instance.byteLength += realLen;
                	if(Fun_RecordPlay.instance.isClose)
                		return;
                	if(Fun_RecordPlay.instance.flag == 0){
                		Fun_RecordPlay.instance.flag = 1;
        				Fun_RecordPlay.instance.closeFirst();
        			}
            		Fun_RecordPlay.instance.setBitmap();
            		return;
                }else if(NewSurfaceTest.instance != null && NewSurfaceTest.isPlay){
                	if(NewSurfaceTest.instance._snapImg){
                		NewSurfaceTest.instance.screenshot(bitmap);
                	}
                	if(NewSurfaceTest._paly == 1 && _flag == 0){//远程回放 第一次打开关闭加载框
                		_flag = 1;
                		NewSurfaceTest.instance.closeWait();
                	}
                	NewSurfaceTest.instance.setBitmap();
                	return;
                }
    		}
		} catch (Exception e) {
			System.out.println(11);
		}
    }

    public void DrawBitmap() {
		if (bitmap == null || surfaceHolder == null)
			return;
		lckBitmap.lock();
		Canvas canvas = surfaceHolder.lockCanvas();
		if (canvas != null) {
			canvas.drawBitmap(bitmap, srcRect, dstRect, null);
//			 Rect rect = m_rect;
//			 canvas.drawBitmap(bitmap, null, rect, null);
			surfaceHolder.unlockCanvasAndPost(canvas);

		} else {
			Log.e("udx", "draw bitmap invalid holder");
		}
		lckBitmap.unlock();
	}

}
