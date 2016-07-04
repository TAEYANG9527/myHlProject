package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.RecyclerRenmaiQuanItemAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.AddFriendTask;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.context.upgrade.UpgradeActivity;
import com.itcalf.renhe.dto.AddFriend;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.utils.DensityUtil;
import com.itcalf.renhe.utils.MaterialDialogsUtil;
import com.itcalf.renhe.utils.RenmaiQuanUtils;
import com.itcalf.renhe.utils.StatisticsUtil;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.http.okhttp.OkHttpClientManager;
import com.itcalf.renhe.view.ActionItem;
import com.itcalf.renhe.view.ReplyPopupWindow;
import com.itcalf.renhe.view.SharePopupWindow;
import com.itcalf.renhe.view.TitlePopup;
import com.itcalf.renhe.widget.emojitextview.AisenReplyTextView;
import com.squareup.okhttp.Request;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author wangning  on 2015/10/12.
 */
public class RenmaiQuanViewHolder extends RecyclerHolder {
    public RecyclerView renmaiQuanRecyclerView;
    public MessageBoards.NewNoticeList newNoticeList;
    public MessageBoards.ContentInfo contentInfo;
    public MessageBoards.SenderInfo senderInfo;
    //item 头部布局
    public ImageView userHeadIv;
    public TextView userNameTv;
    public TextView userTitleTv;
    public TextView createTimeTv;
    public TextView addFriendTv;//人脉圈动态发送者不是好友，显示加好友按钮
    public ImageView userIdentityIv;//身份图标，vip、实名认证等
    public RelativeLayout userInfoRl;

    //item 尾部布局
    public ImageView moreIv;
    public LinearLayout goodLl;
    public ImageView goodIv;
    public TextView goodTv;
    public ImageView goodIvOne;
    public ImageView goodIvTwo;
    public ImageView goodIvThree;
    public ImageView goodIvFour;
    public ImageView goodIvFive;
    public AisenReplyTextView replyTvOne;
    public AisenReplyTextView replyTvTwo;
    public AisenReplyTextView replyTvThree;
    public AisenReplyTextView replyTvFour;
    public AisenReplyTextView replyTvFive;
    public LinearLayout replyLl;
    public TextView replySeemoreTv;
    public RelativeLayout contentRl;//人脉圈正文
    public View goodReplySeperateLine;
    public TitlePopup moreItemPopup;


    public DateUtil dateUtil;
    public RenmaiQuanUtils renmaiQuanUtils;
    public RecyclerRenmaiQuanItemAdapter recyclerRenmaiQuanItemAdapter;
    public MaterialDialogsUtil materialDialogsUtil;
    //常量初始化
    public static final int RENMAIQUAN_REPLY_MAX_NUM = 5;

