package com.itcalf.renhe.context.wukong.im;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.IMInnerMsgConversationListOperation;
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
public class GetInnermsgConversationListTask extends BaseAsyncTask<IMInnerMsgConversationListOperation> {
	private Context mContext;

	public GetInnermsgConversationListTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(IMInnerMsgConversationListOperation result) {

	}

	@Override
	protected IMInnerMsgConversationListOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("page", params[2]);// 
		reqParams.put("pageCount", params[3]);// 
		try {
			IMInnerMsgConversationListOperation mb = (IMInnerMsgConversationListOperation) HttpUtil.doHttpRequest(
					Constants.Http.GET_IM_INNERMSG_LIST, reqParams, IMInnerMsgConversationListOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
