package com.itcalf.renhe.bean;

/**
 * @description 通讯录上传 
 * 用于gjson转化为json格式字符串
 * @author Chan
 * @date 2015-4-13
 */
public class ContactUpLoadBean {
	private String id;
	private String name;
	private String[] mobiles;
	private String[] emails;
	private String[] addresses;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getMobiles() {
		return mobiles;
	}

	public void setMobiles(String[] mobiles) {
		this.mobiles = mobiles;
	}

	public String[] getEmails() {
		return emails;
	}

	public void setEmails(String[] emails) {
		this.emails = emails;
	}

	public String[] getAddresses() {
		return addresses;
	}

	public void setAddresses(String[] addresses) {
		this.addresses = addresses;
	}

}
