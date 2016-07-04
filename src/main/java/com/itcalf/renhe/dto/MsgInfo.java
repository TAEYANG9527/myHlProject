package com.itcalf.renhe.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class MsgInfo implements Serializable {

	private static final long serialVersionUID = -2921013125545048211L;
	private int state;
	private MessageInfo messageInfo;
	private UserInfo userInfo;
	//	private int totalMessageCount;
	//	private int currentMessageIndexNum;
	private boolean showReply;
	//	private boolean showPreMessage;
	//	private boolean showNextMessage;
	//	private String preMessageSId;
	//	private String preMessageObjectId;
	//	private String nextMessageSId;
	//	private String nextMessageObjectId;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public MessageInfo getMessageInfo() {
		return messageInfo;
	}

	public void setMessageInfo(MessageInfo messageInfo) {
		this.messageInfo = messageInfo;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	//	public int getTotalMessageCount() {
	//		return totalMessageCount;
	//	}
	//
	//	public void setTotalMessageCount(int totalMessageCount) {
	//		this.totalMessageCount = totalMessageCount;
	//	}
	//
	//	public int getCurrentMessageIndexNum() {
	//		return currentMessageIndexNum;
	//	}
	//
	//	public void setCurrentMessageIndexNum(int currentMessageIndexNum) {
	//		this.currentMessageIndexNum = currentMessageIndexNum;
	//	}

	public boolean isShowReply() {
		return showReply;
	}

	public void setShowReply(boolean showReply) {
		this.showReply = showReply;
	}

	//	public boolean isShowPreMessage() {
	//		return showPreMessage;
	//	}
	//
	//	public void setShowPreMessage(boolean showPreMessage) {
	//		this.showPreMessage = showPreMessage;
	//	}
	//
	//	public boolean isShowNextMessage() {
	//		return showNextMessage;
	//	}
	//
	//	public void setShowNextMessage(boolean showNextMessage) {
	//		this.showNextMessage = showNextMessage;
	//	}
	//
	//	public String getPreMessageSId() {
	//		return preMessageSId;
	//	}
	//
	//	public void setPreMessageSId(String preMessageSId) {
	//		this.preMessageSId = preMessageSId;
	//	}
	//
	//	public String getPreMessageObjectId() {
	//		return preMessageObjectId;
	//	}
	//
	//	public void setPreMessageObjectId(String preMessageObjectId) {
	//		this.preMessageObjectId = preMessageObjectId;
	//	}
	//
	//	public String getNextMessageSId() {
	//		return nextMessageSId;
	//	}
	//
	//	public void setNextMessageSId(String nextMessageSId) {
	//		this.nextMessageSId = nextMessageSId;
	//	}
	//
	//	public String getNextMessageObjectId() {
	//		return nextMessageObjectId;
	//	}
	//
	//	public void setNextMessageObjectId(String nextMessageObjectId) {
	//		this.nextMessageObjectId = nextMessageObjectId;
	//	}

	public static class MessageInfo implements Serializable {
		private static final long serialVersionUID = 2081861580218788774L;
		public String msid;
		public String messageObjectId;
		public String subject;
		public String content;
		public String createdDate;

		private boolean isInviteType;// 是否是邀请类型的站内信 如果是，则需要特殊处理，如果不是则直接显示站内信内容
		private int inviteType;// int 添加好友类型： 1:直接添加好友
								// 2：通过引荐添加好友，此时由被引荐人决定是否同意引荐 3:
								// 通过引荐添加好友，引荐传递后，被添加好友的人决定是否同意接受加好友请求
		private InviteXMessageInfo inviteXMessageInfo;// （邀请站内信信息

		public static class InviteXMessageInfo implements Serializable {
			private static final long serialVersionUID = 2834015509224966653L;
			private int inviteState;// 邀请状态 0 未处理，显示，同意、拒绝（只在引荐的时候才显示）、暂不处理按钮； 1
									// 已同意(显示已同意状态) 2 已拒绝（显示已拒绝状态）
			private String inviteId;// 邀请的id,用于同意或拒绝好友邀请
			private String fromMemberSId;// String 邀请来源的会员sid
			private String fromMemberName;// String 邀请来源的会员名字
			private String fromMemberTitle;// String 邀请来源的会员职位(2011-12-6修改)
			private String fromMemberCompany;// String 邀请来源的会员公司(2011-12-6修改)
			private String fromContent;// String 邀请留言的内容
			private String purpose;// String 邀请目的
			private String toMemberSId;// String 邀请接受会员的sid
			private String toMemberName;// String 邀请接受会员的名字
			private String toMemberUserface;// String 邀请接受会员的会员头像
			private String inviteContent;// String 引荐时说明性文字
			private String recommendFromContent;// String 引荐人留言的内容

			@Override
			public String toString() {
				return new ToStringBuilder(this).append("inviteState", inviteState).append("inviteId", inviteId)
						.append("fromMemberSId", fromMemberSId).append("fromMemberName", fromMemberName)
						.append("fromMemberTitle", fromMemberTitle).append("fromMemberCompany", fromMemberCompany)
						.append("fromContent", fromContent).append("purpose", purpose).append("toMemberSId", toMemberSId)
						.append("toMemberName", toMemberName).append("toMemberUserface", toMemberUserface)
						.append("inviteContent", inviteContent).append("recommendFromContent", recommendFromContent).toString();
			}

			public int getInviteState() {
				return inviteState;
			}

			public void setInviteState(int inviteState) {
				this.inviteState = inviteState;
			}

			public String getInviteId() {
				return inviteId;
			}

			public void setInviteId(String inviteId) {
				this.inviteId = inviteId;
			}

			public String getFromMemberSId() {
				return fromMemberSId;
			}

			public void setFromMemberSId(String fromMemberSId) {
				this.fromMemberSId = fromMemberSId;
			}

			public String getFromMemberName() {
				return fromMemberName;
			}

			public void setFromMemberName(String fromMemberName) {
				this.fromMemberName = fromMemberName;
			}

			public String getFromMemberTitle() {
				return fromMemberTitle;
			}

			public void setFromMemberTitle(String fromMemberTitle) {
				this.fromMemberTitle = fromMemberTitle;
			}

			public String getFromMemberCompany() {
				return fromMemberCompany;
			}

			public void setFromMemberCompany(String fromMemberCompany) {
				this.fromMemberCompany = fromMemberCompany;
			}

			public String getFromContent() {
				return fromContent;
			}

			public void setFromContent(String fromContent) {
				this.fromContent = fromContent;
			}

			public String getPurpose() {
				return purpose;
			}

			public void setPurpose(String purpose) {
				this.purpose = purpose;
			}

			public String getToMemberSId() {
				return toMemberSId;
			}

			public void setToMemberSId(String toMemberSId) {
				this.toMemberSId = toMemberSId;
			}

			public String getToMemberName() {
				return toMemberName;
			}

			public void setToMemberName(String toMemberName) {
				this.toMemberName = toMemberName;
			}

			public String getToMemberUserface() {
				return toMemberUserface;
			}

			public void setToMemberUserface(String toMemberUserface) {
				this.toMemberUserface = toMemberUserface;
			}

			public String getInviteContent() {
				return inviteContent;
			}

			public void setInviteContent(String inviteContent) {
				this.inviteContent = inviteContent;
			}

			public String getRecommendFromContent() {
				return recommendFromContent;
			}

			public void setRecommendFromContent(String recommendFromContent) {
				this.recommendFromContent = recommendFromContent;
			}

		}

		public boolean isInviteType() {
			return isInviteType;
		}

		public void setInviteType(boolean isInviteType) {
			this.isInviteType = isInviteType;
		}

		public int getInviteType() {
			return inviteType;
		}

		public void setInviteType(int inviteType) {
			this.inviteType = inviteType;
		}

		public InviteXMessageInfo getInviteXMessageInfo() {
			return inviteXMessageInfo;
		}

		public void setInviteXMessageInfo(InviteXMessageInfo inviteXMessageInfo) {
			this.inviteXMessageInfo = inviteXMessageInfo;
		}

		public String getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(String createdDate) {
			this.createdDate = createdDate;
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

		@Override
		public String toString() {
			return "MessageInfo [msid=" + msid + ", messageObjectId=" + messageObjectId + ", subject=" + subject + ", content="
					+ content + ", createdDate=" + createdDate + ", isInviteType=" + isInviteType + ", inviteType=" + inviteType
					+ ", inviteXMessageInfo=" + inviteXMessageInfo + "]";
		}
	}

	public static class UserInfo implements Serializable {
		private static final long serialVersionUID = 3051424846158109995L;
		private String sid;
		private String name;
		private String userface;
		private String company;
		private boolean showLink;
		private int accountType;
		private boolean isRealname;
		private String title;
		private String industry;
		private String location;

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

		public String getCompany() {
			return company;
		}

		public void setCompany(String company) {
			this.company = company;
		}

		public boolean isShowLink() {
			return showLink;
		}

		public void setShowLink(boolean showLink) {
			this.showLink = showLink;
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

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getIndustry() {
			return industry;
		}

		public void setIndustry(String industry) {
			this.industry = industry;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

	}

	@Override
	public String toString() {
		return "MsgInfo [state=" + state + ", messageInfo=" + messageInfo + ", userInfo=" + userInfo + ", totalMessageCount="
				+ ", currentMessageIndexNum=" + ", showReply=" + showReply + ", showPreMessage=" + ", showNextMessage="
				+ ", preMessageSId=" + ", preMessageObjectId=" + ", nextMessageSId=" + ", nextMessageObjectId=" + "]";
	}
}
