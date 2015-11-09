package com.views.analog.camera.encode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import com.utils.LogUtil;
import com.views.analog.camera.encode.CodecManager.Codec;
import com.views.analog.camera.exceptions.CameraInUseException;
import com.views.analog.camera.exceptions.InvalidSurfaceException;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
/**
 * @author: li_jianhua Date: 2015-6-11 下午5:00:21
 * To change this template use File | Settings | File Templates.
 * Description： MediaCodec 视频硬编码
 */
@SuppressLint("NewApi")
public class EncoderDebugger {
	private String TAG = EncoderDebugger.class.getSimpleName();
	
	private final static String MIME_TYPE = "video/avc";
	/** Bitrate that will be used with the encoder. */
	private final static int BITRATE = 1000000;
	/** Framerate that will be used to test the encoder. */
	public static int FRAMERATE = 15;
	public static int PreviewFpsRange = 1500;//摄像机帧率
	protected int mOrientation = 90;//设置图片旋转度数
	private MediaCodec mediaCodec;
	//private MediaCodec mediaCodecDecode;
	public int m_width;
	public int m_height;
	int mSize;
	byte[] m_info = null;
	private BufferedOutputStream outputStream;
	private byte[] yuv420 = null; 
	public MediaFormat mDecOutputFormat;
	public Surface surface;
	protected SharedPreferences mSettings = null;
	
	public static Boolean startEncodeNow = false; //是否编码并发送数据
	public Boolean startSnap = false;//截图片
	public String fileName;
	
	public EncoderDebugger(SurfaceView  sv,int width,int height) { 
		this.mSurfaceView = sv;
		/*File f = new File(Environment.getExternalStorageDirectory(), "/video_encoded.264");
	    touch (f);
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
	        Log.i("AvcEncoder", "outputStream initialized");
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }*/
		m_width  = width;
		m_height = height;
		mSize = m_width * m_height;
		yuv420 = new byte[m_width*m_height*3/2];
		getCodes();
		if(PreviewFpsRange == 1000){
			FRAMERATE = 10;
		}else if(PreviewFpsRange == 1500){
			FRAMERATE = 15;
		}else if(PreviewFpsRange == 2000){
			FRAMERATE = 20;
		}
	}
	
