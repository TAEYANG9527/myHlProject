package com.itcalf.renhe.dto;

/**
 * Title: BookFriendOperation.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-3 下午5:10:34 <br>
 * @author wangning
 */
public class MemberCoverOperation {
	private int state;
	private MemberCover[] profileCoverList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public MemberCover[] getProfileCoverList() {
		return profileCoverList;
	}

	public void setProfileCoverList(MemberCover[] profileCoverList) {
		this.profileCoverList = profileCoverList;
	}

}
