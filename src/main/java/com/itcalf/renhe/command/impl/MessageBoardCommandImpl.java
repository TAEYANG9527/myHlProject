package com.itcalf.renhe.command.impl;

import android.content.Context;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.bean.CirCleInfo;
import com.itcalf.renhe.bean.CircleJoinINfo;
import com.itcalf.renhe.command.IMessageBoardCommand;
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
import com.itcalf.renhe.utils.DeviceUitl;
import com.itcalf.renhe.utils.HttpUtil;
import com.itcalf.renhe.utils.UtfHttpUtil;

import java.util.HashMap;
import java.util.Map;

public class MessageBoardCommandImpl implements IMessageBoardCommand {

    @Override
    public MessageBoards getMsgBoards(String adSId, String sid, String type, Integer count, Long minCreateDate,
                                      Long maxCreateDate, int androidPhotoType, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", sid);
        reqParams.put("count", count);// 取数据的count
        reqParams.put("adSId", adSId);
        reqParams.put("type", type);
        if (null != minCreateDate) {
            reqParams.put("minRank", minCreateDate);
        }
        if (null != maxCreateDate) {
            reqParams.put("maxRank", maxCreateDate);
        }
        reqParams.put("androidPhotoType", androidPhotoType);
        reqParams.put("bundle", DeviceUitl.getDeviceInfo());
        return (MessageBoards) HttpUtil.doHttpRequest(Constants.Http.GET_RENMAIQUAN_V3, reqParams, MessageBoards.class,
                context);
    }

    @Override
    public FoundMessageBoards getFoundMsgBoards(String adSId, String sid, String type, int page, Long maxLastUpdaatedDate,
                                                int cacheTime, int androidPhotoType, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", sid);
        reqParams.put("page", page);// 取数据的count
        reqParams.put("adSId", adSId);
        reqParams.put("type", type);
        if (null != maxLastUpdaatedDate) {
            reqParams.put("maxLastUpdatedDate", maxLastUpdaatedDate);
        }
        reqParams.put("androidPhotoType", androidPhotoType);
        reqParams.put("cacheTime", cacheTime);
        return (FoundMessageBoards) HttpUtil.doHttpRequest(Constants.Http.GET_FAXIAN, reqParams, FoundMessageBoards.class,
                context);
    }

    @Override
    public PersonalMessageBoards getPersonRenmaiquanMsgBoards(String viewSid, String adSId, String sid, int mType, String type,
                                                              Integer count, Long minCreateDate, Long maxCreateDate, Long maxLastUpdaatedDate, int androidPhotoType,
                                                              Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", sid);
        reqParams.put("count", count);// 取数据的count
        reqParams.put("adSId", adSId);
        reqParams.put("viewSId", viewSid);
        reqParams.put("type", type);
        if (null != minCreateDate) {
            reqParams.put("minCreatedDate", minCreateDate);
        }
        if (null != maxCreateDate) {
            reqParams.put("maxCreatedDate", maxCreateDate);
        }
        if (null != maxLastUpdaatedDate) {
            reqParams.put("maxLastUpdatedDate", maxLastUpdaatedDate);
        }
        reqParams.put("androidPhotoType", androidPhotoType);
        return (PersonalMessageBoards) HttpUtil.doHttpRequest(Constants.Http.PERSONAL_RENMAIQUAN, reqParams,
                PersonalMessageBoards.class, context);
    }

    @Override
    public MessageBoards getPersonalMsgBoards(String adSId, String sid, String viewSId, Integer type, String cls, Integer count,
                                              Long minCreateDate, Long maxCreateDate, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("sid", sid);
        reqParams.put("count", count);// 取数据的count
        reqParams.put("adSId", adSId);
        if (null != viewSId) {
            reqParams.put("viewSId", viewSId);
        }
        reqParams.put("type", cls);
        if (null != minCreateDate) {
            reqParams.put("minCreatedDate", minCreateDate);
        }
        if (null != maxCreateDate) {
            reqParams.put("maxCreatedDate", maxCreateDate);
        }
        switch (type) {
            case PERSONAL_MESSAGEBOARDS:
                return (MessageBoards) HttpUtil.doHttpRequest(Constants.Http.PERSONAL_MESSAGEBOARDS, reqParams, MessageBoards.class,
                        context);
        }
        return null;
    }

