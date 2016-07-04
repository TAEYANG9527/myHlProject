package com.itcalf.renhe.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.viewholder.CollectionCircleViewHolder;
import com.itcalf.renhe.viewholder.CollectionMemberViewHolder;
import com.itcalf.renhe.viewholder.CollectionRmqTextAndImageViewHolder;
import com.itcalf.renhe.viewholder.CollectionRmqTextViewHolder;
import com.itcalf.renhe.viewholder.CollectionShareCircleViewHolder;
import com.itcalf.renhe.viewholder.CollectionShareMemberViewHolder;
import com.itcalf.renhe.viewholder.CollectionShareWebViewHolder;
import com.itcalf.renhe.viewholder.EmptyViewHolder;
import com.itcalf.renhe.viewholder.FooterViewHolder;
import com.itcalf.renhe.viewholder.RecyclerHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.renhe.heliao.idl.collection.MyCollection;

/**
 * 收藏列表的recycler adapter
 * Author:wang ning
 */
public class RecyclerCollectionItemAdapter extends BaseRecyclerAdapter<MyCollection.CollectionInfo> {
    //人脉圈类型
    public static final int ITEM_TYPE_RMQ_TEXT = 1;//收藏的人脉圈，普通留言，纯文字
    public static final int ITEM_TYPE_RMQ_TEXT_AND_PIC = 2;//收藏的人脉圈，图片+图片
    public static final int ITEM_TYPE_RMQ_SHARE_WEB = 3;//收藏的人脉圈，分享的网页
    public static final int ITEM_TYPE_RMQ_SHARE_MEMBER = 4;//收藏的人脉圈，分享的人脉
    public static final int ITEM_TYPE_RMQ_SHARE_CIRCLE = 5;//收藏的人脉圈，分享的圈子
    public static final int ITEM_TYPE_MEMBER = 6;//收藏的人脉
    public static final int ITEM_TYPE_CIRCLE = 7;//收藏的圈子
    public static final int ITEM_TYPE_FOOTER = 8;//footView
    public static final int ITEM_TYPE_END = 9;//没有更多数据
    public static final int ITEM_TYPE_READY_LOAD_MORE = 10;//加载更多失败时，提示上拉加载更多
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private ArrayList<MyCollection.CollectionInfo> datas;
    private RecyclerView collectRecyclerView;

    private boolean isShowFooter = false;
    private boolean isEnd = false;
    private boolean isShowTip = false;//加载更多失败时，提示上拉加载更多

