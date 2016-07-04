package com.itcalf.renhe.bean;

/**
 * @description 账户限额信息
 * @author Chan
 * @date 2015-6-29
 */
public class AccountLimitInfo {

	private int state;
	// 每日可发送好友邀请数
	private int addFriendPerdayLimit;
	private int addFriendCountToday;
	private boolean increaseAddFriendLimit;
	// 人脉上限
	private int friendAmountLimit;
	private int friendAmountUsed;
	private boolean increaseFriendAmountLimit;
	// 人脉搜索列表
	private int renmaiSearchListLimit;
	private boolean increaseRenmaiSearchListLimit;
	// 高级搜索
	private boolean advacedSearchPrivilege;
	private boolean memberNearbyFilter;

	public AccountLimitInfo() {
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getAddFriendPerdayLimit() {
		return addFriendPerdayLimit;
	}

	public void setAddFriendPerdayLimit(int addFriendPerdayLimit) {
		this.addFriendPerdayLimit = addFriendPerdayLimit;
	}

	public int getAddFriendCountToday() {
		return addFriendCountToday;
	}

	public void setAddFriendCountToday(int addFriendCountToday) {
		this.addFriendCountToday = addFriendCountToday;
	}

	public boolean isIncreaseAddFriendLimit() {
		return increaseAddFriendLimit;
	}

	public void setIncreaseAddFriendLimit(boolean increaseAddFriendLimit) {
		this.increaseAddFriendLimit = increaseAddFriendLimit;
	}

	public int getFriendAmountLimit() {
		return friendAmountLimit;
	}

	public void setFriendAmountLimit(int friendAmountLimit) {
		this.friendAmountLimit = friendAmountLimit;
	}

	public int getFriendAmountUsed() {
		return friendAmountUsed;
	}

	public void setFriendAmountUsed(int friendAmountUsed) {
		this.friendAmountUsed = friendAmountUsed;
	}

	public boolean isIncreaseFriendAmountLimit() {
		return increaseFriendAmountLimit;
	}

	public void setIncreaseFriendAmountLimit(boolean increaseFriendAmountLimit) {
		this.increaseFriendAmountLimit = increaseFriendAmountLimit;
	}

	public int getRenmaiSearchListLimit() {
		return renmaiSearchListLimit;
	}

	public void setRenmaiSearchListLimit(int renmaiSearchListLimit) {
		this.renmaiSearchListLimit = renmaiSearchListLimit;
	}

	public boolean isIncreaseRenmaiSearchListLimit() {
		return increaseRenmaiSearchListLimit;
	}

	public void setIncreaseRenmaiSearchListLimit(boolean increaseRenmaiSearchListLimit) {
		this.increaseRenmaiSearchListLimit = increaseRenmaiSearchListLimit;
	}

	public boolean isAdvacedSearchPrivilege() {
		return advacedSearchPrivilege;
	}

	public void setAdvacedSearchPrivilege(boolean advacedSearchPrivilege) {
		this.advacedSearchPrivilege = advacedSearchPrivilege;
	}

	public boolean isMemberNearbyFilter() {
		return memberNearbyFilter;
	}

	public void setMemberNearbyFilter(boolean memberNearbyFilter) {
		this.memberNearbyFilter = memberNearbyFilter;
	}
}
