package com.views.analog.camera.encode;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import com.utils.ColorFormatUtil;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;
import com.views.NewMain;
import com.views.NewSurfaceTest;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Surface;


/**
 * @author: li_jianhua Date: 2015-5-6 上午8:55:26
 * To change this template use File | Settings | File Templates.
 * @Description：mediaCodec 视频硬解码
 */
@SuppressLint("NewApi")
public class DecoderDebugger {
	private String TAG = DecoderDebugger.class.getSimpleName();
	private final String MIME_TYPE = "video/avc";
    
	public int width = 352;//176;//1280;//;
	public int height = 288;//144;//720;//
	public int FRAMERATE = 20;//15
	public int BITRATE = 80000000;//125000;
	
	public MediaCodec mediaCodecDecode;
	public Surface _surface;
	private byte[] _data;
	//private BufferedOutputStream outputStream;
	Thread _sthread = null;
	//public boolean _isRecording = false;//录像
	public String _fileName = "";//文件名
	//public boolean isInitDecoder = false;
	/** 是否支持硬解码 */
	public boolean canDecode = false;
	/** 硬解码是否释放 */
	private boolean isRelease = false;
	
//	private RealHandler _handler = null;
//	private BaseApplication mAPP = null;
	
	public DecoderDebugger(Surface surface,Context context){
		this._surface = surface;
		configureDecoder(FRAMERATE,BITRATE,width,height);
		
//		mAPP = (BaseApplication) context.getApplicationContext();
		// 获得该共享变量实例
//		_handler = mAPP.getRedlandler();
		
//		File f = new File(Environment.getExternalStorageDirectory(), "/IPC_test.264");
//	    touch (f);
//	    try {
//	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
//	    } catch (Exception e){ 
//	        e.printStackTrace();
//	    }
		
	}
	
	public DecoderDebugger(Surface surface,int width,int height) {
		this.width=width;
		this.height=height;
		this._surface = surface;
		configureDecoder(FRAMERATE,BITRATE,width,height);
	}
	
	//写文件可以不用SPS PPS 头
	/*public void recordFile(String filePath){
        try {   
            File file = new File(filePath);   
            if (file.exists())   
                file.delete();   
            //outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
            //_isRecording = true;
        } catch (Exception ex) {   
            Log.v("System.out", ex.toString());   
        }
	}*/
	
	public void touch(File f) {
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isCanDecode() {
		return canDecode;
	}
	public void setCanDecode(boolean canDecode) {
		this.canDecode = canDecode;
	}
	public boolean isRelease() {
		return isRelease;
	}

	public void setRelease(boolean isRelease) {
		this.isRelease=isRelease;
	}
	public Surface GetSurface(){
		return _surface;
	}
	
	/*public void setData(byte[] data,int length){
    	this._data = data;
    	if(null != _data && mediaCodecDecode != null){
    		decoder(data,length);
//    		EncodeTask mEncTask = new EncodeTask(data,length);
//			mEncTask.execute((Void) null);
		}
    }
	//异步方法
	private class EncodeTask extends AsyncTask<Void, Void, Void> {
		private byte[] mData;
		private int length;
		// 构造函数
		EncodeTask(byte[] data,int length) {
			this.mData = data;
			this.length = length;
		}
		@Override
		protected Void doInBackground(Void... params) {
			//decoder(mData,length);
			return null;
		}
	}*/
	
	
	public synchronized void close() {
		if(mediaCodecDecode != null){
			long t3= System.currentTimeMillis();
			try{ 
				mediaCodecDecode.flush();
			}catch(IllegalStateException e){}
		    try {
		        mediaCodecDecode.stop();
		        mediaCodecDecode.release();
		        mediaCodecDecode = null;
		        _surface = null;
		        long t4= System.currentTimeMillis();
		        LogUtil.d(TAG, " 解码退出  mediaCodecDecode.stop() time= "+(t4-t3));
		    } catch (Exception e){
		    	LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		    }
		}
	}
	
	/**
	 * 释放解码器
	 */
	public synchronized void release() {
		if (!isRelease) {
			try {			
		        mediaCodecDecode.stop();
		        mediaCodecDecode.release();
		        isRelease = true;
		    } catch (Exception e){ 
		    	LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		    }
		}
	}
	
	/**
	 * 释放解码器
	 */
	public void release2() {
		if (!isRelease) {
			Log.v("debug", "....mediaThread.release...."+mediaCodecDecode);
			mediaCodecDecode.release();
			isRelease = true;
		}
	}
	
	//初始化解码器  MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
	public void configureDecoder2(int framerate,int bitrate,int _width,int _height){
		try {
			width = _width==0?width:_width;
			height = _height==0?height:_height;
			int colorFormat = ColorFormatUtil.selectColorFormat(ColorFormatUtil.selectCodec(MIME_TYPE), MIME_TYPE);
			mediaCodecDecode = MediaCodec.createDecoderByType(MIME_TYPE);
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
		    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);    
		    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
			mediaCodecDecode.configure(mediaFormat, _surface, null, 0);  //直接 surfce 显示
			mediaCodecDecode.start();
			flag = 0;
		} catch (Exception e) {
			LogUtil.e(TAG,ExceptionsOperator.getExceptionInfo(e));
		}
	}
	
	public void configureDecoder(int framerate,int bitrate,int _width,int _height) {
		try {
			MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
	        // little tricky here, csd-0 is required in order to configure the codec properly
	        // it is basically the first sample from encoder with flag: BUFFER_FLAG_CODEC_CONFIG
	        byte[] mBuffer = new byte[0];
	        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
	        mBuffer = new byte[info.size];
	        format.setByteBuffer("csd-0", ByteBuffer.wrap(mBuffer, 0, info.size));
	        try {
	        	mediaCodecDecode = MediaCodec.createDecoderByType(MIME_TYPE);
	        } catch (IOException e) {
	            throw new RuntimeException("Failed to create codec", e);
	        }
	        mediaCodecDecode.configure(format, _surface, null, 0);
	        mediaCodecDecode.start();
	        flag = 0;
		} catch (Exception e) {
		}
    }
	
