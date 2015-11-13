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

package net.majorkernelpanic.streaming.rtp;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.utils.LogUtil;
import com.views.bovine.Fun_AnalogVideo;

import net.majorkernelpanic.streaming.hw.AnalogvideoActivity;

import net.majorkernelpanic.streaming.MediaStream;

import P2P.SDK;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 *   RFC 3984.
 *   H.264 streaming over RTP.
 *   Must be fed with an InputStream containing H.264 NAL units preceded by their length (4 bytes).
 *   The stream must start with mpeg4 or 3gpp header, it will be skipped.
 *   
 */
public class H264Packetizer extends AbstractPacketizer implements Runnable {

	public final static String TAG = "H264Packetizer";

	private Thread t = null;
	private int naluLength = 0;
	private long delay = 0, oldtime = 0;
	private Statistics stats = new Statistics();
	public static byte[] sps = null, pps = null,_headAndSPSPPS = null;
	byte[] header = new byte[5];	
	private int count = 0;
	private int streamType = 1;
//	public RandomAccessFile raf = null;
	byte[] h264head={0,0,0,1};
	byte[] _pps = {104, -50, 6, -30};
	byte[] _sps = {103, 66, -128, 13, -38, 5, -126, 90, 1, -76, 40, 77, 64};
	
	byte[] _myBuffer = new byte[64*1024]; 
	byte[] outData_sps_pps = null;
	byte[] outData_head = new byte[_myBuffer.length + 4];
	
	public H264Packetizer() {
		super();
		//socket.setClockFrequency(90000);
		
		/*String filePath = Environment.getExternalStorageDirectory()+"/";
	     try {   
	            File file = new File(filePath+"my_stream.h264");   
	            if (file.exists())   
	                file.delete();   
	            raf = new RandomAccessFile(file, "rw");  
	            raf.write(h264head);
	            raf.write(_sps);
	            raf.write(h264head);
	            raf.write(_pps);
	        } catch (Exception ex) {   
	            Log.v("System.out", ex.toString());   
	        }*/
	     
	}
	
	//写文件可以不用SPS PPS 头
	public void recordFile(String filePath){
        try {  
//        	raf.close();
//        	raf = null;
            File file = new File(filePath);   
            if (file.exists())   
                file.delete();   
            raf = new RandomAccessFile(file, "rw"); 
            raf.write(h264head);
            raf.write(sps);
            raf.write(h264head);
            raf.write(pps);
            
           // _isRecording = true;
            //raf.close();
        } catch (Exception ex) {   
            Log.v("System.out", ex.toString());   
        }
	}

	public void start() {
		if (t == null) {
			isRunFlag = true;
			t = new Thread(this);
			t.start();
		}
	}

	public void stop() {
		if (t != null) {
			isRunFlag = false;
			dataFlag = 0;
			iFrameFlag = 0;
			try {
				//raf.close();
				is.close();
			} catch (IOException e) {}
			t.interrupt();
			try {
				t.join();
			} catch (InterruptedException e) {}
			t = null;
		}
	}
	
//	public void stopInputStream(){
//		try {
//			is.close();
//		} catch (IOException e) {}
//	}

	public void setStreamParameters(byte[] pps, byte[] sps) {
		this.pps = pps;
		this.sps = sps;
		
		byte[] sps1 = byteMerger(h264head ,sps);
		byte[] pps1 = byteMerger(h264head, pps);
        byte[] data = byteMerger(sps1 ,pps1);
        _headAndSPSPPS = byteMerger(data ,h264head);
        outData_sps_pps = new byte[_myBuffer.length + _headAndSPSPPS.length];
	}	

