package com.itcalf.renhe.context.room;

import android.content.Context;
import android.os.AsyncTask;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MsgComments;
import com.itcalf.renhe.dto.MsgComments.CommentList;

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
public class ReplyListTask extends AsyncTask<Object, Void, MsgComments> {

	// 数据回调
	private IDataBack mDataBack;
	private Context mContext;
	private int type;
	// 显示内容字段
	private String[] mFrom = new String[] { "titleTv", "infoTv", "timeTv", "objectId", "userFace", "accountType", "isRealName" };

	public ReplyListTask(Context context, int type, IDataBack back) {
		super();
		this.type = type;
		this.mContext = context;
		this.mDataBack = back;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mDataBack.onPre();
	}

	// 后台线程调用服务端接口
	@Override
	protected MsgComments doInBackground(Object... params) {
		try {
			if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD)
				return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand().getMsgComments(
						(String) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Integer) params[4],
						mContext);
			else
				return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand()
						.getUnMessageBoardMsgComments((String) params[0], (String) params[1], (String) params[2],
								(Integer) params[3], (Integer) params[4], mContext);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	@Override
	protected void onPostExecute(MsgComments result) {
		super.onPostExecute(result);
		if (null != result && 1 == result.getState()) {
			// 获取回复留言列表数据，封装后执行回调
			CommentList[] commentList = result.getCommentList();
			if (null != commentList && commentList.length > 0) {
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < commentList.length; i++) {
					CommentList comment = commentList[i];
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("id", comment.getId());
					map.put("objectId", comment.getObjectId());
					map.put("senderSid", comment.getSenderSid());
					map.put("senderName", comment.getSenderName());
					map.put("senderUserface", comment.getSenderUserFace());
					map.put("reSenderSid", comment.getReSenderSid());
					map.put("reSenderMemberName", comment.getReSenderMemberName());
					map.put("content", comment.getContent());
					map.put("createdDateSeconds", comment.getCreatedDateSeconds());
					list.add(map);
				}
				mDataBack.onPost(list, result.getCommentNum());
			} else {
				mDataBack.onPost(null, 0);
			}
		}
	}

	interface IDataBack {

		void onPre();

		void onPost(List<Map<String, Object>> result, int replyNum);

	}

}
