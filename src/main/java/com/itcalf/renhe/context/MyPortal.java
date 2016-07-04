package com.itcalf.renhe.context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.itcalf.renhe.context.portal.AddMemberDeviceTokenTask;
import com.itcalf.renhe.context.portal.AddNewDeviceTokenTask;
import com.itcalf.renhe.context.portal.FirstGuideVideoSurfaceActivity;
import com.itcalf.renhe.context.portal.LoginAct;
import com.itcalf.renhe.context.portal.LoginActivity;
import com.itcalf.renhe.context.template.BaseActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.ManifestUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.itcalf.renhe.utils.SharedPreferencesUtil;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.xys.shortcut_lib.ShortcutSuperUtils;
import com.xys.shortcut_lib.ShortcutUtils;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.Executors;

/**
 * Feature:系统默认入口界面 Desc:检查系统版本，提示更新下载，默认显示人和网宣传图片
 *
 * @author xp
 */
public class MyPortal extends BaseActivity {

    private TextView mcopyRightText;
    //	private Button registerBtn, loginBtn;
    private SharedPreferences msp;
    private boolean isFromNotify = false;
    private ImageView custlogo;
    private String sid = "";
    private UserInfo userInfo;
    private boolean isGuideVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myportal);
        custlogo = (ImageView) findViewById(R.id.custlogo);
        mcopyRightText = (TextView) findViewById(R.id.copyrightText);
        InputStream is = getResources().openRawResource(R.drawable.logo_bd);
        BitmapDrawable bd = new BitmapDrawable(getResources(), is);
        Bitmap bitmap = bd.getBitmap();
        custlogo.setImageBitmap(bitmap);
        cacheBitmapList.add(bitmap);
        RenheApplication.getInstance().setFromLoginIn(false);
        msp = getSharedPreferences("first_guide_setting_info", 0);
        //Set UMENG channel
        String channel = ManifestUtil.getChannel(getApplicationContext());
        MobclickAgent.startWithConfigure(
                new MobclickAgent.UMAnalyticsConfig(this, "5379634556240b825e0522bd", channel, MobclickAgent.EScenarioType.E_UM_NORMAL));
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        //注册信鸽推送
        PushUtil.registerDevicePush();
        if (getIntent().getBooleanExtra("fromNotify", false)) {
            isFromNotify = true;
        }
        userInfo = getRenheApplication().getUserCommand().getLoginUser();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                userAutoLogin();
            }
        }, 1000);

//        startService(new Intent(this, RenheService.class));
        //判断是否登入，登入传sid
        SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
        boolean islogined = prefs.getBoolean("islogined", false);
        if (userInfo != null) {
            if (islogined) {
                sid = userInfo.getSid();
            } else {
                sid = "";
            }
        }
        addShortcut();
    }

    public void userAutoLogin() {
        if (msp.getBoolean("ifFirst", true)) {
            new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
            Intent intent = new Intent();
            intent.putExtra("sid", sid);
            //视频启动
            intent.setClass(this, FirstGuideVideoSurfaceActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            finish();
        } else {
            // 判断上次是否退出登入
            SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
            boolean islogined = prefs.getBoolean("islogined", true);
            if (null == userInfo) {
                userInfo = getRenheApplication().getUserCommand().getLoginUser();
            }
            String email = "";
            if (null != userInfo) {
                email = userInfo.getLoginAccountType();
                if (null != email) {
                    if (islogined) {
                        // 已登入，去主页
                        forwardToHall(userInfo);// 改为离线登陆，不需要每次都调用登陆接口
                        PushUtil.registerPush(userInfo);
                        new AddMemberDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                    } else {
                        new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                        Intent intent = new Intent();
                        intent.setClass(this, LoginAct.class);
                        intent.putExtra("userInfo", userInfo);
                        intent.putExtra(Constants.DATA_LOGOUT, true);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        finish();
                    }
                } else {
                    new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                    Intent intent = new Intent();
                    intent.setClass(this, LoginActivity.class);
                    intent.putExtra(Constants.DATA_LOGOUT, true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    finish();
                }
            } else {
                new AddNewDeviceTokenTask(getApplicationContext()).executeOnExecutor(Executors.newCachedThreadPool());
                Intent intent = new Intent();
                intent.setClass(this, LoginActivity.class);
                intent.putExtra(Constants.DATA_LOGOUT, true);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();
            }
        }
    }

    private void forwardToHall(UserInfo userInfo) {

        SharedPreferences prefs = getSharedPreferences("islogin_info", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("islogined", true);
        editor.commit();

//        startService(new Intent(this, RenheService.class));
        userInfo.setRemember(true);
        userInfo.setLogintime(DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString());
        //		getRenheApplication().getUserCommand().insertOrUpdate(userInfo);
        getRenheApplication().setUserInfo(userInfo);
        getRenheApplication().setLogin(1);
        //阿里云统计：用户登录埋点
        MANService manService = MANServiceProvider.getService();
        if (null != manService && null != manService.getMANAnalytics())
            manService.getMANAnalytics().updateUserAccount(userInfo.getName(), userInfo.getId() + "");
        //处理从notification、外部浏览器点击圈子、档案URL链接跳转到和聊app之后的intent
        Intent browserIntent = getIntent();
        String data = browserIntent.getDataString();
        if (null == browserIntent) {
            browserIntent = new Intent();
        }
        browserIntent.setClass(this, TabMainFragmentActivity.class);
        if (!TextUtils.isEmpty(data)) {
            browserIntent.putExtra("browserData", data);
        }
        if (isFromNotify) {
            browserIntent.putExtra("fromNotify", true);
            if (null != getIntent().getSerializableExtra("conversation")) {
                Conversation jumpToConversation = (Conversation) getIntent().getSerializableExtra("conversation");
                if (null != jumpToConversation) {
                    browserIntent.putExtra("notifyconversation", jumpToConversation);
                }
            }
        }
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(browserIntent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
        finish();
    }

    /**
     * 添加和聊的快捷方式
     */
    private void addShortcut() {
        if (!SharedPreferencesUtil.getBooleanShareData(Constants.SHAREDPREFERENCES_KEY.ADD_HELIAO_SHORT_CUT, false, false)) {
            SharedPreferencesUtil.putBooleanShareData(Constants.SHAREDPREFERENCES_KEY.ADD_HELIAO_SHORT_CUT, true, false);
            // 系统方式创建
            // ShortcutUtils.addShortcut(this, getShortCutIntent(), mShortcutName);

            // 创建前判断是否存在
            if (!ShortcutSuperUtils.isShortCutExist(this, getString(R.string.app_name), getShortCutIntent())) {
                ShortcutUtils.addShortcut(this, getShortCutIntent(), getString(R.string.app_name), false,
                        BitmapFactory.decodeResource(getResources(), R.drawable.icon));
            }
            // 为某个包创建快捷方式
            // ShortcutSuperUtils.addShortcutByPackageName(this, this.getPackageName());
        }
    }

    private Intent getShortCutIntent() {
        // 使用MAIN，可以避免部分手机(比如华为、HTC部分机型)删除应用时无法删除快捷方式的问题
        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClass(this, MyPortal.class);
        return intent;
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
