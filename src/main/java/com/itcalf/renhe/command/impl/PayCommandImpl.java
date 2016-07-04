package com.itcalf.renhe.command.impl;

import android.content.Context;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.command.IPayCommand;
import com.itcalf.renhe.dto.AccountUpgrade;
import com.itcalf.renhe.dto.AccountVipInfo;
import com.itcalf.renhe.dto.AliPayResult;
import com.itcalf.renhe.dto.WeixinPayResult;
import com.itcalf.renhe.dto.WeixinPrepay;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class PayCommandImpl implements IPayCommand {
	@Override
	public WeixinPrepay repayWeixin(Context context, String sid, String adSId, String bizSId, String fee, String ip, int bizType,
			String tradeDesc) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sid", sid);
		params.put("adSId", adSId);
		params.put("bizSId", bizSId);
		params.put("fee", fee);
		params.put("ip", ip);
		params.put("bizType", bizType);
		params.put("deviceType", Constants.PLATFORM_TYPE);
		params.put("tradeDesc", tradeDesc);
		return (WeixinPrepay) HttpUtil.doHttpRequest(Constants.Http.WEIXIN_REPAY, params, WeixinPrepay.class, context);
	}

	@Override
	public WeixinPayResult searchWeixinPay(Context context, String sid, String adSId, String prepayid) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sid", sid);
		params.put("adSId", adSId);
		params.put("prepayid", prepayid);
		return (WeixinPayResult) HttpUtil.doHttpRequest(Constants.Http.WEIXIN_ORDER_SEARCH, params, WeixinPayResult.class,
				context);
	}

	public AliPayResult repayZhifubao(Context context, String sid, String adSId, String bizSId, String fee, String subject,
			String body, int bizType) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sid", sid);
		params.put("adSId", adSId);
		params.put("bizSId", bizSId);
		params.put("fee", fee);
		params.put("subject", subject);
		params.put("body", body);
		params.put("bizType", bizType);
		params.put("deviceType", Constants.PLATFORM_TYPE);
		return (AliPayResult) HttpUtil.doHttpRequest(Constants.Http.ZHIFUBAO_REPAY, params, AliPayResult.class, context);
	}

	@Override
	public AccountVipInfo accountVipInfo(Context context, String sid, String adSId) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sid", sid);
		params.put("adSId", adSId);
		return (AccountVipInfo) HttpUtil.doHttpRequest(Constants.Http.ACCOUNT_VIP_INFO, params, AccountVipInfo.class, context);
	}

	@Override
	public AccountUpgrade accountUpgrade(Context context, String sid, String adSId, int upgradeType) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("sid", sid);
		params.put("adSId", adSId);
		params.put("type", upgradeType);
		params.put("device_type", Constants.PLATFORM_TYPE);
		return (AccountUpgrade) HttpUtil.doHttpRequest(Constants.Http.ACCOUNT_UPGRADE, params, AccountUpgrade.class, context);
	}
}