    @Override
    public MessageBoardDetail getMsgBoradDetail(String objectId, String adSId, String sid, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("objectId", objectId);// messageBoard的objectId用于请求客厅留言的详细页面
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        return (MessageBoardDetail) HttpUtil.doHttpRequest(Constants.Http.SHOW_MESSAGEBOARD, reqParams, MessageBoardDetail.class,
                context);
    }

    @Override
    public MsgComments getMsgComments(String objectId, String adSId, String sid, int start, int count, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("objectId", objectId);// messageBoard的objectId用于请求客厅留言的详细页面
        reqParams.put("start", start);
        reqParams.put("count", count);
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        return (MsgComments) HttpUtil.doHttpRequest(Constants.Http.MSG_COMMENTS, reqParams, MsgComments.class, context);
    }

    @Override
    public MsgComments getUnMessageBoardMsgComments(String objectId, String adSId, String sid, int start, int count,
                                                    Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("noticeObjectId", objectId);
        reqParams.put("start", start);
        reqParams.put("count", count);
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        return (MsgComments) HttpUtil.doHttpRequest(Constants.Http.UNMESSAGEBOARD_MSG_COMMENTS, reqParams, MsgComments.class,
                context);
    }

    @Override
    public ViewFullMessageBoard viewFullMessageBoard(String objectId, String adSId, String sid, int loadType,
                                                     int androidPhotoType, int count, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("objectId", objectId);// messageBoard的objectId用于请求客厅留言的详细页面
        reqParams.put("sid", sid);
        reqParams.put("adSId", adSId);
        reqParams.put("loadType", loadType);
        reqParams.put("androidPhotoType", androidPhotoType);
        reqParams.put("count", count);
        return (ViewFullMessageBoard) HttpUtil.doHttpRequest(Constants.Http.VIEW_FULLBOARD, reqParams, ViewFullMessageBoard.class,
                context);
    }

    @Override
    public ViewFullMessageBoard viewFullUnMessageBoard(String objectId, String adSId, String sid, int loadType,
                                                       int androidPhotoType, int count, Context context) throws Exception // 查看非MessageBoard详情
    {
        if (loadType == 2)
            loadType = 3;

        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("noticeObjectId", objectId);
        reqParams.put("sid", sid);
        reqParams.put("deviceType", 0);
        reqParams.put("adSId", adSId);
        reqParams.put("loadType", loadType);
        reqParams.put("androidPhotoType", androidPhotoType);
        reqParams.put("count", count);
        return (ViewFullMessageBoard) HttpUtil.doHttpRequest(Constants.Http.VIEW_FULLUNBOARD, reqParams,
                ViewFullMessageBoard.class, context);
    }

    @Override
    public AddMessageBoard publicMessageBoard(String adSId, String sid, String content, String atMembers, int photoNum,
                                              Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("content", content);
        reqParams.put("photoNum", photoNum);
        reqParams.put("atMembers", atMembers);//被@的用户
        return (AddMessageBoard) UtfHttpUtil.doHttpRequest(Constants.Http.PREPUBLISH_MESSAGEBOARD, reqParams,
                AddMessageBoard.class, context);
    }

    @Override
    public ReplyMessageBoard replyMessageBoard(String adSId, String sid, String messageBoardId, String messageBoardObjectId,
                                               String content, boolean forwardMessageBoard, String replyMessageBoardId, String replyMessageBoardObjectId,
                                               Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("messageBoardId", messageBoardId);
        reqParams.put("messageBoardObjectId", messageBoardObjectId);
        reqParams.put("content", content);
        reqParams.put("forwardMessageBoard", forwardMessageBoard);
        if (null != replyMessageBoardId) {
            reqParams.put("replyMessageBoardId", replyMessageBoardId);
        }
        if (null != replyMessageBoardObjectId) {
            reqParams.put("replyMessageBoardObjectId", replyMessageBoardObjectId);
        }
        return (ReplyMessageBoard) UtfHttpUtil.doHttpRequest(Constants.Http.REPLY_MESSAGEBOARD, reqParams,
                ReplyMessageBoard.class, context);
    }

