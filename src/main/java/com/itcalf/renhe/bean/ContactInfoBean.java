package com.itcalf.renhe.bean;

/**
 * 存放联系人的模型
 */
public class ContactInfoBean {
	String name;
	String phone;
	String contactface;

	public ContactInfoBean() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone1) {
		this.phone = phone1;
	}

	public String getContactface() {
		return contactface;
	}

	public void setContactface(String contactface) {
		this.contactface = contactface;
	}
}
