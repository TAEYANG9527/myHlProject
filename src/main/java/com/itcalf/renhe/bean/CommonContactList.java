package com.itcalf.renhe.bean;

/**
 * description :
 * Created by Chans Renhenet
 * 2015/7/27
 */
public class CommonContactList {

	private int state;
	private boolean reflash;
	private MemberSidList[] memberSidList;

	public CommonContactList() {
	}

	public class MemberSidList {
		private String sid;

		public MemberSidList() {
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isReflash() {
		return reflash;
	}

	public void setReflash(boolean reflash) {
		this.reflash = reflash;
	}

	public CommonContactList.MemberSidList[] getMemberSidList() {
		return memberSidList;
	}

	public void setMemberSidList(CommonContactList.MemberSidList[] memberSidList) {
		this.memberSidList = memberSidList;
	}
}
