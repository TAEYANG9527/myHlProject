package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: Recommends.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-12 上午10:59:12 <br>
 * @author wangning
 */
public class Recommends implements Serializable {
	private static final long serialVersionUID = -6501243574466513990L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private RecommendedUser[] memberList; // 客厅留言列表 被转发的数量现在人和网暂时没这个功能

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public RecommendedUser[] getMemberList() {
		return memberList;
	}

	public void setMemberList(RecommendedUser[] memberList) {
		this.memberList = memberList;
	}

}
