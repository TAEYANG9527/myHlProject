package com.itcalf.renhe.dto;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 
 * 显示某条客厅留言的全部回复内容
 * 
 */
public class MsgComments implements Serializable {

	private static final long serialVersionUID = 7615822594754906358L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误
	private CommentList[] commentList; // 留言的列表
	private int commentNum;

	/**
	 * 
	 * 留言的列表
	 * 
	 */
	public static class CommentList implements Serializable {

		private static final long serialVersionUID = -5288665871883713342L;
		private int id;// 留言在mysql中的id
		private String objectId;// 留言的objectId
		private String senderSid; // 留言者的sid
		private String senderName; // 留言者姓名
		private String content; // 留言内容
		private String senderUserface;
		private String reSenderSid; // 留言者的sid
		private String reSenderMemberName; // 留言者姓名
		private long createdDateSeconds;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员

		public String getSenderSid() {
			return senderSid;
		}

		public void setSenderSid(String senderSid) {
			this.senderSid = senderSid;
		}

		public String getSenderName() {
			return senderName;
		}

		public void setSenderName(String senderName) {
			this.senderName = senderName;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getObjecteId() {
			return objectId;
		}

		public void setObjecteId(String objecteId) {
			this.objectId = objecteId;
		}

		public String getSenderUserFace() {
			return senderUserface;
		}

		public void setSenderUserFace(String senderUserFace) {
			this.senderUserface = senderUserFace;
		}

		public String getObjectId() {
			return objectId;
		}

		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		public String getReSenderSid() {
			return reSenderSid;
		}

		public void setReSenderSid(String reSenderSid) {
			this.reSenderSid = reSenderSid;
		}

		public String getReSenderMemberName() {
			return reSenderMemberName;
		}

		public void setReSenderMemberName(String reSenderMemberName) {
			this.reSenderMemberName = reSenderMemberName;
		}

		public long getCreatedDateSeconds() {
			return createdDateSeconds;
		}

		public void setCreatedDateSeconds(long createdDateSeconds) {
			this.createdDateSeconds = createdDateSeconds;
		}

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public CommentList[] getCommentList() {
		return commentList;
	}

	public void setCommentList(CommentList[] commentList) {
		this.commentList = commentList;
	}

	@Override
	public String toString() {
		return "Comments [commentList=" + Arrays.toString(commentList) + ", state=" + state + "]";
	}

	public int getCommentNum() {
		return commentNum;
	}

	public void setCommentNum(int commentNum) {
		this.commentNum = commentNum;
	}

}
