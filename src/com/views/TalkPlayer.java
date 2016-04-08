package com.views;

import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.media.audio.IAudioData;
import com.media.audio.PCMAudioRecorder;
import com.utils.Constants;
/**
 * 对讲Player
 */
public class TalkPlayer implements Runnable, IAudioData {
	AudioTrack _audio;
	int _talkId = 0;
	ByteBuffer _pAudioBuf = null;
	int[] pFInfo = new int[7];
	Thread _thread = null;
	PCMAudioRecorder _audioRecorder;
	public TalkPlayer() {
//		_pAudioBuf = ByteBuffer.allocate(1024);
//		int bufsize = AudioTrack.getMinBufferSize(Constants.CHANNEL_CONFIGURATION_MONO,
//				AudioFormat.CHANNEL_CONFIGURATION_MONO,
//				AudioFormat.ENCODING_PCM_16BIT);
//		//声音采集
//		_audioRecorder = new PCMAudioRecorder(this,Constants.ENCODING_PCM_BIT,Constants.CHANNEL_CONFIGURATION_MONO);
//		_audio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.CHANNEL_CONFIGURATION_MONO, AudioFormat.CHANNEL_CONFIGURATION_MONO, 
//				AudioFormat.ENCODING_PCM_16BIT, bufsize, AudioTrack.MODE_STREAM);//
	}
	public void setTalkPlayer(int bit,int mono) {
		if(bit == 8){
			Constants.ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_8BIT;
		}else if(bit == 16){
			Constants.ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_16BIT;
		}
		Constants.frequency = mono;
		
		_pAudioBuf = ByteBuffer.allocate(1024);
		int bufsize = AudioTrack.getMinBufferSize(Constants.frequency,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				Constants.ENCODING_PCM_BIT);
		//声音采集
		_audioRecorder = new PCMAudioRecorder(this,Constants.ENCODING_PCM_BIT,Constants.frequency);
		
		_audio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO, 
				Constants.ENCODING_PCM_BIT, bufsize, AudioTrack.MODE_STREAM);//
	}

	int[] bitRate = new int[2];
	public int Start() {
		synchronized (this) {
			if(_talkId != 0){
				_talkId = 0;
				_audioRecorder.Stop();
			}
			//_audio.play();//开始
			if(_talkId != 0){
				setTalkPlayer(bitRate[0],bitRate[1]);
				_audio.play();//开始
				_audioRecorder.Start();
				if(_thread == null){
					_thread = new Thread(this);
				}
				_thread.start();
			}
			else
			{
				
			}
		}
		
		return _talkId;
	}
	
	public void Stop() {}

	@Override
	public void run() {
		while (true){
			synchronized (this) {
				if(_talkId == 0){
					break;
				}
				int nRet = 0;//SDK.GetAudioData(_talkId, _pAudioBuf.array(), pFInfo);   //获取声音回调
				//System.out.println("--------SDK.GetAudioData   ");
				if (nRet == 0) {
					System.out.println("dataLen=" + pFInfo[0]);
					_audio.write(_pAudioBuf.array(), 0, pFInfo[0]);
				}
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void OnAudioData(byte[] buffer, int length) {
		synchronized (this) {
			if(_talkId != 0){
				//System.out.println("OnAudioData = " + length);
				///SDK.SendTalkData(_talkId, buffer, length);  //　发送声音
			}
		}
	}
}
