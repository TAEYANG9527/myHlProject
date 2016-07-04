/*
 * Project: Laiwang
 * 
 * File Created at 2014-12-24
 * 
 * Copyright 2013 Alibaba.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
 */
package com.itcalf.renhe.context.wukong.im.kit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * Description.
 *
 * @author zhongqian.wzq
 */
public class PrefsUtil {
    private static final String PREFRENCE_NAME = "imkit_prefs";

    private SharedPreferences mSharedPrefs;

    private PrefsUtil(){

    }

    private static class InstanceHolder {
        public static PrefsUtil sInstance = new PrefsUtil();
    }

    public static PrefsUtil getInstance() {
        return InstanceHolder.sInstance;
    }

    public void init(Context context){
        mSharedPrefs = context.getSharedPreferences(PREFRENCE_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key){
        if(mSharedPrefs == null) return null;
        return mSharedPrefs.getString(key, null);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void putString(String key, String value){
        if(mSharedPrefs == null) return;
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putString(key, value);
        if(Build.VERSION.SDK_INT >= 9)
            editor.apply();
        else
            editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void remove(String key){
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.remove(key);

        if(Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
