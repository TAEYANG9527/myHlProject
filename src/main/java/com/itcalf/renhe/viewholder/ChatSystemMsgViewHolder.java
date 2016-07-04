package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ChatMessage;
import com.itcalf.renhe.utils.ChatUtils;
import com.itcalf.renhe.utils.DateUtil;
import com.itcalf.renhe.view.TextView;

import java.util.Date;

/**
 * @author wangning  on 2015/10/12.
 */
public class ChatSystemMsgViewHolder extends RecyclerHolder {
    public TextView createTimeTv;
    private TextView contentTv;
    public Conversation conversation;
    public ChatMessage chatMessage;
    public Message message;

    public ChatSystemMsgViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                                   RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, adapter);
        this.conversation = conversation;
        createTimeTv = (TextView) itemView.findViewById(R.id.tv_sendtime);
        contentTv = ((TextView) itemView.findViewById(R.id.sysmsg_tv));
    }

    @Override
    public void initView(RecyclerHolder holder, Object messageObj, final int position) {
        if (null == holder)
            return;
        if (null == conversation)
            return;
        if (null == messageObj)
            return;
        if (messageObj instanceof ChatMessage)
            chatMessage = (ChatMessage) messageObj;
        message = chatMessage.getMessage();
        if (null == message)
            return;

        //创建时间
        long datetime = message.createdAt();
        if (datetime > 0) {
            String dateString = DateUtil.newFormatByDay(context, new Date(message.createdAt()));
            createTimeTv.setText(dateString);
        } else {
            createTimeTv.setText("");
        }

        String msg = ((MessageContent.TextContent) message.messageContent()).text();
        if (RenheApplication.getInstance().getUserInfo().getImId() == Integer
                .parseInt(String.valueOf(message.senderId()))) {
            msg = msg.replaceFirst(RenheApplication.getInstance().getUserInfo().getName(), "你");
        }
        contentTv.setText(msg);
    }

}

