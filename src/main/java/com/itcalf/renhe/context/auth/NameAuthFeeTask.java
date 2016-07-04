package com.itcalf.renhe.context.auth;

import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.bean.NameAuthFeeRes;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class NameAuthFeeTask extends AsyncTask<String, Integer, NameAuthFeeRes> {

	private NameAuthFeeTaskListener listener;

	public NameAuthFeeTask(NameAuthFeeTaskListener listener) {
		this.listener = listener;
	}

	@Override
	protected NameAuthFeeRes doInBackground(String... params) {

		Map<String, Object> reqParams = new HashMap<String, Object>();

		reqParams.put("sid", params[0]);
		reqParams.put("adSId", params[1]);
		try {
			NameAuthFeeRes res = (NameAuthFeeRes) HttpUtil.doHttpRequest(Constants.Http.REALNAME_AUTH_FEE, reqParams,
					NameAuthFeeRes.class, null);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(NameAuthFeeRes result) {
		if (listener != null) {
			listener.postExecute(result);
		}
	}

	public interface NameAuthFeeTaskListener {
		public abstract void postExecute(NameAuthFeeRes result);
	}

}
