package com.itcalf.renhe.dto;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * 获取某人关注的人列表的接口/获取某人粉丝列表的接口
 * 
 */
public class MsgFollows implements Serializable {

	private static final long serialVersionUID = -626514841960708673L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private FollowingList[] followerList; // 粉丝列表
	private FollowingList[] followingList; // 关注列表

	/**
	 * 
	 * 关注的人的列表
	 * 
	 */
	public static class FollowingList implements Serializable {

		private static final long serialVersionUID = -7461944132115749229L;
		private String sid; // 关注人的sid
		private String name; // 关注人姓名
		private String userface; // 关注人头像
		private String messageboardContent; // 关注人最近一条客厅留言

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

		@Override
		public String toString() {
			return "FollowingList [messageboardContent=" + messageboardContent + ", name=" + name + ", sid=" + sid + ", userface="
					+ userface + "]";
		}

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public FollowingList[] getFollowerList() {
		return followerList;
	}

	public void setFollowerList(FollowingList[] followerList) {
		this.followerList = followerList;
	}

	public FollowingList[] getFollowingList() {
		return followingList;
	}

	public void setFollowingList(FollowingList[] followingList) {
		this.followingList = followingList;
	}

	@Override
	public String toString() {
		return "MsgFollows [followerList=" + Arrays.toString(followerList) + ", followingList=" + Arrays.toString(followingList)
				+ ", state=" + state + "]";
	}

}
