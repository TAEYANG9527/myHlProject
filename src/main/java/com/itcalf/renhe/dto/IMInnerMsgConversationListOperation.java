package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: ConversationSystemMsgOperation.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-12-10 下午2:36:35 <br>
 * @author wangning
 */
public class IMInnerMsgConversationListOperation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int state;// int 请求是否成功结果 说明：1 请求成功
	private UserInnerMsgConversation[] userConversationList;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public UserInnerMsgConversation[] getUserConversationList() {
		return userConversationList;
	}

	public void setUserConversationList(UserInnerMsgConversation[] userConversationList) {
		this.userConversationList = userConversationList;
	}

	public static class UserInnerMsgConversation implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int id;// int 说明：站内信会话id
		private String participantsMemberSId;// int 说明：参与者id
		private String name;// String 说明：站内信会话名称
		private String userfaceUrl;// String 说明：站内信会话图片
		private int unReadCount;// int 说明：站内信会话未读数量
		private String lastUpdatedContent;// String 说明：站内信会话最后更新内容
		private long lastUpdatedDate;// long 说明：站内信会话最后更新时间

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getParticipantsMemberId() {
			return participantsMemberSId;
		}

		public void setParticipantsMemberId(String participantsMemberId) {
			this.participantsMemberSId = participantsMemberId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUserfaceUrl() {
			return userfaceUrl;
		}

		public void setUserfaceUrl(String userfaceUrl) {
			this.userfaceUrl = userfaceUrl;
		}

		public int getUnReadCount() {
			return unReadCount;
		}

		public void setUnReadCount(int unReadCount) {
			this.unReadCount = unReadCount;
		}

		public String getLastUpdatedContent() {
			return lastUpdatedContent;
		}

		public void setLastUpdatedContent(String lastUpdatedContent) {
			this.lastUpdatedContent = lastUpdatedContent;
		}

		public long getLastUpdatedDate() {
			return lastUpdatedDate;
		}

		public void setLastUpdatedDate(long lastUpdatedDate) {
			this.lastUpdatedDate = lastUpdatedDate;
		}

	}
}
