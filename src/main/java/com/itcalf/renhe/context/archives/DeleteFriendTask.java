package com.itcalf.renhe.context.archives;

import android.content.Context;

import com.itcalf.renhe.BaseAsyncTask;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoardOperationWithErroInfo;
import com.itcalf.renhe.utils.HttpUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 好友添加 参数是被添加好友的SID
 * 
 * @author Administrator
 * 
 */
public abstract class DeleteFriendTask extends BaseAsyncTask<MessageBoardOperationWithErroInfo> {

	public DeleteFriendTask(Context mContext) {
		super(mContext);
	}

	@Override
	protected MessageBoardOperationWithErroInfo doInBackground(String... params) {
		Map<String, Object> reqParams = new HashMap<String, Object>();

		reqParams.put("sid", getMyApplication().getUserInfo().getSid());// 发起添加好友请求的会员sid
		reqParams.put("adSId", getMyApplication().getUserInfo().getAdSId());// 加密后用户id和密码的信息以后的每次请求中都要带上它
		reqParams.put("fsid", params[0]);// 被删除好友的会员sid

		try {
			MessageBoardOperationWithErroInfo mb = (MessageBoardOperationWithErroInfo) HttpUtil.doHttpRequest(Constants.Http.DELETE_FRIEND, reqParams,
					MessageBoardOperationWithErroInfo.class, null);
			return mb;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
