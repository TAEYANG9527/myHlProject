package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.RecyclerRenmaiQuanItemAdapter;
import com.itcalf.renhe.context.archives.AddFriendTask;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.room.db.RenMaiQuanManager;
import com.itcalf.renhe.context.room.db.SQLiteStore;
import com.itcalf.renhe.dto.AddFriend;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ToastUtil;
import com.itcalf.renhe.utils.WriteLogThread;

import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * Created by wangning on 2015/10/12.
 */
public class RenmaiQuanGuideRecommendFriendsViewHolder extends RecyclerHolder {
    private RecyclerView renmaiQuanRecyclerView;
    private MessageBoards.NewNoticeList newNoticeList;
    private MessageBoards.ContentInfo contentInfo;
    //item 头部布局
    private TextView titleTv;
    private LinearLayout recommendFriendGroup;
    private RecyclerRenmaiQuanItemAdapter recyclerRenmaiQuanItemAdapter;
    private MessageBoards.RecommendMember[] recommendMembers;
    private ArrayList<MessageBoards.RecommendMember> recommendMemberList;
    private RenMaiQuanManager renMaiQuanManager;

    public RenmaiQuanGuideRecommendFriendsViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, adapter);
        if (null != adapter && adapter instanceof RecyclerRenmaiQuanItemAdapter)
            recyclerRenmaiQuanItemAdapter = (RecyclerRenmaiQuanItemAdapter) adapter;
        this.renmaiQuanRecyclerView = renmaiQuanRecyclerView;
        this.renMaiQuanManager = new RenMaiQuanManager(context);
        titleTv = (TextView) itemView.findViewById(R.id.recommend_title_tv);
        recommendFriendGroup = (LinearLayout) itemView.findViewById(R.id.recommend_friend_group);
        recommendMemberList = new ArrayList<>();
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
//        if (recommendFriendGroup.getChildCount() <= 0)
        initRecommendFriendsView(position);
    }

    private void initRecommendFriendsView(int position) {
        recommendMemberList.clear();
        recommendFriendGroup.removeAllViews();
        String content = contentInfo.getContent();
        if (!TextUtils.isEmpty(content)) {
            titleTv.setText(content);
        } else {
            if (newNoticeList.getType() == MessageBoards.MESSAGE_TYPE_FRIEND_JOIN_NOTICE) {
                titleTv.setText(context.getString(R.string.renmaiquan_guide_recommend_friend_title2));
            } else {
                titleTv.setText(context.getString(R.string.renmaiquan_guide_recommend_friend_title1));
            }
        }
        recommendMembers = contentInfo.getMembers();
        if (null != recommendMembers && recommendMembers.length > 0) {
            for (MessageBoards.RecommendMember recommendMember : recommendMembers) {
                if (null != recommendMember) {
                    View view = LayoutInflater.from(context).inflate(R.layout.renmaiquan_item_guide_recommend_friend_item_layout, null);
                    initSelfInfoView(recommendMember, view, position);
                    recommendFriendGroup.addView(view);
                    recommendMemberList.add(recommendMember);
                }
            }
        } else {//本来应该已经被删掉的item，再次出现，容错，重新删除一遍
            Intent intent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM);
            intent.putExtra("position", position);
            context.sendBroadcast(intent);
            try {
                renMaiQuanManager.delete(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, contentInfo.getObjectId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initSelfInfoView(final MessageBoards.RecommendMember recommendMember, View view, final int position) {
        ImageView userHeadIv = (ImageView) view.findViewById(R.id.avatar_iv);
        ImageView userIdentityIv = (ImageView) view.findViewById(R.id.identity_iv);//身份图标，vip、实名认证等
        TextView userNameTv = (TextView) view.findViewById(R.id.username_tv);
        TextView userTitleTv = (TextView) view.findViewById(R.id.title_tv);
        ImageButton addIb = (ImageButton) view.findViewById(R.id.add_ib);

        final String userSid = recommendMember.getSid();
        String name = recommendMember.getName();
        String userface = recommendMember.getUserface();
        String company = recommendMember.getCompany();
        String job = recommendMember.getTitle();
        int accountType = recommendMember.getAccountType();
        boolean isRealName = recommendMember.isRealname();

        try {
            imageLoader.displayImage(userface, userHeadIv);
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
        addIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(userSid);
                recommendMemberList.remove(recommendMember);
                if (recommendMemberList.size() <= 0) {
                    Intent intent = new Intent(Constants.BroadCastAction.RMQ_ACTION_RMQ_DELETE_ITEM);
                    intent.putExtra("position", position);
                    context.sendBroadcast(intent);
                } else {
                    MessageBoards.RecommendMember[] newRecommendMembers = new MessageBoards.RecommendMember[recommendMemberList.size()];
                    for (int i = 0; i < newRecommendMembers.length; i++) {
                        newRecommendMembers[i] = recommendMemberList.get(i);
                    }
                    contentInfo.setMembers(newRecommendMembers);
                    newNoticeList.setContentInfo(contentInfo);
                    recyclerRenmaiQuanItemAdapter.updateDatasItem(newNoticeList, position);//更新数据源
                    recyclerRenmaiQuanItemAdapter.notifyItemChanged(position);//更新item
                }
                switch (newNoticeList.getType()) {
                    case MessageBoards.MESSAGE_TYPE_RECOMMEND_FRIEND:
                        new WriteLogThread(context, "5.170.3.2", null).start();
                        break;
                    case MessageBoards.MESSAGE_TYPE_FRIEND_JOIN_NOTICE:
                        new WriteLogThread(context, "5.170.4.2", null).start();
                        break;
                    default:
                        break;
                }
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, userSid);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                switch (newNoticeList.getType()) {
                    case MessageBoards.MESSAGE_TYPE_RECOMMEND_FRIEND:
                        new WriteLogThread(context, "5.170.3.1", null).start();
                        break;
                    case MessageBoards.MESSAGE_TYPE_FRIEND_JOIN_NOTICE:
                        new WriteLogThread(context, "5.170.4.1", null).start();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * @param userSid
     * @author wangning
     */
    private void addFriend(final String userSid) {
        String message = "我是来自" + RenheApplication.getInstance().getUserInfo().getCompany() + "的"
                + RenheApplication.getInstance().getUserInfo().getTitle()
                + RenheApplication.getInstance().getUserInfo().getName();
        new AddFriendTask(context, new AddFriendTask.IAddFriendCallBack() {
            @Override
            public void onPre() {

            }

            @Override
            public void doPost(AddFriend result) {
                if (result == null) {
                    ToastUtil.showErrorToast(context, context.getString(R.string.connect_server_error));
                    return;
                } else if (result.getState() == 1) {

                    try {
                        if (recommendMemberList.size() > 0) {
                            if (!TextUtils.isEmpty(userSid)) {
                                renMaiQuanManager.deleteRecommendFriend(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN_RECOMMEND_FRIEND,
                                        userSid);
                            }
                        } else if (!TextUtils.isEmpty(contentInfo.getObjectId())) {
                            renMaiQuanManager.delete(SQLiteStore.RenheSQLiteOpenHelper.TABLE_RENMAIQUAN, contentInfo.getObjectId());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).executeOnExecutor(Executors.newCachedThreadPool(), userSid, message, "", Constants.ADDFRIENDTYPE[6] + "");
    }

}

