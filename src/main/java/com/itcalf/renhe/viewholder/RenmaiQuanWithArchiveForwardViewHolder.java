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
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithArchiveForwardViewHolder extends RenmaiQuanNormalTextViewHolder {
    private TextView forwardArchiveNameTv;
    private TextView forwardArchiveJobTv;
    private ImageView forwardArchiveIv;
    private RelativeLayout forwardRl;
    private MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo;

    public RenmaiQuanWithArchiveForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
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
                MessageBoards.profileShare profileShare = forwardMessageBoardInfo.getProfileShare();
                if (null != profileShare) {
                    forwardRl.setVisibility(View.VISIBLE);
                    String name = profileShare.getName();//姓名
                    String job = profileShare.getJob();//职务
                    String company = profileShare.getCompany();//公司
                    String imageUrl = profileShare.getPicUrl();//头像
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
                    initListener(profileShare);
                } else {
                    forwardRl.setVisibility(View.GONE);
                }
            } else {
                forwardRl.setVisibility(View.GONE);
            }
        }
    }

    private void initListener(final MessageBoards.profileShare profileShare) {

        forwardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(context, MyHomeArchivesActivity.class);
                i.putExtra(MyHomeArchivesActivity.FLAG_INTENT_DATA, profileShare.getSid());
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
