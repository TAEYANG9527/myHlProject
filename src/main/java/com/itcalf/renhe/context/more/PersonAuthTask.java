package com.itcalf.renhe.context.more;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.PersonAuth;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: EditSummaryInfoProfessionTask.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-8-6 上午9:58:59 <br>
 * @author wangning
 */
public class PersonAuthTask extends BaseAsyncTask<PersonAuth> {
	private Context mContext;

	public PersonAuthTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(PersonAuth result) {

	}

	@Override
	protected PersonAuth doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		try {
			PersonAuth mb = (PersonAuth) HttpUtil.doHttpRequest(Constants.Http.PERSONAL_AUTH, reqParams, PersonAuth.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
