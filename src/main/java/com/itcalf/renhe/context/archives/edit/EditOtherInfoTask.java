package com.itcalf.renhe.context.archives.edit;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Renhe on 2015/7/14.
 * @author chong
 */
public class EditOtherInfoTask extends BaseAsyncTask<MessageBoardOperation> {
	private Context mContext;

	public EditOtherInfoTask(Context mContext) {
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
		reqParams.put("sid", params[0]);
		reqParams.put("adSId", params[1]);
		reqParams.put("associations", params[2]);
		reqParams.put("interests", params[3]);
		reqParams.put("awards", params[4]);
		try {
			return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.EDIT_ALL_OTHER_INFO, reqParams,
					MessageBoardOperation.class, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}