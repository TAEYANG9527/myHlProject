package com.itcalf.renhe.context.more;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.SelfTwoDimenCodeMessageBoardOperation;
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
public class GetSelfTwoDimenCodeTask extends BaseAsyncTask<SelfTwoDimenCodeMessageBoardOperation> {
	private Context mContext;

	public GetSelfTwoDimenCodeTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(SelfTwoDimenCodeMessageBoardOperation result) {

	}

	@Override
	protected SelfTwoDimenCodeMessageBoardOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("viewSId", params[2]);// 
		try {
			SelfTwoDimenCodeMessageBoardOperation mb = (SelfTwoDimenCodeMessageBoardOperation) HttpUtil.doHttpRequest(
					Constants.Http.GET_PROFILE_QRCODE, reqParams, SelfTwoDimenCodeMessageBoardOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
