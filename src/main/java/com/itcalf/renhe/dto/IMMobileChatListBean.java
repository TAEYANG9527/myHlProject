package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * description :手机通讯录IM
 * Created by Chans Renhenet
 * 2015/8/28
 */
public class IMMobileChatListBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state;// int 请求是否成功结果 说明：1 请求成功
	private MobileChat[] chatList;

	public IMMobileChatListBean() {
	}

	public MobileChat[] getChatList() {
		return chatList;
	}

	public void setChatList(MobileChat[] chatList) {
		this.chatList = chatList;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public static class MobileChat implements Serializable {

		public static final int MOBILE_MSG_SENDING = 0;
		public static final int MOBILE_MSG_OFFLINE = -1;
		public static final int MOBILE_MSG_SEND_SUCCESS = 1;
		private int state;

		private String content;
		private long createdDate;

		public MobileChat() {
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public long getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(long createdDate) {
			this.createdDate = createdDate;
		}
	}
}
