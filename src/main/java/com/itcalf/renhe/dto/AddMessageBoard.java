package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class AddMessageBoard {

	private int state; // 请求是否成功结果 说明：1 请求成功；-3：客厅的内容不能为空；-4：发布的图片的数量超过了9张
	private int messageboardPublicationId;//预发布的客厅内容id
	private String[] messageboardPhotoResourceIds;//预发布的客厅图片的资源id，每个id为uuid，数据类型为String类型
	public long createdDate;// long 客厅内容发布时间，毫秒值
	public boolean publishComplete;// boolean 是否已将全部图片发布完成，若为true则代表已完成，继续返回messageboardObjectId
	public String messageboardObjectId;// String 关联的客厅内容的objectId
	public int messageboardId;// int 说明：关联的客厅内容的id

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getMessageboardPublicationId() {
		return messageboardPublicationId;
	}

	public void setMessageboardPublicationId(int messageboardPublicationId) {
		this.messageboardPublicationId = messageboardPublicationId;
	}

	public String[] getMessageboardPhotoResourceIds() {
		return messageboardPhotoResourceIds;
	}

	public void setMessageboardPhotoResourceIds(String[] messageboardPhotoResourceIds) {
		this.messageboardPhotoResourceIds = messageboardPhotoResourceIds;
	}

	public long getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(long createdDate) {
		this.createdDate = createdDate;
	}

	public boolean isPublishComplete() {
		return publishComplete;
	}

	public void setPublishComplete(boolean publishComplete) {
		this.publishComplete = publishComplete;
	}

	public String getMessageboardObjectId() {
		return messageboardObjectId;
	}

	public void setMessageboardObjectId(String messageboardObjectId) {
		this.messageboardObjectId = messageboardObjectId;
	}

	public int getMessageboardId() {
		return messageboardId;
	}

	public void setMessageboardId(int messageboardId) {
		this.messageboardId = messageboardId;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

}
