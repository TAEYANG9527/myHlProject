package com.itcalf.renhe.context;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.portal.FirstGuideVideoSurfaceActivity;
import com.itcalf.renhe.context.portal.LoginAct;
import com.itcalf.renhe.context.portal.LoginActivity;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.ManifestUtil;
import com.itcalf.renhe.utils.PushUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.Date;

/**
 * 和聊登录信息初始化
 * Created by wangning on 2015/7/25.
 */
public class HlEngine {
    private Context context;
    private SharedPreferences msp;
    private String sid = "";
    private String imei = "";
    private String imsi = "";
    private UserInfo userInfo;
    private Intent mIntent;

    public HlEngine(Context context) {
        this.context = context;
    }

    public HlEngine(Context context, Intent intent) {
        this.context = context;
        this.mIntent = intent;
    }

    public void init() {
        RenheApplication.getInstance().setFromLoginIn(false);
        msp = context.getSharedPreferences("first_guide_setting_info", 0);
        //Set UMENG channel
        String channel = ManifestUtil.getChannel(context.getApplicationContext());
//        AnalyticsConfig.setChannel(channel);
        MobclickAgent.startWithConfigure(
                new MobclickAgent.UMAnalyticsConfig(context, "5379634556240b825e0522bd", channel, MobclickAgent.EScenarioType.E_UM_NORMAL));
        MobclickAgent.setScenarioType(context, MobclickAgent.EScenarioType.E_UM_NORMAL);
        // 友盟统计
//        MobclickAgent.up(context);
        PushUtil.registerDevicePush();
        userInfo = RenheApplication.getInstance().getUserCommand().getLoginUser();

//		context.startService(new Intent(context, RenheService.class));
        //判断是否登入，登入传sid
        SharedPreferences prefs = context.getSharedPreferences("islogin_info", 0);
        boolean islogined = prefs.getBoolean("islogined", false);
        if (userInfo != null) {
            if (islogined) {
                sid = userInfo.getSid();
            } else {
                sid = "";
            }
        }
        //初始化手机参数,只在应用启动的时候设置一次
        imei = RenheApplication.getInstance().getDeviceIMEI();
        if (TextUtils.isEmpty(imei)) {
            imei = DeviceUitl.getDeviceIMEI();
            RenheApplication.getInstance().setDeviceIMEI(imei);
        }
        imsi = RenheApplication.getInstance().getDeviceIMSI();
        if (TextUtils.isEmpty(imsi)) {
            imsi = DeviceUitl.getDeviceIMSI();
            RenheApplication.getInstance().setDeviceIMSI(imsi);
        }
        userAutoLogin();
    }

    public void userAutoLogin() {
        if (msp.getBoolean("ifFirst", true)) {
            Intent intent = new Intent();
            intent.putExtra("imei", imei);
            intent.putExtra("imsi", imsi);
            intent.putExtra("sid", sid);
            //视频启动
            intent.setClass(context, FirstGuideVideoSurfaceActivity.class);
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            ((Activity) context).finish();
        } else {
            // 判断上次是否退出登入
            SharedPreferences prefs = context.getSharedPreferences("islogin_info", 0);
            boolean islogined = prefs.getBoolean("islogined", true);
            if (null == userInfo) {
                userInfo = RenheApplication.getInstance().getUserCommand().getLoginUser();
            }
            String email = "";
            if (null != userInfo) {
                email = userInfo.getLoginAccountType();
                if (null != email) {
                    if (islogined) {
                        // 已登入，去主页
                        forwardToHall(userInfo);// 改为离线登陆，不需要每次都调用登陆接口
                        PushUtil.registerPush(userInfo);
                    } else {
                        Intent intent = mIntent;
                        intent.setClass(context, LoginAct.class);
                        intent.putExtra("userInfo", userInfo);
                        intent.putExtra(Constants.DATA_LOGOUT, true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        ((Activity) context).finish();
                    }
                } else {
                    Intent intent = mIntent;
                    intent.setClass(context, LoginActivity.class);
                    intent.putExtra(Constants.DATA_LOGOUT, true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    ((Activity) context).finish();
                }
            } else {
                Intent intent = mIntent;
                intent.setClass(context, LoginActivity.class);
                intent.putExtra(Constants.DATA_LOGOUT, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                ((Activity) context).finish();
            }
        }
    }

    private void forwardToHall(UserInfo userInfo) {
        SharedPreferences prefs = context.getSharedPreferences("islogin_info", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("islogined", true);
        editor.commit();

        userInfo.setRemember(true);
        userInfo.setLogintime(DateFormat.format("yyyy-MM-dd hh:mm:ss", new Date()).toString());
        RenheApplication.getInstance().setUserInfo(userInfo);
        RenheApplication.getInstance().setLogin(1);
    }
}
