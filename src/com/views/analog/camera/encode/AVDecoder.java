package com.views.analog.camera.encode;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.NewMain;
import com.views.NewSurfaceTest;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

@SuppressLint({ "InlinedApi", "NewApi" })
public class AVDecoder {
	private String TAG = DecoderDebugger.class.getSimpleName();
	
	public AVDecoder(Surface surface,int width,int height) {
		this.width=width;
		this.height=height;
		this.surface = surface;
		getDecoder(surface);
	}

	// private static final String LOG_TAG = "AvDecoder";
	MediaCodec mediaCodec;
	Surface surface;
	Canvas canvas;
	private int width ;//= 1280;// 176;//1280;//;
	private int height ;//= 720;// 144;//720;//

	static int framerate = 25;// 15;25
	static int bitrate = 80000000;// 125000;80000000
	/** 是否支持硬解码 */
	private boolean canDecode = true;
	/** 硬解码是否释放 */
	private boolean isRelease = false;
	/**硬解码是否已启动*/
	private boolean isStart=false;
	public boolean isInitDecoder = false;//初始化解码器
	
	public boolean isStart() {
		return isStart;
	}

	public void setStart(boolean isStart) {
		this.isStart = isStart;
	}

	/** 测试 */
	private int outputindex;
	private int tryCount=0;
	private int errorCount = 0;//大于3次真软解
	public int getOutputindex() {
		return outputindex;
	}

	public void setOutputindex(int outputindex) {
		this.outputindex = outputindex;
	}

	public boolean isCanDecode() {
		return canDecode;
	}

	public void setCanDecode(boolean canDecode) {
		this.canDecode = canDecode;
	}

	ByteBuffer[] inputBuffers, outputBuffers;

	// BufferInfo bufferInfo = new BufferInfo();

	public void getDecoder(Surface surface) {
		MediaFormat mediaFormat = null;

		mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
		try {
			mediaCodec = MediaCodec.createDecoderByType("video/avc");

		} catch (Exception e) {
			e.printStackTrace();
		}
//		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
//		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
//		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
//				MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
//		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
		mediaCodec.configure(mediaFormat, surface, null, 0);

		mediaCodec.start();
		isInitDecoder = true;
//		isStart=true;

		// inputBuffers = mediaCodec.getInputBuffers();
		// outputBuffers = mediaCodec.getOutputBuffers();

	}
	/**启动解码器*/
	public void startDecoder(){
		if(!isStart)
		{	
//			mediaCodec.reset();
//			mediaCodec.start();
			isStart=true;
		}
	}
	/**停止解码器*/
	public void stopDecoder(){
		if(isStart){
			mediaCodec.stop();
			isStart=false;
//			mediaCodec.release();
		}
	}

	/**
	 * 释放解码器
	 */
	public synchronized void release() {
		if (!isRelease) {
			Log.v("debug", "....mediaThread.release....");
//			if(!isStart)
			mediaCodec.stop();
			mediaCodec.release();
			isRelease = true;
		}
	}

	public boolean isRelease() {
		return isRelease;
	}

	public void setRelease(boolean isRelease) {
		this.isRelease=isRelease;
	}

	/**
	 * Generates the presentation time for frame N, in microseconds.
	 */
	private static long computePresentationTime(int frameIndex) {
		return 132 + frameIndex * 1000000 / framerate;
	}

	@SuppressLint("NewApi")
	public int offerDecoder(byte[] input,int length) {
		int ret = 0;
		int generateIndex = 0;
			try {
				if(tryCount>50){
					canDecode = false;
					release();
					isRelease=true;
					return 0;
				}
				
				inputBuffers = mediaCodec.getInputBuffers();
				outputBuffers = mediaCodec.getOutputBuffers();
				int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);

				if (inputBufferIndex >= 0) {
					// ByteBuffer
					// bytebuffer=mediaCodec.getInputBuffer(inputBufferIndex);
					long ptsUsec = computePresentationTime(generateIndex);
					ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
					inputBuffer.clear();
					//inputBuffer.put(input);
					inputBuffer.put(input,0,length);

					mediaCodec.queueInputBuffer(inputBufferIndex, 0,
							input.length, ptsUsec, 0);
					generateIndex++;
				}

				// int outputBufferIndex = mediaCodec.dequeueOutputBuffer(
				// bufferInfo, 10000);
				BufferInfo bufferInfo = new BufferInfo();
				int outputBufferIndex = mediaCodec.dequeueOutputBuffer(
						bufferInfo, 10000);
//				setOutputindex(outputBufferIndex);
				switch (outputBufferIndex) {
				case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
					tryCount=0;
					Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
					outputBuffers = mediaCodec.getOutputBuffers();
					break;
				case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
					// if the data is raw steam , coder will parse date
					// format
//					tryCount=0;
					Log.d("DecodeActivity",
							"New format " + mediaCodec.getOutputFormat());
					break;
				case MediaCodec.INFO_TRY_AGAIN_LATER:
					tryCount++;
					Log.d("DecodeActivity",
							"dequeueOutputBuffer timed out AGAIN_LATER!");
					break;
				default:
					// System.out.println("bufferInfo.flag "
					// + bufferInfo.flags + "==" + bufferInfo.offset
					// + "==" + bufferInfo.size + "=="
					// + bufferInfo.presentationTimeUs);
					tryCount=0;
					mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
				}

			} catch (IllegalStateException e) {
				//NewSurfaceTest.instance.avdClose();
				if(NewMain.devType == 1){
					errorCount++;
					if(errorCount > 2){
						canDecode = false;
						release();
						isRelease=true;
						errorCount = 0;
					}else{
						ret = -1;
					}
				}
				LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
			}

		return ret;
	}
	/**刷新解码器*/
	public void flush(){
		mediaCodec.flush();
	}


}
