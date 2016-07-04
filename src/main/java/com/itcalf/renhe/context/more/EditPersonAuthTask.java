package com.itcalf.renhe.context.more;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
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
public class EditPersonAuthTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;

	public EditPersonAuthTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(MessageBoardOperation result) {

	}

	@Override
	protected MessageBoardOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("type", params[2]);// 
		reqParams.put("referralType", params[3]);// 
		reqParams.put("lietouBeContact", params[4]);// 
		reqParams.put("seoCannotEmbody", params[5]);// 
		reqParams.put("stealthViewProfile", params[6]);// 
		reqParams.put("vipViewFullProfile", params[7]);// 
		try {
			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.EDIT_PERSONAL_AUTH,
					reqParams, MessageBoardOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
