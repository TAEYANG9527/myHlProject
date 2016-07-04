package com.itcalf.renhe.dto;

import java.io.Serializable;

public class NearbyPeople implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;-7 没有按照行业过滤的权限
	private int filterPrivilege;//是否有根据行业过滤的权限（1有，-1没有）
	private MemberList[] memberList;

	public static class MemberList implements Serializable {
		private static final long serialVersionUID = 1L;
		private String memberId;
		private String sid;
		private String name;
		private String avatar;
		private String curTitle;
		private String curCompany;
		private String industry;
		private String distance;//距离

		@Override
		public String toString() {
			return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("memberId", memberId).append("sid", sid)
					.append("name", name).append("avatar", avatar).append("curTitle", curTitle).append("curCompany", curCompany)
					.append("industry", industry).append("distance", distance).toString();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDistance() {
			return distance;
		}

		public void setDistance(String distance) {
			this.distance = distance;
		}

		public String getMemberId() {
			return memberId;
		}

		public void setMemberId(String memberId) {
			this.memberId = memberId;
		}

		public String getAvatar() {
			return avatar;
		}

		public void setAvatar(String avatar) {
			this.avatar = avatar;
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

		public String getIndustry() {
			return industry;
		}

		public void setIndustry(String industry) {
			this.industry = industry;
		}

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
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

	public int getFilterPrivilege() {
		return filterPrivilege;
	}

	public void setFilterPrivilege(int filterPrivilege) {
		this.filterPrivilege = filterPrivilege;
	}

	public MemberList[] getMemberList() {
		return memberList;
	}

	public void setMemberList(MemberList[] memberList) {
		this.memberList = memberList;
	}

}