	 //解码器
	public int flag = 0;//第一次停500毫秒
	private int errorCount = 0;//大于3次走软解
//	private boolean _haveIFrameSucce = false;
	public synchronized int decoder(byte[] input,int length) {
		int ret = 0;
		int generateIndex = 0;
		try {
			if(mediaCodecDecode == null) return 0;
			if(flag == 0 && NewSurfaceTest.instance != null){//设备-列表播放视频
				flag = 1;
				NewSurfaceTest.instance.talkAudio();
				NewSurfaceTest.instance.showGpu();
				NewSurfaceTest.instance.closeWait();
			}
			
			//软解start......
			/*if(canDecode){
				canDecode = false;
				release();
				isRelease=true;
			}*/
			//end.....
			
			//硬解 start.....
			ByteBuffer[] inputBuffers = mediaCodecDecode.getInputBuffers();
			ByteBuffer[] outputBuffers = mediaCodecDecode.getOutputBuffers();
			//返回一个inputbuffer的索引用来填充数据，返回-1表示暂无可用buffer
			int inputBufferIndex = mediaCodecDecode.dequeueInputBuffer(10000);
			if (inputBufferIndex >= 0) {
				long ptsUsec = computePresentationTime(generateIndex);
				 ByteBuffer buffer;
                 // since API 21 we have new API to use
                 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                     buffer = mediaCodecDecode.getInputBuffers()[inputBufferIndex];
                     buffer.clear();
                 } else {
                     buffer = mediaCodecDecode.getInputBuffer(inputBufferIndex);
                 }
                 if (buffer != null) {
                     buffer.put(input, 0, length);
                     mediaCodecDecode.queueInputBuffer(inputBufferIndex, 0, length, ptsUsec, 0);
                 }
                 generateIndex++;
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			//获得你接收到结果的ByteBuffer的索引位置 排一个输出buffer,如果等待timeoutUs时间还没响应则跳过，返回TRY_AGAIN_LATER
			int outputBufferIndex = mediaCodecDecode.dequeueOutputBuffer(bufferInfo,10000);
			while (outputBufferIndex >= 0) {
				//如果你对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
				//释放所有权 这个output buffer将被返回到解码器中
				mediaCodecDecode.releaseOutputBuffer(outputBufferIndex, true);
				outputBufferIndex = mediaCodecDecode.dequeueOutputBuffer(bufferInfo,0);//绘图
			}
			
			//end.....
		} catch (Exception e) {
			errorCount ++;
			if(NewSurfaceTest.instance != null && NewMain.devType == 1 && errorCount > 2){//IPC
				errorCount = 0;
				canDecode = false;
				release();
				//不支持硬解 图标变成不可点击
				NewSurfaceTest.instance.showGpu();
				SDK.SetDecoderModel(1,SDK._sessionIdContext);
			}else if(errorCount > 50){
				errorCount = 0;
				canDecode = false;
				release();
			}else{
				flag = 0;
				ret = -1;
			}
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
		return ret;
	}
	
	
	public synchronized int decoderAlarm(byte[] input,int length) {
		int ret = 0;
		int generateIndex = 0;
		try {
			if(mediaCodecDecode == null) return 0;
			//硬解 start.....
			ByteBuffer[] inputBuffers = mediaCodecDecode.getInputBuffers();
			ByteBuffer[] outputBuffers = mediaCodecDecode.getOutputBuffers();
			//返回一个inputbuffer的索引用来填充数据，返回-1表示暂无可用buffer
			int inputBufferIndex = mediaCodecDecode.dequeueInputBuffer(10000);
			if (inputBufferIndex >= 0) {
				long ptsUsec = computePresentationTime(generateIndex);
				 ByteBuffer buffer;
                 // since API 21 we have new API to use
                 if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                     buffer = mediaCodecDecode.getInputBuffers()[inputBufferIndex];
                     buffer.clear();
                 } else {
                     buffer = mediaCodecDecode.getInputBuffer(inputBufferIndex);
                 }
                 if (buffer != null) {
                     buffer.put(input, 0, length);
                     mediaCodecDecode.queueInputBuffer(inputBufferIndex, 0, length, ptsUsec, 0);
                 }
                 generateIndex++;
			}

			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			//获得你接收到结果的ByteBuffer的索引位置 排一个输出buffer,如果等待timeoutUs时间还没响应则跳过，返回TRY_AGAIN_LATER
			int outputBufferIndex = mediaCodecDecode.dequeueOutputBuffer(bufferInfo,10000);
			while (outputBufferIndex >= 0) {
				//如果你对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
				//释放所有权 这个output buffer将被返回到解码器中
				mediaCodecDecode.releaseOutputBuffer(outputBufferIndex, true);
				outputBufferIndex = mediaCodecDecode.dequeueOutputBuffer(bufferInfo,0);//绘图
			}
		} catch (Exception e) {
			errorCount ++;
			if(errorCount > 2){//IPC
				errorCount = 0;
				canDecode = false;
				release();
			}
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
		return ret;
	}
	
	/*private void setData(final YuvImage image , final int w , final int h){
		Bitmap bmp = null;
		try {
			if(image!=null){
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, w, h), 80, stream);
				bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(bmp != null){
				BitmapUtils.saveBitmap(bmp, _fileName);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		bmp = null;
	}*/
	
	/**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(int frameIndex) {
        return 132 + frameIndex * 1000000 / FRAMERATE;
    } 
    
	
	

}
