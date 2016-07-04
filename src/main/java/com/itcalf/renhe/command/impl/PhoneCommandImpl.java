package com.itcalf.renhe.command.impl;

import android.content.Context;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.command.IPhoneCommand;
import com.itcalf.renhe.dto.Version;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.ManifestUtil;

import java.util.HashMap;
import java.util.Map;

public class PhoneCommandImpl implements IPhoneCommand {

	@Override
	public Version getLastedVersion(Context context) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("deviceType", Constants.PLATFORM_TYPE);
		params.put("channel", ManifestUtil.getUmengChannel(context));
		params.put("curVersion", ManifestUtil.getVersionName(context));
		return (Version) HttpUtil.doHttpRequest(Constants.Http.CHECK_VERION_UPDATE, params, Version.class, context);
	}

}
