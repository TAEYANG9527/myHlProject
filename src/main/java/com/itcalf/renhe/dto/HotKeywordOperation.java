package com.itcalf.renhe.dto;

/**
 * Title: BookFriendOperation.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-3 下午5:10:34 <br>
 * @author wangning
 */
public class HotKeywordOperation {
	private int state;
	private String[] hotSearchList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String[] getHotSearchList() {
		return hotSearchList;
	}

	public void setHotSearchList(String[] hotSearchList) {
		this.hotSearchList = hotSearchList;
	}

}
