package P2P;

import java.nio.ByteBuffer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.alibaba.fastjson.JSON;

@SuppressLint({ "InlinedApi", "NewApi" })
public class MediaThread extends Thread{
	
	private String TAG = MediaThread.class.getSimpleName();

	public MediaThread(Surface surface) {
		this.surface = surface;
		getDecoder(surface);
	}

	MediaCodec mediaCodec;
	Surface surface;
	Canvas canvas;
	
	static int width = 352;//176;//1280;//;
	static int height = 288;//144;//720;//
    static int FRAMERATE = 20;//15
    static int BITRATE = 1000000;//125000;

    private byte[] data;
    ByteBuffer[] inputBuffers, outputBuffers;

    BufferInfo bufferInfo = new BufferInfo();
    
    public void setData(byte[] data){
    	this.data = data;
    }
    
	@Override
	public void run() {
		synchronized (this) {
			if(null != data){
				decoder(data);
			}
		}
	}
	
	public void getDecoder(Surface surface){
		try {
			 MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
			    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
			    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAMERATE);
			    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);    
			    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
				
				mediaCodec = MediaCodec.createDecoderByType("video/avc");
				mediaCodec.configure(mediaFormat, surface, null, 0);
				mediaCodec.start();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 释放
	 */
	public void release(){
		Log.v(TAG, "....mediaThread.release....");
		mediaCodec.stop();
		mediaCodec.release();
	}
	
	/**
     * Generates the presentation time for frame N, in microseconds.
     */
    private static long computePresentationTime(int frameIndex) {
        return 132 + frameIndex * 1000000 / FRAMERATE;
    } 
    
    
  //解码器
	public synchronized void decoder(byte[] input) {
		int generateIndex = 0;
		try {
			ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
			ByteBuffer[] decOutputBuffers = mediaCodec.getOutputBuffers();
			int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
			if (inputBufferIndex >= 0) {
				long ptsUsec = computePresentationTime(generateIndex);
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input);
				mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length,ptsUsec, 0);
				generateIndex++;
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,
					0);
			while (outputBufferIndex >= 0) {
				// OK的
				mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
				outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@SuppressLint("NewApi")
	public int offerDecoder(byte[] input) {
		int generateIndex = 0;
		try {
			int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);

			if (inputBufferIndex >= 0) {
				long ptsUsec = computePresentationTime(generateIndex);
				ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
				inputBuffer.clear();
				inputBuffer.put(input);

				mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length,
						ptsUsec, 0);
				generateIndex++;
			}

			// System.out.println("input.length:"+input.length);
			// System.out.println("offset:"+bufferInfo.offset);
			// System.out.println("size:"+bufferInfo.size);
			// System.out.println("presentationTimeUs:"+bufferInfo.presentationTimeUs);
			// System.out.println("flags:"+bufferInfo.flags);

			int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,10000);

			switch (outputBufferIndex) {
			case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
				Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
				outputBuffers = mediaCodec.getOutputBuffers();
				break;
			case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
				// if the data is raw steam , coder will parse date
				Log.d(TAG, "New format " + mediaCodec.getOutputFormat());
				break;
			case MediaCodec.INFO_TRY_AGAIN_LATER:
				Log.d(TAG, "dequeueOutputBuffer timed out AGAIN_LATER!");
				break;
			default:
//				System.out.println("bufferInfo.flag " + bufferInfo.flags + "=="
//						+ bufferInfo.offset + "==" + bufferInfo.size + "=="
//						+ bufferInfo.presentationTimeUs);
				
				mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
	            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
			}

		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
