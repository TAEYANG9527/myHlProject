package com.itcalf.renhe.dto;

import java.io.Serializable;

public class CircleQrcodeInfo implements Serializable {
	private static final long serialVersionUID = 125342511212L;

	/**
	 * 返回状态
	 * */
	private int state;

	/**
	 * 圈子二维码
	 * */
	private String qrcode;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getQrcode() {
		return qrcode;
	}

	public void setQrcode(String qrcode) {
		this.qrcode = qrcode;
	}

}