    @Override
    public ReplyUnMessageBoard replyUnMessageBoard(String adSId, String sid, String messageBoardId, String messageBoardObjectId,
                                                   String content, boolean forwardMessageBoard, String replyMessageBoardId, String replyMessageBoardObjectId,
                                                   Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("content", content);
        reqParams.put("deviceType", 0);
        reqParams.put("noticeId", messageBoardId);
        reqParams.put("noticeObjectId", messageBoardObjectId);
        if (null != replyMessageBoardObjectId) {
            reqParams.put("replyNoticeCommentObjectId", replyMessageBoardObjectId);
        }
        return (ReplyUnMessageBoard) UtfHttpUtil.doHttpRequest(Constants.Http.REPLY_UNMESSAGEBOARD, reqParams,
                ReplyUnMessageBoard.class, context);
    }

    @Override
    public MessageBoardOperation forwardMessageBoard(String adSId, String sid, String messageBoardObjectId, String content,
                                                     String atMembers, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("messageBoardObjectId", messageBoardObjectId);
        reqParams.put("content", content);
        reqParams.put("atMembers", atMembers);
        return (MessageBoardOperation) UtfHttpUtil.doHttpRequest(Constants.Http.FORWARD_MESSAGEBOARD, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation favourMessageBoard(String adSId, String sid, String id, String messageBoardObjectId,
                                                    Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("messageBoardObjectId", messageBoardObjectId);
        reqParams.put("messageBoardId", id);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.FAVOUR_MESSAGEBOARD, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation favourUnMessageBoard(String adSId, String sid, String id, String messageBoardObjectId,
                                                      Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("noticeObjectId", messageBoardObjectId);
        reqParams.put("noticeId", id);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.FAVOUR_UNMESSAGEBOARD, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation unFavourMessageBoard(String adSId, String sid, String messageBoardObjectId, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("messageBoardObjectId", messageBoardObjectId);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.UNFAVOUR_MESSAGEBOARD, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation checkCircleCreationPrivilege(String adSId, String sid, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.CHECK_CIRCLECREATIONPRICILEGE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public CircleAvator generateCircleAvator(String adSId, String sid, int[] memberIds, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imMemberIds", memberIds);
        return (CircleAvator) HttpUtil.doHttpRequest(Constants.Http.GENERATE_CIRCLE_AVATOR, reqParams, CircleAvator.class,
                context);
    }

    @Override
    public CircleAvator updateCircleavator(String adSId, String sid, int[] imMemberIds, String imConversationId, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imMemberIds", imMemberIds);
        reqParams.put("imConversationId", imConversationId);
        return (CircleAvator) HttpUtil.doHttpRequest(Constants.Http.UPDATE_CIRCLE_AVATOR, reqParams, CircleAvator.class, context);
    }

    @Override
    public MessageBoardOperation directJoinCircle(String adSId, String sid, String imConversationId, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imConversationId", imConversationId);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.DIRECT_JOIN_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation checkUpdateCircle(String adSId, String sid, String imConversationId, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imConversationId", imConversationId);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.CHECK_UPDATE_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public CirCleInfo loadCircleInfo(String adSId, String sid, String imConversationId, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imConversationId", imConversationId);
        return (CirCleInfo) HttpUtil.doHttpRequest(Constants.Http.LOAD_CIRCLE_INFO, reqParams, CirCleInfo.class, context);
    }

    @Override
    public MessageBoardOperation createCircle(String adSId, String sid, int circleId, String imConversationId, int preAvatarId,
                                              String name, int joinType, String note, int[] memberIds, boolean searchAble, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("circleId", circleId);
        reqParams.put("imConversationId", imConversationId);
        reqParams.put("preAvatarId", preAvatarId);
        reqParams.put("name", name);
        reqParams.put("joinType", joinType);
        reqParams.put("note", note);
        reqParams.put("imMemberIds", memberIds);
        reqParams.put("searchAble", searchAble);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.CREATE_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation approveCircleJoinRequest(String adSId, String sid, int requestId, int approveState,
                                                          Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("requestId", requestId);
        reqParams.put("approveState", approveState);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.APPROVE_CIRCLE_JONIN_REQUEST, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation deleteCircle(String adSId, String sid, String imConversationId, int[] imMemberId,
                                              Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imMemberIds", imMemberId);
        reqParams.put("imConversationId", imConversationId);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.DELETE_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation updateCircle(String adSId, String sid, String imConversationId, int joninType, String name,
                                              String not, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("joinType", joninType);
        reqParams.put("name", name);
        reqParams.put("note", not);
        reqParams.put("imConversationId", imConversationId);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.UPDATE_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation invitationJoinCircle(String adSId, String sid, String imConversationId,
                                                      int[] invitationImMemberIds, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imConversationId", imConversationId);
        reqParams.put("invitationImMemberIds", invitationImMemberIds);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.INVITATION_JOIN_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation inviteCircle(String adSId, String sid, String imConversationId, int[] invitationImMemberIds,
                                              Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imConversationId", imConversationId);
        reqParams.put("invitationImMemberIds", invitationImMemberIds);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.INVITE_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public CircleJoinINfo circleJoinRequset(String adSId, String sid, String imConversationId, int page, int pageCount,
                                            Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("page", page);
        reqParams.put("pageCount", pageCount);
        reqParams.put("imConversationId", imConversationId);
        return (CircleJoinINfo) HttpUtil.doHttpRequest(Constants.Http.CIRCLE_JOIN_REQUEST, reqParams, CircleJoinINfo.class,
                context);
    }

    @Override
    public CircleJoinINfo myCircleJoinRequset(String adSId, String sid, int page, int pageCount, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("page", page);
        reqParams.put("pageCount", pageCount);
        return (CircleJoinINfo) HttpUtil.doHttpRequest(Constants.Http.MY_CRICLE_JOIN_REQUSET, reqParams, CircleJoinINfo.class,
                context);
    }

    @Override
    public MessageBoardOperation dissolveCircle(String adSId, String sid, String imConversationId, Context context)
            throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("imConversationId", imConversationId);
        return (MessageBoardOperation) HttpUtil.doHttpRequest(Constants.Http.DISSOLVE_CIRCLE, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public NewMessageBoards unReadNewMsg(String adSId, String sid, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        return (NewMessageBoards) HttpUtil.doHttpRequest(Constants.Http.NOTICE_UNREADMSG, reqParams, NewMessageBoards.class,
                context);
    }

    @Override
    public UnReadMsgNum unReadNewMsgNum(String adSId, String sid, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        return (UnReadMsgNum) HttpUtil.doHttpRequest(Constants.Http.UNREADMSGNUM, reqParams, UnReadMsgNum.class, context);
    }

    @Override
    public MessageBoardOperation shareWebToRenmaiquan(String adSId, String sid, Context context, String shareId, String content,
                                                      String atMembers) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("shareId", shareId);
        reqParams.put("content", content);
        reqParams.put("atMembers", atMembers);
        return (MessageBoardOperation) UtfHttpUtil.doHttpRequest(Constants.Http.SHARE_WEBVIEW_CONTENT_TO_RENMAIQUAN, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public CircleQrcodeInfo getCircleQrcode(String adSId, String sid, String circleId, Context context) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("circleId", circleId);
        return (CircleQrcodeInfo) HttpUtil.doHttpRequest(Constants.Http.GET_CIRCLE_QRCODE, reqParams, CircleQrcodeInfo.class,
                context);
    }

    @Override
    public MessageBoardOperation shareCircleToRenmaiquan(String adSId, String sid, Context context, String circleId,
                                                         String content, String atMembers) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("circleId", circleId);
        reqParams.put("content", content);
        reqParams.put("atMembers", atMembers);
        return (MessageBoardOperation) UtfHttpUtil.doHttpRequest(Constants.Http.SHARE_CIRCLE_TO_RENMAIQUAN, reqParams,
                MessageBoardOperation.class, context);
    }

    @Override
    public MessageBoardOperation shareProfileToRenmaiquan(String adSId, String sid, Context context, String profileSid,
                                                          String content, String atMembers) throws Exception {
        Map<String, Object> reqParams = new HashMap<String, Object>();
        reqParams.put("adSId", adSId);
        reqParams.put("sid", sid);
        reqParams.put("profileSid", profileSid);
        reqParams.put("content", content);
        reqParams.put("atMembers", atMembers);
        return (MessageBoardOperation) UtfHttpUtil.doHttpRequest(Constants.Http.SHARE_PROFILE_TO_RENMAIQUAN, reqParams,
                MessageBoardOperation.class, context);
    }
}
