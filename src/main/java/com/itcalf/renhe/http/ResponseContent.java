package com.itcalf.renhe.http;

public class ResponseContent {

	public int code;
	public boolean isSuccessFul;
	public String errMsg;
	public byte[] content;

	@Override
	public String toString() {
		return "ResponseContent [code=" + code + ", isSuccessFul=" + isSuccessFul + ", errMsg=" + errMsg + ", content="
				+ new String(content == null ? new byte[1] : content) + "]";
	}

}
