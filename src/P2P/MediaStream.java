package P2P;

import com.utils.Constants;
import com.utils.ExceptionsOperator;
import com.utils.LogUtil;

// 媒体流基类
public class MediaStream {
	public int NextTime = 40;  //下一次取帧毫秒数
	public int _nSpeed = 0;  //倍率
	public int _nFrameRate = 20; //帧率
	public int _playId = 0;
	protected boolean _isRecording = false;
	protected boolean _isSound = false;
	protected boolean _isPlaying = false;
	public MediaStream() {
	}

//	public abstract String GetTitle();
//	protected abstract int DoPlay(int []ret);
//	public abstract int openRealData();
	
//	public int Play(){
//		synchronized(this){
//			_isRecording = false;
//			_isSound = false;
//			_isPlaying = false;
//			
//			int []ret = new int[1];
//			_playId = DoPlay(ret);
//			if(_playId != 0){
//				_isPlaying = true;
//				return 0;
//			}else{
//				return ret[0];
//			}
//		}
//	}
	
	public void StopPlay() {
		try {
			synchronized(this){
				if (_playId != 0) {
					LogUtil.d("MediaStream","start..SDK.StopPlay(_playId)"+_playId);
					SDK.StopPlay(_playId);
					_playId = 0;
					_isRecording = false;
					_isSound = false;
					_isPlaying = false;
//					if (VSPlayer._timer != null) {
//						VSPlayer._timer.cancel();
//						VSPlayer._timer = null;
//					}
					LogUtil.d("MediaStream","end..SDK.StopPlay(_playId)"+_playId);
				}
			}
		} catch (Exception e) {
			LogUtil.e("MediaStream",ExceptionsOperator.getExceptionInfo(e));
		}
	}

	public int Refresh() {
		synchronized(this){
			if (_playId != 0) {
				//SDK.RefreshPlay(_playId);
			}
		}
		return 0;
	}

	public int PTZControl(int cmd, int nStep, int param) {
		synchronized(this){
		
		}
		return 0;
	}

	// int[] pInfo Width, Height, FrameCount, Rate
	// VSPlaya run 调 获取BMP32位图片   BMP图片有32位和24位的区别.
	public int GetBmp32Frame(byte[] pBmpBuffer, int[] pInfo) {
		int nRet = 1;
		synchronized(this){
			//if(_playId != 0 && _isPlaying){
				nRet = SDK.GetBmp32Framebyte(_playId, Constants.imgViewWidth, Constants.imgViewHeight,pBmpBuffer, pInfo);
				//nRet = SDK.GetBmp32Frame(_playId, Constants.imgViewWidth, Constants.imgViewHeight,pBmpBuffer, pInfo);
//				if(nRet == 0 && pInfo[0] == 0)
					//System.out.println(nRet +"--"+pInfo[0]+"  --"+ pInfo[1]+" ::"+pBmpBuffer.length);
				//System.out.println(nRet+"--"+_nFrameRate+ "--"+ pInfo[0]+"--"+pInfo[1]+"--"+Constants.imgViewWidth+"-"+Constants.imgViewHeight);
				if (nRet == 0 && pInfo[3] > 0 && pInfo[3] != _nFrameRate) {
					//System.out.println("GetBmp32Frame:  "+pInfo[0]+"--"+pInfo[1]+"--"+pBmpBuffer.length);
					_nFrameRate = pInfo[3];
					//System.out.println(nRet+"--"+_nFrameRate+ "--"+ pInfo[3]+"--"+_nFrameRate * 1.1);
					ParamChannge(_nSpeed, (int)(_nFrameRate * 1.1));
//					ParamChannge(_nSpeed, _nFrameRate);
				}
			//}
		}
		return nRet;
	}
	
	//获取声音
	public int GetAudioData(byte[] pBuffer, int[] pInfo) {
		int nRet = 1;
		synchronized(this){
			if(_playId != 0  && _isPlaying){
				//nRet = SDK.GetAudioData(_playId, pBuffer, pInfo);
			}
		}
		return nRet;
	}

//	public int StartRecording(String fileName) {
//		synchronized(this){
//			if (_playId == 0) {
//				return R.string.object_null;
//			}
//			if (!G.CreatePath(Fun_Setting.RecordPath)) {
//				return R.string.object_null;
//			}
//
//			int nRet = SDK.StartRecording(_playId, fileName);
//			if (nRet == 0) {
//				//SDK.SavePicFile(_playId, fileName + ".bmp");
//				_isRecording = true;
//			}
//			return nRet;
//		}
//	}
	
//	public void init
	
	public void StopRecording() {
		synchronized(this){
			if (_playId != 0) {
				//SDK.StopRecording(_playId);
				_isRecording = false;
			}
		}
	}

	public boolean SetSound(int nSound) {
		synchronized (this) {
			if (_playId != 0) {
				_isSound = nSound > 0;
				//SDK.SetSound(_playId, nSound);
				return true;
			}
		}
		return false;
	}

	public boolean IsSound() {
		return _isSound;
	}

	public boolean IsRecording() {
		return _isRecording;
	}

	public boolean IsPlay() {
		return _playId != 0;
	}
	
	public boolean IsPause() {
		return !_isPlaying;
	}

	public boolean Pause() {
		synchronized (this) {
			if (_playId != 0) {
				_isPlaying = !_isPlaying;
			}
		}
		return _isPlaying;
	}
	
	//快进
	public int Fast(){
		return SetSpeed(_nSpeed + 1);
	}
	//慢放
	public int Slow(){
		return SetSpeed(_nSpeed - 1);
	}
	
	public void Normal(){
		SetSpeed(0);
	}
	
//	public int GetPos(){
//		synchronized (this) {
//			if (_playId != 0) {
//				return SDK.GetPlayPos(_playId);
//			}
//		}
//		return 0;
//	}
	
	//实时视频暂停/恢复
	public void realPlayPause(int pause){
		if (_playId != 0) {
			//SDK.RealPlayPause(_playId, pause);
		}
	}
	
	public int SetPos(int pos){
		int nRet = 0;
		synchronized (this) {
			if (_playId != 0) {
				//nRet = SDK.SetPlayPos(_playId, pos);
				if(nRet != 0){
					_playId = 0;
					_isRecording = false;
					_isSound = false;
					_isPlaying = false;
				}
			}
		}
		return nRet;
	}

	//计算帧率(1000/20 1秒取50帧)
	protected synchronized void ParamChannge(int nSpeed, int nFrameRate){
		int newTime = 1;
		int ns = 0; 
		if(nSpeed > 0){
			ns = (int) Math.pow(2, nSpeed); 
			newTime = 1000 / (nFrameRate * ns);
		}else{
			ns = (int) Math.pow(2, -nSpeed); 
			newTime = 1000 * ns / nFrameRate;
		}
		if(newTime == 0) newTime = 40;
		NextTime = newTime;
	}

	// -8~8
	// -3 -2 -1 0 1 2 3   计算倍率
	public int SetSpeed(int nSpeed){
		if(nSpeed < -1 || nSpeed > 1){
			if(_nSpeed < 0){
				return -(int)Math.pow(2, -_nSpeed); 
			}
			return (int) Math.pow(2, _nSpeed); 
		}
		_nSpeed = nSpeed;
		ParamChannge(nSpeed, _nFrameRate);

		if(nSpeed < 0){
			return -(int) Math.pow(2, -nSpeed); 
		}
		return (int) Math.pow(2, nSpeed); 
	}
}