	public void run() {
		long duration = 0, delta2 = 0;
		Log.d(TAG,"H264 packetizer started !");
		stats.reset();
		count = 0;

		if (is instanceof MediaCodecInputStream) {
			streamType = 1;
			//socket.setCacheSize(0);
		} else {
			streamType = 0;	
			//socket.setCacheSize(400);
		}

		try {
			while (!Thread.interrupted()) {
				oldtime = System.nanoTime();
				// We read a NAL units from the input stream and we send them 
				if(isRunFlag) send();
				// We measure how long it took to receive NAL units from the phone  
				duration = System.nanoTime() - oldtime;
				
				// Every 3 secondes, we send two packets containing NALU type 7 (sps) and 8 (pps)
				// Those should allow the H264 stream to be decoded even if no SDP was sent to the decoder.	
				delta2 += duration/1000000;
				if (delta2>3000) {
					delta2 = 0;
					if (sps != null  && SDK._createChnlFlag == 0) {
						//AnalogvideoActivity.instance.avcCodec.setData(_headAndSPSPPS,_headAndSPSPPS.length);
						SDK.SendData(_headAndSPSPPS,_headAndSPSPPS.length,0,0,0);
					}
				}
				//super.send(rtphl+pps.length);
				stats.push(duration);
				// Computes the average duration of a NAL unit 
				delay = stats.average();
				//Log.d(TAG,"duration: "+duration/1000000+" delay: "+delay/1000000);

			}
		} catch (IOException e) {
			LogUtil.d(TAG," run. 11 "+e.getMessage());
		} catch (InterruptedException e) {
			LogUtil.d(TAG,"run 22222222222");
		}
		LogUtil.d(TAG,"H264 packetizer stopped !");
	}

	/**
	 * Reads a NAL unit in the FIFO and sends it.
	 * If it is too big, we split it in FU-A units (RFC 3984).
	 */
	int frame_size = 1024; 
	@SuppressLint("NewApi")
	private void send() throws IOException, InterruptedException {
		if (streamType == 0) {
			// NAL units are preceeded by their length, we parse the length NAL
			fill(header,0,5);
			ts += delay;
			naluLength = header[3]&0xFF | (header[2]&0xFF)<<8 | (header[1]&0xFF)<<16 | (header[0]&0xFF)<<24;
			if (naluLength>100000 || naluLength<0) resync();
		} else if (streamType == 1) {
			// NAL units are preceeded with 0x00000001
			fill(header,0,5);
			ts = ((MediaCodecInputStream)is).getLastBufferInfo().presentationTimeUs*1000L;
			//ts += delay;
			naluLength = is.available()+1;
			if (!(header[0]==0 && header[1]==0 && header[2]==0)) {
				// Turns out, the NAL units are not preceeded with 0x00000001
				Log.e(TAG, "NAL units are not preceeded by 0x00000001");
				streamType = 2; 
				return;
			}
		} else {
			// Nothing preceededs the NAL units
			fill(header,0,1);
			header[4] = header[0];
			ts = ((MediaCodecInputStream)is).getLastBufferInfo().presentationTimeUs*1000L;
			//ts += delay;
			naluLength = is.available()+1;
		}
		
		//System.out.println("naluLength..."+naluLength+"--"+header[4]);
		if(naluLength != 0){
			_myBuffer[0] = header[4];//帧类型
			sendEncoderData(_myBuffer,1,naluLength-1);
		}
	}

	private int fill(byte[] buffer, int offset,int length) throws IOException {
		int sum = 0, len;
		try {
			while (sum<length) {
				len = is.read(buffer, offset+sum, length-sum);
				if (len<0) {
					throw new IOException("fill...End of stream");
				}
				else sum+=len;
			}
		} catch (Exception e) {
			LogUtil.e(TAG,"fill...end of stream11.."+e.getMessage());
		}
		return sum;
	}
	
