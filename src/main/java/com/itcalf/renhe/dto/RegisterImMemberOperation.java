package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class RegisterImMemberOperation {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private int imId;// int im用户id
	private String imPwd;// String im登陆用的密码

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getImId() {
		return imId;
	}

	public void setImId(int imId) {
		this.imId = imId;
	}

	public String getImPwd() {
		return imPwd;
	}

	public void setImPwd(String imPwd) {
		this.imPwd = imPwd;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

}
