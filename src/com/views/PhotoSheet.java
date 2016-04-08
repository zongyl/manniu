package com.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.manniu.manniu.R;


public class PhotoSheet {

	public interface OnActionSheetSelected {
		void onClick(int whichButton);
	}
	
	private PhotoSheet() {
	}

	public static Dialog showSheet(Context context, final OnActionSheetSelected actionSheetSelected,
			OnCancelListener cancelListener) {
		final Dialog dlg = new Dialog(context, R.style.ActionSheet);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.new_uploadphoto_items, null);
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth(cFullFillWidth);
		TextView takeF = (TextView) layout.findViewById(R.id.take_photo);
		TextView getF = (TextView) layout.findViewById(R.id.from_photo);
		//TextView useF = (TextView) layout.findViewById(R.id.use_weixin);
		TextView mCancel = (TextView) layout.findViewById(R.id.cancel_upload);
		
		takeF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick(0);
				dlg.dismiss();
			}
		});
		getF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick(1);
				dlg.dismiss();
			}
		});
		
		/*useF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick(2);
				dlg.dismiss();
			}
		});
		*/
		
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				actionSheetSelected.onClick(3);
				dlg.dismiss();
			}
		});

		Window w = dlg.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		final int cMakeBottom = -1000;
		lp.y = cMakeBottom;
		lp.gravity = Gravity.BOTTOM;
		dlg.onWindowAttributesChanged(lp);
		dlg.setCanceledOnTouchOutside(false);
		if (cancelListener != null)
			dlg.setOnCancelListener(cancelListener);
			dlg.setContentView(layout);
			dlg.show();
		return dlg;
	}

}
