package com.itcalf.renhe.dto;

public class CircleAvator {
	private int state;

	private String avatar;

	private int circleId;

	private int preAvatarId;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getCircleId() {
		return circleId;
	}

	public void setCircleId(int circleId) {
		this.circleId = circleId;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getPreAvatarId() {
		return preAvatarId;
	}

	public void setPreAvatarId(int preAvatarId) {
		this.preAvatarId = preAvatarId;
	}

}
