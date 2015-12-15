/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.streaming.video;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Semaphore;
import com.basic.APP;
import com.basic.G;
import com.manniu.manniu.R;
import com.utils.ExceptionsOperator;
import com.utils.FileUtil;
import com.utils.LogUtil;
import com.utils.ScreenHandler;
import com.views.bovine.Fun_AnalogVideo;
import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.Stream;
import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.ConfNotSupportedException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.exceptions.StorageUnavailableException;
import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;
import net.majorkernelpanic.streaming.hw.EnCoderQueue;
import P2P.SDK;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** 
 * Don't use this class directly.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public abstract class VideoStream extends MediaStream {

	protected final static String TAG = "VideoStream";
	private Semaphore mLock = new Semaphore(0);
	public static VideoQuality mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
	protected VideoQuality mQuality = mRequestedQuality.clone(); 
	protected SurfaceHolder.Callback mSurfaceHolderCallback = null;
	//protected SurfaceView mSurfaceView = null;
	public SurfaceView mSurfaceView = null; 
	protected SharedPreferences mSettings = null;
	protected int mVideoEncoder, mCameraId = 0;//
	protected int mRequestedOrientation = 90, mOrientation = 90;//
	public Camera mCamera;
	protected Thread mCameraThread;
	protected Looper mCameraLooper;

	protected boolean mCameraOpenedManually = true;
	protected boolean mFlashEnabled = false;
	protected boolean mSurfaceReady = false;
	protected boolean mUnlocked = false;
	protected boolean mPreviewStarted = false;

	protected String mMimeType;
	protected String mEncoderName;
	protected int mEncoderColorFormat;
	protected int mCameraImageFormat;
	protected int mMaxFps = 0;	
	public boolean _startSnap = false;//截图
	public boolean _startScreen = false;//设置封面
	public boolean _isPreSize = false;//是否支持352*288
	
