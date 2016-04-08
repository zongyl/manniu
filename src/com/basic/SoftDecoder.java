package com.basic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.bean.Monitor;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

import P2P.SDK;
import P2P.ViESurfaceRenderer;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.support.v4.util.TimeUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

@SuppressLint({ "InlinedApi", "NewApi" })
public class SoftDecoder {	
	
	private static Monitor monitor = null;
	private SurfaceHolder surfaceHolder= null;
	
	private byte[]  bmpBuff=null;
	private ByteBuffer byteBuffer =null; 
	private Bitmap bmp=null;	 
	private Rect rect = null;
	private Lock lock = new ReentrantLock();

	public SoftDecoder() {

	}

	public void SetMonitor(Monitor monitor) {
		this.monitor = monitor;
		if (this.monitor != null && this.monitor.getTileView() != null) {
			surfaceHolder = this.monitor.getTileView().getHolder();
			System.out.println("2016.03.28TEST  this.monitor.getTileView().getHolder()");
		}
	}

	public void SetSurfaceHolder(SurfaceHolder sf) {
		surfaceHolder = sf;
	}
	
	
	
	public SoftDecoder(Monitor monitor) {
		SetMonitor(monitor);
	}
	private int oldWidth =0;
	private int oldHeight= 0;
	private boolean haveNewData = false;
	public void AddDecoderData(byte[] data,int nLength,int nWidth,int nHeight){
		//只做数据拷贝工作
		if(nLength == 0){
			return;
		}
		/*long t1 = System.currentTimeMillis();
		if(bmpBuff == null || nWidth != oldWidth || nHeight!= oldHeight)
		{
			bmpBuff = new byte[nWidth * nHeight * 2];			
		}
		if (bmpBuff != null) {
			System.arraycopy(data, 0, bmpBuff, 0, nWidth
					* nHeight * 2);
			byteBuffer = ByteBuffer.wrap(data);
			oldWidth = nWidth;
			oldHeight = nHeight;
			haveNewData=true;
		}	
		long t2 = System.currentTimeMillis();
		System.out.println("20160324TEST  AddDecoderData TimeMillis:"+(t2-t1));
		DrawH265();*/
		
	}
	public void AddDecoderData(Bitmap bitmap){
//		Monitor monitor = SDK.GetChannelMonitor(deviceChannelID);
//		Lock lock = monitor.getDecoder_lock();
//		lock.lock();
		if (bitmap == null) return;
		try {
			Canvas videoCanvas = surfaceHolder.lockCanvas();
			if (videoCanvas != null) {
				Rect rect = monitor.getDrawRect();
				videoCanvas.drawBitmap(bitmap, null, rect, null);
			}
			if (videoCanvas != null) {
				surfaceHolder.unlockCanvasAndPost(videoCanvas);
				videoCanvas = null;
			}
		} catch (Exception e) {
			//LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
		//bmp = bitmap;
		//DrawBitmap();
		//lock.unlock();
	}
	
	public void DrawBitmap() {
		if (bmp == null || surfaceHolder == null)
			return;
		//lock.lock();
		Canvas videoCanvas = null;
		try {
			videoCanvas = surfaceHolder.lockCanvas();
			if (videoCanvas != null) {
				rect = monitor.getDrawRect();
				videoCanvas.drawBitmap(bmp, null, rect, null);
//				 Rect rect = m_rect;
//				 canvas.drawBitmap(bitmap, null, rect, null);
//				surfaceHolder.unlockCanvasAndPost(canvas);
			} else {
				Log.e("udx", "draw bitmap invalid holder");
			}
		} catch (Exception e) {
		}finally {
			//monitor.setImageBitmap(bmp);
			if (videoCanvas != null) {
				surfaceHolder.unlockCanvasAndPost(videoCanvas);
				videoCanvas = null;
			}
		}
		//lock.unlock();
	}
	protected void destroy() {

		StopDecoder();
		
     }
	private void DrawH265()
	{	
		long t1 = System.currentTimeMillis();
		if(bmp==null)
		{
			bmp = Bitmap.createBitmap(oldWidth, oldHeight,
				android.graphics.Bitmap.Config.RGB_565);
		}
		if (bmpBuff != null) {
			bmp.copyPixelsFromBuffer(byteBuffer);			
		}
		haveNewData = false;
		if(surfaceHolder == null)
		{
			System.out.println("2016.03.28TEST  surfaceHolder == null");
		}
		if(monitor.getAVDecoder()!=null)
		{
			monitor.getAVDecoder().release();
//			monitor.setAVDecoder(null);
		}
		Canvas videoCanvas = null;
		try {			
			rect = null;
			videoCanvas = surfaceHolder.lockCanvas();
			if (videoCanvas != null) {
				rect = monitor.getDrawRect();
				videoCanvas.drawColor(Color.BLACK);
				videoCanvas.drawBitmap(bmp, null, rect, null);
			}
			else
			{
				surfaceHolder.addCallback(null);
				surfaceHolder.removeCallback(null);				
				System.out.println("20160328TEST  surfaceHolder.lockCanvas()==null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			monitor.setImageBitmap(bmp);
			if (videoCanvas != null) {
				surfaceHolder.unlockCanvasAndPost(videoCanvas);
				videoCanvas = null;
			}
//			bmp.recycle();
		}
		long t2 = System.currentTimeMillis();
		System.out.println("20160324TEST  DrawH265 TimeMillis:"+(t2-t1));
	}
	
	private boolean m_ThreadRun = false;
	public void StartDecoder()
	{
		if(1==2)
		{
			m_ThreadRun = true;		
			new Thread() {
				@Override
				public void run() {				
					while(m_ThreadRun)
					{
						//解码处理
						if(oldWidth == 0 || oldHeight == 0  || haveNewData == false )
						{			
							//空转
						}
						else
						{
	//						lock.lock();
	//						bmp = Bitmap.createBitmap(oldWidth, oldHeight,
	//								android.graphics.Bitmap.Config.RGB_565);
	//						if (bmpBuff != null) {
	//							bmp.copyPixelsFromBuffer(byteBuffer);
	//						}
	//						haveNewData = false;
	//						lock.unlock();
	//						Canvas videoCanvas = null;
	//						try {
	//							rect = null;
	//							videoCanvas = surfaceHolder.lockCanvas();
	//							if (videoCanvas != null) {
	//								rect = monitor.getDrawRect();
	//								videoCanvas.drawColor(Color.BLACK);
	//								videoCanvas.drawBitmap(bmp, null, rect, null);
	//							}
	//						} catch (Exception e) {
	//							e.printStackTrace();
	//						} finally {
	//							monitor.setImageBitmap(bmp);
	//							if (videoCanvas != null) {
	//								surfaceHolder.unlockCanvasAndPost(videoCanvas);
	//								videoCanvas = null;
	//							}
	//						}
//							DrawH265();
							DrawBitmap();
						}						
					}				
				};
			}.start();
		}		
	}
	
	public void StopDecoder()
	{
		m_ThreadRun=false;
	}	
//	private static void drawH265(byte[] outBytes,int length, 
//			int width_frame, int height_frame, SurfaceHolder surfaceHolder,
//			Monitor monitor ) {
//		Bitmap bmp = null;
//		if(width_frame == 0 || height_frame == 0 || length ==0 )
//		{			
//			return;
//		}
//		else
//		{
//			byte[]  bmpBuff = new byte[width_frame * height_frame * 2];
//
//			bmp = Bitmap.createBitmap(width_frame, height_frame,
//					android.graphics.Bitmap.Config.RGB_565);
//
//			if (bmpBuff != null) {
//				System.arraycopy(outBytes, 0, bmpBuff, 0, width_frame
//						* height_frame * 2);
//				ByteBuffer byteBuffer = ByteBuffer.wrap(outBytes);
//				bmp.copyPixelsFromBuffer(byteBuffer);
//			}
//		}
//	
//		Matrix matrix = new Matrix();
//		Canvas videoCanvas = null;
//		try {
//			Rect rect = null;		
////			if(monitor.getAVDecoder() != null)
////			{
////				monitor.getAVDecoder().release();
////				monitor.setAVDecoder(null);
////			}
////			if(m_avdecoder!=null)
////			{
////				m_avdecoder.release();
////				m_avdecoder=null;
////			}
////			SurfaceHolder surfaceHolder1 = m_chooseMonitor.tileView
////												.getHolder();
//			videoCanvas = surfaceHolder.lockCanvas();
////			videoCanvas = surfaceHolder1.lockCanvas();
//			if (videoCanvas != null) {				
//				
////				if (!m_isFullView)
//					rect = monitor.getDrawRect();
////				else
////					rect = m_fullViewRect;
////				matrix.setScale(rate, rate, mid.x, mid.y);
////				if (rate > 1) {
////					// matrix.preTranslate( mapCenter.x+(end.x -
////					// start.x)-newBitmap.getWidth()/2 ,
////					// mapCenter.y+(end.y - start.y)-newBitmap.getHeight()/2 );
////					Bitmap newBitmap = ResizeBitmap(bmp,rect.width(),rect.height());
//////					matrix.postTranslate(end.x - start.x, end.y - start.y);
////					videoCanvas.drawRect(rect, new Paint());
////					videoCanvas.drawColor(Color.BLACK);
////					videoCanvas.drawBitmap(newBitmap, matrix, null);
////				} else {
//					videoCanvas.drawColor(Color.BLACK);
//					videoCanvas.drawBitmap(bmp, null, rect, null);
////				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			monitor.setImageBitmap(bmp);
//			if (videoCanvas != null) {
//				surfaceHolder.unlockCanvasAndPost(videoCanvas);
//				videoCanvas = null;
//			}
//			// bmp.recycle();
//		}
//	}
}
