package com.itcalf.renhe.dto;

import java.io.Serializable;

public class NewMessage implements Serializable {
	private static final long serialVersionUID = 149883934230611700L;
	private int state;
	private boolean haveNewMessage;
	private int newMessageNum;
	private String maxMessageObjectId;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isHaveNewMessage() {
		return haveNewMessage;
	}

	public void setHaveNewMessage(boolean haveNewMessage) {
		this.haveNewMessage = haveNewMessage;
	}

	public int getNewMessageNum() {
		return newMessageNum;
	}

	public void setNewMessageNum(int newMessageNum) {
		this.newMessageNum = newMessageNum;
	}

	public String getMaxMessageObjectId() {
		return maxMessageObjectId;
	}

	public void setMaxMessageObjectId(String maxMessageObjectId) {
		this.maxMessageObjectId = maxMessageObjectId;
	}

	@Override
	public String toString() {
		return "NewMessage [state=" + state + ", haveNewMessage=" + haveNewMessage + ", newMessageNum=" + newMessageNum
				+ ", maxMessageObjectId=" + maxMessageObjectId + "]";
	}
}
