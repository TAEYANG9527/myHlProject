package com.itcalf.renhe.context.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.portal.ClauseActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.concurrent.Executors;

/**
 * Title: NewRegisterActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-11 下午1:14:36 <br>
 * @author wangning
 */
public class BindPhoneActivity extends BaseActivity {
	private EditText userTelET;
	private Button registerBt;
	private TextView noticeTv;
	private RelativeLayout clauseRl;
	private TextView clauseTv;
	private String telNum;
	private MaterialDialogsUtil materialDialogsUtil;
	private String contentString = "";
	private EditText mPwdEt;
	private RelativeLayout rootRl;
	private RequestDialog requestDialog;
	private RelativeLayout pwdRl;
	private int flag = 0;//页面标记，0是绑定手机号页面，1是修改手机号页面

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.user_register);
	}

	@Override
	protected void findView() {
		super.findView();
		userTelET = (EditText) findViewById(R.id.register_user_telnum);
		registerBt = (Button) findViewById(R.id.registerBt);
		noticeTv = (TextView) findViewById(R.id.notice_tv);
		clauseRl = (RelativeLayout) findViewById(R.id.clause_rl);
		clauseTv = (TextView) findViewById(R.id.clauseTv);
		mPwdEt = (EditText) findViewById(R.id.pwdEt);
		mPwdEt.setVisibility(View.GONE);
		rootRl = (RelativeLayout) findViewById(R.id.registerRl);
		requestDialog = new RequestDialog(this, "验证中");
		pwdRl = (RelativeLayout) findViewById(R.id.pwRl);
		pwdRl.setVisibility(View.GONE);
		noticeTv.setVisibility(View.VISIBLE);
		clauseRl.setVisibility(View.GONE);
	}

	@Override
	protected void initData() {
		super.initData();
		boolean isBindMobile = getIntent().getBooleanExtra("isBindMobile", false);
		if (isBindMobile) {
			setTextValue(R.id.title_txt, "修改手机号码");
			flag = 1;
			noticeTv.setText(getResources().getString(R.string.change_notice));
		} else {
			setTextValue(R.id.title_txt, "绑定手机号码");
			flag = 0;
			noticeTv.setText(getResources().getString(R.string.bind_notice));
		}
		telNum = getIntent().getStringExtra("phoneNumber");
		if (!TextUtils.isEmpty(telNum)) {
			userTelET.setText(telNum);
			userTelET.requestFocus();
			registerBt.setClickable(true);
			registerBt.setEnabled(true);
		} else {
			registerBt.setClickable(false);
			registerBt.setEnabled(false);
		}

		materialDialogsUtil = new MaterialDialogsUtil(this);
	}

	@Override
	protected void initListener() {
		super.initListener();
		registerBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				telNum = userTelET.getText().toString().trim();
				if (telNum.length() == 0) {
					ToastUtil.showToast(BindPhoneActivity.this, getResources().getString(R.string.phonenotnull));
					userTelET.requestFocus();
					return;
				}
				if (telNum.length() > 0 && !telNum.matches(Constants.TELPHONE_REGULAR_EXPRESSION) && !telNum.startsWith("0000")) {
					ToastUtil.showToast(BindPhoneActivity.this, getResources().getString(R.string.phonerule));
					userTelET.requestFocus();
					return;
				}
				contentString = getString(R.string.new_register_dialog_content) + "\n" + telNum;
				materialDialogsUtil.getBuilder(R.string.new_register_dialog_title, contentString)
						.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						super.onPositive(dialog);
						new BindPhoneTask(BindPhoneActivity.this) {
							public void doPre() {
								requestDialog.addFade(rootRl);
							}

							public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
								requestDialog.removeFade(rootRl);
								if (null == result) {
									ToastUtil.showConnectError(BindPhoneActivity.this);
								} else
									switch (result.getState()) {
									case 1:
										Intent intent = new Intent(BindPhoneActivity.this, BindPhoneAuthActivity.class);
										intent.putExtra("phoneNumber", telNum);
										intent.putExtra("deviceinfo", getDeviceInfo());
										intent.putExtra("flag", flag);
										startActivityForResult(intent, 1);
										overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
										break;
									case -1:
										ToastUtil.showToast(BindPhoneActivity.this,
												getString(R.string.sorry_of_unknow_exception));
										break;
									case -2:
										ToastUtil.showToast(BindPhoneActivity.this,
												getString(R.string.sorry_of_unknow_exception));
										break;
									case -3:
										ToastUtil.showToast(BindPhoneActivity.this, "手机号码不能为空");
										break;
									case -4:

										break;
									case -5:
										ToastUtil.showToast(BindPhoneActivity.this, "手机号码有误，目前仅支持中国大陆地区的手机号码");
										break;
									case -6:
										ToastUtil.showToast(BindPhoneActivity.this,
												getString(R.string.bindphone_error_hasbinded));
										break;
									case -7:
										ToastUtil.showToast(BindPhoneActivity.this, "短信验证码发送过于频繁，请1分钟后重试");
										break;
									case -8:
										ToastUtil.showToast(BindPhoneActivity.this, "短信验证码发送过于频繁，请1小时后重试");
										break;
									case -9:
										ToastUtil.showToast(BindPhoneActivity.this, "短信验证码发送过于频繁，请1天后重试");
										break;
									}
							}
						}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId(), telNum, getDeviceInfo());

					}
				});
				materialDialogsUtil.show();
			}
		});

		clauseTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(BindPhoneActivity.this, ClauseActivity.class));
			}
		});
		userTelET.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (userTelET.getText().toString().trim().length() > 0) {
					registerBt.setEnabled(true);
					registerBt.setClickable(true);
				} else {
					registerBt.setEnabled(false);
					registerBt.setClickable(false);
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
		userTelET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_DONE) {
					registerBt.performClick();
				}
				return true;
			}
		});
	}

	/**
	 * 设备的唯一id，用于做短信验证码的发送数量控制
	 * @return
	 */
	private String getDeviceInfo() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == 1) {
			setResult(RESULT_OK, data);
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		}
	}
}