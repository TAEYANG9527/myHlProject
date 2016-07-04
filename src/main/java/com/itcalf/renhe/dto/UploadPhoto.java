package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class UploadPhoto {

	private int state; // 请求是否成功结果 说明：1 请求成功；-3：客厅的内容不能为空；-4：发布的图片的数量超过了9张
	private String thumbnailPicUrl;
	private String bmiddlePicUrl;
	private boolean publishComplete;// boolean 是否已将全部图片发布完成，若为true则代表已完成，继续返回messageboardObjectId
	private String messageboardObjectId;// String 关联的客厅内容的objectId
	private int messageboardId;// int 说明：关联的客厅内容的id
	private int bmiddlePicWidth;// int 说明：图片的大图信息的宽度
	private int bmiddlePicHeight;// int 说明：图片的大图信息的高度

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getThumbnailPicUrl() {
		return thumbnailPicUrl;
	}

	public void setThumbnailPicUrl(String thumbnailPicUrl) {
		this.thumbnailPicUrl = thumbnailPicUrl;
	}

	public String getBmiddlePicUrl() {
		return bmiddlePicUrl;
	}

	public void setBmiddlePicUrl(String bmiddlePicUrl) {
		this.bmiddlePicUrl = bmiddlePicUrl;
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

	public int getBmiddlePicWidth() {
		return bmiddlePicWidth;
	}

	public void setBmiddlePicWidth(int bmiddlePicWidth) {
		this.bmiddlePicWidth = bmiddlePicWidth;
	}

	public int getBmiddlePicHeight() {
		return bmiddlePicHeight;
	}

	public void setBmiddlePicHeight(int bmiddlePicHeight) {
		this.bmiddlePicHeight = bmiddlePicHeight;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

}
