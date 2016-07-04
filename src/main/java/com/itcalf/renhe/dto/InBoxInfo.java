package com.itcalf.renhe.dto;

import java.io.Serializable;
import java.util.Arrays;

public class InBoxInfo implements Serializable {
	private static final long serialVersionUID = 2840441674391016992L;
	private int state;
	private MessageList[] messageList;
	private String minObjectId;
	private String maxObjectId;
	private boolean aboveMaxCount;

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

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
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
		private SenderInfo senderInfo;
		private long createdTime;//毫秒
		private int read; //0代表未读，1代表已读。

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

		public SenderInfo getSenderInfo() {
			return senderInfo;
		}

		public void setSenderInfo(SenderInfo senderInfo) {
			this.senderInfo = senderInfo;
		}

		public static class SenderInfo implements Serializable {
			private static final long serialVersionUID = -8808186421619704054L;
			private String sid;
			private String name;
			private String userface;
			private int accountType;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
			private boolean isRealname;//是否是实名认证的会员

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

			@Override
			public String toString() {
				return "SenderInfo [sid=" + sid + ", name=" + name + "userface=" + userface + "]";
			}
		}

		public int getRead() {
			return read;
		}

		public void setRead(int read) {
			this.read = read;
		}

		public long getCreatedTime() {
			return createdTime;
		}

		public void setCreatedTime(long createdTime) {
			this.createdTime = createdTime;
		}

		@Override
		public String toString() {
			return "MessageList [msid=" + msid + ", messageObjectId=" + messageObjectId + " ,subject=" + subject + " ,content="
					+ content + " ,createdDate=" + createdDate + " ,senderInfo=" + senderInfo + " ,read=" + read + "]";
		}
	}

	@Override
	public String toString() {
		return "InBoxInfo [state=" + state + ", messageList=" + Arrays.toString(messageList) + " ,minObjectId=" + minObjectId
				+ " , maxObjectId=" + maxObjectId + " ]";
	}
}
