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

package net.majorkernelpanic.streaming.gl;

import java.util.concurrent.Semaphore;

import net.majorkernelpanic.streaming.video.H264Stream;
import net.majorkernelpanic.streaming.video.VideoStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
//自定义 surfaceView 用于录制 mp4文件的 预览
public class V_SurfaceView extends android.view.SurfaceView implements SurfaceHolder.Callback {

	public final static String TAG = "GLSurfaceView";

	private TextureManager mTextureManager = null;
	

	public V_SurfaceView(Context context) {
		super(context);
		getHolder().addCallback(this);
	}	

	public SurfaceTexture getSurfaceTexture() {
		return mTextureManager.getSurfaceTexture();
	}

	

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		System.out.println(22);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println(111);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		if (mThread != null) {
//			mThread.interrupt();
//		}
		//mRunning = false;
	}

}
