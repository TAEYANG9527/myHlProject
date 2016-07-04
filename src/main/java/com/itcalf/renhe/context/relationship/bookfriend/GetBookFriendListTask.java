package com.itcalf.renhe.context.relationship.bookfriend;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.BookFriendOperation;
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
public class GetBookFriendListTask extends BaseAsyncTask<BookFriendOperation> {
	private Context mContext;

	public GetBookFriendListTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(BookFriendOperation result) {

	}

	@Override
	protected BookFriendOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		try {
			BookFriendOperation mb = (BookFriendOperation) HttpUtil.doHttpRequest(Constants.Http.GET_BOOK_FRIEND_LIST, reqParams,
					BookFriendOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
