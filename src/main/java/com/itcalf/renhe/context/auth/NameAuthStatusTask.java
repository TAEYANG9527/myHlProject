package com.itcalf.renhe.context.auth;

import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.bean.NameAuthStatusRes;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class NameAuthStatusTask extends AsyncTask<String, Integer, NameAuthStatusRes> {

	private NameAuthStatusTaskListener listener;

	public NameAuthStatusTask(NameAuthStatusTaskListener listener) {
		this.listener = listener;
	}

	@Override
	protected NameAuthStatusRes doInBackground(String... params) {

		Map<String, Object> reqParams = new HashMap<String, Object>();

		reqParams.put("sid", params[0]);
		reqParams.put("adSId", params[1]);

		try {
			NameAuthStatusRes res = (NameAuthStatusRes) HttpUtil.doHttpRequest(Constants.Http.REALNAME_AUTH_STATUS, reqParams,
					NameAuthStatusRes.class, null);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(NameAuthStatusRes result) {
		if (listener != null) {
			listener.postExecute(result);
		}
	}

	public interface NameAuthStatusTaskListener {
		public abstract void postExecute(NameAuthStatusRes result);
	}

}
