package com.itcalf.renhe.dto;

public class AliPayResult {

	private int state;
	private String outTradeNo;
	private String notifyUrl;

	/**
	 * 1 请求成功；-1 权限不足；-2发生未知错误；-3 支付金额不能为0 -4 支付描述不能为空 -7 bizType不存在
	 * @return
	 */
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

}
