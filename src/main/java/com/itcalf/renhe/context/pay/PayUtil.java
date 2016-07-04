package com.itcalf.renhe.context.pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.pay.alipay.AliPayTask;
import com.itcalf.renhe.context.pay.alipay.AlipayCommand;
import com.itcalf.renhe.context.pay.alipay.PayResult;
import com.itcalf.renhe.context.pay.wxapi.WeichatPayTask;
import com.itcalf.renhe.context.pay.wxapi.WeixinPayCommand;
import com.itcalf.renhe.controller.GrpcController;
import com.itcalf.renhe.dto.AliPayResult;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.dto.WeixinPrepay;
import com.itcalf.renhe.http.Callback;
import com.itcalf.renhe.http.TaskManager;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.orhanobut.logger.Logger;

import java.util.List;

/**
 * Created by wangning on 2016/5/12.
 */
public class PayUtil implements Callback {
    private MaterialDialogsUtil materialDialogsUtil;
    private int ID_TASK_GET_BUILD_ALIPAY_ORDER = TaskManager.getTaskId();
    private int ID_TASK_GET_BUILD_WEIXINPAY_ORDER = TaskManager.getTaskId();
    private GrpcController grpcController;//grpc调用
    private Handler alipayHandler;//阿里支付handler
    private AlipayCommand alipayCommand;//阿里支付接口调用工具类
    private WeixinPayCommand weixinPayCommand;//微信支付接口调用工具类
    public static final int PAY_TYPE_ALI = 1;
    public static final int PAY_TYPE_WEIXIN = 2;
    private StringBuffer weixinBuffer = new StringBuffer();
    public static PayCallBack payCallBack;
    private Context context;
    private UserInfo userInfo;

    public static final int PAY_BIZ_TYPE_LUCKYMONEY = 6;//和聊红包
    public static final String PAY_BIZ_TYPE_LUCKYMONEY_GOODNAME = "和聊红包";//和聊红包
    public static final String PAY_BIZ_TYPE_LUCKYMONEY_AD_GOODNAME = "和聊红包广告";//和聊红包广告
    public static final String PAY_BIZ_TYPE_TOPSPEED_INVITE = "极速邀请";//极速邀请
    public static final String PAY_BIZ_TYPE_RECHARGE_BALANCE = "余额充值";//余额充值

    public PayUtil(Context context, PayCallBack payCallBack) {
        this.context = context;
        this.payCallBack = payCallBack;
        this.userInfo = RenheApplication.getInstance().getUserInfo();
        this.materialDialogsUtil = new MaterialDialogsUtil(context);
        this.materialDialogsUtil.showIndeterminateProgressDialog(R.string.loading)
                .cancelable(true).build();
        initPayUtil();
    }

    /**
     * 初始化微信/阿里支付相关工具，用以处理支付结果
     */
    private void initPayUtil() {
        alipayHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case AlipayCommand.SDK_PAY_FLAG: {
                        PayResult payResult = new PayResult((String) msg.obj);
                        Logger.d("阿里支付结果-->>>" + payResult);
                        // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                        String resultStatus = payResult.getResultStatus();
                        switch (resultStatus) {
                            case Constants.AlipayCode.ALIPAY_SUCCESS:// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                                Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show();
                                if (null != payCallBack)
                                    payCallBack.onAliPaySuccess();
                                break;
                            case Constants.AlipayCode.ALIPAY_HANDLING:// 判断resultStatus 为非“9000”则代表可能支付失败
                                // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                                Toast.makeText(context, "支付结果确认中", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.AlipayCode.ALIPAY_FAIL:
                                Toast.makeText(context, "支付失败", Toast.LENGTH_SHORT).show();
                                if (null != payCallBack)
                                    payCallBack.onAliPayFail();
                                break;
                            case Constants.AlipayCode.ALIPAY_USER_CANCLE:
                                Toast.makeText(context, "取消支付", Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.AlipayCode.ALIPAY_NETWORK_ERROR:
                                Toast.makeText(context, "网络连接出错，支付失败", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                    case AlipayCommand.SDK_CHECK_FLAG: {
                        Toast.makeText(context, "检查结果为：" + msg.obj, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });
        weixinPayCommand = new WeixinPayCommand(context);
        alipayCommand = new AlipayCommand((Activity) context, alipayHandler, context.getString(R.string.rsa_private));
    }

    /**
     * 根据payType调用微信/阿里支付
     *
     * @param fee      金额
     * @param bizType  支付的业务类型 1、 人和币 2、付费会员 3、人脉快递 4、精准推广 5、实名认证 6、和聊红包
     * @param goodName 支付描述
     */
    public void initPay(int payType, String fee, int bizType, String bizSid, String goodName) {
        if (payType == PAY_TYPE_WEIXIN) {
            boolean isInstall = checkAppExist(context, "com.tencent.mm");
            if (!isInstall) {
                ToastUtil.showToast(context, "请安装微信客户端");
                return;
            }
            buildWeixinPayOrder(fee, bizType, bizSid, goodName);
        } else if (payType == PAY_TYPE_ALI) {
            buildAliPayOrder(fee, bizType, bizSid, goodName, goodName);
        }
    }

    /**
     * 付款——获取阿里支付所需字段,生成阿里支付订单
     */
    private void buildAliPayOrder(String fee, int bizType, String bizSid, String goodName, String desc) {
        AliPayTask aliPayTask = new AliPayTask(context, userInfo.getSid(), userInfo.getAdSId(), fee,
                goodName, desc, bizType, bizSid, alipayCommand, new AliPayTask.AliPayTaskCallBack() {

            @Override
            public void onPre() {
                showProgressDialog();
            }

            @Override
            public void doPost(AliPayResult result) {
                dismissProgressDialog();
            }
        });
        aliPayTask.execute();
    }

    /**
     * 付款——获取微信支付所需字段,生成微信支付订单
     */
    private void buildWeixinPayOrder(String fee, int bizType, String bizSid, String goodName) {
        WeichatPayTask weichatPayTask = new WeichatPayTask(context, userInfo.getSid(), userInfo.getAdSId(),
                fee, bizType, bizSid, goodName, weixinPayCommand, new WeichatPayTask.WeiXinTaskCallBack() {

            @Override
            public void onPre() {
                showProgressDialog();
            }

            @Override
            public void doPost(WeixinPrepay result) {
                dismissProgressDialog();
            }
        });
        weichatPayTask.execute();
    }

    @Override
    public void onSuccess(int type, Object result) {

    }

    @Override
    public void onFailure(int type, String msg) {

    }

    @Override
    public void cacheData(int type, Object data) {

    }

    public void showProgressDialog() {
        if (null != materialDialogsUtil)
            materialDialogsUtil.show();
    }

    public void dismissProgressDialog() {
        if (null != materialDialogsUtil)
            materialDialogsUtil.dismiss();
    }

    public boolean checkAppExist(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setPackage(packageName);
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        if (list.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 支付回调
     */
    public interface PayCallBack {
        void onWeiXinPaySuccess();

        void onAliPaySuccess();

        void onWeiXinPayFail();

        void onAliPayFail();
    }
}
