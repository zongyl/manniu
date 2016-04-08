package com.views.analog.camera.audio;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.utils.Constants;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
/**
 * 对讲Player
 */
@SuppressLint("NewApi")
public class TalkPlayer implements Runnable, IAudioData {
	AudioTrack _talkAudio; //播放声音
	int _talkId = 0;
	//ByteBuffer _pAudioBuf = null;
	int[] talk_pFInfo = new int[7];
	Thread _thread = null;
	PCMAudioRecorder _audioRecorder; //采集声音
	private byte[] _data;
	private MediaPlayer mediaPlayer;  
	
	private MediaCodec decoder;
	
	public TalkPlayer() {
		//_pAudioBuf = ByteBuffer.allocate(1024*5);
		int bufsize = AudioTrack.getMinBufferSize(Constants.frequency,Constants.channelConfiguration,Constants.audionEncoding);
		//声音采集
		_audioRecorder = new PCMAudioRecorder(this,AudioFormat.ENCODING_PCM_16BIT,Constants.frequency);
		_talkAudio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.frequency, Constants.channelConfiguration,Constants.audionEncoding, bufsize, AudioTrack.MODE_STREAM);//
		//setDecoder(Constants.frequency);
	}
/*	public void setTalkPlayer(int bit,int mono) {
		if(bit == 8){
			Constants.ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_8BIT;
		}else if(bit == 16){
			Constants.ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_16BIT;
		}
		Constants.CHANNEL_CONFIGURATION_MONO = mono;
		
		_pAudioBuf = ByteBuffer.allocate(1024*5);
		int bufsize = AudioTrack.getMinBufferSize(Constants.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				Constants.ENCODING_PCM_BIT);
		//声音采集
		_audioRecorder = new PCMAudioRecorder(this,Constants.ENCODING_PCM_BIT,Constants.CHANNEL_CONFIGURATION_MONO);
		
		_talkAudio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.CHANNEL_CONFIGURATION_MONO, AudioFormat.CHANNEL_CONFIGURATION_MONO, 
				Constants.ENCODING_PCM_BIT, bufsize, AudioTrack.MODE_STREAM);//
	}*/


	int[] bitRate = new int[2];
	public int Start() {
		try {
			synchronized (this) {
				_talkId = 1;
				
				_talkAudio.play();//开始
				_audioRecorder.Start();
				if(_thread == null){
					_thread = new Thread(this);
				}
				_thread.start();
				
			}
		} catch (Exception e) {
			System.out.println("打开语音对讲失败!");
			return 0;
		}
		return _talkId;
	}


	
	public void Stop() {
		try {
			synchronized (this) {
//				if(_talkId == 0){
//					return;
//				}
//				int talkId = _talkId;
				_talkId = 0;
				_thread = null;
				_talkAudio.stop();
				_audioRecorder.Stop();
				//_pAudioBuf = null;
				_audioRecorder = null;
				_talkAudio = null;
				
				//close();
			}
		} catch (Exception e) {
			return;
		}
	}
	
	public void setData(byte[] data){
    	this._data = data;
    	
//    	if(null != _data && _data.length > 0){
//    		decoder(_data);
//		}
    }

	@Override
	public void run() {
		while (true){
			synchronized (this) {
				if(_talkId == 0){
					break;
				}
				//System.out.println(".....获取声音回调");
				/*if(_talkId == 0){
					break;
				}
				int nRet = SDK.GetAudioData(_talkId, _pAudioBuf.array(), talk_pFInfo);   //获取声音回调
				//System.out.println(talk_pFInfo[0]);
				if (nRet == 0) {
					_talkAudio.write(_pAudioBuf.array(), 0, talk_pFInfo[0]);
				}*/
				
		    	if(null != _data && _talkId != 0 && _data.length > 0){
		    		_talkAudio.write(_data, 0, _data.length);
		    		//decoder(_data);
				}

			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private float BEEP_VOLUME = 0.50f;
    private synchronized void initBeepSound(int type) {
		mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
        mediaPlayer.setOnCompletionListener(beepListener);
//        AssetFileDescriptor file = Main.Instance.getResources().openRawResourceFd(
//        		getSoundFile(type));
        try {
            //mediaPlayer.setDataSource();
            //file.close();
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            mediaPlayer = null;
            return;
        }
    }
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public synchronized void onCompletion(MediaPlayer mediaPlayer) {
            //mediaPlayer.seekTo(0);  
            mediaPlayer.release();
        }
    };
	
	@Override
	public void OnAudioData(byte[] buffer, int length) {
		synchronized (this) {
			//if(_talkId != 0){
				//SDK.SendTalkData(_talkId, buffer, length);  //　发送声音
				//System.out.println("发送声音。。。。");
			//}
		}
	}
	
	
	/*private void setDecoder(int rate){
		final int kBitRates[] = { 64000,96000,128000 };
        decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, rate);
        format.setInteger(MediaFormat.KEY_BIT_RATE, kBitRates[1]);//AAC-HE 64kbps
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        decoder.configure(format, null, null, 0);
        decoder.start();
    }
	
	  //解码器
  	public synchronized void decoder(byte[] input) {
  		//int generateIndex = 0;
  	    try {
  	        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
  	        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
  	        int inputBufferIndex = decoder.dequeueInputBuffer(-1);
  	        if (inputBufferIndex >= 0){
  	        	//long ptsUsec = computePresentationTime(generateIndex);
  	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
  	            inputBuffer.clear();
  	            inputBuffer.put(input);
  	            decoder.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
  	        }

  	        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
  	        int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo,0);
  	        while (outputBufferIndex >= 0) {
  	        	ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                byte[] outData = new byte[bufferInfo.size];
                outputBuffer.get(outData);

                //player.write(outData, 0, outData.length);
                _talkAudio.write(outData, 0, outData.length);
  	        	System.out.println("声音长度：....."+outData.length);
  				 //OK的
  	        	decoder.releaseOutputBuffer(outputBufferIndex, true);
  	            outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
  	        }
  	        
  	    } catch (Throwable t) {
  	        t.printStackTrace();
  	    }
  	}
  	
  	public void close() {
	    try {
	    	decoder.stop();
	    	decoder.release();
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}*/
	
	
}
