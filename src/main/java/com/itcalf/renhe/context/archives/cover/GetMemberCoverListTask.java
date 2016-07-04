package com.itcalf.renhe.context.archives.cover;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MemberCoverOperation;
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
public class GetMemberCoverListTask extends BaseAsyncTask<MemberCoverOperation> {
	private Context mContext;

	public GetMemberCoverListTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(MemberCoverOperation result) {

	}

	@Override
	protected MemberCoverOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		try {
			MemberCoverOperation mb = (MemberCoverOperation) HttpUtil.doHttpRequest(Constants.Http.MEMBER_COVER_LIST, reqParams,
					MemberCoverOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
