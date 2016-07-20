package com.itcalf.renhe.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.viewholder.EmptyViewHolder;
import com.itcalf.renhe.viewholder.FooterViewHolder;
import com.itcalf.renhe.viewholder.HeaderViewHolder;
import com.itcalf.renhe.viewholder.RecyclerHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanGuideRecommendFriendsViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanGuideTipViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanMultiPicViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanNormalTextViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanSendingMsgMultiPicViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanSendingMsgNormalTextViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanSendingMsgSinglePicViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanSinglePicViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithArchiveForwardViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithCircleForwardViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithCommunalForwardViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithMultiPicForwardViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithSinglePicForwardViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithTextForwardViewHolder;
import com.itcalf.renhe.viewholder.RenmaiQuanWithWebForwardViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脉圈列表的recycler adapter
 * Author:wang ning
 */
public class RecyclerRenmaiQuanItemAdapter extends BaseRecyclerAdapter<MessageBoards.NewNoticeList> {
    //人脉圈类型
    public static final int ITEM_TYPE_NORMAL_MESSAGE_TEXT = 1;//普通留言，纯文字
    public static final int ITEM_TYPE_NORMAL_MESSAGE_SINGLE_PIC = 2;//普通留言，单张图片
    public static final int ITEM_TYPE_NORMAL_MESSAGE_MULTI_PICS = 3;//普通留言，多张图片
    public static final int ITEM_TYPE_NORMAL_MESSAGE_WITH_TEXT_FORWARD = 4;//普通留言，附带纯文本转发内容
    public static final int ITEM_TYPE_NORMAL_MESSAGE_WITH_SINGLE_PIC_FORWARD = 5;//普通留言，附带单张图片转发内容
    public static final int ITEM_TYPE_NORMAL_MESSAGE_WITH_MULTI_PICS_FORWARD = 6;//普通留言，附带多张图片转发内容
    public static final int ITEM_TYPE_NORMAL_MESSAGE_WITH_WEB_FORWARD = 7;//普通留言，附带网页分享内容
    public static final int ITEM_TYPE_NORMAL_MESSAGE_CIRCLE = 8;//普通留言，分享的圈子
    public static final int ITEM_TYPE_NORMAL_MESSAGE_ARCHIVE = 9;//普通留言，分享的名片（档案）
    public static final int ITEM_TYPE_ARCHIVE_TEXT_INFO_UPDATE = 10;//动态更新（更新联系方式、更新教育经历、等），只有文本内容，等同于ITEM_TYPE_NORMAL_MESSAGE_TEXT
    public static final int ITEM_TYPE_ARCHIVE_IMAGE_INFO_UPDATE = 11;//动态更新（上传头像、更新档案封面），只有文本、图片内容等同于ITEM_TYPE_NORMAL_MESSAGE_SINGLE_PIC
    public static final int ITEM_TYPE_NEWFISHER_TEXT_GUIDE = 12;//新手引导（绑定手机、导入通讯录、等），文字内容配按钮
    public static final int ITEM_TYPE_NEWFISHER_LIST_GUIDE = 13;//新手引导（好友推荐、好友加入提醒、等），可供添加的好友列表内容
    public static final int ITEM_TYPE_FOOTER = 14;//footView
    public static final int ITEM_TYPE_END = 15;//没有更多数据
    public static final int ITEM_TYPE_READY_LOAD_MORE = 16;//加载更多失败时，提示上拉加载更多
    public static final int ITEM_TYPE_UNREAD_MSG = 17;//有新的未读消息
    public static final int ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_TEXT = 18;//正在发送的人脉圈普通留言，纯文字
    public static final int ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_SINGLE_PIC = 19;//正在发送的人脉圈普通留言，单张图片
    public static final int ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_MULTI_PICS = 20;//正在发送的人脉圈普通留言，多张图片
    public static final int ITEM_TYPE_VIP_UPGRADE_TIP = 21;//好友升级会员，文案+单张图片,UI和 type:2 一致
    public static final int ITEM_TYPE_NORMAL_MESSAGE_COMMUNAL = 22;//普通留言，分享的赞服务店铺
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private ArrayList<MessageBoards.NewNoticeList> datas;
    private RecyclerView renMaiQuanRecyclerView;

    private boolean isShowFooter = false;
    private boolean isEnd = false;
    private boolean isShowTip = false;//加载更多失败时，提示上拉加载更多

