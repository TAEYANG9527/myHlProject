package com.itcalf.renhe.context.archives.edit;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.AddEduInfoTask;
import com.itcalf.renhe.context.archives.edit.task.DeleteEduInfoTask;
import com.itcalf.renhe.context.archives.edit.task.EditEduInfoTask;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.EduExperienceInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Title: EditSelfInfo.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-14 下午4:38:14 <br>
 * @author wangning
 */
public class EditEduInfoTemp extends EditBaseActivity {
	private RelativeLayout startTimeYearEt;
	private RelativeLayout startTimeMonthEt;
	private RelativeLayout stopTimeYearEt;
	private RelativeLayout stopTimeMonthEt;

	private TextView startTimeYearTv;
	private TextView startTimeMonthTv;
	private TextView stopTimeYearTv;
	private TextView stopTimeMonthTv;

	private LinearLayout startLl;
	private LinearLayout stopLl;
	private LinearLayout degreeLl;
	private TextView degreeTv;
	private LinearLayout companyPropertyLl;
	private EditText companyWebSiteEt;
	private LinearLayout companyScaleLl;
	private EditText describeExperienceEt;
	private LinearLayout schoolLl;
	private TextView schoolTv;
	private EditText studyFieldEt;
	private EditText socialActivityEdt;

	public static final int START_YEAR_REQUEST = 0;
	public static final int START_MONTH_REQUEST = 1;
	public static final int STOP_YEAR_REQUEST = 2;
	public static final int STOP_MONTH_REQUEST = 3;

	public static final int EDU_DEGREE = 4;
	public static final int COMPANY_SCALE = 5;

	private String[] times;
	private int startYearItem = 0;
	private int startMonthItem = 0;
	private int stopYearItem = 0;
	private int stopMonthItem = 0;
	private int eduDegreeItem = 0;
	private int flag = START_YEAR_REQUEST;

	private final static int INDUSTRY_REQUEST_CODE = 11;
	private final static int SCHOOL_REQUEST_CODE = 12;
	private int industryCode = -1;
	private Button removeBt;
	private boolean isModify = false;

	private Profile pf;
	private final static int SAVE = 10;

	//新增教育经历
	private String schoolName; // 学校
	private String studyField; // 专业
	private String content; // 学习经历描述

	/**
	 * 以下是档案编辑新增字段
	 * @return
	 */
	private int id;//学习经历id
	private int schoolId;//学校id
	private int startYear;//格式是“2014”
	private int startMonth;//格式是“8”
	private int endYear;
	private int endMonth;
	private String startYearString;//格式是“2014”
	private String startMonthString;//格式是“8”
	private String endYearString;
	private String endMonthString;
	private String degree;//学历
	private String activities;//社会活动

