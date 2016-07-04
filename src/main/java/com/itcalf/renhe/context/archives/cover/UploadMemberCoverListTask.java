package com.itcalf.renhe.context.archives.cover;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.UploadCover;
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
public class UploadMemberCoverListTask extends BaseAsyncTask<UploadCover> {
	private Context mContext;

	public UploadMemberCoverListTask(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	@Override
	public void doPre() {

	}

	@Override
	public void doPost(UploadCover result) {

	}

	@Override
	protected UploadCover doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);// 
		reqParams.put("adSId", params[1]);// 
		reqParams.put("profileCoverId", params[2]);// 
		try {
			UploadCover mb = (UploadCover) HttpUtil.doHttpRequest(Constants.Http.UPLOAD_MEMBER_COVER_LIST, reqParams,
					UploadCover.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
