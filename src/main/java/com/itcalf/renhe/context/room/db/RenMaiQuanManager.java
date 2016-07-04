package com.itcalf.renhe.context.room.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.room.db.AddMsgSQLiteStore.AddSQLiteOpenHelper;
import com.itcalf.renhe.context.room.db.SQLiteStore.RenheSQLiteOpenHelper;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.AtMemmber;
import com.itcalf.renhe.dto.MessageBoards.ContentInfo;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.LikedList;
import com.itcalf.renhe.dto.MessageBoards.NewNoticeList;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.itcalf.renhe.dto.MessageBoards.RecommendMember;
import com.itcalf.renhe.dto.MessageBoards.ReplyList;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;
import com.itcalf.renhe.dto.MessageBoards.circleShare;
import com.itcalf.renhe.dto.MessageBoards.profileShare;
import com.itcalf.renhe.dto.MessageBoards.webShare;
import com.itcalf.renhe.dto.UploadPhotoCacheBean;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RenMaiQuanManager {

    private Context context;

    public RenMaiQuanManager(Context context) {
        this.context = context;
    }

    public void insert(MessageBoards messageBoards) {
        String sid = RenheApplication.getInstance().getUserInfo().getSid();
        NewNoticeList[] newNoticeList = messageBoards.getNewNoticeList();
        if (null != newNoticeList && newNoticeList.length > 0) {
            SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
            ContentValues values;
            for (NewNoticeList noticeList : newNoticeList) {
                if (null != noticeList) {
                    if (null != noticeList.getContentInfo() && isExist(noticeList.getContentInfo().getObjectId())) {
                        continue;
                    }
                    values = new ContentValues();
                    values.put("type", noticeList.getType());
                    if (noticeList.getSenderInfo() != null) {
                        SenderInfo senderInfo = noticeList.getSenderInfo();
                        values.put("senderSid", senderInfo.getSid());
                        values.put("name", senderInfo.getName());
                        values.put("userface", senderInfo.getUserface());
                        values.put("title", senderInfo.getTitle());
                        values.put("company", senderInfo.getCompany());
                        values.put("industry", senderInfo.getIndustry());
                        values.put("location", senderInfo.getLocation());
                        values.put("accountType", senderInfo.getAccountType());
                        values.put("isRealName", senderInfo.isRealname());
                        values.put("friendState", senderInfo.getFriendState());
                    }
                    if (noticeList.getContentInfo() != null) {
                        ContentInfo contentInfo = noticeList.getContentInfo();
                        values.put("objectId", contentInfo.getObjectId());
                        values.put("content", contentInfo.getContent());
                        values.put("subject", contentInfo.getSubject());
                        values.put("url", contentInfo.getUrl());
                    }
                    values.put("createDate", noticeList.getCreatedDate());
                    values.put("source", noticeList.getSource());
                    values.put("score", noticeList.getRank());
                    values.put("sid", sid);
                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, null, values);
                    values = new ContentValues();
                    values.put("type", noticeList.getType());
                    if (noticeList.getContentInfo() != null) {
                        ContentInfo contentInfo = noticeList.getContentInfo();
                        values.put("objectId", contentInfo.getObjectId());
                        values.put("Id", contentInfo.getId());
                        values.put("replyNum", contentInfo.getReplyNum());
                        values.put("likedNumber", contentInfo.getLikedNum());
                        values.put("liked", contentInfo.isLiked());
                        if (null != contentInfo.getForwardMessageBoardInfo()) {
                            ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
                            values.put("ForwardMessageBoardInfo_isForwardRenhe", forwardMessageBoardInfo.isForwardRenhe());
                            values.put("ForwardMessageBoardInfo_ObjectId", forwardMessageBoardInfo.getObjectId());
                            values.put("ForwardMessageBoardInfo_Name", forwardMessageBoardInfo.getName());
                            values.put("ForwardMessageBoardInfo_Sid", forwardMessageBoardInfo.getSid());
                            values.put("ForwardMessageBoardInfo_Content", forwardMessageBoardInfo.getContent());
                            values.put("ForwardMessageBoardInfo_Type", forwardMessageBoardInfo.getType());
                        }
                    }
                    values.put("sid", sid);
                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, null, values);

                    if (noticeList.getContentInfo() != null) {

                        ContentInfo contentInfo = noticeList.getContentInfo();
                        ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
                        values = new ContentValues();
                        if (null != forwardMessageBoardInfo) {
                            values.put("objectId", contentInfo.getObjectId());// 留言内容中@信息
                            values.put("type", forwardMessageBoardInfo.getType());
                            values.put("sid", sid);
                            switch (forwardMessageBoardInfo.getType()) {
                                case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
                                    if (null != forwardMessageBoardInfo.getWebsShare()) {
                                        values.put("id", forwardMessageBoardInfo.getWebsShare().getId());
                                        values.put("content", forwardMessageBoardInfo.getWebsShare().getContent());
                                        values.put("url", forwardMessageBoardInfo.getWebsShare().getUrl());
                                        values.put("picUrl", forwardMessageBoardInfo.getWebsShare().getPicUrl());
                                    }

                                    break;
                                case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
                                    if (null != forwardMessageBoardInfo.getProfileShare()) {
                                        values.put("profileSid", forwardMessageBoardInfo.getProfileShare().getSid());
                                        values.put("name", forwardMessageBoardInfo.getProfileShare().getName());
                                        values.put("job", forwardMessageBoardInfo.getProfileShare().getJob());
                                        values.put("company", forwardMessageBoardInfo.getProfileShare().getCompany());
                                        values.put("picUrl", forwardMessageBoardInfo.getProfileShare().getPicUrl());
                                    }
                                    break;
                                case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:
                                    if (null != forwardMessageBoardInfo.getCircleShare()) {
                                        values.put("id", forwardMessageBoardInfo.getCircleShare().getId());
                                        values.put("name", forwardMessageBoardInfo.getCircleShare().getName());
                                        values.put("note", forwardMessageBoardInfo.getCircleShare().getNote());
                                        values.put("picUrl", forwardMessageBoardInfo.getCircleShare().getPicUrl());
                                    }
                                    break;
                                default:
                                    break;
                            }
                            dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_SHARE, null, values);
                        }

                        boolean isContentAtMemberEmpty = true;
                        boolean isForwardAtMemberEmpty = true;
                        if (null != contentInfo.getAtMembers() && contentInfo.getAtMembers().length > 0) {
                            isContentAtMemberEmpty = false;
                        }
                        if (null != forwardMessageBoardInfo && null != forwardMessageBoardInfo.getAtMembers()
                                && forwardMessageBoardInfo.getAtMembers().length > 0) {
                            isForwardAtMemberEmpty = false;
                        }
                        if (!isContentAtMemberEmpty || !isForwardAtMemberEmpty) {
                            if (!isContentAtMemberEmpty && !isForwardAtMemberEmpty) {
                                for (AtMemmber atMemmber : contentInfo.getAtMembers()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("memberSid", atMemmber.getMemberSid());
                                    values.put("memberName", atMemmber.getMemberName());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);

                                }
                                for (AtMemmber atMemmber : forwardMessageBoardInfo.getAtMembers()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("forward_memberSid", atMemmber.getMemberSid());
                                    values.put("forward_memberName", atMemmber.getMemberName());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);

                                }
                            } else if (!isContentAtMemberEmpty && isForwardAtMemberEmpty) {
                                for (AtMemmber atMemmber : contentInfo.getAtMembers()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("memberSid", atMemmber.getMemberSid());
                                    values.put("memberName", atMemmber.getMemberName());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);

                                }
                            } else if (isContentAtMemberEmpty && !isForwardAtMemberEmpty) {
                                for (AtMemmber atMemmber : forwardMessageBoardInfo.getAtMembers()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("forward_memberSid", atMemmber.getMemberSid());
                                    values.put("forward_memberName", atMemmber.getMemberName());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);
                                }
                            }
                        }

                        if (null != contentInfo.getReplyList() && contentInfo.getReplyList().length > 0) {
                            for (ReplyList replyList : contentInfo.getReplyList()) {
                                values = new ContentValues();
                                values.put("objectId", contentInfo.getObjectId());
                                values.put("senderSid", replyList.getSenderSid());
                                values.put("senderName", replyList.getSenderName());
                                values.put("reSenderSid", replyList.getReSenderSid());
                                values.put("reSenderMemberName", replyList.getReSenderMemberName());
                                values.put("replyId", replyList.getId());
                                values.put("replyObjectId", replyList.getObjectId());
                                values.put("content", replyList.getContent());
                                values.put("sid", sid);
                                dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_REPLY_LIST, null, values);
                            }
                        }

                        if (null != contentInfo.getLikedList() && contentInfo.getLikedList().length > 0) {
                            for (LikedList likedList : contentInfo.getLikedList()) {
                                values = new ContentValues();
                                values.put("objectId", contentInfo.getObjectId());
                                values.put("likeSid", likedList.getSid());
                                values.put("name", likedList.getName());
                                values.put("userface", likedList.getUserface());
                                values.put("sid", sid);
                                dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_LIKED_LIST, null, values);
                            }
                        }

                        boolean isContentPicListEmpty = true;
                        boolean isForwardPicListEmpty = true;
                        if (null != contentInfo.getPicList() && contentInfo.getPicList().length > 0) {
                            isContentPicListEmpty = false;
                        }
                        if (null != forwardMessageBoardInfo && null != forwardMessageBoardInfo.getPicLists()
                                && forwardMessageBoardInfo.getPicLists().length > 0) {
                            isForwardPicListEmpty = false;
                        }
                        if (!isContentPicListEmpty || !isForwardPicListEmpty) {
                            if (!isContentPicListEmpty && !isForwardPicListEmpty) {
                                for (PicList picList : contentInfo.getPicList()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("thumbnailPicUrl", picList.getThumbnailPicUrl());
                                    values.put("bmiddlePicUrl", picList.getBmiddlePicUrl());
                                    values.put("bmiddlePicWidth", picList.getBmiddlePicWidth());
                                    values.put("bmiddlePicHeight", picList.getBmiddlePicHeight());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
                                }
                                for (PicList picList : forwardMessageBoardInfo.getPicLists()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("forward_thumbnailPicUrl", picList.getThumbnailPicUrl());
                                    values.put("forward_bmiddlePicUrl", picList.getBmiddlePicUrl());
                                    values.put("forward_bmiddlePicWidth", picList.getBmiddlePicWidth());
                                    values.put("forward_bmiddlePicHeight", picList.getBmiddlePicHeight());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
                                }
                            } else if (!isContentPicListEmpty && isForwardPicListEmpty) {
                                for (PicList picList : contentInfo.getPicList()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("thumbnailPicUrl", picList.getThumbnailPicUrl());
                                    values.put("bmiddlePicUrl", picList.getBmiddlePicUrl());
                                    values.put("bmiddlePicWidth", picList.getBmiddlePicWidth());
                                    values.put("bmiddlePicHeight", picList.getBmiddlePicHeight());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
                                }
                            } else if (isContentPicListEmpty && !isForwardPicListEmpty) {
                                for (PicList picList : forwardMessageBoardInfo.getPicLists()) {
                                    values = new ContentValues();
                                    values.put("objectId", contentInfo.getObjectId());
                                    values.put("forward_thumbnailPicUrl", picList.getThumbnailPicUrl());
                                    values.put("forward_bmiddlePicUrl", picList.getBmiddlePicUrl());
                                    values.put("forward_bmiddlePicWidth", picList.getBmiddlePicWidth());
                                    values.put("forward_bmiddlePicHeight", picList.getBmiddlePicHeight());
                                    values.put("sid", sid);
                                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
                                }
                            }

                        }
                        switch (noticeList.getType()) {
                            case MessageBoards.MESSAGE_TYPE_FRIEND_JOIN_NOTICE:
                            case MessageBoards.MESSAGE_TYPE_RECOMMEND_FRIEND:
                                if (null != contentInfo.getMembers() && contentInfo.getMembers().length > 0) {
                                    for (RecommendMember recommendMember : contentInfo.getMembers()) {
                                        values = new ContentValues();
                                        values.put("objectId", contentInfo.getObjectId());
                                        values.put("sid", sid);
                                        values.put("memberSid", recommendMember.getSid());
                                        values.put("memberName", recommendMember.getName());
                                        values.put("memberUserface", recommendMember.getUserface());
                                        values.put("memberTitle", recommendMember.getTitle());
                                        values.put("memberCompany", recommendMember.getCompany());
                                        values.put("memberIndustry", recommendMember.getIndustry());
                                        values.put("memberLocation", recommendMember.getLocation());
                                        values.put("memberAccountType", recommendMember.getAccountType());
                                        values.put("memberIsRealName", recommendMember.isRealname());
                                        dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_RECOMMEND_FRIEND, null, values);
                                    }
                                }
                                break;

                            default:
                                break;
                        }
                    }

                }
            }

        }
    }

    public boolean isExist(String objectId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = SQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery(
                    "select count(*) from " + RenheSQLiteOpenHelper.TABLE_RENMAIQUAN + " where objectId=? and sid=?",
                    new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            if (count > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return false;
    }

    public int delete(String table, String objectId) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        try {
            if (!TextUtils.isEmpty(objectId)) {
                return dbWriter.delete(table, "objectId=? and sid=?",
                        new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return 0;
    }

    public ArrayList<String> selectObjectIdsBySenderId(String table, String senderSid) {
        ArrayList<String> objectIds = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = SQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery(
                    "select * from " + table + " where senderSid=? and sid=?",
                    new String[]{String.valueOf(senderSid), RenheApplication.getInstance().getUserInfo().getSid()});
            cursor.moveToFirst();
            String objectId = cursor.getString(11);
            objectIds.add(objectId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return objectIds;
    }

    /**
     * 加入黑名单后删除该好友人脉圈的本地缓存
     *
     * @param table
     * @param senderSid
     * @return
     */
    public int deleteBySenderSid(String table, String senderSid) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        try {
            if (!TextUtils.isEmpty(senderSid)) {
                return dbWriter.delete(table, "senderSid=? and sid=?",
                        new String[]{senderSid, RenheApplication.getInstance().getUserInfo().getSid()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return 0;
    }

    public int deleteReplyList(String table, String replyObjectId) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        try {
            return dbWriter.delete(table, "replyObjectId=? and sid=?",
                    new String[]{String.valueOf(replyObjectId), RenheApplication.getInstance().getUserInfo().getSid()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return 0;
    }

    public int deleteRecommendFriend(String table, String friendSid) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        try {
            return dbWriter.delete(table, "memberSid=? and sid=?",
                    new String[]{String.valueOf(friendSid), RenheApplication.getInstance().getUserInfo().getSid()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return 0;
    }

    public void updateLikedList(String objectId, LikedList[] likedLists) {
        if (null != likedLists && likedLists.length > 0) {
            SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
            dbWriter.execSQL("delete from " + RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_LIKED_LIST + " where objectId=" + "\'"
                    + objectId + "\'" + " and sid=" + "\'" + RenheApplication.getInstance().getUserInfo().getSid() + "\'");
            ContentValues values = new ContentValues();
            for (LikedList likedList : likedLists) {
                values = new ContentValues();
                values.put("objectId", objectId);
                values.put("likeSid", likedList.getSid());
                values.put("name", likedList.getName());
                values.put("userface", likedList.getUserface());
                values.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_LIKED_LIST, null, values);
            }
        }
    }

    public void updateReplyList(String objectId, ReplyList[] replyLists) {
        if (null != replyLists && replyLists.length > 0) {
            SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
            dbWriter.execSQL("delete from " + RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_REPLY_LIST + " where objectId=" + "\'"
                    + objectId + "\'" + " and sid=" + "\'" + RenheApplication.getInstance().getUserInfo().getSid() + "\'");
            ContentValues values = new ContentValues();
            for (ReplyList replyList : replyLists) {
                values = new ContentValues();
                values.put("objectId", objectId);
                values.put("senderSid", replyList.getSenderSid());
                values.put("senderName", replyList.getSenderName());
                values.put("reSenderSid", replyList.getReSenderSid());
                values.put("reSenderMemberName", replyList.getReSenderMemberName());
                values.put("replyId", replyList.getId());
                values.put("replyObjectId", replyList.getObjectId());
                values.put("content", replyList.getContent());
                values.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_REPLY_LIST, null, values);
            }
        }
    }

    public void updateReplyNum(String objectId, int replyNum) {
        try {
            SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("objectId", objectId);
            values.put("replyNum", replyNum);
            dbWriter.update(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, values, "objectId=? and sid=?",
                    new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLikedNum(String objectId, int likeNum, boolean isLiked) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("objectId", objectId);
        values.put("likedNumber", likeNum);
        if (isLiked) {
            values.put("liked", 1);
        } else {
            values.put("liked", 0);
        }
        dbWriter.update(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, values, "objectId=? and sid=?",
                new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
    }

    public void updateLikedState(String objectId, boolean isLiked) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("objectId", objectId);
        if (isLiked) {
            values.put("liked", 1);
        } else {
            values.put("liked", 0);
        }
        dbWriter.update(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, values, "objectId=? and sid=?",
                new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
    }

    public void updateTime(String requestType, long maxCreatedDate, long minCreatedDate) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        Cursor timeCursor = getTimeBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_TIME);
        try {
            if (null != timeCursor) {
                if (timeCursor.moveToLast()) {
                    if (requestType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW)) {
                        if (maxCreatedDate > 0) {
                            values.put("maxCreatedDate", maxCreatedDate);
                        } else {
                            values.put("maxCreatedDate", 0);
                        }
                        if (minCreatedDate > 0) {//每次上拉刷新更新最小rank，方便第二次进入应用，根据max、min直接读取最新的20条数据
                            values.put("minCreatedDate", minCreatedDate);
                        } else {
                            values.put("minCreatedDate", 0);
                        }
                        int _id = timeCursor.getInt(0);
                        dbWriter.update(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_TIME, values, "_id=? and sid=?",
                                new String[]{String.valueOf(_id), RenheApplication.getInstance().getUserInfo().getSid()});
                    } else if (requestType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE)) {
//						if (minCreatedDate > 0) {
//							values.put("minCreatedDate", minCreatedDate);
//						} else {
//							values.put("minCreatedDate", 0);
//						}
                    }

                } else {
                    if (maxCreatedDate > 0) {
                        values.put("maxCreatedDate", maxCreatedDate);
                    } else {
                        values.put("maxCreatedDate", 0);
                    }
                    if (minCreatedDate > 0) {
                        values.put("minCreatedDate", minCreatedDate);
                    } else {
                        values.put("minCreatedDate", 0);
                    }
                    values.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                    dbWriter.insert(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_TIME, null, values);
                }
            }
        } finally {
            closeCurser(timeCursor);
        }
    }

    /**
     * 更新senderInfo的好友状态 0好友 1不是好友 2已发送好友请求
     *
     * @param objectId
     * @param friendState
     */
    public void updateFriendState(String objectId, int friendState) {
        SQLiteDatabase dbWriter = SQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("friendState", friendState);
        dbWriter.update(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, values, "objectId=? and sid=?",
                new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
    }

    public Cursor getMainBoardsCursor(String table, long minScore, long maxScore, int count) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = SQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            if (maxScore > 0) {
                if (maxScore > minScore) {
                    cursor = dbReader.rawQuery("select * from " + table + " WHERE sid=" + "\'"
                            + RenheApplication.getInstance().getUserInfo().getSid() + "\'" + " and score BETWEEN " + minScore
                            + " AND " + maxScore + " ORDER BY score DESC ", null);
                    //					cursor = dbReader.rawQuery("select * from " + table + " WHERE sid=" + "\'"
                    //							+ RenheApplication.getInstance().getUserInfo().getSid() + "\'" + " and score >= " + minScore
                    //							+ " ORDER BY score DESC ", null);
                } else if (maxScore == minScore) {
                    cursor = dbReader.rawQuery("select * from " + table + " WHERE sid=" + "\'"
                            + RenheApplication.getInstance().getUserInfo().getSid() + "\'" + " and score = " + maxScore
                            + " ORDER BY score DESC ", null);
                    //				cursor = dbReader.rawQuery("select * from " + table + " ORDER BY score DESC ", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Cursor getTimeBoardsCursor(String table) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = SQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery("select * from " + table + " WHERE sid=" + "\'"
                    + RenheApplication.getInstance().getUserInfo().getSid() + "\'", null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Cursor getBoardsCursor(String table, String objectId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = SQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery("select * from " + table + " where objectId=? and sid=?",
                    new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Long getMaxCreateDate() {
        long maxCreatedDate = 0;
        Cursor timeCursor = null;
        try {
            timeCursor = getTimeBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_TIME);
            if (null != timeCursor && timeCursor.moveToLast()) {
                maxCreatedDate = timeCursor.getLong(1);
            }
            timeCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCurser(timeCursor);
        }
        return maxCreatedDate;
    }

    public long[] getManMinRank() {
        long[] ranks = new long[2];
        long maxRank = 0;
        long minRank = 0;
        Cursor timeCursor = null;
        try {
            timeCursor = getTimeBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_TIME);
            if (null != timeCursor && timeCursor.moveToLast()) {
                maxRank = timeCursor.getLong(1);
                minRank = timeCursor.getLong(2);
            }
            timeCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCurser(timeCursor);
        }
        ranks[0] = maxRank;
        ranks[1] = minRank;
        return ranks;
    }

    /**
     * @param minScore
     * @param maxScore
     * @param count
     * @return
     */
    public MessageBoards getMessageBoardsFromCursor(long minScore, long maxScore, int count) {
        int mCount = 0;
        Cursor cursor = null;
        Cursor contentCursor = null;
        Cursor atmemberCursor = null;
        Cursor picCursor = null;
        Cursor userFaceCursor = null;
        Cursor replyCursor = null;
        Cursor likedCursor = null;
        Cursor timeCursor = null;
        Cursor shareCursor = null;
        Cursor recommendFriendCursor = null;
        try {
            MessageBoards messageBoards = new MessageBoards();
            NewNoticeList newNoticeList;
            ContentInfo contentInfo;
            ForwardMessageBoardInfo forwardMessageBoardInfo;
            cursor = getMainBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, minScore, maxScore, count);
            if (null != cursor) {
                List<NewNoticeList> newNoticeLists = new ArrayList<MessageBoards.NewNoticeList>();
                while (cursor.moveToNext()) {
                    newNoticeList = new NewNoticeList();
                    contentInfo = new ContentInfo();
                    forwardMessageBoardInfo = new ForwardMessageBoardInfo();
                    int type = cursor.getInt(1);
                    newNoticeList.setType(type);
                    SenderInfo senderInfo = new SenderInfo();
                    senderInfo.setSid(cursor.getString(2));
                    senderInfo.setName(cursor.getString(3));
                    senderInfo.setUserface(cursor.getString(4));
                    senderInfo.setTitle(cursor.getString(5));
                    senderInfo.setCompany(cursor.getString(6));
                    senderInfo.setIndustry(cursor.getString(7));
                    senderInfo.setLocation(cursor.getString(8));
                    senderInfo.setAccountType(cursor.getInt(9));
                    if (cursor.getInt(10) == 0) {
                        senderInfo.setRealname(false);
                    } else {
                        senderInfo.setRealname(true);
                    }
                    senderInfo.setFriendState(cursor.getInt(19));
                    newNoticeList.setSenderInfo(senderInfo);
                    String objectId = cursor.getString(11);
                    contentInfo.setObjectId(objectId);
                    contentInfo.setContent(cursor.getString(12));
                    newNoticeList.setCreatedDate(cursor.getLong(13));
                    newNoticeList.setSource(cursor.getInt(14));
                    newNoticeList.setRank(cursor.getLong(15));
                    contentInfo.setSubject(cursor.getString(17));
                    contentInfo.setUrl(cursor.getString(18));
                    contentCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, objectId);
                    if (null != contentCursor && contentCursor.moveToNext()) {
                        contentInfo.setId(contentCursor.getInt(3));
                        contentInfo.setReplyNum(contentCursor.getInt(4));
                        contentInfo.setLikedNum(contentCursor.getInt(5));
                        if (contentCursor.getInt(6) == 0) {
                            contentInfo.setLiked(false);
                        } else {
                            contentInfo.setLiked(true);
                        }

                        if (contentCursor.getInt(7) == 0) {
                            forwardMessageBoardInfo.setForwardRenhe(false);
                        } else {
                            forwardMessageBoardInfo.setForwardRenhe(true);
                        }
                        forwardMessageBoardInfo.setObjectId(contentCursor.getString(8));
                        forwardMessageBoardInfo.setName(contentCursor.getString(9));
                        forwardMessageBoardInfo.setSid(contentCursor.getString(10));
                        forwardMessageBoardInfo.setContent(contentCursor.getString(11));
                        forwardMessageBoardInfo.setType(contentCursor.getInt(12));

                    }
                    contentCursor.close();
                    shareCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_SHARE, objectId);
                    if (null != shareCursor && shareCursor.moveToNext()) {
                        switch (forwardMessageBoardInfo.getType()) {
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB:
                                webShare wShare = new webShare();
                                wShare.setId(shareCursor.getInt(3));
                                wShare.setContent(shareCursor.getString(5));
                                wShare.setUrl(shareCursor.getString(4));
                                wShare.setPicUrl(shareCursor.getString(6));
                                forwardMessageBoardInfo.setWebsShare(wShare);
                                break;
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE:
                                profileShare pShare = new profileShare();
                                pShare.setSid(shareCursor.getString(7));
                                pShare.setName(shareCursor.getString(8));
                                pShare.setJob(shareCursor.getString(9));
                                pShare.setCompany(shareCursor.getString(10));
                                pShare.setPicUrl(shareCursor.getString(6));
                                forwardMessageBoardInfo.setProfileShare(pShare);
                                break;
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE:
                                circleShare cShare = new circleShare();
                                cShare.setNote(shareCursor.getString(11));
                                cShare.setName(shareCursor.getString(8));
                                cShare.setId(shareCursor.getInt(3));
                                cShare.setPicUrl(shareCursor.getString(6));
                                forwardMessageBoardInfo.setCircleShare(cShare);
                                break;

                            default:
                                break;
                        }
                    }

                    atmemberCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, objectId);
                    if (null != atmemberCursor) {
                        List<AtMemmber> contentAtMemmbers = new ArrayList<MessageBoards.AtMemmber>();
                        List<AtMemmber> forwardAtMemmbers = new ArrayList<MessageBoards.AtMemmber>();
                        while (atmemberCursor.moveToNext()) {
                            if (!TextUtils.isEmpty(atmemberCursor.getString(2))
                                    && !TextUtils.isEmpty(atmemberCursor.getString(3))) {
                                AtMemmber atMemmber = new AtMemmber();
                                atMemmber.setMemberSid(atmemberCursor.getString(2));
                                atMemmber.setMemberName(atmemberCursor.getString(3));
                                contentAtMemmbers.add(atMemmber);
                            }
                            if (!TextUtils.isEmpty(atmemberCursor.getString(4))
                                    && !TextUtils.isEmpty(atmemberCursor.getString(5))) {
                                AtMemmber atMemmber1 = new AtMemmber();
                                atMemmber1.setMemberSid(atmemberCursor.getString(4));
                                atMemmber1.setMemberName(atmemberCursor.getString(5));
                                forwardAtMemmbers.add(atMemmber1);
                            }
                        }
                        if (contentAtMemmbers.size() > 0) {
                            AtMemmber[] atMemmbers = new AtMemmber[contentAtMemmbers.size()];
                            for (int i = 0; i < contentAtMemmbers.size(); i++) {
                                atMemmbers[i] = contentAtMemmbers.get(i);
                            }
                            contentInfo.setAtMembers(atMemmbers);
                        }
                        if (forwardAtMemmbers.size() > 0) {
                            AtMemmber[] atMemmbers = new AtMemmber[forwardAtMemmbers.size()];
                            for (int i = 0; i < forwardAtMemmbers.size(); i++) {
                                atMemmbers[i] = forwardAtMemmbers.get(i);
                            }
                            forwardMessageBoardInfo.setAtMembers(atMemmbers);
                        }
                    }
                    atmemberCursor.close();
                    picCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, objectId);
                    if (null != picCursor) {
                        List<PicList> contentPicList = new ArrayList<MessageBoards.PicList>();
                        List<PicList> forwardPicList = new ArrayList<MessageBoards.PicList>();
                        while (picCursor.moveToNext()) {
                            if (!TextUtils.isEmpty(picCursor.getString(2)) && !TextUtils.isEmpty(picCursor.getString(3))) {
                                PicList picList = new PicList();
                                picList.setThumbnailPicUrl(picCursor.getString(2));
                                picList.setBmiddlePicUrl(picCursor.getString(3));
                                picList.setBmiddlePicWidth(picCursor.getInt(6));
                                picList.setBmiddlePicHeight(picCursor.getInt(7));
                                contentPicList.add(picList);
                            }
                            if (!TextUtils.isEmpty(picCursor.getString(4)) && !TextUtils.isEmpty(picCursor.getString(5))) {
                                PicList picList1 = new PicList();
                                picList1.setThumbnailPicUrl(picCursor.getString(4));
                                picList1.setBmiddlePicUrl(picCursor.getString(5));
                                picList1.setBmiddlePicWidth(picCursor.getInt(8));
                                picList1.setBmiddlePicHeight(picCursor.getInt(9));
                                forwardPicList.add(picList1);
                            }
                        }
                        if (contentPicList.size() > 0) {
                            PicList[] picLists = new PicList[contentPicList.size()];
                            for (int i = 0; i < contentPicList.size(); i++) {
                                picLists[i] = contentPicList.get(i);
                            }
                            contentInfo.setPicList(picLists);
                        }
                        if (forwardPicList.size() > 0) {
                            PicList[] picLists = new PicList[forwardPicList.size()];
                            for (int i = 0; i < forwardPicList.size(); i++) {
                                picLists[i] = forwardPicList.get(i);
                            }
                            forwardMessageBoardInfo.setPicLists(picLists);
                        }
                    }
                    picCursor.close();
                    contentInfo.setForwardMessageBoardInfo(forwardMessageBoardInfo);

                    replyCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_REPLY_LIST, objectId);
                    if (null != replyCursor) {
                        List<ReplyList> replyLists = new ArrayList<>();
                        while (replyCursor.moveToNext()) {
                            ReplyList replyList = new ReplyList();
                            replyList.setId(replyCursor.getInt(6));
                            replyList.setObjectId(replyCursor.getString(7));
                            replyList.setSenderSid(replyCursor.getString(2));
                            replyList.setSenderName(replyCursor.getString(3));
                            replyList.setReSenderSid(replyCursor.getString(4));
                            replyList.setReSenderMemberName(replyCursor.getString(5));
                            String replyContent = replyCursor.getString(8);
                            if (replyContent.length() >= 2 && replyContent.lastIndexOf("\n") == replyContent.length() - 1) {
                                replyContent = replyContent.substring(0, replyContent.length() - 2);
                            }
                            replyList.setContent(replyContent);
                            replyLists.add(replyList);
                        }
                        if (replyLists.size() > 0) {
                            ReplyList[] reLists = new ReplyList[replyLists.size()];
                            for (int i = 0; i < reLists.length; i++) {
                                reLists[i] = replyLists.get(i);
                            }
                            contentInfo.setReplyList(reLists);
                        }
                    }
                    replyCursor.close();
                    likedCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_LIKED_LIST, objectId);
                    if (null != likedCursor) {
                        List<LikedList> likedLists = new ArrayList<MessageBoards.LikedList>();
                        while (likedCursor.moveToNext()) {
                            LikedList likedList = new LikedList();
                            likedList.setSid(likedCursor.getString(2));
                            likedList.setName(likedCursor.getString(3));
                            likedList.setUserface(likedCursor.getString(4));
                            likedLists.add(likedList);
                        }
                        if (likedLists.size() > 0) {
                            LikedList[] reLists = new LikedList[likedLists.size()];
                            for (int i = 0; i < reLists.length; i++) {
                                reLists[i] = likedLists.get(i);
                            }
                            contentInfo.setLikedList(reLists);
                        }
                    }
                    likedCursor.close();
                    //推荐好友
                    recommendFriendCursor = getBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_RECOMMEND_FRIEND, objectId);
                    if (null != recommendFriendCursor) {
                        List<RecommendMember> recommendMembers = new ArrayList<RecommendMember>();
                        while (recommendFriendCursor.moveToNext()) {
                            RecommendMember recommendMember = new RecommendMember();
                            recommendMember.setSid(recommendFriendCursor.getString(2));
                            recommendMember.setName(recommendFriendCursor.getString(3));
                            recommendMember.setUserface(recommendFriendCursor.getString(4));
                            recommendMember.setTitle(recommendFriendCursor.getString(5));
                            recommendMember.setCompany(recommendFriendCursor.getString(6));
                            recommendMember.setIndustry(recommendFriendCursor.getString(7));
                            recommendMember.setLocation(recommendFriendCursor.getString(8));
                            recommendMember.setAccountType(recommendFriendCursor.getInt(9));
                            if (recommendFriendCursor.getInt(10) == 0) {
                                recommendMember.setRealname(false);
                            } else {
                                recommendMember.setRealname(true);
                            }
                            recommendMembers.add(recommendMember);
                        }
                        if (recommendMembers.size() > 0) {
                            RecommendMember[] reMembers = new RecommendMember[recommendMembers.size()];
                            for (int i = 0; i < recommendMembers.size(); i++) {
                                reMembers[i] = recommendMembers.get(i);
                            }
                            contentInfo.setMembers(reMembers);
                        }
                    }
                    recommendFriendCursor.close();
                    newNoticeList.setContentInfo(contentInfo);
                    newNoticeLists.add(newNoticeList);
                    if (count > 0) {
                        mCount++;
                        if (mCount >= count)
                            break;
                    }

                }
                if (newNoticeLists.size() > 0) {
                    NewNoticeList[] newNoticeLists2 = new NewNoticeList[newNoticeLists.size()];
                    for (int i = 0; i < newNoticeLists.size(); i++) {
                        newNoticeLists2[i] = newNoticeLists.get(i);
                    }
                    messageBoards.setNewNoticeList(newNoticeLists2);
                    timeCursor = getTimeBoardsCursor(RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_TIME);
                    if (null != timeCursor && timeCursor.moveToLast()) {
                        long maxCreatedDate = timeCursor.getLong(1);
                        long minCreatedDate = timeCursor.getLong(2);
                        messageBoards.setMaxRank(maxCreatedDate);
                        messageBoards.setMinRank(minCreatedDate);
                    }
                    timeCursor.close();
                }
            }

            return messageBoards;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
            if (null != contentCursor) {
                contentCursor.close();
            }
            if (null != atmemberCursor) {
                atmemberCursor.close();
            }
            if (null != picCursor) {
                picCursor.close();
            }
            if (null != userFaceCursor) {
                userFaceCursor.close();
            }
            if (null != replyCursor) {
                replyCursor.close();
            }
            if (null != likedCursor) {
                likedCursor.close();
            }
            if (null != timeCursor) {
                timeCursor.close();
            }
            if (null != recommendFriendCursor) {
                recommendFriendCursor.close();
            }
        }
        return null;
    }

    public void addNewMsgId(int messageboardPublicationId, String content, String atMembersStr, long createTime) {
        SQLiteDatabase dbWriter = AddMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("messageboardPublicationId", messageboardPublicationId);
        values.put("content", content);
        values.put("atmembers", atMembersStr);
        values.put("create_time", createTime);
        dbWriter.insert(AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE_ID, null, values);
    }

    public void addNewMsgPhoto(int messageboardPublicationId, String resourceId, String picPath, String content, long createTime) {
        SQLiteDatabase dbWriter = AddMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("create_time", createTime);
        values.put("messageboardPublicationId", messageboardPublicationId);
        values.put("messageboardPhotoResourceId", resourceId);
        values.put("thumbnailPicPath", picPath);
        values.put("bmiddlePicPath", picPath);
        dbWriter.insert(AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE, null, values);
    }

    public Queue<UploadPhotoCacheBean> getAllNeedUploadMsg() {
        Queue<UploadPhotoCacheBean> queue = null;
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = AddMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery("select * from " + AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE_ID, null);
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    long createTime = cursor.getLong(1);
                    int messageboardPublicationId = cursor.getInt(2);
                    String content = cursor.getString(3);
                    String atMemberStr = cursor.getString(4);
                    queue = queryNewMsg(createTime, content, atMemberStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCurser(cursor);
        }

        return queue;
    }

    public Cursor getNewMsgBoardsCursor(String table, long createTime) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = AddMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery("select * from " + table + " where create_time=?",
                    new String[]{String.valueOf(createTime)});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Queue<UploadPhotoCacheBean> queryNewMsg(long createTime, String content, String atMemberStr) {
        Queue<UploadPhotoCacheBean> uploadPhotoQueue = new LinkedList<UploadPhotoCacheBean>();
        UploadPhotoCacheBean uploadPhotoCacheBean = null;
        Cursor cursor = null;
        try {
            cursor = getNewMsgBoardsCursor(AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE, createTime);
            if (null != cursor) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        uploadPhotoCacheBean = new UploadPhotoCacheBean();
                        uploadPhotoCacheBean.setContent(content);
                        uploadPhotoCacheBean.setMessageboardPublicationId(cursor.getInt(2));
                        uploadPhotoCacheBean.setMessageboardPhotoResourceId(cursor.getString(3));
                        uploadPhotoCacheBean.setThumbnailPicPath(cursor.getString(4));
                        uploadPhotoCacheBean.setBmiddlePicPath(cursor.getString(5));
                        uploadPhotoCacheBean.setAtMemberStr(atMemberStr);
                        uploadPhotoQueue.offer(uploadPhotoCacheBean);
                    }
                } else {
                    uploadPhotoCacheBean = new UploadPhotoCacheBean();
                    uploadPhotoCacheBean.setContent(content);
                    uploadPhotoCacheBean.setAtMemberStr(atMemberStr);
                    uploadPhotoCacheBean.setMessageboardPublicationId(0);
                    uploadPhotoCacheBean.setMessageboardPhotoResourceId("");
                    uploadPhotoCacheBean.setThumbnailPicPath("");
                    uploadPhotoCacheBean.setBmiddlePicPath("");
                    uploadPhotoQueue.offer(uploadPhotoCacheBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCurser(cursor);
        }
        return uploadPhotoQueue;
    }

    public int deleteNewMsgID(int messageboardPublicationId) {
        SQLiteDatabase dbWriter = AddMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        try {
            return dbWriter.delete(AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE_ID, "messageboardPublicationId=?",
                    new String[]{String.valueOf(messageboardPublicationId)});
        } finally {
        }
    }

    public int deletePhoto(int messageboardPublicationId) {
        SQLiteDatabase dbWriter = AddMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        try {
            return dbWriter.delete(AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE, "messageboardPublicationId=?",
                    new String[]{String.valueOf(messageboardPublicationId)});
        } finally {

        }
    }

    public void updateNewMsg(String messageboardPublicationId, String messageboardPhotoResourceId, String thumbnailPicUrl,
                             String bmiddlePicUrl, int state) {
        SQLiteDatabase dbWriter = AddMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thumbnailPicUrl", thumbnailPicUrl);
        values.put("bmiddlePicUrl", bmiddlePicUrl);
        //		if (state == 1) {
        values.put("state", state);
        //		}
        dbWriter.update(AddSQLiteOpenHelper.TABLE_RENMAIQUAN_ADD_NEWMESSAGE, values, "messageboardPublicationId=?",
                new String[]{String.valueOf(messageboardPublicationId)});
    }

    public void deleteDatabase() {
        if (null != SQLiteStore.getInstance(context).getHelper()) {
            SQLiteStore.getInstance(context).getHelper().close();
        }
        if (null != FindSQLiteStore.getInstance(context).getHelper()) {
            FindSQLiteStore.getInstance(context).getHelper().close();
        }
        if (null != AddMsgSQLiteStore.getInstance(context).getHelper()) {
            AddMsgSQLiteStore.getInstance(context).getHelper().close();
        }
    }

    public void closeCurser(Cursor cursor) {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }
}
