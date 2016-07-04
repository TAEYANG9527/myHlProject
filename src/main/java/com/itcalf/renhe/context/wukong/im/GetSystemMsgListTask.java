package com.itcalf.renhe.context.wukong.im;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.ConversationSystemMsgOperation;
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
public class GetSystemMsgListTask extends BaseAsyncTask<ConversationSystemMsgOperation> {
	private Context mContext;

	public GetSystemMsgListTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(ConversationSystemMsgOperation result) {

	}

	@Override
	protected ConversationSystemMsgOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("page", params[2]);// 
		reqParams.put("pageCount", params[3]);// 
		try {
			ConversationSystemMsgOperation mb = (ConversationSystemMsgOperation) HttpUtil
					.doHttpRequest(Constants.Http.LIST_SYSTEM_MESSAGE, reqParams, ConversationSystemMsgOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
