package com.itcalf.renhe.context.room;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.FoundMessageBoards;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.ContentInfo;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.NewNoticeList;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;

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
public class FoundRoomTask extends AsyncTask<Object, Void, FoundMessageBoards> {

	// 数据回调
	private IRoomBack mRoomBack;
	private Context mContext;

	public FoundRoomTask(Context context, IRoomBack back) {
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
	protected FoundMessageBoards doInBackground(Object... params) {
		try {
			return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand().getFoundMsgBoards(
					(String) params[0], (String) params[1], (String) params[2], (Integer) params[3], (Long) params[4],
					(Integer) params[5], (Integer) params[6], mContext);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.d(Constants.TAG, "CityTask", e);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(FoundMessageBoards result) {
		super.onPostExecute(result);
		if (null != result && 1 == result.getState()) {
			// 获取留言列表数据，封装后执行回调
			NewNoticeList[] newNoticeArray = result.getNewNoticeList();
			List<Map<String, Object>> newNoticeList = new ArrayList<Map<String, Object>>();
			if (null != newNoticeArray && newNoticeArray.length > 0) {
				for (int i = 0; i < newNoticeArray.length; i++) {
					NewNoticeList newNoticeListItem = newNoticeArray[i];
					if (null != newNoticeListItem) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("result", newNoticeListItem);
						map.put("type", newNoticeListItem.getType());
						SenderInfo senderInfo = newNoticeListItem.getSenderInfo();
						ContentInfo contentInfo = newNoticeListItem.getContentInfo();
						if (null != senderInfo) {
							map.put("sid", senderInfo.getSid());
							map.put("name", senderInfo.getName());
							map.put("userface", senderInfo.getUserface());
							map.put("title", senderInfo.getTitle());
							map.put("company", senderInfo.getCompany());
							map.put("industry", senderInfo.getIndustry());
							map.put("location", senderInfo.getLocation());
							map.put("accountType", senderInfo.getAccountType());
							map.put("isRealName", senderInfo.isRealname());
						}
						map.put("objectId", newNoticeListItem.getContentInfo().getObjectId());
						map.put("content", contentInfo.getContent());
						if (newNoticeListItem.getType() == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD) {
							if (null != contentInfo) {
								map.put("Id", newNoticeListItem.getContentInfo().getId());
								ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
								if (null != forwardMessageBoardInfo) {
									map.put("ForwardMessageBoardInfo_isForwardRenhe", forwardMessageBoardInfo.isForwardRenhe());// 是否是人和网的转发，是人和网的转发，会返回forwardMemberName、forwardMemberSId、forwardMessageBoardObjectId、forwardMessageBoardId
									map.put("ForwardMessageBoardInfo_ObjectId", forwardMessageBoardInfo.getObjectId());//被转发的客厅objectId
									map.put("ForwardMessageBoardInfo_Id", forwardMessageBoardInfo.getId());//被转发的客厅id
									map.put("ForwardMessageBoardInfo_Name", forwardMessageBoardInfo.getName());//转发者的姓名
									map.put("ForwardMessageBoardInfo_Sid", forwardMessageBoardInfo.getSid());// 转发者的sid
									map.put("ForwardMessageBoardInfo_Content", forwardMessageBoardInfo.getContent());// 转发的内容
									map.put("ForwardMessageBoardInfo_PicList", forwardMessageBoardInfo.getPicLists());// 图片列表信息
									map.put("ForwardMessageBoardInfo_AtMembers", forwardMessageBoardInfo.getAtMembers());// 留言内容中@信息 
								}
								map.put("atMembers", contentInfo.getAtMembers());
								map.put("replyNum", contentInfo.getReplyNum());
								map.put("replyList", contentInfo.getReplyList());
								map.put("likedList", contentInfo.getLikedList());
								map.put("likedNumber", contentInfo.getLikedNum());
								map.put("liked", contentInfo.isLiked());
								map.put("picList", contentInfo.getPicList());// 图片列表信息
							}
						} else if (newNoticeListItem.getType() == MessageBoards.MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE) {
							if (null != contentInfo.getPicList()) {
								map.put("picList", contentInfo.getPicList());// 图片列表信息
							}
							//							map.put("userfaceUrl", contentInfo.getUserfaceUrl());//头像url
						}
						map.put("createDate", newNoticeListItem.getCreatedDate());//人脉圈的创建毫秒数
						newNoticeList.add(map);

					}
				}
			}

			mRoomBack.doPost(newNoticeList, result);
			return;
		}
		mRoomBack.doPost(null, null);
	}

	public interface IRoomBack {

		void onPre();

		void doPost(List<Map<String, Object>> result, FoundMessageBoards msg);

	}
}
