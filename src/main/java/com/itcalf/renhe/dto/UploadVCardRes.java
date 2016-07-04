package com.itcalf.renhe.dto;

import java.io.Serializable;

public class UploadVCardRes implements Serializable {

	private static final long serialVersionUID = 9101549438623058425L;

	public int state;
	public long cardId;

	@Override
	public String toString() {
		return "UploadVCardRes [state=" + state + ", cardId=" + cardId + "]";
	}

}
