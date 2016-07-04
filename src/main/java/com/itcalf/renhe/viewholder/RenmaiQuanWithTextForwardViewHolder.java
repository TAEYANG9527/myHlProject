package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.itcalf.renhe.R;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.utils.ContentUtil;
import com.itcalf.renhe.utils.LoggerFileUtil;
import com.itcalf.renhe.widget.emojitextview.AisenRmqForwardTextView;
import com.itcalf.renhe.widget.emojitextview.AisenTextView;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by wangning on 2015/10/13.
 */
public class RenmaiQuanWithTextForwardViewHolder extends RenmaiQuanNormalTextViewHolder {
    public AisenRmqForwardTextView forwardContentTv;
    public RelativeLayout forwardRl;
    public MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo;
    private int emojiHeight;

    public RenmaiQuanWithTextForwardViewHolder(Context context, View itemView, RecyclerView renmaiQuanRecyclerView, RecyclerView.Adapter adapter) {
        super(context, itemView, renmaiQuanRecyclerView, adapter);
        forwardRl = (RelativeLayout) itemView.findViewById(R.id.forward_rl);
        forwardContentTv = (AisenRmqForwardTextView) itemView.findViewById(R.id.forward_tv);
        emojiHeight = forwardContentTv.getLineHeight();
    }

    @Override
    public void initView(RecyclerHolder holder, Object mNewNoticeList, int position) {
        super.initView(holder, mNewNoticeList, position);
        if (null != newNoticeList && null != contentInfo) {
            forwardMessageBoardInfo = contentInfo.getForwardMessageBoardInfo();
            if (null != forwardMessageBoardInfo) {
                String content = forwardMessageBoardInfo.getContent().trim();//正文内容
                if (!TextUtils.isEmpty(content)) {
                    forwardRl.setVisibility(View.VISIBLE);
                    forwardContentTv.setAtMemmbers(forwardMessageBoardInfo.getAtMembers());
                    forwardContentTv.setContent(content);
                    initListener(forwardMessageBoardInfo);
                } else {
                    forwardRl.setVisibility(View.GONE);
                    forwardContentTv.setText("");
                }
            } else {
                forwardRl.setVisibility(View.GONE);
            }
        }
    }

    private void initListener(final MessageBoards.ForwardMessageBoardInfo forwardMessageBoardInfo) {

        forwardRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (forwardMessageBoardInfo.isForwardRenhe()) {
                    Bundle bundle = new Bundle();
                    MessageBoards.NewNoticeList forwardNewNoticeListItem = new MessageBoards.NewNoticeList();
                    MessageBoards.ContentInfo contentInfo = new MessageBoards.ContentInfo();
                    contentInfo.setId(forwardMessageBoardInfo.getId());
                    contentInfo.setObjectId(forwardMessageBoardInfo.getObjectId());
                    contentInfo.setContent(forwardMessageBoardInfo.getContent());
                    contentInfo.setAtMembers(forwardMessageBoardInfo.getAtMembers());
                    contentInfo.setPicList(forwardMessageBoardInfo.getPicLists());
                    forwardNewNoticeListItem.setContentInfo(contentInfo);
                    bundle.putSerializable("forwardNewNoticeListItem", forwardNewNoticeListItem);
                    bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_FORWARD);
                    bundle.putInt("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
                    Intent intent = new Intent(context, TwitterShowMessageBoardActivity.class);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                }
                MobclickAgent.onEvent(context, "renmaiquan_forward");
            }
        });
        forwardRl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new ContentUtil().createRenMaiQuanDialog(context, 4, "",newNoticeList);
                return true;
            }
        });
    }
}
