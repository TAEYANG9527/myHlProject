package com.itcalf.renhe.bean;

import java.io.Serializable;

public class SearchRecommendedBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String sid;
	private String userface;
	private String name;
	private String title;
	private String company;
	private String location;
	private String industry;
	private int accountType;
	private boolean isRealname;
	private int connectionNum;
	private int source;// 1 默认 2 邮件导入 3 同事 4 人脉订阅 5 供需关键字匹配 6 分类匹配
	private int state;//1 新的推荐人脉 0 不是新的推荐人脉

	public SearchRecommendedBean() {
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getUserface() {
		return userface;
	}

	public void setUserface(String userface) {
		this.userface = userface;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public boolean isRealname() {
		return isRealname;
	}

	public void setRealname(boolean isRealname) {
		this.isRealname = isRealname;
	}

	public int getConnectionNum() {
		return connectionNum;
	}

	public void setConnectionNum(int connectionNum) {
		this.connectionNum = connectionNum;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}
