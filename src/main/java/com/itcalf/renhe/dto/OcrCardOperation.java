package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class OcrCardOperation {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private String uuid;//被保存/删除 的名片uuid
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

}
