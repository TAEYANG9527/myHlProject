package com.itcalf.renhe.context.register;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

/**
 * Title: BindPhoneAuthActivity.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-9 下午1:14:36 <br>
 * @author wangning
 */
public class BindPhoneAuthActivity extends BaseActivity {
	private RelativeLayout rootRl;
	private EditText captchasEt;
	private Button resendIdcodeBtn;
	private Button submitBtn;
	private TimeCount time;//计时器
	private String phoneNumber, deviceinfo;
	private final static int RETRYTIME = 60000;
	private RequestDialog requestDialog;
	private MaterialDialogsUtil materialDialogsUtil;
	private LinearLayout resultLl;
	private TextView successTv;
	private int flag;//跳转页面标记，0是从绑定手机号页面跳转过来，1是从修改手机号页面跳转过来
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new ActivityTemplate().doInActivity(this, R.layout.captchas);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("绑定手机——填写验证码"); //统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("绑定手机——填写验证码"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
		MobclickAgent.onPause(this);
	}

	@Override
	protected void findView() {
		super.findView();
		setTextValue(R.id.title_txt, "填写验证码");
		rootRl = (RelativeLayout) findViewById(R.id.rootRl);
		captchasEt = (EditText) findViewById(R.id.captchas_et);
		resendIdcodeBtn = (Button) findViewById(R.id.resendIdcode_btn);
		submitBtn = (Button) findViewById(R.id.submit_btn);
		resultLl = (LinearLayout) findViewById(R.id.result_ll);
		resultLl.setVisibility(View.GONE);
		successTv = (TextView) findViewById(R.id.success_tv);
	}

	@Override
	protected void initData() {
		super.initData();
		time = new TimeCount(RETRYTIME, 1000);//构造CountDownTimer对象
		time.start();//开始计时
		requestDialog = new RequestDialog(this, "验证中");
		phoneNumber = getIntent().getStringExtra("phoneNumber");
		deviceinfo = getIntent().getStringExtra("deviceinfo");
		flag = getIntent().getIntExtra("flag", 0);
		mHandler = new Handler();
		materialDialogsUtil = new MaterialDialogsUtil(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			materialDialogsUtil.getBuilder(R.string.auth_click_back).callback(new MaterialDialog.ButtonCallback() {
				@Override
				public void onPositive(MaterialDialog dialog) {
					super.onPositive(dialog);
					finish();
					overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
				}
			});
			materialDialogsUtil.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void initListener() {
		super.initListener();
		resendIdcodeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				materialDialogsUtil.getBuilder(R.string.auth_code_receive_one_more_time)
						.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						super.onPositive(dialog);
						new BindPhoneTask(BindPhoneAuthActivity.this) {
							public void doPre() {
								requestDialog.addFade(rootRl);
							}

							public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
								requestDialog.removeFade(rootRl);
								if (null == result) {
									ToastUtil.showConnectError(BindPhoneAuthActivity.this);
									return;
								} else
									switch (result.getState()) {
									case 1:
										break;
									case -1:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "发生未知错误，请返回前一步重新输入");
										return;
									case -2:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "发生未知错误，请退出重试");
										return;
									case -3:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "手机号码不能为空，请返回前一步重新输入");
										return;
									case -4:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "数据异常，请退出重试");
										return;
									case -5:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "手机号码格式有误，目前进支持大陆地区用户注册");
										return;
									case -6:
										ToastUtil.showToast(BindPhoneAuthActivity.this,
												getString(R.string.bindphone_error_hasbinded));
										return;
									case -7:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "短信验证码发送过于频繁，请1分钟后重试");
										return;
									case -8:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "短信验证码发送过于频繁，请1小时后重试");
										return;
									case -9:
										ToastUtil.showToast(BindPhoneAuthActivity.this, "短信验证码发送过于频繁，请1天后重试");
										return;
									}
								time.start();//开始计时
							}
						}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
								getRenheApplication().getUserInfo().getAdSId(), phoneNumber, deviceinfo);
					}
				});
				materialDialogsUtil.show();
			}
		});
		submitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!TextUtils.isEmpty(captchasEt.getText().toString().trim())) {
					submitBtn.setClickable(false);
					checkAuthCode(captchasEt.getText().toString().trim());
				}

				if (flag == 0) {
					//和聊统计
					String content = "5.161" + LoggerFileUtil.getConstantInfo(BindPhoneAuthActivity.this) + "|" + phoneNumber;
					LoggerFileUtil.writeFile(content, true);
				} else {
					//和聊统计
					String content = "5.162" + LoggerFileUtil.getConstantInfo(BindPhoneAuthActivity.this) + "|" + phoneNumber;
					LoggerFileUtil.writeFile(content, true);
				}
			}
		});
		captchasEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!TextUtils.isEmpty(captchasEt.getText().toString().trim())) {
					activateButton(submitBtn);
				} else {
					nagativeButton(submitBtn);
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
		captchasEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				if (arg1 == EditorInfo.IME_ACTION_DONE) {
					submitBtn.performClick();
				}
				return true;
			}
		});

	}

	private void activateButton(Button button) {
		button.setClickable(true);
		button.setEnabled(true);
	}

	private void nagativeButton(Button button) {
		button.setClickable(false);
		button.setEnabled(false);
	}

	private void checkAuthCode(String code) {
		if (null != code && !"".equals(code)) {
			//如果验证成功
			new BindCheckCodeTask(BindPhoneAuthActivity.this) {
				public void doPre() {
					requestDialog.addFade(rootRl);
				}

				public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
					requestDialog.removeFade(rootRl);
					if (null == result) {
						ToastUtil.showConnectError(BindPhoneAuthActivity.this);
					} else
						switch (result.getState()) {
						case 1:
							resultLl.setVisibility(View.VISIBLE);
							if (flag == 0) {
								successTv.setText(getResources().getString(R.string.bind_success));
							} else {
								successTv.setText(getResources().getString(R.string.change_success));
							}
							mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									goBack();
								}
							}, 2000);
							break;
						case -1:
							ToastUtil.showToast(BindPhoneAuthActivity.this, "发生未知错误，请返回前一步重新输入");
							break;
						case -2:
							ToastUtil.showToast(BindPhoneAuthActivity.this, getString(R.string.sorry_of_unknow_exception));
							break;
						case -3:
							ToastUtil.showToast(BindPhoneAuthActivity.this, "手机号码不能为空，请返回前一步重新输入");
							break;
						case -4:
							ToastUtil.showToast(BindPhoneAuthActivity.this, "验证码不能为空");
							break;
						case -5:
							ToastUtil.showToast(BindPhoneAuthActivity.this, " 数据异常，请返回前一步重新输入");
							break;
						case -6:
							ToastUtil.showToast(BindPhoneAuthActivity.this, "短信验证码已过期，请重新申请发送绑定手机号码请求");
							break;
						case -7:
							ToastUtil.showToast(BindPhoneAuthActivity.this, "短信验证码有误，请重新申请验证");
							break;
						case -8:
							ToastUtil.showToast(BindPhoneAuthActivity.this, "会员已绑定手机号码，不能再绑定");
							break;
						}
					activateButton(submitBtn);
				}
			}.executeOnExecutor(Executors.newCachedThreadPool(), getRenheApplication().getUserInfo().getSid(),
					getRenheApplication().getUserInfo().getAdSId(), phoneNumber, code, flag + "");
		} else {
			ToastUtil.showToast(BindPhoneAuthActivity.this, "验证码不能为空");
		}
	}

	private void goBack() {
		Intent data = new Intent();
		data.putExtra("phoneNumber", phoneNumber);
		setResult(RESULT_OK, data);
		finish();
		overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
	}

	@Override
	public void onBackPressed() {
		materialDialogsUtil.getBuilder(R.string.auth_click_back).callback(new MaterialDialog.ButtonCallback() {
			@Override
			public void onPositive(MaterialDialog dialog) {
				super.onPositive(dialog);
				finish();
				overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			}
		});
		materialDialogsUtil.show();
	}

	/* 定义一个倒计时的内部类 */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {//计时完毕时触发
			resendIdcodeBtn.setText("重新发送");
			resendIdcodeBtn.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {//计时过程显示
			resendIdcodeBtn.setClickable(false);
			resendIdcodeBtn.setText(millisUntilFinished / 1000 + "秒重新发送");
		}
	}
}
