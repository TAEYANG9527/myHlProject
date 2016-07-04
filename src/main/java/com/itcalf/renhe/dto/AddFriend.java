package com.itcalf.renhe.dto;

/**
 * 
 * 添加好友接口
 */
public class AddFriend {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;-3 被添加好友的会员sid参数有误;
						// -4 10天内已发出过添加好友邀请,请勿重复发送加好友请求;-5 已经是好友了;
						// -6 Android客户端超过每日加好友的限制(目前限制为50个);
	private int limit;//添加上限

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "AddFriend [state=" + state + "]";
	}
}