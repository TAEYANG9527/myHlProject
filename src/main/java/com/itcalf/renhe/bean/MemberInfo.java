package com.itcalf.renhe.bean;

import java.io.Serializable;

public class MemberInfo implements Serializable {
	private static final long serialVersionUID = 1234234L;

	/**
	 * 用户openId
	 * */
	private long openId;
	/**
	 * 用户头像
	 * */
	private String avatar;
	/**
	 * 用户名称
	 * */
	private String nickName;
	/**
	 * 用户名称对应的拼音
	 * */
	private String pinyin;

	private boolean isMaster;

	public MemberInfo() {

	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public MemberInfo(long openId, String avatar, String nickName, String pinyin) {
		this.openId = openId;
		this.avatar = avatar;
		this.nickName = nickName;
		this.pinyin = pinyin;
	}

	public long getOpenId() {
		return openId;
	}

	public void setOpenId(long openId) {
		this.openId = openId;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

}
