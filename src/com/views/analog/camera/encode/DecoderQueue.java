package com.views.analog.camera.encode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import com.basic.G;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.NewMain;
import com.views.NewSurfaceTest;
import com.vss.vssmobile.decoder.Mp4Enc;

import P2P.SDK;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
/**
 * @author: li_jianhua Date: 2015-7-24 上午8:55:26
 * To change this template use File | Settings | File Templates.
 * @Description：//视频解码队列
 */
public class DecoderQueue implements Runnable{
			
	private final static int MAX_SIZE = 100;
	public boolean runFlag;
	public int i_flag = 0;//I帧标志位
	private int b_flag = 0;//录像也要从I帧开始
	
//    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
//    public static BufferedOutputStream outputStream;
//    static File f = new File(Environment.getExternalStorageDirectory(), "/IPC_test2.h264");
	private long time;
	public DecoderQueue(){
		runFlag = true;
		
//	    touch (f);
//	    try {
//	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
//	    } catch (Exception e){ 
//	        e.printStackTrace();
//	    }
	}
//	public void touch(File f) {
//		try {
//			if (!f.exists())
//				f.createNewFile();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	public static void writeFile(byte[] buffer){
//		try {
//			if(outputStream == null){
//				outputStream = new BufferedOutputStream(new FileOutputStream(f));
//			}
//			outputStream.write(buffer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	private Queue<QueueBean> queue = new LinkedList<QueueBean>();
	private Boolean _blnHaveIFrame = false;
	private int i = 0;
	public void addData(byte[] data,int length,int isIFrame){
		synchronized (queue) {			
			if(_thread == null) return;			
			if(isIFrame == 1)
			{
				 _blnHaveIFrame = true;
			}
			if(_blnHaveIFrame == true )
			{
				if(queue.size() < MAX_SIZE){
					if(i == 0){
						queue.offer(new QueueBean(data,length,isIFrame));	
						queue.offer(new QueueBean(data,length,isIFrame));	
						queue.offer(new QueueBean(data,length,isIFrame));	
						queue.offer(new QueueBean(data,length,isIFrame));	
						queue.offer(new QueueBean(data,length,isIFrame));	
						queue.offer(new QueueBean(data,length,isIFrame));
						i = 1;
					}
					queue.offer(new QueueBean(data,length,isIFrame));					
					runFlag = true;
				}
			}
		}		
	}
	
	Thread _thread = null;
	public void Start() {
		try {
			synchronized (queue) {
				runFlag = true;
				if(_thread == null){
					_thread = new Thread(this);
				}
				_thread.start();
			}
			//LogUtil.d("DecoderQueue", "....DecoderQueue.start()....");
		} catch (Exception e) {
			System.out.println("打开失败!");
		}
	}
	
	public void Stop() {
		try {
//			long t1= System.currentTimeMillis();
			synchronized (queue) {
				runFlag = false;
				_thread.interrupt(); //外围调用关闭
				_thread = null;
				i_flag = 0;
				clearQueue();
//				if(outputStream != null){
//					outputStream.close();
//					outputStream = null;
//				}
//				long t2= System.currentTimeMillis();
//				LogUtil.d("DecoderQueue", "..数据队列..DecoderQueue.stop()....time = "+(t2-t1));
			}
		} catch (Exception e) {
			return;
		}
	}
	
	public void clearQueue(){
		while (queue.size() > 0) {
			queue.poll();
		}
	}
	
	public boolean _isRecording = false;//录像
	private String fileName = "";//模拟录像要用到
	FileOutputStream outsStream;
    public RandomAccessFile raf = null;
	public Boolean _startSnap = false;//截图片发数据
	//写文件可以不用SPS PPS 头
	public void recordFile(String filePath){
        try {   
//            File file = new File(filePath);   
//            if (file.exists())   
//                file.delete();   
//            outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
//        	Log.i("DecoderQueue", ""+filePath);
//        	if(NewMain.devType == 4){
        		this.fileName = filePath;
                File file = new File(filePath + ".h264");   
                if (file.exists()){
                	file.delete();
                }
                raf = new RandomAccessFile(file, "rw");
//        	}else{
//        		Log.i("DecoderQueue", "1111..Mp4Enc.handle=="+Mp4Enc.handle);
//            	Mp4Enc.SetVideoFrameRate(Mp4Enc.handle, DecoderDebugger.FRAMERATE);
//    			Mp4Enc.SetVideoSize(Mp4Enc.handle, DecoderDebugger.width, DecoderDebugger.height);
//    			Log.i("DecoderQueue", "222");
//        	}
            _isRecording = true;
            Thread.sleep(100);
        } catch (Exception ex) {
            Log.v("System.out", ex.toString());   
        }
	}
	
