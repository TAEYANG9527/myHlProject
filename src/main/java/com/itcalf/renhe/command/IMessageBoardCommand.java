package com.itcalf.renhe.command;

import android.content.Context;

import com.itcalf.renhe.bean.CirCleInfo;
import com.itcalf.renhe.bean.CircleJoinINfo;
import com.itcalf.renhe.dto.AddMessageBoard;
import com.itcalf.renhe.dto.CircleAvator;
import com.itcalf.renhe.dto.CircleQrcodeInfo;
import com.itcalf.renhe.dto.FoundMessageBoards;
import com.itcalf.renhe.dto.MessageBoardDetail;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MsgComments;
import com.itcalf.renhe.dto.NewMessageBoards;
import com.itcalf.renhe.dto.PersonalMessageBoards;
import com.itcalf.renhe.dto.ReplyMessageBoard;
import com.itcalf.renhe.dto.ReplyUnMessageBoard;
import com.itcalf.renhe.dto.UnReadMsgNum;
import com.itcalf.renhe.dto.ViewFullMessageBoard;

public interface IMessageBoardCommand {

	public static final int SELF_MSG_BOARD = 1;
	public static final int FRIEND_MSG_BOARD = 2;
	public static final int INDUSTRY_MSG_BOARD = 3;
	public static final int CITY_MSG_BOARD = 4;
	public static final int FOLLOW_MSG_BOARD = 5;
	public static final int PERSONAL_MESSAGEBOARDS = 6;

	/**
	 * 
	 * @param sid
	 *            会员加密后的id用于请求客厅留言
	 * @param adSId
	 *            加密后用户id和密码的信息 以后的每次请求中都要带上它
	 * @param type
	 *            取值1、2、3、4、5
	 * @param cls
	 *            取值必须为more 更多,new 更新, renew 强制取最新
	 * @param count
	 *            取数据的count
	 * @param minCreateDate
	 *            Integer 最小的createDate
	 * @param maxCreateDate
	 *            Integer 最大的createDate
	 * @return
	 * @throws Exception
	 */
	MessageBoards getMsgBoards(String adSId, String sid, String type, Integer count, Long minCreateDate,
			Long maxCreateDate,int androidPhotoType, Context context) throws Exception;

	/**
	 * 获取信息详情
	 * 
	 * @param objectId
	 *            messageBoard的objectId用于请求客厅留言的详细页面
	 * @param adSId
	 * @param sid
	 * @return
	 * @throws Exception
	 */
	MessageBoardDetail getMsgBoradDetail(String objectId, String adSId, String sid, Context context) throws Exception;

	/**
	 * 获取留言列表
	 * 
	 * @param objectId
	 *            messageBoard的objectId用于请求客厅留言的详细页面
	 * @param adSId
	 * @param sid
	 * @param start
	 * @param count
	 * @return
	 * @throws Exception
	 */
	MsgComments getMsgComments(String objectId, String adSId, String sid, int start, int count, Context context) throws Exception;

	/**
	 * 获取非MessageBoard类型留言列表
	 * 
	 * @param objectId
	 *            messageBoard的objectId用于请求客厅留言的详细页面
	 * @param adSId
	 * @param sid
	 * @param start
	 * @param count
	 * @return
	 * @throws Exception
	 */
	MsgComments getUnMessageBoardMsgComments(String objectId, String adSId, String sid, int start, int count, Context context)
			throws Exception;

	/**
	 * 发布留言
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param content
	 * @return
	 * @throws Exception
	 */
	AddMessageBoard publicMessageBoard(String adSId, String sid, String content, String atMembers, int photoNum, Context context)
			throws Exception;

	/**
	 * 回复留言
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param messageBoardId
	 * @param messageBoardObjectId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	ReplyMessageBoard replyMessageBoard(String adSId, String sid, String messageBoardId, String messageBoardObjectId,
			String content, boolean forwardMessageBoard, String replyMessageBoardId, String replyMessageBoardObjectId,
			Context context) throws Exception;

	/**
	 * 回复非MessageBoard类型留言
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param messageBoardId
	 * @param messageBoardObjectId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	ReplyUnMessageBoard replyUnMessageBoard(String adSId, String sid, String messageBoardId, String messageBoardObjectId,
			String content, boolean forwardMessageBoard, String replyMessageBoardId, String replyMessageBoardObjectId,
			Context context) throws Exception;

	/**
	 * 转发留言
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param messageBoardObjectId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public MessageBoardOperation forwardMessageBoard(String adSId, String sid, String messageBoardObjectId, String content,
			String atMembers, Context context) throws Exception;

	/**
	 * 对留言点赞
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param messageBoardObjectId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public MessageBoardOperation favourMessageBoard(String adSId, String sid, String id, String messageBoardObjectId,
			Context context) throws Exception;

	/**
	 * 对非留言版点赞
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param messageBoardObjectId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public MessageBoardOperation favourUnMessageBoard(String adSId, String sid, String id, String messageBoardObjectId,
			Context context) throws Exception;

	/**
	 * 对留言取消点赞
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @param messageBoardObjectId
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public MessageBoardOperation unFavourMessageBoard(String adSId, String sid, String messageBoardObjectId, Context context)
			throws Exception;

	/**
	 * 检测能否创建圈子
	 * */
	public MessageBoardOperation checkCircleCreationPrivilege(String adSId, String sid, Context context) throws Exception;

