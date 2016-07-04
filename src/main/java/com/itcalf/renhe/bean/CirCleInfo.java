package com.itcalf.renhe.bean;

import java.io.Serializable;

public class CirCleInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private int state;

	/**
	 * 圈子是否存在
	 * */
	private boolean circleExists;
	/**
	 * 圈子id
	 * */
	private int id;
	/**
	 * im的群聊会话id
	 * */
	private String imConversationId;
	/**
	 * 圈子创建者id
	 * */
	private int creatorMemberId;
	/**
	 * 圈子名称
	 * */
	private String name;
	/**
	 * 圈子加入类型
	 * */
	private int joinType;
	/**
	 * 圈子头像
	 * */
	private String avatar;
	/**圈子成员数量**/
	private int memberCount;
	/**圈子是否已满员**/
	private boolean memberCountAboveMax;
	/**是否已经是该圈子成员**/
	private boolean memberExists;
	/**是否已申请加入**/
	private boolean requestExists;
	/**圈子公告**/
	private String note;
	/** 圈子成员最大上限数**/
	private int maxMemberCount;
	/** 圈子H5页面Url**/
	private String httpShortUrl;
	private boolean isCollected;// boolean 说明：是否已经收藏
	public CirCleInfo() {

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isCircleExists() {
		return circleExists;
	}

	public void setCircleExists(boolean circleExists) {
		this.circleExists = circleExists;
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

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public int getMemberMaxCount() {
		return maxMemberCount;
	}

	public void setMemberMaxCount(int memberMaxCount) {
		this.maxMemberCount = memberMaxCount;
	}

	public String getHttpShortUrl() {
		return httpShortUrl;
	}

	public void setHttpShortUrl(String httpShortUrl) {
		this.httpShortUrl = httpShortUrl;
	}

	public boolean isCollected() {
		return isCollected;
	}

	public void setIsCollected(boolean isCollected) {
		this.isCollected = isCollected;
	}
}
