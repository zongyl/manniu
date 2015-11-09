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
import com.zl.faad.AacDecoder;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
//音频 AAC 解码
public class AudioQueue implements Runnable{
			
	private final static int MAX_SIZE = 5000;
	public static boolean runFlag;
	
	static AudioTrack _talkAudio = null; //播放声音
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
	
	String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
	//private String AudioName = filepath +"/aac播放.aac";  
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
	
	static{
		int bufsize = AudioTrack.getMinBufferSize(Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding);
		if(_talkAudio == null)
			_talkAudio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding, bufsize, AudioTrack.MODE_STREAM);//
		//_talkAudio.setStereoVolume(0.5f, 0.5f);
	}
	
	/*public void init(){
		int bufsize = AudioTrack.getMinBufferSize(Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding);
		if(_talkAudio == null)
			_talkAudio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding, bufsize, AudioTrack.MODE_STREAM);//
		//_talkAudio.setStereoVolume(0.5f, 0.5f);
	}*/
	
	public static Queue<byte[]> queue = new LinkedList<byte[]>();
	
	public static void addSound(byte[] data){
		try {
			synchronized (queue) {
				if(queue != null)
				{
					if(queue.size() < MAX_SIZE){
						queue.offer(data);			
					}
				}
			}	
		} catch (Exception e) {
		}
	}
	
	Thread _thread = null;
	public void Start() {
		try {
			synchronized (queue) {
				aacdncoder = new AacDecoder();
				_talkAudio.play();//开始
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
			synchronized (queue) {
				runFlag = false;
				_thread = null;
				_talkAudio.flush();
				_talkAudio.stop();
				//_talkAudio = null;
				isDecorderOpen = false;
				if(aacdncoder != null)					
					aacdncoder.Close(long_decoderRet[0],long_decoderRet[1],long_decoderRet[2]); //关闭解码
				aacdncoder = null;
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
			while(runFlag){
				try {
					synchronized (queue) {
						if(queue != null && queue.size() > 0 && aacdncoder != null){
							//setTime(1);
							byte[] _data = queue.poll();
							if(null != _data && _data.length > 0){
								//解码
		            			if(isDecorderOpen){
		            				isDecorderOpen = false;
		            				long_decoderRet = aacdncoder.Open(_data);
		            			}
		            			if(long_decoderRet[0] != 0){
		            				byte[] b2 = aacdncoder.Write(long_decoderRet[0],long_decoderRet[1],long_decoderRet[2],_data);
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
			        		
						}/*else{
							setTime(10);
						}*/
					}
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

	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
