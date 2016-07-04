package com.itcalf.renhe.context.innermsg;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.ReceiveAddFriend;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 三个参数
 * @author Administrator
 *
 */
public abstract class ReceiveFriend extends BaseAsyncTask<ReceiveAddFriend> {

	public ReceiveFriend(Context mContext) {
		super(mContext);
	}

	@Override
	protected ReceiveAddFriend doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("sid", getMyApplication().getUserInfo().getSid());// 发起添加好友请求的会员sid
		reqParams.put("inviteId", params[0]);// 邀请的id对应站内信中的邀请id
		reqParams.put("inviteType", params[1]);// 邀请的类型 对应站内信中的邀请类型
		reqParams.put("acceptType", params[2]);// 同意还是不同意好友请求 true为同意，false为不同意
		reqParams.put("adSId", getMyApplication().getUserInfo().getAdSId());// 加密后用户id和密码的信息 以后的每次请求中都要带上它
		try {
			ReceiveAddFriend mb = (ReceiveAddFriend) HttpUtil.doHttpRequest(Constants.Http.RECEIVEADDFRIEND, reqParams,
					ReceiveAddFriend.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
