package com.itcalf.renhe.context.archives.edit.task;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo;
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
public class DeleteEduInfoTask extends BaseAsyncTask<MessageBoardOperationWithErroInfo> {
	private Context mContext;

	public DeleteEduInfoTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(MessageBoardOperationWithErroInfo result) {

	}

	@Override
	protected MessageBoardOperationWithErroInfo doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("id", params[2]);// 
		try {
			MessageBoardOperationWithErroInfo mb = (MessageBoardOperationWithErroInfo) HttpUtil
					.doHttpRequest(Constants.Http.DELETE_EDU_INFO, reqParams, MessageBoardOperationWithErroInfo.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
