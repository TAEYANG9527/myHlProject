package com.itcalf.renhe.bean;

import com.alibaba.wukong.im.Conversation;

import java.io.Serializable;

/**
 * @author chan
 * @createtime 2015-1-19
 * @功能说明 圈子模型
 */
public class CircleBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private int roleType;
	private int myCreateCount;
	private int participatedCount;
	private String avater;// 头像
	private String name;// 圈名
	private int number;// 圈号
	private int count;// 圈的人数
	private Conversation conversation;

	public CircleBean() {
	}

	public String getAvater() {
		return avater;
	}

	public void setAvater(String avater) {
		this.avater = avater;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	/**
	 * roleType :1 创建者；2管理员；3普通成员
	 */
	public int getRoleType() {
		return roleType;
	}

	/**
	 * roleType :1 创建者；2管理员；3普通成员
	 */
	public void setRoleType(int roleType) {
		this.roleType = roleType;
	}

	public int getMyCreateCount() {
		return myCreateCount;
	}

	public void setMyCreateCount(int myCreateCount) {
		this.myCreateCount = myCreateCount;
	}

	public int getParticipatedCount() {
		return participatedCount;
	}

	public void setParticipatedCount(int participatedCount) {
		this.participatedCount = participatedCount;
	}

}
