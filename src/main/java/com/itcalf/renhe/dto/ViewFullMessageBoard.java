package com.itcalf.renhe.dto;

import com.itcalf.renhe.dto.MessageBoards.AtMemmber;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;

import java.io.Serializable;

/**
 * Title: ViewFullMessageBoard.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-28 下午7:22:37 <br>
 * @author wangning
 */
public class ViewFullMessageBoard implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int state;
	private int id;
	private String objectId;
	private int replyNum;
	private ReplyList[] replyList;
	private boolean liked;//是否已经点赞了此条留言
	private int likedNum;//说明：点赞的数量
	private LikedList[] likedList;
	private SenderInfo senderInfo;
	private long createdDateSeconds;
	private String content;
	private ForwardMessageBoardInfo forwardMessageBoardInfo;
	private AtMemmber[] atMembers;
	private PicList[] picList;
	private int type;

	public static class ReplyList implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int id;// 留言在mysql中的id
		private String objectId;// 留言的objectId
		private String senderSid; // 留言者的sid
		private String senderName; // 留言者姓名
		private String content; // 留言内容
		private String reSenderSid;// 被回复者的sid
		private String reSenderMemberName;// 被回复者姓名
		private String senderUserface;// 被回复者姓名
		private long createdDateSeconds;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getObjectId() {
			return objectId;
		}

		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

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

		public String getSenderUserface() {
			return senderUserface;
		}

		public void setSenderUserface(String senderUserface) {
			this.senderUserface = senderUserface;
		}

		public long getCreatedDateSeconds() {
			return createdDateSeconds;
		}

		public void setCreatedDateSeconds(long createdDateSeconds) {
			this.createdDateSeconds = createdDateSeconds;
		}

	}

	public static class LikedList implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String sid;// 点赞者的sid
		private String userface;// 点赞者的name

		public String getSid() {
			return sid;
		}

		public void setSid(String sid) {
			this.sid = sid;
		}

		public String getUserface() {
			return userface;
		}

		public void setUserface(String userface) {
			this.userface = userface;
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public int getReplyNum() {
		return replyNum;
	}

	public void setReplyNum(int replyNum) {
		this.replyNum = replyNum;
	}

	public ReplyList[] getReplyList() {
		return replyList;
	}

	public void setReplyList(ReplyList[] replyList) {
		this.replyList = replyList;
	}

	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public int getLikedNum() {
		return likedNum;
	}

	public void setLikedNum(int likedNum) {
		this.likedNum = likedNum;
	}

	public LikedList[] getLikedList() {
		return likedList;
	}

	public void setLikedList(LikedList[] likedList) {
		this.likedList = likedList;
	}

	public SenderInfo getSenderInfo() {
		return senderInfo;
	}

	public void setSenderInfo(SenderInfo senderInfo) {
		this.senderInfo = senderInfo;
	}

	public long getCreatedDateSeconds() {
		return createdDateSeconds;
	}

	public void setCreatedDateSeconds(long createdDateSeconds) {
		this.createdDateSeconds = createdDateSeconds;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ForwardMessageBoardInfo getForwardMessageBoardInfo() {
		return forwardMessageBoardInfo;
	}

	public void setForwardMessageBoardInfo(ForwardMessageBoardInfo forwardMessageBoardInfo) {
		this.forwardMessageBoardInfo = forwardMessageBoardInfo;
	}

	public AtMemmber[] getAtMembers() {
		return atMembers;
	}

	public void setAtMembers(AtMemmber[] atMembers) {
		this.atMembers = atMembers;
	}

	public PicList[] getPicList() {
		return picList;
	}

	public void setPicList(PicList[] picList) {
		this.picList = picList;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