	/*public void touch(File f) {
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	
	/**
	 * Instantiates and starts the encoder.
	 * 实例化并启动编码器。
	 */
	public void configureEncoder()  {
		try {
			mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
			//int bitRate = camera.getFpsRange() //可以通过摄像头获取 bit大小
		    MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, m_width, m_height);
		    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BITRATE);
		    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20);
		    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, mEncoderColorFormat);    
		    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		    mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		    mediaCodec.start();
		} catch (Exception e) {
		}
	}

	public void close() {
	    try {
	    	if(mediaCodec != null){
	    		mediaCodec.stop();
		        mediaCodec.release();
		        mediaCodec = null;
	    	}
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	
	// called from Camera.setPreviewCallbackWithBuffer(...) in other class //这种方式的文件可以直接 用暴风播放
	public synchronized void offerEncoderForNV21(byte[] input) {
	    try {
	        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
	        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
	        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
	        if (inputBufferIndex >= 0) {
	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
	            inputBuffer.clear();
	            inputBuffer.put(input);
	            mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, 0, 0);
	        }

	        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
	        while (outputBufferIndex >= 0) {
	            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
	            byte[] outData = new byte[bufferInfo.size];
	            outputBuffer.get(outData);
//	            byte[] outData = new byte[bufferInfo.size + 4];  
//	            outputBuffer.get(outData, 4, bufferInfo.size);   // 0 是音频 1 是视频 后面三个字节填
//	            outData[0] = 1;  
//	            outData[1] = 0;  
//	        	outData[2] = 0; 
//	        	outData[3] = 0; 
	            
	            //offerDecoder(outData);
	            if(SDK._sessionId != 0){
            		//SDK.SendData(SDK._sessionId,outData,outData.length,1,0);
//	            		LogUtil.d("SDK", "NV21发送视频..."+outData.length+" -- "+outData[0]+","+outData[1]+","+outData[2]+","+outData[3]+","+outData[4]+"------"+outData[outData.length-1]+","+outData[outData.length-2]+","+outData[outData.length-3]+","+outData[outData.length-4]+","+outData[outData.length-5]
//	    						+"------"+outData[outData.length-6]+","+outData[outData.length-7]+","+outData[outData.length-8]+","+outData[outData.length-9]+","+outData[outData.length-10]);
	            }
	            //outputStream.write(outData, 0, outData.length);
	            //Log.i("AvcEncoder", " ..............编码后的长度   "+outData.length);
	            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
	            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
	        }
	    } catch (Throwable t) {
	        t.printStackTrace();
	    }
	}

	//不能直接播放
	public synchronized int offerEncoderForYV12(byte[] input) {	
		int pos = 0;
		swapYV12toI420(input, yuv420, m_width, m_height);
	    try {
	        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
	        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
	        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
	        if (inputBufferIndex >= 0) {
	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
	            inputBuffer.clear();
	            inputBuffer.put(yuv420);
	            mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420.length, 0, 0);
	        }

	        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo,0);
	       
	        while (outputBufferIndex >= 0){
	            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
	            byte[] outData = new byte[bufferInfo.size];
	            outputBuffer.get(outData);
//	            byte[] outData = new byte[bufferInfo.size + 4];  
//	            outputBuffer.get(outData, 4, bufferInfo.size);   // 0 是音频 1 是视频 后面三个字节填
//	            outData[0] = 1;  
//	            outData[1] = 0;  
//	        	outData[2] = 0; 
//	        	outData[3] = 0; 
	            
	            //offerDecoder(outData);
	            
	            if(SDK._sessionId != 0){
            		//SDK.SendData(SDK._sessionId,outData,outData.length,1,0);
	            		//outputStream.write(newData, 0, newData.length);
	            		//SDK.SendData(outData,outData.length,0,0,SDK.lessionList.get(i));
//	            		LogUtil.d("SDK", "yv12发送视频..."+outData.length+" -- "+outData[0]+","+outData[1]+","+outData[2]+","+outData[3]+","+outData[4]+"------"+outData[outData.length-1]+","+outData[outData.length-2]+","+outData[outData.length-3]+","+outData[outData.length-4]+","+outData[outData.length-5]
//	    						+"------"+outData[outData.length-6]+","+outData[outData.length-7]+","+outData[outData.length-8]+","+outData[outData.length-9]+","+outData[outData.length-10]);
	            }
	            
	            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
	            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
	        }
//	        if(output[4] == 0x65){ //key frame
//	        	System.arraycopy(output, 0,  yuv420, 0, pos);
//	        	System.arraycopy(m_info, 0,  output, 0, m_info.length);
//	        	System.arraycopy(yuv420, 0,  output, m_info.length, pos);
//	        	pos += m_info.length;
//	        }
	    } catch (Throwable t) {
	        t.printStackTrace();
	    }
	    return pos;
	}
	
    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height){   
    	System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
    	System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
    	System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4);  
    }  
    
    protected SurfaceView mSurfaceView = null;
    public static Camera mCamera;
	protected Thread mCameraThread;
	protected Looper mCameraLooper;
	protected boolean mUnlocked = false;
	protected boolean mCameraOpenedManually = true;
	protected boolean mPreviewStarted = false;
	protected boolean mStreaming = false, mConfigured = false;
	protected boolean mFlashEnabled = false;
	protected VideoQuality mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
	protected VideoQuality mQuality = mRequestedQuality.clone(); 
	protected int mCameraId = 0;
	/**
	 * camera 支持的颜色 
	 * 在摄像头预览输出是YV12，在编码器输入是COLOR_FormatYUV420Planar : 19
	 * 在摄像头预览输出是NV21，在编码器输入是   COLOR_FormatYUV420SemiPlanar :21
	 * 
	 * 联想P780支持是 YV12 编码时需要转swapYV12toI420 不然图片会花屏
	 * 中兴手机支持NV21 这种格式不需要转
	 */
	protected int mCameraImageFormat = ImageFormat.NV21;	
	private int mEncoderColorFormat;
	
	public void getCodes(){
		Codec[] encoders = CodecManager.findEncodersForMimeType(MIME_TYPE);
		for (int i=0;i<encoders.length;i++) {
			for (int j=0;j<encoders[i].formats.length;j++) {
				mEncoderColorFormat = encoders[i].formats[j];
				//Log.i(TAG,"mEncoderColorFormat::"+mEncoderColorFormat+"......");
			}
		}
		if(mEncoderColorFormat == 19){
			mCameraImageFormat = ImageFormat.YV12;
		}else if(mEncoderColorFormat == 21){
			mCameraImageFormat = ImageFormat.NV21;
		}
		Log.i(TAG,"mEncoderColorFormat::"+mEncoderColorFormat+"......");
//		Codec[] decoders = CodecManager.findDecodersForMimeType(MIME_TYPE);
//		for (int k=0;k<decoders.length;k++) {
//			for (int l=0;l<decoders[k].formats.length;l++) {
//				mDecoderColorFormat = decoders[k].formats[l];
//				Log.i(TAG,"mDecoderColorFormat::"+mDecoderColorFormat);
//			}
//		}
		
	}
	
	
	public void switchCamera() throws RuntimeException, IOException {
		int CammeraIndex = FindBackCamera();  
        if(CammeraIndex == -1){  
            CammeraIndex = FindFrontCamera();  
        }  
		setCamera(CammeraIndex);
	}
	public void setCamera(int camera) {
		CameraInfo cameraInfo = new CameraInfo();
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i=0;i<numberOfCameras;i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == camera) {
				mCameraId = i;
				break;
			}
		}
	}
	
	@TargetApi(9)  
    private int FindFrontCamera(){  
        int cameraCount = 0;  
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  
        cameraCount = Camera.getNumberOfCameras(); // get cameras number  
        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {  
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo  
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {   
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置  
               return camIdx;  
            }  
        }  
        return -1;  
    }  
    @TargetApi(9)  
    private int FindBackCamera(){  
        int cameraCount = 0;  
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  
        cameraCount = Camera.getNumberOfCameras(); // get cameras number  
        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {  
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo  
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_BACK ) {   
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置  
               return camIdx;  
            }  
        }  
        return -1;  
    } 
    
    
	protected void encodeWithMediaCodecMethod1() throws RuntimeException, IOException {
//		createCamera();
//		updateCamera();
		configureEncoder();
//		measureFramerate();
	}
	
	/**
	 * Computes the average frame rate at which the preview callback is called.
	 * We will then use this average framerate with the MediaCodec.  
	 * Blocks the thread in which this function is called.
	 * mQuality.resX, mQuality.resY
	 *  在视频流编码处理 中通过setPreviewCallback添加对视频流进行处理，如
   　　mCamera.setPreviewCallback(new encoderVideo(mCamera.getParameters().getPreviewSize().width, 
     　　　　mCamera.getParameters().getPreviewSize().height,(ImageView) findViewById(R.id.ImageView2)));//①原生yuv420sp视频存储方式
   　　mCamera.setPreviewCallback(new encoderH264(mCamera.getParameters().getPreviewSize().width, 
     　　　　mCamera.getParameters().getPreviewSize().height)); //②x264编码方式 
  　　 mCamera.setPreviewCallback(mJpegPreviewCallback);  //③JPEG压缩方式
	 */
	byte[] h264 = new byte[m_width*m_height*3/2];
	byte[] rgb24 = new byte[m_width*m_height];
	public void measureFramerate() {
		final Semaphore lock = new Semaphore(0);

		final Camera.PreviewCallback callback = new Camera.PreviewCallback() {
			int i = 0, t = 0;
			long now, oldnow, count = 0;
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				//LogUtil.d("back", i+".........");
				//传递进来的data,默认是YUV420SP的
				i++;
				now = System.nanoTime()/1000;
				if (i>3) {
					t += now - oldnow;
					count++;
				}
				if (i>20) {
					mQuality.framerate = (int) (1000000/(t/count)+1);
					lock.release();
					i = 0;
				}
				oldnow = now;
				//LogUtil.d("back", i+".........");
				//byte[] outdata = rotateYUV420Degree90(data,m_width,m_height);
				//方法一   把yuv420数据推送给mediacodec，编码后取出nalu，写入到文件
				if(startEncodeNow){
					if(mEncoderColorFormat == 19){
						offerEncoderForYV12(data);
					}else{
						offerEncoderForNV21(data);
					}
				}
					
				//rotateYUV240SP(data,h264,m_width,m_height);
				//offerEncoder(data,h264);
				
				//第二方法用  bitmap.compress 可能是因为压缩过了 写入的文件不能播放  YV12格式数据怎么存储为图片
				if(startSnap){
					startSnap = false;
					Size size = camera.getParameters().getPreviewSize(); 
					if(mEncoderColorFormat == 19){//yv12需要转码
//						SDK.YV12toRGB24(data,data.length,fileName,size.width, size.height);
//						System.out.println(11);
						
//						EncodeTask mEncTask = new EncodeTask(data);
//						mEncTask.execute((Void) null);
						
					}else{
						try{  
					        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);  
					        if(image!=null){  
					            ByteArrayOutputStream stream = new ByteArrayOutputStream();  
					            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);  
					            Bitmap bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());  
					            stream.close();  
					            //测试不预览直接显示图片
					            AnalogvideoActivity.instance.snapPic(bitmap,fileName);
					        }  
					    }catch(Exception ex){  
					        LogUtil.e(TAG,"Error:"+ex.getMessage());  
					    }
					}
				}
			}
		};
		mCamera.setPreviewCallback(callback);
		
