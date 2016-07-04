package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class UploadPhotoCacheBean {
	private int messageboardPublicationId;
	private String content;
	private String messageboardPhotoResourceId;
	private String thumbnailPicPath;
	private String bmiddlePicPath;
	private String atMemberStr;
	public int getMessageboardPublicationId() {
		return messageboardPublicationId;
	}

	public void setMessageboardPublicationId(int messageboardPublicationId) {
		this.messageboardPublicationId = messageboardPublicationId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMessageboardPhotoResourceId() {
		return messageboardPhotoResourceId;
	}

	public void setMessageboardPhotoResourceId(String messageboardPhotoResourceId) {
		this.messageboardPhotoResourceId = messageboardPhotoResourceId;
	}

	public String getThumbnailPicPath() {
		return thumbnailPicPath;
	}

	public void setThumbnailPicPath(String thumbnailPicPath) {
		this.thumbnailPicPath = thumbnailPicPath;
	}

	public String getBmiddlePicPath() {
		return bmiddlePicPath;
	}

	public void setBmiddlePicPath(String bmiddlePicPath) {
		this.bmiddlePicPath = bmiddlePicPath;
	}

	public String getAtMemberStr() {
		return atMemberStr;
	}

	public void setAtMemberStr(String atMemberStr) {
		this.atMemberStr = atMemberStr;
	}
}
