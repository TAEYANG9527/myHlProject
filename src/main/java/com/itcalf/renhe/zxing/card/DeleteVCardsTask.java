package com.itcalf.renhe.zxing.card;

import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class DeleteVCardsTask extends AsyncTask<String, Integer, Integer> {

	private TaskListener listener;

	public DeleteVCardsTask(TaskListener listener) {
		this.listener = listener;
	}

	@Override
	protected Integer doInBackground(String... params) {

		Map<String, Object> reqParams = new HashMap<String, Object>();

		reqParams.put("sid", params[0]);
		reqParams.put("adSId", params[1]);
		reqParams.put("cardId", params[2]);

		try {
			DeleteVCardRes res = (DeleteVCardRes) HttpUtil.doHttpRequest(Constants.Http.DELETE_VCARDS, reqParams,
					DeleteVCardRes.class, null);
			return res.state;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	protected void onPostExecute(Integer state) {
		if (listener != null) {
			listener.postExecute(state);
		}
	}

	public interface TaskListener {
		public abstract void postExecute(int state);
	}

	class DeleteVCardRes {
		public int state;
	}
}
