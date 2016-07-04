package com.itcalf.renhe.context.pay.alipay;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.R;
import com.itcalf.renhe.command.impl.PayCommandImpl;
import com.itcalf.renhe.dto.AliPayResult;
import com.itcalf.renhe.utils.ToastUtil;

/**
 * Created by wangning on 2016/5/12.
 */
public class AliPayTask extends AsyncTask<Void, Void, AliPayResult> {
    private Context context;
    private PayCommandImpl payCommand = new PayCommandImpl();
    private String sid;
    private String adSId;
    private String fee;
    private String subject;
    private String body;
    private int bizType;
    private String bizSId;
    private AliPayTaskCallBack aliPayTaskCallBack;
    private AlipayCommand alipayCommand;//阿里支付接口调用工具类

    /**
     * @param sid
     * @param adSId
     * @param fee     金额
     * @param subject 名称
     * @param body    详情
     * @param bizType 请使用Constants里的常量
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENHE_MONEY    人和币
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_MEMBER_PAY     付费会员
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENMAI_EXPRESS 人脉快递
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_GENERALIZATION 精准推广
     */
    public AliPayTask(Context context, String sid, String adSId, String fee, String subject, String body, int bizType,
                      String bizSId, AlipayCommand alipayCommand, AliPayTaskCallBack aliPayTaskCallBack) {
        this.context = context;
        this.sid = sid;
        this.adSId = adSId;
        this.fee = fee;
        this.subject = subject;
        this.body = body;
        this.bizType = bizType;
        this.bizSId = bizSId;
        this.alipayCommand = alipayCommand;
        this.aliPayTaskCallBack = aliPayTaskCallBack;
    }

    @Override
    protected void onPreExecute() {
//        showDialog(1);
        aliPayTaskCallBack.onPre();
    }

    @Override
    protected AliPayResult doInBackground(Void... params) {
        try {
            return payCommand.repayZhifubao(context, sid, adSId, bizSId, fee, subject, body, bizType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(AliPayResult result) {
        aliPayTaskCallBack.doPost(result);
//        removeDialog(1);

        if (result == null) {
            ToastUtil.showToast(context, R.string.hl_no_newwork);
            return;
        }
        if (result.getState() != 1) {
            ToastUtil.showToast(context, "支付宝生成订单失败,code=" + result.getState());
            return;
        }
        alipayCommand.pay(subject, body, fee, result.getOutTradeNo(), result.getNotifyUrl());
    }

    public interface AliPayTaskCallBack {

        void onPre();

        void doPost(AliPayResult result);

    }
}
