package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class MessageBoardOperationWithErroInfo {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private String errorInfo;//错误信息，若state为-3时，用于直接提示用户的错误信息

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(String errorInfo) {
		this.errorInfo = errorInfo;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

}
