package com.basic;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.manniu.manniu.R;

/**
 * 日期时间选择控件
 */

@SuppressLint("SimpleDateFormat")
public class DateTimePickerDialog implements OnDateChangedListener,
		OnTimeChangedListener {
	private DatePicker datePicker;
	private TimePicker timePicker;
	// private NumberPicker secPicker;
	private AlertDialog ad;
	private String dateTime;
	private Activity activity;

	// int _second = 0;

	/**
	 * 日期时间弹出选择框构
	 * 
	 * @param activity
	 *            ：调用的父activity
	 */
	public DateTimePickerDialog(Activity activity) {
		this.activity = activity;
	}

	@SuppressLint("NewApi")
	public AlertDialog dateTimePicKDialog(final EditText dateTimeTextEdite,
			int type) {
		Calendar c = Calendar.getInstance();
		switch (type) {
		case 1:
			new DatePickerDialog(activity,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker datePicker, int year,
								int monthOfYear, int dayOfMonth) {
							Calendar calendar = Calendar.getInstance();
							calendar.set(datePicker.getYear(),
									datePicker.getMonth(),
									datePicker.getDayOfMonth());
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy-MM-dd");
							dateTime = sdf.format(calendar.getTime());
							dateTimeTextEdite.setText(dateTime);
						}
					}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DATE)).show();
			return null;
		case 2:
			new TimePickerDialog(activity,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker timePicker,
								int hourOfDay, int minute) {
							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.YEAR, Calendar.MONTH,
									Calendar.DAY_OF_MONTH,
									timePicker.getCurrentHour(),
									timePicker.getCurrentMinute());
							SimpleDateFormat sdf = new SimpleDateFormat(
									"HH:mm:ss");
							dateTime = sdf.format(calendar.getTime());
							dateTimeTextEdite.setText(dateTime);
						}
					},

					c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
					.show();
			return null;
		default:
			LinearLayout dateTimeLayout = (LinearLayout) activity
					.getLayoutInflater().inflate(R.layout.datetime, null);
			datePicker = (DatePicker) dateTimeLayout
					.findViewById(R.id.datepicker);
			timePicker = (TimePicker) dateTimeLayout
					.findViewById(R.id.timepicker);
			dateTime = dateTimeTextEdite.getText().toString();

			if (android.os.Build.VERSION.SDK_INT >= 14) {
				datePicker.setCalendarViewShown(false);
			}

			timePicker.setIs24HourView(true);
			int[] t = new int[6];
			if (G.StrToDate(dateTime, t)) {
				datePicker.init(t[0], t[1] - 1, t[2], this);
				timePicker.setCurrentHour(t[3]);
				timePicker.setCurrentMinute(t[4]);
			}

			timePicker.setOnTimeChangedListener(this);
			ad = new AlertDialog.Builder(activity)
					.setIcon(R.drawable.bg_pop_item)
					.setTitle(dateTime)
					.setView(dateTimeLayout)
					.setPositiveButton("设置",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dateTimeTextEdite.setText(dateTime);
								}
							}).setNegativeButton("取消",

					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
						}
					}).show();

			onDateChanged(null, 0, 0, 0);
			return ad;
		}
	}

	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		onDateChanged(null, 0, 0, 0);
	}

	public void onDateChanged(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(datePicker.getYear(), datePicker.getMonth(),
				datePicker.getDayOfMonth(), timePicker.getCurrentHour(),
				timePicker.getCurrentMinute(), 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateTime = sdf.format(calendar.getTime());
		ad.setTitle(dateTime);
	}

}