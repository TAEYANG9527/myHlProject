package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itcalf.renhe.R;
import com.itcalf.renhe.adapter.RecyclerCollectionItemAdapter;
import com.itcalf.renhe.adapter.RecyclerRenmaiQuanItemAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.widget.emojitextview.AisenTextView;

import java.util.List;

import cn.renhe.heliao.idl.collection.MyCollection;

/**
 * Created by wangning on 2015/10/13.
 */
public class CollectionRmqTextAndImageViewHolder extends RecyclerHolder {
    public RecyclerView collectionRecyclerView;
    public RecyclerCollectionItemAdapter recyclerCollectionItemAdapter;
    public MyCollection.CollectionInfo collectionInfo;
    public MyCollection.MessageBoardInfo messageBoardInfo;
    //item 头部布局
    public ImageView userHeadIv;
    public TextView userNameTv;
    public TextView userTitleTv;
    public TextView createTimeTv;
    public ImageView userIdentityIv;//身份图标，vip、实名认证等
    public RelativeLayout userInfoRl;
    //正文布局
    public AisenTextView forwardContentTv;
    public RelativeLayout forwardRl;
    private ImageView forwardIv;
    public DateUtil dateUtil;

    public CollectionRmqTextAndImageViewHolder(Context context, View itemView, RecyclerView collectionRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, adapter);
        if (null != adapter && adapter instanceof RecyclerRenmaiQuanItemAdapter)
            recyclerCollectionItemAdapter = (RecyclerCollectionItemAdapter) adapter;
        this.collectionRecyclerView = collectionRecyclerView;
        userHeadIv = (ImageView) itemView.findViewById(R.id.avatar_iv);
        userNameTv = (TextView) itemView.findViewById(R.id.username_tv);
        userTitleTv = (TextView) itemView.findViewById(R.id.title_tv);
        createTimeTv = (TextView) itemView.findViewById(R.id.datetime_tv);
        userIdentityIv = (ImageView) itemView.findViewById(R.id.identity_iv);
        userInfoRl = (RelativeLayout) itemView.findViewById(R.id.userinfo_rl);
        forwardIv = (ImageView) itemView.findViewById(R.id.forward_web_pic_iv);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
        forwardContentTv = (AisenTextView) itemView.findViewById(R.id.forward_tv);
        this.dateUtil = new DateUtil();
    }

    @Override
    public void initView(RecyclerHolder holder, Object mCollectionInfo, int position) {
        if (null == holder)
            return;
        if (null == mCollectionInfo)
            return;
        if (mCollectionInfo instanceof MyCollection.CollectionInfo)
            collectionInfo = (MyCollection.CollectionInfo) mCollectionInfo;
        if (null == collectionInfo)
            return;
        messageBoardInfo = collectionInfo.getMessageBoardInfo();
        if (null == messageBoardInfo)
            return;
        initSelfInfoView();
        String content = messageBoardInfo.getContent().trim();//正文内容
        forwardRl.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(content))
            forwardContentTv.setContent(content);
        else
            forwardContentTv.setText("");
        List<MyCollection.MessageBoardPhotoInfo> picList = messageBoardInfo.getPicListList();
        if (null != picList && !picList.isEmpty()) {
            forwardIv.setVisibility(View.VISIBLE);
            String imageUrl = picList.get(0).getThumbnailPicUrl();
            if (!TextUtils.isEmpty(imageUrl)) {
                imageLoader.displayImage(imageUrl, forwardIv,
                        CacheManager.imageOptions);
            }
        } else {
            forwardIv.setVisibility(View.GONE);
        }
        initListener();
    }

    private void initSelfInfoView() {
        MyCollection.CollectionMemberInfo senderInfo = messageBoardInfo.getSenderInfo();
        String name = senderInfo.getName();
        String userface = senderInfo.getUserface();

        long datetime = collectionInfo.getCreatedDate();
        String company = senderInfo.getCompany();
        String job = senderInfo.getTitle();
        int accountType = senderInfo.getAccountType();
        boolean isRealName = senderInfo.getIsRealname();
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

        if (datetime > 0) {
            createTimeTv.setVisibility(View.VISIBLE);
            dateUtil.collectString2Date(context, datetime, createTimeTv);
        } else {
            createTimeTv.setVisibility(View.GONE);
            createTimeTv.setText("");
        }
    }

    private void initListener() {

        forwardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String objectId = messageBoardInfo.getObjectId();
                String sid = messageBoardInfo.getSenderInfo().getSid();
                Bundle bundle = new Bundle();
                bundle.putString("sid", sid);
                bundle.putInt("type", messageBoardInfo.getMessageBoardType());
                bundle.putString("objectId", objectId);
                bundle.putBoolean("isFromNoticeList", true);
                bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
                Intent intent = new Intent(context, TwitterShowMessageBoardActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }
}
