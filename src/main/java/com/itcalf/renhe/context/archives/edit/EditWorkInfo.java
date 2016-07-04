package com.itcalf.renhe.context.archives.edit;

import android.app.AlertDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.AddWorkInfoTask;
import com.itcalf.renhe.context.archives.edit.task.DeleteWorkInfoTask;
import com.itcalf.renhe.context.archives.edit.task.EditWorkInfoTask;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryExpandableListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.WorkExperienceInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

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
public class EditWorkInfo extends EditBaseActivity {
	private RelativeLayout startTimeYearEt;
	private RelativeLayout startTimeMonthEt;
	private RelativeLayout stopTimeYearEt;
	private RelativeLayout stopTimeMonthEt;

	private TextView startTimeYearTv;
	private TextView startTimeMonthTv;
	private TextView stopTimeYearTv;
	private TextView stopTimeMonthTv;

	private EditText jobEt;
	private EditText companyEt;
	private CheckBox isCurrentJobCb;
	private LinearLayout startLl;
	private LinearLayout stopLl;
	private LinearLayout industryLl;
	private LinearLayout companyPropertyLl;
	private EditText companyWebSiteEt;
	private LinearLayout companyScaleLl;
	private EditText describeExperienceEt;

	private TextView industryTv;
	private TextView companyPropertyTv;
	private TextView companyScaleTv;

	public static final int START_YEAR_REQUEST = 0;
	public static final int START_MONTH_REQUEST = 1;
	public static final int STOP_YEAR_REQUEST = 2;
	public static final int STOP_MONTH_REQUEST = 3;

	public static final int COMPANY_PROPERTY = 4;
	public static final int COMPANY_SCALE = 5;

	private String[] times;
	private int startYearItem = 0;
	private int startMonthItem = 0;
	private int stopYearItem = 0;
	private int stopMonthItem = 0;
	private int companyPropertyItem = 0;
	private int companyScaleItem = 0;
	private int flag = START_YEAR_REQUEST;

	private final static int INDUSTRY_REQUEST_CODE = 11;
	private int industryCode = -1;
	private Button removeBt;
	private boolean isModify = false;

	private Profile pf;
	private final static int SAVE = 10;

	//新增工作
	private String title; // 职务
	private String company; // 公司
	private String content; // 工作经历描述

	/**
	 * 以下是档案编辑新增字段
	 * @return
	 */
	private int id;//工作经历id
	private String website;//公司网址
	private int startYear;//格式是“2014”
	private int startMonth;//格式是“8”
	private int endYear;
	private int endMonth;
	private String startYearString;//格式是“2014”
	private String startMonthString;//格式是“8”
	private String endYearString;
	private String endMonthString;
	private int status = 1;//是否是当前职位，若为1 则代表是当前职位，endYear和endMonth无效，表示目前还是这个岗位；若为0 则代表不是当前职位，endYear和endMonth有效
	private int industry;//公司行业id
	private String industryName;//公司行业
	private String industryString;//公司行业
	private int orgtype;//公司性质
	private String orgtypeString;//公司性质
	private int orgsize;//公司规模
	private String orgsizeString;//公司规模

