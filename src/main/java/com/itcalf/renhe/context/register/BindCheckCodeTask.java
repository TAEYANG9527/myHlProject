package com.itcalf.renhe.context.register;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: CheckCodeTask.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-26 上午9:25:56 <br>
 * @author wangning
 */
public class BindCheckCodeTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;

	public BindCheckCodeTask(Context mContext) {
		super(mContext);
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
		reqParams.put("mobile", params[2]);// 手机号码
		reqParams.put("code", params[3]);// 验证码
		int flag = Integer.parseInt(params[4]);
		MessageBoardOperation mb;
		try {
			if (flag == 0) {//绑定手机号
				mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.BINDMOBILE, reqParams,
						MessageBoardOperation.class, null);
			} else {//修改手机号
				mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.MODIFYMOBILE, reqParams,
						MessageBoardOperation.class, null);
			}

			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
