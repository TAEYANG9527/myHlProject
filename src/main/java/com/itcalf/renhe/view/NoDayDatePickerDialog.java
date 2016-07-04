package com.itcalf.renhe.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.DatePicker;

import java.lang.reflect.Field;
import java.util.Calendar;

public class NoDayDatePickerDialog extends DatePickerDialog {

	public NoDayDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
		super(context, callBack, year, monthOfYear, dayOfMonth);
	}

	@Override
	public void onDateChanged(DatePicker view, int year, int month, int day) {
		super.onDateChanged(view, year, month, day);
		setTitle(year + "年" + (month + 1) + "月");
	}

	/**
	 * 只显示年月，隐藏日期
	 */
	public void hideDay() {
		try {
			Field[] datePickerDialogFields = this.getClass().getSuperclass().getDeclaredFields();
			for (Field datePickerDialogField : datePickerDialogFields) {
				if (datePickerDialogField.getName().equals("mDatePicker")) {
					datePickerDialogField.setAccessible(true);
					DatePicker dp = (DatePicker) datePickerDialogField.get(this);
					Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
					for (Field datePickerField : datePickerFields) {
						if ("mDaySpinner".equals(datePickerField.getName())) {
							datePickerField.setAccessible(true);
							Object dayPicker = datePickerField.get(dp);
							((View) dayPicker).setVisibility(View.GONE);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 限定时间范围
	 */
	public void limiteDate() {
		Calendar calendar = Calendar.getInstance();
		int cuurrentYear = calendar.get(Calendar.YEAR);
		int cuurrentMonth = calendar.get(Calendar.MONTH);
		int cuurrentDay = calendar.get(Calendar.DAY_OF_MONTH);
		DatePicker datePicker = this.getDatePicker();
		datePicker.init(cuurrentYear, cuurrentMonth, cuurrentDay, new DatePicker.OnDateChangedListener() {

			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

				if (isDateAfter(view)) {
					Calendar mCalendar = Calendar.getInstance();
					view.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH),
							this);
				}
			}

			private boolean isDateAfter(DatePicker tempView) {
				Calendar mCalendar = Calendar.getInstance();
				Calendar tempCalendar = Calendar.getInstance();
				tempCalendar.set(tempView.getYear(), tempView.getMonth(), tempView.getDayOfMonth(), 0, 0, 0);
				if (tempCalendar.after(mCalendar))
					return true;
				else
					return false;
			}
		});
	}
}
