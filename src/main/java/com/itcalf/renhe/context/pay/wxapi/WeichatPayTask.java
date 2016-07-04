package com.itcalf.renhe.context.pay.wxapi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.itcalf.renhe.R;
import com.itcalf.renhe.command.IPayCommand;
import com.itcalf.renhe.command.impl.PayCommandImpl;
import com.itcalf.renhe.dto.WeixinPrepay;
import com.itcalf.renhe.utils.ToastUtil;

/**
 * Created by wangning on 2016/5/12.
 */
public class WeichatPayTask extends AsyncTask<Void, Void, WeixinPrepay> {
    private Context context;
    private IPayCommand payCommand = new PayCommandImpl();
    private String sid;
    private String adSId;
    private String fee;
    private int bizType;
    private String bizSId;
    private String tradeDesc;
    private StringBuffer sBuffer = new StringBuffer();

    private WeiXinTaskCallBack weiXinTaskCallBack;
    private WeixinPayCommand weixinPayCommand;

    /**
     * @param sid
     * @param adSId
     * @param fee
     * @param bizType   请使用Constants里的常量
     * @param tradeDesc 支付描述
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENHE_MONEY    人和币
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_MEMBER_PAY     付费会员
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_RENMAI_EXPRESS 人脉快递
     * @see com.itcalf.renhe.Constants#BIZ_TYPE_GENERALIZATION 精准推广
     */
    public WeichatPayTask(Context context, String sid, String adSId, String fee, int bizType, String bizSId, String tradeDesc
            , WeixinPayCommand weixinPayCommand, WeiXinTaskCallBack weiXinTaskCallBack) {
        this.context = context;
        this.sid = sid;
        this.adSId = adSId;
        this.fee = fee;
        this.bizType = bizType;
        this.bizSId = bizSId;
        this.tradeDesc = tradeDesc;
        this.weiXinTaskCallBack = weiXinTaskCallBack;
        this.weixinPayCommand = weixinPayCommand;
    }

    @Override
    protected void onPreExecute() {
//        showDialog(1);
        weiXinTaskCallBack.onPre();
    }

    @Override
    protected void onPostExecute(WeixinPrepay result) {
        weiXinTaskCallBack.doPost(result);
//        removeDialog(1);

        if (result == null) {
            ToastUtil.showToast(context, R.string.hl_no_newwork);
            return;
        }
        if (result.getState() != 1) {
            ToastUtil.showToast(context, "微信生成订单失败,code=" + result.getState());
            return;
        }

        sBuffer.append("prepay_id\n" + result.getPrepayid() + "\n\n");
        weixinPayCommand.sendPayReq();

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected WeixinPrepay doInBackground(Void... params) {
        try {
            String ip = getIp();
            WeixinPrepay prepay = payCommand.repayWeixin(context, sid, adSId, bizSId, fee, ip, bizType,
                    tradeDesc);
            if (prepay != null) {
                weixinPayCommand.genPayReq(prepay, sBuffer);
            }
            return prepay;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getIp() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    public interface WeiXinTaskCallBack {

        void onPre();

        void doPost(WeixinPrepay result);

    }
}