package com.itcalf.renhe.receiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationListener;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Title: ChatConversationListener.java<br>
 * Description: <br>
 * Copyright (c) 人和网版权所有 2015    <br>
 * Create DateTime: 2015-6-27 下午3:30:13 <br>
 *
 * @author wangning
 */
public class MyConversationListener implements ConversationListener {
    private Context ct;
    private SharedPreferences blackListSp;
    private SharedPreferences msp;
    private ConversationListUtil.ConversationCallBack conversationCallBack;
    private ArrayList<ConversationItem> datas;
    private ConversationListUtil conversationListUtil;

    public MyConversationListener(ArrayList<ConversationItem> datas, ConversationListUtil.ConversationCallBack conversationCallBack
            , ConversationListUtil conversationListUtil) {
        ct = RenheApplication.getInstance();
        msp = RenheApplication.getInstance().getUserSharedPreferences();
        blackListSp = ct.getSharedPreferences(Constants.BLOCKED_CONTACTS_SHAREDPREFERENCES, 0);
        this.datas = datas;
        this.conversationCallBack = conversationCallBack;
        this.conversationListUtil = conversationListUtil;
    }

    @Override
    public void onAdded(List<Conversation> conversationList) {
        // 会话事件
        if (conversationList == null || conversationList.isEmpty())
            return;
        Logger.w("conversation onAdded");
        ArrayList<ConversationItem> conversationItems = new ArrayList<>();
        for (Conversation conversation : conversationList) {
            if (conversation.type() == Conversation.ConversationType.CHAT) {
                if (blackListSp.getBoolean(conversation.getPeerId() + "", false)) {
                    continue;
                }
            }
            if (conversation.status() != Conversation.ConversationStatus.OFFLINE) {
                ConversationItem conversationItem = new ConversationItem();
                conversationItem.setType(ConversationItem.IM_CONVERSATION_TYPE);
                if (conversation.type() == Conversation.ConversationType.CHAT) {
                    Map<String, String> chatConverInfoMap = conversation.extension();
                    if (null != chatConverInfoMap) {
                        String name = chatConverInfoMap.get(conversation.getPeerId() + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME);
                        String userface = chatConverInfoMap.get(conversation.getPeerId() + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE);
                        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(userface)) {
                            conversationItem.setNickname(name);
                            conversationItem.setIconUrl(userface);
                        } else {
                            conversationListUtil.getUserInfoById(conversation, conversation.getPeerId());
                        }
                    } else {
                        conversationListUtil.getUserInfoById(conversation, conversation.getPeerId());
                    }
                } else {
                    conversationItem.setNickname(conversation.title());
                    conversationItem.setIconUrl(conversation.icon());
                }
                conversationItem.setConversation(conversation);
                conversationItems.add(conversationItem);
            }
        }
        conversationCallBack.onConversationsAdd(conversationItems);
    }

    @Override
    public void onRemoved(List<Conversation> conversationList) {
        // 会话事件
        if (conversationList == null || conversationList.isEmpty())
            return;
        Logger.w("conversation onRemoved");
        ArrayList<ConversationItem> conversationItems = new ArrayList<>();
        for (Conversation conversation : conversationList) {
            if (conversation.status() != Conversation.ConversationStatus.OFFLINE) {
                ConversationItem conversationItem = getConversationItem(conversation);
                if (null != conversationItem) {
                    conversationItem.setConversation(conversation);
                    conversationItems.add(conversationItem);
                }
            }
        }
        conversationCallBack.onConversationsRemoved(conversationItems);
    }

    private ConversationItem getConversationItem(Conversation conversation) {
        for (ConversationItem conversationItem : datas) {
            if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE &&
                    conversationItem.getConversation().conversationId().equals(conversation.conversationId())) {
                return conversationItem;
            }
        }
        return null;
    }
}
