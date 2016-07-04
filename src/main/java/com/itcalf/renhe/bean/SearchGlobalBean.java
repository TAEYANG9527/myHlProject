package com.itcalf.renhe.bean;

import com.itcalf.renhe.po.Contact;

import java.io.Serializable;

/**
 * @description 全局搜索模型类
 * @author Chan
 * @date 2015-4-2
 */

public class SearchGlobalBean implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean isTitle;
	private boolean isFooter;
	private boolean isShowDivideLine;

	/**类型：0 推荐；1 联系人；2 新的人脉 ；3 人脉圈； 4 聊天记录，5 圈子**/
	private int type = 0;
	private String typeName;
	/**推荐**/
	private SearchRecommendedBean searchRecommended;
	/****联系人***/
	private HlContacts contact;
	/****聊天记录***/
	private ChatLog chatLog;
	/**搜索出来新的好友类型**/
	private MemberList memberList;
	/** 人脉圈 * */
	private MemberCircle memberCircleList;
	/**圈子列表**/
	private CircleList circleList;

	public SearchGlobalBean() {

	}

	public boolean isFooter() {
		return isFooter;
	}

	public void setIsFooter(boolean isFooter) {
		this.isFooter = isFooter;
	}

	public boolean isTitle() {
		return isTitle;
	}

	public void setIsTitle(boolean isTitle) {
		this.isTitle = isTitle;
	}

	/**类型：0 推荐；1 联系人,（11,圈子联系人）；2 新的人脉 ；3 人脉圈； 4 聊天记录;5 圈子**/
	public int getType() {
		return type;
	}

	/**类型：0 推荐；1 联系人,（11,圈子联系人）；2 新的人脉 ；3 人脉圈； 4 聊天记录；5 圈子**/
	public void setType(int type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public SearchRecommendedBean getSearchRecommended() {
		return searchRecommended;
	}

	public void setSearchRecommended(SearchRecommendedBean searchRecommended) {
		this.searchRecommended = searchRecommended;
	}

	public HlContacts getContact() {
		return contact;
	}

	public void setContact(HlContacts contact) {
		this.contact = contact;
	}

	public MemberList getMemberList() {
		return memberList;
	}

	public void setMemberList(MemberList memberList) {
		this.memberList = memberList;
	}

	public MemberCircle getMemberCircleList() {
		return memberCircleList;
	}

	public void setMemberCircleList(MemberCircle memberCircleList) {
		this.memberCircleList = memberCircleList;
	}

	public CircleList getCircleList() {
		return circleList;
	}

	public void setCircleList(CircleList circleList) {
		this.circleList = circleList;
	}

	public ChatLog getChatLog() {
		return chatLog;
	}

	public void setChatLog(ChatLog chatLog) {
		this.chatLog = chatLog;
	}

	public boolean isShowDivideLine() {
		return isShowDivideLine;
	}

	public void setIsShowDivideLine(boolean isShowDivideLine) {
		this.isShowDivideLine = isShowDivideLine;
	}
}
