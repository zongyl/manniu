package com.zl.faad;

public class AacDecoder {
	static {
		System.loadLibrary("faad");
	}
	//[0]==hDecoder,[1]==pbBuffer,[2]==pnBufferSize
	public native long[] Open(byte[] inputBuffer);
	//输出音频格式：32KHz，立体声，16Bit，Intel PCM
	public native byte[] Write(long hDecoder, long pbBuffer, long pnBufferSize, byte[] inputBuffer);
	public native int Close(long hDecoder, long pbBuffer, long pnBufferSize);
}
