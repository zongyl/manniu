package net.majorkernelpanic.streaming.hw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;

import net.majorkernelpanic.streaming.rtp.H264Packetizer;

import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.NewSurfaceTest;
import com.views.bovine.Fun_AnalogVideo;
import com.zl.faac.AacEncoder;
import P2P.SDK;
import android.os.Environment;
import android.util.Log;
/**
 * @author: li_jianhua Date: 2015-8-28 上午8:55:26
 * To change this template use File | Settings | File Templates.
 * @Description：//视频软编码
 */
public class EnCoderQueue implements Runnable{
	protected final static String TAG = "EnCoderQueue";
	private final static int MAX_SIZE = 500;
	public static boolean runFlag;
	public boolean _isRecording = false;//录像
	public boolean _isEncord = false;//是否编码
	
    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //FileOutputStream outsStream;
    private String fileName = "";
    public RandomAccessFile raf = null;
    
	private long time;
	public EnCoderQueue(){
		runFlag = true;
		//time = 10;
		//aacEncoder = new AacEncoder();
		
//		try {
//			File file = new File(filepath+"/analog_test1.h264");
//			if(!file.exists()){
//				file.createNewFile();
//			}
//			outsStream = new FileOutputStream(file);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public void recordFile(String filePath){
        try {  
        	this.fileName = filePath;
            File file = new File(filePath);   
            if (file.exists()){
            	file.delete();  
            }  
            raf = new RandomAccessFile(file, "rw"); 
            _isRecording = true;
            if(_isEncord == false) _isEncord = true;
        } catch (Exception e) {   
            LogUtil.v(TAG, ExceptionsOperator.getExceptionInfo(e));   
        }
	}
	//录像结束 转MP4文件
	public void h264ToMp4(){
		try {
			if(fileName != null && !fileName.equals("")){
				int ret = SDK.Ffmpegh264ToMp4(fileName, fileName.replace(".h264", ".mp4"));
				if(ret == 0){
					File file = new File(fileName); 
					if(file.exists()) file.delete();
				}
			}
			if(raf != null){
				raf.close();
				raf = null;
			}
			i_flag = 0;
		} catch (Exception e) {
		}
	}
	
	public static Queue<byte[]> queue = new LinkedList<byte[]>();
	
	public static void addSound(byte[] data){
		synchronized (queue) {
			if(queue.size() < MAX_SIZE){
				queue.offer(data);			
			}
		}		
	}
	
	public Thread _thread = null;
	public void Start() {
		try {
			synchronized (queue) {
				runFlag = true;
				if(_thread == null){
					_thread = new Thread(this);
				}
				_thread.start();
				LogUtil.i(TAG, "视频_thread id = "+_thread.getId());
			}
		} catch (Exception e) {
			System.out.println("打开失败!");
		}
	}
	
	public void Stop() {
		try {
			synchronized (queue) {
				runFlag = false;
				_isRecording = false;
				_thread = null;
				while (queue.size() > 0) {
					queue.poll();
				}
			}
		} catch (Exception e) {
			return;
		}
	}
	
	public byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}
	
	byte[] newData = new byte[1024*750];
	int[] framerate = new int[1]; 
	private int i_flag = 0; //录像第一帧不I帧标识
	@Override
	public void run() {	
		while(runFlag){
			try {
				synchronized (queue) {
					if(queue != null && queue.size() > 0){
						byte[] _data = queue.poll();
						if(_isEncord){
							//开始编码
							int dataLenth = SDK.Ffmpegh264EnCoder(_data,_data.length,newData,framerate);
							//LogUtil.d(TAG,"end.."+dataLenth+":"+newData[0]+":"+newData[1]+":"+newData[2]+":"+newData[3]+":"+newData[4]+":"+newData[5]+":"+newData[20]+":"+newData[21]);
							if(dataLenth > 0){
								if(SDK._sessionId != 0 && SDK._createChnlFlag == 0){
//									outsStream.write(newData);
									int ret = SDK.SendData(newData,dataLenth,0,0,framerate[0]);
									if(ret < 0){
										LogUtil.e(TAG,"sdk.sendDate error....");
									}
									//LogUtil.d(TAG, "SDK.SendData....");
								}
								if(_isRecording){
									if(i_flag == 0 && framerate[0] == 1) i_flag = 1;
									if(i_flag == 1){
										byte[] newbuf = new byte[dataLenth-32];
										System.arraycopy(newData, 24, newbuf, 0, dataLenth-32);
										raf.write(newbuf);
									}
								}
							}
						}
					}
				} 
			}catch (Exception e) {
				LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}	
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
