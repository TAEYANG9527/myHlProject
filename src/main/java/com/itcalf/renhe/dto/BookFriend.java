package com.itcalf.renhe.dto;

import java.io.Serializable;

/**
 * Title: BookFriend.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-11-3 下午5:08:41 <br>
 * @author wangning
 */
public class BookFriend implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;// int 说明： 人脉订阅记录唯一 Id
	int memberId;// int 说明： memberId
	String keywords;// String 说明：订阅的关键字
	int country;// String 说明：国际
	int prov;// String 说明：省份
	int city;// int 说明：城市
	int industryParents;// int 说明：父行业
	int industry;// int 说明：子行业
	int state;// int 说明：1:开放 2:暂停

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public int getProv() {
		return prov;
	}

	public void setProv(int prov) {
		this.prov = prov;
	}

	public int getCity() {
		return city;
	}

	public void setCity(int city) {
		this.city = city;
	}

	public int getIndustryParents() {
		return industryParents;
	}

	public void setIndustryParents(int industryParents) {
		this.industryParents = industryParents;
	}

	public int getIndustry() {
		return industry;
	}

	public void setIndustry(int industry) {
		this.industry = industry;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

}
