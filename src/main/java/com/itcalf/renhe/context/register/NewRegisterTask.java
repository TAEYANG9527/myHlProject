package com.itcalf.renhe.context.register;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: NewRegisterTask.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2014    <br>
 * Create DateTime: 2014-6-26 上午9:25:56 <br>
 * @author wangning
 */
public class NewRegisterTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;

	public NewRegisterTask(Context mContext) {
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
		reqParams.put("mobile", params[0]);// 手机号码
		//		reqParams.put("password", params[1]);// 密码
		if (Constants.DEBUG_MODE) {
			String testid = String.valueOf(System.currentTimeMillis());
			reqParams.put("deviceInfo", testid);// 设备的唯一id，用于做短信验证码的发送数量控制
		} else {
			reqParams.put("deviceInfo", params[1]);// 设备的唯一id，用于做短信验证码的发送数量控制
		}
		try {
			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.SENDREGISTERVERIFICATION,
					reqParams, MessageBoardOperation.class, mContext);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
