package com.itcalf.renhe.dto;

import com.itcalf.renhe.dto.MessageBoards.AtMemmber;

import java.io.Serializable;

/**
 * 
 * 显示某条客厅留言的详细内容接口
 * 
 */
public class MessageBoardDetail implements Serializable {

	private static final long serialVersionUID = -7833148939621459821L;
	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private SenderInfo senderInfo; // 发布者信息
	private MessageBoardInfo messageBoardInfo; // 留言信息

	public static class SenderInfo implements Serializable {

		private static final long serialVersionUID = -4010739126010801059L;
		private String sid; // 会员的sid
		private String name; // 发布者的姓名
		private String userface; // 发布者的头像
		private String title; // 发布者的职务
		private String company; // 发布者的公司名
		private String industry; // 发布者的公司行业
		private String location; // 发布者的公司地址
		private boolean isSelf; // 发布者是否是自己
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

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getCompany() {
			return company;
		}

		public void setCompany(String company) {
			this.company = company;
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

		public boolean isSelf() {
			return isSelf;
		}

		public void setSelf(boolean isSelf) {
			this.isSelf = isSelf;
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
			return "SenderInfo [company=" + company + ", industry=" + industry + ", isSelf=" + isSelf + ", location=" + location
					+ ", name=" + name + ", sid=" + sid + ", title=" + title + ", userface=" + userface + "]";
		}

	}

	public static class MessageBoardInfo implements Serializable {

		private static final long serialVersionUID = -7185346370151578317L;
		private String id;// 留言的id
		private String objectId;// 留言的ObjectId
		private String content; // 留言正文
		private ForwardMessageBoardInfo forwardMessageBoardInfo; //
		private String fromSource; // 留言来自于哪里 来自网页客户端或来自Android客户端
		private String createdDate; // 格式为：6-28 11:12
		private int replyNum; // 回复数
		private int likedNum;//赞的个数
		private boolean liked;//该用户是否点过赞
		private String thumbnailPic;// 留言内容中的小图片
		private String bmiddlePic;//留言内容中的大图片 点击小图片后再出现此大图片
		private AtMemmber[] atMembers;//留言中@ 列表

		public static class ForwardMessageBoardInfo implements Serializable {

			private static final long serialVersionUID = 5779951482395904597L;
			private boolean isForwardRenhe; // 是否是人和网的转发，是人和网的转发需要使用senderName和senderSId
			private String senderName; // 转发者的姓名
			private String senderSId; // 转发者的sid
			private String content; // 转发的内容
			private String thumbnailPic; //转发留言内容中的小图片
			private String bmiddlePic;//转发留言内容中的大图片 点击小图片后再出现此大图片
			private AtMemmber[] atMembers; // 留言转发的id

			private String messageBoardObjectId;//被转发的客厅objectId
			private String messageBoardId;//被转发的客厅id

			public String getThumbnailPic() {
				return thumbnailPic;
			}

			public void setThumbnailPic(String thumbnailPic) {
				this.thumbnailPic = thumbnailPic;
			}

			public String getBmiddlePic() {
				return bmiddlePic;
			}

			public void setBmiddlePic(String bmiddlePic) {
				this.bmiddlePic = bmiddlePic;
			}

			public boolean isForwardRenhe() {
				return isForwardRenhe;
			}

			public void setForwardRenhe(boolean isForwardRenhe) {
				this.isForwardRenhe = isForwardRenhe;
			}

			public String getSenderName() {
				return senderName;
			}

			public void setSenderName(String senderName) {
				this.senderName = senderName;
			}

			public String getSenderSId() {
				return senderSId;
			}

			public void setSenderSId(String senderSId) {
				this.senderSId = senderSId;
			}

			public String getContent() {
				return content;
			}

			public void setContent(String content) {
				this.content = content;
			}

			public AtMemmber[] getForwardMessageBoardAtMembers() {
				return atMembers;
			}

			public void setForwardMessageBoardAtMembers(AtMemmber[] forwardMessageBoardAtMembers) {
				this.atMembers = forwardMessageBoardAtMembers;
			}

			public String getMessageBoardObjectId() {
				return messageBoardObjectId;
			}

			public void setMessageBoardObjectId(String messageBoardObjectId) {
				this.messageBoardObjectId = messageBoardObjectId;
			}

			public String getMessageBoardId() {
				return messageBoardId;
			}

			public void setMessageBoardId(String messageBoardId) {
				this.messageBoardId = messageBoardId;
			}

			@Override
			public String toString() {
				return "ForwardMessageBoardInfo [content=" + content + ", isForwardRenhe=" + isForwardRenhe + ", senderName="
						+ senderName + ", senderSId=" + senderSId + "]";
			}

		}

		public String getThumbnailPic() {
			return thumbnailPic;
		}

		public void setThumbnailPic(String thumbnailPic) {
			this.thumbnailPic = thumbnailPic;
		}

		public String getBmiddlePic() {
			return bmiddlePic;
		}

		public void setBmiddlePic(String bmiddlePic) {
			this.bmiddlePic = bmiddlePic;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
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

		public ForwardMessageBoardInfo getForwardMessageBoardInfo() {
			return forwardMessageBoardInfo;
		}

		public void setForwardMessageBoardInfo(ForwardMessageBoardInfo forwardMessageBoardInfo) {
			this.forwardMessageBoardInfo = forwardMessageBoardInfo;
		}

		public String getFromSource() {
			return fromSource;
		}

		public void setFromSource(String fromSource) {
			this.fromSource = fromSource;
		}

		public int getReplyNum() {
			return replyNum;
		}

		public void setReplyNum(int replyNum) {
			this.replyNum = replyNum;
		}

		public String getCreatedDate() {
			return createdDate;
		}

		public void setCreatedDate(String createdDate) {
			this.createdDate = createdDate;
		}

		public AtMemmber[] getAtMembers() {
			return atMembers;
		}

		public void setAtMembers(AtMemmber[] atMembers) {
			this.atMembers = atMembers;
		}

		public int getLikedNum() {
			return likedNum;
		}

		public void setLikedNum(int likedNum) {
			this.likedNum = likedNum;
		}

		public boolean isLiked() {
			return liked;
		}

		public void setLiked(boolean liked) {
			this.liked = liked;
		}

		@Override
		public String toString() {
			return "MessageBoardInfo [content=" + content + ", createdDate=" + createdDate + ", forwardMessageBoardInfo="
					+ forwardMessageBoardInfo + ", fromSource=" + fromSource + ", id=" + id + ", objectId=" + objectId
					+ ", replyNum=" + replyNum + "]";
		}

	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public SenderInfo getSenderInfo() {
		return senderInfo;
	}

	public void setSenderInfo(SenderInfo senderInfo) {
		this.senderInfo = senderInfo;
	}

	public MessageBoardInfo getMessageBoardInfo() {
		return messageBoardInfo;
	}

	public void setMessageBoardInfo(MessageBoardInfo messageBoardInfo) {
		this.messageBoardInfo = messageBoardInfo;
	}

	@Override
	public String toString() {
		return "ShowMessageBoard [messageBoardInfo=" + messageBoardInfo + ", senderInfo=" + senderInfo + ", state=" + state + "]";
	}

}