	public void h264ToMp4(){
		try {
			if(fileName != null && !fileName.equals("")){
				int ret = SDK.Ffmpegh264ToMp4(fileName + ".h264","",fileName + ".mp4",0);
				if(ret == 0){
					File file = new File(fileName + ".h264"); 
					if(file.exists()) file.delete();
				}
			}
			if(raf != null){
				raf.close();
				raf = null;
			}
			b_flag = 0;
		} catch (Exception e) {
			LogUtil.e("DecoderQueue", ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	@Override
	public void run() {
//		while(true){
		while(runFlag){
				try {
					//synchronized (queue) {
						if(queue != null && queue.size() > 0){
							QueueBean bean = null;
							synchronized (queue) {
							bean = queue.poll();
							}
							if(bean != null)
							{
							if(i_flag == 0 && bean.getIsIFrame() == 1){
								i_flag = 1;
							}
							if(i_flag == 1){
								if(_startSnap && bean.getIsIFrame() == 1){//截图也去头
									if(SDK._manufactorType == 0){
										int exHead = (int)bean.getData()[22];
										if(exHead < 0) continue;
										int realHead = 24 + exHead;
										int realLen = bean.getLength() - realHead - 8;
										byte[] newbuf = new byte[realLen];
										System.arraycopy(bean.getData(), realHead, newbuf, 0, realLen);
										NewSurfaceTest.instance.h264DecoderSnapImg(newbuf, realLen);
									}else{
										NewSurfaceTest.instance.h264DecoderSnapImg(bean.getData(), bean.getLength());
									}
								}
								
								if(NewSurfaceTest.instance._decoderDebugger.isCanDecode()){//IPC 解码不动走软解    模拟不用
									if(NewMain.devType == 1){//IPC去头
										if(SDK._manufactorType == 0){
											//int exHead =  Integer.valueOf(G.byte2hex(bean.getData(), 22, 1));
											int exHead = (int)bean.getData()[22];
											if(exHead < 0) continue;
											int realHead = 24 + exHead;
											int realLen = bean.getLength() - realHead - 8;
											byte[] newbuf = new byte[realLen];
											System.arraycopy(bean.getData(), realHead, newbuf, 0, realLen);
											NewSurfaceTest.instance._decoderDebugger.decoder(newbuf, realLen);
										}else{
											NewSurfaceTest.instance._decoderDebugger.decoder(bean.getData(), bean.getLength());
										}
									}else{ //模拟不用去头
										/*byte[] newbuf = new byte[bean.getLength()-32];
										System.arraycopy(bean.getData(), 24, newbuf, 0, bean.getLength()-32);*/
										if(NewSurfaceTest.instance._decoderDebugger.decoder(bean.getData(), bean.getLength()) == -1) i_flag = 0;
									}
									
								}else{
									//软解码 直接送数据
									//NewSurfaceTest.instance.h264Decoder2(bean.getData(), bean.getLength());
								}
							}
						}else{
							setTime(10);
						}
						} 
				}catch (Exception e) {
					LogUtil.e("RecoderQueue",ExceptionsOperator.getExceptionInfo(e));
				}
			}
//		}
	}	
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	public class QueueBean{
		public byte[] data;
		public int length;
		public int isIFrame;
		QueueBean(){
		}
		QueueBean(byte[] data,int length,int isIFrame){
			this.data = data;
			this.length = length;
			this.isIFrame = isIFrame;
		}
		
		public byte[] getData() {
			return data;
		}
		public void setData(byte[] data) {
			this.data = data;
		}
		public int getLength() {
			return length;
		}
		public void setLength(int length) {
			this.length = length;
		}
		public int getIsIFrame() {
			return isIFrame;
		}
		public void setIsIFrame(int isIFrame) {
			this.isIFrame = isIFrame;
		}
		
	}
	
}
