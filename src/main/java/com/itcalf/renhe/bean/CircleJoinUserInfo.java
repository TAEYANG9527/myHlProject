package com.itcalf.renhe.bean;

import java.io.Serializable;

public class CircleJoinUserInfo implements Serializable {
	private static final long serialVersionUID = 1254353L;

	/**
	 * 会员sid
	 * */
	private String sid;

	/**
	 * 会员的im账号id
	 * */
	private int imId;

	/**
	 * 头像
	 * */
	private String userfaceUrl;

	/**
	 * 姓名
	 * */
	private String name;

	/**
	 * 职务
	 * */
	private String title;

	/**
	 * 公司
	 * */
	private String company;

	/**
	 * 行业
	 * */
	private String industry;

	/**
	 * 地区
	 * */
	private String location;

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public int getImId() {
		return imId;
	}

	public void setImId(int imId) {
		this.imId = imId;
	}

	public String getUserfaceUrl() {
		return userfaceUrl;
	}

	public void setUserfaceUrl(String userfaceUrl) {
		this.userfaceUrl = userfaceUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
