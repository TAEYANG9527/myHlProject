package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.room.TwitterShowMessageBoardActivity;
import com.itcalf.renhe.dto.MessageBoards;
import com.itcalf.renhe.view.TextView;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalRmqViewHolder extends ChatViewHolder {

    private TextView titleTv;
    private ImageView rmqPicIv;
    private TextView rmqInfoTv;
    public MessageContent.LinkedContent messageContent;
    private String linkedContentLink;
    private String linkedContentTitle;
    private String linkedContentMsg;
    private String linkedContentPicUrl;

    public ChatNormalRmqViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                   RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        titleTv = ((TextView) itemView.findViewById(R.id.title_tv));
        rmqPicIv = ((ImageView) itemView.findViewById(R.id.rmq_pic_iv));
        rmqInfoTv = ((TextView) itemView.findViewById(R.id.rmq_info_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.LinkedContent) message.messageContent();
        if (null == messageContent)
            return;
        linkedContentLink = messageContent.url();
        linkedContentTitle = messageContent.title();
        linkedContentMsg = messageContent.text();
        linkedContentPicUrl = messageContent.picUrl();
        titleTv.setText(linkedContentTitle);
        if (!TextUtils.isEmpty(linkedContentPicUrl))
            loadImage(rmqPicIv, linkedContentPicUrl, CacheManager.imageOptions);
        else
            rmqPicIv.setImageResource(R.drawable.chat_link_default);
        rmqInfoTv.setText(linkedContentMsg);
    }

    @Override
    public void onContentRlClickListener() {
        String objectId = linkedContentLink.substring(linkedContentLink.indexOf("//") + 2, linkedContentLink.length());
        Bundle bundle = new Bundle();
        bundle.putInt("loadType", TwitterShowMessageBoardActivity.LOAD_TYPE_FROM_NOTICE);
        bundle.putString("objectId", objectId);
        bundle.putInt("type", MessageBoards.MESSAGE_TYPE_MESSAGE_BOARD);
        Intent intent = new Intent(context, TwitterShowMessageBoardActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void onContentRlLongClickListener() {
        if (null != chatUtils) {
            chatUtils.createCopyDialog(context, linkedContentMsg, Constants.ChatShareType.CHAT_SHARE_LINK, linkedContentTitle,
                    linkedContentLink, linkedContentPicUrl);
        }
    }

}

