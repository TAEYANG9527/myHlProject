package com.itcalf.renhe.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class SendBoxInfo implements Serializable {
	private static final long serialVersionUID = 2840441674391016992L;
	private int state;
	private MessageList[] messageList;
	private String minObjectId;
	private String maxObjectId;
	private boolean aboveMaxCount;

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("state", state).append("messageList", messageList)
				.append("minObjectId", minObjectId).append("maxObjectId", maxObjectId).append("aboveMaxCount", aboveMaxCount)
				.toString();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMinObjectId() {
		return minObjectId;
	}

	public void setMinObjectId(String minObjectId) {
		this.minObjectId = minObjectId;
	}

	public String getMaxObjectId() {
		return maxObjectId;
	}

	public void setMaxObjectId(String maxObjectId) {
		this.maxObjectId = maxObjectId;
	}

	public MessageList[] getMessageList() {
		return messageList;
	}

	public void setMessageList(MessageList[] messageList) {
		this.messageList = messageList;
	}

	public boolean isAboveMaxCount() {
		return aboveMaxCount;
	}

	public void setAboveMaxCount(boolean aboveMaxCount) {
		this.aboveMaxCount = aboveMaxCount;
	}

	public static class MessageList implements Serializable {

		private static final long serialVersionUID = 3778067319463893399L;
		private String msid;
		private String messageObjectId;
		private String subject;
		private String content;
		private String createdDate;
		private long createdTime;//毫秒
		private ReceiverInfo receiverInfo;

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("msid", msid).append("messageObjectId", messageObjectId)
					.append("subject", subject).append("content", content).append("createdDate", createdDate)
					.append("receiverInfo", receiverInfo).toString();
		}

		public String getMsid() {
			return msid;
		}

		public void setMsid(String msid) {
			this.msid = msid;
		}

		public String getMessageObjectId() {
			return messageObjectId;
		}

		public void setMessageObjectId(String messageObjectId) {
			this.messageObjectId = messageObjectId;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(String createdDate) {
			this.createdDate = createdDate;
		}

		public ReceiverInfo getReceiverInfo() {
			return receiverInfo;
		}

		public void setReceiverInfo(ReceiverInfo receiverInfo) {
			this.receiverInfo = receiverInfo;
		}

		public static class ReceiverInfo implements Serializable {

			private static final long serialVersionUID = -8808186421619704054L;
			private String sid;
			private String name;
			private String userface;
			private int accountType;
			private boolean isRealname;

			public String getSid() {
				return sid;
			}

			public void setSid(String sid) {
				this.sid = sid;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getUserface() {
				return userface;
			}

			public void setUserface(String userface) {
				this.userface = userface;
			}

			public int getAccountType() {
				return accountType;
			}

			public void setAccountType(int accountType) {
				this.accountType = accountType;
			}

			public boolean isRealname() {
				return isRealname;
			}

			public void setRealname(boolean isRealname) {
				this.isRealname = isRealname;
			}

		}

		public long getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(long createdTime) {
			this.createdTime = createdTime;
		}

	}

}
