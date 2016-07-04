package com.itcalf.renhe.context.relationship;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.NearbyPeople;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

public class NearbyTask extends AsyncTask<Object, Void, NearbyPeople> {
	private IDataBack mDataBack;
	private Context mContext;

	public NearbyTask(Context context, IDataBack back) {
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
	protected void onPostExecute(NearbyPeople result) {
		super.onPostExecute(result);
		if (null != result) {
			//			if (1 == result.getState()) {
			//				if (null != result.getMemberList() && result.getMemberList().length > 0) {
			//					List<Map<String, Object>> rsList = new ArrayList<Map<String, Object>>();
			//					for (int i = 0; i < result.getMemberList().length; i++) {
			//						final Map<String, Object> map = new LinkedHashMap<String, Object>();
			//						map.put("avatar_path", result.getMemberList()[i].getAvatar());
			//						map.put("memberId", result.getMemberList()[i].getMemberId());
			//						map.put("sid", result.getMemberList()[i].getSid());
			//						map.put("name", result.getMemberList()[i].getName());
			//						map.put("title", result.getMemberList()[i].getCurTitle());
			//						map.put("company", result.getMemberList()[i].getCurCompany());
			//						map.put("industry", result.getMemberList()[i].getIndustry());
			//						map.put("distance", result.getMemberList()[i].getDistance());
			//						rsList.add(map);
			//					}
			//					mDataBack.onPost(rsList);
			//					return;
			//				}
			//			}
			mDataBack.onPost(result);
			return;
		}
		mDataBack.onPost(null);
	}

	@Override
	protected NearbyPeople doInBackground(Object... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getSid());
		reqParams.put("adSId", ((RenheApplication) mContext.getApplicationContext()).getUserInfo().getAdSId());
		reqParams.put("industryId", params[0]);
		reqParams.put("lat", params[1]);
		reqParams.put("lng", params[2]);
		reqParams.put("cityName", params[3]);
		reqParams.put("pageNo", params[4]);
		reqParams.put("pageSize", params[5]);
		try {
			NearbyPeople mb = (NearbyPeople) HttpUtil.doHttpRequest(Constants.Http.NEARBY_PEOPLE, reqParams, NearbyPeople.class,
					mContext);
			return mb;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public interface IDataBack {

		void onPre();

		//		void onPost(List<Map<String, Object>> result);
		void onPost(NearbyPeople result);
	}
}
