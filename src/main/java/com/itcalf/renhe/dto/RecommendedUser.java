package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: RecommendedUser.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-9-12 上午10:53:55 <br>
 * @author wangning
 */
public class RecommendedUser implements Serializable {
	private static final long serialVersionUID = 1L;
	private String sid;
	private String userface;
	private String name;
	private String title;
	private String company;
	private int accountType;
	private int accountTypeYearPeriod;
	private boolean isRealname = false;
	private boolean isChecked = false;

	@Override
	public String toString() {
		return new org.apache.commons.lang3.builder.ToStringBuilder(this).append("sid", sid).append("userface", userface)
				.append("name", name).append("title", title).append("company", company).append("accountType", accountType)
				.append("accountTypeYearPeriod", accountTypeYearPeriod).append("isChecked", isChecked).toString();
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

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public int getAccountTypeYearPeriod() {
		return accountTypeYearPeriod;
	}

	public void setAccountTypeYearPeriod(int accountTypeYearPeriod) {
		this.accountTypeYearPeriod = accountTypeYearPeriod;
	}

	public boolean isRealname() {
		return isRealname;
	}

	public void setRealname(boolean isRealname) {
		this.isRealname = isRealname;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

}
