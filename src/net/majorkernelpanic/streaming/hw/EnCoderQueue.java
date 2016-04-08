package net.majorkernelpanic.streaming.hw;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import P2P.SDK;
import android.os.Environment;
/**
 * @author: li_jianhua Date: 2015-8-28 上午8:55:26
 * To change this template use File | Settings | File Templates.
 * @Description：//视频软编码
 */
public class EnCoderQueue implements Runnable{
	protected final String TAG = "EnCoderQueue";
	public static boolean runFlag;
	public boolean _isRecording = false;//录像
	public boolean _isEncord = false;//是否编码
	
    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //FileOutputStream outsStream;
    private String fileName = "";
    public RandomAccessFile raf = null;
    
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
				int ret = SDK.Ffmpegh264ToMp4(fileName,fileName.replace(".h264", ".aac"), fileName.replace(".h264", ".mp4"),1);
				if(ret == 0){
					File file = new File(fileName); 
					if(file.exists()) file.delete();
					File file2 = new File(fileName.replace(".h264", ".aac"));
					if(file2.exists()) file2.delete();
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
	
	private byte[] tempData;
	private Lock lock = new ReentrantLock();
	public void addSound(byte[] data){
		lock.lock();
		tempData = data;
		lock.unlock();
	}
	
	public Thread _thread = null;
	public void Start() {
		try {
			runFlag = true;
			if(_thread == null){
				_thread = new Thread(this);
			}
			_thread.start();
		} catch (Exception e) {
		}
	}
	
	public void Stop() {
		try {
			runFlag = false;
			_isRecording = false;
			_thread = null;
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
	private int isSend = 0;//是否发送数据
	@Override
	public void run() {
		while(runFlag){
			try {
				if(tempData != null && tempData.length > 0){
					if(_isEncord){
						if(SDK._sessionIdContext != 0 && SDK._createChnlFlag == 0){
							isSend = 1;
						}
						lock.lock();
						byte[] myData = tempData;
						lock.unlock();
						//开始编码
						long t1 = System.currentTimeMillis();
						int dataLenth = SDK.Ffmpegh264EnCoder(myData,myData.length,newData,framerate,0,isSend);
						long t2 = System.currentTimeMillis();
						//LogUtil.d(TAG,"end.."+dataLenth+":"+newData[0]+":"+newData[1]+":"+newData[2]+":"+newData[3]+":"+newData[4]+":"+newData[5]+":"+newData[20]+":"+newData[21]);
						if(dataLenth > 0 && _isRecording){
							if(i_flag == 0 && framerate[0] == 1) i_flag = 1;
							if(i_flag == 1){
//								byte[] newbuf = new byte[dataLenth-36];
//								System.arraycopy(newData, 28, newbuf, 0, dataLenth-36);
								int exHead = (int)newData[22];
								int realHead = 24 + exHead;
								int realLen = dataLenth - realHead - 8;
								byte[] newbuf = new byte[realLen];
								System.arraycopy(newData, realHead, newbuf, 0, realLen);
								raf.write(newbuf);
							}
						}
						if((t2-t1) < 100){
							Thread.sleep(100-(t2-t1));
						}
					}
				}
			}catch (Exception e) {
				LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}
	
	
	
	
}
