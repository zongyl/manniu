package com.views.analog.camera.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.zl.faac.AacEncoder;
import P2P.SDK;
import android.os.Environment;
/**
 * @author: li_jianhua Date: 2015-6-8 上午8:55:26
 * To change this template use File | Settings | File Templates.
 * @Description：//音频采集 PCM 编码 -> AAC
 */
public class RecoderQueue implements Runnable{
			
	private final static int MAX_SIZE = 500;
	public static boolean runFlag;
	
	//AudioTrack _talkAudio; //播放声音
	 // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025  
//    private static int sampleRateInHz = 44100;
//    // 设置音频的录制的声道CHANNEL_IN_STEREO 16 为双声道，CHANNEL_CONFIGURATION_MONO 2为单声道  
//    private static int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;  
//    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。  
//    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;  
	
    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
//    private String AudioName = filepath +"/aac采集.aac";  
//    FileOutputStream outsStream;
    private AacEncoder aacEncoder;//编码
    private long[] longRet;
    
    
	private long time;
	public RecoderQueue(){
		runFlag = true;
		aacEncoder = new AacEncoder();
		longRet = aacEncoder.Open(1); //1单声道 2 双声道
		Constants.maxInputSamples = longRet[1];
		
//		try {
//			File file = new File(AudioName);
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
	
	public static Queue<byte[]> queue = new LinkedList<byte[]>();
	
	public static void addSound(byte[] data){
		synchronized (queue) {
			if(queue.size() < MAX_SIZE){
				queue.offer(data);			
			}
		}		
	}
	
	private Thread _thread = null;
	public void Start() {
		try {
			synchronized (queue) {
				//aacEncoder = new AacEncoder();
				//longRet = aacEncoder.Open(1); //1单声道 2 双声道
				if(_thread == null){
					_thread = new Thread(this);
				}
				_thread.start();
				LogUtil.i("RecoderQueue", "音频_thread id = "+_thread.getId());
			}
		} catch (Exception e) {
			System.out.println("打开语音对讲失败!");
		}
	}
	
	public void Stop() {
		try {
			synchronized (queue) {
				aacEncoder.Close(longRet[0]);//关闭编码
				runFlag = false;
				_thread = null;
				aacEncoder = null;
				while (queue.size() > 0) {
					queue.poll();
				}
			}
		} catch (Exception e) {
			return;
		}
	}
	 
	@Override
	public void run() {	
		while(true){
			if(runFlag){
				try {
					synchronized (queue) {
						if(queue != null && queue.size() > 0 && aacEncoder != null){
							byte[] _data = queue.poll();
							if(SDK._sessionId != 0 && SDK._createChnlFlag == 0){
								//开始编码
								byte[] data = aacEncoder.Write(longRet[0],(int)longRet[1],(int)longRet[2],_data);
								if(data != null && data.length > 0){
									//outsStream.write(data);
									//发送数据
				            		SDK.SendData(data,data.length,1,0,1);
//					            		LogUtil.d("SDK", "AAC发送音频数据..."+data.length+" -- "+data[0]+","+data[1]+","+data[2]+","+data[3]+","+data[4]+"------"+data[data.length-1]+","+data[data.length-2]+","+data[data.length-3]+","+data[data.length-4]+","+data[data.length-5]
//												+"------"+data[data.length-6]+","+data[data.length-7]+","+data[data.length-8]+","+data[data.length-9]+","+data[data.length-10]);
								}
							}
						}
					} 
				}catch (Exception e) {
					LogUtil.e("RecoderQueue",ExceptionsOperator.getExceptionInfo(e));
				}
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