	private int sendEncoderData(byte[] buffer, int offset,int length) throws IOException {
		int sum = 0, len;
		while (sum<length) {
			try {
				len = is.read(buffer, offset+sum, length-sum);
				if (len<0) {
					throw new IOException("sendEncoderData  End of stream");
				}
				else sum+=len;
			} catch (Exception e) {
				LogUtil.e(TAG,"sendEncoderData.."+e.getMessage());
			}
			/*byte[] outData = new byte[_myBuffer.length + 4];  
            outData[0] = 0;  
            outData[1] = 0;  
        	outData[2] = 0; 
        	outData[3] = 1;
        	System.arraycopy(_myBuffer, 0, outData, 4, _myBuffer.length);
        	raf.write(outData,0,naluLength+4);*/
        	
			if(SDK._sessionId != 0 && SDK._createChnlFlag == 0){
        		//添加SPS PPS  src:源数组；srcPos:源数组要复制的起始位置；dest:目的数组；destPos:目的数组放置的起始位置；length:复制的长度。
        		if(iFrameFlag == 0 && _myBuffer[0] == 0x65){//I帧
        			iFrameFlag = 1;
				}
				if(iFrameFlag == 1 && dataFlag == 0){
					dataFlag = 1;
        			for (int j = 0; j < _headAndSPSPPS.length; j++) {
        				outData_sps_pps[j] = _headAndSPSPPS[j];
        			}
        			System.arraycopy(_myBuffer, 0, outData_sps_pps, _headAndSPSPPS.length, _myBuffer.length);
            		SDK.SendData(outData_sps_pps,naluLength+_headAndSPSPPS.length,0,0,1);
            		Log.i(TAG, "iiiiii"+outData_sps_pps[25]+"-"+outData_sps_pps[26]);
            		try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(dataFlag == 1){
					outData_head[0] = 0;  
					outData_head[1] = 0;  
					outData_head[2] = 0; 
					outData_head[3] = 1;
		        	System.arraycopy(_myBuffer, 0, outData_head, 4, _myBuffer.length);
		        	if(_myBuffer[0] == 0x65){
		        		SDK.SendData(outData_head,naluLength+4,0,0,1);
		        	}else{
		        		SDK.SendData(outData_head,naluLength+4,0,0,0);
		        	}
					Log.i(TAG, "发送数据...length.."+(naluLength+4));
				}
            }
			/*if(_isRecording && AnalogvideoActivity.instance._recordShow){//录像第一帧要是对   && _myBuffer[0] == 0x65
				if(flag == 0 && _myBuffer[0] == 0x65){//I帧
					flag = 1;
				}
				if(flag == 1){
					byte[] recData = new byte[_myBuffer.length + 4];  
					recData[0] = 0;  
					recData[1] = 0;  
					recData[2] = 0; 
					recData[3] = 1;
		        	System.arraycopy(_myBuffer, 0, recData, 4, _myBuffer.length);
		        	raf.write(recData,0,naluLength+4);
				}
			}*/
			//AnalogvideoActivity2.instance.avcCodec.setData(outData,naluLength+_headAndSPSPPS.length);
		}
		return sum;
	}
	
	public byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	private void resync() {
		int type;
		Log.i(TAG,"Packetizer out of sync ! Let's try to fix that...(NAL length: "+naluLength+")");
		while (true) {
			try {
				header[0] = header[1];
				header[1] = header[2];
				header[2] = header[3];
				header[3] = header[4];
				header[4] = (byte) is.read();
				
				type = header[4]&0x1F;

				if (type == 5 || type == 1) {
					naluLength = header[3]&0xFF | (header[2]&0xFF)<<8 | (header[1]&0xFF)<<16 | (header[0]&0xFF)<<24;
					if (naluLength>0 && naluLength<100000) {
						oldtime = System.nanoTime();
						Log.i(TAG,"A NAL unit may have been found in the bit stream !");
						break;
					}
					if (naluLength==0) {
						Log.i(TAG,"NAL unit with NULL size found...");
					} else if (header[3]==0xFF && header[2]==0xFF && header[1]==0xFF && header[0]==0xFF) {
						Log.i(TAG,"NAL unit with 0xFFFFFFFF size found...");
					}
				}
			} catch (Exception e) {
				Log.e(TAG,"resync()  header[4] = (byte) is.read()");
			}
		}
	}

}