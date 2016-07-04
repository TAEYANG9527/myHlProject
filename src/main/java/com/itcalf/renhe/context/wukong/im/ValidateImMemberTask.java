package com.itcalf.renhe.context.wukong.im;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.RegisterImMemberOperation;
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
public class ValidateImMemberTask extends BaseAsyncTask<RegisterImMemberOperation> {
	private Context mContext;

	public ValidateImMemberTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(RegisterImMemberOperation result) {

	}

	@Override
	protected RegisterImMemberOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		try {
			RegisterImMemberOperation mb = (RegisterImMemberOperation) HttpUtil.doHttpRequest(Constants.Http.REGISTER_IM_MEMBER,
					reqParams, RegisterImMemberOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
