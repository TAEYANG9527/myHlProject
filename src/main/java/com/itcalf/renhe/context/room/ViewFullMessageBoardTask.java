package com.itcalf.renhe.context.room;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.ViewFullMessageBoard;
import com.itcalf.renhe.dto.ViewFullMessageBoard.ReplyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Feature:回复留言列表异步加载 Desc:回复留言列表异步加载
 * 
 * @author xp
 * 
 */
public class ViewFullMessageBoardTask extends AsyncTask<Object, Void, Object> {

	// 数据回调
	private IDataBack mDataBack;
	private Context mContext;
	private int type;

	public ViewFullMessageBoardTask(Context context, int type, IDataBack back) {
		super();
		this.mContext = context;
		this.mDataBack = back;
		this.type = type;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mDataBack.onPre();
	}

	// 后台线程调用服务端接口
	@Override
	protected Object doInBackground(Object... params) {
		try {
			if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD)
				return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand().viewFullMessageBoard(
						(String) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4],
						(Integer) params[5], mContext);
			else
				return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand().viewFullUnMessageBoard(
						(String) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4],
						(Integer) params[5], mContext);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(Object results) {
		super.onPostExecute(results);
		ViewFullMessageBoard result = (ViewFullMessageBoard) results;
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		if (null != result && 1 == result.getState()) {
			if (null != result.getReplyList() && result.getReplyList().length > 0) {
				for (ReplyList replyList : result.getReplyList()) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", replyList.getId());
					map.put("objectId", replyList.getObjectId());
					map.put("senderSid", replyList.getSenderSid());
					map.put("senderName", replyList.getSenderName());
					map.put("senderUserface", replyList.getSenderUserface());
					map.put("reSenderSid", replyList.getReSenderSid());
					map.put("reSenderMemberName", replyList.getReSenderMemberName());
					map.put("content", replyList.getContent());
					map.put("createdDateSeconds", replyList.getCreatedDateSeconds());
					resultList.add(map);
				}
			}
			mDataBack.onPost(resultList, result);
		} else if (null != result && -4 == result.getState()) {
			mDataBack.onPost(null, result);
		} else {
			mDataBack.onPost(null, null);
		}
	}

	interface IDataBack {

		void onPre();

		void onPost(List<Map<String, Object>> result, ViewFullMessageBoard viewFullMessageBoard);

	}

}
