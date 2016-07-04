package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class ReplyMessageBoard {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private String messageboardId;
	private String messageboardObjectId;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMessageboardId() {
		return messageboardId;
	}

	public void setMessageboardId(String messageboardId) {
		this.messageboardId = messageboardId;
	}

	public String getMessageboardObjectId() {
		return messageboardObjectId;
	}

	public void setMessageboardObjectId(String messageboardObjectId) {
		this.messageboardObjectId = messageboardObjectId;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

}