    public RecyclerCollectionItemAdapter(Context context, RecyclerView view, ArrayList<MyCollection.CollectionInfo> datas) {
        super(view, datas, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.datas = datas;
        this.collectRecyclerView = view;
    }

    @Override
    public void convert(RecyclerHolder holder, MyCollection.CollectionInfo item, int position) {
        if (null != holder)
            holder.initView(holder, item, position);
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_RMQ_TEXT:
                return new CollectionRmqTextViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_rmq_text_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_RMQ_TEXT_AND_PIC:
                return new CollectionRmqTextAndImageViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_rmq_text_and_image_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_MEMBER:
                return new CollectionMemberViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_member_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_CIRCLE:
                return new CollectionCircleViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_circle_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_RMQ_SHARE_WEB:
                return new CollectionShareWebViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_share_web_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_RMQ_SHARE_MEMBER:
                return new CollectionShareMemberViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_share_member_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_RMQ_SHARE_CIRCLE:
                return new CollectionShareCircleViewHolder(mContext, mLayoutInflater.inflate(R.layout.collection_item_share_circle_layout, parent, false), collectRecyclerView, this);
            case ITEM_TYPE_FOOTER:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.rma_recycler_view_footer, parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_LOADING);
            case ITEM_TYPE_END:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.rma_recycler_view_footer, parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_END);
            case ITEM_TYPE_READY_LOAD_MORE:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.rma_recycler_view_footer, parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_READY);
            case -1:
                return new EmptyViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_empty_layout, parent, false), this);
            default:
                break;
        }
        return new EmptyViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_empty_layout, parent, false), this);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() > 5) {
            if (position == getItemCount() - 1 && isShowTip())
                return ITEM_TYPE_READY_LOAD_MORE;
            if (position == getItemCount() - 1 && isEnd())
                return ITEM_TYPE_END;
            if (position == getItemCount() - 1 && isShowFooter()) {
                return ITEM_TYPE_FOOTER;
            }
        }
        if (position < datas.size()) {
            MyCollection.CollectionInfo collectionInfo = datas.get(position);
            if (null == collectionInfo)
                return -1;
            //收藏内容类型 0=人脉圈 1=人脉 2=圈子
            MyCollection.CollectResquest.CollectionType collectionType = collectionInfo.getCollectionType();
            if (collectionType == MyCollection.CollectResquest.CollectionType.RENMAIQUAN) {
                MyCollection.MessageBoardInfo messageBoardInfo = collectionInfo.getMessageBoardInfo();
                MyCollection.MessageBoardShareInfo messageBoardShareInfo = collectionInfo.getMessageBoardShareInfo();
                if (null != messageBoardShareInfo && messageBoardShareInfo.getShareId() > 0 && messageBoardShareInfo.getShareType() >= 0) {
                    switch (messageBoardShareInfo.getShareType()) {
                        case 0:
                            return ITEM_TYPE_RMQ_SHARE_WEB;
                        case 1:
                            return ITEM_TYPE_RMQ_SHARE_MEMBER;
                        case 2:
                            return ITEM_TYPE_RMQ_SHARE_CIRCLE;
                    }
                }
                List<MyCollection.MessageBoardPhotoInfo> picList = messageBoardInfo.getPicListList();
                if (null != picList && !picList.isEmpty()) {
                    return ITEM_TYPE_RMQ_TEXT_AND_PIC;
                } else {
                    return ITEM_TYPE_RMQ_TEXT;
                }
            } else if (collectionType == MyCollection.CollectResquest.CollectionType.RENMAI) {
                return ITEM_TYPE_MEMBER;
            } else if (collectionType == MyCollection.CollectResquest.CollectionType.QUANZI) {
                return ITEM_TYPE_CIRCLE;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return datas == null ? 1 : datas.size() + 1;//多了一个footer
    }

    /**
     * 更新数据源中的某个item
     *
     * @param newNoticeList 新的数据
     * @param position      数据源列表中要更新的position
     */
    public void updateDatasItem(MyCollection.CollectionInfo newNoticeList, int position) {
        if (null == datas)
            return;
        if (position >= datas.size())
            return;
        datas.set(position, newNoticeList);
    }

    /**
     * 刷新RecyclerView某个item 点赞列表view的状态
     */
    public void updateItemGoodView(int position) {
        RecyclerView.ViewHolder viewHolder = collectRecyclerView.findViewHolderForAdapterPosition(position);
        if (null != viewHolder && viewHolder instanceof RecyclerHolder) {
            if (viewHolder instanceof RenmaiQuanViewHolder) {
                RenmaiQuanViewHolder holder = (RenmaiQuanViewHolder) viewHolder;
                holder.initGoodView(position);
            }
        }
    }

    /**
     * 刷新RecyclerView某个item 评论列表view的状态
     */
    public void updateItemReplyView(int position) {
        RecyclerView.ViewHolder viewHolder = collectRecyclerView.findViewHolderForAdapterPosition(position);
        if (null != viewHolder && viewHolder instanceof RecyclerHolder) {
            if (viewHolder instanceof RenmaiQuanViewHolder) {
                RenmaiQuanViewHolder holder = (RenmaiQuanViewHolder) viewHolder;
                holder.initReplyView(position);
            }
        }
    }

    public boolean isShowFooter() {
        return isShowFooter;
    }

    public void setIsShowFooter(boolean isShowFooter) {
        this.isShowFooter = isShowFooter;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isShowTip() {
        return isShowTip;
    }

    public void setIsShowTip(boolean isShowTip) {
        this.isShowTip = isShowTip;
    }

    public List<MyCollection.CollectionInfo> getDatasList() {
        return this.datas;
    }
}
