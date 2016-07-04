package com.itcalf.renhe.utils;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.UserInfo;

/**
 * 修改本地UserInfo
 * Created by Chong on 2015/9/9.
 */
public class UserInfoUtil {
	public static final int USERFACE = 1001;
	public static final int NAME = 1002;
	public static final int TITLE = 1003;
	public static final int COMPANY = 1004;
	public static final int LOCATION = 1005;

	public static void chengeUserInfo(int type, String changedItem) {
		UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
		switch (type) {
		case USERFACE:
			userInfo.setUserface(changedItem);
			break;
		case NAME:
			userInfo.setName(changedItem);
			break;
		case TITLE:
			userInfo.setTitle(changedItem);
			break;
		case COMPANY:
			userInfo.setCompany(changedItem);
			break;
		case LOCATION:
			userInfo.setLocation(changedItem);
			break;
		default:
			break;
		}
		RenheApplication.getInstance().setUserInfo(userInfo);
		RenheApplication.getInstance().getUserCommand().updateUserInfo(type, changedItem,
				RenheApplication.getInstance().getUserInfo().getSid());
	}

	public static void chengeUserImValidInfo(boolean imvalid) {
		UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
		userInfo.setImValid(imvalid);
		RenheApplication.getInstance().setUserInfo(userInfo);
		RenheApplication.getInstance().getUserCommand().updateUserImValidInfo(imvalid,
				RenheApplication.getInstance().getUserInfo().getSid());
	}

	public static void chengeUserImOpenIdInfo(int imOpenId) {
		UserInfo userInfo = RenheApplication.getInstance().getUserInfo();
		userInfo.setImId(imOpenId);
		RenheApplication.getInstance().setUserInfo(userInfo);
		RenheApplication.getInstance().getUserCommand().updateUserImOpenIdInfo(imOpenId,
				RenheApplication.getInstance().getUserInfo().getSid());
	}
}
