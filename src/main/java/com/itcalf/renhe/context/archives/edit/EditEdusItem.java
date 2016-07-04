package com.itcalf.renhe.context.archives.edit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.archives.EditMyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.AddEduInfoTask;
import com.itcalf.renhe.context.archives.edit.task.DeleteEduInfoTask;
import com.itcalf.renhe.context.archives.edit.task.EditEduInfoTask;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.dto.Profile.UserInfo.EduExperienceInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.view.NoDayDatePickerDialog;

import java.util.Calendar;
import java.util.concurrent.Executors;

public class EditEdusItem extends EditBaseActivity {
    private TextView schoolTv;
    private EditText studyFieldEt;
    private TextView degreesTv;
    private EditText descriptionEt;
    private TextView startTimeTv;
    private TextView endTimeTv;
    private Button deleteBtn;
    private RelativeLayout degreesRl;
    private RelativeLayout schoolRl;
    private EduExperienceInfo eduInfo;
    private String school;
    private String studyField;
    private String degrees;
    private String description;
    private String startYear;
    private String startMonth;
    private String endYear;
    private String endMonth;
    private int id;//教育经历id
    private int schoolId;//学校id
    private boolean isAdd;
    private boolean isModify = false;
    private final static int SCHOOL_REQUEST_CODE = 201;
    private final static int SAVE = 101;
    private final static int START_TIME = 10;
    private final static int END_TIME = 11;
    private int currentYear;
    private int currentMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.edit_edu_item);
    }

    @Override
    protected void findView() {
        super.findView();
        schoolTv = (TextView) findViewById(R.id.school_tv);
        studyFieldEt = (EditText) findViewById(R.id.study_field_et);
        degreesTv = (TextView) findViewById(R.id.degrees_tv);
        descriptionEt = (EditText) findViewById(R.id.description_et);
        startTimeTv = (TextView) findViewById(R.id.start_time_tv);
        endTimeTv = (TextView) findViewById(R.id.end_time_tv);
        deleteBtn = (Button) findViewById(R.id.delete_bt);
        degreesRl = (RelativeLayout) findViewById(R.id.degrees_rl);
        schoolRl = (RelativeLayout) findViewById(R.id.school_rl);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "教育经历");
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        isAdd = getIntent().getBooleanExtra("isAdd", true);
        if (isAdd) {
            deleteBtn.setVisibility(View.GONE);
        }
        eduInfo = (EduExperienceInfo) getIntent().getSerializableExtra("eduInfo");
        if (eduInfo != null) {
            populateData();
        }

        schoolTv.addTextChangedListener(new EditTextListener());
        studyFieldEt.addTextChangedListener(new EditTextListener());
        degreesTv.addTextChangedListener(new EditTextListener());
        descriptionEt.addTextChangedListener(new EditTextListener());
        startTimeTv.addTextChangedListener(new EditTextListener());
        endTimeTv.addTextChangedListener(new EditTextListener());
    }

    private void populateData() {
        school = eduInfo.getSchoolName();
        studyField = eduInfo.getStudyField();
        degrees = eduInfo.getDegree();
        description = eduInfo.getContent();
        startYear = eduInfo.getStartYear();
        startMonth = eduInfo.getStartMonth();
        endYear = eduInfo.getEndYear();
        endMonth = eduInfo.getEndMonth();
        id = eduInfo.getId();
        if (!TextUtils.isEmpty(school)) {
            schoolTv.setText(school);
        }
        if (!TextUtils.isEmpty(studyField)) {
            studyFieldEt.setText(studyField);
            studyFieldEt.setSelection(studyField.length());
        }
        if (!TextUtils.isEmpty(degrees)) {
            degreesTv.setText(degrees);
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
            if (endYearInt > currentYear) {
                endTimeTv.setText("至今");
            } else if (endYearInt == currentYear && endMonthInt >= (currentMonth + 1)) {
                endTimeTv.setText("至今");
            } else {
                endTimeTv.setText(endYear + "." + endMonth);
            }
        }


    }

    @Override
    protected void initListener() {
        super.initListener();
        schoolRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditEdusItem.this, EditEduInfoSelectSchool.class);
                intent.putExtra("school", school);
                startActivityForResult(intent, SCHOOL_REQUEST_CODE);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
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
        degreesRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PopupWindows(EditEdusItem.this, degreesRl);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(EditEdusItem.this);
                materialDialog.getBuilder(R.string.material_dialog_title, "确定删除该教育经历？")
                        .callback(new MaterialDialog.ButtonCallback() {

                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                new DeleteEduInfoTask(EditEdusItem.this) {
                                    public void doPre() {
                                        showDialog(SAVE);
                                    }

                                    public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
                                        if (result == null) {
                                            removeDialog(SAVE);
                                            ToastUtil.showToast(EditEdusItem.this, R.string.network_anomaly);
                                        } else if (result.getState() == -3) {
                                            removeDialog(SAVE);
                                            if (!TextUtils.isEmpty(result.getErrorInfo())) {
                                                ToastUtil.showToast(EditEdusItem.this, result.getErrorInfo());
                                            } else {
                                                ToastUtil.showToast(EditEdusItem.this, R.string.edit_edu_error);
                                            }

                                        } else if (result.getState() == 1) {
                                            Intent brocastIntent = new Intent(MyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                                            sendBroadcast(brocastIntent);
                                            new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                                    getRenheApplication().getUserInfo().getSid(),
                                                    getRenheApplication().getUserInfo().getSid(),
                                                    getRenheApplication().getUserInfo().getAdSId());
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
            dpd.setTitle("请选择入学时间");
        } else if (tag == END_TIME) {
            dpd = new NoDayDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    if (year >= EditEdusItem.this.currentYear && monthOfYear >= EditEdusItem.this.currentMonth) {
                        endYear = year + "";
                        endMonth = (monthOfYear + 1) + "";
                        endTimeTv.setText("至今");
                    } else {
                        endYear = year + "";
                        endMonth = (monthOfYear + 1) + "";
                        endTimeTv.setText(endYear + "." + endMonth);
                    }
                }
            }, currentYear, currentMonth, currentDay);
            dpd.setTitle("请选择毕业时间");
        }
        if (dpd != null) {
            dpd.hideDay();
        }
        return dpd;
    }

    @Override
    public void goSave() {
        super.goSave();
        if (!checkPreSave()) {
            return;
        }
        if (isAdd) {
            new AddEduInfoTask(EditEdusItem.this) {
                public void doPre() {
                    showDialog(SAVE);
                }

                public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
                    if (result == null) {
                        removeDialog(SAVE);
                        ToastUtil.showToast(EditEdusItem.this, R.string.network_anomaly);
                    } else if (result.getState() == -3) {
                        removeDialog(SAVE);
                        if (!TextUtils.isEmpty(result.getErrorInfo())) {
                            ToastUtil.showToast(EditEdusItem.this, result.getErrorInfo());
                        } else {
                            ToastUtil.showToast(EditEdusItem.this, R.string.edit_edu_error);
                        }

                    } else if (result.getState() == 1) {
                        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
                                getRenheApplication().getUserInfo().getAdSId());
                    }
                }
            }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
                    getRenheApplication().getUserInfo().getAdSId(), schoolId + "", studyField, degrees, startYear + "",
                    startMonth + "", endYear + "", endMonth + "", description, school);
        } else {
            new EditEduInfoTask(EditEdusItem.this) {
                public void doPre() {
                    showDialog(SAVE);
                }

                public void doPost(com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo result) {
                    if (result == null) {
                        removeDialog(SAVE);
                        ToastUtil.showToast(EditEdusItem.this, R.string.network_anomaly);
                    } else if (result.getState() == -3) {
                        removeDialog(SAVE);
                        if (!TextUtils.isEmpty(result.getErrorInfo())) {
                            ToastUtil.showToast(EditEdusItem.this, result.getErrorInfo());
                        } else {
                            ToastUtil.showToast(EditEdusItem.this, R.string.edit_edu_error);
                        }

                    } else if (result.getState() == 1) {
                        new ProfileTask().executeOnExecutor(Executors.newCachedThreadPool(),
                                getRenheApplication().getUserInfo().getSid(), getRenheApplication().getUserInfo().getSid(),
                                getRenheApplication().getUserInfo().getAdSId());
                    }
                }
            }.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
                    getRenheApplication().getUserInfo().getAdSId(), id + "", schoolId + "", studyField, startYear + "",
                    startMonth + "", endYear + "", endMonth + "", degrees, description, school);
        }
    }

    private boolean checkPreSave() {
        school = schoolTv.getText().toString().trim();
        if (TextUtils.isEmpty(school)) {
            ToastUtil.showToast(EditEdusItem.this, "学校不能为空");
            schoolTv.requestFocus();
            return false;
        }
        if (school.length() > Constants.WORK_COMPANY) {
            ToastUtil.showToast(EditEdusItem.this, "学校名称不能超过32字");
            schoolTv.requestFocus();
            return false;
        }
        studyField = studyFieldEt.getText().toString().trim();
        if (TextUtils.isEmpty(studyField)) {
            ToastUtil.showToast(EditEdusItem.this, "专业不能为空");
            studyFieldEt.requestFocus();
            return false;
        }
        if (studyField.length() > Constants.WORK_TITLE) {
            ToastUtil.showToast(EditEdusItem.this, "专业名称不能超过32字");
            studyFieldEt.requestFocus();
            return false;
        }
        degrees = degreesTv.getText().toString().trim();
        if (TextUtils.isEmpty(degrees)) {
            ToastUtil.showToast(EditEdusItem.this, "学历不能为空");
            return false;
        }
        description = descriptionEt.getText().toString().trim();
        if (description.length() > Constants.WORK_DESCRIPTION) {
            ToastUtil.showToast(EditEdusItem.this, "描述不能超过320字");
            descriptionEt.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(startYear) || TextUtils.isEmpty(startMonth)) {
            ToastUtil.showToast(EditEdusItem.this, "请填写入学时间");
            return false;
        }
        if (TextUtils.isEmpty(endYear) || TextUtils.isEmpty(endMonth)) {
            ToastUtil.showToast(EditEdusItem.this, "请填写毕业时间");
            return false;
        }
        int startYearInt = Integer.parseInt(startYear);
        int startMonthInt = Integer.parseInt(startMonth);
        int endYearInt = Integer.parseInt(endYear);
        int endMonthInt = Integer.parseInt(endMonth);
        if (startYearInt > currentYear) {
            ToastUtil.showToast(EditEdusItem.this, "入学时间不能大于当前时间");
            return false;
        }
        if (startYearInt > endYearInt) {
            ToastUtil.showToast(EditEdusItem.this, "入学时间不能大于毕业时间");
            return false;
        }
        if (startYearInt == endYearInt && startMonthInt > endMonthInt) {
            ToastUtil.showToast(EditEdusItem.this, "入学时间不能毕业离职时间");
            return false;
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
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SAVE:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.saving).cancelable(false).build();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCHOOL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String yourSchool = data.getStringExtra("name");
                int yourSchoolId = data.getIntExtra("id", EditEduInfoSelectSchool.NO_THIS_SCHOOL);
                if (!TextUtils.isEmpty(yourSchool)) {
                    school = yourSchool;
                    schoolTv.setText(school);
                }
                schoolId = yourSchoolId;
            }
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
                return getRenheApplication().getProfileCommand().showProfile(params[0], params[1], params[2], EditEdusItem.this);
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
                    Intent brocastIntent2 = new Intent(EditMyHomeArchivesActivity.REFRESH_ARCHIEVE_RECEIVER_ACTION);
                    brocastIntent2.putExtra("Profile", result);
                    sendBroadcast(brocastIntent2);
                    Intent intent = new Intent();
                    intent.putExtra("Profile", result);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                }
            } else {
                ToastUtil.showNetworkError(EditEdusItem.this);
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

    public class PopupWindows extends PopupWindow {

        public PopupWindows(Context mContext, View parent) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(degreesTv.getWindowToken(), 0);
            final View view = View.inflate(mContext, R.layout.degrees_item, null);
            final LinearLayout ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
            view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_ins));
            ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in_2));

            setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            setBackgroundDrawable(new BitmapDrawable());
            setFocusable(true);
            setOutsideTouchable(true);
            setContentView(view);
            showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            update();
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = view.findViewById(R.id.ll_popup).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });

            final Button juniorCollegeBtn = (Button) view.findViewById(R.id.junior_college_bt);
            final Button bachelorBtn = (Button) view.findViewById(R.id.bachelor_btn);
            final Button masterBtn = (Button) view.findViewById(R.id.master_btn);
            final Button doctorBtn = (Button) view.findViewById(R.id.doctor_btn);
            final Button postDoctoralBtn = (Button) view.findViewById(R.id.post_doctoral_btn);
            final Button othersBtn = (Button) view.findViewById(R.id.others_btn);
            juniorCollegeBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    degrees = juniorCollegeBtn.getText().toString();
                    degreesTv.setText(degrees);
                    dismiss();
                }
            });
            bachelorBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    degrees = bachelorBtn.getText().toString();
                    degreesTv.setText(degrees);
                    dismiss();
                }
            });
            masterBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    degrees = masterBtn.getText().toString();
                    degreesTv.setText(degrees);
                    dismiss();
                }
            });
            doctorBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    degrees = doctorBtn.getText().toString();
                    degreesTv.setText(degrees);
                    dismiss();
                }
            });
            postDoctoralBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    degrees = postDoctoralBtn.getText().toString();
                    degreesTv.setText(degrees);
                    dismiss();
                }
            });
            othersBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    degrees = othersBtn.getText().toString();
                    degreesTv.setText(degrees);
                    dismiss();
                }
            });
        }
    }
}