	//编辑教育经历
	private EduExperienceInfo editWorkInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.archieve_eidt_eduinfo);

	}

	@Override
	protected void findView() {
		super.findView();
		setTextValue(R.id.title_txt, "添加教育经历");
		startTimeYearEt = (RelativeLayout) findViewById(R.id.start_time_year_et);
		startTimeMonthEt = (RelativeLayout) findViewById(R.id.start_time_month_et);
		stopTimeYearEt = (RelativeLayout) findViewById(R.id.stop_time_year_et);
		stopTimeMonthEt = (RelativeLayout) findViewById(R.id.stop_time_month_et);
		startTimeYearTv = (TextView) findViewById(R.id.start_time_year_tv);
		startTimeMonthTv = (TextView) findViewById(R.id.start_time_month_tv);
		stopTimeYearTv = (TextView) findViewById(R.id.stop_time_year_tv);
		stopTimeMonthTv = (TextView) findViewById(R.id.stop_time_month_tv);

		degreeLl = (LinearLayout) findViewById(R.id.degree_ll);
		degreeTv = (TextView) findViewById(R.id.degree_et);

		schoolLl = (LinearLayout) findViewById(R.id.school_ll);
		schoolTv = (TextView) findViewById(R.id.school_et);
		removeBt = (Button) findViewById(R.id.remove_btn);
		studyFieldEt = (EditText) findViewById(R.id.profession_et);
		socialActivityEdt = (EditText) findViewById(R.id.social_activityEdt);
		describeExperienceEt = (EditText) findViewById(R.id.study_experEdt);
	}

	@Override
	protected void initData() {
		super.initData();
		if (getIntent().getSerializableExtra("Profile") != null) {
			pf = (Profile) getIntent().getSerializableExtra("Profile");
		} else {
			pf = (Profile) CacheManager.getInstance().populateData(this).getObject(getRenheApplication().getUserInfo().getEmail(),
					CacheManager.PROFILE);
		}
		if (getIntent().getBooleanExtra("addEdu", false)) {
			removeBt.setVisibility(View.GONE);
		} else {
			removeBt.setVisibility(View.VISIBLE);
			if (null != getIntent().getSerializableExtra("edu")) {
				editWorkInfo = (EduExperienceInfo) getIntent().getSerializableExtra("edu");
			} else {
				editWorkInfo = new EduExperienceInfo();
			}
			id = editWorkInfo.getId();
			schoolId = editWorkInfo.getSchoolId();
			schoolName = editWorkInfo.getSchoolName();
			studyField = editWorkInfo.getStudyField();
			degree = editWorkInfo.getDegree();
			if (!TextUtils.isEmpty(schoolName)) {
				schoolTv.setText(schoolName);
			}
			if (!TextUtils.isEmpty(studyField)) {
				studyFieldEt.setText(studyField);
				studyFieldEt.setSelection(studyField.length());
			}
			if (!TextUtils.isEmpty(degree)) {
				degreeTv.setText(degree);
			}
			startTimeYearTv.setText(editWorkInfo.getStartYear());
			startTimeMonthTv.setText(editWorkInfo.getStartMonth() + "月");
			stopTimeYearTv.setText(editWorkInfo.getEndYear());
			stopTimeMonthTv.setText(editWorkInfo.getEndMonth() + "月");
			activities = editWorkInfo.getActivities();
			content = editWorkInfo.getContent();
			if (!TextUtils.isEmpty(activities)) {
				socialActivityEdt.setText(activities);
			}
			if (!TextUtils.isEmpty(content)) {
				describeExperienceEt.setText(content);
			}
		}

		schoolTv.addTextChangedListener(new EditTextListener());
		studyFieldEt.addTextChangedListener(new EditTextListener());
		startTimeYearTv.addTextChangedListener(new EditTextListener());
		stopTimeMonthTv.addTextChangedListener(new EditTextListener());
		startTimeMonthTv.addTextChangedListener(new EditTextListener());
		stopTimeYearTv.addTextChangedListener(new EditTextListener());
		degreeTv.addTextChangedListener(new EditTextListener());
		socialActivityEdt.addTextChangedListener(new EditTextListener());
		describeExperienceEt.addTextChangedListener(new EditTextListener());
	}

	@Override
	protected void initListener() {
		super.initListener();
		startTimeYearEt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int year;
				try {
					year = Integer.parseInt(startTimeYearTv.getText().toString().trim());
				} catch (NumberFormatException e) {
					year = getDefaultYear();
				}
				flag = START_YEAR_REQUEST;
				times = getData(flag);
				checkSelectedItem(flag, year, 1);
				showDialog(START_YEAR_REQUEST);
			}
		});
		startTimeMonthEt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int month;
				try {
					String monthString = startTimeMonthTv.getText().toString().trim();
					if (TextUtils.isEmpty(monthString)) {
						month = 1;
					} else {
						String temMonth = monthString.substring(0, monthString.length() - 1);
						month = Integer.parseInt(temMonth);
					}
					;
				} catch (NumberFormatException e) {
					month = 1;
				}
				flag = START_MONTH_REQUEST;
				times = getData(flag);
				checkSelectedItem(flag, getDefaultYear(), month);
				showDialog(START_MONTH_REQUEST);
			}
		});
		stopTimeYearEt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int year;
				try {
					year = Integer.parseInt(stopTimeYearTv.getText().toString().trim());
				} catch (NumberFormatException e) {
					year = getDefaultYear();
				}
				flag = STOP_YEAR_REQUEST;
				times = getData(flag);
				checkSelectedItem(flag, year, 1);
				showDialog(STOP_YEAR_REQUEST);
			}
		});
		stopTimeMonthEt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int month;
				try {
					String monthString = stopTimeMonthTv.getText().toString().trim();
					if (TextUtils.isEmpty(monthString)) {
						month = 1;
					} else {
						String temMonth = monthString.substring(0, monthString.length() - 1);
						month = Integer.parseInt(temMonth);
					}
				} catch (NumberFormatException e) {
					month = 1;
				}
				flag = STOP_MONTH_REQUEST;
				times = getData(flag);
				checkSelectedItem(flag, getDefaultYear(), month);
				showDialog(STOP_MONTH_REQUEST);
			}
		});

		degreeLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String scaleString = degreeTv.getText().toString().trim();
				flag = EDU_DEGREE;
				times = getData(flag);
				checkSelectedCompanyItem(flag, scaleString);
				showDialog(flag);
			}
		});
		removeBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Dialog alertDialog = new AlertDialog.Builder(EditEduInfoTemp.this).setTitle("提示").setMessage("确定删除该教育经历？")
						.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new DeleteEduInfoTask(EditEduInfoTemp.this) {
							public void doPre() {
								showDialog(SAVE);
							};

							public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
								if (result == null) {
									removeDialog(SAVE);
									ToastUtil.showToast(EditEduInfoTemp.this, R.string.network_anomaly);
								} else if (result.getState() == -3) {
									removeDialog(SAVE);
									if (!TextUtils.isEmpty(result.getErrorInfo())) {
										ToastUtil.showToast(EditEduInfoTemp.this, result.getErrorInfo());
									} else {
										ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error);
									}

								} else if (result.getState() == 1) {
									Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
									sendBroadcast(brocastIntent);
									new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
											getRenheApplication().getUserInfo().getSid(),
											getRenheApplication().getUserInfo().getSid(),
											getRenheApplication().getUserInfo().getAdSId());
								}
							};
						}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId(), id + "");

					}
				}).setNeutralButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
				alertDialog.show();

			}
		});
	}

	@Override
	public void goBack() {
		super.goBack();
		if (isModify) {
			Dialog alertDialog = new AlertDialog.Builder(EditEduInfoTemp.this).setTitle("提示").setMessage("有更改尚未保存，是否保存更改的内容？")
					.setPositiveButton("保存", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							goSave();
						}
					}).setNegativeButton("放弃", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
							overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
						}
					}).setNeutralButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create();

			alertDialog.show();
		} else {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}

	@Override
	public void goSave() {
		super.goSave();
		if (!checkPreSave()) {
			return;
		}
		if (getIntent().getBooleanExtra("addEdu", false)) {
			new AddEduInfoTask(EditEduInfoTemp.this) {
				public void doPre() {
					showDialog(SAVE);
				};

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditEduInfoTemp.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							ToastUtil.showToast(EditEduInfoTemp.this, result.getErrorInfo());
						} else {
							ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error);
						}

					} else if (result.getState() == 1) {
						Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
						sendBroadcast(brocastIntent);
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					}
				};
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), schoolId + "", studyField, degree, startYear + "",
					startMonth + "", endYear + "", endMonth + "", activities, content + "");
		} else {
			new EditEduInfoTask(EditEduInfoTemp.this) {
				public void doPre() {
					showDialog(SAVE);
				};

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditEduInfoTemp.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							ToastUtil.showToast(EditEduInfoTemp.this, result.getErrorInfo());
						} else {
							ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error);
						}

					} else if (result.getState() == 1) {
						Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
						sendBroadcast(brocastIntent);
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					}
				};
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), id + "", schoolId + "", studyField, startYear + "",
					startMonth + "", endYear + "", endMonth + "", degree, activities, content);
		}
	}

	private void checkSelectedItem(int flag, int year, int month) {
		switch (flag) {
		case START_YEAR_REQUEST:
			for (int i = 0; i < times.length; i++) {
				int time;
				try {
					time = Integer.parseInt(times[i]);
				} catch (NumberFormatException e) {
					time = getDefaultYear();
				}
				if (time == year) {
					startYearItem = i;
				}
			}
			break;
		case START_MONTH_REQUEST:
			for (int i = 0; i < times.length; i++) {
				String time = month + "月";
				if (times[i].equals(time)) {
					startMonthItem = i;
				}
			}
			break;
		case STOP_YEAR_REQUEST:
			for (int i = 0; i < times.length; i++) {
				int time;
				try {
					time = Integer.parseInt(times[i]);
				} catch (NumberFormatException e) {
					time = getDefaultYear();
				}
				if (time == year) {
					stopYearItem = i;
				}
			}
			break;
		case STOP_MONTH_REQUEST:
			for (int i = 0; i < times.length; i++) {
				String time = month + "月";
				if (times[i].equals(time)) {
					stopMonthItem = i;
				}
			}
			break;

		default:
			break;
		}
	}

	private void checkSelectedCompanyItem(int flag, String name) {
		switch (flag) {
		case EDU_DEGREE:
			for (int i = 0; i < times.length; i++) {
				if (times[i].equals(name)) {
					eduDegreeItem = i;
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case START_YEAR_REQUEST:
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("年");
			final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
			builder.setSingleChoiceItems(times, startYearItem, choiceListener);

			DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					finish();
					dialogInterface.cancel();
				}
			};
			builder.setPositiveButton("取消", btnListener);
			dialog = builder.create();
			break;
		case START_MONTH_REQUEST:
			Builder monthBuilder = new AlertDialog.Builder(this);
			monthBuilder.setTitle("月份");
			final ChoiceOnClickListener monthChoiceListener = new ChoiceOnClickListener();
			monthBuilder.setSingleChoiceItems(times, startMonthItem, monthChoiceListener);

			DialogInterface.OnClickListener monthBtnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					finish();
					dialogInterface.cancel();
				}
			};
			monthBuilder.setPositiveButton("取消", monthBtnListener);
			dialog = monthBuilder.create();
			break;
		case STOP_YEAR_REQUEST:
			Builder stopYearBuilder = new AlertDialog.Builder(this);
			stopYearBuilder.setTitle("年");
			final ChoiceOnClickListener stopYearchoiceListener = new ChoiceOnClickListener();
			stopYearBuilder.setSingleChoiceItems(times, stopYearItem, stopYearchoiceListener);

			DialogInterface.OnClickListener stopYearbtnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					finish();
					dialogInterface.cancel();
				}
			};
			stopYearBuilder.setPositiveButton("取消", stopYearbtnListener);
			dialog = stopYearBuilder.create();
			break;
		case STOP_MONTH_REQUEST:
			Builder stopmonthBuilder = new AlertDialog.Builder(this);
			stopmonthBuilder.setTitle("月份");
			final ChoiceOnClickListener stopmonthChoiceListener = new ChoiceOnClickListener();
			stopmonthBuilder.setSingleChoiceItems(times, stopMonthItem, stopmonthChoiceListener);

			DialogInterface.OnClickListener stopmonthBtnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					finish();
					dialogInterface.cancel();
				}
			};
			stopmonthBuilder.setPositiveButton("取消", stopmonthBtnListener);
			dialog = stopmonthBuilder.create();
			break;
		case EDU_DEGREE:
			Builder propertyBuilder = new AlertDialog.Builder(this);
			propertyBuilder.setTitle("学历");
			final ChoiceOnClickListener propertyChoiceListener = new ChoiceOnClickListener();
			propertyBuilder.setSingleChoiceItems(times, eduDegreeItem, propertyChoiceListener);

			DialogInterface.OnClickListener propertyBtnListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int which) {
					//					finish();
					dialogInterface.cancel();
				}
			};
			propertyBuilder.setPositiveButton("取消", propertyBtnListener);
			dialog = propertyBuilder.create();
			break;
		case SAVE:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
		}
		return dialog;
	}

	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {

		private int which = 0;

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {

			this.which = which;
			switch (flag) {
			case START_YEAR_REQUEST:
				int year; //获取当前年份 
				try {
					year = Integer.parseInt(times[which]);
				} catch (NumberFormatException e) {
					year = getDefaultYear();
				}
				startTimeYearTv.setText(year + "");
				break;
			case START_MONTH_REQUEST:
				startTimeMonthTv.setText(times[which]);
				break;
			case STOP_YEAR_REQUEST:
				int stopyear; //获取当前年份 
				try {
					stopyear = Integer.parseInt(times[which]);
				} catch (NumberFormatException e) {
					stopyear = getDefaultYear();
				}
				stopTimeYearTv.setText(stopyear + "");
				break;
			case STOP_MONTH_REQUEST:
				stopTimeMonthTv.setText(times[which]);
				break;
			case EDU_DEGREE:
				degreeTv.setText(times[which]);
				break;
			default:
				break;
			}
			dialogInterface.cancel();
		}

		public int getWhich() {
			return which;
		}
	}

	private String[] getData(int flag) {
		if (flag == EditEduInfoTemp.START_YEAR_REQUEST || flag == EditEduInfoTemp.STOP_YEAR_REQUEST) {
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
		} else if (flag == START_MONTH_REQUEST || flag == STOP_MONTH_REQUEST) {
			int month;
			final Calendar c = Calendar.getInstance();
			month = c.get(Calendar.MONTH); //获取当前年份 
			String[] times = new String[12];
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (int i = 0; i <= 11; i++) {
				times[i] = (i + 1) + "月";
			}
			return times;
		} else if (flag == EDU_DEGREE) {
			times = getResources().getStringArray(R.array.education_degree);
			return times;
		} else {
			times = new String[0];
			return times;
		}
	}

	public int getDefaultYear() {
		final Calendar c = Calendar.getInstance();
		int defaultYear = c.get(Calendar.YEAR); //获取当前年份 
		return defaultYear;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INDUSTRY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String yourIndustry = data.getStringExtra("yourindustry");
				String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
				String yourindustrySection = data.getStringExtra("yourindustrySection");
				if (yourIndustry != null && yourIndustryCodetemp != null && yourindustrySection != null) {
					//					industryTv.setText(yourIndustry);
					industryCode = Integer.parseInt(yourIndustryCodetemp);
				}
			}
		} else if (requestCode == SCHOOL_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String yourSchool = data.getStringExtra("name");
				int yourSchoolId = data.getIntExtra("id", EditEduInfoSelectSchool.NO_THIS_SCHOOL);
				if (!TextUtils.isEmpty(yourSchool)) {
					schoolTv.setText(yourSchool);
				}
			}
		}
	}

	private boolean checkPreSave() {
		schoolName = schoolTv.getText().toString().trim();
		if (TextUtils.isEmpty(schoolName)) {
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error1);
			return false;
		}
		studyField = studyFieldEt.getText().toString().trim();
		if (TextUtils.isEmpty(studyField)) {
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error2);
			studyFieldEt.requestFocus();
			return false;
		}
		startYearString = startTimeYearTv.getText().toString().trim();
		startMonthString = startTimeMonthTv.getText().toString().trim();
		if (TextUtils.isEmpty(startYearString) || TextUtils.isEmpty(startMonthString)) {
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error3);
			return false;
		}
		try {
			startYear = Integer.parseInt(startYearString);
		} catch (NumberFormatException e) {
			if (Constants.DEBUG_MODE) {
				Log.e("EditWork", "入学时间年份格式错误");
			}
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error4);
			return false;
		}
		try {
			String temMonth = startMonthString.substring(0, startMonthString.length() - 1);
			startMonth = Integer.parseInt(temMonth);
		} catch (NumberFormatException e) {
			if (Constants.DEBUG_MODE) {
				Log.e("EditWork", "入学时间月份格式错误");
			}
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error4);
			return false;
		}
		endYearString = stopTimeYearTv.getText().toString().trim();
		endMonthString = stopTimeMonthTv.getText().toString().trim();
		if (TextUtils.isEmpty(endYearString) || TextUtils.isEmpty(endMonthString)) {
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error5);
			return false;
		}
		try {
			endYear = Integer.parseInt(endYearString);
		} catch (NumberFormatException e) {
			if (Constants.DEBUG_MODE) {
				Log.e("EditWork", "毕业时间年份格式错误");
			}
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error5);
			return false;
		}
		try {
			String temMonth = endMonthString.substring(0, endMonthString.length() - 1);
			endMonth = Integer.parseInt(temMonth);
		} catch (NumberFormatException e) {
			if (Constants.DEBUG_MODE) {
				Log.e("EditWork", "毕业时间月份格式错误");
			}
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error5);
			return false;
		}
		if (endYear < startYear) {
			ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error6);
			return false;
		} else if (endYear == startYear) {
			if (endMonth < startMonth) {
				ToastUtil.showToast(EditEduInfoTemp.this, R.string.edit_edu_error6);
				return false;
			}
		}
		activities = socialActivityEdt.getText().toString().trim();
		content = describeExperienceEt.getText().toString().trim();
		return true;
	}

	class EditTextListener implements TextWatcher {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			isModify = true;
		}

	}

	class ProfileTask extends AsyncTask<String, Void, Profile> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Profile doInBackground(String... params) {
			try {
				return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2],
						EditEduInfoTemp.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Profile result) {
			super.onPostExecute(result);

			if (null != result) {
				if (1 == result.getState() && null != result.getUserInfo()) {
					Intent intent = new Intent();
					intent.putExtra("Profile", result);
					setResult(RESULT_OK, intent);
					removeDialog(SAVE);
					finish();
					overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				}
			} else {
				ToastUtil.showNetworkError(EditEduInfoTemp.this);
			}
		}
	}
}
