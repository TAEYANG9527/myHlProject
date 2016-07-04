package com.itcalf.renhe.dto;

import java.io.Serializable;

public class CheckMobile implements Serializable {

	private static final long serialVersionUID = 1L;
	private int state;
	private boolean bind;
	private String mobile;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isBind() {
		return bind;
	}

	public void setBind(boolean bind) {
		this.bind = bind;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("state", state).append("bind", bind)
				.append("mobile", mobile).toString();
	}

}
