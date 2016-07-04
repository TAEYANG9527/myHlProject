package com.itcalf.renhe.dto;

import com.alibaba.wukong.im.Conversation;

import java.io.Serializable;

/**
 * Title: ConversationItem.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-5 下午6:37:07 <br>
 * @author wangning
 */
public class ConversationItem implements Serializable {

	/**
	 * 
	 */
	public static final int NEW_FRIEND_TYPE = 1;
	public static final int SYSTEM_MSG_TYPE = 2;
	public static final int NEW_INNERMSG_TYPE = 3;
	public static final int IM_CONVERSATION_TYPE = 4;
	public static final int TOU_TIAO_TYPE = 5;
	private static final long serialVersionUID = 1L;

	private int type;//1：是新的好友  2：是系统消息   3：新的站内信    4：IM回话

	private Conversation conversation;
	private ConversationListOtherItem conversationListOtherItem;

	private String iconUrl;
	private String nickname;

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public ConversationListOtherItem getConversationListOtherItem() {
		return conversationListOtherItem;
	}

	public void setConversationListOtherItem(ConversationListOtherItem conversationListOtherItem) {
		this.conversationListOtherItem = conversationListOtherItem;
	}

	public static class ConversationListOtherItem implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String title;
		private String picUrl;
		private String lastMessage;
		private Long createTime;
		private int unreadCount;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getPicUrl() {
			return picUrl;
		}

		public void setPicUrl(String picUrl) {
			this.picUrl = picUrl;
		}

		public String getLastMessage() {
			return lastMessage;
		}

		public void setLastMessage(String lastMessage) {
			this.lastMessage = lastMessage;
		}

		public Long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Long createTime) {
			this.createTime = createTime;
		}

		public int getUnreadCount() {
			return unreadCount;
		}

		public void setUnreadCount(int unreadCount) {
			this.unreadCount = unreadCount;
		}

	}
}
