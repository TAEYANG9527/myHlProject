package com.itcalf.renhe.database.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.itcalf.renhe.dto.UserInfo;
import com.itcalf.renhe.utils.UserInfoUtil;

public class UserDBHelper extends BaseDBHelper {

	public UserDBHelper(Context context, String table) {
		super(context, table);
	}

	/**
	 * 获取所有用户账户
	 * 
	 * @return
	 */
	public synchronized UserInfo[] findAllUser() {
		Cursor cursor = find(null, null, null, null, null, TablesConstant.USER_TABLE_COLUMN_LOGINTIME + " DESC");
		UserInfo[] userInfos = null;
		if (null != cursor) {
			if (cursor.moveToFirst()) {
				userInfos = new UserInfo[cursor.getCount()];
				do {
					UserInfo userInfo = new UserInfo();
					contentValueToPo(cursor, userInfo);
					userInfos[cursor.getPosition()] = userInfo;
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return userInfos;
	}

	//
	/**
	 * 插入一个用户账户
	 * 
	 * @param userInfo
	 * @return
	 */
	public synchronized long insertOrUpdateUser(UserInfo userInfo) {
		ContentValues values = poToContentValues(userInfo);
		// 判断原来表中是否有数据，清空;保证表中只有一个登入用户的信息
		delData(null, null);
		return insertData(values);
		//		}
	}

	/**
	 * 更新用户的某一个字段
	 * 
	 */
	public synchronized long updateUser(int type, String changedItem, String sid) {
		ContentValues values;
		switch (type) {
		case UserInfoUtil.USERFACE:
			values = new ContentValues();
			values.put(TablesConstant.USER_TABLE_COLUMN_USERFACE, changedItem);
			return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
		case UserInfoUtil.NAME:
			values = new ContentValues();
			values.put(TablesConstant.USER_TABLE_COLUMN_NAME, changedItem);
			return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
		case UserInfoUtil.TITLE:
			values = new ContentValues();
			values.put(TablesConstant.USER_TABLE_COLUMN_TITLE, changedItem);
			return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
		case UserInfoUtil.COMPANY:
			values = new ContentValues();
			values.put(TablesConstant.USER_TABLE_COLUMN_COMPANY, changedItem);
			return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
		case UserInfoUtil.LOCATION:
			values = new ContentValues();
			values.put(TablesConstant.USER_TABLE_COLUMN_LOCATION, changedItem);
			return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
		}
		return 0;
	}

	/**
	 * 更新用户的IMvalid字段
	 *
	 */
	public synchronized long updateUserImValid(boolean imvalid, String sid) {
		ContentValues values;
		values = new ContentValues();
		if (imvalid) {
			values.put(TablesConstant.USER_TABLE_COLUMN_IMVALID, 1);
		} else {
			values.put(TablesConstant.USER_TABLE_COLUMN_IMVALID, 0);
		}
		return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
	}

	/**
	 * 更新用户的IMOpenId字段
	 *
	 */
	public synchronized long updateUserImOpenIdValid(int imOpenId, String sid) {
		ContentValues values;
		values = new ContentValues();
		values.put(TablesConstant.USER_TABLE_COLUMN_IMOPENID, imOpenId);
		return updateData(values, TablesConstant.USER_TABLE_COLUMN_SID + "=?", new String[] { sid });
	}

	/**
	 * 根据用户名删除用户账户
	 * 
	 * @param email
	 * @return
	 */
	public synchronized long delUserByEmail(String email) {
		return delData(TablesConstant.USER_TABLE_COLUMN_EMAIL + "=?", new String[] { email });
	}

	public ContentValues poToContentValues(UserInfo userInfo) {
		ContentValues values = new ContentValues();
		if (null != userInfo.getEmail()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_EMAIL, userInfo.getEmail());
		}
		if (null != userInfo.getId()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_USERID, userInfo.getId());
		}
		if (null != userInfo.getName()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_NAME, userInfo.getName());
		}
		if (null != userInfo.getLogintime()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_LOGINTIME, userInfo.getLogintime());
		}
		values.put(TablesConstant.USER_TABLE_COLUMN_REMEMBER, userInfo.isRemember());
		if (null != userInfo.getUserface()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_USERFACE, userInfo.getUserface());
		}
		if (null != userInfo.getMobile()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_MOBILE, userInfo.getMobile());
		}
		if (!TextUtils.isEmpty(userInfo.getSid())) {
			values.put(TablesConstant.USER_TABLE_COLUMN_SID, userInfo.getSid());
		}
		if (!TextUtils.isEmpty(userInfo.getAdSId())) {
			values.put(TablesConstant.USER_TABLE_COLUMN_ADSID, userInfo.getAdSId());
		}
		if (!TextUtils.isEmpty(userInfo.getTitle())) {
			values.put(TablesConstant.USER_TABLE_COLUMN_TITLE, userInfo.getTitle());
		}
		if (!TextUtils.isEmpty(userInfo.getCompany())) {
			values.put(TablesConstant.USER_TABLE_COLUMN_COMPANY, userInfo.getCompany());
		}
		if (null != userInfo.getLoginAccountType()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_LOGINACCOUNT, userInfo.getLoginAccountType());
		}
		if (userInfo.isImValid()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_IMVALID, 1);
		} else {
			values.put(TablesConstant.USER_TABLE_COLUMN_IMVALID, 0);
		}
		if (userInfo.getImId() > 0) {
			values.put(TablesConstant.USER_TABLE_COLUMN_IMOPENID, userInfo.getImId());
		}
		if (!TextUtils.isEmpty(userInfo.getLocation())) {
			values.put(TablesConstant.USER_TABLE_COLUMN_LOCATION, userInfo.getLocation());
		}
		values.put(TablesConstant.USER_TABLE_COLUMN_PRO, userInfo.getProv());
		values.put(TablesConstant.USER_TABLE_COLUMN_CITY, userInfo.getCity());
		if (!TextUtils.isEmpty(userInfo.getIndustry())) {
			values.put(TablesConstant.USER_TABLE_COLUMN_INDUSTRY, userInfo.getIndustry());
		}
		values.put(TablesConstant.USER_TABLE_COLUMN_ACCOUNT_TYPE, userInfo.getAccountType());
		if (userInfo.isRealName()) {
			values.put(TablesConstant.USER_TABLE_COLUMN_REALNAME, 1);
		} else {
			values.put(TablesConstant.USER_TABLE_COLUMN_REALNAME, 0);
		}
		return values;
	}

	public void contentValueToPo(Cursor cursor, UserInfo userInfo) {
		ContentValues values = new ContentValues();
		DatabaseUtils.cursorRowToContentValues(cursor, values);
		userInfo.setId(values.getAsLong(TablesConstant.USER_TABLE_COLUMN_USERID));
		userInfo.setName(values.getAsString(TablesConstant.USER_TABLE_COLUMN_NAME));
		userInfo.setPwd(values.getAsString(TablesConstant.USER_TABLE_COLUMN_PASSWORD));
		userInfo.setEmail(values.getAsString(TablesConstant.USER_TABLE_COLUMN_EMAIL));
		userInfo.setLogintime(values.getAsString(TablesConstant.USER_TABLE_COLUMN_LOGINTIME));
		userInfo.setRemember(values.getAsBoolean(TablesConstant.USER_TABLE_COLUMN_REMEMBER));
		userInfo.setUserface(values.getAsString(TablesConstant.USER_TABLE_COLUMN_USERFACE));
		userInfo.setLoginAccountType(values.getAsString(TablesConstant.USER_TABLE_COLUMN_LOGINACCOUNT));
		userInfo.setSid(values.getAsString(TablesConstant.USER_TABLE_COLUMN_SID));
		userInfo.setAdSId(values.getAsString(TablesConstant.USER_TABLE_COLUMN_ADSID));
		userInfo.setMobile(values.getAsString(TablesConstant.USER_TABLE_COLUMN_MOBILE));
		userInfo.setTitle(values.getAsString(TablesConstant.USER_TABLE_COLUMN_TITLE));
		userInfo.setCompany(values.getAsString(TablesConstant.USER_TABLE_COLUMN_COMPANY));
		userInfo.setLocation(values.getAsString(TablesConstant.USER_TABLE_COLUMN_LOCATION));
		if (values.getAsInteger(TablesConstant.USER_TABLE_COLUMN_IMVALID) == 1) {
			userInfo.setImValid(true);
		} else {
			userInfo.setImValid(false);
		}
		if (null != values.getAsLong(TablesConstant.USER_TABLE_COLUMN_IMOPENID)
				&& values.getAsLong(TablesConstant.USER_TABLE_COLUMN_IMOPENID) > 0) {
			userInfo.setImId(values.getAsInteger(TablesConstant.USER_TABLE_COLUMN_IMOPENID));
		}
		userInfo.setProv(values.getAsInteger(TablesConstant.USER_TABLE_COLUMN_PRO));
		userInfo.setCity(values.getAsInteger(TablesConstant.USER_TABLE_COLUMN_CITY));
		userInfo.setIndustry(values.getAsString(TablesConstant.USER_TABLE_COLUMN_INDUSTRY));
		userInfo.setAccountType(values.getAsInteger(TablesConstant.USER_TABLE_COLUMN_ACCOUNT_TYPE));
		if (values.getAsInteger(TablesConstant.USER_TABLE_COLUMN_REALNAME) == 1) {
			userInfo.setRealName(true);
		} else {
			userInfo.setRealName(false);
		}
	}

}
