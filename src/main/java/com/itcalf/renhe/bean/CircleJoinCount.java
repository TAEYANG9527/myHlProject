package com.itcalf.renhe.bean;

import java.io.Serializable;

public class CircleJoinCount implements Serializable {

	private static final long serialVersionUID = 1L;
	private int state;//说明：1 请求成功；-1 权限不足；-2发生未知错误；-3 imConversationId不能为空；-4 必须是圈子创建者才能调用此接口
	private int unReadCount;//未读加入圈子的申请数
	private int unReadReplyCount;// int 未读的我的话题个数
	private int unReadTopicCount;// int 未读的圈子话题个数
	private String myTopicUrl;// string 我的话题链接
	private String circleTopicUrl;// string 圈子话题链接
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getUnReadCount() {
		return unReadCount;
	}

	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}

	public int getUnReadReplyCount() {
		return unReadReplyCount;
	}

	public void setUnReadReplyCount(int unReadReplyCount) {
		this.unReadReplyCount = unReadReplyCount;
	}

	public int getUnReadTopiCount() {
		return unReadTopicCount;
	}

	public void setUnReadTopiCount(int unReadTopiCount) {
		this.unReadTopicCount = unReadTopiCount;
	}

	public String getMyTopicUrl() {
		return myTopicUrl;
	}

	public void setMyTopicUrl(String myTopicUrl) {
		this.myTopicUrl = myTopicUrl;
	}

	public String getCircleTopicUrl() {
		return circleTopicUrl;
	}

	public void setCircleTopicUrl(String circleTopicUrl) {
		this.circleTopicUrl = circleTopicUrl;
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("state", state)
				.append("unReadCount", unReadCount).toString();
	}
}