	//编辑工作
	private WorkExperienceInfo editWorkInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.archieve_eidt_workinfo);

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("编辑工作经历"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("编辑工作经历"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();

		startTimeYearEt = (RelativeLayout) findViewById(R.id.start_time_year_et);
		startTimeMonthEt = (RelativeLayout) findViewById(R.id.start_time_month_et);
		stopTimeYearEt = (RelativeLayout) findViewById(R.id.stop_time_year_et);
		stopTimeMonthEt = (RelativeLayout) findViewById(R.id.stop_time_month_et);
		startTimeYearTv = (TextView) findViewById(R.id.start_time_year_tv);
		startTimeMonthTv = (TextView) findViewById(R.id.start_time_month_tv);
		stopTimeYearTv = (TextView) findViewById(R.id.stop_time_year_tv);
		stopTimeMonthTv = (TextView) findViewById(R.id.stop_time_month_tv);

		jobEt = (EditText) findViewById(R.id.name_et);
		companyEt = (EditText) findViewById(R.id.company_et);
		isCurrentJobCb = (CheckBox) findViewById(R.id.forwardCk);
		startLl = (LinearLayout) findViewById(R.id.start_ll);
		stopLl = (LinearLayout) findViewById(R.id.stop_ll);
		industryLl = (LinearLayout) findViewById(R.id.industry_ll);
		companyPropertyLl = (LinearLayout) findViewById(R.id.company_property_ll);
		companyWebSiteEt = (EditText) findViewById(R.id.company_website_et);
		companyScaleLl = (LinearLayout) findViewById(R.id.company_scale_ll);
		describeExperienceEt = (EditText) findViewById(R.id.contentEdt);

		industryTv = (TextView) findViewById(R.id.industry_et);
		companyPropertyTv = (TextView) findViewById(R.id.company_property_et);
		companyScaleTv = (TextView) findViewById(R.id.company_scale_et);
		removeBt = (Button) findViewById(R.id.remove_btn);

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
		if (getIntent().getBooleanExtra("addWork", false)) {
			setTextValue(R.id.title_txt, "添加工作经历");
			removeBt.setVisibility(View.GONE);
		} else {
			setTextValue(R.id.title_txt, "编辑工作经历");
			removeBt.setVisibility(View.VISIBLE);
			if (null != getIntent().getSerializableExtra("work")) {
				editWorkInfo = (WorkExperienceInfo) getIntent().getSerializableExtra("work");
			} else {
				editWorkInfo = new WorkExperienceInfo();
			}
			id = editWorkInfo.getId();
			status = editWorkInfo.getStatus();
			industry = editWorkInfo.getIndustry();
			jobEt.setText(editWorkInfo.getTitle());
			jobEt.setSelection(editWorkInfo.getTitle().length());
			companyEt.setText(editWorkInfo.getCompany());
			if (!TextUtils.isEmpty(editWorkInfo.getStartYear()) && !TextUtils.isEmpty(editWorkInfo.getStartMonth())) {
				startTimeYearTv.setText(editWorkInfo.getStartYear());
				startTimeMonthTv.setText(editWorkInfo.getStartMonth() + "月");
			}
			if (editWorkInfo.getStatus() == 1) {
				isCurrentJobCb.setChecked(true);
				stopLl.setVisibility(View.GONE);
			} else {
				isCurrentJobCb.setChecked(false);
				stopLl.setVisibility(View.VISIBLE);
				stopTimeYearTv.setText(editWorkInfo.getEndYear());
				stopTimeMonthTv.setText(editWorkInfo.getEndMonth() + "月");
			}
			//			industryTv.setText(editWorkInfo.geti)
			if (editWorkInfo.getOrgtype() > 0 && editWorkInfo.getOrgtype() <= 9) {
				orgtype = editWorkInfo.getOrgtype();
				companyPropertyTv.setText(getResources().getStringArray(R.array.compamy_property)[editWorkInfo.getOrgtype() - 1]);
			}
			if (editWorkInfo.getOrgsize() > 0 && editWorkInfo.getOrgsize() <= 9) {
				orgsize = editWorkInfo.getOrgsize();
				companyScaleTv.setText(getResources().getStringArray(R.array.compamy_scale)[editWorkInfo.getOrgsize() - 1]);
			}
			companyWebSiteEt.setText(editWorkInfo.getWebsite());
			describeExperienceEt.setText(editWorkInfo.getContent());
			industryName = editWorkInfo.getIndustryName();
			if (!TextUtils.isEmpty(industryName)) {
				industryTv.setText(editWorkInfo.getIndustryName());
			}
		}

		jobEt.addTextChangedListener(new EditTextListener());
		companyEt.addTextChangedListener(new EditTextListener());
		startTimeYearTv.addTextChangedListener(new EditTextListener());
		stopTimeMonthTv.addTextChangedListener(new EditTextListener());
		startTimeMonthTv.addTextChangedListener(new EditTextListener());
		stopTimeYearTv.addTextChangedListener(new EditTextListener());
		industryTv.addTextChangedListener(new EditTextListener());
		companyPropertyTv.addTextChangedListener(new EditTextListener());
		companyScaleTv.addTextChangedListener(new EditTextListener());
		companyWebSiteEt.addTextChangedListener(new EditTextListener());
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

		isCurrentJobCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				isModify = true;
				if (arg1) {
					status = 1;
					stopLl.setVisibility(View.GONE);
				} else {
					status = 0;
					stopLl.setVisibility(View.VISIBLE);
				}
			}
		});
		industryLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//				Intent intent = new Intent(EditWorkInfo.this, AdvanceSearchSelectIndustryMainActivity.class);
				Intent intent = new Intent(EditWorkInfo.this, SelectIndustryExpandableListActivity.class);

				intent.putExtra("isFromArcheveEdit", true);
				intent.putExtra("selectedId", industry);
				intent.putExtra("selectedIndustry", industryName);
				startActivityForResult(intent, INDUSTRY_REQUEST_CODE);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
		companyPropertyLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String propertyString = companyPropertyTv.getText().toString().trim();
				flag = COMPANY_PROPERTY;
				times = getData(flag);
				checkSelectedCompanyItem(flag, propertyString);
				showDialog(flag);
			}
		});
		companyScaleLl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String scaleString = companyScaleTv.getText().toString().trim();
				flag = COMPANY_SCALE;
				times = getData(flag);
				checkSelectedCompanyItem(flag, scaleString);
				showDialog(flag);
			}
		});
		removeBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Dialog alertDialog = new AlertDialog.Builder(EditWorkInfo.this).setTitle("提示").setMessage("确定删除该工作经历？")
						.setPositiveButton("删除", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new DeleteWorkInfoTask(EditWorkInfo.this) {
							public void doPre() {
								showDialog(SAVE);
							};

							public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
								if (result == null) {
									removeDialog(SAVE);
									ToastUtil.showToast(EditWorkInfo.this, R.string.network_anomaly);
								} else if (result.getState() == -3) {
									removeDialog(SAVE);
									if (!TextUtils.isEmpty(result.getErrorInfo())) {
										Toast.makeText(EditWorkInfo.this, result.getErrorInfo(), Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(EditWorkInfo.this, "新增的工作信息不完整", Toast.LENGTH_SHORT).show();
									}

								} else if (result.getState() == 1) {
									//更新本地缓存
									Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
									//											brocastIntent.putExtra("updateArchieve", true);
									sendBroadcast(brocastIntent);
									//											try {
									//												Thread.sleep(2000);
									//											} catch (InterruptedException e) {
									//												e.printStackTrace();
									//											}
									new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
											getRenheApplication().getUserInfo().getSid(),
											getRenheApplication().getUserInfo().getSid(),
											getRenheApplication().getUserInfo().getAdSId());
									//											Intent intent = new Intent();
									////											setResult(RESULT_OK, intent);
									//											removeDialog(SAVE);
									//											finish();
									//											overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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
		//		companyWebSiteEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		//			@Override
		//			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		//				return true;
		//			}
		//		});
	}

	@Override
	public void goBack() {
		super.goBack();
		if (isModify) {
			MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(this);
			materialDialog
					.getBuilder(R.string.material_dialog_title, R.string.is_save, R.string.material_dialog_sure,
							R.string.material_dialog_cancel, R.string.material_dialog_give_up)
					.callback(new MaterialDialog.ButtonCallback() {
						@Override
						public void onNeutral(MaterialDialog dialog) {
							finish();
							overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
						}

						@Override
						public void onPositive(MaterialDialog dialog) {
							goSave();
						}
					});
			materialDialog.show();
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
		if (getIntent().getBooleanExtra("addWork", false)) {
			new AddWorkInfoTask(EditWorkInfo.this) {
				public void doPre() {
					showDialog(SAVE);
				}

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditWorkInfo.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							Toast.makeText(EditWorkInfo.this, result.getErrorInfo(), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(EditWorkInfo.this, "新增的工作信息不完整", Toast.LENGTH_SHORT).show();
						}

					} else if (result.getState() == 1) {
						Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
						sendBroadcast(brocastIntent);
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), title, company, website, startYear + "", startMonth + "",
					endYear + "", endMonth + "", status + "", industry + "", orgtype + "", orgsize + "", content);
		} else {
			new EditWorkInfoTask(EditWorkInfo.this) {
				public void doPre() {
					showDialog(SAVE);
				}

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditWorkInfo.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							Toast.makeText(EditWorkInfo.this, result.getErrorInfo(), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(EditWorkInfo.this, "新增的工作信息不完整", Toast.LENGTH_SHORT).show();
						}

					} else if (result.getState() == 1) {
						Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
						sendBroadcast(brocastIntent);
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), title, company, website, startYear + "", startMonth + "",
					endYear + "", endMonth + "", status + "", industry + "", orgtype + "", orgsize + "", content, id + "");
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
		case COMPANY_PROPERTY:
			for (int i = 0; i < times.length; i++) {
				if (times[i].equals(name)) {
					companyPropertyItem = i;
				}
			}
			break;
		case COMPANY_SCALE:
			for (int i = 0; i < times.length; i++) {
				if (times[i].equals(name)) {
					companyScaleItem = i;
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		MaterialDialogsUtil materialDialog;
		switch (id) {
		case START_YEAR_REQUEST:
			//			Builder builder = new AlertDialog.Builder(this);
			//			builder.setTitle("年");
			//			final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
			//			builder.setSingleChoiceItems(times, startYearItem, choiceListener);
			//
			//			DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
			//				@Override
			//				public void onClick(DialogInterface dialogInterface, int which) {
			//					//					finish();
			//					dialogInterface.cancel();
			//				}
			//			};
			//			builder.setPositiveButton("取消", btnListener);
			//			dialog = builder.create();
			materialDialog = new MaterialDialogsUtil(this);
			materialDialog.showSingleChoice(R.string.years).items(times).itemsCallbackSingleChoice(startYearItem,
					new ChoiceOnClickListener());
			materialDialog.show();
			break;
		case START_MONTH_REQUEST:
			//			Builder monthBuilder = new AlertDialog.Builder(this);
			//			monthBuilder.setTitle("月份");
			//			final ChoiceOnClickListener monthChoiceListener = new ChoiceOnClickListener();
			//			monthBuilder.setSingleChoiceItems(times, startMonthItem, monthChoiceListener);
			//
			//			DialogInterface.OnClickListener monthBtnListener = new DialogInterface.OnClickListener() {
			//				@Override
			//				public void onClick(DialogInterface dialogInterface, int which) {
			//					//					finish();
			//					dialogInterface.cancel();
			//				}
			//			};
			//			monthBuilder.setPositiveButton("取消", monthBtnListener);
			//			dialog = monthBuilder.create();
			materialDialog = new MaterialDialogsUtil(this);
			materialDialog.showSingleChoice(R.string.months).items(times).itemsCallbackSingleChoice(startMonthItem,
					new ChoiceOnClickListener());
			materialDialog.show();
			break;
		case STOP_YEAR_REQUEST:
			//			Builder stopYearBuilder = new AlertDialog.Builder(this);
			//			stopYearBuilder.setTitle("年");
			//			final ChoiceOnClickListener stopYearchoiceListener = new ChoiceOnClickListener();
			//			stopYearBuilder.setSingleChoiceItems(times, stopYearItem, stopYearchoiceListener);
			//
			//			DialogInterface.OnClickListener stopYearbtnListener = new DialogInterface.OnClickListener() {
			//				@Override
			//				public void onClick(DialogInterface dialogInterface, int which) {
			//					//					finish();
			//					dialogInterface.cancel();
			//				}
			//			};
			//			stopYearBuilder.setPositiveButton("取消", stopYearbtnListener);
			//			dialog = stopYearBuilder.create();
			materialDialog = new MaterialDialogsUtil(this);
			materialDialog.showSingleChoice(R.string.years).items(times).itemsCallbackSingleChoice(stopYearItem,
					new ChoiceOnClickListener());
			materialDialog.show();
			break;
		case STOP_MONTH_REQUEST:
			//			Builder stopmonthBuilder = new AlertDialog.Builder(this);
			//			stopmonthBuilder.setTitle("月份");
			//			final ChoiceOnClickListener stopmonthChoiceListener = new ChoiceOnClickListener();
			//			stopmonthBuilder.setSingleChoiceItems(times, stopMonthItem, stopmonthChoiceListener);
			//
			//			DialogInterface.OnClickListener stopmonthBtnListener = new DialogInterface.OnClickListener() {
			//				@Override
			//				public void onClick(DialogInterface dialogInterface, int which) {
			//					//					finish();
			//					dialogInterface.cancel();
			//				}
			//			};
			//			stopmonthBuilder.setPositiveButton("取消", stopmonthBtnListener);
			//			dialog = stopmonthBuilder.create();
			materialDialog = new MaterialDialogsUtil(this);
			materialDialog.showSingleChoice(R.string.months).items(times).itemsCallbackSingleChoice(stopMonthItem,
					new ChoiceOnClickListener());
			materialDialog.show();
			break;
		case COMPANY_PROPERTY:
			//			Builder propertyBuilder = new AlertDialog.Builder(this);
			//			propertyBuilder.setTitle("公司性质");
			//			final ChoiceOnClickListener propertyChoiceListener = new ChoiceOnClickListener();
			//			propertyBuilder.setSingleChoiceItems(times, companyPropertyItem, propertyChoiceListener);
			//
			//			DialogInterface.OnClickListener propertyBtnListener = new DialogInterface.OnClickListener() {
			//				@Override
			//				public void onClick(DialogInterface dialogInterface, int which) {
			//					//					finish();
			//					dialogInterface.cancel();
			//				}
			//			};
			//			propertyBuilder.setPositiveButton("取消", propertyBtnListener);
			//			dialog = propertyBuilder.create();
			materialDialog = new MaterialDialogsUtil(this);
			materialDialog.showSingleChoice(R.string.company_property).items(times).itemsCallbackSingleChoice(companyPropertyItem,
					new ChoiceOnClickListener());
			materialDialog.show();
			break;
		case COMPANY_SCALE:
			//			Builder scaleBuilder = new AlertDialog.Builder(this);
			//			scaleBuilder.setTitle("公司规模");
			//			final ChoiceOnClickListener scaleChoiceListener = new ChoiceOnClickListener();
			//			scaleBuilder.setSingleChoiceItems(times, companyScaleItem, scaleChoiceListener);
			//
			//			DialogInterface.OnClickListener scaleBtnListener = new DialogInterface.OnClickListener() {
			//				@Override
			//				public void onClick(DialogInterface dialogInterface, int which) {
			//					//					finish();
			//					dialogInterface.cancel();
			//				}
			//			};
			//			scaleBuilder.setPositiveButton("取消", scaleBtnListener);
			//			dialog = scaleBuilder.create();
			materialDialog = new MaterialDialogsUtil(this);
			materialDialog.showSingleChoice(R.string.company_scale).items(times).itemsCallbackSingleChoice(companyScaleItem,
					new ChoiceOnClickListener());
			materialDialog.show();
			break;
		case SAVE:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
		}
		return null;
	}

	private class ChoiceOnClickListener implements MaterialDialog.ListCallbackSingleChoice {

		@Override
		public boolean onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
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
			case COMPANY_PROPERTY:
				companyPropertyTv.setText(times[which]);
				orgtype = which + 1;
				break;
			case COMPANY_SCALE:
				companyScaleTv.setText(times[which]);
				orgsize = which + 1;
				break;
			default:
				break;
			}
			return true;
		}
	}

	//	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {
	//
	//		private int which = 0;
	//
	//		@Override
	//		public void onClick(DialogInterface dialogInterface, int which) {
	//
	//			this.which = which;
	//			switch (flag) {
	//			case START_YEAR_REQUEST:
	//				int year; //获取当前年份
	//				try {
	//					year = Integer.parseInt(times[which]);
	//				} catch (NumberFormatException e) {
	//					year = getDefaultYear();
	//				}
	//				startTimeYearTv.setText(year + "");
	//				break;
	//			case START_MONTH_REQUEST:
	//				startTimeMonthTv.setText(times[which]);
	//				break;
	//			case STOP_YEAR_REQUEST:
	//				int stopyear; //获取当前年份
	//				try {
	//					stopyear = Integer.parseInt(times[which]);
	//				} catch (NumberFormatException e) {
	//					stopyear = getDefaultYear();
	//				}
	//				stopTimeYearTv.setText(stopyear + "");
	//				break;
	//			case STOP_MONTH_REQUEST:
	//				stopTimeMonthTv.setText(times[which]);
	//				break;
	//			case COMPANY_PROPERTY:
	//				companyPropertyTv.setText(times[which]);
	//				orgtype = which + 1;
	//				break;
	//			case COMPANY_SCALE:
	//				companyScaleTv.setText(times[which]);
	//				orgsize = which + 1;
	//				break;
	//			default:
	//				break;
	//			}
	//			dialogInterface.cancel();
	//		}
	//
	//		public int getWhich() {
	//			return which;
	//		}
	//	}

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
		} else if (flag == COMPANY_PROPERTY) {
			times = getResources().getStringArray(R.array.compamy_property);
			return times;
		} else {
			times = getResources().getStringArray(R.array.compamy_scale);
			return times;
		}
	}

	public int getDefaultYear() {
		final Calendar c = Calendar.getInstance();
		int defaultYear = c.get(Calendar.YEAR); //获取当前年份
		return defaultYear;
	}

	public int getDefaultMonth() {
		final Calendar c = Calendar.getInstance();
		int defaultYear = c.get(Calendar.MONTH); //获取当前年份
		return defaultYear + 1;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INDUSTRY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String yourIndustry = data.getStringExtra("yourindustry");
				String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
				String yourindustrySection = data.getStringExtra("yourindustrySection");
				if (yourIndustry != null && yourIndustryCodetemp != null) {
					industryTv.setText(yourIndustry);
					industryCode = Integer.parseInt(yourIndustryCodetemp);
					industry = industryCode;
					industryName = yourIndustry;
				}
			}
		}
	}

	private boolean checkPreSave() {
		title = jobEt.getText().toString().trim();
		if (TextUtils.isEmpty(title)) {
			Toast.makeText(EditWorkInfo.this, "职务不能为空", Toast.LENGTH_SHORT).show();
			jobEt.requestFocus();
			return false;
		}
		company = companyEt.getText().toString().trim();
		if (TextUtils.isEmpty(company)) {
			Toast.makeText(EditWorkInfo.this, "工作单位不能为空", Toast.LENGTH_SHORT).show();
			companyEt.requestFocus();
			return false;
		}
		startYearString = startTimeYearTv.getText().toString().trim();
		startMonthString = startTimeMonthTv.getText().toString().trim();
		if (TextUtils.isEmpty(startYearString) || TextUtils.isEmpty(startMonthString)) {
			Toast.makeText(EditWorkInfo.this, "入职时间填写不完整", Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			startYear = Integer.parseInt(startYearString);
		} catch (NumberFormatException e) {
			if (Constants.DEBUG_MODE) {
				Log.e("EditWork", "入职时间年份格式错误");
			}
			Toast.makeText(EditWorkInfo.this, "入职时间填写不完整", Toast.LENGTH_SHORT).show();
			return false;
		}
		try {
			String temMonth = startMonthString.substring(0, startMonthString.length() - 1);
			startMonth = Integer.parseInt(temMonth);

			if (startYear == getDefaultYear() && startMonth > getDefaultMonth()) {
				Toast.makeText(EditWorkInfo.this, "请填写一个不晚于本月的入职时间", Toast.LENGTH_SHORT).show();
				return false;
			}
		} catch (NumberFormatException e) {
			if (Constants.DEBUG_MODE) {
				Log.e("EditWork", "入职时间月份格式错误");
			}
			Toast.makeText(EditWorkInfo.this, "入职时间填写不完整", Toast.LENGTH_SHORT).show();
			return false;
		}

		if (status != 1) {
			endYearString = stopTimeYearTv.getText().toString().trim();
			endMonthString = stopTimeMonthTv.getText().toString().trim();
			if (TextUtils.isEmpty(endYearString) || TextUtils.isEmpty(endMonthString)) {
				Toast.makeText(EditWorkInfo.this, "离职时间填写不完整", Toast.LENGTH_SHORT).show();
				return false;
			}
			try {
				endYear = Integer.parseInt(endYearString);
			} catch (NumberFormatException e) {
				if (Constants.DEBUG_MODE) {
					Log.e("EditWork", "离职时间年份格式错误");
				}
				Toast.makeText(EditWorkInfo.this, "离职时间填写不完整", Toast.LENGTH_SHORT).show();
				return false;
			}
			try {
				String temMonth = endMonthString.substring(0, endMonthString.length() - 1);
				endMonth = Integer.parseInt(temMonth);

				if (endYear == getDefaultYear() && endMonth > getDefaultMonth()) {
					Toast.makeText(EditWorkInfo.this, "请填写一个不晚于本月的离职时间", Toast.LENGTH_SHORT).show();
					return false;
				}
			} catch (NumberFormatException e) {
				if (Constants.DEBUG_MODE) {
					Log.e("EditWork", "离职时间月份格式错误");
				}
				Toast.makeText(EditWorkInfo.this, "离职时间填写不完整", Toast.LENGTH_SHORT).show();
				return false;
			}
			if (endYear < startYear) {
				Toast.makeText(EditWorkInfo.this, "入职时间必须早于离职时间", Toast.LENGTH_SHORT).show();
				return false;
			} else if (endYear == startYear) {
				if (endMonth < startMonth) {
					Toast.makeText(EditWorkInfo.this, "入职时间必须早于离职时间", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		}

		industryString = industryTv.getText().toString().trim();
		if (TextUtils.isEmpty(industryString)) {
			Toast.makeText(EditWorkInfo.this, "从事行业不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		orgtypeString = companyPropertyTv.getText().toString().trim();
		if (TextUtils.isEmpty(orgtypeString)) {
			Toast.makeText(EditWorkInfo.this, "公司性质不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		website = companyWebSiteEt.getText().toString().trim();
		if (TextUtils.isEmpty(website)) {
			Toast.makeText(EditWorkInfo.this, "公司网站不能为空", Toast.LENGTH_SHORT).show();
			companyWebSiteEt.requestFocus();
			return false;
		}
		orgsizeString = companyScaleTv.getText().toString().trim();
		if (TextUtils.isEmpty(orgsizeString)) {
			Toast.makeText(EditWorkInfo.this, "公司规模不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		content = describeExperienceEt.getText().toString().trim();
		//		if (TextUtils.isEmpty(content)) {
		//			Toast.makeText(EditWorkInfo.this, "经历描述不能为空", Toast.LENGTH_SHORT).show();
		//			describeExperienceEt.requestFocus();
		//			return false;
		//		}
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
				return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2], EditWorkInfo.this);
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
				} else {
					removeDialog(SAVE);
				}
			} else {
				removeDialog(SAVE);
				ToastUtil.showNetworkError(EditWorkInfo.this);
			}
		}
	}
}
