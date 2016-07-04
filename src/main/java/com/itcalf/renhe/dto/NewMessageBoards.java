package com.itcalf.renhe.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Arrays;

public class NewMessageBoards implements Serializable {
	private static final long serialVersionUID = 1L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private NewMessageBoardList[] notifyList; // 留言列表

	public NewMessageBoardList[] getNewMessageBoardList() {
		return notifyList;
	}

	public void setNewMessageBoardList(NewMessageBoardList[] newMessageBoardList) {
		this.notifyList = newMessageBoardList;
	}

	/**
	 * 
	 * 客厅留言列表 被转发的数量现在人和网暂时没这个功能
	 * 
	 */
	public static class NewMessageBoardList implements Serializable {
		private static final long serialVersionUID = 1L;
		private String objectId;

		/**
		 * 提醒本身的objectId
		 * */
		private String notifyObjectId;

		/**
		 * 说明：人脉圈动态类型 1：代表 "message_board"，人脉圈留言类型 2：代表 "member_update_contact"
		 * 更新了联系方式动态 3：代表 "member_update_user_face" 更新了头像动态 4：代表
		 * "profile_add_position" 新增了工作经历动态 5：代表 "profile_update_position"
		 * 更新了工作经历动态 6：代表 "profile_add_education" 新增了教育经历动态
		 * 7：代表"profile_update_education" 更新了教育经历动态
		 * 8：代表"profile_update_cover_image" 更新了档案封面动态
		 * */
		private int noticeType;

		/**
		 * 说明：提醒类型 当 noticeType = 1时，type有如下值 1 回复了您评论过的客厅留言 2 回复了您的客厅留言 3
		 * 回复了你在客厅发布的评论 4 转发你的客厅留言 5 在客厅中@提到了您 6 赞了您的留言 当 noticeType =
		 * 2,3,4,5,6,7,8时 type 有如下值 1 评论了您的人脉圈动态 2 回复了您的人脉圈评论 4 赞了你的人脉圈动态
		 * */
		private int type;

		/**
		 * 提醒发送者的信息
		 * */
		private ReplyInfo notifySenderInfo;

		/**
		 * 人脉圈动态
		 * */
		private SourceInfo messageboardContentInfo;

		public String getObjectId() {
			return objectId;
		}

		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		public String getNotifyObjectId() {
			return notifyObjectId;
		}

		public void setNotifyObjectId(String notifyObjectId) {
			this.notifyObjectId = notifyObjectId;
		}

		public int getNoticeType() {
			return noticeType;
		}

		public void setNoticeType(int noticeType) {
			this.noticeType = noticeType;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public ReplyInfo getReplyInfo() {
			return notifySenderInfo;
		}

		public void setReplyInfo(ReplyInfo replyInfo) {
			this.notifySenderInfo = replyInfo;
		}

		public SourceInfo getSourceInfo() {
			return messageboardContentInfo;
		}

		public void setSourceInfo(SourceInfo sourceInfo) {
			this.messageboardContentInfo = sourceInfo;
		}

		public static class ReplyInfo {
			private String sid;
			private String name;
			private String userface;
			private String replyContent;
			private String fromSource;
			private String createdDate;
			private int accountType;// 账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
			private boolean isRealName;// 是否是实名认证的会员

			@Override
			public String toString() {
				return new ToStringBuilder(this).append("sid", sid).append("name", name).append("userface", userface)
						.append("replyContent", replyContent).append("fromSource", fromSource).append("createdDate", createdDate)
						.toString();
			}

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

			public String getReplyContent() {
				return replyContent;
			}

			public void setReplyContent(String replyContent) {
				this.replyContent = replyContent;
			}

			public String getFromSource() {
				return fromSource;
			}

			public void setFromSource(String fromSource) {
				this.fromSource = fromSource;
			}

			public String getCreatedDate() {
				return createdDate;
			}

			public void setCreatedDate(String createdDate) {
				this.createdDate = createdDate;
			}

			public int getAccountType() {
				return accountType;
			}

			public void setAccountType(int accountType) {
				this.accountType = accountType;
			}

			public boolean isRealName() {
				return isRealName;
			}

			public void setRealName(boolean isRealName) {
				this.isRealName = isRealName;
			}

		}

		public static class SourceInfo {
			private String objectId;
			private String content;// 原留言内容

			@Override
			public String toString() {
				return new ToStringBuilder(this).append("objectId", objectId).append("content", content).toString();
			}

			public String getObjectId() {
				return objectId;
			}

			public void setObjectId(String objectId) {
				this.objectId = objectId;
			}

			public String getContent() {
				return content;
			}

			public void setContent(String content) {
				this.content = content;
			}

		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "NewMessageBoards [state=" + state + ", NewMessageBoardList=" + Arrays.toString(notifyList);
	}

}
