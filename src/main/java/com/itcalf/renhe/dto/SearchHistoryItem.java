package com.itcalf.renhe.dto;

/**
 * Title: SearchHistoryItem.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-7-24 上午9:28:41 <br>
 * @author wangning
 */
public class SearchHistoryItem {
	private String kewword;
	private String area;
	private String industry;
	private int areaCode;
	private int industryCode;
	private String company;
	private String job;
	private String name;
	private long createTime;

	public SearchHistoryItem(String kewword, String area, String industry, int areaCode, int industryCode, String company,
			String job, String name, long createTime) {
		super();
		this.kewword = kewword;
		this.area = area;
		this.industry = industry;
		this.areaCode = areaCode;
		this.industryCode = industryCode;
		this.company = company;
		this.job = job;
		this.name = name;
		this.createTime = createTime;
	}

	public String getKewword() {
		return kewword;
	}

	public void setKewword(String kewword) {
		this.kewword = kewword;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public int getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(int areaCode) {
		this.areaCode = areaCode;
	}

	public int getIndustryCode() {
		return industryCode;
	}

	public void setIndustryCode(int industryCode) {
		this.industryCode = industryCode;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}
