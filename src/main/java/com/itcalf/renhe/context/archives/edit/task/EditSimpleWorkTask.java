package com.itcalf.renhe.context.archives.edit.task;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class EditSimpleWorkTask extends BaseAsyncTask<MessageBoardOperationWithErroInfo> {
	public EditSimpleWorkTask(Context mContext) {
		super(mContext);
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
		reqParams.put("title", params[2]);//
		reqParams.put("company", params[3]);//
		try {
			MessageBoardOperationWithErroInfo mb = (MessageBoardOperationWithErroInfo) HttpUtil.doHttpRequest(
					Constants.Http.EDIT_SIMPLE_WORK_INFO, reqParams, MessageBoardOperationWithErroInfo.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
