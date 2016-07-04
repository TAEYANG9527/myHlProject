package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: LoadConversationInfo.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-8 下午4:56:37 <br>
 * @author wangning
 */
public class LoadConversationInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int state;// int 请求是否成功结果 说明：1 请求成功
	private long lastLoadTime;// long 最后的加载时间，服务器端返回此字段后，客户端需保存起来下次调用时使用
	private ConversationInfo conversationInfo;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getLastLoadTime() {
		return lastLoadTime;
	}

	public void setLastLoadTime(long lastLoadTime) {
		this.lastLoadTime = lastLoadTime;
	}

	public ConversationInfo getConversationInfo() {
		return conversationInfo;
	}

	public void setConversationInfo(ConversationInfo conversationInfo) {
		this.conversationInfo = conversationInfo;
	}

	public class ConversationInfo implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int newFriendCount;// int 说明：新的朋友数
		private int toutiaoUnReadCount;// int 说明：未读行业头条数
		private String toutiaoContent;// String 说明：最后一条行业头条内容
		private long toutiaoUpdatedDate;// long 说明：最后一条行业头条时间
		private int messageUnReadCount;// int 说明：未读站内信数
		private String messageContent;// String 说明：最后一条站内信内容
		private long messageUpdatedDate;// long 说明：最后一条站内信时间
		private int systemMessageUnReadCount;// int 说明：未读系统消息数
		private String systemMessageContent;// String 说明：最后一条系统消息内容
		private long systemMessageUpdatedDate;// long 说明：最后一条系统消息时间 
		private String messageAvatar;//说明：最后一条站内信的参与者头像，若参与者头像为默认头像，则messageAvatar为空，请使用默认的站内信图标

		public int getNewFriendCount() {
			return newFriendCount;
		}

		public void setNewFriendCount(int newFriendCount) {
			this.newFriendCount = newFriendCount;
		}

		public int getToutiaoUnReadCount() {
			return toutiaoUnReadCount;
		}

		public void setToutiaoUnReadCount(int toutiaoUnReadCount) {
			this.toutiaoUnReadCount = toutiaoUnReadCount;
		}

		public String getToutiaoContent() {
			return toutiaoContent;
		}

		public void setToutiaoContent(String toutiaoContent) {
			this.toutiaoContent = toutiaoContent;
		}

		public long getToutiaoUpdatedDate() {
			return toutiaoUpdatedDate;
		}

		public void setToutiaoUpdatedDate(long toutiaoUpdatedDate) {
			this.toutiaoUpdatedDate = toutiaoUpdatedDate;
		}

		public int getMessageUnReadCount() {
			return messageUnReadCount;
		}

		public void setMessageUnReadCount(int messageUnReadCount) {
			this.messageUnReadCount = messageUnReadCount;
		}

		public String getMessageContent() {
			return messageContent;
		}

		public void setMessageContent(String messageContent) {
			this.messageContent = messageContent;
		}

		public long getMessageUpdatedDate() {
			return messageUpdatedDate;
		}

		public void setMessageUpdatedDate(long messageUpdatedDate) {
			this.messageUpdatedDate = messageUpdatedDate;
		}

		public int getSystemMessageUnReadCount() {
			return systemMessageUnReadCount;
		}

		public void setSystemMessageUnReadCount(int systemMessageUnReadCount) {
			this.systemMessageUnReadCount = systemMessageUnReadCount;
		}

		public String getSystemMessageContent() {
			return systemMessageContent;
		}

		public void setSystemMessageContent(String systemMessageContent) {
			this.systemMessageContent = systemMessageContent;
		}

		public long getSystemMessageUpdatedDate() {
			return systemMessageUpdatedDate;
		}

		public void setSystemMessageUpdatedDate(long systemMessageUpdatedDate) {
			this.systemMessageUpdatedDate = systemMessageUpdatedDate;
		}

		public String getMessageAvatar() {
			return messageAvatar;
		}

		public void setMessageAvatar(String messageAvatar) {
			this.messageAvatar = messageAvatar;
		}

	}
}
