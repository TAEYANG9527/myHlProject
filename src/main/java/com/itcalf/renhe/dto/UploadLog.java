package com.itcalf.renhe.dto;

import java.io.Serializable;

public class UploadLog implements Serializable {

	private static final long serialVersionUID = 1L;
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "UploadLog [state=" + state + "]";
	}
}
