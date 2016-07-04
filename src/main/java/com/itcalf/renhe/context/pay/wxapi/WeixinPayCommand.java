package com.itcalf.renhe.context.pay.wxapi;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.itcalf.renhe.dto.WeixinPrepay;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 微信支付封装
 * @author ZYQ
 *
 */
public class WeixinPayCommand {

	private static final String TAG = "WeixinPayCommand";

	private PayReq req;
	private IWXAPI msgApi;

	public WeixinPayCommand(Context context) {
		msgApi = WXAPIFactory.createWXAPI(context, null);
		req = new PayReq();
		msgApi.registerApp(Constants.APP_ID);
	}

	/**
	 * 生成签名参数
	 */
	public void genPayReq(WeixinPrepay weixinPrepay, StringBuffer stringBuffer) {

		req.appId = weixinPrepay.getAppid();//Constants.APP_ID;
		req.partnerId = weixinPrepay.getPartnerid();// Constants.MCH_ID;
		req.prepayId = weixinPrepay.getPrepayid();// resultunifiedorder.get("prepay_id");
		req.packageValue = weixinPrepay.getExtend();//"Sign=WXPay";
		req.nonceStr = weixinPrepay.getNoncestr();//genNonceStr();
		req.timeStamp = weixinPrepay.getTimestamp();//String.valueOf(genTimeStamp());

		ContentValues contentValues = new ContentValues();
		contentValues.put("appid", req.appId);
		contentValues.put("noncestr", req.nonceStr);
		contentValues.put("package", req.packageValue);
		contentValues.put("partnerid", req.partnerId);
		contentValues.put("prepayid", req.prepayId);
		contentValues.put("timestamp", req.timeStamp);

		req.sign = genAppSign(contentValues, stringBuffer);

		stringBuffer.append("sign\n" + req.sign + "\n\n");

		if (com.itcalf.renhe.Constants.DEBUG_MODE)
			Log.e("orion", contentValues.toString());
	}

	private String genAppSign(ContentValues values, StringBuffer stringBuffer) {
		StringBuilder sb = new StringBuilder();
		sb.append("appid" + '=' + values.get("appid") + '&');
		sb.append("noncestr" + '=' + values.get("noncestr") + '&');
		sb.append("package" + '=' + values.get("package") + '&');
		sb.append("partnerid" + '=' + values.get("partnerid") + '&');
		sb.append("prepayid" + '=' + values.get("prepayid") + '&');
		sb.append("timestamp" + '=' + values.get("timestamp") + '&');
		sb.append("key=" + Constants.API_KEY);
		stringBuffer.append("sign str\n" + sb.toString() + "\n\n");
		String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		if (com.itcalf.renhe.Constants.DEBUG_MODE)
			Log.e("orion", appSign);
		return appSign;
	}

	public void sendPayReq() {
		msgApi.registerApp(Constants.APP_ID);
		msgApi.sendReq(req);
	}

}
