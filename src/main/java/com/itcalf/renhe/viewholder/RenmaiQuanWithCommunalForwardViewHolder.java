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

import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.view.WebViewForIndustryCircle;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithCommunalForwardViewHolder extends RenmaiQuanNormalTextViewHolder {
    private TextView forwardArchiveTitleTv;
    private TextView forwardArchiveNameTv;
    private TextView forwardArchiveJobTv;
    private ImageView forwardArchiveIv;
    private RelativeLayout forwardRl;
    private MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo;

    public RenmaiQuanWithCommunalForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
        forwardArchiveTitleTv = (TextView) itemView.findViewById(R.id.forward_archive_title_tv);
        forwardArchiveNameTv = (TextView) itemView.findViewById(R.id.forward_archive_name_tv);
        forwardArchiveJobTv = (TextView) itemView.findViewById(R.id.forward_archive_job_tv);
        forwardArchiveIv = (ImageView) itemView.findViewById(R.id.forward_archive_pic_iv);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
            if (null != forwardMessageBoardInfo) {
                MessageBoards.communalShare communalShare = forwardMessageBoardInfo.getCommunalShare();
                if (null != communalShare) {
                    forwardRl.setVisibility(View.VISIBLE);
                    String title = communalShare.getTitle();//姓名
                    String name = communalShare.getName();//姓名
                    String job = communalShare.getDescription();//职务
                    String company = communalShare.getCompany();//公司
                    String imageUrl = communalShare.getPicUrl();//头像
                    if (!TextUtils.isEmpty(title)) {
                        forwardArchiveTitleTv.setVisibility(View.VISIBLE);
                        forwardArchiveTitleTv.setText(title);
                    } else {
                        forwardArchiveTitleTv.setVisibility(View.VISIBLE);
                        forwardArchiveTitleTv.setText("赞服务");
                    }
                    if (!TextUtils.isEmpty(name)) {
                        forwardArchiveNameTv.setVisibility(View.VISIBLE);
                        forwardArchiveNameTv.setText(name);
                    } else {
                        forwardArchiveNameTv.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(job) && !TextUtils.isEmpty(company)) {
                        forwardArchiveJobTv.setVisibility(View.VISIBLE);
                        forwardArchiveJobTv.setText(job + "\n" + company);
                    } else if (!TextUtils.isEmpty(job) && TextUtils.isEmpty(company)) {
                        forwardArchiveJobTv.setVisibility(View.VISIBLE);
                        forwardArchiveJobTv.setText(job);
                    } else if (TextUtils.isEmpty(job) && !TextUtils.isEmpty(company)) {
                        forwardArchiveJobTv.setVisibility(View.VISIBLE);
                        forwardArchiveJobTv.setText(company);
                    } else {
                        forwardArchiveJobTv.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(imageUrl)) {
                        imageLoader.displayImage(imageUrl, forwardArchiveIv, CacheManager.circleImageOptions);
                    }
                    initListener(communalShare);
                } else {
                    forwardRl.setVisibility(View.GONE);
                }
            } else {
                forwardRl.setVisibility(View.GONE);
            }
        }
    }

    private void initListener(final MessageBoards.communalShare communalShare) {

        forwardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                if (communalShare.getUrl().contains(Constants.TOPIC_URL)) {
                    i.setClass(context, WebViewForIndustryCircle.class);
                } else {
                    i.setClass(context, WebViewActWithTitle.class);
                }
                i.putExtra("url", communalShare.getUrl());
                context.startActivity(i);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
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
