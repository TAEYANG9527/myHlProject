package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.itcalf.renhe.R;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalLeftAudioViewHolder extends ChatNormalRightAudioViewHolder {

    private ImageView unreadIv;

    public ChatNormalLeftAudioViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                         RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        unreadIv = ((ImageView) itemView.findViewById(R.id.unread_circle_view));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        if (!message.iHaveRead()) {
            unreadIv.setVisibility(View.VISIBLE);
        } else {
            unreadIv.setVisibility(View.GONE);
        }
        contentRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unreadIv.setVisibility(View.GONE);
                chatUtils.playAudio(message, true, audioIv, position);
            }
        });
    }

    @Override
    public void onContentRlLongClickListener() {
    }

}

