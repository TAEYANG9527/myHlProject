package com.itcalf.renhe.dto;

public class ReplyUnMessageBoard {
	private int state; // 说明：1 请求成功；-3 noticeId为空；-4 noticeObjectId为空；-5 content内容超过了800的字数限制或为空；-6 noticeType 错误；

	private String noticeCommentObjectId; // 人脉圈评论的objectId

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getNoticeCommentObjectId() {
		return noticeCommentObjectId;
	}

	public void setNoticeCommentObjectId(String noticeCommentObjectId) {
		this.noticeCommentObjectId = noticeCommentObjectId;
	}

}
