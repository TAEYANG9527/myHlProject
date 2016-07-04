package com.itcalf.renhe.context.room.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.MessageBoards.AtMemmber;
import com.itcalf.renhe.dto.MessageBoards.ContentInfo;
import com.itcalf.renhe.dto.MessageBoards.ForwardMessageBoardInfo;
import com.itcalf.renhe.dto.MessageBoards.NewNoticeList;
import com.itcalf.renhe.dto.MessageBoards.PicList;
import com.itcalf.renhe.dto.MessageBoards.SenderInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 未发布成功的人脉圈
 */
public class AddNewMsgManager {

    private Context context;

    public AddNewMsgManager(Context context) {
        this.context = context;
    }

    public void insert(NewNoticeList noticeList) {
        String sid = RenheApplication.getInstance().getUserInfo().getSid();
        if (null != noticeList) {
            SQLiteDatabase dbWriter = AddNewMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
            ContentValues values;
            if (null != noticeList) {
                values = new ContentValues();
                values.put("type", MessageBoards.MESSAGE_TYPE_ADD_NEWMSG);
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
                }
                if (noticeList.getContentInfo() != null) {
                    ContentInfo contentInfo = noticeList.getContentInfo();
                    values.put("objectId", contentInfo.getObjectId());
                    values.put("content", contentInfo.getContent());
                    values.put("subject", contentInfo.getSubject());
                    values.put("url", contentInfo.getUrl());
                    values.put("messageboardPublicationId", contentInfo.getMessageboardPublicationId());
                }
                values.put("createDate", noticeList.getCreatedDate());
                values.put("source", noticeList.getSource());
                values.put("score", noticeList.getRank());
                values.put("sid", sid);

                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN, null, values);
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
                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, null, values);

