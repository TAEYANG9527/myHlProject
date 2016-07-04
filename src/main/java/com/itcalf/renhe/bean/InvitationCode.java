package com.itcalf.renhe.bean;

/**
 * @description 邀请
 * @author Chan
 * @date 2015-7-1
 */
public class InvitationCode {

	private int state;
	private String inviteCode;
	private int canInput;
	private int used;
	private String sid;
	private String name;
	private String curCompany;
	private String curTitle;
	private int accountType;
	private String userFace;
	private boolean isRealname;

	public InvitationCode() {
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public int getCanInput() {
		return canInput;
	}

	public void setCanInput(int canInput) {
		this.canInput = canInput;
	}

	public int getUsed() {
		return used;
	}

	public void setUsed(int used) {
		this.used = used;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurCompany() {
		return curCompany;
	}

	public void setCurCompany(String curCompany) {
		this.curCompany = curCompany;
	}

	public String getCurTitle() {
		return curTitle;
	}

	public void setCurTitle(String curTitle) {
		this.curTitle = curTitle;
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public String getUserFace() {
		return userFace;
	}

	public void setUserFace(String userFace) {
		this.userFace = userFace;
	}

	public boolean isRealname() {
		return isRealname;
	}

	public void setRealname(boolean isRealname) {
		this.isRealname = isRealname;
	}
}
