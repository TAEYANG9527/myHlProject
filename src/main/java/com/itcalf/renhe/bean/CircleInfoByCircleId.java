package com.itcalf.renhe.bean;

import java.io.Serializable;

public class CircleInfoByCircleId implements Serializable {

	/**
	* 
	*/
	private int state;
	private static final long serialVersionUID = 1L;
	private int id;//圈子id
	private String imConversationId;// String im的群聊会话id
	private int creatorMemberId;// int 圈子创建者id
	private String name;//String 圈子名称
	private String note;// String 圈子公告
	private int joinType;// int 圈子加入类型：1 代表所有人可以加入；2 代表需要审批才可以加入；3 代表所有人都不可以加入
	private String avatar;// String 圈子头像
	private int memberCount;// int 圈子成员数量
	private int maxMemberCount;// int 圈子成员最大上限数
	private boolean memberCountAboveMax;// boolean 圈子是否已满员
	private boolean memberExists;// 是否已经是该圈子成员
	private boolean requestExists;// boolean 是否已申请加入
	private boolean circleExists;// boolean 圈子是否存在，true为存在，false为不存在
	private boolean isCreator;// boolean 是否是圈主
	private CircleMember[] memberList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getImConversationId() {
		return imConversationId;
	}

	public void setImConversationId(String imConversationId) {
		this.imConversationId = imConversationId;
	}

	public int getCreatorMemberId() {
		return creatorMemberId;
	}

	public void setCreatorMemberId(int creatorMemberId) {
		this.creatorMemberId = creatorMemberId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getJoinType() {
		return joinType;
	}

	public void setJoinType(int joinType) {
		this.joinType = joinType;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getMemberCount() {
		return memberCount;
	}

	public void setMemberCount(int memberCount) {
		this.memberCount = memberCount;
	}

	public int getMaxMemberCount() {
		return maxMemberCount;
	}

	public void setMaxMemberCount(int maxMemberCount) {
		this.maxMemberCount = maxMemberCount;
	}

	public boolean isCircleExists() {
		return circleExists;
	}

	public void setCircleExists(boolean circleExists) {
		this.circleExists = circleExists;
	}

	public boolean isCreator() {
		return isCreator;
	}

	public void setCreator(boolean isCreator) {
		this.isCreator = isCreator;
	}

	public boolean isMemberCountAboveMax() {
		return memberCountAboveMax;
	}

	public void setMemberCountAboveMax(boolean memberCountAboveMax) {
		this.memberCountAboveMax = memberCountAboveMax;
	}

	public boolean isMemberExists() {
		return memberExists;
	}

	public void setMemberExists(boolean memberExists) {
		this.memberExists = memberExists;
	}

	public boolean isRequestExists() {
		return requestExists;
	}

	public void setRequestExists(boolean requestExists) {
		this.requestExists = requestExists;
	}

	public CircleMember[] getMemberList() {
		return memberList;
	}

	public void setMemberList(CircleMember[] memberList) {
		this.memberList = memberList;
	}

	public static class CircleMember implements Serializable {

		private static final long serialVersionUID = 1L;
		private String name;// String 名字
		private String userface;// String 头像 

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
	}

}
