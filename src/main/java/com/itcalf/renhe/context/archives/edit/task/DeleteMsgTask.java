package com.itcalf.renhe.context.archives.edit.task;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: EditSummaryInfoProfessionTask.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014 <br>
 * Create DateTime: 2014-8-6 上午9:58:59 <br>
 * 
 * @author wangning
 */
public class DeleteMsgTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;
	private int type;

	public DeleteMsgTask(Context mContext, int type) {
		super(mContext);
		this.type = type;
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
		try {
			MessageBoardOperation mb;
			if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD) {
				reqParams.put("type", params[2]);//
				reqParams.put("messageBoardId", params[3]);//
				reqParams.put("messageBoardObjectId", params[4]);//
				mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.DELETE_RENMAIQUAN_MSG, reqParams,
						MessageBoardOperation.class, null);
			} else {
				reqParams.put("commentObjectId", params[4]);//
				mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.DELETE_UNMESSAGEBOARD_RENMAIQUAN_MSG,
						reqParams, MessageBoardOperation.class, null);
			}
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
