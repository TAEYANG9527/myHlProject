package com.itcalf.renhe.dto;

import java.io.Serializable;

public class Blacklist implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state;
	private BlockedMemberList[] blockedMemberList;

	public class BlockedMemberList implements Serializable {
		private static final long serialVersionUID = 1L;
		private String sid;
		private String name;
		private String userface;
		private String curTitle;
		private String curCompany;

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUserface() {
			return userface;
		}

		public void setUserface(String userface) {
			this.userface = userface;
		}

		public String getCurTitle() {
			return curTitle;
		}

		public void setCurTitle(String curTitle) {
			this.curTitle = curTitle;
		}

		public String getCurCompany() {
			return curCompany;
		}

		public void setCurCompany(String curCompany) {
			this.curCompany = curCompany;
		}

		@Override
		public String toString() {
			return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("sid", sid).append("name", name)
					.append("userface", userface).append("curTitle", curTitle).append("curCompany", curCompany).toString();
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public BlockedMemberList[] getBlockedMemberList() {
		return blockedMemberList;
	}

	public void setBlockedMemberList(BlockedMemberList[] blockedMemberList) {
		this.blockedMemberList = blockedMemberList;
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("state", state)
				.append("blockedMemberList", blockedMemberList).toString();
	}

}
