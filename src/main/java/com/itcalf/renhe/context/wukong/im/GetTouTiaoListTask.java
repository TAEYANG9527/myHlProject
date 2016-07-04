package com.itcalf.renhe.context.wukong.im;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.TouTiaoOperation;
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
public class GetTouTiaoListTask extends BaseAsyncTask<TouTiaoOperation> {
	public GetTouTiaoListTask(Context mContext) {
		super(mContext);
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(TouTiaoOperation result) {

	}

	@Override
	protected TouTiaoOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("type", params[2]);// 
		reqParams.put("maxUpdatedDate", params[3]);// 
		reqParams.put("minUpdatedDate", params[4]);// 
		try {
			TouTiaoOperation mb = (TouTiaoOperation) HttpUtil.doHttpRequest(Constants.Http.GET_TOUTIAO_LIST, reqParams,
					TouTiaoOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
