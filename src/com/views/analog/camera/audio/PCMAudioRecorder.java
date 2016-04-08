package com.views.analog.camera.audio;

import com.utils.Constants;

import android.media.AudioRecord;
import android.media.MediaRecorder;

public class PCMAudioRecorder implements Runnable {
	private volatile boolean isRecording = false;
//	private static final int frequency = 16000;
	
//	public static int ENCODING_PCM_BIT = AudioFormat.ENCODING_PCM_16BIT; //bit率
//	public static int CHANNEL_CONFIGURATION_MONO = 8000;//每秒8K个点
	
	IAudioData _user;		// 用户接口
	AudioRecord _recorder;	// 音频录制
	byte[] _tempBuffer;		// 临时Buffer
 
	public PCMAudioRecorder(IAudioData user,int bit,int mono) {
		super();
		_user = user;

		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
		//int bufferRead = 0;
		// CHANNEL_IN_STEREO：双声道，CHANNEL_CONFIGURATION_MONO：单声道
		int bufferSize = AudioRecord.getMinBufferSize(Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding);

		if(bufferSize > 0){
			_tempBuffer = new byte[bufferSize];
			_recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Constants.frequency, Constants.channelConfiguration, Constants.audionEncoding, bufferSize);
		}
	}

	public int Start() {
		synchronized (this) {
			if (isRecording) {
				return 0;
			}

			if(_recorder != null){
				isRecording = true;
				_recorder.startRecording();
				_thread = new Thread(this);
				_thread.start();
			}
		}
		return 0;
	}

	Thread _thread = null;

	public void Stop() {
		synchronized (this) {
			isRecording = false;
			_thread = null;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			_recorder.stop();
			_recorder.release();
		}
	}

	public void run() {
		int bufferRead = 0;
		int bufferSize = _tempBuffer.length;
		while (this.isRecording) {
			bufferRead = _recorder.read(_tempBuffer, 0, bufferSize);
			// if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
			// throw new
			// IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
			// } else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
			// throw new
			// IllegalStateException("read() returned AudioRecord.ERROR_BAD_VALUE");
			// } else if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
			// throw new
			// IllegalStateException("read() returned AudioRecord.ERROR_INVALID_OPERATION");
			// }

			if (bufferRead > 0) {
				_user.OnAudioData(_tempBuffer, bufferRead);
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
