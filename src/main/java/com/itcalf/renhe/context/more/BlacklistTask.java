package com.itcalf.renhe.context.more;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.Blacklist;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BlacklistTask extends AsyncTask<String, Void, Blacklist> {
	private IDataBack mDataBack;
	private Context mContext;

	public BlacklistTask(Context context, IDataBack back) {
		super();
		this.mContext = context;
		this.mDataBack = back;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mDataBack.onPre();
	}

	@Override
	protected Blacklist doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getSid());
		reqParams.put("adSId", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getAdSId());
		try {
			Blacklist bl = (Blacklist) HttpUtil.doHttpRequest(Constants.Http.BLACKLIST, reqParams, Blacklist.class, mContext);
			return bl;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Blacklist result) {
		super.onPostExecute(result);
		if (null != result) {
			if (1 == result.getState()) {
				if (null != result.getBlockedMemberList() && result.getBlockedMemberList().length > 0) {
					List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < result.getBlockedMemberList().length; i++) {
						final Map<String, Object> map = new LinkedHashMap<String, Object>();
						map.put("avatar_path", result.getBlockedMemberList()[i].getUserface());
						map.put("sid", result.getBlockedMemberList()[i].getSid());
						map.put("name", result.getBlockedMemberList()[i].getName());
						map.put("title", result.getBlockedMemberList()[i].getCurTitle());
						map.put("company", result.getBlockedMemberList()[i].getCurCompany());
						rsList.add(map);
					}
					mDataBack.onPost(rsList);
					return;
				}
			}
		}
		mDataBack.onPost(null);
	}

	public interface IDataBack {

		void onPre();

		void onPost(List<Map<String, Object>> result);

	}
}
