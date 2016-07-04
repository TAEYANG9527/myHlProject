package com.itcalf.renhe.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class FollowState implements Serializable {

	private static final long serialVersionUID = -1103429939337738086L;

	private int state; //说明：1 请求成功；-1 权限不足；-2发生未知错误;

	private int followState; //1 已关注过人和网 2. 未关注过人和网

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("state", state).append("followState", followState).toString();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getFollowState() {
		return followState;
	}

	public void setFollowState(int followState) {
		this.followState = followState;
	}

}
