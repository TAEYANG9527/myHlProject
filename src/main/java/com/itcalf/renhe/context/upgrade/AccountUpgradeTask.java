package com.itcalf.renhe.context.upgrade;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.R;
import com.itcalf.renhe.command.impl.PayCommandImpl;
import com.itcalf.renhe.context.pay.ChoosePayWayActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.dto.AccountUpgrade;
import com.itcalf.renhe.utils.ToastUtil;

/**
 * 获取支付信息的接口
 * Created by wangning on 2016/3/30.
 */
public class AccountUpgradeTask extends AsyncTask<Void, Void, AccountUpgrade> {
    private Context context;
    private String sid;
    private String adSId;
    private int upgradeType;
    private ChoosePayWayActivity.PayCallback payCallback;

    public AccountUpgradeTask(Context context, String sid, String adSId, int upgradeType, ChoosePayWayActivity.PayCallback payCallback) {
        this.context = context;
        this.sid = sid;
        this.adSId = adSId;
        this.upgradeType = upgradeType;
        this.payCallback = payCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        RenheIMUtil.showProgressDialog(context, R.string.loading);
    }

    @Override
    protected AccountUpgrade doInBackground(Void... params) {
        try {
            return new PayCommandImpl().accountUpgrade(context, sid, adSId, upgradeType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(AccountUpgrade result) {
        super.onPostExecute(result);
        RenheIMUtil.dismissProgressDialog();
        if (result == null) {
            ToastUtil.showToast(context, "请求失败，稍候重试");
            return;
        }
        ChoosePayWayActivity.launch((Activity) context, result.getBizType(), result.getBizSId(),
                result.getBizSubject(), result.getBizSubject(), result.getTotalFee(), result.getPayFee(),
                result.getCouponFee(), result.getOriginalFee(), payCallback, upgradeType);
    }
}
