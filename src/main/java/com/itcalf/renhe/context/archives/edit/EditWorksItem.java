package com.itcalf.renhe.context.archives.edit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.archives.EditMyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.AddWorkInfoTask;
import com.itcalf.renhe.context.archives.edit.task.DeleteWorkInfoTask;
import com.itcalf.renhe.context.archives.edit.task.EditSimpleWorkTask;
import com.itcalf.renhe.context.archives.edit.task.EditWorkInfoTask;
import com.itcalf.renhe.context.relationship.selectindustry.SelectIndustryExpandableListActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.WorkExperienceInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.UserInfoUtil;
import com.itcalf.renhe.view.NoDayDatePickerDialog;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class EditWorksItem extends EditBaseActivity {
	private EditText companyEt;
	private EditText titleEt;
	private TextView industryTv;
	private EditText descriptionEt;
	private TextView startTimeTv;
	private TextView endTimeTv;
	private Button deleteBtn;
	private View separateLine;
	private RelativeLayout industryRl;
	private RelativeLayout descriptionRl;
	private RelativeLayout timeRl;
	private WorkExperienceInfo workInfo;
	private String company;
	private String title;
	private String industry;
	private String description;
	private String startYear;
	private String startMonth;
	private String endYear;
	private String endMonth;
	private int id;//工作经历id
	private int status = 1;//是否是当前职位，若为1 则代表是当前职位；若为0 则代表不是当前职位
	private boolean isAdd;//添加工作经历
	private boolean isSimple;//只编辑公司和职位
	private boolean isModify = false;
	private int industryId;
	private final static int INDUSTRY_REQUEST_CODE = 201;
	private final static int SAVE = 101;
	private final static int START_TIME = 10;
	private final static int END_TIME = 11;
	private int currentYear;
	private int currentMonth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.edit_work_item);
	}

	@Override
	protected void findView() {
		super.findView();
		companyEt = (EditText) findViewById(R.id.company_et);
		titleEt = (EditText) findViewById(R.id.title_et);
		industryTv = (TextView) findViewById(R.id.industry_tv);
		descriptionEt = (EditText) findViewById(R.id.description_et);
		startTimeTv = (TextView) findViewById(R.id.start_time_tv);
		endTimeTv = (TextView) findViewById(R.id.end_time_tv);
		deleteBtn = (Button) findViewById(R.id.delete_bt);
		separateLine = findViewById(R.id.separate_line);
		industryRl = (RelativeLayout) findViewById(R.id.industry_rl);
		descriptionRl = (RelativeLayout) findViewById(R.id.description_rl);
		timeRl = (RelativeLayout) findViewById(R.id.time_rl);
	}

	@Override
	protected void initData() {
		super.initData();
		setTextValue(R.id.title_txt, "工作经历");
		Calendar calendar = Calendar.getInstance();
		currentYear = calendar.get(Calendar.YEAR);
		currentMonth = calendar.get(Calendar.MONTH);
		isAdd = getIntent().getBooleanExtra("isAdd", false);
		if (isAdd) {
			deleteBtn.setVisibility(View.GONE);
		}
		isSimple = getIntent().getBooleanExtra("simple", false);
		if (isSimple) {
			separateLine.setVisibility(View.INVISIBLE);
			industryRl.setVisibility(View.GONE);
			descriptionRl.setVisibility(View.GONE);
			timeRl.setVisibility(View.GONE);
			deleteBtn.setVisibility(View.GONE);
			populateSimpleData();
		}

		workInfo = (WorkExperienceInfo) getIntent().getSerializableExtra("workInfo");
		if (workInfo != null) {
			populateData();
		}

		companyEt.addTextChangedListener(new EditTextListener());
		titleEt.addTextChangedListener(new EditTextListener());
		industryTv.addTextChangedListener(new EditTextListener());
		descriptionEt.addTextChangedListener(new EditTextListener());
		startTimeTv.addTextChangedListener(new EditTextListener());
		endTimeTv.addTextChangedListener(new EditTextListener());
	}

	private void populateSimpleData() {
		company = getIntent().getStringExtra("company");
		title = getIntent().getStringExtra("title");
		if (!TextUtils.isEmpty(company)) {
			companyEt.setText(company);
			companyEt.setSelection(company.length());
		}
		if (!TextUtils.isEmpty(title)) {
			titleEt.setText(title);
			titleEt.setSelection(title.length());
		}
	}

	private void populateData() {
		company = workInfo.getCompany();
		title = workInfo.getTitle();
		industry = workInfo.getIndustryName();
		industryId = workInfo.getIndustry();
		description = workInfo.getContent();
		startYear = workInfo.getStartYear();
		startMonth = workInfo.getStartMonth();
		endYear = workInfo.getEndYear();
		endMonth = workInfo.getEndMonth();
		id = workInfo.getId();
		status = workInfo.getStatus();
		if (!TextUtils.isEmpty(company)) {
			companyEt.setText(company);
			companyEt.setSelection(company.length());
		}
		if (!TextUtils.isEmpty(title)) {
			titleEt.setText(title);
			titleEt.setSelection(title.length());
		}
		if (!TextUtils.isEmpty(industry)) {
			industryTv.setText(industry);
		}
		if (!TextUtils.isEmpty(description)) {
			descriptionEt.setText(description);
			descriptionEt.setSelection(description.length());
		}
		if (!TextUtils.isEmpty(startYear) && !TextUtils.isEmpty(startMonth)) {
			startTimeTv.setText(startYear + "." + startMonth);
		}
		if (!TextUtils.isEmpty(endYear) && !TextUtils.isEmpty(endMonth)) {
			int endYearInt = Integer.parseInt(endYear);
			int endMonthInt = Integer.parseInt(endMonth);
			if (endYearInt == currentYear && endMonthInt == (currentMonth + 1)) {
				endTimeTv.setText("至今");
			} else {
				endTimeTv.setText(endYear + "." + endMonth);
			}
		}
	}

	@Override
	protected void initListener() {
		super.initListener();
		startTimeTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateDialog(START_TIME, startYear, startMonth);
			}
		});
		endTimeTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateDialog(END_TIME, endYear, endMonth);
			}
		});
		industryRl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditWorksItem.this, SelectIndustryExpandableListActivity.class);
				intent.putExtra("isFromArcheveEdit", true);
				intent.putExtra("selectedId", industryId);
				intent.putExtra("selectedIndustry", industry);
				startActivityForResult(intent, INDUSTRY_REQUEST_CODE);
				overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			}
		});
		deleteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(EditWorksItem.this);
				materialDialog.getBuilder(R.string.material_dialog_title, "确定删除该工作经历？")
						.callback(new MaterialDialog.ButtonCallback() {

					@Override
					public void onPositive(MaterialDialog dialog) {
						new DeleteWorkInfoTask(EditWorksItem.this) {
							public void doPre() {
								showDialog(SAVE);
							}

							public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
								if (result == null) {
									removeDialog(SAVE);
									ToastUtil.showToast(EditWorksItem.this, R.string.network_anomaly);
								} else if (result.getState() == -3) {
									removeDialog(SAVE);
									if (!TextUtils.isEmpty(result.getErrorInfo())) {
										Toast.makeText(EditWorksItem.this, result.getErrorInfo(), Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(EditWorksItem.this, "新增的工作信息不完整", Toast.LENGTH_SHORT).show();
									}

								} else if (result.getState() == 1) {
									new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
											getRenheApplication().getUserInfo().getSid(),
											getRenheApplication().getUserInfo().getSid(),
											getRenheApplication().getUserInfo().getAdSId());
									overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
								} else {
									removeDialog(SAVE);
									if (!TextUtils.isEmpty(result.getErrorInfo())) {
										Toast.makeText(EditWorksItem.this, result.getErrorInfo(), Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(EditWorksItem.this, R.string.sorry_of_unknow_exception, Toast.LENGTH_SHORT)
												.show();
									}
								}
							}
						}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId(), id + "");
					}

				});
				materialDialog.show();
			}
		});
	}

	private void showDateDialog(int tag, String year, String month) {
		Calendar calendar = Calendar.getInstance();
		int yearInt = calendar.get(Calendar.YEAR);
		int monthInt = calendar.get(Calendar.MONTH);
		int dayInt = calendar.get(Calendar.DAY_OF_MONTH);
		if (!TextUtils.isEmpty(year)) {
			yearInt = Integer.parseInt(year);
		}
		if (!TextUtils.isEmpty(month)) {
			monthInt = Integer.parseInt(month) - 1;
		}

		createDateDialog(tag, yearInt, monthInt, dayInt).show();
	}

	@Override
	public void goSave() {
		super.goSave();
		if (!checkPreSave()) {
			return;
		}
		if (isAdd && !isSimple) {
			new AddWorkInfoTask(EditWorksItem.this) {
				public void doPre() {
					showDialog(SAVE);
				}

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditWorksItem.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							Toast.makeText(EditWorksItem.this, result.getErrorInfo(), Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(EditWorksItem.this, "新增的工作信息不完整", Toast.LENGTH_SHORT).show();
						}

					} else if (result.getState() == 1) {
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					} else {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo()))
							ToastUtil.showToast(EditWorksItem.this, result.getErrorInfo());
						else
							Toast.makeText(EditWorksItem.this, R.string.sorry_of_unknow_exception, Toast.LENGTH_SHORT).show();
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), title, company, startYear + "", startMonth + "", endYear + "",
					endMonth + "", status + "", industryId + "", description);
		} else if (!isAdd && isSimple) {
			new EditSimpleWorkTask(EditWorksItem.this) {

				public void doPre() {
					showDialog(SAVE);
				}

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditWorksItem.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							ToastUtil.showToast(EditWorksItem.this, result.getErrorInfo());
						} else {
							ToastUtil.showToast(EditWorksItem.this, "公司或职位为空，请正确填写");
						}
					} else if (result.getState() == 1) {
						UserInfoUtil.chengeUserInfo(UserInfoUtil.COMPANY, company);
						UserInfoUtil.chengeUserInfo(UserInfoUtil.TITLE, title);
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					} else {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo()))
							ToastUtil.showToast(EditWorksItem.this, result.getErrorInfo());
						else
							Toast.makeText(EditWorksItem.this, R.string.sorry_of_unknow_exception, Toast.LENGTH_SHORT).show();
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(),

			getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getAdSId(), title, company);
		} else if (!isAdd && !isSimple)

		{
			new EditWorkInfoTask(EditWorksItem.this) {
				public void doPre() {
					showDialog(SAVE);
				}

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
					if (result == null) {
						removeDialog(SAVE);
						ToastUtil.showToast(EditWorksItem.this, R.string.network_anomaly);
					} else if (result.getState() == -3) {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo())) {
							ToastUtil.showToast(EditWorksItem.this, result.getErrorInfo());
						} else {
							ToastUtil.showToast(EditWorksItem.this, "编辑的工作信息不完整");
						}

					} else if (result.getState() == 1) {
						new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
								getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId());
					} else {
						removeDialog(SAVE);
						if (!TextUtils.isEmpty(result.getErrorInfo()))
							ToastUtil.showToast(EditWorksItem.this, result.getErrorInfo());
						else
							Toast.makeText(EditWorksItem.this, R.string.sorry_of_unknow_exception, Toast.LENGTH_SHORT).show();
					}
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), title, company, startYear + "", startMonth + "", endYear + "",
					endMonth + "", status + "", industryId + "", description, id + "");
		}

	}

	private boolean checkPreSave() {
		company = companyEt.getText().toString().trim();
		if (TextUtils.isEmpty(company)) {
			ToastUtil.showToast(EditWorksItem.this, "公司不能为空");
			companyEt.requestFocus();
			return false;
		}
		if (company.length() > Constants.WORK_COMPANY) {
			ToastUtil.showToast(EditWorksItem.this, "公司名称不能超过32字");
			companyEt.requestFocus();
			return false;
		}
		title = titleEt.getText().toString().trim();
		if (TextUtils.isEmpty(title)) {
			ToastUtil.showToast(EditWorksItem.this, "职位不能为空");
			titleEt.requestFocus();
			return false;
		}
		if (title.length() > Constants.WORK_TITLE) {
			ToastUtil.showToast(EditWorksItem.this, "职位名称不能超过32字");
			titleEt.requestFocus();
			return false;
		}
		if (!isSimple) {
			industry = industryTv.getText().toString().trim();
			if (TextUtils.isEmpty(industry)) {
				ToastUtil.showToast(EditWorksItem.this, "行业不能为空");
				return false;
			}
			description = descriptionEt.getText().toString().trim();
			if (description.length() > Constants.WORK_DESCRIPTION) {
				ToastUtil.showToast(EditWorksItem.this, "描述不能超过320字");
				descriptionEt.requestFocus();
				return false;
			}
			if (TextUtils.isEmpty(startYear) || TextUtils.isEmpty(startMonth)) {
				ToastUtil.showToast(EditWorksItem.this, "请填写入职时间");
				return false;
			}
			if (TextUtils.isEmpty(endYear) || TextUtils.isEmpty(endMonth)) {
				ToastUtil.showToast(EditWorksItem.this, "请填写离职时间");
				return false;
			}
			int startYearInt = Integer.parseInt(startYear);
			int startMonthInt = Integer.parseInt(startMonth);
			int endYearInt = Integer.parseInt(endYear);
			int endMonthInt = Integer.parseInt(endMonth);
			if (startYearInt > currentYear) {
				ToastUtil.showToast(EditWorksItem.this, "入职时间不能大于当前时间");
				return false;
			}
			if (startYearInt > endYearInt) {
				ToastUtil.showToast(EditWorksItem.this, "入职时间不能大于离职时间");
				return false;
			}
			if (startYearInt == endYearInt && startMonthInt > endMonthInt) {
				ToastUtil.showToast(EditWorksItem.this, "入职时间不能大于离职时间");
				return false;
			}
		}
		return true;
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case INDUSTRY_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				String yourIndustry = data.getStringExtra("yourindustry");
				String yourIndustryCodetemp = data.getStringExtra("yourindustrycode");
				if (yourIndustry != null && yourIndustryCodetemp != null) {
					industryTv.setText(yourIndustry);
					industryId = Integer.parseInt(yourIndustryCodetemp);
					industry = yourIndustry;
				}
			}
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SAVE:
			return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
		}
		return null;
	}

	class ProfileTask extends AsyncTask<String, Void, Profile> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Profile doInBackground(String... params) {
			try {
				return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2], EditWorksItem.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Profile result) {
			super.onPostExecute(result);
			removeDialog(SAVE);
			if (null != result) {
				if (1 == result.getState() && null != result.getUserInfo()) {
					Intent brocastIntent1 = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
					brocastIntent1.putExtra("Profile", result);
					sendBroadcast(brocastIntent1);
					if (!isSimple) {
						Intent brocastIntent2 = new Intent(EditMyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
						brocastIntent2.putExtra("Profile", result);
						sendBroadcast(brocastIntent2);
					}
					Intent intent = new Intent();
					intent.putExtra("Profile", result);
					setResult(RESULT_OK, intent);
					finish();
					overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				}
			} else {
				ToastUtil.showNetworkError(EditWorksItem.this);
			}
		}
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

	private NoDayDatePickerDialog createDateDialog(int tag, int currentYear, int currentMonth, int currentDay) {
		NoDayDatePickerDialog dpd = null;
		if (tag == START_TIME) {
			dpd = new NoDayDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					startYear = year + "";
					startMonth = (monthOfYear + 1) + "";
					startTimeTv.setText(startYear + "." + startMonth);
				}
			}, currentYear, currentMonth, currentDay);
			dpd.setTitle("请选择入职时间");
		} else if (tag == END_TIME) {
			dpd = new NoDayDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					if (year >= EditWorksItem.this.currentYear && monthOfYear >= EditWorksItem.this.currentMonth) {
						endYear = year + "";
						endMonth = (monthOfYear + 1) + "";
						endTimeTv.setText("至今");
						status = 1;
					} else {
						endYear = year + "";
						endMonth = (monthOfYear + 1) + "";
						endTimeTv.setText(endYear + "." + endMonth);
						status = 0;
					}
				}
			}, currentYear, currentMonth, currentDay);
			dpd.setTitle("请选择离职时间");
		}
		if (dpd != null) {
			dpd.hideDay();
		}
		return dpd;
	}
}
