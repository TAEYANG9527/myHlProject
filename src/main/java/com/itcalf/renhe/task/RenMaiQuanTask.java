package com.itcalf.renhe.task;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.room.db.RenMaiQuanManager;
import com.itcalf.renhe.context.room.db.SQLiteStore;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.NetworkUtil;
import com.itcalf.renhe.utils.ToastUtil;

import java.util.ArrayList;

/**
 * Feature:留言列表异步加载
 */
public class RenMaiQuanTask extends AsyncTask<Object, Void, MessageBoards> {
    // 数据回调
    private IRoomBack mRoomBack;
    // 数据回调
    private Context mContext;

    private String adSId;
    private String sid;
    private Integer count;
    private Long minRank;
    private Long maxRank;
    private int androidPhotoType;

    private ArrayList<MessageBoards.NewNoticeList> datasList;

    private String requestType;
    private RenMaiQuanManager renMaiQuanManager;

    public RenMaiQuanTask(Context mContext,
                          Integer count, Long minRank, Long maxRank, int androidPhotoType,
                          ArrayList<MessageBoards.NewNoticeList> datasList, IRoomBack back) {
        this.mContext = mContext;
        this.adSId = RenheApplication.getInstance().getUserInfo().getAdSId();
        this.sid = RenheApplication.getInstance().getUserInfo().getSid();
        this.count = count;
        this.minRank = minRank;
        this.maxRank = maxRank;
        this.androidPhotoType = androidPhotoType;
        this.datasList = datasList;
        this.mRoomBack = back;
        renMaiQuanManager = new RenMaiQuanManager(mContext);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mRoomBack.onPre();
    }

