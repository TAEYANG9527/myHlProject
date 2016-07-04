package com.itcalf.renhe.bean;

import com.itcalf.renhe.po.Contact;

/**
 * @author 		chan
 * @createtime 	2014-10-20
 * @功能说明 		联系人列表（带首字母）的 模型类
 */
public class ContactItem {

	public static final int ITEM = 0;// 区别是否选择的是字母还是选项
	public static final int SECTION = 1;

	public final int type;
	public String text;
	public String id;
	public int sectionPosition;// 字母的item位置
	public int listPosition;// 联系人的item

	public Contact contact;

	public ContactItem(int type, String text, String id) {
		this.type = type;
		this.text = text;
		this.id = id;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

}
