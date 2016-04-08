package com.ctrl;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.manniu.manniu.R;

public class BottomCheck extends LinearLayout {
	private ImageView _image = null;
	private int _btnValue = 0;
	private int _imageID[] = new int[2];

	public BottomCheck(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 在构造函数中将Xml中定义的布局解析出来。
		LayoutInflater.from(context).inflate(R.layout.bottom_tab_btn, this, true);
		TypedArray params = context.obtainStyledAttributes(attrs, R.styleable.BottomCheck);
		// 取得
		_btnValue = params.getInteger(R.styleable.BottomCheck_BtnValue, 0);
		_imageID[0] = params.getResourceId(R.styleable.BottomCheck_NormalBkg, 0);
		_imageID[1] = params.getResourceId(R.styleable.BottomCheck_SelectedBkg, 0);

		params.recycle();
	}

	// 当View中所有的子控件 均被映射成xml后触发
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		_image = (ImageView) findViewById(R.id.tab_image);
		_image.setBackgroundResource(_imageID[_btnValue]);
		_image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				_btnValue = _btnValue == 0 ? 1 : 0;
				_image.setBackgroundResource(_imageID[_btnValue]);
			}
		});
	}
}
