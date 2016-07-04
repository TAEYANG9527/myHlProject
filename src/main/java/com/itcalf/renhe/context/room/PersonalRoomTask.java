package com.itcalf.renhe.context.room;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;
import com.itcalf.renhe.dto.PersonalMessageBoards;
import com.itcalf.renhe.dto.PersonalMessageBoards.NewNoticeList;
import com.itcalf.renhe.utils.DateUtil;

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
public class PersonalRoomTask extends AsyncTask<Object, Void, PersonalMessageBoards> {

	// 数据回调
	private IRoomBack mRoomBack;
	private Context mContext;
	private long lastVisibleItemTime = 0;//用来判断要显示的这条的时间是否与上条是同一天，如果是，则不显示时间
	private DateUtil dateUtil;

	public PersonalRoomTask(Context context, Long lastVisibleItemTime, IRoomBack back) {
		super();
		this.mContext = context;
		this.mRoomBack = back;
		this.dateUtil = new DateUtil();
		this.lastVisibleItemTime = lastVisibleItemTime;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mRoomBack.onPre();
	}

	// 后台线程调用服务端接口
	@Override
	protected PersonalMessageBoards doInBackground(Object... params) {
		try {
			return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand().getPersonRenmaiquanMsgBoards(
					(String) params[9], (String) params[0], (String) params[1], (Integer) params[7], (String) params[2],
					(Integer) params[3], (Long) params[4], (Long) params[5], (Long) params[6], (Integer) params[8], mContext);
		} catch (Exception e) {
			if (Constants.LOG) {
				Log.d(Constants.TAG, "CityTask", e);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(PersonalMessageBoards result) {
		super.onPostExecute(result);
		if (null != result && 1 == result.getState()) {
			// 获取留言列表数据，封装后执行回调
			NewNoticeList[] newNoticeArray = result.getNewMessageBoardList();
			List<Map<String, Object>> newNoticeList = new ArrayList<Map<String, Object>>();
			if (null != newNoticeArray && newNoticeArray.length > 0) {
				for (int i = 0; i < newNoticeArray.length; i++) {
					NewNoticeList newNoticeListItem = newNoticeArray[i];
					if (null != newNoticeListItem) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
						SenderInfo senderInfo = newNoticeListItem.getSenderInfo();
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
						map.put("objectId", newNoticeListItem.getObjectId());
						map.put("content", newNoticeListItem.getContent());
						map.put("Id", newNoticeListItem.getId());
						ForwardMessageBoardInfo forwardMessageBoardInfo = newNoticeListItem.getForwardMessageBoardInfo();
						if (null != forwardMessageBoardInfo) {
							map.put("ForwardMessageBoardInfo_isForwardRenhe", forwardMessageBoardInfo.isForwardRenhe());// 是否是人和网的转发，是人和网的转发，会返回forwardMemberName、forwardMemberSId、forwardMessageBoardObjectId、forwardMessageBoardId
							map.put("ForwardMessageBoardInfo_ObjectId", forwardMessageBoardInfo.getObjectId());//被转发的客厅objectId
							map.put("ForwardMessageBoardInfo_Id", forwardMessageBoardInfo.getId());//被转发的客厅id
							map.put("ForwardMessageBoardInfo_Name", forwardMessageBoardInfo.getName());//转发者的姓名
							map.put("ForwardMessageBoardInfo_Sid", forwardMessageBoardInfo.getSid());// 转发者的sid
							map.put("ForwardMessageBoardInfo_Content", forwardMessageBoardInfo.getContent());// 转发的内容
							map.put("ForwardMessageBoardInfo_PicList", forwardMessageBoardInfo.getPicLists());// 图片列表信息
							map.put("ForwardMessageBoardInfo_AtMembers", forwardMessageBoardInfo.getAtMembers());// 留言内容中@信息 

							map.put("ForwardMessageBoardInfo_Type", forwardMessageBoardInfo.getType());// 留言内容中为链接 
							switch (forwardMessageBoardInfo.getType()) {
							case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
								map.put("ForwardMessageBoardInfo_Url", forwardMessageBoardInfo.getWebsShare().getUrl());// 留言内容中@信息 
								map.put("ForwardMessageBoardInfo_PicUrl", forwardMessageBoardInfo.getWebsShare().getPicUrl());
								map.put("ForwardMessageBoardInfo_Content", forwardMessageBoardInfo.getWebsShare().getContent());// 转发的内容
								break;
							case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
								map.put("ForwardMessageBoardInfo_share_sid", forwardMessageBoardInfo.getProfileShare().getSid());// 留言内容中@信息 
								map.put("ForwardMessageBoardInfo_share_name",
										forwardMessageBoardInfo.getProfileShare().getName());
								map.put("ForwardMessageBoardInfo_share_job", forwardMessageBoardInfo.getProfileShare().getJob());// 转发的内容
								map.put("ForwardMessageBoardInfo_share_company",
										forwardMessageBoardInfo.getProfileShare().getCompany());// 转发的内容
								map.put("ForwardMessageBoardInfo_share_picUrl",
										forwardMessageBoardInfo.getProfileShare().getPicUrl());// 转发的内容
								break;
							case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:
								map.put("ForwardMessageBoardInfo_share_id",
										forwardMessageBoardInfo.getCircleShare().getId() + "");// 留言内容中@信息 
								map.put("ForwardMessageBoardInfo_share_name", forwardMessageBoardInfo.getCircleShare().getName());// 留言内容中@信息 
								map.put("ForwardMessageBoardInfo_share_note", forwardMessageBoardInfo.getCircleShare().getNote());
								map.put("ForwardMessageBoardInfo_share_picUrl",
										forwardMessageBoardInfo.getCircleShare().getPicUrl());// 转发的内容
								break;
							default:
								break;
							}
						}
						map.put("atMembers", newNoticeListItem.getAtMembers());
						map.put("replyNum", newNoticeListItem.getReplyNum());
						map.put("likedNumber", newNoticeListItem.getLikedNum());
						map.put("liked", newNoticeListItem.isLiked());
						map.put("picList", newNoticeListItem.getPicList());// 图片列表信息
						map.put("createDate", newNoticeListItem.getCreatedDateSeconds());//人脉圈的创建毫秒数
						if (!dateUtil.areSameDay(newNoticeListItem.getCreatedDateSeconds(), lastVisibleItemTime)) {
							map.put("isNeedShowTime", true);
						}
						lastVisibleItemTime = newNoticeListItem.getCreatedDateSeconds();
						newNoticeList.add(map);

					}
				}
			}

			mRoomBack.doPost(newNoticeList, result, lastVisibleItemTime);
			return;
		}
		mRoomBack.doPost(null, null, lastVisibleItemTime);
	}

	public interface IRoomBack {

		void onPre();

		void doPost(List<Map<String, Object>> result, PersonalMessageBoards msg, Long lastVisibleItemTime);

	}
}
