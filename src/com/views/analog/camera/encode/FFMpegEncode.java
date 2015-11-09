package com.views.analog.camera.encode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;

import com.utils.LogUtil;
import com.views.analog.camera.exceptions.CameraInUseException;
import com.views.analog.camera.exceptions.InvalidSurfaceException;

/**
 * @author: li_jianhua Date: 2015-8-24 下午4:07:42
 * To change this template use File | Settings | File Templates.
 * Description：
 */
public class FFMpegEncode {
	private String TAG = FFMpegEncode.class.getSimpleName();
	
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
	
	
	public void measureFramerate() {
		final Semaphore lock = new Semaphore(0);

		final Camera.PreviewCallback callback = new Camera.PreviewCallback() {
			int i = 0, t = 0;
			long now, oldnow, count = 0;
			@Override
			public void onPreviewFrame(byte[] data, Camera camera) {
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
				
				
					
			}
		};
		mCamera.setPreviewCallback(callback);

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
//				Parameters parameters = mCamera.getParameters();
//				if (parameters.getFlashMode()!=null) {
//					parameters.setFlashMode(mFlashEnabled?Parameters.FLASH_MODE_TORCH:Parameters.FLASH_MODE_OFF);
//				}
//				parameters.setRecordingHint(true);
//				mCamera.setParameters(parameters);
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
	
	protected synchronized int updateCamera() throws RuntimeException {
		int ret = 0;
		if (mPreviewStarted) {
			mPreviewStarted = false;
			mCamera.stopPreview();
		}

		Parameters parameters = mCamera.getParameters();
		mQuality = VideoQuality.determineClosestSupportedResolution(parameters, mQuality);
		int[] max = VideoQuality.determineMaximumSupportedFramerate(parameters);
		parameters.setPreviewFormat(ImageFormat.NV21);
		parameters.setPreviewSize(mRequestedQuality.resX, mRequestedQuality.resY);
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
	

}
