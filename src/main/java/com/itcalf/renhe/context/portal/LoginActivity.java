package com.itcalf.renhe.context.portal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.register.RegisterAuthActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.Date;
import java.util.concurrent.Executors;

public class LoginActivity extends BaseActivity {

    private TextView returnTv;
    private Button mRegisterBt;
    private EditText mEmailEt;
    private EditText mPwdEt;
    private Button mLoginBt;
    private Drawable imgCloseButton;
    //    private RequestDialog requestDialog;
    private RelativeLayout loginRl;
    public static final String FINISHLOGINACTIVITY_1 = "com.renhe.finishloginactivity1";
    public static final String FINISHLOGINACTIVITY_2 = "com.renhe.finishloginactivity2";
    private FinishSelfReceiver finishSelfReceiver;
    private TextView findPwdTv;
    private String comeFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.portal_login);
        if (null != getSupportActionBar()) {
            getSupportActionBar().hide();
        }
        RenheApplication.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("登陆页面"); //统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("登陆页面"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void findView() {
        super.findView();
        returnTv = (TextView) findViewById(R.id.returnTv);
        mRegisterBt = (Button) findViewById(R.id.registerbt);
        mEmailEt = (EditText) findViewById(R.id.mailEt);
        mPwdEt = (EditText) findViewById(R.id.pwdEt);
        mLoginBt = (Button) findViewById(R.id.loginBt);
        mEmailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        loginRl = (RelativeLayout) findViewById(R.id.login_rl);
        findPwdTv = (TextView) findViewById(R.id.text1);
        mLoginBt.setEnabled(false);
    }

    @Override
    protected void initData() {
        super.initData();
        materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.showIndeterminateProgressDialog("登录中...").cancelable(false).build();
        comeFlag = getIntent().getStringExtra("comeflag");
        // 从登入界面2过来的，需要加标题
        if ("2login".equals(comeFlag)) {
            setTextValue(R.id.title_txt, "登录人和网");
            returnTv.setVisibility(View.VISIBLE);
            mRegisterBt.setVisibility(View.GONE);
        } else {
            //			getSupportActionBar().hide();
            returnTv.setVisibility(View.GONE);
            mRegisterBt.setVisibility(View.VISIBLE);
        }
        if (imgCloseButton != null) {
            imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
        }
//        requestDialog = new RequestDialog(this, "登录中");
        finishSelfReceiver = new FinishSelfReceiver();
        IntentFilter intentFilter = new IntentFilter(FINISHLOGINACTIVITY_1);
        intentFilter.addAction(FINISHLOGINACTIVITY_2);
        registerReceiver(finishSelfReceiver, intentFilter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        returnTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });
        mEmailEt.addTextChangedListener(new TextWatchListener(mEmailEt));
        mPwdEt.addTextChangedListener(new TextWatchListener(mPwdEt));
        mRegisterBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(LoginActivity.this, "register_bt");
                startActivity(RegisterAuthActivity.class);
            }
        });
        mLoginBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(LoginActivity.this, "login_bt");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String email = mEmailEt.getText().toString().trim();
                String pwd = mPwdEt.getText().toString().trim();
                if (email.length() == 0) {
                    ToastUtil.showToast(LoginActivity.this, getResources().getString(R.string.mailnotnull));
                    return;
                }
                if (!email.matches("^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$")
                        && !email.matches(Constants.TELPHONE_REGULAR_EXPRESSION)) {
                    if (Constants.DEBUG_MODE && email.startsWith("0000")) {

                    } else {
                        ToastUtil.showToast(LoginActivity.this, getResources().getString(R.string.mailrule));
                        return;
                    }
                }

                if (pwd.length() == 0) {
                    ToastUtil.showToast(LoginActivity.this, getResources().getString(R.string.passwordnotnull));
                    return;
                }
                if (-1 != NetworkUtil.hasNetworkConnection(LoginActivity.this)) {
                    new LoginTask().executeOnExecutor(Executors.newCachedThreadPool(), email, pwd);
                } else {
                    ToastUtil.showNetworkError(LoginActivity.this);
                }
            }
        });
        mEmailEt.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    if (mEmailEt.getText().toString().trim().length() > 0) {
                        mEmailEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getResources().getDrawable(R.drawable.relationship_input_del), null);
                    } else {
                        mEmailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                } else {
                    if (!TextUtils.isEmpty(mEmailEt.getText().toString().trim())) {
                        mEmailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    } else {
                        mEmailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                }
            }
        });
        mPwdEt.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean arg1) {
                if (arg1) {
                    if (mPwdEt.getText().toString().trim().length() > 0) {
                        mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getResources().getDrawable(R.drawable.relationship_input_del), null);
                    } else {
                        mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                } else {
                    if (!TextUtils.isEmpty(mPwdEt.getText().toString().trim())) {
                        mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    } else {
                        mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                }
            }
        });
        findPwdTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(LoginActivity.this, "find_pwd");
                Intent intent = new Intent(LoginActivity.this, WebViewActivityForFindPwd.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    private static final int REGISTER_CODE = 10;
    private static final int COMPLETE_REGISTER_CODE = 5;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REGISTER_CODE:
                if (resultCode == RESULT_OK) {
                    if (null != data) {
                        UserInfo userInfo = (UserInfo) data.getSerializableExtra("userInfo");
                        if (null != userInfo) {
                            userInfo.setLoginAccountType(userInfo.getEmail());
                            forwardToHall(userInfo);
                        }
                    }
                }
                break;
            case COMPLETE_REGISTER_CODE:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    private void forwardToHall(UserInfo userInfo) {
        if (1 == NetworkUtil.hasNetworkConnection(this)) {
        } else if (0 == NetworkUtil.hasNetworkConnection(this)) {
        } else {
            ToastUtil.showNetworkError(this);
        }
        // 记录是登录状态
        SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("islogined", true);
        editor.commit();
        //记录该程序是否被登录使用过
        SharedPreferences prefs2 = getSharedPreferences("first_guide_setting_info", 0);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.putBoolean("ifFirst", false);
        editor2.commit();

        sendBroadcast(new Intent(LoginAct.FINISHLOGINACT_1));
        sendBroadcast(new Intent(LoginAct.FINISHLOGINACT_2));
        userInfo.setRemember(true);
        userInfo.setLogintime(DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString());
//        userInfo.setSid("13f6aed3a010ff30");
//        userInfo.setAdSId("41a4418c333c5d79f52af35373967f75");//人脉最多的人 栗艳琴
        getRenheApplication().getUserCommand().insertOrUpdate(userInfo);
        getRenheApplication().setUserInfo(userInfo);
        getRenheApplication().setLogin(1);
        //		startService(new Intent(this, RenheService.class));
        Intent i = new Intent(this, TabMainFragmentActivity.class);
        if (null != getIntent()) {
            i = getIntent();
            i.setClass(this, TabMainFragmentActivity.class);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    class LoginTask extends AsyncTask<String, Void, UserInfo> {
        private String userAccount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (null != materialDialogsUtil)
                materialDialogsUtil.show();
        }

        @Override
        protected UserInfo doInBackground(String... params) {
            UserInfo userInfo = new UserInfo();
            userInfo.setLoginAccountType(params[0]);
            userInfo.setPwd(params[1]);
            this.userAccount = params[0];
            return getRenheApplication().getUserCommand().login(userInfo);
        }

        @Override
        protected void onPostExecute(UserInfo result) {
            super.onPostExecute(result);
            if (null != result) {
                if (1 == result.getState()) {
                    RenheApplication.getInstance().setFromLoginIn(true);
                    // 设置密码（result中不返回密码）
                    result.setPwd(mPwdEt.getText().toString());
                    result.setLoginAccountType(userAccount);
                    forwardToHall(result);
                    // 设置JPush推送
                    PushUtil.deletePush();//为了防止用户卸载和聊后（信鸽是不知道用户已卸载，所以没有对该账户和设备接触绑定）再次安装，并且使用另外一个账户登录，导致推送消息错乱
                    PushUtil.registerPush(result);
                    //阿里云统计：用户登录埋点
                    MANService manService = MANServiceProvider.getService();
                    if (null != manService && null != manService.getMANAnalytics())
                        manService.getMANAnalytics().updateUserAccount(result.getName(), result.getId() + "");
                } else if (2 == result.getState()) {// 手机号码登录而且未走完注册流程的，需要跳到完善注册资料的页面（会继续返回mobile字段）
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                } else if (-1 == result.getState()) {
                    ToastUtil.showErrorToast(LoginActivity.this, getResources().getString(R.string.user_error));
                } else if (-2 == result.getState()) {
                    ToastUtil.showErrorToast(LoginActivity.this, getResources().getString(R.string.user_null));
                } else if (-3 == result.getState()) {
                    MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(LoginActivity.this);
                    materialDialogsUtil.getBuilder("登录失败", getString(R.string.user_spam_exception),
                            getString(R.string.material_dialog_sure));
                    materialDialogsUtil.show();
                }
            } else {
                ToastUtil.showErrorToast(LoginActivity.this, getResources().getString(R.string.connect_server_error));
            }
            if (null != materialDialogsUtil)
                materialDialogsUtil.dismiss();
        }

    }

    class TextWatchListener implements TextWatcher {
        EditText et;

        public TextWatchListener(EditText et) {
            this.et = et;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (et.getId() == R.id.mailEt) {
                if (!TextUtils.isEmpty(mEmailEt.getText().toString().trim())) {
                    mEmailEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.drawable.relationship_input_del), null);
                } else {
                    mEmailEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }
            if (et.getId() == R.id.pwdEt) {
                if (!TextUtils.isEmpty(mPwdEt.getText().toString().trim())) {
                    mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.drawable.relationship_input_del), null);
                    if (!TextUtils.isEmpty(mEmailEt.getText().toString().trim()))
                        mLoginBt.setEnabled(true);
                } else {
                    mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    mLoginBt.setEnabled(false);
                }
            }
        }
    }

    class FinishSelfReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (null != arg1.getAction()
                    && (arg1.getAction().equals(FINISHLOGINACTIVITY_1) || arg1.getAction().equals(FINISHLOGINACTIVITY_2))) {
                finish();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != finishSelfReceiver) {
            unregisterReceiver(finishSelfReceiver);
        }
    }

}