	/**
	 * 根据会员id生成圈子头像
	 * */
	public CircleAvator generateCircleAvator(String adSId, String sid, int[] memberIds, Context context) throws Exception;

	/**
	 * 更改圈子头像
	 * */
	public CircleAvator updateCircleavator(String adSId, String sid, int[] imMemberIds, String imConversationId, Context context)
			throws Exception;

	/**
	 * 圈子二维码
	 * */
	public CircleQrcodeInfo getCircleQrcode(String adSId, String sid, String circleId, Context context) throws Exception;

	/**
	 * 创建圈子
	 * */
	public MessageBoardOperation createCircle(String adSId, String sid, int circleId, String imConversationId, int preAvatarId,
			String name, int joinType, String note, int[] memberIds, boolean searchAble, Context context) throws Exception;

	/**
	 * 直接加入无需审批的圈子
	 * */
	public MessageBoardOperation directJoinCircle(String adSId, String sid, String imConversationId, Context context)
			throws Exception;

	/**
	 * 判断是否有修改圈子信息的权限
	 * */
	public MessageBoardOperation checkUpdateCircle(String adSId, String sid, String imConversationId, Context context)
			throws Exception;

	/**
	 * 获取某个圈子所有申请记录
	 * */
	public CircleJoinINfo circleJoinRequset(String adSId, String sid, String imConversationId, int page, int pageCount,
			Context context) throws Exception;

	/**
	 * 获取某个圈子所有申请记录
	 * */
	public CircleJoinINfo myCircleJoinRequset(String adSId, String sid, int page, int pageCount, Context context)
			throws Exception;

	/**
	 * 根据id加载圈子信息
	 * */
	public CirCleInfo loadCircleInfo(String adSId, String sid, String imConversationId, Context context) throws Exception;

	/**
	 * 更新圈子信息
	 * */
	public MessageBoardOperation updateCircle(String adSId, String sid, String imConversationId, int joninType, String name,
			String not, Context context) throws Exception;

	/**
	 * 管理员邀人入群
	 * */
	public MessageBoardOperation invitationJoinCircle(String adSId, String sid, String imConversationId,
			int[] invitationImMemberIds, Context context) throws Exception;

	/**
	 * 邀请人入群
	 * */
	public MessageBoardOperation inviteCircle(String adSId, String sid, String imConversationId, int[] invitationImMemberIds,
			Context context) throws Exception;

	/**
	 * 审批邀请记录
	 * */
	public MessageBoardOperation approveCircleJoinRequest(String adSId, String sid, int requestId, int approveState,
			Context context) throws Exception;

	/**
	 * 解散圈子
	 * */
	public MessageBoardOperation dissolveCircle(String adSId, String sid, String imConversationId, Context context)
			throws Exception;

	/**
	 * 退出圈子
	 * */
	public MessageBoardOperation deleteCircle(String adSId, String sid, String imConversationId, int[] imMemberId,
			Context context) throws Exception;

	/**
	 * 将jpush设置到服务器
	 * 
	 * @param id
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	// public MessageBoardOperation setMyJPush(String id, String token,
	// String codeOS,String codeAPP,Context context) throws Exception;
	//
	// /**
	// * 删除jpush设置
	// *
	// * @param id
	// *
	// * @param token
	// * @return
	// * @throws Exception
	// */
	// public MessageBoardOperation delMyJPush(String id, String token,
	// String codeOS,String codeAPP,Context context) throws Exception;
	/**
	 * 获取新消息
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @return
	 * @throws Exception
	 */
	public NewMessageBoards unReadNewMsg(String adSId, String sid, Context context) throws Exception;

	/**
	 * 获取新消息数目
	 * 
	 * @param adSId
	 *            会员加密后的id用于请求客厅留言
	 * @param sid
	 * @return
	 * @throws Exception
	 */
	public UnReadMsgNum unReadNewMsgNum(String adSId, String sid, Context context) throws Exception;

	public ViewFullMessageBoard viewFullMessageBoard(String objectId, String adSId, String sid, int loadType,
			int viewFullMessageBoard, int count, Context context) throws Exception;

	public ViewFullMessageBoard viewFullUnMessageBoard(String objectId, String adSId, String sid, int loadType,
			int viewFullMessageBoard, int count, Context context) throws Exception;

	MessageBoards getPersonalMsgBoards(String adSId, String sid, String viewSId, Integer type, String cls, Integer count,
			Long minCreateDate, Long maxCreateDate, Context context) throws Exception;

	FoundMessageBoards getFoundMsgBoards(String adSId, String sid, String type, int page, Long maxLastUpdaatedDate, int cacheTime,
			int androidPhotoType, Context context) throws Exception;

	PersonalMessageBoards getPersonRenmaiquanMsgBoards(String viewSid, String adSId, String sid, int mType, String type,
			Integer count, Long minCreateDate, Long maxCreateDate, Long maxLastUpdaatedDate, int androidPhotoType,
			Context context) throws Exception;

	MessageBoardOperation shareWebToRenmaiquan(String adSId, String sid, Context context, String shareId, String content,
			String atMembers) throws Exception;

	MessageBoardOperation shareCircleToRenmaiquan(String adSId, String sid, Context context, String circleId, String content,
			String atMembers) throws Exception;

	MessageBoardOperation shareProfileToRenmaiquan(String adSId, String sid, Context context, String profileSid, String content,
			String atMembers) throws Exception;
}
