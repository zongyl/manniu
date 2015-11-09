package com.utils;
/**
 * @author: li_jianhua Date: 2015-7-18 上午9:45:41
 * To change this template use File | Settings | File Templates.
 * Description：
 */
public class StatisticsTime {
	
	
	public final static String TAG = "Statistics";
	
	private int count=700, c = 0;
	private float m = 0, q = 0;
	private long elapsed = 0;
	private long start = 0;
	private long duration = 0;
	private long period = 10000000000L;
	private boolean initoffset = false;
	
	public void Statistics() {}
	
	public void Statistics(int count, int period) {
		this.count = count;
		this.period = period;
	}
	
	public void reset() {
		initoffset = false;
		q = 0; m = 0; c = 0;
		elapsed = 0;
		start = 0;
		duration = 0;
	}
	
	public void push(long value) {
		elapsed += value;
		if (elapsed>period) {
			elapsed = 0;
			long now = System.nanoTime();
			if (!initoffset || (now - start < 0)) {
				start = now;
				duration = 0;
				initoffset = true;
			}
			// Prevents drifting issues by comparing the real duration of the 
			// stream with the sum of all temporal lengths of RTP packets. 
			value += (now - start) - duration;
			//Log.d(TAG, "sum1: "+duration/1000000+" sum2: "+(now-start)/1000000+" drift: "+((now-start)-duration)/1000000+" v: "+value/1000000);
		}
		if (c<5) {
			// We ignore the first 20 measured values because they may not be accurate
			c++;
			m = value;
		} else {
			m = (m*q+value)/(q+1);
			if (q<count) q++;
		}
	}
	
	public long average() {
		long l = (long)m;
		duration += l;
		return l;
	}
	

}
