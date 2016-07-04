package com.itcalf.renhe.context.wukong.im;

import android.app.Dialog;
import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.wukong.auth.AuthParam;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.utils.MaterialDialogsUtil;

/**
 * 工具类
 * Created by zhongqian.wzq on 2014/11/1.
 */
public class RenheIMUtil {
    private static Dialog mProgressDialog;
    private static MaterialDialogsUtil materialDialogsUtil;

    public static AuthParam buildAuthParam(Long openid, String nickName, String secret) {
        AuthParam param = new AuthParam();
        //demo日常
        param.org = Constants.IMParams.ORG;
        param.domain = Constants.IMParams.DOMAIN;
        param.appKey = Constants.IMParams.APP_KEY;
        param.appSecret = Constants.IMParams.APP_SECRET;
        param.openId = openid;
        param.nickname = nickName;
        param.openSecret = secret;
        return param;
    }

    public synchronized static void showProgressDialog(Context context) {
        materialDialogsUtil = new MaterialDialogsUtil(context);
        if (mProgressDialog == null) {
            mProgressDialog = materialDialogsUtil.showIndeterminateProgressDialog().cancelable(false).build();
        }
        materialDialogsUtil.show();
    }

    public synchronized static void showProgressDialog(Context context, int titleRes) {
        materialDialogsUtil = new MaterialDialogsUtil(context);
        if (mProgressDialog == null) {
            mProgressDialog = materialDialogsUtil.showIndeterminateProgressDialog(titleRes).cancelable(false).build();
        }
        materialDialogsUtil.show();
    }

    public synchronized static void showProgressDialog(Context context, int titleRes, boolean cancelable) {
        materialDialogsUtil = new MaterialDialogsUtil(context);
        if (mProgressDialog == null) {
            mProgressDialog = materialDialogsUtil.showIndeterminateProgressDialog(titleRes).cancelable(cancelable).build();
        }
        materialDialogsUtil.show();
    }

    public synchronized static void dismissProgressDialog() {
        if (materialDialogsUtil != null)
            materialDialogsUtil.dismiss();
        mProgressDialog = null;
    }

    public static void showAlertDialog(final Context context, final String title, final DialogCallback callback) {
        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(context);
        materialDialogsUtil.getBuilder(title).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                callback.onPositive();
            }

            @Override
            public void onNeutral(MaterialDialog dialog) {
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                callback.onCancle();
            }
        });
        materialDialogsUtil.show();
    }

    public interface DialogCallback {
        public void onPositive();

        public void onCancle();
    }

}
