package com.views.analog.camera.audio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;

import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.zl.faad.AacDecoder;
//音频 AAC 解码
public class AudioQueue implements Runnable{
			
	private final static int MAX_SIZE = 5000;
	public static boolean runFlag;
	
	public static AudioTrack _talkAudio = null; //播放声音
	 // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025  
//    private static int sampleRateInHz = 32000;
//    // 设置音频的录制的声道CHANNEL_IN_STEREO 16 为双声道，CHANNEL_CONFIGURATION_MONO 2为单声道  
//    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;  
//    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。  
//    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;  
    
    private boolean isDecorderOpen = false; //判断只执行一次OPEN
    private long[] long_decoderRet;//解码OPEN返回值
    
    private AacDecoder aacdncoder;
	private long time;
	
//	String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
//	private String AudioName = filepath +"/音频TEST.pcm";  
//    FileOutputStream outsStream;
	    
	public AudioQueue(){
		runFlag = true;
		
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
	
	
	public static void init(int type){
		if(type == 1){//ipc 对应 8000采集 牛眼不变
			Constants.frequency = 8000;
		}
		int bufsize = AudioTrack.getMinBufferSize(Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding);
		if(_talkAudio == null)
			_talkAudio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding, bufsize, AudioTrack.MODE_STREAM);//
		//_talkAudio.setStereoVolume(0.5f, 0.5f);
		//_talkAudio.play();//开始
	}
	
	public static Queue<QueuePcmBean> _queue = new LinkedList<QueuePcmBean>();
	public static int _flag = 0;
	public static void addSound(byte[] data,int pcmType){
		if(_flag == 0){
			_flag = 1;
			init(pcmType);
		}
		try {
			synchronized (_queue) {
				if(_queue != null){
					if(_queue.size() < MAX_SIZE){
						_queue.offer(new QueuePcmBean(data,pcmType));			
					}
				}
			}	
		} catch (Exception e) {
		}
	}
	
	Thread _thread = null;
	public void Start() {
		try {
			synchronized (_queue) {
				aacdncoder = new AacDecoder();
				//_talkAudio.play();//开始
				if(_thread == null){
					_thread = new Thread(this);
				}
				_thread.start();
				isDecorderOpen = true;
			}
		} catch (Exception e) {
			System.out.println("打开语音对讲失败!");
		}
	}
	
	public void Stop() {
		try {
//			long t1= System.currentTimeMillis();
			synchronized (_queue) {
				runFlag = false;
				_thread = null;
				if(_talkAudio != null){
					_talkAudio.flush();
					_talkAudio.stop();
					_talkAudio = null;
				}
				isDecorderOpen = false;
				if(aacdncoder != null && long_decoderRet != null)					
					aacdncoder.Close(long_decoderRet[0],long_decoderRet[1],long_decoderRet[2]); //关闭解码
				aacdncoder = null;
				_flag = 0;
				while (_queue.size() > 0) {
					_queue.poll();
				}
			}
//			long t2= System.currentTimeMillis();
//			LogUtil.d("AudioQueue", "..音频退出..AudioQueue.stop()....time = "+(t2-t1));
		} catch (Exception e) {
			LogUtil.d("AudioQueue",ExceptionsOperator.getExceptionInfo(e));
		}
	}

	
	@Override
	public void run() {	
			while(runFlag){
				try {
//					synchronized (_queue) {
						if(_queue != null && _queue.size() > 0 && aacdncoder != null){
							QueuePcmBean bean = null;
							synchronized (_queue) {
								bean = _queue.poll();
							}
							if(null != bean.getData() && bean.getData().length > 0){
								if(bean.getPcmType() == 1){//IPC过来数据是PCM 直接去播放
									_talkAudio.write(bean.getData(), 0, bean.getData().length);//播放
									//outsStream.write(bean.getData());
								}else{
									//解码--牛眼需要解码AAC
			            			if(isDecorderOpen){
			            				isDecorderOpen = false;
			            				long_decoderRet = aacdncoder.Open(bean.getData());
			            			}
			            			if(long_decoderRet[0] != 0){
			            				byte[] b2 = aacdncoder.Write(long_decoderRet[0],long_decoderRet[1],long_decoderRet[2],bean.getData());
					    				if(b2 != null && b2.length > 0){
					    					//outsStream.write(b2);
					    					_talkAudio.write(b2, 0, b2.length);//播放
					    					try {
												Thread.sleep(1);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
					    				}
			            			}
								}
							}
			        		
						}
//					}
				} catch (Exception e) {
					LogUtil.e("AudioQueue",ExceptionsOperator.getExceptionInfo(e));
				}
			}
//			try {
//				Thread.sleep(time);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
	}	
	
	public static class QueuePcmBean{
		public byte[] data;
		public int pcmType;
		QueuePcmBean(){
		}
		QueuePcmBean(byte[] data,int pcmType){
			this.data = data;
			this.pcmType = pcmType;
		}
		
		public byte[] getData() {
			return data;
		}
		public void setData(byte[] data) {
			this.data = data;
		}
		public int getPcmType() {
			return pcmType;
		}
		public void setPcmType(int pcmType) {
			this.pcmType = pcmType;
		}
	}

	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
