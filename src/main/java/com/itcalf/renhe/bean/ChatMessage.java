package com.itcalf.renhe.bean;

import com.alibaba.wukong.im.Message;
import com.itcalf.renhe.viewholder.ChatViewHolder;

/**
 * Created by wangning on 2015/11/17.
 */
public class ChatMessage {
    private String senderName;
    private String senderUserFace;
    private Message message;
    private ChatViewHolder chatViewHolder;
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderUserFace() {
        return senderUserFace;
    }

    public void setSenderUserFace(String senderUserFace) {
        this.senderUserFace = senderUserFace;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public ChatViewHolder getChatViewHolder() {
        return chatViewHolder;
    }

    public void setChatViewHolder(ChatViewHolder chatViewHolder) {
        this.chatViewHolder = chatViewHolder;
    }
}