//	private AnalogHandler _handler = null;
//	private BaseApplication mAPP = null;

	/** 
	 * Don't use this class directly.
	 * Uses CAMERA_FACING_BACK by default.
	 */
	public VideoStream() {
		this(CameraInfo.CAMERA_FACING_BACK);
	}	

	/** 
	 * Don't use this class directly
	 * @param camera Can be either CameraInfo.CAMERA_FACING_BACK or CameraInfo.CAMERA_FACING_FRONT
	 */
	@SuppressLint("InlinedApi")
	public VideoStream(int camera) {
		super();
		setCamera(camera);
	}

	/**
	 * Sets the camera that will be used to capture video.
	 * You can call this method at any time and changes will take effect next time you start the stream.
	 * @param camera Can be either CameraInfo.CAMERA_FACING_BACK or CameraInfo.CAMERA_FACING_FRONT
	 */
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

	/**	Switch between the front facing and the back facing camera of the phone. 
	 * If {@link #startPreview()} has been called, the preview will be  briefly interrupted. 
	 * If {@link #start()} has been called, the stream will be  briefly interrupted.
	 * You should not call this method from the main thread if you are already streaming. 
	 * @throws IOException 
	 * @throws RuntimeException 
	 **/
	public void switchCamera() throws RuntimeException, IOException {
		if (Camera.getNumberOfCameras() == 1) throw new IllegalStateException("Phone only has one camera !");
		boolean streaming = mStreaming;
		boolean previewing = mCamera!=null && mCameraOpenedManually; 
		mCameraId = (mCameraId == CameraInfo.CAMERA_FACING_BACK) ? CameraInfo.CAMERA_FACING_FRONT : CameraInfo.CAMERA_FACING_BACK; 
		setCamera(mCameraId);
		stopPreview();
		mFlashEnabled = false;
		if (previewing) startPreview();
		if (streaming) start(); 
	}

	public int getCamera() {
		return mCameraId;
	}

	/**
	 * Sets a Surface to show a preview of recorded media (video). 
	 * You can call this method at any time and changes will take effect next time you call {@link #start()}.
	 */
	public synchronized void setSurfaceView(SurfaceView view) {
		mSurfaceView = view;
		/*if (mSurfaceHolderCallback != null && mSurfaceView != null && mSurfaceView.getHolder() != null) {
			mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
		}
		if (mSurfaceView.getHolder() != null) {
			mSurfaceHolderCallback = new Callback() {
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					mSurfaceReady = false;
					stopPreview();
					Log.d(TAG,"Surface destroyed !");
				}
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					mSurfaceReady = true;
				}
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					Log.d(TAG,"Surface Changed !");
				}
			};
			mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
			mSurfaceReady = true;
		}*/
	}

	/** Turns the LED on or off if phone has one. */
	public synchronized void setFlashState(boolean state) {
		// If the camera has already been opened, we apply the change immediately
		if (mCamera != null) {

			if (mStreaming && mMode == MODE_MEDIARECORDER_API) {
				lockCamera();
			}

			Parameters parameters = mCamera.getParameters();

			// We test if the phone has a flash
			if (parameters.getFlashMode()==null) {
				// The phone has no flash or the choosen camera can not toggle the flash
				throw new RuntimeException("Can't turn the flash on !");
			} else {
				parameters.setFlashMode(state?Parameters.FLASH_MODE_TORCH:Parameters.FLASH_MODE_OFF);
				try {
					mCamera.setParameters(parameters);
					mFlashEnabled = state;
				} catch (RuntimeException e) {
					mFlashEnabled = false;
					throw new RuntimeException("Can't turn the flash on !");
				} finally {
					if (mStreaming && mMode == MODE_MEDIARECORDER_API) {
						unlockCamera();
					}
				}
			}
		} else {
			mFlashEnabled = state;
		}
	}

	/** Toggle the LED of the phone if it has one. */
	public synchronized void toggleFlash() {
		setFlashState(!mFlashEnabled);
	}

	/** Indicates whether or not the flash of the phone is on. */
	public boolean getFlashState() {
		return mFlashEnabled;
	}

	/** 
	 * Sets the orientation of the preview.
	 * @param orientation The orientation of the preview
	 */
	public void setPreviewOrientation(int orientation) {
		mRequestedOrientation = orientation;
	}
	
	
	/** 
	 * Sets the configuration of the stream. You can call this method at any time 
	 * and changes will take effect next time you call {@link #configure()}.
	 * @param videoQuality Quality of the stream
	 */
	public void setVideoQuality(VideoQuality videoQuality) {
		mRequestedQuality = videoQuality.clone();
	}

	/** 
	 * Returns the quality of the stream.  
	 */
	public VideoQuality getVideoQuality() {
		return mRequestedQuality;
	}

	/**
	 * Some data (SPS and PPS params) needs to be stored when {@link #getSessionDescription()} is called 
	 * @param prefs The SharedPreferences that will be used to save SPS and PPS parameters
	 */
	public void setPreferences(SharedPreferences prefs) {
		mSettings = prefs;
		System.out.println(mSettings+"---");
	}

	/**
	 * Configures the stream. You need to call this before calling {@link #getSessionDescription()} 
	 * to apply your configuration of the stream.
	 */
	public synchronized void configure() throws IllegalStateException, IOException {
		super.configure();
		mOrientation = mRequestedOrientation;
	}	
	
	/**
	 * Starts the stream.
	 * This will also open the camera and dispay the preview 
	 * if {@link #startPreview()} has not aready been called.
	 */
	public synchronized void start() throws IllegalStateException, IOException {
		if (!mPreviewStarted) mCameraOpenedManually = false;
		super.start();
		Log.d(TAG,"Stream configuration: FPS: "+mQuality.framerate+" Width: "+mQuality.resX+" Height: "+mQuality.resY);
	}

	/** Stops the stream. */
	public synchronized void stop() {
		if (mCamera != null) {
			if (mMode == MODE_MEDIACODEC_API) {
				mCamera.setPreviewCallbackWithBuffer(null);
			}
			super.stop();
			// We need to restart the preview
			/*if (!mCameraOpenedManually) {
				destroyCamera();
			} else {
				try {
					startPreview();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}*/
		}
	}

	public synchronized void startPreview() 
			throws CameraInUseException, 
			InvalidSurfaceException, 
			ConfNotSupportedException, 
			RuntimeException {
		
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

	/**
	 * Stops the preview.
	 */
	public synchronized void stopPreview() {
		mCameraOpenedManually = false;
		stop();
	}

	/**
	 * Video encoding is done by a MediaRecorder.
	 */
	public void encodeWithMediaRecorder() throws IOException {
		Log.d(TAG,"Video encoded using the MediaRecorder API");

		// We need a local socket to forward data output by the camera to the packetizer
		createSockets();

		// Reopens the camera if needed
		destroyCamera();
		createCamera();
		//measureFramerate();
		// The camera must be unlocked before the MediaRecorder can use it
		unlockCamera();

		try {
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setCamera(mCamera);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setVideoEncoder(mVideoEncoder);
			mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
//			mMediaRecorder.setVideoSize(352,288);
//			mMediaRecorder.setVideoFrameRate(20);
//			mMediaRecorder.setVideoEncodingBitRate(1000000);
			
			mMediaRecorder.setVideoSize(mRequestedQuality.resX,mRequestedQuality.resY);
			System.out.println(mRequestedQuality.resX+"---"+mRequestedQuality.resY);
			mMediaRecorder.setVideoFrameRate(mRequestedQuality.framerate);
			mMediaRecorder.setVideoEncodingBitRate((int)(mRequestedQuality.bitrate*0.8));

			// We write the ouput of the camera in a local socket instead of a file !			
			// This one little trick makes streaming feasible quiet simply: data from the camera
			// can then be manipulated at the other end of the socket
			mMediaRecorder.setOutputFile(mSender.getFileDescriptor());

			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (Exception e) {
			LogUtil.e(TAG, new ConfNotSupportedException(e.getMessage()));
			return;
			//throw new ConfNotSupportedException(e.getMessage());
		}

		// This will skip the MPEG4 header if this step fails we can't stream anything :(
		outer: while (true) {
			InputStream is = mReceiver.getInputStream();
	    	while (true) { 
	    		try {
	    			byte buffer[] = new byte[4];
	    			// Skip all atoms preceding mdat atom
	    			while (true) {
	    				while (is.read() != 'm');
	    				is.read(buffer,0,3);
	    				if (buffer[0] == 'd' && buffer[1] == 'a' && buffer[2] == 't'){
	    					AnalogvideoActivity.instance.closeWait();
	    					break outer;
	    				} 
	    			}
	    		} catch (IOException e) {
	    			Log.e(TAG,"Couldn't skip mp4 header :/");
	    			continue outer;
	    		}
	    	}
	    }
		
		/*InputStream is = mReceiver.getInputStream();
		try {
			byte buffer[] = new byte[4];
			// Skip all atoms preceding mdat atom
			while (!Thread.interrupted()) {
				while (is.read() != 'm');
				is.read(buffer,0,3);
				if (buffer[0] == 'd' && buffer[1] == 'a' && buffer[2] == 't') break;
			}
		} catch (IOException e) {
			Log.e(TAG,"Couldn't skip mp4 header :/");
			stop();
			throw e;
		}*/

		// The packetizer encapsulates the bit stream in an RTP stream and send it over the network
		//mPacketizer.setDestination(mDestination, mRtpPort, mRtcpPort);
		mPacketizer.setInputStream(mReceiver.getInputStream());
		mPacketizer.start();
		mStreaming = true;
	}
	
	public void startThread(){
		mPacketizer.start();
	}
	
	public void stopThread(){
		mPacketizer.stop();
	}
	
//	public void startRecord(String name){
//		mPacketizer.recordFile(name);
//	}
	
	
	private BufferedOutputStream outputStream;
	public static byte[] toByteArray(InputStream input) throws IOException {
	    ByteArrayOutputStream output = new ByteArrayOutputStream();
	    byte[] buffer = new byte[1024 * 64];
	    int n = 0;
	    while (-1 != (n = input.read(buffer))) {
	        output.write(buffer, 0, n);
	    }
	    return output.toByteArray();
	}
	
	public void createFile(){
		File f = new File(Environment.getExternalStorageDirectory(), "/spydroid_encoded.h264");
	    touch (f);
	    try {
	        outputStream = new BufferedOutputStream(new FileOutputStream(f));
	        Log.i("AvcEncoder", "outputStream initialized");
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
	}
	public void touch(File f) {
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 public void releaseMediaRecorder() {   
	        Log.v(TAG, "Releasing media recorder.");   
	        if (mMediaRecorder != null) {   
	            if (mStreaming) {   
	                try {   
	                    mMediaRecorder.setOnErrorListener(null);   
	                    mMediaRecorder.setOnInfoListener(null);   
	                    mMediaRecorder.stop();   
	                } catch (RuntimeException e) {   
	                    Log.e(TAG, "stop fail: " + e.getMessage());   
	                }   
	                mStreaming = false;   
	            }   
	            mMediaRecorder.reset();   
	            mMediaRecorder.release();   
	            mMediaRecorder = null;   
	        }   
	    }
	 

	/**
	 * Returns a description of the stream using SDP. 
	 * This method can only be called after {@link Stream#configure()}.
	 * @throws IllegalStateException Thrown when {@link Stream#configure()} wa not called.
	 */	
	public abstract String getSessionDescription() throws IllegalStateException;

	/**
	 * Opens the camera in a new Looper thread so that the preview callback is not called from the main thread
	 * If an exception is thrown in this Looper thread, we bring it back into the main thread.
	 * @throws RuntimeException Might happen if another app is already using the camera.
	 */
	private void openCamera(){
		final RuntimeException[] exception = new RuntimeException[1];
		try {
			final Semaphore lock = new Semaphore(0);
			mCameraThread = new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					mCameraLooper = Looper.myLooper();
					try {
						mCamera = Camera.open(mCameraId);
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
			//if (exception[0] != null) throw new CameraInUseException(exception[0].getMessage());
		} catch (Exception e) {
			if (exception[0] != null) LogUtil.e(TAG, exception[0].getMessage());
		}
	}

	@SuppressLint("NewApi")
	protected synchronized void createCamera() throws RuntimeException {
		if (mSurfaceView == null)
			throw new InvalidSurfaceException("Invalid surface !");
//		if (mSurfaceView.getHolder() == null || !mSurfaceReady) 
//			throw new InvalidSurfaceException("Invalid surface !");

		if (mCamera == null) {
//			openCamera();
			mCamera = Camera.open(mCameraId);
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
				// If the phone has a flash, we turn it on/off according to mFlashEnabled
				// setRecordingHint(true) is a very nice optimisation if you plane to only use the Camera for recording
				Parameters parameters = mCamera.getParameters();
				//动态获取支持的prviewSize
				List<Size> previewSizes = parameters.getSupportedPreviewSizes(); 
		        for(int i=0; i<previewSizes.size(); i++){  
			          Size size = previewSizes.get(i);  
			          //LogUtil.d(TAG, "width*height="+size.width+"*"+size.height);
			          if(size.width == 352 && size.height == 288){
			        	  _isPreSize = true;
			        	  break;
			          }else{
			        	  _isPreSize = false;
			          }
		        }
		        if(!_isPreSize && mRequestedQuality.resX != 640){
		        	Fun_AnalogVideo.instance.updateRectype();
		        }
				
				mQuality = VideoQuality.determineClosestSupportedResolution(parameters, mQuality);
				if (parameters.getFlashMode()!=null) {
					parameters.setFlashMode(mFlashEnabled?Parameters.FLASH_MODE_TORCH:Parameters.FLASH_MODE_OFF);
				}
				parameters.setRecordingHint(true);
//				parameters.setPictureFormat(PixelFormat.JPEG);
//				parameters.setPictureSize(mRequestedQuality.resX, mRequestedQuality.resY);
				mCamera.setParameters(parameters);
				try {
					mCamera.setPreviewDisplay(mSurfaceView.getHolder());
				} catch (IOException e) {
					throw new InvalidSurfaceException("Invalid surface !");
				}
			} catch (RuntimeException e) {
				destroyCamera();
				throw e;
			}

		}
	}
	
	public PictureCallback BmpCallback = new PictureCallback() {
		public void onPictureTaken(byte[] _data, Camera _camera) {
			 //onPictureTaken传入的第一个参数即为相片的byte 
			try {
				Bitmap bm = BitmapFactory.decodeByteArray(_data, 0, _data.length);
				File myCaptureFile = new File(mPacketizer._fileName);
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
				// 采用压缩转档方法 
				bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				// 调用flush()方法，更新BufferStream 
				bos.flush();
				// 结束OutputStream 
				bos.close();
				if(mPacketizer._fileName.indexOf("images") != -1){
					File file = new File(Fun_AnalogVideo.instance.h.mPacketizer._fileName);
					if(file.isFile() && file.exists()){
						APP.ShowToast("截图成功");
					}
				}
			} catch (Exception e) {
				LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
			} catch (Error e){
				LogUtil.e(TAG, ExceptionsOperator.getErrorInfo(e));
			}
		}
	};

	public synchronized void destroyCamera() {
		try {
			if (mCamera != null) {
				mPacketizer.isRunFlag = false;
				if (mStreaming) super.stop();
				lockCamera();
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				try {
					mCamera.release();
				} catch (Exception e) {
					Log.e(TAG,e.getMessage()!=null?e.getMessage():"unknown error");
				}
				mCamera = null;
				//mCameraLooper.quit();
				mUnlocked = false;
				mPreviewStarted = false;
			}
		} catch (Exception e) {
			System.out.println(11);
		}
	}

	@SuppressLint("NewApi")
	protected synchronized void updateCamera() throws RuntimeException {
		if (mPreviewStarted) {
			mPreviewStarted = false;
			mCamera.stopPreview();
		}

		Parameters parameters = mCamera.getParameters();
//		List<int[]> range=parameters.getSupportedPreviewFpsRange();  
//        Log.d(TAG, "range:"+range.size());  
//        for(int j=0;j<range.size();j++) {  
//            int[] r=range.get(j);  
//            for(int k=0;k<r.length;k++) {  
//                Log.d(TAG, TAG+r[k]);  
//            }  
//        }  
//		mQuality = VideoQuality.determineClosestSupportedResolution(parameters, mQuality);
		int[] max = VideoQuality.determineMaximumSupportedFramerate(parameters);
		parameters.setPreviewFormat(mCameraImageFormat);
		parameters.setPreviewSize(mRequestedQuality.resX,mRequestedQuality.resY);
		parameters.setPreviewFpsRange(max[0], max[1]);
		//parameters.setPreviewFpsRange(mRequestedQuality.framerate * 1000, mRequestedQuality.framerate * 1000);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
		try {
			mCamera.setParameters(parameters);
			mCamera.startPreview();
			mCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上 
			//mCamera.setAutoFocusMoveCallback(null);
			mPreviewStarted = true;
		} catch (RuntimeException e) {
			destroyCamera();
			throw e;
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
	
	//录像测试
	public void recordMediaRecorderAPI(String fileName) throws RuntimeException, IOException {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new StorageUnavailableException("No external storage or external storage not ready !");
		}
		//mPacketizer.recordFile(fileName);
		
		// Save flash state & set it to false so that led remains off while testing h264
		boolean savedFlashState = mFlashEnabled;
		mFlashEnabled = false;
		boolean cameraOpen = mCamera!=null;
		createCamera();
		// Stops the preview if needed
		if (mPreviewStarted) {
			lockCamera();
			try {
				mCamera.stopPreview();
			} catch (Exception e) {}
			mPreviewStarted = false;
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		unlockCamera();

		try {
			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setCamera(mCamera);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mMediaRecorder.setVideoEncoder(mVideoEncoder);
			mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
			mMediaRecorder.setVideoSize(mRequestedQuality.resX,mRequestedQuality.resY);
			mMediaRecorder.setVideoFrameRate(mRequestedQuality.framerate);
			mMediaRecorder.setVideoEncodingBitRate((int)(mRequestedQuality.bitrate*0.8));
			
			mMediaRecorder.setOutputFile(fileName);//
			// We wait a little and stop recording
			mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
				public void onInfo(MediaRecorder mr, int what, int extra) {
					Log.d(TAG,"MediaRecorder callback called !");
					if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
						Log.d(TAG,"MediaRecorder: MAX_DURATION_REACHED");
					} else if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
						Log.d(TAG,"MediaRecorder: MAX_FILESIZE_REACHED");
					} else if (what==MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
						Log.d(TAG,"MediaRecorder: INFO_UNKNOWN");
					} else {
						Log.d(TAG,"WTF ?");
					}
					//mLock.release();
				}
			});

			// Start recording
			mMediaRecorder.prepare();
			mMediaRecorder.start();
//			if (mLock.tryAcquire(6,TimeUnit.SECONDS)) {
//				Log.d(TAG,"MediaRecorder callback was called :)");
//				Thread.sleep(400);
//			} else {
//				Log.d(TAG,"MediaRecorder callback was not called after 6 seconds... :(");
//			}
			
		} catch (IOException e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		} catch (RuntimeException e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}/*catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		 finally {
			/*try {
				mMediaRecorder.stop();
			} catch (Exception e) {}
			mMediaRecorder.release();
			mMediaRecorder = null;
			lockCamera();*/
			//if (!cameraOpen) destroyCamera();
			// Restore flash state
			mFlashEnabled = savedFlashState;
		}
	}
	
	public void stopRecorder(){
		try {
			mMediaRecorder.stop();
		} catch (Exception e) {}
		mMediaRecorder.release();
		mMediaRecorder = null;
		lockCamera();
		// Restore flash state
		//mFlashEnabled = savedFlashState;
	}
	
	//软编码
	public void encodeFFMpeg() throws RuntimeException, IOException {
		try {
			//createFile();
			// Updates the parameters of the camera if needed
			createCamera();
			updateCamera();
			//mCamera.autoFocus(null);//只有加上了这一句，才会自动对焦。
			// Estimates the framerate of the camera
			measureFramerate();
			SDK.Ffmpegh264EncoderInit(10, mRequestedQuality.bitrate,mRequestedQuality.resX, mRequestedQuality.resY);
		} catch (Exception e) {
			LogUtil.e(TAG, ExceptionsOperator.getExceptionInfo(e));
		}
	}

	/**
	 * Computes the average frame rate at which the preview callback is called.
	 * We will then use this average framerate with the MediaCodec.  
	 * Blocks the thread in which this function is called.
	 */
	public void measureFramerate() {
		final Semaphore lock = new Semaphore(0);
		final Camera.PreviewCallback callback = new Camera.PreviewCallback() {
			int i = 0, t = 0;
			long now, oldnow, count = 0;
			@SuppressWarnings("static-access")
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
				//传递进来的data,默认是YUV420SP的
				/*i++;
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
				oldnow = now;*/
				
				if(_startSnap){//截图
					try {
						//SDK.ScreenShotsNv21ToRgb24(data,mPacketizer._fileName,mRequestedQuality.resX,mRequestedQuality.resY);
						_startSnap = false;
		                Camera.Parameters parameters = camera.getParameters();
		                Size size = parameters.getPreviewSize();
		                YuvImage image = new YuvImage(data,parameters.getPreviewFormat(), size.width, size.height,null);
		                FileOutputStream filecon = new FileOutputStream(mPacketizer._fileName);
		                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()),90, filecon);
		                filecon.flush();
		                filecon.close();
		                if(mPacketizer._fileName.indexOf("images") != -1){
							File file = new File(Fun_AnalogVideo.instance.h.mPacketizer._fileName);
							if(file.isFile() && file.exists()){
								APP.ShowToast(APP.GetString(R.string.Video_snap_success));
							}
						}
		                //模拟抓图
		                if(SDK.analogPic){
		                	byte[] temData = FileUtil.getBytes(mPacketizer._fileName);
		                	SDK.SendSnapshotData(0,temData,temData.length);
		                	SDK.analogPic = false;
		                	sendImg(mPacketizer._fileName.replace(".bmp", ".jpg"));
		                }
		                if(_startScreen){//上传封面
		                	_startScreen = false;
		                	byte[] temData = FileUtil.getBytes(mPacketizer._fileName);
		                	byte[] imgByte = new byte[210]; 
		                	int ret = SDK.uploadlocalsnapshot(imgByte, 0, temData, temData.length);
		                	if(ret > 0){
		                		String str = G.BytesToStr(imgByte,0,ret);
		                		sendImg(str);
		                		APP.ShowToast(APP.GetString(R.string.SUCCESS_UPLOAD));
		                	}
		                }
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
				}
				
				if(EnCoderQueue.runFlag && AnalogvideoActivity.instance != null)
					AnalogvideoActivity.instance._encoderQueue.addSound(data);
			}
		};
		mCamera.setPreviewCallback(callback);

	}
	
	private void sendImg(String url){
		Message msg = new Message();
		Bundle tdata = new Bundle();
		tdata.putString("deviceId", AnalogvideoActivity.instance.deviceSid);
		tdata.putInt("result", 0);
		tdata.putString("type", "1");
		tdata.putString("url", url);
		msg.setData(tdata);
		new ScreenHandler().sendMessage(msg);
	}
	
	

}
