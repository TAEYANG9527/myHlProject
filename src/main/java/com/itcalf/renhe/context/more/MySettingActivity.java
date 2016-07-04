package com.itcalf.renhe.context.more;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alibaba.wukong.auth.AuthService;
import com.alibaba.wukong.im.IMEngine;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.portal.LogOutTask;
import com.itcalf.renhe.context.portal.LoginAct;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.AsyncImageLoader;
import com.itcalf.renhe.utils.LogoutUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.utils.RequestDialog;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

public class MySettingActivity extends BaseActivity implements OnClickListener {

    private RelativeLayout rootRl;
    private RelativeLayout accountManageRl, payManageRl, privacyRl, newMessageRl, aboutHeliaoRl, commonProblems;
    private Button exitBt;
    private AlertDialog mAlertDialog;
    private LinearLayout closeRH;
    private LinearLayout closeRHLogin;

    private RequestDialog requestDialog;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMyContentView(R.layout.settingmain);
    }

    @Override
    protected void findView() {
        super.findView();
        rootRl = (RelativeLayout) findViewById(R.id.rootrl);
        accountManageRl = (RelativeLayout) findViewById(R.id.account_manage_Rl);
        payManageRl = (RelativeLayout) findViewById(R.id.pay_manage_Rl);
        privacyRl = (RelativeLayout) findViewById(R.id.privacy_Rl);
        newMessageRl = (RelativeLayout) findViewById(R.id.new_message_Rl);
        aboutHeliaoRl = (RelativeLayout) findViewById(R.id.about_heliao_Rl);//关于和聊
        commonProblems = (RelativeLayout) findViewById(R.id.common_problems_Rl);//常见问题
        exitBt = (Button) findViewById(R.id.exitBt);
    }

    @Override
    protected void initData() {
        super.initData();
        setTextValue(R.id.title_txt, "设置");
        requestDialog = new RequestDialog(this, "正在注销");
        msp = this.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        mEditor = msp.edit();
        checkAuth();
    }

    @Override
    protected void initListener() {
        super.initListener();
        accountManageRl.setOnClickListener(this);
        payManageRl.setOnClickListener(this);
        privacyRl.setOnClickListener(this);
        newMessageRl.setOnClickListener(this);
        commonProblems.setOnClickListener(this);
        aboutHeliaoRl.setOnClickListener(this);
        exitBt.setOnClickListener(this);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new MaterialDialogsUtil(this).showIndeterminateProgressDialog(R.string.waitting).cancelable(false).build();
            default:
                return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.account_manage_Rl:
                startActivity(new Intent(this, AccountManagementActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.pay_manage_Rl:
                startActivity(new Intent(this, PayManageActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.privacy_Rl:
                Intent intent = new Intent(this, SettingAuthActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.new_message_Rl:
                startActivity(new Intent(this, SettingNewMsgActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.common_problems_Rl:
                Intent i = new Intent();
                i.setClass(this, WebViewActWithTitle.class);
                i.putExtra("url", Constants.HlUseHelpUrl.HL_USE_HELP_URL);
                i.putExtra("shareable", false);
                startActivity(i);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.about_heliao_Rl://关于和聊
                startActivity(new Intent(this, AboutHeliaoActivity.class));
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.exitBt:
                new LogoutUtil(this, rootRl).closeLogin(false);
                break;
            default:
                break;
        }
    }

    private void checkAuth() {
        new PersonAuthTask(this) {
            public void doPre() {
            }

            public void doPost(com.itcalf.renhe.dto.PersonAuth result) {
                if (null != result && result.getState() == 1) {
                    if (result.isShowVipAddFriendPrivilege()) {
                        mEditor.putBoolean("showVipAddFriendPrivilege", true);
                    } else {
                        mEditor.putBoolean("showVipAddFriendPrivilege", false);
                    }
                    mEditor.putInt("referralType", result.getReferralType());
                    mEditor.putInt("lietouBeContact", result.getLietouBeContact());
                    if (result.isViewProfileHidePrivilege()) {
                        mEditor.putBoolean("viewProfileHidePrivilege", true);
                    } else {
                        mEditor.putBoolean("viewProfileHidePrivilege", false);
                    }
                    if (result.isPayAccountViewFullProfilePrivilege()) {
                        mEditor.putBoolean("payAccountViewFullProfilePrivilege", true);
                    } else {
                        mEditor.putBoolean("payAccountViewFullProfilePrivilege", false);
                    }

                    if (result.isSeoCannotEmbody()) {
                        mEditor.putBoolean("seoCannotEmbody", true);
                    } else {
                        mEditor.putBoolean("seoCannotEmbody", false);
                    }
                    if (result.isStealthViewProfile()) {
                        mEditor.putBoolean("stealthViewProfile", true);
                    } else {
                        mEditor.putBoolean("stealthViewProfile", false);
                    }
                    if (result.isVipViewFullProfile()) {
                        mEditor.putBoolean("vipViewFullProfile", true);
                    } else {
                        mEditor.putBoolean("vipViewFullProfile", false);
                    }
                    mEditor.commit();
                }
            }
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId());
    }

    public void createDialog(Context context) {
        RelativeLayout view = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.exit_dialog, null);
        Builder mDialog = new AlertDialog.Builder(context);
        closeRH = (LinearLayout) view.findViewById(R.id.closeRH);
        closeRHLogin = (LinearLayout) view.findViewById(R.id.closeRH_login);
        mAlertDialog = mDialog.create();
        mAlertDialog.setView(view, 0, 0, 0, 0);
        mAlertDialog.setCanceledOnTouchOutside(true);
        mAlertDialog.show();
        closeRH.setOnClickListener(new ButtonListener());
        closeRHLogin.setOnClickListener(new ButtonListener());
    }

    class ButtonListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (null != mAlertDialog) {
                mAlertDialog.dismiss();
            }
            switch (v.getId()) {
                case R.id.closeRH:
                    // 清空图片内存缓存
                    AsyncImageLoader.getInstance().clearCache();
                    // stopService(new Intent(this, RenheService.class));
                    // 退出时是否清除用户数据
                    if (getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                            .getBoolean("clearcache", false)) {
                        CacheManager.getInstance().populateData(MySettingActivity.this)
                                .clearCache(getRenheApplication().getUserInfo().getEmail(), true);
                    }
                    // 关闭通知栏消息
                    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(10001);
                    RenheApplication.getInstance().exit();
                    break;
                case R.id.closeRH_login:
                    closeLogin();
                    break;
                default:
                    break;
            }
        }
    }

    private void closeLogin() {
        MobclickAgent.onEvent(MySettingActivity.this, "setting_logout");
        requestDialog.addFade(rootRl);
        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                RenheApplication.getInstance().exit();
            }
        };
        final Handler handler = new Handler(new Callback() {

            @Override
            public boolean handleMessage(Message arg0) {
                if (arg0.what == 10) {
                    IMEngine.getIMService(AuthService.class).logout();//退出IM
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.DATA_LOGOUT, true);
                    bundle.putSerializable("userInfo", userInfo);
                    startActivity(LoginAct.class, bundle, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    requestDialog.removeFade(rootRl);
                }
                return false;
            }
        });
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                new LogOutTask(MySettingActivity.this).executeOnExecutor(Executors.newCachedThreadPool());
                userInfo = getRenheApplication().getUserInfo();
                // 清空图片内存缓存
                AsyncImageLoader.getInstance().clearCache();
                CacheManager.getInstance().populateData(MySettingActivity.this)
                        .clearCache(getRenheApplication().getUserInfo().getEmail(), false);
                // 清空shareprefrence数据
                mEditor.clear();
                mEditor.commit();
                /*** 清除新的朋友请求标志 ***/
                SharedPreferences sp = getSharedPreferences("newfriendsCount", 0);
                SharedPreferences.Editor spEditor = sp.edit();
                spEditor.clear();
                spEditor.commit();

                RenheApplication.getInstance().setEnterFound(0);
                // 删除jpush设置
                delMyJPush(RenheApplication.getInstance().getUserInfo());
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(1000);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
                SharedPreferences mmsp = RenheApplication.getInstance().getSharedPreferences("notify_id", Context.MODE_PRIVATE);
                SharedPreferences.Editor mmEditor = mmsp.edit();
                mmEditor.putInt("notify_num", 1);
                mmEditor.commit();

                // 退出登录
                SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("islogined", false);
                editor.commit();

                RenheApplication.getInstance().clearActivity();
                Message msg = new Message();
                msg.what = 10;
                handler.sendMessageDelayed(msg, 2000);
            }
        };

        handler.post(runnable);
    }

    private void delMyJPush(UserInfo userInfo) {
        if (userInfo != null) {
            PushUtil.deletePush();
            PushUtil.registerDevicePush();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
