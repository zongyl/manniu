package com.views.analog.camera.audio;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.utils.Constants;
import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

/**
 * @author: li_jianhua Date: 2015-6-11 下午5:00:21
 * To change this template use File | Settings | File Templates.
 * Description： MediaCodec 音频硬解码  不用
 */
@SuppressLint("NewApi")
public class AudioDecoder {
	AudioTrack _talkAudio; //播放声音
	private MediaCodec decoder;
	private byte[] _data;
	int _audioBufSize = 0;
	private BufferedOutputStream outputStream;
	public AudioDecoder(){
		setDecoder(Constants.frequency);
		
		_audioBufSize = AudioTrack.getMinBufferSize(Constants.frequency,Constants.channelConfiguration,Constants.audionEncoding);
		//声音播放
		_talkAudio = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.frequency, Constants.channelConfiguration,Constants.audionEncoding, _audioBufSize, AudioTrack.MODE_STREAM);//
		selectEncoder("audio/mp4a-latm");
		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/audio_decoder.aac");
		try {
			if (!f.exists())
				f.createNewFile();
			outputStream = new BufferedOutputStream(new FileOutputStream(f));
			Log.e("AudioEncoder", "outputStream initialized");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setData(byte[] data){
    	this._data = data;
    	if(null != _data && _data.length > 20){
			decoder(_data);
			try {
				outputStream.write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }
	
	public void Start() {
		try {
			_talkAudio.play();//开始
		} catch (Exception e) {
			System.out.println("打开语音对讲失败!");
		}
	}
	
	public void Stop() {
		try {
			_talkAudio.stop();
			_talkAudio = null;
			close();
		} catch (Exception e) {
			return;
		}
	}
	
//	int sampleRate = 0, channels = 0, bitrate = 0;
//	long presentationTimeUs = 0, duration = 0;
	private String selectEncoder(String mime) {
//		MediaExtractor extractor = new MediaExtractor();
//		  MediaFormat format = null;
//	        try {
//	        	format = extractor.getTrackFormat(0);
//		        mime = format.getString(MediaFormat.KEY_MIME);
//		        sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//				channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//				// if duration is 0, we are probably playing a live stream
//				duration = format.getLong(MediaFormat.KEY_DURATION);
//				bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE);
//	        } catch (Exception e) {
//				Log.e("LOG_TAG", "Reading format parameters exception:"+e.getMessage());
//				// don't exit, tolerate this error, we'll fail later if this is critical
//			}
//	        Log.d("LOG_TAG", "Track info: mime:" + mime + " sampleRate:" + sampleRate + " channels:" + channels + " bitrate:" + bitrate + " duration:" + duration);
	       
	        
	    for (int index = 0; index < MediaCodecList.getCodecCount(); index++) {
	        MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(index);

	        if (!codecInfo.isEncoder()) {
	            continue;
	        }

	        for (String type : codecInfo.getSupportedTypes()) {
	            if (type.equalsIgnoreCase(mime)) {
	            	System.out.println(type);
	                return "";
	            }
	        }
	    }
	    return null;
	 }
	
//	ByteBuffer[] inputBuffers = null;
//	ByteBuffer[] outputBuffers = null;
	
	private String mediaType = "OMX.google.aac.decoder";
	private void setDecoder(int rate){
		try {
			//decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
			decoder = MediaCodec.createByCodecName(mediaType);
	        MediaFormat format = new MediaFormat();
	        format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
	        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
	        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, rate);
	        format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);//AAC-HE 64kbps
	        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, _audioBufSize*2);
	        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
	        decoder.configure(format, null, null, 0);
	        decoder.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
	
	  //解码器
	public synchronized void decoder(byte[] input) {
		//LogUtil.d("AudioDecoder", "..XGPS.."+input.length);
  	    try {
  	        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
  	        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
  	        int inputBufferIndex = decoder.dequeueInputBuffer(-1);
  	        if (inputBufferIndex >= 0){
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

                _talkAudio.write(outData, 0, outData.length);
  	        	//System.out.println("声音长度：....."+outData.length);
  				 //OK的
  	        	decoder.releaseOutputBuffer(outputBufferIndex, false);
  	            outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 0);
  	        }
  	        
  	    } catch (Throwable t) {
  	        t.printStackTrace();
  	    }
  	}
  	
	//停止解码器
  	public void close() {
	    try {
	    	decoder.stop();
	    	decoder.release();
	    	decoder = null;
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
  	
	

}