package com.itcalf.renhe.dto;

/**
 * Title: MemberCover.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-13 下午5:19:20 <br>
 * @author wangning
 */
public class MemberCover {
	private int id;//封面的id，用于请求用会员提供的封面替换封面的接口
	private String minCover;// String 小的封面图片url
	private String cover;// String 封面的url

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMinCover() {
		return minCover;
	}

	public void setMinCover(String minCover) {
		this.minCover = minCover;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

}
