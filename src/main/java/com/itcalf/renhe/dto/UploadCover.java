package com.itcalf.renhe.dto;

import java.io.Serializable;

public class UploadCover implements Serializable {

	private static final long serialVersionUID = 7474656825244674338L;
	private int state;
	private String cover;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

}
