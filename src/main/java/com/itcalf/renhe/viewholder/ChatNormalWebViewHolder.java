package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.view.TextView;
import com.itcalf.renhe.view.WebViewActWithTitle;
import com.itcalf.renhe.view.WebViewForIndustryCircle;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalWebViewHolder extends ChatViewHolder {

    private TextView titleTv;
    private ImageView rmqPicIv;
    private TextView rmqInfoTv;
    public MessageContent.LinkedContent messageContent;
    private String linkedContentLink;
    private String linkedContentTitle;
    private String linkedContentMsg;
    private String linkedContentPicUrl;

    public ChatNormalWebViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
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
        titleTv.setText(TextUtils.isEmpty(linkedContentTitle) ? linkedContentMsg : linkedContentTitle);
        if (!TextUtils.isEmpty(linkedContentPicUrl))
            loadImage(rmqPicIv, linkedContentPicUrl, CacheManager.imWebImageOptions);
        else
            rmqPicIv.setImageResource(R.drawable.chat_link_default);
        rmqInfoTv.setText(linkedContentMsg);
    }

    @Override
    public void onContentRlClickListener() {
        Intent intent = new Intent();
        if (linkedContentLink.contains(Constants.TOPIC_URL)) {
            intent.setClass(context, WebViewForIndustryCircle.class);
        } else {
            intent.setClass(context, WebViewActWithTitle.class);
        }
        intent.putExtra("picture", linkedContentPicUrl);
        intent.putExtra("title", linkedContentMsg);
        intent.putExtra("url", linkedContentLink);
        if (linkedContentLink.contains("renhe")) {
            String fix = "?";
            if (linkedContentLink.contains("?")) {
                fix = "&";
            }
            intent.putExtra("login",
                    fix + "adSid=" + RenheApplication.getInstance().getUserInfo().getAdSId() + "&sid="
                            + RenheApplication.getInstance().getUserInfo().getSid());
        }
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

