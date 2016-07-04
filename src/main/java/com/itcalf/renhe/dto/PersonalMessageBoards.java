package com.itcalf.renhe.dto;

import com.itcalf.renhe.dto.MessageBoards.AtMemmber;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;

import java.io.Serializable;

/**
 * 
 * 1,查看自己客厅留言接口(自己发布的客厅留言及自己关注的客厅留言) 2,查看朋友客厅的留言接口 3,查看同城的留言接口 4,查看同行的留言接口
 * 5,最受关注的留言接口
 */
public class PersonalMessageBoards implements Serializable {

	private static final long serialVersionUID = -6501243574466513990L;
	private int state; // 请求是否成功结果 说明：1 请求成功；-3：type必须为renew、new、more；-4：type类型为new，而maxCreatedDate和maxLastUpdaatedDate未正确设置值；-5：type类型为more，而minCreatedDate和minLastUpdaatedDate未指定
	private NewNoticeList[] newMessageBoardList; // 新增的人脉圈列表
	private long maxCreatedDate; //long 说明：最大的创建时间，type为renew和new时才会返回
	private long minCreatedDate; //long 说明：最小的创建时间，type为renew和more时才会返回
	private long maxLastUpdatedDate; //long 说明：最大的修改时间，type为renew和new时才会返回
	/**
	 * 	type String 人脉圈类型:
		    1、"message_board" 客厅类型
		    2、"member_update_contact" 更新了联系方式
		    3、"member_update_user_face" 更新了头像
		    4、"profile_add_position" 新增了工作经历
		    5、"profile_update_position" 更新了工作经历
		    6、"profile_add_education" 新增了教育经历
		    7、"profile_update_education" 更新了教育经历
	 */

	public static final int MESSAGE_TYPE_MESSAGE_BOARD = 1;
	public static final int MESSAGE_TYPE_MEMBER_UPDATE_CONTACT = 2;
	public static final int MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE = 3;
	public static final int MESSAGE_TYPE_PROFILE_ADD_POSITION = 4;
	public static final int MESSAGE_TYPE_PROFILE_UPDATE_POSITION = 5;
	public static final int MESSAGE_TYPE_PROFILE_ADD_EDUCATION = 6;
	public static final int MESSAGE_TYPE_PROFILE_UPDATE_EDUCATION = 7;

	public static final int MESSAGE_TYPE_ADD_NEWMSG = 8;

	/**
	 * 人脉圈类型——人脉圈、发现
	 * @author Renhe
	 *
	 */
	public static final int MESSAGE_TYPE_RENMAIQUAN = 1;
	public static final int MESSAGE_TYPE_FAXIAN = 2;

	public static class NewNoticeList implements Serializable {
		/**
		* 
		*/
		private static final long serialVersionUID = 1L;
		private SenderInfo senderInfo;
		private int id;// int 说明：客厅的id
		private String objectId;// String 说明：客厅的objectId
		private String content;// String 说明：客厅的内容
		private long createdDateSeconds;
		private int replyNum;//说明：回复数量
		private boolean liked;//是否已经点赞了此条留言
		private int likedNum;//说明：点赞的数量
		private ForwardMessageBoardInfo forwardMessageBoardInfo;
		private AtMemmber[] atMembers;//留言内容中@信息 
		private PicList[] picList;

		public SenderInfo getSenderInfo() {
			return senderInfo;
		}

		public void setSenderInfo(SenderInfo senderInfo) {
			this.senderInfo = senderInfo;
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

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public long getCreatedDateSeconds() {
			return createdDateSeconds;
		}

		public void setCreatedDateSeconds(long createdDateSeconds) {
			this.createdDateSeconds = createdDateSeconds;
		}

		public int getReplyNum() {
			return replyNum;
		}

		public void setReplyNum(int replyNum) {
			this.replyNum = replyNum;
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

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getMaxCreatedDate() {
		return maxCreatedDate;
	}

	public void setMaxCreatedDate(long maxCreatedDate) {
		this.maxCreatedDate = maxCreatedDate;
	}

	public long getMinCreatedDate() {
		return minCreatedDate;
	}

	public void setMinCreatedDate(long minCreatedDate) {
		this.minCreatedDate = minCreatedDate;
	}

	public long getMaxLastUpdatedDate() {
		return maxLastUpdatedDate;
	}

	public void setMaxLastUpdatedDate(long maxLastUpdatedDate) {
		this.maxLastUpdatedDate = maxLastUpdatedDate;
	}

	public NewNoticeList[] getNewMessageBoardList() {
		return newMessageBoardList;
	}

	public void setNewMessageBoardList(NewNoticeList[] newMessageBoardList) {
		this.newMessageBoardList = newMessageBoardList;
	}

}
