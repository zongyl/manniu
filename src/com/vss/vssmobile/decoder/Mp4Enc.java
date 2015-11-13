package com.vss.vssmobile.decoder;

//智诺 IPC   MP4 录像接口(暂时不用)
public class Mp4Enc
{
	public native static long getInstance();

	public native static long ReleaseInstance(long handle);

	public native static long startwrite(long handle, String filename);

	public native static long SetVideoFrameRate(long handle, long framerate);

	public native static long SetVideoSize(long handle, int width, int height);

	public native static long InsertVideoBuffer(long handle, byte[] b, long size);

	public native static long InsertAudioBuffer(long handle, byte[] b, long size);

	public native static long stop(long handle);

	public native static long StartRead(long handle, String filename);

//	static
//	{
//		try
//		{
//			System.loadLibrary("Mp4Enc");
//		}
//		catch (UnsatisfiedLinkError ule)
//		{
//			System.out.println("loadLibrary(Mp4Enc)," + ule.getMessage());
//		}
//	}

	public static long handle = getInstance();
}
