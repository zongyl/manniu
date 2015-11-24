package com.views.analog.camera.audio;

import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.analog.camera.encode.EncoderDebugger;

import P2P.SDK;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

//音频采集线程
public class AudioRecorder implements Runnable {
	private volatile boolean isRecording = false;
//	private static final int frequency = 16000;
//	public static int ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_16BIT; //bit率
//	public static int CHANNEL_CONFIGURATION_MONO = 8000;//每秒8K个点
	// CHANNEL_IN_STEREO：双声道，CHANNEL_CONFIGURATION_MONO：单声道
	AudioRecord _recorder;	// 音频录制
	byte[] _tempBuffer;		// 临时Buffer
	public RecoderQueue _recoderQueue;//编码队列
	//public Boolean _startEncodeNow = false; //是否编码并发送数据
	
	public void intAudioRecorder() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		int bufferSize = AudioRecord.getMinBufferSize(Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding);
		//int tem = ((bufferSize/ret*2)+1)*2048;
		_recoderQueue = new RecoderQueue();
		bufferSize = (int) (((bufferSize/(Constants.maxInputSamples*2))+1)*2048);
		LogUtil.d("AudioRecorder", "采集端缓冲区大小："+bufferSize);
		if(bufferSize > 0){
			_tempBuffer = new byte[bufferSize];
			_recorder = new AudioRecord(AudioSource.MIC, Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding, bufferSize);
		}
	}

	public int Start() {
		try {
			synchronized (this) {
				if (isRecording) {
					return 0;
				}
				intAudioRecorder();
				if(_recorder != null){
					isRecording = true;
					//_startEncodeNow = true;
					_recorder.startRecording();
					_thread = new Thread(this);
					_thread.start();
					_recoderQueue.Start();
				}
			}
		} catch (Exception e) {
		}
		return 0;
	}

	Thread _thread = null;

	public void Stop() {
		synchronized (this) {
			try {
				isRecording = false;
				//_startEncodeNow = false;
				_thread = null;
				//Thread.sleep(200);
				if(_recorder != null){
					_recorder.stop();
					_recorder.release();
				}
				if(_recoderQueue != null)_recoderQueue.Stop();
				_tempBuffer = null;
				_recorder = null;
				_recoderQueue = null;
			} catch (Exception e) {
				LogUtil.e("AudioRecorder",ExceptionsOperator.getExceptionInfo(e));
			}
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		int bufferRead = 0;
		int bufferSize = _tempBuffer.length;
		while (this.isRecording) {
			bufferRead = _recorder.read(_tempBuffer, 0, bufferSize);

			if (bufferRead > 0) {// && _startEncodeNow
	        	//if(SDK._sessionId != 0){
	        		_recoderQueue.addSound(_tempBuffer);
	        	//}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
