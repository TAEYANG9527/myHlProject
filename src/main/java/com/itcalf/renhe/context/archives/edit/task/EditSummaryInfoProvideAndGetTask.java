package com.itcalf.renhe.context.archives.edit.task;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author       chan
 * @createtime   2014-12-23
 * @功能说明       获取能提供，想得到 ；合并接口
 */
public class EditSummaryInfoProvideAndGetTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;
	private String[] provideStrings;
	private String[] getStrings;

	public EditSummaryInfoProvideAndGetTask(Context mContext, String[] provideStrings, String[] getStrings) {
		super(mContext);
		this.mContext = mContext;
		this.provideStrings = provideStrings;
		this.getStrings = getStrings;
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
		reqParams.put("aimTags", getStrings);// 
		reqParams.put("preferredTags", provideStrings);// 
		try {
			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.EDIT_PROVIDE_GET, reqParams,
					MessageBoardOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
