package com.itcalf.renhe.dto;

/**
 * Title: WebViewContent.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2015    <br>
 * Create DateTime: 2015-3-6 上午11:55:36 <br>
 * @author wangning
 */
public class WebViewContent {
	private int state;
	private int id;
	private String title;
	private String image;
	private String describe;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicUrl() {
		return image;
	}

	public void setPicUrl(String picUrl) {
		this.image = picUrl;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

}
