package com.itcalf.renhe.receiver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.ConversationChangeListener;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Title: ChatFragmentConversationChangeListener.java<br>
 * Description: 会话局部属性变更<br>
 * Copyright (c) 人和网版权所有 2015    <br>
 * Create DateTime: 2015-6-29 下午1:37:43 <br>
 *
 * @author wangning
 */
public class MyConversationChangeListener extends ConversationChangeListener {
    private Context ct;
    private SharedPreferences blackListSp;
    private SharedPreferences msp;
    private ConversationListUtil.ConversationCallBack conversationCallBack;
    private ArrayList<ConversationItem> datas;
    private ConversationListUtil conversationListUtil;

    public MyConversationChangeListener(ArrayList<ConversationItem> datas,
                                        ConversationListUtil.ConversationCallBack conversationCallBack
            , ConversationListUtil conversationListUtil) {
        ct = RenheApplication.getInstance();
        msp = RenheApplication.getInstance().getUserSharedPreferences();
        blackListSp = ct.getSharedPreferences(Constants.BLOCKED_CONTACTS_SHAREDPREFERENCES, 0);
        this.datas = datas;
        this.conversationCallBack = conversationCallBack;
        this.conversationListUtil = conversationListUtil;
    }

    /**
     * 会话标题变更
     */
    @Override
    public void onTitleChanged(List<Conversation> list) {
        Logger.w("onTitleChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话icon变更
     */
    @Override
    public void onIconChanged(List<Conversation> list) {
        Logger.w("onIconChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话状态变更
     */
    @Override
    public void onStatusChanged(List<Conversation> list) {
        Logger.w("onStatusChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话标题变更，如
     */
    @Override
    public void onLatestMessageChanged(List<Conversation> list) {
        Logger.w("onLatestMessageChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话未读消息数变更
     */
    @Override
    public void onUnreadCountChanged(List<Conversation> list) {
        Logger.w("onUnreadCountChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话草稿变更
     */
    @Override
    public void onDraftChanged(List<Conversation> list) {
        Logger.w("onDraftChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话tag变更
     */
    @Override
    public void onTagChanged(List<Conversation> list) {
        Logger.w("onTagChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话extension变更
     */
    @Override
    public void onExtensionChanged(List<Conversation> list) {
        Logger.w("onExtensionChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话@状态变更
     */
    @Override
    public void onAtMeStatusChanged(List<Conversation> list) {
        Logger.w("onAtMeStatusChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话是否通知的状态变更
     */
    @Override
    public void onNotificationChanged(List<Conversation> list) {
        Logger.w("onNotificationChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话置顶状态变更
     */
    @Override
    public void onTopChanged(List<Conversation> list) {
        Logger.w("onTopChanged");
        doUpdateConversation(list);
    }

    /**
     * 会话成员数变更
     */
    @Override
    public void onMemberCountChanged(List<Conversation> list) {
        Logger.w("onMemberCountChanged");
        doUpdateConversation(list);
    }

    @Override
    public void onLocalExtrasChanged(List<Conversation> list) {
        Logger.w("onLocalExtrasChanged");
        doUpdateConversation(list);
    }

    /**
     * 执行conversation更新的具体操作
     *
     * @param conversationList
     */
    private void doUpdateConversation(List<Conversation> conversationList) {
        if (conversationList == null || conversationList.isEmpty())
            return;
        Logger.w("doUpdateConversation");
        ArrayList<ConversationItem> conversationItems = new ArrayList<>();
        ArrayList<ConversationItem> kickOuts = new ArrayList<>();
        for (Conversation conversation : conversationList) {
            if (conversation.type() == Conversation.ConversationType.CHAT) {
                if (blackListSp.getBoolean(conversation.getPeerId() + "", false)) {
                    continue;
                }
            }
            if (conversation.status() != Conversation.ConversationStatus.OFFLINE) {
                ConversationItem conversationItem = getConversationItem(conversation);
                if (null != conversationItem) {
                    conversationItem.setConversation(conversation);
                    if (conversation.type() == Conversation.ConversationType.CHAT) {
                        if (TextUtils.isEmpty(conversationItem.getNickname()) || TextUtils.isEmpty(conversationItem.getIconUrl())) {
                            Map<String, String> chatConverInfoMap = conversation.extension();
                            if (null != chatConverInfoMap) {
                                String name = chatConverInfoMap.get(conversation.getPeerId() + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME);
                                String userface = chatConverInfoMap.get(conversation.getPeerId() + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE);
                                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(userface)) {
                                    conversationItem.setNickname(name);
                                    conversationItem.setIconUrl(userface);
                                }
                            }
                        }
                    }
                    conversationItems.add(conversationItem);
                    //关闭掉相应会话的notification
                    //被圈主移除
                    if (conversation.status() == Conversation.ConversationStatus.KICKOUT) {
                        kickOuts.add(conversationItem);
                    }
                } else {
                    conversationItem = new ConversationItem();
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
                if (conversation.status() == Conversation.ConversationStatus.HIDE
                        || conversation.status() == Conversation.ConversationStatus.KICKOUT
                        || conversation.status() == Conversation.ConversationStatus.QUIT) {
                    int openId = conversation.type() == Conversation.ConversationType.CHAT ? (int) conversation.getPeerId()
                            : Integer.parseInt(conversation.conversationId());
                    ((NotificationManager) ct.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(openId);
                }
            }
        }
        if (kickOuts.size() > 0) {
            conversationCallBack.onConversationsRemoved(kickOuts);
        }
        conversationCallBack.onConversationsUpdated(conversationItems);
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
