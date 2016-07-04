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
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithWebForwardViewHolder extends RenmaiQuanNormalTextViewHolder {
    private TextView forwardWebContentTv;
    private ImageView forwardWebIv;
    private RelativeLayout forwardRl;
    private MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo;

    public RenmaiQuanWithWebForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
        forwardWebContentTv = (TextView) itemView.findViewById(R.id.forward_web_tv);
        forwardWebIv = (ImageView) itemView.findViewById(R.id.forward_web_pic_iv);
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
            if (null != forwardMessageBoardInfo) {
                MessageBoards.webShare webShare = forwardMessageBoardInfo.getWebsShare();
                if (null != webShare) {
                    forwardRl.setVisibility(View.VISIBLE);
                    String content = webShare.getContent();//正文内容
                    String imageUrl = webShare.getPicUrl();
                    if (!TextUtils.isEmpty(content)) {
                        forwardWebContentTv.setText(content);
                    } else {
                        forwardWebContentTv.setText("");
                    }
                    if (!TextUtils.isEmpty(imageUrl)) {
                        imageLoader.displayImage(imageUrl, forwardWebIv,
                                CacheManager.imageOptions);
                    }
                    initListener(webShare);
                } else {
                    forwardRl.setVisibility(View.GONE);
                }
            } else {
                forwardRl.setVisibility(View.GONE);
            }
        }
    }

    private void initListener(final MessageBoards.webShare webShare) {

        forwardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                if (webShare.getUrl().contains(Constants.TOPIC_URL)) {
                    i.setClass(context, WebViewForIndustryCircle.class);
                } else {
                    i.setClass(context, WebViewActWithTitle.class);
                }
                i.putExtra("url", webShare.getUrl());
                context.startActivity(i);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                MobclickAgent.onEvent(context, "renmaiquan_forward");
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
