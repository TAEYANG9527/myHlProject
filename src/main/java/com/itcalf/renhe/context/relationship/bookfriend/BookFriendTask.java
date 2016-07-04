package com.itcalf.renhe.context.relationship.bookfriend;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.AddBookFriendOperation;
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
public class BookFriendTask extends BaseAsyncTask<AddBookFriendOperation> {
	private Context mContext;

	public BookFriendTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(AddBookFriendOperation result) {

	}

	@Override
	protected AddBookFriendOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("country", params[2]);// 
		reqParams.put("prov", params[3]);// 
		reqParams.put("city", params[4]);// 
		reqParams.put("industryParents", params[5]);// 
		reqParams.put("industry", params[6]);// 
		reqParams.put("keywords", params[7]);// 
		try {
			AddBookFriendOperation mb = (AddBookFriendOperation) HttpUtil.doHttpRequest(Constants.Http.BOOK_FRIEND, reqParams,
					AddBookFriendOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
