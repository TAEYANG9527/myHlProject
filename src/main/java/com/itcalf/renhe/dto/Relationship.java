package com.itcalf.renhe.dto;

import java.io.Serializable;

public class Relationship implements Serializable {
	private static final long serialVersionUID = -7616454345512833252L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private MemberList[] memberList;

	public static class MemberList implements Serializable {
		private static final long serialVersionUID = 7527964386843338509L;
		private String sid;
		private String name;
		private String userface;
		private String title;
		private String company;
		private int contactStep;
		private int accountType;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
		private boolean isRealname;//是否是实名认证的会员

		@Override
		public String toString() {
			return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("sid", sid).append("name", name)
					.append("userface", userface).append("title", title).append("company", company)
					.append("contactStep", contactStep).toString();
		}

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

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getCompany() {
			return company;
		}

		public void setCompany(String company) {
			this.company = company;
		}

		public int getContactStep() {
			return contactStep;
		}

		public void setContactStep(int contactStep) {
			this.contactStep = contactStep;
		}

		public int getAccountType() {
			return accountType;
		}

		public void setAccountType(int accountType) {
			this.accountType = accountType;
		}

		public boolean isRealname() {
			return isRealname;
		}

		public void setRealname(boolean isRealname) {
			this.isRealname = isRealname;
		}

	}

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("state", state).append("memberList", memberList)
				.toString();
	}

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

}
