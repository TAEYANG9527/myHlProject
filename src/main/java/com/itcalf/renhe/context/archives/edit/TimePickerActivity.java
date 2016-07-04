package com.itcalf.renhe.context.archives.edit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Title: TimePickerActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-21 下午3:14:48 <br>
 * @author wangning
 */
public class TimePickerActivity extends Activity {
	private final int YEAR_SING_CHOICE_DIALOG = 1;
	private final int MONTH_SING_CHOICE_DIALOG = 2;
	private String[] times;
	private int flag;
	private int selectedYear;
	private int selectedMonth;
	private int yearItem = 0;
	private int monthItem = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		flag = getIntent().getIntExtra("flag", EditWorkInfo.START_YEAR_REQUEST);
		times = getData(flag);
		switch (flag) {
		case EditWorkInfo.START_YEAR_REQUEST:
			selectedYear = getIntent().getIntExtra("startyear", getDefaultYear());
			checkSelectedItem(YEAR_SING_CHOICE_DIALOG, selectedYear, 1);
			showDialog(YEAR_SING_CHOICE_DIALOG);
			break;
		case EditWorkInfo.START_MONTH_REQUEST:
			selectedMonth = getIntent().getIntExtra("startmonth", 1);
			checkSelectedItem(MONTH_SING_CHOICE_DIALOG, getDefaultYear(), selectedMonth);
			showDialog(MONTH_SING_CHOICE_DIALOG);
			break;
		case EditWorkInfo.STOP_YEAR_REQUEST:
			selectedYear = getIntent().getIntExtra("stopyear", getDefaultYear());
			checkSelectedItem(YEAR_SING_CHOICE_DIALOG, selectedYear, 1);
			showDialog(YEAR_SING_CHOICE_DIALOG);
			break;
		case EditWorkInfo.STOP_MONTH_REQUEST:
			selectedMonth = getIntent().getIntExtra("stopmonth", 1);
			checkSelectedItem(MONTH_SING_CHOICE_DIALOG, getDefaultYear(), selectedMonth);
			showDialog(MONTH_SING_CHOICE_DIALOG);
			break;
		default:
			break;
		}

	}

	private void checkSelectedItem(int flag, int year, int month) {
		if (flag == YEAR_SING_CHOICE_DIALOG) {
			for (int i = 0; i < times.length; i++) {
				int time;
				try {
					time = Integer.parseInt(times[i]);
				} catch (NumberFormatException e) {
					time = getDefaultYear();
				}
				if (time == year) {
					yearItem = i;
				}
			}
		} else if (flag == MONTH_SING_CHOICE_DIALOG) {
			for (int i = 0; i < times.length; i++) {
				String time = month + "月";
				if (times[i].equals(time)) {
					monthItem = i;
				}
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case YEAR_SING_CHOICE_DIALOG:
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("年");
			final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
			builder.setSingleChoiceItems(times, yearItem, choiceListener);

			DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					int choiceWhich = choiceListener.getWhich();
					//					Intent intent = new Intent();
					//					final Calendar c = Calendar.getInstance();
					//					int year; //获取当前年份 
					//					try {
					//						year = Integer.parseInt(times[choiceWhich]);
					//					} catch (NumberFormatException e) {
					//						year = c.get(Calendar.YEAR);
					//					}
					//					intent.putExtra("year", year);
					//					setResult(RESULT_OK, intent);
					finish();
				}
			};
			//			builder.setPositiveButton("确定", btnListener);
			builder.setPositiveButton("取消", btnListener);
			dialog = builder.create();
			break;
		case MONTH_SING_CHOICE_DIALOG:
			Builder monthBuilder = new AlertDialog.Builder(this);
			monthBuilder.setTitle("月份");
			final ChoiceOnClickListener monthChoiceListener = new ChoiceOnClickListener();
			monthBuilder.setSingleChoiceItems(times, monthItem, monthChoiceListener);

			DialogInterface.OnClickListener monthBtnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					int choiceWhich = monthChoiceListener.getWhich();
					//					Intent intent = new Intent();
					//					intent.putExtra("month", times[choiceWhich]);
					//					setResult(RESULT_OK, intent);
					finish();
				}
			};
			//			monthBuilder.setPositiveButton("确定", monthBtnListener);
			monthBuilder.setPositiveButton("取消", monthBtnListener);
			dialog = monthBuilder.create();
			break;
		}
		return dialog;
	}

	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {

		private int which = 0;

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			this.which = which;
			if (flag == EditWorkInfo.START_YEAR_REQUEST || flag == EditWorkInfo.STOP_YEAR_REQUEST) {
				Intent intent = new Intent();
				final Calendar c = Calendar.getInstance();
				int year; //获取当前年份 
				try {
					year = Integer.parseInt(times[which]);
				} catch (NumberFormatException e) {
					year = c.get(Calendar.YEAR);
				}
				intent.putExtra("year", year);
				setResult(RESULT_OK, intent);
				finish();
			} else {
				Intent intent = new Intent();
				intent.putExtra("month", times[which]);
				setResult(RESULT_OK, intent);
				finish();
			}
		}

		public int getWhich() {
			return which;
		}
	}

	private String[] getData(int flag) {
		if (flag == EditWorkInfo.START_YEAR_REQUEST || flag == EditWorkInfo.STOP_YEAR_REQUEST) {
			int year;
			final Calendar c = Calendar.getInstance();
			year = c.get(Calendar.YEAR); //获取当前年份 
			String[] times = new String[year - 1954];
			int count = 0;
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = year; i >= 1955; i--) {
				if (count >= times.length) {
					break;
				} else {
					times[count++] = i + "";
				}
			}
			return times;
		} else {
			int month;
			final Calendar c = Calendar.getInstance();
			month = c.get(Calendar.MONTH); //获取当前年份 
			String[] times = new String[12];
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i <= 11; i++) {
				times[i] = (i + 1) + "月";
			}
			return times;
		}
	}

	public int getDefaultYear() {
		final Calendar c = Calendar.getInstance();
		int defaultYear = c.get(Calendar.YEAR); //获取当前年份 
		return defaultYear;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return false;
	}
}
