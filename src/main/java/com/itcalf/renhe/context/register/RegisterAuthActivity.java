package com.itcalf.renhe.context.register;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.portal.ClauseActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.WriteLogThread;

import java.util.concurrent.Executors;

/**
 * description :注册带验证 step1
 * Created by Chans Renhenet
 * 2015/8/21
 */
public class RegisterAuthActivity extends BaseActivity {

    private RelativeLayout rootLl;
    private EditText mobileEdt;
    private EditText authEdt;
    private Button retryBtn;
    private EditText passwordEdt;
    private ImageView pwdIsVisibleIv;
    private Button nextBtn;
    private TextView clauseTv;

    private RequestDialog requestDialog;
    private MaterialDialogsUtil materialDialogsUtil;
    private TimeCount time;//计时器
    private boolean isVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.register_auth_activity);
    }

    @Override
    protected void findView() {
        super.findView();
        mobileEdt = (EditText) findViewById(R.id.register_mobile_edt);
        authEdt = (EditText) findViewById(R.id.register_auth_edt);
        retryBtn = (Button) findViewById(R.id.resend_code_btn);
        passwordEdt = (EditText) findViewById(R.id.password_edt);
        pwdIsVisibleIv = (ImageView) findViewById(R.id.pwdisvisible_iv);

        nextBtn = (Button) findViewById(R.id.register_next_btn);
        rootLl = (RelativeLayout) findViewById(R.id.register_rootLl);
        clauseTv = (TextView) findViewById(R.id.clauseTv);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(0, "注册");
        retryBtn.setText("发送验证码");
        requestDialog = new RequestDialog(this, "正在发送验证码");
        materialDialogsUtil = new MaterialDialogsUtil(RegisterAuthActivity.this);

        time = new TimeCount(Constants.COUNTDOWN_TIME, 1000);//构造CountDownTimer对象

        SmsContent content = new SmsContent(RegisterAuthActivity.this, new Handler(), authEdt);
        // 注册短信变化监听
        this.getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, content);
    }

    @Override
    protected void initListener() {
        super.initListener();
        //发送验证码
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telNum = mobileEdt.getText().toString().trim();
                if (telNum.length() == 0) {
                    ToastUtil.showToast(RegisterAuthActivity.this, R.string.phonenotnull);
                    mobileEdt.requestFocus();
                    return;
                } else if (telNum.length() > 0 && !telNum.matches(Constants.TELPHONE_REGULAR_EXPRESSION)
                        && !telNum.startsWith("0000")) {
                    ToastUtil.showToast(RegisterAuthActivity.this, R.string.phonerule);
                    mobileEdt.requestFocus();
                    return;
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String contentString = getString(R.string.new_register_dialog_content) + "\n" + telNum;
                materialDialogsUtil.getBuilder(R.string.new_register_dialog_title, contentString)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                new NewRegisterTask(RegisterAuthActivity.this) {
                                    public void doPre() {
                                        requestDialog.addFade(rootLl);
                                    }

                                    public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
                                        requestDialog.removeFade(rootLl);
                                        if (null == result) {
                                            ToastUtil.showConnectError(RegisterAuthActivity.this);
                                        } else {
                                            switch (result.getState()) {
                                                case 1:
                                                    time.start();//开始计时
                                                    break;
                                                case 2:
                                                    time.start();//开始计时
                                                    break;
                                                case -1:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "手机号码不能为空");
                                                    break;
                                                case -2:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "密码不能为空");
                                                    break;
                                                case -3:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "数据异常，请退出重试");
                                                    break;
                                                case -4:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "手机号码格式有误，目前进支持大陆地区用户注册");
                                                    break;
                                                case -5:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "手机号码已被注册，请尝试换一个手机号码注册");
                                                    break;
                                                case -6:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "短信验证码发送过于频繁，请1分钟后重试");
                                                    break;
                                                case -7:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "短信验证码发送过于频繁，请1小时后重试");
                                                    break;
                                                case -8:
                                                    ToastUtil.showToast(RegisterAuthActivity.this, "短信验证码发送过于频繁，请1天后重试");
                                                    break;
                                            }
                                        }
                                    }
                                }.executeOnExecutor(Executors.newCachedThreadPool(), mobileEdt.getText().toString().trim(),
                                        DeviceUitl.getDeviceInfo());
                            }
                        });
                materialDialogsUtil.show();
            }
        });

        //密码是否可见
        pwdIsVisibleIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVisible) {
                    isVisible = false;
                    pwdIsVisibleIv.setImageResource(R.drawable.icon_follow_clicked);
                    passwordEdt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    isVisible = true;
                    pwdIsVisibleIv.setImageResource(R.drawable.icon_follow);
                    passwordEdt.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    //					passwordEdt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                passwordEdt.setTypeface(Constants.APP_TYPEFACE);
                passwordEdt.requestFocus();
                passwordEdt.setSelection(passwordEdt.length());
            }
        });

        //下一步
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceUitl.hideSoftInput(nextBtn);
                String telNum = mobileEdt.getText().toString().trim();
                if (telNum.length() == 0) {
                    ToastUtil.showToast(RegisterAuthActivity.this, R.string.phonenotnull);
                    mobileEdt.requestFocus();
                    return;
                } else if (telNum.length() > 0 && !telNum.matches(Constants.TELPHONE_REGULAR_EXPRESSION)
                        && !telNum.startsWith("0000")) {
                    ToastUtil.showToast(RegisterAuthActivity.this, R.string.phonerule);
                    mobileEdt.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(authEdt.getText().toString().trim())) {
                    ToastUtil.showToast(RegisterAuthActivity.this, "请输入验证码");
                    return;
                }
                //验证密码
                String pwd = passwordEdt.getText().toString().trim();
                if (pwd.length() == 0) {
                    ToastUtil.showToast(RegisterAuthActivity.this, getResources().getString(R.string.passwordnotnull));
                    passwordEdt.requestFocus();
                    return;
                } else if (pwd.length() <= 5 || pwd.length() >= 17 || !pwd.matches("^[_a-zA-Z0-9]+$")) {
                    ToastUtil.showToast(RegisterAuthActivity.this, getResources().getString(R.string.passwordrule));
                    passwordEdt.requestFocus();
                    return;
                }
                checkAuthCode(authEdt.getText().toString());
            }
        });

        //服务条款
        clauseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(RegisterAuthActivity.this, ClauseActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    /* 定义一个倒计时的内部类 */
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            retryBtn.setText("重新发送");
            retryBtn.setEnabled(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
            retryBtn.setEnabled(false);
            retryBtn.setText(millisUntilFinished / 1000 + "秒重新发送");
        }
    }

    /**
     * @param code 验证码验证
     */
    private void checkAuthCode(String code) {
        if (null != code && !"".equals(code)) {
            new CheckCodeTask(RegisterAuthActivity.this) {
                public void doPre() {
                    requestDialog = new RequestDialog(RegisterAuthActivity.this, "请求正在处理...");
                    requestDialog.addFade(rootLl);
                }

                public void doPost(com.itcalf.renhe.dto.MessageBoardOperation result) {
                    requestDialog.removeFade(rootLl);
                    if (null == result) {
                        ToastUtil.showErrorToast(RegisterAuthActivity.this, getString(R.string.connect_server_error));
                    } else {
                        switch (result.getState()) {
                            case 1:
//                                ToastUtil.showToast(RegisterAuthActivity.this, "短信验证码验证成功");
                                Intent i = new Intent(RegisterAuthActivity.this, RegisterPerfectInfoActivity.class);
                                i.putExtra("mobile", mobileEdt.getText().toString().trim());
                                i.putExtra("isSimplify", true);
                                startActivity(i);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                new WriteLogThread(RegisterAuthActivity.this, "5.400", new String[]{"0"}).start();
                                break;
                            case 2:
                                ToastUtil.showToast(RegisterAuthActivity.this, "短信验证码验证成功");
                                Intent i2 = new Intent(RegisterAuthActivity.this, RegisterPerfectInfoActivity.class);
                                i2.putExtra("mobile", mobileEdt.getText().toString().trim());
                                i2.putExtra("isSimplify", false);
                                startActivity(i2);
                                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                                new WriteLogThread(RegisterAuthActivity.this, "5.400", new String[]{"1"}).start();
                                break;
                            case -1:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "手机号码不能为空");
                                time.cancel();
                                time.onFinish();
                                break;
                            case -2:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "短信验证码不能为空");
                                time.cancel();
                                time.onFinish();
                                break;
                            case -3:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "此手机号码未接收过验证码");
                                time.cancel();
                                time.onFinish();
                                break;
                            case -4:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "验证码已过期，请重新发送验证码");
                                time.cancel();
                                time.onFinish();
                                break;
                            case -5:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "验证码错误，请重新输入");
                                time.cancel();
                                time.onFinish();
                                break;
                            case -7:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "密码不能为空");
                                time.cancel();
                                time.onFinish();
                                break;
                            case -8:
                                ToastUtil.showErrorToast(RegisterAuthActivity.this, "密码为6-16个数字字母组合");
                                time.cancel();
                                time.onFinish();
                                break;
                            default:
                                break;
                        }
                    }
                }
            }.executeOnExecutor(Executors.newCachedThreadPool(), mobileEdt.getText().toString().trim(), code,
                    passwordEdt.getText().toString().trim());
        } else {
            ToastUtil.showErrorToast(RegisterAuthActivity.this, "验证码不能为空");
        }
    }

}
