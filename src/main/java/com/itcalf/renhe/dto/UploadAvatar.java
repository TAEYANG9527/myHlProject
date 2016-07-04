package com.itcalf.renhe.dto;

import java.io.Serializable;

public class UploadAvatar implements Serializable {

	private static final long serialVersionUID = 7474656825244674338L;
	private int state;
	private String userface;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getUserface() {
		return userface;
	}

	public void setUserface(String userface) {
		this.userface = userface;
	}

	@Override
	public String toString() {
		return "UploadAvatar [state=" + state + ", userface=" + userface + "]";
	}

}
