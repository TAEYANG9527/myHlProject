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
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.wukong.im.ActivityCircleDetail;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithCircleForwardViewHolder extends RenmaiQuanNormalTextViewHolder {
    private TextView forwardCircleNameTv;
    private TextView forwardCircleDescTv;
    private ImageView forwardCircleIv;
    private RelativeLayout forwardRl;
    private MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo;

    public RenmaiQuanWithCircleForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
        forwardCircleNameTv = (TextView) itemView.findViewById(R.id.forward_circle_name_tv);
        forwardCircleDescTv = (TextView) itemView.findViewById(R.id.forward_circle_desc_tv);
        forwardCircleIv = (ImageView) itemView.findViewById(R.id.forward_circle_pic_iv);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
            if (null != forwardMessageBoardInfo) {
                MessageBoards.circleShare circleShare = forwardMessageBoardInfo.getCircleShare();
                if (null != circleShare) {
                    forwardRl.setVisibility(View.VISIBLE);
                    String name = circleShare.getName();//圈名
                    String desc = circleShare.getNote();//圈子公告
                    String imageUrl = circleShare.getPicUrl();//圈子头像
                    if (!TextUtils.isEmpty(name)) {
                        forwardCircleNameTv.setVisibility(View.VISIBLE);
                        forwardCircleNameTv.setText(name);
                    } else {
                        forwardCircleNameTv.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(desc)) {
                        forwardCircleDescTv.setVisibility(View.VISIBLE);
                        forwardCircleDescTv.setText(desc);
                    } else {
                        forwardCircleDescTv.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(imageUrl)) {
                        imageLoader.displayImage(imageUrl, forwardCircleIv, CacheManager.circleImageOptions);
                    }
                    initListener(circleShare);
                } else {
                    forwardRl.setVisibility(View.GONE);
                }
            } else {
                forwardRl.setVisibility(View.GONE);
            }
        }
    }

    private void initListener(final MessageBoards.circleShare circleShare) {

        forwardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(context, ActivityCircleDetail.class);
                i.putExtra("circleId", circleShare.getId() + "");
                context.startActivity(i);
                ((Activity)context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        forwardRl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new ContentUtil().createRenMaiQuanDialog(context, 2, "", newNoticeList);
                return true;
            }
        });
    }
}
