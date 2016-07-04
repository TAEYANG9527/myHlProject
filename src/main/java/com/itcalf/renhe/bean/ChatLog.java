package com.itcalf.renhe.bean;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.Message;

import java.util.List;

/**
 * description :聊天记录
 * Created by Chans Renhenet
 * 2015/7/30
 */
public class ChatLog {

	private Conversation conversation;
	private List<Message> message;

	public ChatLog() {

	}

	public Conversation getConversation() {
		return conversation;
	}

	public void setConversation(Conversation conversation) {
		this.conversation = conversation;
	}

	public List<Message> getMessage() {
		return message;
	}

	public void setMessage(List<Message> message) {
		this.message = message;
	}
}
