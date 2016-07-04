package com.itcalf.renhe.bean;

import java.io.Serializable;

public class SearchRenmai implements Serializable {
	private static final long serialVersionUID = 1L;

	private int state;
	private MemberList[] memberList;
	private MemberCircle[] memberCircleList;
	private CircleList[] circleList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public MemberList[] getMemberList() {
		return memberList;
	}

	public void setMemberList(MemberList[] memberList) {
		this.memberList = memberList;
	}

	public MemberCircle[] getMemberCircleList() {
		return memberCircleList;
	}

	public void setMemberCircleList(MemberCircle[] memberCircleList) {
		this.memberCircleList = memberCircleList;
	}

	public CircleList[] getCircleList() {
		return circleList;
	}

	public void setCircleList(CircleList[] circleList) {
		this.circleList = circleList;
	}
}