//		byte[] buf = new byte[m_width * m_height * 3 / 2];  
//		mCamera.addCallbackBuffer(buf);  
//		mCamera.setPreviewCallbackWithBuffer(callback);
		
//		int buffersize = m_width * m_height * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
//        byte[] previewBuffer = new byte[buffersize];
//        mCamera.addCallbackBuffer(previewBuffer);
//        mCamera.setPreviewCallbackWithBuffer(this);		

	}

	
	//获取压缩后的BYTE数据
	public synchronized byte[] getCompressBytes(Bitmap bitmap) {
		ByteArrayOutputStream baos = null ; 
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();  
				bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos); //这里压缩30%，把压缩后的数据存放到baos中 
				System.out.println("..........compress压缩后的长度："+baos.toByteArray().length);
				outputStream.write(baos.toByteArray(), 0, baos.toByteArray().length); //先写放 然后在解码看看能不能解
				return baos.toByteArray();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{  
            try {  
                if(baos != null)  
                    baos.close() ;  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
		return null;
	}

//	public byte[] Bitmap2Bytes(Bitmap bitmap) {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//		return baos.toByteArray();
//	}
    
	protected synchronized void createCamera() throws RuntimeException {
		if (mSurfaceView == null)
			throw new InvalidSurfaceException("Invalid surface !");
		if (mCamera == null) {
			openCamera();
			mUnlocked = false;
			mCamera.setErrorCallback(new Camera.ErrorCallback() {
				@Override
				public void onError(int error, Camera camera) {
					// On some phones when trying to use the camera facing front the media server will die
					// Whether or not this callback may be called really depends on the phone
					if (error == Camera.CAMERA_ERROR_SERVER_DIED) {
						// In this case the application must release the camera and instantiate a new one
						Log.e(TAG,"Media server died !");
						// We don't know in what thread we are so stop needs to be synchronized
						mCameraOpenedManually = false;
						stop();
					} else {
						Log.e(TAG,"Error unknown with the camera: "+error);
					}	
				}
			});
			try {
				Parameters parameters = mCamera.getParameters();
				if (parameters.getFlashMode()!=null) {
					parameters.setFlashMode(mFlashEnabled?Parameters.FLASH_MODE_TORCH:Parameters.FLASH_MODE_OFF);
				}
				parameters.setRecordingHint(true);
				mCamera.setParameters(parameters);
				//mCamera.setDisplayOrientation(mOrientation);//设置旋转度数
				try {
					mCamera.setPreviewDisplay(mSurfaceView.getHolder()); //显示预览
					//mCamera.setPreviewTexture(AnalogvideoActivity2.surfaceTexture); //不显示预览获取到数据帧
				} catch (IOException e) {
					e.printStackTrace();
					throw new InvalidSurfaceException("Invalid surface !");
				}
			} catch (RuntimeException e) {
				destroyCamera();
				throw e;
			}
		}
	}
	
	private void openCamera() throws RuntimeException {
		final Semaphore lock = new Semaphore(0);
		final RuntimeException[] exception = new RuntimeException[1];
		mCameraThread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				mCameraLooper = Looper.myLooper();
				try {
					mCamera = Camera.open(mCameraId);
					
					/*Camera.Parameters parameters = mCamera.getParameters();
					  List<Size> pictureSizes = parameters.getSupportedPictureSizes();  
				      List<Size> previewSizes = parameters.getSupportedPreviewSizes();  
				      //这个方法就可以显示出你的手机摄像头支持的范围
				      List<int[]> range = parameters.getSupportedPreviewFpsRange(); 
				      Log.d(TAG, "range:"+range.size()); 
				      for(int j=0;j<range.size();j++) { 
				      	int[] r=range.get(j); 
				      	for(int k=0;k<r.length;k++) { 
				      		LogUtil.d(TAG, TAG+r[k]); 
				      	} 
				      }
				      for(int i=0; i<pictureSizes.size(); i++){  
				          Size size = pictureSizes.get(i);  
				          LogUtil.i(TAG, "摄像头支持的pictureSizes: width = "+size.width+"*"+size.height);  
				      }  
				      for(int i=0; i<previewSizes.size(); i++){  
				          Size size = previewSizes.get(i);  
				          LogUtil.i(TAG, "  摄像头支持的previewSizes: width = "+size.width+"*"+size.height);  
				  
			          }*/
				} catch (RuntimeException e) {
					exception[0] = e;
				} finally {
					lock.release();
					Looper.loop();
				}
			}
		});
		mCameraThread.start();
		lock.acquireUninterruptibly();
		if (exception[0] != null) throw new CameraInUseException(exception[0].getMessage());
	}
	
	protected synchronized int updateCamera() throws RuntimeException {
		int ret = 0;
		if (mPreviewStarted) {
			mPreviewStarted = false;
			mCamera.stopPreview();
		}

		Parameters parameters = mCamera.getParameters();
		mQuality = VideoQuality.determineClosestSupportedResolution(parameters, mQuality);
		int[] max = VideoQuality.determineMaximumSupportedFramerate(parameters);
		parameters.setPreviewFormat(mCameraImageFormat);
		parameters.setPreviewSize(m_width, m_height);
		//parameters.setPreviewFpsRange(PreviewFpsRange,PreviewFpsRange);// 每秒显示20~30帧	 setPreviewFpsRange(1500, 1500)	固定15帧
		parameters.setPreviewFpsRange(max[0], max[1]);
		
		//parameters.setPictureFormat(mCameraImageFormat);           // 设置图片格式
		//parameters.setPictureSize(m_width, m_height);
		//Log.i(TAG+"initCamera", "after setting, previewframetate is " + mCamera.getParameters().getPreviewFrameRate());
		
		try {
			mCamera.setParameters(parameters); //这一句加上预览时图片拉伸了  注释后 看看解码后的数据正常不
			mCamera.startPreview();
			mPreviewStarted = true;
			ret = 0;
		} catch (RuntimeException e) {
			ret = -1;
			destroyCamera();
		}
		return ret;
	}
	 
	
	/** Stops the stream. */
	public synchronized void stop() {
		if (mCamera != null) {
			if (!mCameraOpenedManually) {
				destroyCamera();
			} else {
				try {
					startPreview();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void startPreview() {
		mCameraOpenedManually = true;
		if (!mPreviewStarted) {
			createCamera();
			updateCamera();
			try {
				mCamera.startPreview();
				mPreviewStarted = true;
			} catch (RuntimeException e) {
				destroyCamera();
				throw e;
			}
		}
	}
	
	public synchronized void destroyCamera() {
		if (mCamera != null) {
			lockCamera();
			mCamera.setPreviewCallback(null);
			//mCamera.setPreviewCallbackWithBuffer(null);//这种是用缓冲区的方法
			mCamera.stopPreview();
			try {
				mCamera.release();
			} catch (Exception e) {
				Log.e(TAG,e.getMessage()!=null?e.getMessage():"unknown error");
			}
			mCamera = null;
			mCameraLooper.quit();
			mUnlocked = false;
			mPreviewStarted = false;
		}	
	}
	
	protected void lockCamera() {
		if (mUnlocked) {
			Log.d(TAG,"Locking camera");
			try {
				mCamera.reconnect();
			} catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}
			mUnlocked = false;
		}
	}
	
	protected void unlockCamera() {
		if (!mUnlocked) {
			Log.d(TAG,"Unlocking camera");
			try {	
				mCamera.unlock();
			} catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}
			mUnlocked = true;
		}
	}
	
	/*//异步方法
	private class EncodeTask extends AsyncTask<Void, Void, Void> {
		private byte[] mData;
		// 构造函数
		EncodeTask(byte[] data) {
			this.mData = data;
		}
		@Override
		protected Void doInBackground(Void... params) {
			YV12ToRGB24 yv = new YV12ToRGB24(m_width, m_height,fileName);
			yv.convert(mData, rgb24);
			return null;
		}
	}*/


	
}


