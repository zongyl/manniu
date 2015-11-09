package com.views.analog.camera.audio;


public abstract interface IAudioData{
	/**
	 * 获取声音的回调
	 * @param buffer
	 * @param length
	*/
	public abstract void OnAudioData(byte[] buffer, int length);
}