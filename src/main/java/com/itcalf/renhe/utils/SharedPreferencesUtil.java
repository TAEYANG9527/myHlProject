package com.itcalf.renhe.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/11/5
 */
public class SharedPreferencesUtil {

    private static Context mContext;

    public SharedPreferencesUtil() {
    }

    public static void config(Context context) {
        mContext = context;
    }

    /**
     * 获取 String 类型
     *
     * @param key      储存关键字
     * @param defValue 默认值
     * @param isUser   是否储存在用户账号下（false：储存在终端下与用户无关）
     * @return
     */
    public static String getStringShareData(String key, String defValue, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        return sp.getString(key, defValue);
    }

    /**
     * 获取 int 类型
     *
     * @param key      储存关键字
     * @param defValue 默认值
     * @param isUser   是否储存在用户账号下（false：储存在终端下与用户无关）
     * @return
     */
    public static int getIntShareData(String key, int defValue, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        return sp.getInt(key, defValue);
    }

    /**
     * 获取 boolean 类型
     *
     * @param key      储存关键字
     * @param defValue 默认值
     * @param isUser   是否储存在用户账号下（false：储存在终端下与用户无关）
     * @return
     */
    public static boolean getBooleanShareData(String key, boolean defValue, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key, defValue);
    }

    /**
     * 获取 long 类型
     *
     * @param key      储存关键字
     * @param defValue 默认值
     * @param isUser   是否储存在用户账号下（false：储存在终端下与用户无关）
     * @return
     */
    public static long getLongShareData(String key, long defValue, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        return sp.getLong(key, defValue);
    }

    /**
     * 写入 String 类型
     *
     * @param key    储存关键字
     * @param value  输入保存值
     * @param isUser 是否储存在用户账号下（false：储存在终端下与用户无关）
     */
    public static void putStringShareData(String key, String value, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor et = sp.edit();
        et.putString(key, value);
        et.apply();
    }

    /**
     * 写入 int 类型
     *
     * @param key    储存关键字
     * @param value  输入保存值
     * @param isUser 是否储存在用户账号下（false：储存在终端下与用户无关）
     */
    public static void putIntShareData(String key, int value, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor et = sp.edit();
        et.putInt(key, value);
        et.apply();
    }

    /**
     * 写入 boolean 类型
     *
     * @param key    储存关键字
     * @param value  输入保存值
     * @param isUser 是否储存在用户账号下（false：储存在终端下与用户无关）
     */
    public static void putBooleanShareData(String key, boolean value, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor et = sp.edit();
        et.putBoolean(key, value);
        et.apply();
    }

    /**
     * 写入 long 类型
     *
     * @param key    储存关键字
     * @param value  输入保存值
     * @param isUser 是否储存在用户账号下（false：储存在终端下与用户无关）
     */
    public static void putLongShareData(String key, long value, boolean isUser) {
        SharedPreferences sp;
        if (isUser) {
            sp = mContext.getSharedPreferences(Constants.USER_SHAREDPREFERENCES + RenheApplication.getInstance().getUserInfo().getSid(), Context.MODE_PRIVATE);
        } else {
            sp = mContext.getSharedPreferences(Constants.HL_SHAREDPREFERENCES, Context.MODE_PRIVATE);
        }
        SharedPreferences.Editor et = sp.edit();
        et.putLong(key, value);
        et.apply();
    }

}