                if (noticeList.getContentInfo() != null) {

                    ContentInfo contentInfo = noticeList.getContentInfo();
                    ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();

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
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);

                            }
                            for (AtMemmber atMemmber : forwardMessageBoardInfo.getAtMembers()) {
                                values = new ContentValues();
                                values.put("objectId", contentInfo.getObjectId());
                                values.put("forward_memberSid", atMemmber.getMemberSid());
                                values.put("forward_memberName", atMemmber.getMemberName());
                                values.put("sid", sid);
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);

                            }
                        } else if (!isContentAtMemberEmpty && isForwardAtMemberEmpty) {
                            for (AtMemmber atMemmber : contentInfo.getAtMembers()) {
                                values = new ContentValues();
                                values.put("objectId", contentInfo.getObjectId());
                                values.put("memberSid", atMemmber.getMemberSid());
                                values.put("memberName", atMemmber.getMemberName());
                                values.put("sid", sid);
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);

                            }
                        } else if (isContentAtMemberEmpty && !isForwardAtMemberEmpty) {
                            for (AtMemmber atMemmber : forwardMessageBoardInfo.getAtMembers()) {
                                values = new ContentValues();
                                values.put("objectId", contentInfo.getObjectId());
                                values.put("forward_memberSid", atMemmber.getMemberSid());
                                values.put("forward_memberName", atMemmber.getMemberName());
                                values.put("sid", sid);
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, null, values);
                            }
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
                                values.put("resourceId", picList.getResourceId());
                                values.put("uploadState", picList.getUploadState());
                                values.put("sid", sid);
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
                            }
                            for (PicList picList : forwardMessageBoardInfo.getPicLists()) {
                                values = new ContentValues();
                                values.put("objectId", contentInfo.getObjectId());
                                values.put("forward_thumbnailPicUrl", picList.getThumbnailPicUrl());
                                values.put("forward_bmiddlePicUrl", picList.getBmiddlePicUrl());
                                values.put("forward_bmiddlePicWidth", picList.getBmiddlePicWidth());
                                values.put("forward_bmiddlePicHeight", picList.getBmiddlePicHeight());
                                values.put("sid", sid);
                                values.put("resourceId", picList.getResourceId());
                                values.put("uploadState", picList.getUploadState());
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
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
                                values.put("resourceId", picList.getResourceId());
                                values.put("uploadState", picList.getUploadState());
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
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
                                values.put("resourceId", picList.getResourceId());
                                values.put("uploadState", picList.getUploadState());
                                dbWriter.insert(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, null, values);
                            }
                        }

                    }
                }

            }

        }
    }

    public boolean isExist(String objectId) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = AddNewMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery(
                    "select count(*) from " + AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN + " where objectId=? and sid=?",
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
        SQLiteDatabase dbWriter = AddNewMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
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
            SQLiteDatabase dbReader = AddNewMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
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
        SQLiteDatabase dbWriter = AddNewMsgSQLiteStore.getInstance(context).getHelper().getWritableDatabase();
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

    public Cursor getMainBoardsCursor(String table) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = AddNewMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery("select * from " + table + " WHERE sid=" + "\'"
                    + RenheApplication.getInstance().getUserInfo().getSid() + "\'" + " ORDER BY createDate DESC ", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    public Cursor getTimeBoardsCursor(String table) {
        Cursor cursor = null;
        try {
            SQLiteDatabase dbReader = AddNewMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
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
            SQLiteDatabase dbReader = AddNewMsgSQLiteStore.getInstance(context).getHelper().getReadableDatabase();
            cursor = dbReader.rawQuery("select * from " + table + " where objectId=? and sid=?",
                    new String[]{String.valueOf(objectId), RenheApplication.getInstance().getUserInfo().getSid()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cursor;
    }

    /**
     * @return
     */
    public MessageBoards getMessageBoardsFromCursor() {
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
            cursor = getMainBoardsCursor(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN);
            if (null != cursor) {
                List<NewNoticeList> newNoticeLists = new ArrayList<NewNoticeList>();
                while (cursor.moveToNext()) {
                    newNoticeList = new NewNoticeList();
                    contentInfo = new ContentInfo();
                    forwardMessageBoardInfo = new ForwardMessageBoardInfo();
//                    int type = cursor.getInt(1);
                    newNoticeList.setType(MessageBoards.MESSAGE_TYPE_ADD_NEWMSG);
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
                    newNoticeList.setSenderInfo(senderInfo);
                    String objectId = cursor.getString(11);
                    contentInfo.setObjectId(objectId);
                    contentInfo.setContent(cursor.getString(12));
                    newNoticeList.setCreatedDate(cursor.getLong(13));
                    newNoticeList.setSource(cursor.getInt(14));
                    newNoticeList.setRank(cursor.getLong(15));
                    contentInfo.setSubject(cursor.getString(17));
                    contentInfo.setUrl(cursor.getString(18));
                    contentInfo.setMessageboardPublicationId(cursor.getInt(19));
                    contentCursor = getBoardsCursor(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_CONTENT, objectId);
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
                    atmemberCursor = getBoardsCursor(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_AT_MEMBER, objectId);
                    if (null != atmemberCursor) {
                        List<AtMemmber> contentAtMemmbers = new ArrayList<AtMemmber>();
                        List<AtMemmber> forwardAtMemmbers = new ArrayList<AtMemmber>();
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
                    picCursor = getBoardsCursor(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, objectId);
                    if (null != picCursor) {
                        List<PicList> contentPicList = new ArrayList<PicList>();
                        List<PicList> forwardPicList = new ArrayList<PicList>();
                        while (picCursor.moveToNext()) {
                            if (!TextUtils.isEmpty(picCursor.getString(2)) && !TextUtils.isEmpty(picCursor.getString(3))) {
                                PicList picList = new PicList();
                                picList.setThumbnailPicUrl(picCursor.getString(2));
                                picList.setBmiddlePicUrl(picCursor.getString(3));
                                picList.setBmiddlePicWidth(picCursor.getInt(6));
                                picList.setBmiddlePicHeight(picCursor.getInt(7));
                                picList.setResourceId(picCursor.getString(11));
                                picList.setUploadState(picCursor.getInt(12));
                                contentPicList.add(picList);
                            }
                            if (!TextUtils.isEmpty(picCursor.getString(4)) && !TextUtils.isEmpty(picCursor.getString(5))) {
                                PicList picList1 = new PicList();
                                picList1.setThumbnailPicUrl(picCursor.getString(4));
                                picList1.setBmiddlePicUrl(picCursor.getString(5));
                                picList1.setBmiddlePicWidth(picCursor.getInt(8));
                                picList1.setBmiddlePicHeight(picCursor.getInt(9));
                                picList1.setResourceId(picCursor.getString(11));
                                picList1.setUploadState(picCursor.getInt(12));
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

                    newNoticeList.setContentInfo(contentInfo);
                    newNoticeLists.add(newNoticeList);
                }
                if (newNoticeLists.size() > 0) {
                    NewNoticeList[] newNoticeLists2 = new NewNoticeList[newNoticeLists.size()];
                    for (int i = 0; i < newNoticeLists.size(); i++) {
                        newNoticeLists2[i] = newNoticeLists.get(i);
                    }
                    messageBoards.setNewNoticeList(newNoticeLists2);
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

    public void deleteDatabase() {
        if (null != AddNewMsgSQLiteStore.getInstance(context).getHelper()) {
            AddNewMsgSQLiteStore.getInstance(context).getHelper().close();
        }
    }

    public void closeCurser(Cursor cursor) {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }
}
