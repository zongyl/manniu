package com.vss.vssmobile.decoder;

public class H264Dec
{
	static
	{
		try
		{
			System.loadLibrary("H264Android");
		}
		catch (UnsatisfiedLinkError ule)
		{
			System.out.println("loadLibrary(H264Android)," + ule.getMessage());
		}
	}

	public static synchronized native long InitDecoder();

	public static synchronized native long UninitDecoder(long handle);

	public static native int DecoderNal(long handle, byte[] in, int insize, int[] frameParam, byte[] out);
}
