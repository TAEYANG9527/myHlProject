package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.widget.emojitextview.AisenTextView;
import com.orhanobut.logger.Logger;

import java.util.Collection;

import cn.renhe.heliao.idl.collection.MyCollection;

/**
 * Created by wangning on 2015/10/13.
 */
public class CollectionMemberViewHolder extends RecyclerHolder {
    public RecyclerView collectionRecyclerView;
    public RecyclerCollectionItemAdapter recyclerCollectionItemAdapter;
    public MyCollection.CollectionInfo collectionInfo;
    public MyCollection.CollectionMemberInfo collectionMemberInfo;
    //item 头部布局
    public ImageView userHeadIv;
    public TextView userNameTv;
    public TextView userTitleTv;
    public TextView createTimeTv;
    public ImageView userIdentityIv;//身份图标，vip、实名认证等
    public RelativeLayout userInfoRl;
    //正文布局
    public ImageView memberAvatarIv;
    public TextView forwardContentTv;
    public RelativeLayout forwardRl;

    public DateUtil dateUtil;

    public CollectionMemberViewHolder(Context context, View itemView, RecyclerView collectionRecyclerView, RecyclerView.Adapter adapter) {
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
        memberAvatarIv = (ImageView) itemView.findViewById(R.id.forward_web_pic_iv);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
        forwardContentTv = (TextView) itemView.findViewById(R.id.forward_tv);
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
        collectionMemberInfo = collectionInfo.getCollectionMemberInfo();
        if (null == collectionMemberInfo)
            return;
        initSelfInfoView();
        String content = collectionInfo.getContent().trim();//正文内容
        forwardRl.setVisibility(View.VISIBLE);
        forwardContentTv.setText(content);
        String imageUrl = collectionMemberInfo.getUserface();
        if (!TextUtils.isEmpty(imageUrl)) {
            imageLoader.displayImage(imageUrl, memberAvatarIv,
                    CacheManager.circleImageOptions);
        }
        initListener();
    }

    private void initSelfInfoView() {
        String name = collectionMemberInfo.getName();
        String userface = collectionMemberInfo.getUserface();
        long datetime = collectionInfo.getCreatedDate();
        String company = collectionMemberInfo.getCompany();
        String job = collectionMemberInfo.getTitle();
        int accountType = collectionMemberInfo.getAccountType();
        boolean isRealName = collectionMemberInfo.getIsRealname();
        Logger.d("userface-->" + userface + "--" + accountType);
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
                Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                intent.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, collectionMemberInfo.getSid());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
    }
}
