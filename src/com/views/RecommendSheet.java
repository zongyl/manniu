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


public class RecommendSheet {

	public interface OnRecmSheetSelected {
		void onClick(int whichButton);
	}
	
	private RecommendSheet() {
	}

	public static Dialog showSheet(Context context, final OnRecmSheetSelected racmSheetSelected,
			OnCancelListener cancelListener) {
		final Dialog dlg = new Dialog(context, R.style.ActionSheet);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.new_recommend_item, null);
		final int cFullFillWidth = 10000;
		layout.setMinimumWidth(cFullFillWidth);
		TextView recmF = (TextView) layout.findViewById(R.id.recm_frid);
		TextView recmFs = (TextView) layout.findViewById(R.id.recm_frids);
		TextView mCancel = (TextView) layout.findViewById(R.id.cancel_share);
		
		recmF.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				racmSheetSelected.onClick(0);
			}
		});
		recmFs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				racmSheetSelected.onClick(1);
			}
		});
		
		
		mCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				racmSheetSelected.onClick(2);
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
