package com.ctrl;
/**
 * Created by IntelliJ IDEA. User: li_jianhua Date: 2014-7-29 上午8:45:01
 * To change this template use File | Settings | File Templates.
 * Description： 自定义 Button 实现图片+文字 
 */
import com.utils.Constants;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
//http://www.linuxidc.com/Linux/2013-05/85013p2.htm

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class XButton extends Button {

	public XButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public XButton(Context context) {
		super(context);
	}

	private Paint mPaint = null;
	private String mText;
	private int mX, mY;

	public void onSetText(String text, int nLeft, int nBottom, int nTextSize,
			int nTextColor) {
		mPaint = new Paint();
		mPaint.setTextSize(nTextSize);
		mPaint.setColor(nTextColor);
		this.mText = text;
		this.mX = nLeft;
		this.mY = nBottom;
	}

	@SuppressWarnings("unused")
	private int mDownBmpId, mUpBmpId;
	
	public void onSetBmp(int nDownID, int nUpID) {
		this.mDownBmpId = nDownID;
		this.mUpBmpId = nUpID;
	}

	public void setImageDrawable(int id){
		Drawable drawableTop = getResources().getDrawable(id);
		this.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop , null, null);
	}
	public void setImageDrawableLeft(int id){
		Drawable drawableLeft = getResources().getDrawable(id);
		this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null , null, null);
	}
	
	public void onDraw(Canvas canvas) {
		if (mPaint != null)
			canvas.drawText(mText, mX, mY, mPaint);
		super.onDraw(canvas);
	}

	public boolean onTouchEvent(MotionEvent event) {
//	     int x = (int) event.getX();
//	     int y = (int) event.getY();
//	     System.out.println("触笔点击坐标 : ("+Integer.toString(x)+","+Integer.toString(y)+")");
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			//super.setBackgroundResource(mDownBmpId);
			this.setBackgroundColor(Constants.COLOR_SELECTED);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			this.setBackgroundColor(Constants.COLOR_Transparent);
			this.getBackground().setAlpha(0);//0~255透明度值
			//super.setBackgroundResource(mUpBmpId);
		}
		return super.onTouchEvent(event);
	}

}