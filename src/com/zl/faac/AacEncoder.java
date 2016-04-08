package com.zl.faac;

public class AacEncoder {
	static {
		System.loadLibrary("faac");
	}
	//[0]==hEncoder,[1]==maxInputSamples,[2]==maxOutputBytes
	public native long[] Open(int channels);
	//输入音频格式：16KHz，立体声，16Bit，Intel PCM
	public native byte[] Write(long hEncoder, int maxInputSamples, int maxOutputBytes, byte[] inputBuffer);
	public native int Close(long hEncoder);
}
