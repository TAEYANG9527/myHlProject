package com.itcalf.renhe.dto;

/**
 * 
 * 同意加好友的接口
 */
public class ReceiveAddFriend {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;-3 您已经通过该请求了;-4 您已经拒绝过该请求;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "ReceiveAddFriend [state=" + state + "]";
	}
}