package com.itcalf.renhe.dto;

public class WeixinPrepay {

	/**
	 * state int 说明：1 请求成功；-1 权限不足；-2发生未知错误；-3 支付金额不能为0 -4 支付描述不能为空 -5 客户端ip不能为空 -6 prepayid不能为空 -7 bizType不存在
	appid String 公众账号ID
	partnered String 商户号
	prepaid String 预支付交易会话ID
	package String 扩展字段
	noncestr String 随机字符串
	timestamp String 时间戳
	sign String 签名
	 */

	private int state;
	private String appid;
	private String partnerid;
	private String prepayid;
	//private String package
	private String extend;
	private String noncestr;
	private String timestamp;
	private String sign;

	public String getPartnerid() {
		return partnerid;
	}

	public void setPartnerid(String partnerid) {
		this.partnerid = partnerid;
	}

	public String getPrepayid() {
		return prepayid;
	}

	public void setPrepayid(String prepayid) {
		this.prepayid = prepayid;
	}

	public String getExtend() {
		return extend;
	}

	public void setExtend(String extend) {
		this.extend = extend;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getNoncestr() {
		return noncestr;
	}

	public void setNoncestr(String noncestr) {
		this.noncestr = noncestr;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public String toString() {
		return "WeixinPrepay [state=" + state + ", appid=" + appid + ", partnered=" + partnerid + ", prepaid=" + prepayid
				+ ", extend=" + extend + ", noncestr=" + noncestr + ", timestamp=" + timestamp + ", sign=" + sign + "]";
	}
}
