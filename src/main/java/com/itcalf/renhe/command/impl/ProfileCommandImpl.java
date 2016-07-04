package com.itcalf.renhe.command.impl;

import android.content.Context;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.command.IProfileCommand;
import com.itcalf.renhe.dto.ContactProfile;
import com.itcalf.renhe.dto.Profile;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class ProfileCommandImpl implements IProfileCommand {

	@Override
	public Profile showProfile(String viewSId, String sid, String adSId, Context context) throws Exception {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("viewSId", viewSId);// 请求viewprofile的会员sid
		reqParams.put("sid", sid);
		reqParams.put("adSId", adSId);
		return (Profile) HttpUtil.doHttpRequest(Constants.Http.SHOW_PROFILE, reqParams, Profile.class, context);
	}

	@Override
	public ContactProfile showContactProfile(String viewSId, String sid, String adSId, Context context) throws Exception {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("viewSId", viewSId);// 请求viewprofile的会员sid
		reqParams.put("sid", sid);
		reqParams.put("adSId", adSId);
		return (ContactProfile) HttpUtil.doHttpRequest(Constants.Http.VIEW_CONTACT_PROFILE, reqParams, ContactProfile.class,
				context);
	}

	@Override
	public ContactProfile showIMContactProfile(String openId, String sid, String adSId, Context context) throws Exception {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("openid", openId);// 请求viewprofile的会员sid
		reqParams.put("sid", sid);
		reqParams.put("adSId", adSId);
		return (ContactProfile) HttpUtil.doHttpRequest(Constants.Http.VIEW_CONTACT_PROFILE_WITH_OPENID, reqParams,
				ContactProfile.class, context);
	}

	@Override
	public Profile showIMNoFriendsProfile(String openId, String sid, String adSId, Context context) throws Exception {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("openid", openId);// 根据openid请求非好友的个人档案
		reqParams.put("sid", sid);
		reqParams.put("adSId", adSId);
		return (Profile) HttpUtil.doHttpRequest(Constants.Http.VIEW_PROFILE_WITH_OPENID, reqParams, Profile.class, context);
	}
}
