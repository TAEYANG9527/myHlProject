package com.itcalf.renhe.context.register;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.Recommends;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 好友添加  参数是被添加好友的SID
 * @author Administrator
 *
 */
public abstract class RecommendTask extends BaseAsyncTask<Recommends> {

	public RecommendTask(Context mContext) {
		super(mContext);
	}

	@Override
	protected Recommends doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", getMyApplication().getUserInfo().getSid());// 发起添加好友请求的会员sid
		reqParams.put("adSId", getMyApplication().getUserInfo().getAdSId());// 加密后用户id和密码的信息以后的每次请求中都要带上它
		try {
			Recommends mb = (Recommends) HttpUtil.doHttpRequest(Constants.Http.GET_REGISTER_RECOMMEND, reqParams,
					Recommends.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
