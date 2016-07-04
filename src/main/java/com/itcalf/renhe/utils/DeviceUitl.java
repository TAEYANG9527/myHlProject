package com.itcalf.renhe.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;

/**
 * Title: DeviceUitl.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-29 上午10:45:34 <br>
 *
 * @author wangning
 */
public class DeviceUitl {
    /**
     * 设备的唯一id，用于做短信验证码的发送数量控制
     *
     * @return
     */
    public static String getDeviceInfo() {
        TelephonyManager tm = (TelephonyManager) RenheApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId() + Constants.JPUSH_APP_BUNDLE;
    }

    public static String getAppVersion() {
        String ver = "";
        try {
            ver = RenheApplication.getInstance().getPackageManager()
                    .getPackageInfo(RenheApplication.getInstance().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        return ver;
    }

    /**
     * 返回当前移动终端的唯一标识
     * 如果是GSM网络，返回IMEI；如果是CDMA网络，返回MEID
     */
    public static String getDeviceIMEI() {
        TelephonyManager tm = (TelephonyManager) RenheApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId() == null ? "" : tm.getDeviceId();
    }

    /**
     * 返回MCC+MNC代码 (SIM卡运营商国家代码和运营商网络代码)(IMSI)
     */
    public static String getDeviceIMSI() {
        TelephonyManager tm = (TelephonyManager) RenheApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId() == null ? "" : tm.getSubscriberId();
    }


    /**
     * 获取手机屏幕高度
     */
    public static int getScreenHeight(Activity paramActivity) {
        Display display = paramActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * 获取手机状态栏高度
     */
    public static int getStatusBarHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.top;
    }

    /**
     * 获取APP的界面高
     * below status bar,include actionbar, above softkeyboard
     */
    public static int getAppHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.height();
    }

    /**
     * 获取软件盘的高度
     */
    public static int getKeyboardHeight(Activity paramActivity) {

        int height = getScreenHeight(paramActivity) - getStatusBarHeight(paramActivity)
                - getAppHeight(paramActivity);
        if (height == 0) {
            height = SharedPreferencesUtil.getIntShareData("KeyboardHeight", 400, false);
        } else {
            SharedPreferencesUtil.putIntShareData("KeyboardHeight", height, false);
        }
        return height;
    }

    /**
     * 判断软键盘是否打开
     */
    public static boolean isKeyBoardShow(Activity paramActivity) {
        int height = getScreenHeight(paramActivity) - getStatusBarHeight(paramActivity)
                - getAppHeight(paramActivity);
        return height != 0;
    }

    /**
     * 隐藏软件盘
     */
    public static void hideSoftInput(View paramEditText) {
        ((InputMethodManager) RenheApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(paramEditText.getWindowToken(), 0);
    }

    /**
     * 显示软键盘
     */
    public static void showKeyBoard(final View paramEditText) {
        paramEditText.requestFocus();
        paramEditText.post(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) RenheApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(paramEditText, 0);
            }
        });
    }
}
