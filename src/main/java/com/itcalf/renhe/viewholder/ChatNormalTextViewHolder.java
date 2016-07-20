package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.widget.emojitextview.AisenChatTextView;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatNormalTextViewHolder extends ChatViewHolder {

    private String content;//聊天内容
    private AisenChatTextView contentTv;

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        super.initView(holder, messageObj, position);
        contentTv.setOpenId(message.senderId());//设置发送者ID，用来作为aisentextview的spandeString缓存的key
        //根据是否是自己发送的消息来设置文本中链接的颜色
        if (isSenderIsSelf())
            contentTv.setWebLinkColor("#ffffff");
        else
            contentTv.setWebLinkColor("#4492EC");
        content = ((MessageContent.TextContent) message.messageContent()).text();
        if (!TextUtils.isEmpty(content))
            contentTv.setContent(content);
        else
            contentTv.setText("");
    }

    public ChatNormalTextViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                    RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, chatRecyclerView, adapter, conversation);
        contentTv = ((AisenChatTextView) itemView.findViewById(R.id.chat_content_tv));
    }

    @Override
    public void onContentRlClickListener() {

    }

    @Override
    public void onContentRlLongClickListener() {
        if (null != chatUtils) {
            chatUtils.createCopyDialog(context, content, Constants.ChatShareType.CHAT_SHARE_TEXT, null, null, null);
        }
    }

}

