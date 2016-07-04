package com.itcalf.renhe.bean;

import java.io.Serializable;

public class CircleJoinRequestListInfo implements Serializable {
	private static final long serialVersionUID = 1234523523452L;

	/**
	 * 申请记录id
	 * */
	private int id;

	/**
	 * 申请记录id
	 * */
	private boolean invitationMemberExists;

	/**
	 * 申请状态，0代表未处理，1代表申请通过，2代表申请被拒绝
	 * */
	private int approveState;

	/**
	 * 申请理由
	 * */
	private String purpose;

	/**
	 * 申请时间毫秒数
	 * */
	private String createdDate;

	/**
	 * 圈子信息 :此实体类用于查询个人圈子申请
	 * */
	private CirCleInfo circleInfo;

	/**
	 * 申请者信息
	 * */
	private CircleJoinUserInfo memberInfo;

	/**
	 * 邀请者信息
	 * */
	private CircleJoinUserInfo invitationMemberInfo;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isInvitationMemberExists() {
		return invitationMemberExists;
	}

	public void setInvitationMemberExists(boolean invitationMemberExists) {
		this.invitationMemberExists = invitationMemberExists;
	}

	public int getApproveState() {
		return approveState;
	}

	public void setApproveState(int approveState) {
		this.approveState = approveState;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public CirCleInfo getCircleInfo() {
		return circleInfo;
	}

	public void setCircleInfo(CirCleInfo circleInfo) {
		this.circleInfo = circleInfo;
	}

	public CircleJoinUserInfo getMemberInfo() {
		return memberInfo;
	}

	public void setMemberInfo(CircleJoinUserInfo memberInfo) {
		this.memberInfo = memberInfo;
	}

	public CircleJoinUserInfo getInvitationMemberInfo() {
		return invitationMemberInfo;
	}

	public void setInvitationMemberInfo(CircleJoinUserInfo invitationMemberInfo) {
		this.invitationMemberInfo = invitationMemberInfo;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}