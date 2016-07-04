package com.itcalf.renhe.command.impl;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.command.IUserCommand;
import com.itcalf.renhe.database.sqlite.TablesConstant;
import com.itcalf.renhe.database.sqlite.UserDBHelper;
import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.ManifestUtil;

import java.util.HashMap;
import java.util.Map;

public class UserCommandImpl implements IUserCommand {

	private Context mContext;

	public UserCommandImpl(Application application) {
		super();
		mContext = application.getApplicationContext();
	}

	@Override
	public UserInfo login(UserInfo userInfo) {
		try {
			Map<String, Object> reqParams = new HashMap<String, Object>();
			reqParams.put("username", userInfo.getLoginAccountType());
			reqParams.put("password", userInfo.getPwd());
			reqParams.put("createImUser", true);// 说明：是否需要新增并注册im用户
			reqParams.put("token", Constants.PUSH_DFVICE_TOKEN);// 说明：用于推送的token，登录后用member_id加此字段保存推送用的信息
			reqParams.put("bundle", Constants.JPUSH_APP_BUNDLE);// 说明：设备bundle，用来区分是哪个应用，android客户端传递renhe_android就可以了
			reqParams.put("version", DeviceUitl.getAppVersion());// 说明：设备版本号：例如3.0.1
			UserInfo info = (UserInfo) HttpUtil.doHttpRequest(Constants.Http.NEWLOGIN, reqParams, UserInfo.class, mContext);
			return info;
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "login", e);
			}
			return null;
		}
	}

	@Override
	public UserInfo register(UserInfo userInfo, int industryId, String gender) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("mobile", userInfo.getMobile());
		reqParams.put("email", userInfo.getEmail());
		//		reqParams.put("password", userInfo.getPwd());
		//		reqParams.put("repeatPassword", userInfo.getPwd());
		reqParams.put("name", userInfo.getName());
		reqParams.put("title", userInfo.getTitle());
		reqParams.put("company", userInfo.getCompany());
		reqParams.put("industry", industryId);
		reqParams.put("gender", gender);
		reqParams.put("createImUser", true);
		reqParams.put("token", Constants.PUSH_DFVICE_TOKEN);// 说明：用于推送的token，登录后用member_id加此字段保存推送用的信息
		reqParams.put("bundle", Constants.JPUSH_APP_BUNDLE);// 说明：设备bundle，用来区分是哪个应用，android客户端传递renhe_android就可以了
		reqParams.put("version", DeviceUitl.getAppVersion());// 说明：设备版本号：例如3.0.1

		try {
			return (UserInfo) HttpUtil.doHttpRequest(Constants.Http.REGISTERMOBILE, reqParams, UserInfo.class, mContext);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "register", e);
			}
		}
		return null;
	}

	/**
	 * 新注册接口V6  参数变化
	 *  add by chan 2015.4.9
	 */
	@Override
	public UserInfo register_v6(UserInfo userInfo, int gender, int industryId, int cityCode, String Longitude, String latitude) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("mobile", userInfo.getMobile());
		reqParams.put("password", userInfo.getPwd());
		reqParams.put("name", userInfo.getName());
		reqParams.put("gender", gender);//可传“”
		reqParams.put("title", userInfo.getTitle());
		reqParams.put("company", userInfo.getCompany());
		reqParams.put("industry", industryId);
		reqParams.put("token", Constants.PUSH_DFVICE_TOKEN);// 说明：用于推送的token，登录后用member_id加此字段保存推送用的信息
		reqParams.put("city", cityCode);
		reqParams.put("lat", latitude);
		reqParams.put("lng", Longitude);
		reqParams.put("channelCode", ManifestUtil.getUmengChannel(mContext));
		reqParams.put("deviceType", 0);
		try {
			return (UserInfo) HttpUtil.doHttpRequest(Constants.Http.REGISTERMOBILE_V6, reqParams, UserInfo.class, mContext);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "register", e);
			}
		}
		return null;
	}

	@Override
	public UserInfo getLoginUser() {
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		UserInfo userInfo = null;
		Cursor cursor = null;
		try {
			cursor = userDBHelper.find(null, TablesConstant.USER_TABLE_COLUMN_REMEMBER + "=?", new String[] { "1" }, null, null,
					null);
			if (null != cursor) {
				if (cursor.moveToLast()) {
					userInfo = new UserInfo();
					userDBHelper.contentValueToPo(cursor, userInfo);
				}
			}
		} catch (Exception e) {
			if (Constants.DEBUG_MODE) {
				Log.w(Constants.TAG, "getLoginUser", e);
			}
		} finally {
			if (null != cursor) {
				cursor.close();
			}
			userDBHelper.closeDB();
		}
		return userInfo;
	}

	@Override
	public UserInfo[] getAllUsers() {
		UserInfo[] userInfos = null;
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		try {
			userInfos = userDBHelper.findAllUser();
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "getAllUsers", e);
			}
		} finally {
			userDBHelper.closeDB();
		}
		return userInfos;
	}

	@Override
	public long delUser(String email) {
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		try {
			return userDBHelper.delUserByEmail(email);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "delUser", e);
			}
		} finally {
			userDBHelper.closeDB();
		}
		return 0;
	}

	@Override
	public long insertOrUpdate(UserInfo userInfo) {
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		try {
			return userDBHelper.insertOrUpdateUser(userInfo);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "insertOrUpdate", e);
			}
		} finally {
			userDBHelper.closeDB();
		}
		return 0;
	}

	@Override
	public UserInfo oldRegister(UserInfo userInfo, int industryId) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("email", userInfo.getEmail());
		reqParams.put("password", userInfo.getPwd());
		reqParams.put("repeatPassword", userInfo.getPwd());
		reqParams.put("name", userInfo.getName());
		reqParams.put("title", userInfo.getTitle());
		reqParams.put("company", userInfo.getCompany());
		reqParams.put("mobile", userInfo.getMobile());
		reqParams.put("industry", industryId);
		reqParams.put("createImUser", true);
		reqParams.put("token", Constants.PUSH_DFVICE_TOKEN);// 说明：用于推送的token，登录后用member_id加此字段保存推送用的信息
		reqParams.put("bundle", Constants.JPUSH_APP_BUNDLE);// 说明：设备bundle，用来区分是哪个应用，android客户端传递renhe_android就可以了
		reqParams.put("version", DeviceUitl.getAppVersion());// 说明：设备版本号：例如3.0.1

		try {
			return (UserInfo) HttpUtil.doHttpRequest(Constants.Http.REGISTER, reqParams, UserInfo.class, mContext);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "register", e);
			}
		}
		return null;
	}

	@Override
	public long updateUserInfo(int type, String changedItem, String sid) {
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		try {
			return userDBHelper.updateUser(type, changedItem, sid);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "updateUserInfo", e);
			}
		} finally {
			userDBHelper.closeDB();
		}
		return 0;
	}
	@Override
	public long updateUserImValidInfo(boolean imvalid, String sid) {
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		try {
			return userDBHelper.updateUserImValid(imvalid, sid);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "updateUserInfo", e);
			}
		} finally {
			userDBHelper.closeDB();
		}
		return 0;
	}
	@Override
	public long updateUserImOpenIdInfo(int imOpenId, String sid) {
		UserDBHelper userDBHelper = new UserDBHelper(mContext, TablesConstant.USER_TABLE);
		try {
			return userDBHelper.updateUserImOpenIdValid(imOpenId, sid);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.w(Constants.TAG, "updateUserInfo", e);
			}
		} finally {
			userDBHelper.closeDB();
		}
		return 0;
	}
}
