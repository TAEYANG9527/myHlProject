package com.itcalf.renhe.context.register;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.Recommends;
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
public class AddRecommendIndustrysTask extends BaseAsyncTask<Recommends> {
	private Context mContext;
	private String[] strings;

	public AddRecommendIndustrysTask(Context mContext, String[] strings) {
		super(mContext);
		this.mContext = mContext;
		this.strings = strings;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(Recommends result) {

	}

	@Override
	protected Recommends doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("categoryIds", strings);// 
		try {
			Recommends mb = (Recommends) HttpUtil.doHttpRequest(Constants.Http.ADD_RECOMMEND_INDUSTRYS, reqParams,
					Recommends.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
