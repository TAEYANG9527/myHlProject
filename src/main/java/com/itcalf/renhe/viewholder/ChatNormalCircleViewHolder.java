package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.context.wukong.im.ActivityCircleDetail;
import com.itcalf.renhe.view.TextView;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalCircleViewHolder extends ChatViewHolder {

    private TextView titleTv;
    private ImageView userPicIv;
    private TextView userInfoTv;
    public MessageContent.LinkedContent messageContent;
    private String linkedContentLink;
    private String linkedContentTitle;
    private String linkedContentMsg;
    private String linkedContentPicUrl;

    public ChatNormalCircleViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                      RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        titleTv = ((TextView) itemView.findViewById(R.id.title_tv));
        userPicIv = ((ImageView) itemView.findViewById(R.id.user_pic_iv));
        userInfoTv = ((TextView) itemView.findViewById(R.id.user_info_tv));
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
        loadImage(userPicIv, linkedContentPicUrl);
        SpannableString selfInfoSpannableString = new SpannableString(linkedContentMsg);
        handlerSelfInfoString(selfInfoSpannableString, linkedContentMsg);
        userInfoTv.setText(selfInfoSpannableString);
    }

    private void handlerSelfInfoString(SpannableString spannableString, String content) {
        String[] texts = content.split("\n");
        if (texts.length > 1) {
            int index = content.indexOf("\n");
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.C1)), 0, index,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.C2)), index, content.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    public void onContentRlClickListener() {
        String circleId = linkedContentLink.substring(linkedContentLink.indexOf("//") + 2, linkedContentLink.length());
        Intent bundle = new Intent(context, ActivityCircleDetail.class);
        bundle.putExtra("circleId", circleId);
        context.startActivity(bundle);
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