    // 后台线程调用服务端接口
    @Override
    protected MessageBoards doInBackground(Object... params) {
        requestType = params[0].toString();
        if (-1 == NetworkUtil.hasNetworkConnection(mContext)) {//网络未连接
            return null;
        }
        try {
            return ((RenheApplication) mContext.getApplicationContext()).getMessageBoardCommand()
                    .getMsgBoards(adSId, sid, requestType, count, minRank, maxRank, androidPhotoType, mContext);
        } catch (Exception e) {
            if (Constants.DEBUG_MODE) {
                Log.e(Constants.TAG, "RenMaiQuanTask", e);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(MessageBoards result) {
        super.onPostExecute(result);
        if (null != result) {
            if (result.getState() == 1) {
                updateDB(result, requestType);//更新数据库'
                //统计人脉圈引导展示
//                MessageBoards.NewNoticeList[] newNoticeLists = result.getNewNoticeList();
//                if (null != newNoticeLists && newNoticeLists.length > 0) {
//                    for (MessageBoards.NewNoticeList newNoticeList : newNoticeLists) {
//                    }
//                }
            } else {
                if (requestType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE))
                    loadMoreDBWhenNetError();
                else {
                    mRoomBack.doPost(null);
                    ToastUtil.showNetworkError(mContext);
                }
            }
        } else {
            if (requestType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_MORE)) {
                loadMoreDBWhenNetError();
            } else {
                mRoomBack.doPost(null);
                ToastUtil.showNetworkError(mContext);
            }
        }
    }

    /**
     * 根据objectId检查新增的人脉圈留言是否和已经显示的列表中的某条重复 true：存在 false：不存在
     *
     * @param addItemObjectId 新增item的objectId
     */
    private boolean isItemExist(String addItemObjectId) {
        for (MessageBoards.NewNoticeList newNoticeList : datasList) {
            if (newNoticeList != null && null != newNoticeList.getContentInfo()) {
                String objectId = newNoticeList.getContentInfo().getObjectId();
                if (!TextUtils.isEmpty(objectId) && objectId.equals(addItemObjectId))
                    return true;
            }
        }
        return false;
    }

    /**
     * 更新数据库
     *
     * @param result      从server获取的最新数据
     * @param requestType 请求类型：new、more
     */
    private void updateDB(final MessageBoards result, final String requestType) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (null != result.getDeleteNoticeList()
                        && result.getDeleteNoticeList().length > 0) {
                    for (MessageBoards.DeleteNoticeList deleteNoticeList : result.getDeleteNoticeList()) {
                        if (null != deleteNoticeList) {
                            try {
                                renMaiQuanManager.delete(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN,
                                        deleteNoticeList.getObjectId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                renMaiQuanManager.updateTime(requestType, result.getMaxRank(), result.getMinRank());//更新maxRank、minRank
                if (null != result.getUpdatedNoticeList()
                        && result.getUpdatedNoticeList().length > 0) {
                    for (MessageBoards.UpdatedNoticeList updatedNoticeList : result.getUpdatedNoticeList()) {
                        if (null != updatedNoticeList) {
                            try {
                                renMaiQuanManager.updateLikedList(
                                        updatedNoticeList.getContentInfo().getObjectId(),
                                        updatedNoticeList.getContentInfo().getLikedList());
                                renMaiQuanManager.updateReplyList(
                                        updatedNoticeList.getContentInfo().getObjectId(),
                                        updatedNoticeList.getContentInfo().getReplyList());
                                renMaiQuanManager.updateLikedNum(
                                        updatedNoticeList.getContentInfo().getObjectId(),
                                        updatedNoticeList.getContentInfo().getLikedNum(),
                                        updatedNoticeList.getContentInfo().isLiked());
                                renMaiQuanManager.updateReplyNum(
                                        updatedNoticeList.getContentInfo().getObjectId(),
                                        updatedNoticeList.getContentInfo().getReplyNum());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                renMaiQuanManager.insert(result);//将新增的数据插入数据库
                //根据max、min从数据库取出相应数量的数据用于显示
                MessageBoards cacheResult = renMaiQuanManager.getMessageBoardsFromCursor(result.getMinRank(), result.getMaxRank(), Constants.RENMAIQUAN_CONSTANTS.REQUEST_COUNT);
                MessageBoards.NewNoticeList[] cacheNoticeList = cacheResult.getNewNoticeList();
                if (null != cacheNoticeList && cacheNoticeList.length > 0) {
                    if (requestType.equals(Constants.RENMAIQUAN_CONSTANTS.REQUEST_TYPE_NEW))
                        datasList.clear();
                    for (MessageBoards.NewNoticeList newNoticeList : cacheNoticeList) {
                        datasList.add(newNoticeList);
                    }
                }
                mRoomBack.doPost(result);//回调给fragment，做UI相关操作
            }
        }).start();
    }

    /**
     * 在加载更多时，网络错误，从数据库读取20条
     */
    private void loadMoreDBWhenNetError() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long maxRank = datasList.get(datasList.size() - 1).getRank();
                MessageBoards cacheResult = renMaiQuanManager.getMessageBoardsFromCursor(0, maxRank, Constants.RENMAIQUAN_CONSTANTS.REQUEST_COUNT);
                if (null != cacheResult) {
                    cacheResult.setIsEnd(false);
                    cacheResult.setUpdatedNoticeList(null);
                    cacheResult.setDeleteNoticeList(null);
                    //根据max、min从数据库取出相应数量的数据用于显示
                    MessageBoards.NewNoticeList[] cacheNoticeList = cacheResult.getNewNoticeList();
                    boolean hasNewMsg = false;//当本地数据库没有更多数据时，由于用的sql是between并且maxRank是datalist的最后一个的rank，造成取到的cacheNoticeList只有一项，并且是和datalist的最后一项重复
                    if (null != cacheNoticeList && cacheNoticeList.length > 0) {
                        for (MessageBoards.NewNoticeList newNoticeList : cacheNoticeList) {
                            if (!isItemExist(newNoticeList.getContentInfo().getObjectId())) {
                                datasList.add(newNoticeList);
                                hasNewMsg = true;
                            }
                        }
                        if (hasNewMsg)
                            mRoomBack.doPost(cacheResult);//回调给fragment，做UI相关操作
                        else
                            mRoomBack.doPost(null);
                    } else {
                        mRoomBack.doPost(null);
                    }
                } else {
                    mRoomBack.doPost(null);
                }
            }
        }).start();
    }

    public interface IRoomBack {

        void onPre();

        void doPost(MessageBoards result);

    }
}
