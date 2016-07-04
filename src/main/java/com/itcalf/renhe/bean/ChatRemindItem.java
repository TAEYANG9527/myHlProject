package com.itcalf.renhe.bean;

import com.alibaba.wukong.im.Member;

import cn.renhe.heliao.idl.circle.CircleMember;

/**
 * Created by Chong on 2015/8/7.
 */
public class ChatRemindItem {
	public static final int ITEM = 0;// 区别是否选择的是字母还是选项
	public static final int SECTION = 1;

	public final int type;
	public String text;
	public int sectionPosition;// 字母的item位置
	public int listPosition;// 联系人的item

	public Member member;
	public CircleMember.MemberInfo memberInfo;

	public ChatRemindItem(int type, String text) {
		this.type = type;
		this.text = text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Member getMember() {
		return member;
	}
	public void setMemberInfo(CircleMember.MemberInfo memberInfo){
		this.memberInfo = memberInfo;
	}
	public CircleMember.MemberInfo getMemberInfo(){
		return memberInfo;
	}

	@Override
	public String toString() {
		return text;
	}

}