    public RecyclerRenmaiQuanItemAdapter(Context context, RecyclerView view, ArrayList<MessageBoards.NewNoticeList> datas) {
        super(view, datas, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.datas = datas;
        this.renMaiQuanRecyclerView = view;
    }

    @Override
    public void convert(RecyclerHolder holder, MessageBoards.NewNoticeList item, int position) {
        if (null != holder)
            holder.initView(holder, item, position);
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_NORMAL_MESSAGE_TEXT:
                return new RenmaiQuanNormalTextViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_text_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_SINGLE_PIC:
                return new RenmaiQuanSinglePicViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_single_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_MULTI_PICS:
                return new RenmaiQuanMultiPicViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_multi_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_WITH_TEXT_FORWARD:
                return new RenmaiQuanWithTextForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_text_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_WITH_SINGLE_PIC_FORWARD:
                return new RenmaiQuanWithSinglePicForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_single_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_WITH_MULTI_PICS_FORWARD:
                return new RenmaiQuanWithMultiPicForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_multi_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_WITH_WEB_FORWARD:
                return new RenmaiQuanWithWebForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_web_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_CIRCLE:
                return new RenmaiQuanWithCircleForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_circle_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_ARCHIVE:
                return new RenmaiQuanWithArchiveForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_archive_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NORMAL_MESSAGE_COMMUNAL:
                return new RenmaiQuanWithCommunalForwardViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_with_forward_archive_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_ARCHIVE_TEXT_INFO_UPDATE:
                break;
            case ITEM_TYPE_ARCHIVE_IMAGE_INFO_UPDATE:
                return new RenmaiQuanSinglePicViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_single_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NEWFISHER_TEXT_GUIDE:
                return new RenmaiQuanGuideTipViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_guide_tip_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_NEWFISHER_LIST_GUIDE:
                return new RenmaiQuanGuideRecommendFriendsViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_guide_recommend_friends_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_FOOTER:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.rma_recycler_view_footer, parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_LOADING);
            case ITEM_TYPE_END:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.rma_recycler_view_footer, parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_END);
            case ITEM_TYPE_READY_LOAD_MORE:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.rma_recycler_view_footer, parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_READY);
            case ITEM_TYPE_UNREAD_MSG:
                return new HeaderViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_unread_tip_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_TEXT:
                return new RenmaiQuanSendingMsgNormalTextViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_sending_msg_normal_text_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_SINGLE_PIC:
                return new RenmaiQuanSendingMsgSinglePicViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_sending_msg_normal_single_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_MULTI_PICS:
                return new RenmaiQuanSendingMsgMultiPicViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_sending_msg_normal_multi_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case ITEM_TYPE_VIP_UPGRADE_TIP:
                return new RenmaiQuanSinglePicViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_single_pic_layout, parent, false), renMaiQuanRecyclerView, this);
            case -1:
                return new EmptyViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_empty_layout, parent, false), this);
            default:
                break;
        }
        return new RenmaiQuanNormalTextViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_normal_text_layout, parent, false), renMaiQuanRecyclerView, this);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() > 2) {
            if (position == getItemCount() - 1 && isShowTip())
                return ITEM_TYPE_READY_LOAD_MORE;
            if (position == getItemCount() - 1 && isEnd())
                return ITEM_TYPE_END;
            if (position == getItemCount() - 1 && isShowFooter()) {
                return ITEM_TYPE_FOOTER;
            }
        }
        if (position < datas.size()) {
            MessageBoards.NewNoticeList newNoticeList = datas.get(position);
            int msgType = newNoticeList.getType();//人脉圈类型
            MessageBoards.ContentInfo contentInfo = newNoticeList.getContentInfo();
            if (msgType == MessageBoards.MESSAGE_TYPE_UNREAD_NOTICE)
                return ITEM_TYPE_UNREAD_MSG;
            if (null == contentInfo)
                return -1;
            switch (msgType) {
                case MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD:
                    int picNum = contentInfo.getPicList() == null ? 0 : contentInfo.getPicList().length;
                    if (picNum > 1) {//多张图片
                        return ITEM_TYPE_NORMAL_MESSAGE_MULTI_PICS;
                    } else if (picNum == 1) {
                        return ITEM_TYPE_NORMAL_MESSAGE_SINGLE_PIC;
                    } else {
                        MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
                        if (null == forwardMessageBoardInfo)
                            return ITEM_TYPE_NORMAL_MESSAGE_TEXT;
                        int forwardType = forwardMessageBoardInfo.getType();//转发类型
                        String forwardContent = forwardMessageBoardInfo.getContent();
                        switch (forwardType) {
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_WEB://网页分享
                                return ITEM_TYPE_NORMAL_MESSAGE_WITH_WEB_FORWARD;
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_PROFILE://档案分享
                                return ITEM_TYPE_NORMAL_MESSAGE_ARCHIVE;
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_CIRCLE://圈子分享
                                return ITEM_TYPE_NORMAL_MESSAGE_CIRCLE;
                            case Constants.RenmaiquanShareType.RENMAIQUAN_TYPE_COMMUNAL://赞服务分享
                                return ITEM_TYPE_NORMAL_MESSAGE_COMMUNAL;
                        }
                        int forwardPicNum = forwardMessageBoardInfo.getPicLists() == null ? 0 : forwardMessageBoardInfo.getPicLists().length;
                        if (forwardPicNum > 1) {//多张图片
                            return ITEM_TYPE_NORMAL_MESSAGE_WITH_MULTI_PICS_FORWARD;
                        } else if (forwardPicNum == 1) {
                            return ITEM_TYPE_NORMAL_MESSAGE_WITH_SINGLE_PIC_FORWARD;
                        } else {
                            if (!TextUtils.isEmpty(forwardContent))
                                return ITEM_TYPE_NORMAL_MESSAGE_WITH_TEXT_FORWARD;
                            else
                                return ITEM_TYPE_NORMAL_MESSAGE_TEXT;
                        }
                    }
                case MessageBoards.MESSAGE_TYPE_MEMBER_UPDATE_CONTACT:
                case MessageBoards.MESSAGE_TYPE_PROFILE_ADD_POSITION:
                case MessageBoards.MESSAGE_TYPE_PROFILE_UPDATE_POSITION:
                case MessageBoards.MESSAGE_TYPE_PROFILE_ADD_EDUCATION:
                case MessageBoards.MESSAGE_TYPE_PROFILE_UPDATE_EDUCATION:
                case MessageBoards.MESSAGE_TYPE_PUBLISH_JOG:
                case MessageBoards.MESSAGE_TYPE_PUBLISH_ACTIVITY:
                case MessageBoards.MESSAGE_TYPE_PUBLISH_XIAOZU:
                    return ITEM_TYPE_ARCHIVE_TEXT_INFO_UPDATE;
                case MessageBoards.MESSAGE_TYPE_MEMBER_UPDATE_USER_FACE:
                case MessageBoards.MESSAGE_TYPE_PROFILE_UPDATE_COVER:
                case MessageBoards.MESSAGE_TYPE_REALNAME:
                    return ITEM_TYPE_ARCHIVE_IMAGE_INFO_UPDATE;
                case MessageBoards.MESSAGE_TYPE_FRIEND_JOIN_NOTICE:
                case MessageBoards.MESSAGE_TYPE_RECOMMEND_FRIEND:
                    return ITEM_TYPE_NEWFISHER_LIST_GUIDE;
                case MessageBoards.MESSAGE_TYPE_BINDPHONE:
                case MessageBoards.MESSAGE_TYPE_IMPORT_CONTACT:
                case MessageBoards.MESSAGE_TYPE_PERFECT_PROFILE:
                case MessageBoards.MESSAGE_TYPE_UPLOAD_AVATAR:
                case MessageBoards.MESSAGE_TYPE_WEBSITE:
                    return ITEM_TYPE_NEWFISHER_TEXT_GUIDE;
                case MessageBoards.MESSAGE_TYPE_ADD_NEWMSG:
                    MessageBoards.PicList[] picLists = contentInfo.getPicList();
                    if (null == picLists || picLists.length <= 0) {
                        return ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_TEXT;
                    } else if (picLists.length == 1) {
                        return ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_SINGLE_PIC;
                    } else {
                        return ITEM_TYPE_SENDING_MSG_NORMAL_MESSAGE_MULTI_PICS;
                    }
                case MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP:
                    int vipPicNum = contentInfo.getPicList() == null ? 0 : contentInfo.getPicList().length;
                    if (vipPicNum == 1) {
                        return ITEM_TYPE_VIP_UPGRADE_TIP;
                    }
                    return ITEM_TYPE_NORMAL_MESSAGE_TEXT;
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
    public void updateDatasItem(MessageBoards.NewNoticeList newNoticeList, int position) {
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
        RecyclerView.ViewHolder viewHolder = renMaiQuanRecyclerView.findViewHolderForAdapterPosition(position);
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
        RecyclerView.ViewHolder viewHolder = renMaiQuanRecyclerView.findViewHolderForAdapterPosition(position);
        if (null != viewHolder && viewHolder instanceof RecyclerHolder) {
            if (viewHolder instanceof RenmaiQuanViewHolder) {
                RenmaiQuanViewHolder holder = (RenmaiQuanViewHolder) viewHolder;
                holder.initReplyView(position);
            }
        }
    }

    /**
     * 刷新RecyclerView某个item 右上角“加好友”按钮的状态
     */
    public void updateItemAddFriendView(int position) {
        RecyclerView.ViewHolder viewHolder = renMaiQuanRecyclerView.findViewHolderForAdapterPosition(position);
        if (null != viewHolder && viewHolder instanceof RecyclerHolder) {
            if (viewHolder instanceof RenmaiQuanViewHolder) {
                RenmaiQuanViewHolder holder = (RenmaiQuanViewHolder) viewHolder;
                holder.initNotFriendView(position);
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

    public List<MessageBoards.NewNoticeList> getDatasList() {
        return this.datas;
    }

}
