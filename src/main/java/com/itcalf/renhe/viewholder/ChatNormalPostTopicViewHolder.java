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
import com.itcalf.renhe.view.WebViewForIndustryCircle;

/**
 * 成员发布新话题，在群里发一条特殊消息
 *
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalPostTopicViewHolder extends ChatViewHolder {

    private TextView titleTv;
    private ImageView rmqPicIv;
    private TextView rmqInfoTv;
    public MessageContent.TextContent messageContent;
    private String linkedContentLink;
    private String linkedContentTitle;
    private String linkedContentMsg;
    private String linkedContentPicUrl;

    private static final String TITLE = "title";//标题
    private static final String CONTENT = "content";//内容
    private static final String IMAGE = "image";// : 图片
    private static final String URL = "url";//: 话题的地址

    public ChatNormalPostTopicViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                         RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        titleTv = ((TextView) itemView.findViewById(R.id.title_tv));
        rmqPicIv = ((ImageView) itemView.findViewById(R.id.rmq_pic_iv));
        rmqInfoTv = ((TextView) itemView.findViewById(R.id.rmq_info_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        messageContent = (MessageContent.TextContent) message.messageContent();
        if (null == messageContent)
            return;
        linkedContentLink = message.extension(URL);
        linkedContentTitle = message.extension(TITLE);
        linkedContentMsg = message.extension(CONTENT);
        linkedContentPicUrl = message.extension(IMAGE);
        titleTv.setText(linkedContentTitle);
        if (!TextUtils.isEmpty(linkedContentPicUrl))
            loadImage(rmqPicIv, linkedContentPicUrl, CacheManager.imageOptions);
        else
            rmqPicIv.setImageResource(R.drawable.chat_link_default);
        rmqInfoTv.setTextColor(context.getResources().getColor(R.color.C1));
        rmqInfoTv.setText(linkedContentMsg);
    }

    @Override
    public void onContentRlClickListener() {
        Intent intent = new Intent();
        intent.setClass(context, WebViewForIndustryCircle.class);
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