    public RenmaiQuanViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, adapter);
        if (null != adapter && adapter instanceof RecyclerRenmaiQuanItemAdapter)
            recyclerRenmaiQuanItemAdapter = (RecyclerRenmaiQuanItemAdapter) adapter;
        this.renmaiQuanRecyclerView = renmaiQuanRecyclerView;
        userHeadIv = (ImageView) itemView.findViewById(R.id.avatar_iv);
        userNameTv = (TextView) itemView.findViewById(R.id.username_tv);
        userTitleTv = (TextView) itemView.findViewById(R.id.title_tv);
        createTimeTv = (TextView) itemView.findViewById(R.id.datetime_tv);
        addFriendTv = (TextView) itemView.findViewById(R.id.addfriend_tv);
        userIdentityIv = (ImageView) itemView.findViewById(R.id.identity_iv);
        userInfoRl = (RelativeLayout) itemView.findViewById(R.id.userinfo_rl);

        moreIv = (ImageView) itemView.findViewById(R.id.more_iv);
        goodLl = (LinearLayout) itemView.findViewById(R.id.good_ll);
        goodIv = (ImageView) itemView.findViewById(R.id.good_iv);
        goodTv = (TextView) itemView.findViewById(R.id.good_tv);
        goodIvOne = (ImageView) itemView.findViewById(R.id.good_iv_one);
        goodIvTwo = (ImageView) itemView.findViewById(R.id.good_iv_two);
        goodIvThree = (ImageView) itemView.findViewById(R.id.good_iv_three);
        goodIvFour = (ImageView) itemView.findViewById(R.id.good_iv_four);
        goodIvFive = (ImageView) itemView.findViewById(R.id.good_iv_five);
        replyTvOne = (AisenReplyTextView) itemView.findViewById(R.id.reply_tv_one);
        replyTvTwo = (AisenReplyTextView) itemView.findViewById(R.id.reply_tv_two);
        replyTvThree = (AisenReplyTextView) itemView.findViewById(R.id.reply_tv_three);
        replyTvFour = (AisenReplyTextView) itemView.findViewById(R.id.reply_tv_four);
        replyTvFive = (AisenReplyTextView) itemView.findViewById(R.id.reply_tv_five);
        replyLl = (LinearLayout) itemView.findViewById(R.id.reply_ll);
        replySeemoreTv = (TextView) itemView.findViewById(R.id.reply_seemore_tv);
        goodReplySeperateLine = itemView.findViewById(R.id.good_reply_sepreateView);
        contentRl = (RelativeLayout) itemView.findViewById(R.id.content_Rl);
        moreItemPopup = new TitlePopup(context,
                DensityUtil.dip2px(context, Constants.RenMaiQuanPOPWindowParams.WIDTH_MAX),
                DensityUtil.dip2px(context, Constants.RenMaiQuanPOPWindowParams.HEIGHT_MAX));
        this.dateUtil = new DateUtil();
        renmaiQuanUtils = new RenmaiQuanUtils(context);
        materialDialogsUtil = new MaterialDialogsUtil(context);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, final int position) {
        if (null == holder)
            return;
        if (null == mNewNoticeList)
            return;
        if (mNewNoticeList instanceof MessageBoards.NewNoticeList)
            newNoticeList = (MessageBoards.NewNoticeList) mNewNoticeList;
        contentInfo = newNoticeList.getContentInfo();
        if (null == contentInfo)
            return;
        senderInfo = newNoticeList.getSenderInfo();
        initSelfInfoView();
        initNotFriendView(position);
        initGoodView(position);
        initMorePopWindow(position);
        initReplyView(position);
        userInfoRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(context, "renmai_avartar");
                Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, newNoticeList.getSenderInfo().getSid());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        contentRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = newNoticeList.getType();
                switch (type) {
                    case MessageBoards.MESSAGE_TYPE_BINDPHONE:
                    case MessageBoards.MESSAGE_TYPE_FRIEND_JOIN_NOTICE:
                    case MessageBoards.MESSAGE_TYPE_IMPORT_CONTACT:
                    case MessageBoards.MESSAGE_TYPE_RECOMMEND_FRIEND:
                    case MessageBoards.MESSAGE_TYPE_ADD_NEWMSG:
                        break;
                    case MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP:
                        context.startActivity(new Intent(context, UpgradeActivity.class));
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        Map<String, String> statisticsMap = new HashMap<>();//阿里云统计自定义事件自定义参数
                        statisticsMap.put("type", "1");
                        StatisticsUtil.statisticsCustomClickEvent(context.getString(R.string.android_btn_pop_upgrade_click), 0, "", statisticsMap);
                        break;
                    default:
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("result", newNoticeList);
                        bundle.putInt("position", position);
                        Intent intent = new Intent(context, TwitterShowMessageBoardActivity.class);
                        intent.putExtras(bundle);
                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                        break;
                }
            }
        });
        final String content = contentInfo.getContent();
        contentRl.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                if (newNoticeList.getType() != MessageBoards.MESSAGE_TYPE_VIP_UPGRADE_TIP) {
                    new ContentUtil().createRenMaiQuanDialog(context, 1, content, newNoticeList);
                    return true;
                }
                return false;
            }
        });
        moreIv.setOnClickListener(new View.OnClickListener() {//弹出评论、赞框

            @Override
            public void onClick(View arg0) {
                moreItemPopup.setAnimationStyle(R.style.cricleBottomAnimation);
                moreItemPopup.show(arg0);
            }
        });
    }

    private void initSelfInfoView() {
        String name = senderInfo.getName();
        String userface = senderInfo.getUserface();
        long datetime = newNoticeList.getCreatedDate();
        String company = senderInfo.getCompany();
        String job = senderInfo.getTitle();
        int accountType = senderInfo.getAccountType();
        boolean isRealName = senderInfo.isRealname();

        try {
            imageLoader.displayImage(userface, userHeadIv, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        userNameTv.setText(name);
        if (!TextUtils.isEmpty(job)) {
            userTitleTv.setText(job);
        }
        if (!TextUtils.isEmpty(company)) {
            if (!TextUtils.isEmpty(userTitleTv.getText().toString())) {
                userTitleTv.setText(userTitleTv.getText().toString() + " / " + company.trim());
            } else {
                userTitleTv.setText(company.trim());
            }
        }
        if (TextUtils.isEmpty(job) && TextUtils.isEmpty(company)) {
            userTitleTv.setVisibility(View.GONE);
        } else {
            userTitleTv.setVisibility(View.VISIBLE);
        }
        userIdentityIv.setVisibility(View.GONE);
        switch (accountType) {
            case 0:
                if (isRealName) {
                    userIdentityIv.setVisibility(View.VISIBLE);
                    userIdentityIv.setImageResource(R.drawable.archive_realname);
                }
                break;
            case 1:
                userIdentityIv.setVisibility(View.VISIBLE);
                userIdentityIv.setImageResource(R.drawable.archive_vip_1);
                break;
            case 2:
                userIdentityIv.setVisibility(View.VISIBLE);
                userIdentityIv.setImageResource(R.drawable.archive_vip_2);
                break;
            case 3:
                userIdentityIv.setVisibility(View.VISIBLE);
                userIdentityIv.setImageResource(R.drawable.archive_vip_3);
                break;
            default:
                break;
        }

        // TODO: 2016/6/13  由于人脉圈发送者不是好友的情况下，右上角要显示加好友按钮，所以把时间控件去掉
//        if (datetime > 0) {
//            dateUtil.string2Date(context, datetime, createTimeTv);
//        } else {
//            createTimeTv.setText("");
//        }
    }

    /**
     * 根据好友关系显示右上角加好友按钮的状态
     * 判断动态发送者是否是好友，如果不是则右上角显示加好友按钮
     * 如果以及发送好友请求，则显示“已发送好友请求”
     */
    public void initNotFriendView(final int position) {
        //显示加好友的按钮
        if (senderInfo.getFriendState() != 0) {
            addFriendTv.setVisibility(View.VISIBLE);
        } else {
            addFriendTv.setVisibility(View.GONE);
        }

        addFriendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(position);
            }
        });
    }

    /**
     * 初始化点赞界面
     *
     * @param position 列表position
     */
    public void initGoodView(int position) {
        //初始化点赞界面
        int likedNum = contentInfo.getLikedNum();
        boolean isLiked = contentInfo.isLiked();
        goodTv.setText(likedNum + "");
        if (likedNum > 0) {
            if (isLiked) {
                goodIv.setImageResource(R.drawable.good_number_icon);
                goodTv.setTextColor(context.getResources().getColor(R.color.CP));
            } else {
                goodIv.setImageResource(R.drawable.ungood_number_icon);
                goodTv.setTextColor(context.getResources().getColor(R.color.C2));
            }
            showGoodListByGoodNum();
        } else {
            goodIvOne.setVisibility(View.GONE);
            goodIvTwo.setVisibility(View.GONE);
            goodIvThree.setVisibility(View.GONE);
            goodIvFour.setVisibility(View.GONE);
            goodIvFive.setVisibility(View.GONE);
            goodIv.setImageResource(R.drawable.ungood_number_icon);
            goodTv.setTextColor(context.getResources().getColor(R.color.C2));
        }

        goodLl.setOnClickListener(new GoodClickListener(position));

    }

    private void showGoodListByGoodNum() {
        MessageBoards.LikedList[] likedList = contentInfo.getLikedList();
        if (null != likedList && likedList.length > 0) {
            switch (likedList.length) {
                case 1:
                    goodIvOne.setVisibility(View.VISIBLE);
                    loadImage(goodIvOne, likedList[0].getUserface(), likedList[0].getSid());
                    goodIvTwo.setVisibility(View.GONE);
                    goodIvThree.setVisibility(View.GONE);
                    goodIvFour.setVisibility(View.GONE);
                    goodIvFive.setVisibility(View.GONE);
                    break;
                case 2:
                    goodIvOne.setVisibility(View.VISIBLE);
                    loadImage(goodIvOne, likedList[0].getUserface(), likedList[0].getSid());
                    goodIvTwo.setVisibility(View.VISIBLE);
                    loadImage(goodIvTwo, likedList[1].getUserface(), likedList[1].getSid());
                    goodIvThree.setVisibility(View.GONE);
                    goodIvFour.setVisibility(View.GONE);
                    goodIvFive.setVisibility(View.GONE);
                    break;
                case 3:
                    goodIvOne.setVisibility(View.VISIBLE);
                    loadImage(goodIvOne, likedList[0].getUserface(), likedList[0].getSid());
                    goodIvTwo.setVisibility(View.VISIBLE);
                    loadImage(goodIvTwo, likedList[1].getUserface(), likedList[1].getSid());
                    goodIvThree.setVisibility(View.VISIBLE);
                    loadImage(goodIvThree, likedList[2].getUserface(), likedList[2].getSid());
                    goodIvFour.setVisibility(View.GONE);
                    goodIvFive.setVisibility(View.GONE);
                    break;
                case 4:
                    goodIvOne.setVisibility(View.VISIBLE);
                    loadImage(goodIvOne, likedList[0].getUserface(), likedList[0].getSid());
                    goodIvTwo.setVisibility(View.VISIBLE);
                    loadImage(goodIvTwo, likedList[1].getUserface(), likedList[1].getSid());
                    goodIvThree.setVisibility(View.VISIBLE);
                    loadImage(goodIvThree, likedList[2].getUserface(), likedList[2].getSid());
                    goodIvFour.setVisibility(View.VISIBLE);
                    loadImage(goodIvFour, likedList[3].getUserface(), likedList[3].getSid());
                    goodIvFive.setVisibility(View.GONE);
                    break;
                case 5:
                    goodIvOne.setVisibility(View.VISIBLE);
                    loadImage(goodIvOne, likedList[0].getUserface(), likedList[0].getSid());
                    goodIvTwo.setVisibility(View.VISIBLE);
                    loadImage(goodIvTwo, likedList[1].getUserface(), likedList[1].getSid());
                    goodIvThree.setVisibility(View.VISIBLE);
                    loadImage(goodIvThree, likedList[2].getUserface(), likedList[2].getSid());
                    goodIvFour.setVisibility(View.VISIBLE);
                    loadImage(goodIvFour, likedList[3].getUserface(), likedList[3].getSid());
                    goodIvFive.setVisibility(View.VISIBLE);
                    loadImage(goodIvFive, likedList[4].getUserface(), likedList[4].getSid());
                    break;
                default:
                    goodIvOne.setVisibility(View.VISIBLE);
                    loadImage(goodIvOne, likedList[0].getUserface(), likedList[0].getSid());
                    goodIvTwo.setVisibility(View.VISIBLE);
                    loadImage(goodIvTwo, likedList[1].getUserface(), likedList[1].getSid());
                    goodIvThree.setVisibility(View.VISIBLE);
                    loadImage(goodIvThree, likedList[2].getUserface(), likedList[2].getSid());
                    goodIvFour.setVisibility(View.VISIBLE);
                    loadImage(goodIvFour, likedList[3].getUserface(), likedList[3].getSid());
                    goodIvFive.setVisibility(View.VISIBLE);
                    loadImage(goodIvFive, likedList[4].getUserface(), likedList[4].getSid());
                    break;
            }
        } else {
            goodIvOne.setVisibility(View.GONE);
            goodIvTwo.setVisibility(View.GONE);
            goodIvThree.setVisibility(View.GONE);
            goodIvFour.setVisibility(View.GONE);
            goodIvFive.setVisibility(View.GONE);
        }
    }

    private void showReplyListByGoodNum(final int position) {
        MessageBoards.ReplyList[] replyLists = contentInfo.getReplyList();
        if (null != replyLists && replyLists.length > 0) {
            switch (replyLists.length) {
                case 1:
                    replyTvOne.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[0], replyTvOne, position);
                    replyTvTwo.setVisibility(View.GONE);
                    replyTvThree.setVisibility(View.GONE);
                    replyTvFour.setVisibility(View.GONE);
                    replyTvFive.setVisibility(View.GONE);
                    break;
                case 2:
                    replyTvOne.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[0], replyTvOne, position);
                    replyTvTwo.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[1], replyTvTwo, position);
                    replyTvThree.setVisibility(View.GONE);
                    replyTvFour.setVisibility(View.GONE);
                    replyTvFive.setVisibility(View.GONE);
                    break;
                case 3:
                    replyTvOne.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[0], replyTvOne, position);
                    replyTvTwo.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[1], replyTvTwo, position);
                    replyTvThree.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[2], replyTvThree, position);
                    replyTvFour.setVisibility(View.GONE);
                    replyTvFive.setVisibility(View.GONE);
                    break;
                case 4:
                    replyTvOne.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[0], replyTvOne, position);
                    replyTvTwo.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[1], replyTvTwo, position);
                    replyTvThree.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[2], replyTvThree, position);
                    replyTvFour.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[3], replyTvFour, position);
                    replyTvFive.setVisibility(View.GONE);
                    break;
                case 5:
                    replyTvOne.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[0], replyTvOne, position);
                    replyTvTwo.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[1], replyTvTwo, position);
                    replyTvThree.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[2], replyTvThree, position);
                    replyTvFour.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[3], replyTvFour, position);
                    replyTvFive.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[4], replyTvFive, position);
                    break;
                default:
                    replyTvOne.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[0], replyTvOne, position);
                    replyTvTwo.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[1], replyTvTwo, position);
                    replyTvThree.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[2], replyTvThree, position);
                    replyTvFour.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[3], replyTvFour, position);
                    replyTvFive.setVisibility(View.VISIBLE);
                    loadReplyView(replyLists[4], replyTvFive, position);
                    break;
            }
        } else {
            replyTvOne.setVisibility(View.GONE);
            replyTvTwo.setVisibility(View.GONE);
            replyTvThree.setVisibility(View.GONE);
            replyTvFour.setVisibility(View.GONE);
            replyTvFive.setVisibility(View.GONE);
        }
        if (contentInfo.getReplyNum() > RENMAIQUAN_REPLY_MAX_NUM) {
            replySeemoreTv.setVisibility(View.VISIBLE);
            //添加“查看全部评论”view
            replySeemoreTv.setText("全部" + contentInfo.getReplyNum() + "条评论");
            replySeemoreTv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    MobclickAgent.onEvent(context, "renmai_checkmore");
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("result", newNoticeList);
                    bundle.putInt("position", position);
                    Intent intent = new Intent(context, TwitterShowMessageBoardActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
            });
        } else {
            replySeemoreTv.setVisibility(View.GONE);
        }
    }

    private void loadReplyView(MessageBoards.ReplyList replyList, AisenReplyTextView replyTv, int position) {
        if (null != replyList) {
            String replyContent = replyList.getContent().trim();
            StringBuilder replyBuffer = new StringBuilder();
            if (!TextUtils.isEmpty(replyList.getSenderName())) {
                String senderSid = replyList.getSenderSid();
                String senderName = replyList.getSenderName();
                String reSenderName = replyList.getReSenderMemberName();
                String reSenderSid = replyList.getReSenderSid();
                replyBuffer.append(senderName);
                if (!TextUtils.isEmpty(reSenderName)) {
                    replyBuffer.append("回复" + reSenderName);
                }
                replyBuffer.append(": " + replyContent);
                replyTv.setReplyList(replyList);
                replyTv.setContent(replyBuffer.toString());
                final String finalReplyContent = replyContent;
                replyTv.setOnClickListener(new ReplyContentSpanClick(contentInfo.getObjectId(), contentInfo.getId(), senderSid, senderName,
                        replyList.getId(), replyList.getObjectId(), position, newNoticeList.getType()));
                replyTv.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View arg0) {
                        new ContentUtil().createCopyDialog(context, finalReplyContent);
                        return true;
                    }
                });
            }
        }
    }

    private void loadImage(ImageView imageView, String url, final String sid) {
        try {
            imageLoader.displayImage(url, imageView, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, sid);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }

    /**
     * 初始化评论、分享弹出框界面
     */
    private void initMorePopWindow(int position) {
        if (null == moreItemPopup)
            return;
        int type = newNoticeList.getType();
        moreItemPopup.addAction(new com.itcalf.renhe.view.ActionItem(context, "评论", R.drawable.circle_comment));
        moreItemPopup.addAction(new com.itcalf.renhe.view.ActionItem(context, "分享", R.drawable.btn_icon_share));
        if (type != MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD) {
            moreItemPopup.hideShareItem();
            moreItemPopup.setWidth(DensityUtil.dip2px(context, Constants.RenMaiQuanPOPWindowParams.WIDTH_MIN));
        } else {
            moreItemPopup.showShareItem();
            moreItemPopup.setWidth(DensityUtil.dip2px(context, Constants.RenMaiQuanPOPWindowParams.WIDTH_MAX));
        }
        moreItemPopup.setItemOnClickListener(new PopwindowItemClickListener(position));
    }

    class PopwindowItemClickListener implements TitlePopup.OnItemOnClickListener {

        int position;

        public PopwindowItemClickListener(int position) {
            this.position = position;
        }

        public PopwindowItemClickListener() {
            super();
        }

        @Override
        public void onItemClick(ActionItem item, int pos) {
            switch (pos) {
                case 0://评论
                    gotoReply(position);
                    break;
                case 1://分享
                    gotoShare();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 评论
     */
    private void gotoReply(int position) {
        String content = "";
        new ReplyPopupWindow(context, renmaiQuanRecyclerView, contentInfo.getObjectId(),
                contentInfo.getId(), RenheApplication.getInstance().getUserInfo().getSid(),
                RenheApplication.getInstance().getUserInfo().getName(), null, null, false,
                newNoticeList.getType(), newNoticeList, position);
        renmaiQuanUtils.popupInputMethodWindow();
    }

    /**
     * 分享
     */
    private void gotoShare() {
        MobclickAgent.onEvent(context, "renmai_share");
        String myContent = contentInfo.getContent();
        if (!TextUtils.isEmpty(myContent)) {
            if (myContent.length() > 50) {
                myContent = myContent.substring(0, 50);
            }
        }
        String forwardContent = "";
        String shareContent = myContent;
        MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
        String mforwardContent = forwardMessageBoardInfo == null ? null : forwardMessageBoardInfo.getContent();
        if (null != myContent && !TextUtils.isEmpty(myContent) && !TextUtils.isEmpty(mforwardContent)) {
            forwardContent = myContent;
        }
        if (null != mforwardContent && !TextUtils.isEmpty(mforwardContent)) {
            shareContent = mforwardContent;
        }
        MessageBoards.PicList[] mPicLists = contentInfo.getPicList();
        MessageBoards.PicList[] mForwardPicLists = forwardMessageBoardInfo == null ? null : forwardMessageBoardInfo.getPicLists();
        if (TextUtils.isEmpty(shareContent) && null != mForwardPicLists
                && mForwardPicLists.length > 0) {
            shareContent = "";
        }
        String sharePic = "";
        if (null != mForwardPicLists && mForwardPicLists.length > 0) {
            sharePic = mForwardPicLists[0].getThumbnailPicUrl();
        }
        if (null != mPicLists && mPicLists.length > 0) {
            sharePic = mPicLists[0].getThumbnailPicUrl();
        }
        if (TextUtils.isEmpty(sharePic) && null != forwardMessageBoardInfo) {
            if (null != forwardMessageBoardInfo.getWebsShare()) {
                sharePic = forwardMessageBoardInfo.getWebsShare().getPicUrl();
            }
        }

        MessageBoards.SenderInfo senderInfo = newNoticeList.getSenderInfo();
        if (null == senderInfo)
            return;
        if (TextUtils.isEmpty(sharePic)) {
            sharePic = senderInfo.getUserface();
        }
        new SharePopupWindow(context, renmaiQuanRecyclerView, senderInfo.getSid(), senderInfo.getName(),
                "", senderInfo.getUserface(), senderInfo.getCompany(), senderInfo.getTitle(), myContent, senderInfo.getSid(),
                contentInfo.getId(), true, contentInfo.getObjectId(), forwardContent, shareContent, sharePic, "5.108", 1);
    }

    /**
     * 点赞事件监听
     */
    class GoodClickListener implements View.OnClickListener {

        int position;

        public GoodClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            boolean isLiked = contentInfo.isLiked();
            if (!isLiked) {
                //是否完善资料
                MobclickAgent.onEvent(context, "renmaiquan_good");
                RenmaiQuanUtils.updateRenmaiQuanItemAddLiked(newNoticeList);
                recyclerRenmaiQuanItemAdapter.updateDatasItem(newNoticeList, position);//更新数据源
//                recyclerRenmaiQuanItemAdapter.notifyItemChanged(position);//更新item
                recyclerRenmaiQuanItemAdapter.updateItemGoodView(position);
                markFavour();
            }
        }
    }

    /**
     * 调用点赞接口
     */
    private void markFavour() {
        int type = newNoticeList.getType();
        int id = contentInfo.getId();
        String objectId = contentInfo.getObjectId();
        if (TextUtils.isEmpty(objectId))
            return;
        if (type == MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD) {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
            reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
            reqParams.put("messageBoardObjectId", objectId);
            reqParams.put("messageBoardId", id + "");
            OkHttpClientManager.postAsyn(Constants.Http.FAVOUR_MESSAGEBOARD, reqParams, Object.class, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {

                }

                @Override
                public void onResponse(Object response) {
                }
            });
        } else {
            Map<String, Object> reqParams = new HashMap<>();
            reqParams.put("adSId", RenheApplication.getInstance().getUserInfo().getAdSId());
            reqParams.put("sid", RenheApplication.getInstance().getUserInfo().getSid());
            reqParams.put("noticeObjectId", objectId);
            reqParams.put("noticeId", id);
            OkHttpClientManager.postAsyn(Constants.Http.FAVOUR_UNMESSAGEBOARD, reqParams, Object.class, new OkHttpClientManager.ResultCallback() {
                @Override
                public void onError(Request request, Exception e) {

                }

                @Override
                public void onResponse(Object response) {
                }
            });
        }
    }

    /**
     * 初始化评论界面
     *
     * @param position 列表position
     */
    public void initReplyView(final int position) {
        int replyNum = contentInfo.getReplyNum();
        if (replyNum > 0) {
            goodReplySeperateLine.setVisibility(View.VISIBLE);
            replyLl.setVisibility(View.VISIBLE);
            showReplyListByGoodNum(position);
        } else {
            goodReplySeperateLine.setVisibility(View.GONE);
            replyLl.setVisibility(View.GONE);
            replyTvOne.setVisibility(View.GONE);
            replyTvTwo.setVisibility(View.GONE);
            replyTvThree.setVisibility(View.GONE);
            replyTvFour.setVisibility(View.GONE);
            replyTvFive.setVisibility(View.GONE);
        }
    }

    class ReplyContentSpanClick extends ClickableSpan implements View.OnClickListener {
        private String mObjectId;
        private int mId;
        private String senderSid;
        private String senderName;
        private int replyId;
        private String replyObjectId;
        private int position;
        private int contentType;

        public ReplyContentSpanClick(String mObjectId, int mId, String senderSid, String senderName, int replyId,
                                     String replyObjectId, int position, int type) {
            this.mObjectId = mObjectId;
            this.mId = mId;
            this.senderName = senderName;
            this.senderSid = senderSid;
            this.replyId = replyId;
            this.replyObjectId = replyObjectId;
            this.position = position;
            this.contentType = type;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setUnderlineText(false); // 去掉下划线
        }

        @Override
        public void onClick(final View v) {
            MobclickAgent.onEvent(context, "reply_replysitem");
            if (!senderSid.equals(RenheApplication.getInstance().getUserInfo().getSid())) {
                new ReplyPopupWindow(context, renmaiQuanRecyclerView, mObjectId, mId, senderSid, senderName, replyId, replyObjectId, true,
                        contentType, newNoticeList, position);
                renmaiQuanUtils.popupInputMethodWindow();
            } else {
                RenmaiQuanUtils.createSelfDialog(newNoticeList, position, 2, replyId, replyObjectId, null);
            }
        }

    }

    private void addFriend(final int position) {
        new AddFriendTask(context, new AddFriendTask.IAddFriendCallBack() {
            @Override
            public void onPre() {
                showMaterialLoadingDialog(R.string.add_friend_sending);
            }

            @Override
            public void doPost(AddFriend result) {
                hideMaterialLoadingDialog();
                if (result == null) {
                    ToastUtil.showErrorToast(context, context.getString(R.string.connect_server_error));
                    return;
                } else if (result.getState() == 1) {
                    ToastUtil.showToast(context, R.string.success_friend_request);
                    RenmaiQuanUtils.updateRenmaiQuanItemAddFriend(newNoticeList);
                    recyclerRenmaiQuanItemAdapter.updateDatasItem(newNoticeList, position);//更新数据源
                    recyclerRenmaiQuanItemAdapter.updateItemAddFriendView(position);
                    renmaiQuanUtils.updateDBRenmaiQuanItemFriendState(newNoticeList);
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), senderInfo.getSid(), "", "", 10 + "");
    }

    //加载框
    public void showMaterialLoadingDialog(int loadingInfoRes) {
        if (materialDialogsUtil != null) {
            materialDialogsUtil.showIndeterminateProgressDialog(loadingInfoRes).canceledOnTouchOutside(true).build();
            materialDialogsUtil.show();
        }
    }

    public void hideMaterialLoadingDialog() {
        if (null != materialDialogsUtil) {
            materialDialogsUtil.dismiss();
        }
    }
}

