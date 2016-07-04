package com.itcalf.renhe.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.actionprovider.PlusActionProvider;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.archives.edit.task.DeleteMsgTask;
import com.itcalf.renhe.context.relationship.AdvancedSearchIndexActivityTwo;
import com.itcalf.renhe.context.room.AddMessageBoardActivity;
import com.itcalf.renhe.context.room.db.AddNewMsgManager;
import com.itcalf.renhe.context.room.db.AddNewMsgSQLiteStore;
import com.itcalf.renhe.context.room.db.RenMaiQuanManager;
import com.itcalf.renhe.context.room.db.SQLiteStore;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.dto.AddMessageBoard;
import com.itcalf.renhe.dto.MessageBoardOperation;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.dto.UploadPhoto;
import com.itcalf.renhe.dto.UploadPhotoCacheBean;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.TipBox;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanUtils {
    private static Context ct;
    private static RenMaiQuanManager renMaiQuanManager;
    private static AddNewMsgManager addNewMsgManager;
    private ArrayList<MessageBoards.NewNoticeList> datas;
    private ArrayList<MessageBoards.NewNoticeList> addDatasList;
    private ProgressBar progressBar;//发送人脉圈留言的进度条
    private static TipBox tipBox;
    private static SharedPreferences msp;
    private ImageLoader imageLoader;

    public RenmaiQuanUtils(Context ct) {
        this.ct = ct;
        renMaiQuanManager = new RenMaiQuanManager(ct);
        addNewMsgManager = new AddNewMsgManager(ct);
        imageLoader = ImageLoader.getInstance();
    }

    public RenmaiQuanUtils(Context ct, ArrayList<MessageBoards.NewNoticeList> datasList,
                           ArrayList<MessageBoards.NewNoticeList> addDatasList, ProgressBar sendingPb) {
        this(ct);
        this.datas = datasList;
        this.addDatasList = addDatasList;
        this.progressBar = sendingPb;
    }

    @SuppressWarnings("unused")
    @SuppressLint("SimpleDateFormat")
    public void string2Date(long date, TextView dateTv) {
        long DAY = 24L * 60L * 60L * 1000L;
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        Date date2 = new Date(date);
        if (null != date2) {
            long diff = now.getTime() - date2.getTime();
            dateTv.setText(DateUtil.formatToGroupTagByDay(ct, date2));
            dateTv.setText("2013年12月12日");
        } else {
            dateTv.setText("");
        }

    }

    public int[] count(String text, String sub) {
        int count = 0, start = 0;
        while ((start = text.indexOf(sub, start)) >= 0) {
            start += sub.length();
            count++;
        }
        if (count == 0) {
            return null;
        }
        int a[] = new int[count];
        int count2 = 0;
        while ((start = text.indexOf(sub, start)) >= 0) {
            a[count2] = start;
            start += sub.length();
            count2++;
        }
        return a;
    }

    public static class MessageMemberSpanClick extends ClickableSpan implements View.OnClickListener {
        String id;

        public MessageMemberSpanClick(String id) {
            this.id = id;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false); // 去掉下划线
        }

        @Override
        public void onClick(View v) {
            if (null != id && !"".equals(id)) {
                Intent intent = new Intent(ct, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, id);
                ct.startActivity(intent);
                ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        }
    }

    /**
     * 删除对话框
     *
     * @param newNoticeList 删除的数据源
     * @param position      位置
     * @param deleteType    删除类型，1：删除留言/ 2：删除评论
     */
    public static void createSelfDialog(final MessageBoards.NewNoticeList newNoticeList, final int position,
                                        final int deleteType, final int replyId, final String replyObjectId,
                                        final ArrayList<MessageBoards.NewNoticeList> addDatasList) {
        MaterialDialogsUtil materialDialog = new MaterialDialogsUtil(ct);
        materialDialog.showSelectList(R.array.conversation_choice_items).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                switch (which) {
                    case 0:
                        int contentRes = R.string.renmaiquan_delete_message_tip;
                        if (deleteType == 2) {
                            contentRes = R.string.renmaiquan_delete_comment_tip;
                        }
                        MaterialDialogsUtil materialDialogsUtil = new MaterialDialogsUtil(ct);
                        materialDialogsUtil.getBuilder(contentRes).callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                if (-1 == NetworkUtil.hasNetworkConnection(ct)) {//网络未连接
                                    ToastUtil.showToast(ct, R.string.network_error_message);
                                    return;
                                }
                                if (deleteType == 1) {//如果是删留言，为了交互体验，采用异步删除，先删掉list中的item，再调用删除接口
                                    Intent intent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM);
                                    intent.putExtra("position", position);
                                    intent.putExtra("objectId", newNoticeList.getContentInfo().getObjectId());
                                    ct.sendBroadcast(intent);
                                }
                                if (newNoticeList.getType() == MessageBoards.MESSAGE_TYPE_ADD_NEWMSG) {
                                    if (null != addDatasList && !addDatasList.isEmpty()) {
                                        for (MessageBoards.NewNoticeList newNoticeList1 : addDatasList) {
                                            if (newNoticeList1.getContentInfo().getObjectId().equals
                                                    (newNoticeList.getContentInfo().getObjectId())) {
                                                addDatasList.remove(newNoticeList1);
                                                break;
                                            }
                                        }
                                    }
                                    addNewMsgManager.delete(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN,
                                            newNoticeList.getContentInfo().getObjectId());
                                } else {
                                    goDelete(deleteType, newNoticeList, position, replyId, replyObjectId);
                                }
                            }

                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                            }
                        });
                        materialDialogsUtil.show();
                        break;
                    case 1:
                        break;
                    default:
                        break;
                }
            }
        });
        materialDialog.show();
    }

    public void popupInputMethodWindow() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) ct
                        .getSystemService(Service.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 0);
    }

    /**
     * 删除留言、评论
     *
     * @param deleteType    删除类型，1：删除留言/ 2：删除评论
     * @param newNoticeList 删除留言的数据源
     * @param position      要删除的留言列表的position
     */
    public static void goDelete(final int deleteType, final MessageBoards.NewNoticeList newNoticeList,
                                final int position, int replyId, final String replyObjectId) {
        final MessageBoards.ContentInfo contentInfo = newNoticeList.getContentInfo();
        int type = newNoticeList.getType();
        new DeleteMsgTask(ct, type) {
            public void doPre() {
            }

            public void doPost(MessageBoardOperation result) {
                if (null != result) {
                    if (result.getState() == 1) {
                        if (deleteType == 1) {
//                        Intent intent = new Intent(ct.getString(R.string.action_rmq_delete_item));
//                        intent.putExtra("position", position);
//                        intent.putExtra("objectId", newNoticeList.getContentInfo().getObjectId());
//                        ct.sendBroadcast(intent);
                            new RenmaiQuanUtils(ct).deleteRenmaiQuanItem(newNoticeList.getContentInfo().getObjectId());
                        } else if (deleteType == 2) {
                            if (null != contentInfo.getReplyList()) {
                                updateRenmaiQuanItemDeleteReply(newNoticeList, replyObjectId);
                                Intent intent = new Intent();
                                intent.putExtra("refreshNoticeListItem", newNoticeList);
                                intent.putExtra("position", position);
                                intent.putExtra("isToReply", true);
                                intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                                ct.sendBroadcast(intent);
                            }

                        }
                    } else {
                        if (deleteType == 1) {
                            ToastUtil.showToast(ct, "这条留言不存在");
                        } else if (deleteType == 2) {
                            ToastUtil.showToast(ct, "这条评论不存在");
                        }
                    }
                } else {
                    ToastUtil.showToast(ct, R.string.network_error_message);
                }
            }

            ;
        }.executeOnExecutor(Executors.newCachedThreadPool(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getAdSId(), deleteType == 1 ?
                        "mainMessageBoard" : "replyMessageBoard", deleteType == 1 ? contentInfo.getId() + "" : replyId + "" + "",
                deleteType == 1 ? contentInfo.getObjectId() : replyObjectId);
    }

    public static void updateRenmaiQuanItemDeleteReply(MessageBoards.NewNoticeList newNoticeList, String replyObjectId) {
        if (null != newNoticeList) {
            MessageBoards.ContentInfo contentInfo = newNoticeList.getContentInfo();
            MessageBoards.ReplyList[] replyList = contentInfo.getReplyList();
            List<MessageBoards.ReplyList> replyLists = new ArrayList<>();
            for (int i = 0; i < replyList.length; i++) {
                replyLists.add(replyList[i]);
            }
            for (int i = 0; i < replyLists.size(); i++) {
                if (replyList[i].getObjectId().equals(replyObjectId)) {
                    replyLists.remove(i);
                    MessageBoards.ReplyList[] replyListTemp = new MessageBoards.ReplyList[replyLists.size()];
                    for (int k = 0; k < replyListTemp.length; k++) {
                        replyListTemp[k] = replyLists.get(k);
                    }
                    replyList = replyListTemp;
                    contentInfo.setReplyList(replyList);
                    contentInfo.setReplyNum(contentInfo.getReplyNum() <= 0 ? 0 : contentInfo.getReplyNum() - 1);
                    newNoticeList.setContentInfo(contentInfo);
                    break;
                }
            }
        }
    }

    /**
     * 新增评论，更新数据源
     *
     * @param refreshNoticeListItem 待更新的数据
     * @param replyContent          评论正文
     * @param replyObjectId         评论的objectId
     * @param replyId               评论的Id
     * @param orignSenderName       被回复的人名
     * @param orignSenderSid        被回复的人的Sid
     */
    public static void updateRenmaiQuanItemAddReply(MessageBoards.NewNoticeList refreshNoticeListItem,
                                                    String replyContent, String replyObjectId, String replyId,
                                                    String orignSenderName, String orignSenderSid) {
        String replyUserName = RenheApplication.getInstance().getUserInfo().getName();
        String replyUserSid = RenheApplication.getInstance().getUserInfo().getSid();
        if (null != refreshNoticeListItem) {
            MessageBoards.ContentInfo contentInfo = refreshNoticeListItem.getContentInfo();
            if (null == contentInfo)
                return;
            MessageBoards.ReplyList[] tempReplyList = contentInfo.getReplyList();
            MessageBoards.ReplyList[] replyLists = null;
            if (null != tempReplyList) {
                replyLists = new MessageBoards.ReplyList[tempReplyList.length + 1];
                System.arraycopy(tempReplyList, 0, replyLists, 0, tempReplyList.length);
            } else {
                replyLists = new MessageBoards.ReplyList[1];
            }
            //将自己的评论添加到评论列表数组末尾
            MessageBoards.ReplyList replyList = new MessageBoards.ReplyList();
            try {
                replyList.setId(Integer.parseInt(replyId));
            } catch (NumberFormatException e) {
                replyList.setId(0);
                e.printStackTrace();
            }

            replyList.setObjectId(replyObjectId);
            replyList.setContent(replyContent);
            if (!TextUtils.isEmpty(orignSenderName) && !TextUtils.isEmpty(orignSenderSid)) {
                replyList.setReSenderMemberName(orignSenderName);
                replyList.setReSenderSid(orignSenderSid);
            }
            replyList.setSenderName(replyUserName);
            replyList.setSenderSid(replyUserSid);
            replyLists[replyLists.length - 1] = replyList;

            contentInfo.setReplyList(replyLists);
            contentInfo.setReplyNum(contentInfo.getReplyNum() + 1);
            refreshNoticeListItem.setContentInfo(contentInfo);
        }
    }

    /**
     * 人脉圈点赞列表新增自己
     *
     * @param refreshNoticeListItem 待更新的数据
     */
    public static void updateRenmaiQuanItemAddLiked(MessageBoards.NewNoticeList refreshNoticeListItem) {
        MessageBoards.ContentInfo contentInfo = refreshNoticeListItem.getContentInfo();
        contentInfo.setLiked(true);
        contentInfo.setLikedNum(contentInfo.getLikedNum() + 1);
        MessageBoards.LikedList[] tempLikedList = contentInfo.getLikedList();
        MessageBoards.LikedList[] likedLists = null;
        if (null != tempLikedList) {
            likedLists = new MessageBoards.LikedList[tempLikedList.length + 1];
            System.arraycopy(tempLikedList, 0, likedLists, 0, tempLikedList.length);
        } else {
            likedLists = new MessageBoards.LikedList[1];
        }
        MessageBoards.LikedList meLikedList = new MessageBoards.LikedList();
        meLikedList.setName(RenheApplication.getInstance().getUserInfo().getName());
        meLikedList.setSid(RenheApplication.getInstance().getUserInfo().getSid());
        meLikedList.setUserface(RenheApplication.getInstance().getUserInfo().getUserface());
        likedLists[likedLists.length - 1] = meLikedList;//将自己添加到点赞列表的末尾
        contentInfo.setLikedList(likedLists);
        refreshNoticeListItem.setContentInfo(contentInfo);
    }

    /**
     * 人脉圈点击右上角“加好友”按钮后更新按钮状态
     *
     * @param refreshNoticeListItem 待更新的数据
     */
    public static void updateRenmaiQuanItemAddFriend(MessageBoards.NewNoticeList refreshNoticeListItem) {
        MessageBoards.SenderInfo senderInfo = refreshNoticeListItem.getSenderInfo();
        senderInfo.setFriendState(0);
        refreshNoticeListItem.setSenderInfo(senderInfo);
    }

    /**
     * 人脉圈点击右上角“加好友”按钮后更新数据库状态
     *
     * @param refreshNoticeListItem 待更新的数据
     */
    public void updateDBRenmaiQuanItemFriendState(final MessageBoards.NewNoticeList refreshNoticeListItem) {
        if (null != refreshNoticeListItem) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    renMaiQuanManager.updateFriendState(refreshNoticeListItem.getContentInfo().getObjectId(), 0);
                }
            }).start();
        }
    }

    /**
     * 删除某条人脉圈留言
     *
     * @param objectId objectId
     */
    public void deleteRenmaiQuanItem(final String objectId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                renMaiQuanManager.delete(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, objectId);
            }
        }).start();
    }

    /**
     * 删除某人发的所有本地缓存的人脉圈留言
     *
     * @param senderSid senderSid
     */
    public void deleteRenmaiQuanItemsBySid(final String senderSid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> objectIds = renMaiQuanManager.selectObjectIdsBySenderId(
                        SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, senderSid);
                renMaiQuanManager.deleteBySenderSid(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, senderSid);
                if (null != objectIds) {
                    for (String objectId : objectIds) {
                        renMaiQuanManager.delete(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_PIC_LIST, objectId);
                    }
                }
            }
        }).start();
    }

    /**
     * 检查是否有未读消息需要显示
     *
     * @param datasList
     */
    public static void checkUnreadNotice(ArrayList<MessageBoards.NewNoticeList> datasList) {
        int unreadCount = RenheApplication.getInstance().getUserSharedPreferences().getInt(
                Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_UNREAD_COUNT, 0);
        if (unreadCount > 0) {//添加未读新消息view
            MessageBoards.NewNoticeList mNewNoticeList = datasList.isEmpty() ? null : datasList.get(0);
            if (null == mNewNoticeList || mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE) {
                MessageBoards.NewNoticeList newNoticeList = new MessageBoards.NewNoticeList();
                newNoticeList.setType(MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE);
                datasList.add(0, newNoticeList);
            }
        }
    }

    /**
     * 在下拉刷新（new）时，检查是否有正在发布/发布失败 的新留言
     *
     * @return
     */
    public static boolean checkNeedAddNewMsg(ArrayList<MessageBoards.NewNoticeList> datasList) {
        if (datasList.size() > 0) {
            MessageBoards.NewNoticeList mNewNoticeList = datasList.get(0);
            if (mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE) {
                if (mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_ADD_NEWMSG) {
                    return true;
                }
            } else {
                if (datasList.size() > 1) {
                    mNewNoticeList = datasList.get(1);
                    if (mNewNoticeList.getType() != MessageBoards.MESSAGE_TYPE_ADD_NEWMSG) {
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 发布新留言
     *
     * @param addNewNoticeListItem
     */
    public void sendNewMsg(final MessageBoards.NewNoticeList addNewNoticeListItem) {
        StringBuilder atMemberBuilder = new StringBuilder();
        MessageBoards.AtMemmber[] atMemmbers = addNewNoticeListItem.getContentInfo().getAtMembers();
        String atMemberStr = "";
        if (null != atMemmbers && atMemmbers.length > 0) {
            for (MessageBoards.AtMemmber atMemmber : atMemmbers) {
                atMemberBuilder.append(atMemmber.getMemberSid() + ":" + atMemmber.getMemberName() + ";");
            }
            atMemberStr = atMemberBuilder.substring(0, atMemberBuilder.length() - 1);
        }
        int picNum = 0;
        final MessageBoards.PicList[] picList = addNewNoticeListItem.getContentInfo().getPicList();
        if (null != picList) {
            picNum = picList.length;
        }
        final Map<String, Object> reqParams = new HashMap<>();
        reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
        reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
        reqParams.put("content", addNewNoticeListItem.getContentInfo().getContent());
        reqParams.put("photoNum", picNum + "");
        reqParams.put("atMembers", atMemberStr);
        OkHttpClientManager.postAsyn(Constants.Http.PREPUBLISH_MESSAGEBOARD, reqParams,
                AddMessageBoard.class, new OkHttpClientManager.ResultCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        insertMsg(datas, addNewNoticeListItem);
                    }

                    @Override
                    public void onResponse(Object response) {
                        if (null != response) {
                            AddMessageBoard addMessageBoard = (AddMessageBoard) response;
                            if (addMessageBoard.getState() == 1) {
                                if (null != progressBar) {//更新progressbar进度
                                    progressBar.setVisibility(View.VISIBLE);
                                    updateProgressBar(progressBar, 100);//设置最大值
                                }
                                MessageBoards.ContentInfo contentInfo = addNewNoticeListItem.getContentInfo();
                                MessageBoards.PicList[] picLists = contentInfo.getPicList();
                                if (addMessageBoard.isPublishComplete()) {
                                    updateProgressBar(progressBar, 100);//如果发布的新留言是纯文本，没有图片，直接进度条走完
                                    hideProgressBar(progressBar);
                                    handleAfterComeplete(addNewNoticeListItem.getContentInfo().getObjectId(),
                                            addMessageBoard.getMessageboardId(), addNewNoticeListItem.getContentInfo().getContent(), "");
                                    updateDatasList(datas, addDatasList, null, addNewNoticeListItem,
                                            addMessageBoard.getMessageboardObjectId(),
                                            addMessageBoard.getMessageboardId(), addMessageBoard.getCreatedDate());
                                } else {
                                    if (null != picLists && picLists.length > 0) {
                                        progressBar.setProgress(10);//如果发布的新留言含有图片，首先走掉1/10
                                        int messageboardPublicationId = addMessageBoard.getMessageboardPublicationId();
                                        String[] resourceId = addMessageBoard.getMessageboardPhotoResourceIds();
                                        //将待上传的图片放入队列
                                        Queue<UploadPhotoCacheBean> uploadPhotoQueue = new LinkedList<>();
                                        for (int i = 0; i < picLists.length; i++) {
                                            picLists[i].setResourceId(resourceId[i]);
                                            if (picLists[i].getUploadState() == 0) {
                                                UploadPhotoCacheBean uploadPhotoCacheBean = new UploadPhotoCacheBean();
                                                uploadPhotoCacheBean.setContent(addNewNoticeListItem.getContentInfo().getContent());
                                                uploadPhotoCacheBean.setThumbnailPicPath(picLists[i].getThumbnailPicUrl());
                                                uploadPhotoCacheBean.setBmiddlePicPath(picLists[i].getBmiddlePicUrl());
                                                uploadPhotoCacheBean.setMessageboardPublicationId(addMessageBoard.getMessageboardPublicationId());
                                                uploadPhotoCacheBean.setMessageboardPhotoResourceId(resourceId[i]);
                                                uploadPhotoQueue.offer(uploadPhotoCacheBean);
                                            }
                                        }
                                        contentInfo.setMessageboardPublicationId(messageboardPublicationId);
                                        contentInfo.setPicList(picLists);
                                        addNewNoticeListItem.setContentInfo(contentInfo);
                                        List<UploadPhoto> uploadPhotos = new ArrayList<>();
                                        uploadPhoto(datas, addDatasList, uploadPhotos, addNewNoticeListItem,
                                                uploadPhotoQueue, progressBar, picLists.length, addMessageBoard.getCreatedDate());
                                    } else {
                                        updateProgressBar(progressBar, 100);//如果发布的新留言是纯文本，没有图片，直接进度条走完
                                        hideProgressBar(progressBar);
                                    }
                                }
                            } else {
                                insertMsg(datas, addNewNoticeListItem);
                            }
                        } else {
                            insertMsg(datas, addNewNoticeListItem);
                        }
                    }
                });
    }

    /**
     * 使用队列，上传图片
     *
     * @param uploadPhotoQueue 图片队列
     * @param progressBar      人脉圈头部的发送进度条
     * @param photoNum         上传的图片总数
     */
    private void uploadPhoto(final List<MessageBoards.NewNoticeList> datas,
                             final ArrayList<MessageBoards.NewNoticeList> addDatasList,
                             final List<UploadPhoto> uploadPhotos,
                             final MessageBoards.NewNoticeList addNewNoticeListItem,
                             final Queue<UploadPhotoCacheBean> uploadPhotoQueue,
                             final ProgressBar progressBar, final int photoNum, final long createDate) {
        if (!uploadPhotoQueue.isEmpty()) {
            final UploadPhotoCacheBean readyUploadPhotoCacheBean = uploadPhotoQueue.peek();
            final String picPath = readyUploadPhotoCacheBean.getThumbnailPicPath();
            File file = new File(picPath);
            if (file.exists()) {
                final Map<String, Object> reqParams = new HashMap<>();
                reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
                reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
                reqParams.put("messageboardPublicationId", readyUploadPhotoCacheBean.getMessageboardPublicationId());
                reqParams.put("resourceId", readyUploadPhotoCacheBean.getMessageboardPhotoResourceId());
                reqParams.put("androidPhotoType", 1);
                OkHttpClientManager.postPhotoAsyn(Constants.Http.UPLOAD_PHOTO,
                        "messageboardPhoto", file, reqParams, UploadPhoto.class,
                        new OkHttpClientManager.ResultCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                insertMsg(datas, addNewNoticeListItem);
                            }

                            @Override
                            public void onResponse(Object response) {
                                if (null != response) {
                                    UploadPhoto uploadPhoto = (UploadPhoto) response;
                                    if (uploadPhoto.getState() == 1) {
                                        uploadPhotos.add(uploadPhoto);
                                        if (!uploadPhoto.isPublishComplete()) {
                                            MessageBoards.ContentInfo contentInfo = addNewNoticeListItem.getContentInfo();
                                            MessageBoards.PicList[] picLists = contentInfo.getPicList();
                                            for (int i = 0; i < picLists.length; i++) {
                                                MessageBoards.PicList picList = picLists[i];
                                                if (picList.getResourceId().equals(readyUploadPhotoCacheBean.getMessageboardPhotoResourceId())) {
                                                    picList.setUploadState(1);
                                                    picLists[i] = picList;
                                                    contentInfo.setPicList(picLists);
                                                    addNewNoticeListItem.setContentInfo(contentInfo);
                                                    break;
                                                }
                                            }
                                        }
                                        uploadPhotoQueue.poll();
                                        int nowProgress = progressBar.getProgress();
                                        int afterProgress = nowProgress + (90 / photoNum);
                                        updateProgressBar(progressBar, afterProgress);//如果发布的新留言含有图片，没上传完成一张，进度条走掉相应的进度
                                        uploadPhoto(datas, addDatasList, uploadPhotos, addNewNoticeListItem, uploadPhotoQueue, progressBar, photoNum, createDate);
                                        if (uploadPhoto.isPublishComplete()) {
                                            handleAfterComeplete(addNewNoticeListItem.getContentInfo().getObjectId(),
                                                    uploadPhoto.getMessageboardId(), addNewNoticeListItem.getContentInfo().getContent(),
                                                    (uploadPhotos == null || uploadPhotos.size() == 0) ? null : uploadPhotos.get(0).getThumbnailPicUrl());
                                            updateDatasList(datas, addDatasList, uploadPhotos, addNewNoticeListItem,
                                                    uploadPhoto.getMessageboardObjectId(), uploadPhoto.getMessageboardId(),
                                                    createDate);
                                        }
                                    } else {
                                        insertMsg(datas, addNewNoticeListItem);
                                    }
                                } else {
                                    insertMsg(datas, addNewNoticeListItem);
                                }
                            }
                        }, Constants.RENMAIQUAN_CONSTANTS.RMQ_QUEST_TAG);
            }
        } else {
            updateProgressBar(progressBar, 100);//图片已经上传完成，直接进度条走完
            hideProgressBar(progressBar);
        }
    }

    private void handleAfterComeplete(String objectId, final int newMsgId, final String content, final String picUrl) {
        String syncToWeichat;
        //如果发布完成，同步到朋友圈
        if (RenheApplication.getInstance().getUserSharedPreferences()
                .getBoolean(Constants.SHAREDPREFERENCES_KEY.RENMAIQUAN_SYNC_TO_WEICHAT, false)) {
            imageLoader.loadImage(picUrl, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    shareToWeichat(newMsgId, content, picUrl);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    shareToWeichat(newMsgId, content, picUrl);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    shareToWeichat(newMsgId, content, picUrl);
                }
            });
            syncToWeichat = "1";
        } else {
            syncToWeichat = "0";
        }
        addNewMsgManager.delete(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN, objectId);
        new WriteLogThread(ct, "5.409", new String[]{ct.getString(R.string.versionname), syncToWeichat}).start();
    }

    private void shareToWeichat(int newMsgId, String content, String picUrl) {
        ShareUtil shareUtil = new ShareUtil(ct,
                RenheApplication.getInstance().getUserInfo().getName(), newMsgId,
                content, picUrl);
        shareUtil.share2Tencent(true);
    }

    /**
     * 更新进度条进度
     *
     * @param progressBar
     * @param progress
     */
    private static void updateProgressBar(ProgressBar progressBar, int progress) {
        if (null != progressBar) {//更新progressbar进度
            progressBar.setProgress(progress);//图片已上传完成，进度走完
        }
    }

    /**
     * 隐藏进度条
     *
     * @param progressBar
     */
    private static void hideProgressBar(final ProgressBar progressBar) {
        if (null != progressBar)
            progressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            }, 500);
    }

    private static void updateDatasList(List<MessageBoards.NewNoticeList> datas,
                                        ArrayList<MessageBoards.NewNoticeList> addDatasList,
                                        List<UploadPhoto> uploadPhotos,
                                        MessageBoards.NewNoticeList addNewNoticeListItem,
                                        String newMsgObjectId, int newMsgId, long createDate) {
        if (null != addDatasList) {
            for (MessageBoards.NewNoticeList newNoticeList : addDatasList) {
                if (newNoticeList.getContentInfo().getObjectId().equals(addNewNoticeListItem.getContentInfo().getObjectId())) {
                    addDatasList.remove(newNoticeList);
                    break;
                }
            }
//            addDatasList.remove(addNewNoticeListItem);
        }
        MessageBoards.ContentInfo contentInfo = addNewNoticeListItem.getContentInfo();
        String objectId = contentInfo.getObjectId();
        contentInfo.setObjectId(newMsgObjectId);
        contentInfo.setId(newMsgId);
        if (null != uploadPhotos && uploadPhotos.size() > 0) {
            MessageBoards.PicList[] picLists = new MessageBoards.PicList[uploadPhotos.size()];
            for (int i = 0; i < picLists.length; i++) {
                MessageBoards.PicList picList = new MessageBoards.PicList();
                picList.setThumbnailPicUrl(uploadPhotos.get(i).getThumbnailPicUrl());
                picList.setBmiddlePicUrl(uploadPhotos.get(i).getBmiddlePicUrl());
                picList.setBmiddlePicWidth(uploadPhotos.get(i).getBmiddlePicWidth());
                picList.setBmiddlePicHeight(uploadPhotos.get(i).getBmiddlePicHeight());
                picLists[i] = picList;
            }
            contentInfo.setPicList(picLists);
        }
        addNewNoticeListItem.setCreatedDate(createDate);
        addNewNoticeListItem.setContentInfo(contentInfo);
        addNewNoticeListItem.setType(MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
        for (int i = 0; i < datas.size(); i++) {
            if (null != datas.get(i).getContentInfo() && datas.get(i).getContentInfo().getObjectId().equals(objectId)) {
                Intent intent = new Intent();
                intent.putExtra("refreshNoticeListItem", addNewNoticeListItem);
                intent.putExtra("position", i);
                intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                ct.sendBroadcast(intent);
                break;
            }
        }
    }

    /**
     * 当发布留言第一步（发布文本内容）就失败时，将该条留言保存到数据库
     */
    private static void insertMsg(List<MessageBoards.NewNoticeList> datas, MessageBoards.NewNoticeList addNewNoticeListItem) {
        MessageBoards.ContentInfo contentInfo = addNewNoticeListItem.getContentInfo();
        String objectId = contentInfo.getObjectId();
        String newObjectId = System.currentTimeMillis() + "";
        addNewNoticeListItem.setUploadState(Constants.RENMAIQUAN_CONSTANTS.RMQ_UPLOAD_STATE_ERROR);
        for (int i = 0; i < datas.size(); i++) {
            if (null != datas.get(i).getContentInfo() && datas.get(i).getContentInfo().getObjectId().equals(objectId)) {
                Intent intent = new Intent();
                contentInfo.setObjectId(newObjectId);
                addNewNoticeListItem.setContentInfo(contentInfo);
                intent.putExtra("refreshNoticeListItem", addNewNoticeListItem);
                intent.putExtra("position", i);
                intent.setAction(Constants.BroadCastAction.REFRESH_RECYCLERVIEW_ITEM_RECEIVER_ACTION);
                ct.sendBroadcast(intent);
                break;
            }
        }
        contentInfo.setObjectId(newObjectId);
        addNewNoticeListItem.setContentInfo(contentInfo);
        addNewMsgManager.insert(addNewNoticeListItem);
        addNewMsgManager.delete(AddNewMsgSQLiteStore.AddNewMsgRenheSQLiteOpenHelper.TABLE_RENMAIQUAN, objectId);
    }

    /**
     * 刚注册进来的用户，有个popWindows引导框
     */
    public static void handleGuideNewFisher(final Context context) {
        msp = context.getSharedPreferences("regiser_guide_setting_info", 0);
        boolean isRegister = msp.getBoolean("regiser_messageboard" + RenheApplication.getInstance().getUserInfo().getSid(), false);
        Handler mHandler = new Handler();
        if (isRegister) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    {
                        tipBox = new TipBox(context, 4, new TipBox.OnItemClickListener() {
                            @Override
                            public void onItemClick() {
                                SharedPreferences.Editor editor2 = msp.edit();
                                editor2.putBoolean("regiser_messageboard" + RenheApplication.getInstance().getUserInfo().getSid(),
                                        false);
                                editor2.commit();
                            }
                        });
                        tipBox.showAtLocation(tipBox.getContentView(), Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0,
                                ((AppCompatActivity) context).getSupportActionBar().getHeight() +
                                        getStatusBarHeight(context) - 15);
                    }
                }
            }, 50);
        }
    }

    public static void handelGuideNewFisherWindow(Context context, boolean isVisibleToUser) {
        if (null == msp) {
            if (null != context) {
                msp = context.getSharedPreferences("regiser_guide_setting_info", 0);
            }
        }
        if (null != msp) {
            if (isVisibleToUser) {
                if (tipBox != null) {
                    boolean isRegister = msp.getBoolean("regiser_messageboard" + RenheApplication.getInstance().getUserInfo().getSid(),
                            false);
                    if (isRegister) {
                        tipBox.showAtLocation(tipBox.getContentView(), Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0,
                                ((AppCompatActivity) context).getSupportActionBar().getHeight() + getStatusBarHeight(context)
                                        - 15);
                    }
                }
            } else {
                if (tipBox != null && tipBox.isShowing()) {
                    tipBox.dismiss();
                }
            }
        }
    }

    public static void resetGuideNewFisherWindow() {
        if (tipBox != null) {
            tipBox.dismiss();
            tipBox = null;
        }
    }

    public static int getStatusBarHeight(Context context) {
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    public static PopupWindow createMenuPopupWindow(final Fragment fragment, final Activity mActivity, final PopupWindow pop, ListView listView) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        fragment.startActivityForResult(new Intent(fragment.getActivity(), AddMessageBoardActivity.class),
                                Constants.RENMAIQUAN_REQUEST_CODE.REQUEST_CODE_ADDNEWMSG);
                        fragment.getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        MobclickAgent.onEvent(fragment.getActivity(), "add_newmsg");
                        break;
                    case 1:
                        RenheIMUtil.showProgressDialog(ct, R.string.loading);
                        if (NetworkUtil.hasNetworkConnection(ct) != -1) {
                            PlusActionProvider.checkCircleCreationPrivilege(mActivity);//检查是否有权限创建圈子
                        } else
                            ToastUtil.showToast(ct, "网络异常");
                        RenheIMUtil.dismissProgressDialog();//如果网络异常加载提示消失
                        MobclickAgent.onEvent(ct, "create_circle");
                        StatisticsUtil.statisticsCustomClickEvent(mActivity.getString(R.string.android_btn_menu1_create_circle_click), 0, "", null);
                        break;
                    case 2:
                        MobclickAgent.onEvent(ct, "add_new_friend");
                        ct.startActivity(new Intent(ct, AdvancedSearchIndexActivityTwo.class));
                        ((Activity) ct).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                }
                if (pop != null) {
                    pop.dismiss();
                }
            }
        });
        return pop;
    }
}
