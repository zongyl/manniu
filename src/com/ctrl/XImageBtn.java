package com.ctrl;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.basic.APP;
import com.manniu.manniu.R;

public class XImageBtn extends ImageButton {

	private Drawable _normal = null, _selected = null;

	public XImageBtn(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XImageBtn);
		_normal = a.getDrawable(R.styleable.XImageBtn_NormalImage);
		_selected = a.getDrawable(R.styleable.XImageBtn_SelectedImage);
		if (_selected == null) {
			_selected = _normal;
		}

		this.setBackgroundDrawable(null);
		this.setPadding(1, 1, 1, 1);
		// this.setOnClickListener(this);
		a.recycle();
	}

	public void SetImages(int normalId, int selectedId) {
		Resources res = APP.GetMainActivity().getResources();
		_normal = res.getDrawable(normalId);		
		_selected = res.getDrawable(selectedId);	
		this.refreshDrawableState();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		//int delta = isPressed() ? 1 : -1;
		if (isPressed()) {
			this.setImageDrawable(_selected);
		} else {
			this.setImageDrawable(_normal);
		}
	}
}