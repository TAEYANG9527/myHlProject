package com.itcalf.renhe.context.room;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.NewMessageBoards;
import com.itcalf.renhe.dto.NewMessageBoards.NewMessageBoardList;
import com.itcalf.renhe.dto.NewMessageBoards.NewMessageBoardList.ReplyInfo;
import com.itcalf.renhe.dto.NewMessageBoards.NewMessageBoardList.SourceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature:留言列表异步加载 Desc:留言列表异步加载
 * 
 * @author xp
 * 
 */
public class RoomNewMsgTask extends AsyncTask<Object, Void, NewMessageBoards> {

	// 数据回调
	private IRoomBack mRoomBack;
	private Context mContext;

	public RoomNewMsgTask(Context context, IRoomBack back) {
		super();
		this.mContext = context;
		this.mRoomBack = back;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mRoomBack.onPre();
	}

	// 后台线程调用服务端接口
	@Override
	protected NewMessageBoards doInBackground(Object... params) {
		try {
			return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand().unReadNewMsg((String) params[0],
					(String) params[1], mContext);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.d(Constants.TAG, "RoomNewMsgTask", e);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(NewMessageBoards result) {
		super.onPostExecute(result);
		if (null != result && 1 == result.getState()) {
			// 获取留言列表数据，封装后执行回调
			NewMessageBoardList[] mbList = result.getNewMessageBoardList();
			List<Map<String, Object>> weiboList = new ArrayList<Map<String, Object>>();
			if (null != mbList && mbList.length > 0) {
				for (int i = 0; i < mbList.length; i++) {
					NewMessageBoardList mb = mbList[i];
					ReplyInfo replyInfo = mb.getReplyInfo();
					SourceInfo sourceInfo = mb.getSourceInfo();
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("type", mb.getType());
					map.put("noticeType", mb.getNoticeType());
					map.put("unreadObjectId", mb.getObjectId());
					map.put("notifyObjectId", mb.getNotifyObjectId());
					if (null != sourceInfo) {
						map.put("sourceObjectId", sourceInfo.getObjectId());
						map.put("sourceContent", sourceInfo.getContent());
					}
					if (null != replyInfo) {
						map.put("userface", replyInfo.getUserface());
						map.put("senderSid", replyInfo.getSid());
						map.put("senderUsername", replyInfo.getName());
						map.put("datetime", replyInfo.getCreatedDate());
						map.put("replyContent", replyInfo.getReplyContent());
						map.put("client", "来自" + replyInfo.getFromSource());
					}
					map.put("avatar", R.drawable.avatar);
					map.put("accountType", replyInfo.getAccountType());
					map.put("isRealName", replyInfo.isRealName());
					weiboList.add(map);
				}
			}
			mRoomBack.doPost(weiboList, result);
			return;
		}
		mRoomBack.doPost(null, null);
	}

	interface IRoomBack {

		void onPre();

		void doPost(List<Map<String, Object>> result, NewMessageBoards msg);

	}
}
