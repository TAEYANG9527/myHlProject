package com.itcalf.renhe.context.register;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: EditSummaryInfoSpecialtiesTask.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-8-6 上午9:58:59 <br>
 * @author wangning
 */
public class EditMemberPreferredAimTagsTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;
	private String[] preferredTags;
	private String[] aimTags;

	public EditMemberPreferredAimTagsTask(Context mContext, String[] preferredTags, String[] aimTags) {
		super(mContext);
		this.mContext = mContext;
		this.preferredTags = preferredTags;
		this.aimTags = aimTags;
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
		reqParams.put("aimTags", aimTags);// 
		reqParams.put("preferredTags", preferredTags);// 
		try {
			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil
					.doHttpRequest(Constants.Http.EDIT_MEMBER_PREFERRED_AIM_TAGS, reqParams, MessageBoardOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
