package com.itcalf.renhe.dto;

/**
 * 
 * 1,发布客厅留言 2,转发客厅留言 3,给客厅留言进行回复
 */
public class PersonAuth {

	private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
	private boolean showVipAddFriendPrivilege;// boolean 是否显示“只允许付费会员添加我为好友”这个选项
	private boolean viewProfileHidePrivilege;// boolean 说明：是否显示“希望隐身访问其他会员档案”选项
	private boolean payAccountViewFullProfilePrivilege;// boolean 说明：是否显示“只允许付费会员查看我的全部档案”选项
	private int referralType;// int 1：允许直接添加我为好友 2：只允许通过实名认证的会员添加我为好友 3：只允许付费会员添加我为好友
	private int lietouBeContact;// int 说明：1 允许猎头直接联系；0 不允许猎头直接联系
	private boolean memberLocationStatus;// int 说明：true 打开附近的人脉；false 关闭附近的人脉
	private boolean seoCannotEmbody;// boolean 说明：为true时代表设置了“我不希望档案被搜索引擎收录”，false代表不设置
	private boolean stealthViewProfile;// boolean 说明：为true时代表设置了“我希望隐身访问其他会员档案”，false代表不设置
	private boolean vipViewFullProfile;// boolean 说明：为true时代表设置了“只允许付费会员查看我的全部档案”，false代表不设置

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public boolean isShowVipAddFriendPrivilege() {
		return showVipAddFriendPrivilege;
	}

	public void setShowVipAddFriendPrivilege(boolean showVipAddFriendPrivilege) {
		this.showVipAddFriendPrivilege = showVipAddFriendPrivilege;
	}

	public boolean isViewProfileHidePrivilege() {
		return viewProfileHidePrivilege;
	}

	public void setViewProfileHidePrivilege(boolean viewProfileHidePrivilege) {
		this.viewProfileHidePrivilege = viewProfileHidePrivilege;
	}

	public boolean isPayAccountViewFullProfilePrivilege() {
		return payAccountViewFullProfilePrivilege;
	}

	public void setPayAccountViewFullProfilePrivilege(boolean payAccountViewFullProfilePrivilege) {
		this.payAccountViewFullProfilePrivilege = payAccountViewFullProfilePrivilege;
	}

	public int getReferralType() {
		return referralType;
	}

	public void setReferralType(int referralType) {
		this.referralType = referralType;
	}

	public int getLietouBeContact() {
		return lietouBeContact;
	}

	public void setLietouBeContact(int lietouBeContact) {
		this.lietouBeContact = lietouBeContact;
	}

	public boolean isSeoCannotEmbody() {
		return seoCannotEmbody;
	}

	public void setSeoCannotEmbody(boolean seoCannotEmbody) {
		this.seoCannotEmbody = seoCannotEmbody;
	}

	public boolean isStealthViewProfile() {
		return stealthViewProfile;
	}

	public void setStealthViewProfile(boolean stealthViewProfile) {
		this.stealthViewProfile = stealthViewProfile;
	}

	public boolean isVipViewFullProfile() {
		return vipViewFullProfile;
	}

	public void setVipViewFullProfile(boolean vipViewFullProfile) {
		this.vipViewFullProfile = vipViewFullProfile;
	}

	@Override
	public String toString() {
		return "PublishMessageBoard [state=" + state + "]";
	}

	public boolean isMemberLocationStatus() {
		return memberLocationStatus;
	}

	public void setMemberLocationStatus(boolean memberLocationStatus) {
		this.memberLocationStatus = memberLocationStatus;
	}

}
