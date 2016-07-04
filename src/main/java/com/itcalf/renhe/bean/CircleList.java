package com.itcalf.renhe.bean;

import com.alibaba.wukong.im.Conversation;

import java.io.Serializable;
import java.util.List;

/**
 * description : 搜索的圈子列表 
 * Created by Chans Renhenet 公用
 * 2015/7/29
 */
public class CircleList implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String imConversationId;
	private String creatorMemberId;
	private String name;
	private String note;
	private int joinType;
	private String avatar;
	private int memberCount;
	private int memberMaxCount;
	private boolean memberCountAboveMax;
	private boolean memberExists;
	private boolean requestExists;

	private Conversation conversation;
	private List<String> listSearchMembers;

	private int position;

	public CircleList() {
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

	public String getCreatorMemberId() {
		return creatorMemberId;
	}

	public void setCreatorMemberId(String creatorMemberId) {
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

	public int getMemberMaxCount() {
		return memberMaxCount;
	}

	public void setMemberMaxCount(int memberMaxCount) {
		this.memberMaxCount = memberMaxCount;
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

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<String> getListSearchMembers() {
		return listSearchMembers;
	}

	public void setListSearchMembers(List<String> listSearchMembers) {
		this.listSearchMembers = listSearchMembers;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}
}
