package com.itcalf.renhe.dto;

import java.io.Serializable;
import java.util.Arrays;

public class Followers implements Serializable {
	private static final long serialVersionUID = 5914500440053877364L;
	private int state;
	private FollowerList[] followerList;
	private FollowingList[] followingList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public FollowerList[] getFollowerList() {
		return followerList;
	}

	public void setFollowerList(FollowerList[] followerList) {
		this.followerList = followerList;
	}

	public FollowingList[] getFollowingList() {
		return followingList;
	}

	public void setFollowingList(FollowingList[] followingList) {
		this.followingList = followingList;
	}

	public static class FollowerList implements Serializable {
		private static final long serialVersionUID = 3466364597986358586L;
		private String sid;
		private String name;
		private String userface;
		private String messageboardContent;
		private int accountType;
		private boolean isRealname;

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

		public String getMessageboardContent() {
			return messageboardContent;
		}

		public void setMessageboardContent(String messageboardContent) {
			this.messageboardContent = messageboardContent;
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

		@Override
		public String toString() {
			return "FollowerList [sid=" + sid + " ,name = " + name + " ,userface = " + userface + ", messageboardContent="
					+ messageboardContent + " ]";
		}
	}

	public static class FollowingList implements Serializable {

		private static final long serialVersionUID = -21698138665968790L;
		private String sid;
		private String name;
		private String userface;
		private String messageboardContent;
		private int accountType;
		private boolean isRealname;

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

		public String getMessageboardContent() {
			return messageboardContent;
		}

		public void setMessageboardContent(String messageboardContent) {
			this.messageboardContent = messageboardContent;
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

		@Override
		public String toString() {
			return "FollowingList [sid=" + sid + " ,name = " + name + " ,userface = " + userface + ", messageboardContent="
					+ messageboardContent + " ]";
		}
	}

	@Override
	public String toString() {
		return "Followers [state=" + state + " ,followerList = " + Arrays.toString(followerList) + " ,followingList = "
				+ Arrays.toString(followingList) + " ]";
	}
}
