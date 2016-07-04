package com.itcalf.renhe.context.contacts;

import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @description 统计搜索结果的click事件
 * @author Chan
 * @date 2015-5-12
 */
public class RecordSearchmembersTask extends AsyncTask<String, Void, MessageBoardOperation> {
	@Override
	protected MessageBoardOperation doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", params[0]);
		reqParams.put("adSId", params[1]);
		reqParams.put("keyword", params[2]);
		reqParams.put("type", params[3]);
		reqParams.put("location", params[4]);
		try {
			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.RECORD_MEMBERCLICK,
					reqParams, MessageBoardOperation.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(MessageBoardOperation result) {
		super.onPostExecute(result);
		if (result != null) {
			switch (result.getState()) {
			case 1:
				break;
			case -3:
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}
}
