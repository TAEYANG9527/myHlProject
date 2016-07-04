package com.itcalf.renhe.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.pay.ChoosePayWayActivity;
import com.itcalf.renhe.context.pay.PayUtil;
import com.itcalf.renhe.context.pay.wxapi.Constants;
import com.itcalf.renhe.utils.ToastUtil;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weixin_pay_result);

        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    ToastUtil.showToast(getApplicationContext(), "支付成功");
                    if (ChoosePayWayActivity.payCallback != null) {
                        ChoosePayWayActivity.payCallback.onPayResult(0, 1);
                    }
                    if (null != PayUtil.payCallBack)
                        PayUtil.payCallBack.onWeiXinPaySuccess();
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    ToastUtil.showToast(getApplicationContext(), "已经取消支付");
                    if (ChoosePayWayActivity.payCallback != null) {
                        ChoosePayWayActivity.payCallback.onPayResult(0, 0);
                    }
                    break;
                default:
                    ToastUtil.showToast(getApplicationContext(), "支付失败" + resp.errCode);
                    if (ChoosePayWayActivity.payCallback != null) {
                        ChoosePayWayActivity.payCallback.onPayResult(0, 0);
                    }
                    if (null != PayUtil.payCallBack)
                        PayUtil.payCallBack.onWeiXinPayFail();
                    break;
            }
        }
        finish();
    }
}