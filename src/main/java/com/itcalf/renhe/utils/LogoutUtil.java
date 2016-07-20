package com.itcalf.renhe.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.ViewGroup;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;
import com.alibaba.wukong.auth.AuthService;
import com.alibaba.wukong.im.IMEngine;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.portal.LogOutTask;
import com.itcalf.renhe.context.portal.LoginAct;
import com.itcalf.renhe.dto.UserInfo;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.Executors;

/**
 * Title: LogoutUtil.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-26 下午3:52:26 <br>
 *
 * @author wangning
 */
public class LogoutUtil {
    private Context context;
    private RequestDialog requestDialog;
    private SharedPreferences msp;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences conversationMsp;
    private SharedPreferences.Editor conversationEditor;
    private ViewGroup rootLayout;
    private UserInfo userInfo;

    public LogoutUtil(Context context, ViewGroup rootLayout) {
        this.context = context;
        msp = context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        mEditor = msp.edit();
        conversationMsp = context.getSharedPreferences("conversation_list", 0);
        conversationEditor = conversationMsp.edit();
        requestDialog = new RequestDialog(context, "正在注销");
        this.rootLayout = rootLayout;
        this.userInfo = RenheApplication.getInstance().getUserInfo();
    }

    /***
     * @功能说明 退出登录
     */
    public void closeLogin(final boolean isKikOutFromBackStack) {
        MobclickAgent.onEvent(context, "setting_logout");
        final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message arg0) {
                return false;
            }
        });
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                new LogOutTask(context).executeOnExecutor(Executors.newCachedThreadPool());
                userInfo = RenheApplication.getInstance().getUserInfo();
                // 清空图片内存缓存
                AsyncImageLoader.getInstance().clearCache();
                CacheManager.getInstance().populateData(context)
                        .clearCache(RenheApplication.getInstance().getUserInfo().getEmail(), false);
                /*** 清除新的朋友请求标志 ***/
                SharedPreferences sp = context.getSharedPreferences("newfriendsCount", 0);
                SharedPreferences.Editor spEditor = sp.edit();
                spEditor.clear();
                spEditor.commit();
                //清除朋友圈的未读提醒
                SharedPreferences sPreferences = context.getSharedPreferences("room_newNotice", 0);
                SharedPreferences.Editor sEditor = sPreferences.edit();
                sEditor.clear();
                sEditor.commit();
                RenheApplication.getInstance().setEnterFound(0);
                //重新初始化首页tab
                RenheApplication.getInstance().initTabFragmentState();
                // 删除信鸽推送设置
                delMyJPush(RenheApplication.getInstance().getUserInfo());
                ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
                SharedPreferences mmsp = RenheApplication.getInstance().getSharedPreferences("notify_id", Context.MODE_PRIVATE);
                SharedPreferences.Editor mmEditor = mmsp.edit();
                mmEditor.putInt("notify_num", 1);
                mmEditor.commit();

                // 退出登录
                SharedPreferences prefs = context.getSharedPreferences("islogin_info", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("islogined", false);
                editor.commit();
                RenheApplication.getInstance().setLogOut(1);
                RenheApplication.getInstance().clearActivity();
                IMEngine.getIMService(AuthService.class).logout();//退出IM
                //阿里云统计：用户注销埋点
                MANService manService = MANServiceProvider.getService();
                if (null != manService && null != manService.getMANAnalytics())
                    manService.getMANAnalytics().updateUserAccount("", "");
                //Growing IO 清空自定义维度
                StatisticsUtil.clearGrowingIOCS();
                if (!isKikOutFromBackStack) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.DATA_LOGOUT, true);
                    bundle.putSerializable("userInfo", userInfo);
                    Intent iii = new Intent(context, LoginAct.class);
                    iii.putExtras(bundle);
                    iii.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(iii);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
                RenheApplication.getInstance().exit();
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
}
