package com.itcalf.renhe.context.wukong.im;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.LoadConversationInfo;
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
public class LoadConversationInfoTask extends BaseAsyncTask<LoadConversationInfo> {
	private Context mContext;

	public LoadConversationInfoTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(LoadConversationInfo result) {

	}

	@Override
	protected LoadConversationInfo doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("lastLoadTime", params[2]);// 
		try {
			LoadConversationInfo mb = (LoadConversationInfo) HttpUtil.doHttpRequest(Constants.Http.LOAD_CONVERSATION_INFO,
					reqParams, LoadConversationInfo.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
