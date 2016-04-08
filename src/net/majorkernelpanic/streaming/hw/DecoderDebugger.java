package net.majorkernelpanic.streaming.hw;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;


/**
 * @author: li_jianhua Date: 2015-5-6 上午8:55:26
 * To change this template use File | Settings | File Templates.
 * @Description：mediaCodec 视频硬解码
 */
@SuppressLint("NewApi")
public class DecoderDebugger{
	private String TAG = DecoderDebugger.class.getSimpleName();
	private final static String MIME_TYPE = "video/avc";
    
	static int width = 352;//176;//1280;//;
	static int height = 288;//144;//720;//
    static int FRAMERATE = 20;//15
    static int BITRATE = 1000000;//125000;
	
	private MediaCodec mediaCodecDecode;
	public Surface _surface;
	private byte[] _data;
//	private BufferedOutputStream outputStream;
	Thread _sthread = null;
	public DecoderDebugger(Surface surface){
		this._surface = surface;
		configureDecoder();
		
		/*File f = new File(Environment.getExternalStorageDirectory(), "/video_encoded11.264");
	    touch (f);
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }*/
		
	}
	
	public void touch(File f) {
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setData(byte[] data,int length){
    	this._data = data;
    	if(null != _data){
    		
//    		try {
//				outputStream.write(_data);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//    		EncodeTask mEncTask = new EncodeTask(data);
//			mEncTask.execute((Void) null);
    		
			decoder(_data,length);
		}
    }
	
	//异步方法
	/*private class EncodeTask extends AsyncTask<Void, Void, Void> {
		private byte[] mData;
		EncodeTask(byte[] data) {
			this.mData = data;
		}
		@Override
		protected Void doInBackground(Void... params) {
			//decoder(mData);
			return null;
		}
	}*/
	
	
	public void close() {
	    try {
	        mediaCodecDecode.stop();
	        mediaCodecDecode.release();
	        mediaCodecDecode = null;
	    } catch (Exception e){ 
	    }
	}
	

    
	//初始化解码器  MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
	public void configureDecoder(){
		try {
			mediaCodecDecode = MediaCodec.createDecoderByType(MIME_TYPE);
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
		    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
		    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAMERATE);
		    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,  MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);    
		    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
			mediaCodecDecode.configure(mediaFormat, _surface, null, 0);  //ֱ�� surfce ��ʾ
			mediaCodecDecode.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	//解码器
	public synchronized void decoder(byte[] input,int length) {
		int generateIndex = 0;
		try {
			ByteBuffer[] inputBuffers = mediaCodecDecode.getInputBuffers();
//			ByteBuffer[] decOutputBuffers = mediaCodecDecode.getOutputBuffers();
			int inputBufferIndex = mediaCodecDecode.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				long ptsUsec = computePresentationTime(generateIndex);
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input,0,length);
				mediaCodecDecode.queueInputBuffer(inputBufferIndex, 0, length,ptsUsec, 0);
				generateIndex++;
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = mediaCodecDecode.dequeueOutputBuffer(bufferInfo,0);
			while (outputBufferIndex >= 0) {
				mediaCodecDecode.releaseOutputBuffer(outputBufferIndex, true);
				outputBufferIndex = mediaCodecDecode.dequeueOutputBuffer(bufferInfo,0);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
     * Generates the presentation time for frame N, in microseconds.
     */
    private static long computePresentationTime(int frameIndex) {
        return 132 + frameIndex * 1000000 / FRAMERATE;
    } 
    
//    public boolean isRunFlag() {
//		synchronized (this) {
//			return runFlag;
//		}
//	}
//
//	public void setRunFlag(boolean runFlag) {
//		synchronized (this) {
//			this.runFlag = runFlag;
//		}
//	}
	

/*	@Override
	public void run() {
		while(true){
//			System.out.println("........................."+isRunFlag());
			if(isRunFlag()){
				try {
					if(null != _data){
						decoder(_data);
					}
				} catch (Exception e) {
					LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
				}
			}
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}
	}*/
	
	
	
	
	

}
