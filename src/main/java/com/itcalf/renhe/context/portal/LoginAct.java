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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.register.RegisterAuthActivity;
import com.itcalf.renhe.context.template.ActivityTemplate;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.util.Date;
import java.util.concurrent.Executors;

public class LoginAct extends BaseActivity {

    private TextView changeAccountTv;
    private ImageView headImage;
    private TextView userAccount;
    private Button mRegisterBt;
    private EditText mPwdEt;
    private Button mLoginBt;
    private Drawable imgCloseButton;

    private boolean mLogout;
    private RelativeLayout loginRl;
    public static final String FINISHLOGINACT_1 = "com.renhe.finishloginact1";
    public static final String FINISHLOGINACT_2 = "com.renhe.finishloginact2";
    private FinishSelfReceiver finishSelfReceiver;
    private TextView findPwdTv;

    private RenheApplication renheApplication;
    String userName = "";//用户姓名
    String userEmail = "";//用户登录用户名，手机号或邮箱
    String userHeader = "";
    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ActivityTemplate().doInActivity(this, R.layout.renhe_login);
        if (null != getSupportActionBar()) {
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void findView() {
        super.findView();
        // 标题栏切换按钮
        changeAccountTv = (TextView) findViewById(R.id.changeAccountTv);
        mRegisterBt = (Button) findViewById(R.id.registerbt);
        mPwdEt = (EditText) findViewById(R.id.pwdEt);
        mLoginBt = (Button) findViewById(R.id.loginBt);
        imgCloseButton = getResources().getDrawable(R.drawable.relationship_input_del);
        loginRl = (RelativeLayout) findViewById(R.id.login_rl);
        findPwdTv = (TextView) findViewById(R.id.text1);
        headImage = (ImageView) findViewById(R.id.headImage);
        userAccount = (TextView) findViewById(R.id.userAccount);
        mLoginBt.setEnabled(false);
    }

    @Override
    protected void initData() {
        super.initData();
        materialDialogsUtil = new MaterialDialogsUtil(this);
        materialDialogsUtil.showIndeterminateProgressDialog("登录中...").cancelable(false).build();
        renheApplication = RenheApplication.getInstance();
        mLogout = getIntent().getBooleanExtra(Constants.DATA_LOGOUT, false);
        if (mLogout) {
            userInfo = (UserInfo) getIntent().getSerializableExtra("userInfo");
            if (userInfo == null) {
                userInfo = RenheApplication.getInstance().getUserCommand().getLoginUser();
            }
        } else {
            userInfo = RenheApplication.getInstance().getUserCommand().getLoginUser();
        }
        if (userInfo != null) {
            userName = userInfo.getName();//用户名
            userEmail = userInfo.getLoginAccountType();//登录账户
            userName = TextUtils.isEmpty(userName) ? userEmail : userName;
            userHeader = userInfo.getUserface();

            if (!TextUtils.isEmpty(userName)) {
                userAccount.setText(userName);
                mPwdEt.requestFocus();
            }
            // 头像显示
            if (!TextUtils.isEmpty(userHeader) && null != headImage) {
                try {
                    ImageLoader.getInstance().displayImage(userHeader, headImage, CacheManager.options,
                            CacheManager.animateFirstDisplayListener);
                } catch (Exception e) {
                }
            }
        }

        if (imgCloseButton != null) {
            imgCloseButton.setBounds(0, 0, imgCloseButton.getIntrinsicWidth(), imgCloseButton.getIntrinsicHeight());
        }
        finishSelfReceiver = new FinishSelfReceiver();
        IntentFilter intentFilter = new IntentFilter(FINISHLOGINACT_1);
        intentFilter.addAction(FINISHLOGINACT_2);
        registerReceiver(finishSelfReceiver, intentFilter);

    }

    @Override
    protected void initListener() {
        super.initListener();
        mPwdEt.addTextChangedListener(new TextWatchListener(mPwdEt));
        mRegisterBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                MobclickAgent.onEvent(LoginAct.this, "register_bt");
                startActivity(new Intent(LoginAct.this, RegisterAuthActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        mLoginBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                String pwd = mPwdEt.getText().toString().trim();
                if (pwd.length() == 0) {
                    ToastUtil.showToast(LoginAct.this, getResources().getString(R.string.passwordnotnull));
                    return;
                }
                if (-1 != NetworkUtil.hasNetworkConnection(LoginAct.this)) {
                    new LoginTask().executeOnExecutor(Executors.newCachedThreadPool(), userEmail, pwd);
                } else {
                    ToastUtil.showNetworkError(LoginAct.this);
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
                        mPwdEt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.pwd_icon_aft), null,
                                null, null);
                    } else {
                        mPwdEt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.pwd_icon_pre), null,
                                null, null);
                    }
                }
            }
        });
        findPwdTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(LoginAct.this, WebViewActivityForFindPwd.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        changeAccountTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(LoginAct.this, LoginActivity.class);
                i.putExtra("comeflag", "2login");
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem share = menu.findItem(R.id.item_changelogin);
        share.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_changelogin:
                Intent i = new Intent(LoginAct.this, LoginActivity.class);
                i.putExtra("comeflag", "2login");
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                return true;
            case android.R.id.home:
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            // ToastUtil.showNetworkWIFI(this);
        } else if (0 == NetworkUtil.hasNetworkConnection(this)) {
            // ToastUtil.showNetworkMobile(this);
        } else {
            ToastUtil.showNetworkError(this);
        }
        // 记录是登录状态
        SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("islogined", true);
        editor.commit();

        userInfo.setRemember(true);
        userInfo.setLogintime(DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString());
        renheApplication.getUserCommand().insertOrUpdate(userInfo);
        renheApplication.setUserInfo(userInfo);
        renheApplication.setLogin(1);
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
            return renheApplication.getUserCommand().login(userInfo);
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
                    PushUtil.deletePush();//为了防止用户卸载和聊后（信鸽是不知道用户已卸载，所以没有对该账户和设备接触绑定）再次安装，并且使用另外一个账户登录，导致推送消息错乱
                    PushUtil.registerPush(result);
                    //阿里云统计：用户登录埋点
                    MANService manService = MANServiceProvider.getService();
                    if (null != manService && null != manService.getMANAnalytics())
                        manService.getMANAnalytics().updateUserAccount(result.getName(), result.getId() + "");
                    //Growing IO 设置自定义维度
                    StatisticsUtil.setGrowingIOCS();
                } else if (2 == result.getState()) {// 手机号码登录而且未走完注册流程的，需要跳到完善注册资料的页面（会继续返回mobile字段）
                } else if (-1 == result.getState()) {
                    ToastUtil.showErrorToast(LoginAct.this, getResources().getString(R.string.user_error));
                } else if (-2 == result.getState()) {
                    ToastUtil.showErrorToast(LoginAct.this, getResources().getString(R.string.user_null));
                } else if (-3 == result.getState()) {
                    MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(LoginAct.this);
                    materialDialogsUtil.getBuilder("登录失败", getString(R.string.user_spam_exception),
                            getString(R.string.material_dialog_sure));
                    materialDialogsUtil.show();
                }
            } else {
                ToastUtil.showErrorToast(LoginAct.this, getResources().getString(R.string.connect_server_error));
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
            if (et.getId() == R.id.pwdEt) {
                if (!TextUtils.isEmpty(mPwdEt.getText().toString().trim())) {
                    //					mPwdEt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.pwd_icon_aft), null,
                    //							getResources().getDrawable(R.drawable.relationship_input_del), null);

                    mPwdEt.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.drawable.relationship_input_del), null);
                    mLoginBt.setEnabled(true);
                } else {
                    //					mPwdEt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.pwd_icon_pre), null,
                    //							null, null);

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
                    && (arg1.getAction().equals(FINISHLOGINACT_1) || arg1.getAction().equals(FINISHLOGINACT_2))) {
                finish();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("登陆页面2"); // 统计页面
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("登陆页面2"); // 保证onPageEnd在onPause之前调用,因为onPause中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != finishSelfReceiver) {
            unregisterReceiver(finishSelfReceiver);
        }
    }
}
