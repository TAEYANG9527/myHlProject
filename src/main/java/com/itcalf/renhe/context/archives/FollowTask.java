package com.itcalf.renhe.context.archives;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public abstract class FollowTask extends AsyncTask<String, Void, Integer> {
	private Context mContext;

	public FollowTask(Context context) {
		super();
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		doPre();
	}

	public abstract void doPre();

	@Override
	protected Integer doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getSid());
		reqParams.put("followerSId", params[1]);
		reqParams.put("adSId", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getAdSId());
		try {

			MessageBoardOperation mb = (MessageBoardOperation) HttpUtil.doHttpRequest(params[0], reqParams,
					MessageBoardOperation.class, mContext);
			if (mb != null)
				return mb.getState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		doPost(result);
	}

	public abstract void doPost(Integer result);
}