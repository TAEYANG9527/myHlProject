package com.itcalf.renhe.command;

import android.content.Context;

import com.itcalf.renhe.dto.AccountUpgrade;
import com.itcalf.renhe.dto.AccountVipInfo;
import com.itcalf.renhe.dto.AliPayResult;
import com.itcalf.renhe.dto.WeixinPayResult;
import com.itcalf.renhe.dto.WeixinPrepay;

public interface IPayCommand {

	/**
	 * 微信支付前操作
	 * @param sid 会员加密后的id
	 * @param adSId 加密后用户id和密码的信息 以后的每次请求中都要带上它
	 * @param fee 金额 String也可以
	 * @param ip 客户端IP
	 * @param bizType 支付的业务类型 1、 人和币 2、付费会员 3、人脉快递 4、精准推广
	 * @param deviceType 设备
	 * @param tradeDesc 支付描述
	 */
	public WeixinPrepay repayWeixin(Context context, String sid, String adSId, String bizSId, String fee, String ip, int bizType,
			String tradeDesc) throws Exception;

	/**
	 * 查询微信支付的结果
	 * @param sid 会员加密后的id
	 * @param adSId 加密后用户id和密码的信息 以后的每次请求中都要带上它
	 * @param prepayid  预支付回话ID 
	 * @return  状态 1 支付成功 -3 未完成支付 -4 订单未找到 -5 prepayid不能为空
	 */
	public WeixinPayResult searchWeixinPay(Context context, String sid, String adSId, String prepayid) throws Exception;

	/**
	 * @param sid
	 * @param adSId
	 * @param fee 金额
	 * @param subject  名称
	 * @param body 详情
	 * @param bizType 支付的业务类型 1、 人和币 2、付费会员 3、人脉快递 4、精准推广
	 * @param deviceType
	 * @return
	 * @throws Exception 
	 */
	public AliPayResult repayZhifubao(Context context, String sid, String adSId, String bizSId, String fee, String subject,
			String body, int bizType) throws Exception;

	public AccountVipInfo accountVipInfo(Context context, String sid, String adSId) throws Exception;

	public AccountUpgrade accountUpgrade(Context context, String sid, String adSId, int upgradeType) throws Exception;
}
