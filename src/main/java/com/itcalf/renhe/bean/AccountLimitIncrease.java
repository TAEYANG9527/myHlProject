package com.itcalf.renhe.bean;

/**
 * @description 账户限额升级
 * @author Chan
 * @date 2015-6-30
 */
public class AccountLimitIncrease {

	private int state;
	private AccountLimitIncreaseResult result;

	public AccountLimitIncrease() {
	}

	public class AccountLimitIncreaseResult {
		private int addFriendPerdayLimit;
		private int friendAmountLimit;
		private int renmaiSearchListLimit;

		private IncreaseAddition bindMobileAddition;
		private IncreaseAddition importContactAddition;
		private IncreaseAddition validateRealNameAddition;
		private IncreaseAddition invitePerMemberAddition;
		private IncreaseAddition approvedUserHeadImgAddition;//头像审批通过增加量
		private IncreaseAddition approvedCardsAddition;//名片审批通过增加量
		private IncreaseAddition completeMemberDataAddition;//资料填写完整增加量

		public void setAddFriendPerdayLimit(int addFriendPerdayLimit) {
			this.addFriendPerdayLimit = addFriendPerdayLimit;
		}

		public int getFriendAmountLimit() {
			return friendAmountLimit;
		}

		public void setFriendAmountLimit(int friendAmountLimit) {
			this.friendAmountLimit = friendAmountLimit;
		}

		public int getRenmaiSearchListLimit() {
			return renmaiSearchListLimit;
		}

		public void setRenmaiSearchListLimit(int renmaiSearchListLimit) {
			this.renmaiSearchListLimit = renmaiSearchListLimit;
		}

		public IncreaseAddition getBindMobileAddition() {
			return bindMobileAddition;
		}

		public void setBindMobileAddition(IncreaseAddition bindMobileAddition) {
			this.bindMobileAddition = bindMobileAddition;
		}

		public IncreaseAddition getImportContactAddition() {
			return importContactAddition;
		}

		public void setImportContactAddition(IncreaseAddition importContactAddition) {
			this.importContactAddition = importContactAddition;
		}

		public IncreaseAddition getValidateRealNameAddition() {
			return validateRealNameAddition;
		}

		public void setValidateRealNameAddition(IncreaseAddition validateRealNameAddition) {
			this.validateRealNameAddition = validateRealNameAddition;
		}

		public IncreaseAddition getInvitePerMemberAddition() {
			return invitePerMemberAddition;
		}

		public void setInvitePerMemberAddition(IncreaseAddition invitePerMemberAddition) {
			this.invitePerMemberAddition = invitePerMemberAddition;
		}

		public class IncreaseAddition {

			private int addFriendPerDayAddition;
			private int friendAmountLimitAddition;
			private int renmaiSearchListLimitAddition;
			private boolean advancedSearch;
			private boolean memberNearbyFilter;

			public int getAddFriendPerDayAddition() {
				return addFriendPerDayAddition;
			}

			public void setAddFriendPerDayAddition(int addFriendPerDayAddition) {
				this.addFriendPerDayAddition = addFriendPerDayAddition;
			}

			public int getFriendAmountLimitAddition() {
				return friendAmountLimitAddition;
			}

			public void setFriendAmountLimitAddition(int friendAmountLimitAddition) {
				this.friendAmountLimitAddition = friendAmountLimitAddition;
			}

			public int getRenmaiSearchListLimitAddition() {
				return renmaiSearchListLimitAddition;
			}

			public void setRenmaiSearchListLimitAddition(int renmaiSearchListLimitAddition) {
				this.renmaiSearchListLimitAddition = renmaiSearchListLimitAddition;
			}

			public boolean isAdvancedSearch() {
				return advancedSearch;
			}

			public void setAdvancedSearch(boolean advancedSearch) {
				this.advancedSearch = advancedSearch;
			}

			public boolean isMemberNearbyFilter() {
				return memberNearbyFilter;
			}

			public void setMemberNearbyFilter(boolean memberNearbyFilter) {
				this.memberNearbyFilter = memberNearbyFilter;
			}
		}

		public int getAddFriendPerdayLimit() {
			return addFriendPerdayLimit;
		}

		public IncreaseAddition getApprovedUserHeadImgAddition() {
			return approvedUserHeadImgAddition;
		}

		public void setApprovedUserHeadImgAddition(IncreaseAddition approvedUserHeadImgAddition) {
			this.approvedUserHeadImgAddition = approvedUserHeadImgAddition;
		}

		public IncreaseAddition getApprovedCardsAddition() {
			return approvedCardsAddition;
		}

		public void setApprovedCardsAddition(IncreaseAddition approvedCardsAddition) {
			this.approvedCardsAddition = approvedCardsAddition;
		}

		public IncreaseAddition getCompleteMemberDataAddition() {
			return completeMemberDataAddition;
		}

		public void setCompleteMemberDataAddition(IncreaseAddition completeMemberDataAddition) {
			this.completeMemberDataAddition = completeMemberDataAddition;
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public AccountLimitIncreaseResult getResult() {
		return result;
	}

	public void setResult(AccountLimitIncreaseResult result) {
		this.result = result;
	}

}
