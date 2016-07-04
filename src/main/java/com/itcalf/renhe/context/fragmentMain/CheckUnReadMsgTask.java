package com.itcalf.renhe.context.fragmentMain;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.UnReadMsg;
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
public class CheckUnReadMsgTask extends BaseAsyncTask<UnReadMsg> {
	private Context mContext;

	public CheckUnReadMsgTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(UnReadMsg result) {

	}

	@Override
	protected UnReadMsg doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("maxCreatedDate", params[2]);// 
		try {
			UnReadMsg mb = (UnReadMsg) HttpUtil.doHttpRequest(Constants.Http.CHECK_UNREAD_MSG, reqParams, UnReadMsg.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
