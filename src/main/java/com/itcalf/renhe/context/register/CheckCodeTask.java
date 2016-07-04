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
 * Description: <br>验证密码及保存信息
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-26 上午9:25:56 <br>
 * @author wangning
 */
public class CheckCodeTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;

	public CheckCodeTask(Context mContext) {
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
		Map<String, Object> reqParams = new HashMap<>();
		reqParams.put("mobile", params[0]);// 手机号码
		reqParams.put("code", params[1]);// 验证码
		reqParams.put("password", params[2]);//密码
		try {
			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.VERIFICATIONREGISTER_V7,
					reqParams, MessageBoardOperation.class, mContext);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
