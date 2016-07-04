package com.itcalf.renhe.command;

import com.itcalf.renhe.dto.UserInfo;

public interface IUserCommand {

	UserInfo login(UserInfo userInfo);

	UserInfo register(UserInfo userInfo, int industryId, String gender);

	UserInfo oldRegister(UserInfo userInfo, int industryId);

	UserInfo getLoginUser();

	UserInfo[] getAllUsers();

	long delUser(String email);

	long insertOrUpdate(UserInfo userInfo);

	/**
	 * 新注册接口V6  参数变化加经纬度
	 *  add by chan 2015.4.9
	 */
	UserInfo register_v6(UserInfo userInfo, int gender, int industryId, int cityCode, String Longitude, String latitude);

	long updateUserInfo(int type, String changedItem, String sid);
	long updateUserImValidInfo(boolean imvalid, String sid);
	long updateUserImOpenIdInfo(int imOpenId, String sid);

}
