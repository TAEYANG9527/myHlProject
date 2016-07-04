package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: IMInnerMsgChatListOperation.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-10 下午2:36:35 <br>
 * @author wangning
 */
public class IMInnerMsgChatListOperation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int state;// int 请求是否成功结果 说明：1 请求成功
	private UserInnerMsgChat[] messageList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public UserInnerMsgChat[] getUserConversationList() {
		return messageList;
	}

	public void setUserConversationList(UserInnerMsgChat[] userConversationList) {
		this.messageList = userConversationList;
	}

	public static class UserInnerMsgChat implements Serializable {
		public static final int INNER_MSG_SENDING = 0;
		public static final int INNER_MSG_OFFLINE = -1;
		public static final int INNER_MSG_SENDSUCCESS = 1;
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int id;// int 说明：站内信id
		private String senderSId;// int 说明：站内信发送者id
		private String senderName;// String 说明：站内信发送者名
		private String senderUserfaceUrl;// String 说明：站内信发送者头像
		private String receiverSId;// int 说明：站内信接收者id
		private String receiverName;// String 说明：站内信接收者名
		private String receiverUserfaceUrl;// String 说明：站内信接收者头像
		private String content;// String 说明：站内信内容
		private boolean readState;// boolean 说明：是否是已读内容
		private long createdDate;// long 说明：站内信发送时间
		private int status;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getSender() {
			return senderSId;
		}

		public void setSender(String sender) {
			this.senderSId = sender;
		}

		public String getSenderName() {
			return senderName;
		}

		public void setSenderName(String senderName) {
			this.senderName = senderName;
		}

		public String getSenderUserfaceUrl() {
			return senderUserfaceUrl;
		}

		public void setSenderUserfaceUrl(String senderUserfaceUrl) {
			this.senderUserfaceUrl = senderUserfaceUrl;
		}

		public String getReceiver() {
			return receiverSId;
		}

		public void setReceiver(String receiver) {
			this.receiverSId = receiver;
		}

		public String getReceiverName() {
			return receiverName;
		}

		public void setReceiverName(String receiverName) {
			this.receiverName = receiverName;
		}

		public String getReceiverUserfaceUrl() {
			return receiverUserfaceUrl;
		}

		public void setReceiverUserfaceUrl(String receiverUserfaceUrl) {
			this.receiverUserfaceUrl = receiverUserfaceUrl;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public boolean isReadState() {
			return readState;
		}

		public void setReadState(boolean readState) {
			this.readState = readState;
		}

		public long getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(long createdDate) {
			this.createdDate = createdDate;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

	}
}
