package com.itcalf.renhe.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.ConversationRecyclerItemAdapter;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.dto.ConversationItem;
import com.itcalf.renhe.utils.ConversationListUtil;
import com.itcalf.renhe.utils.DateUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author wangning  on 2015/10/12.
 */
public class ConversationViewHolder extends RecyclerHolder {
    private RecyclerView mRecyclerView;
    private ConversationItem conversationItem;
    private String senderName;
    private String senderUserface;
    private long createdAt;
    private StringBuilder lastMsg;
    private int unreadCount;
    private ArrayList<ConversationItem> datas;
    //item 头部布局
    private ImageView userHeadIv;
    private TextView userNameTv;
    private TextView createTimeTv;
    private TextView lastmsgTv;
    private TextView lastMsgTipTv;//[有人@我]、[草稿]
    private TextView mentionTv;
    private TextView mentionBotherTv;
    private ConversationRecyclerItemAdapter conversationRecyclerItemAdapter;

    //工具初始化
    public ConversationListUtil conversationListUtil;

    public static final String HIGHLIGHT = "highlight";//高亮的内容
    public static final String CONTENT = "content";//标题
    public static final String OPENID = "openId";// 需要提醒的人
    public static final String TAB_ICON_NUM_TOO_LARGE = "...";
    public ConversationViewHolder(Context context, View itemView, RecyclerView mRecyclerView,
                                  RecyclerView.Adapter adapter) {
        super(context, itemView, adapter);
        if (null != adapter && adapter instanceof ConversationRecyclerItemAdapter) {
            conversationRecyclerItemAdapter = (ConversationRecyclerItemAdapter) adapter;
            datas = (ArrayList<ConversationItem>) conversationRecyclerItemAdapter.getDatasList();
        }
        this.mRecyclerView = mRecyclerView;
        userHeadIv = (ImageView) itemView.findViewById(R.id.cl_icon);
        userNameTv = (TextView) itemView.findViewById(R.id.cl_title);
        createTimeTv = (TextView) itemView.findViewById(R.id.cl_time);
        lastmsgTv = (TextView) itemView.findViewById(R.id.cl_lastmsg);
        mentionTv = (TextView) itemView.findViewById(R.id.message_mention);
        mentionBotherTv = (TextView) itemView.findViewById(R.id.message_mention_bother);
        lastMsgTipTv = (TextView) itemView.findViewById(R.id.cl_tip_tv);
        this.conversationListUtil = conversationRecyclerItemAdapter.getConversationListUtil();
    }

    @Override
    public void initView(RecyclerHolder holder, Object conversationObj, final int position) {
        if (null == holder)
            return;
        if (null == conversationObj)
            return;
        if (conversationObj instanceof ConversationItem) {
            conversationItem = (ConversationItem) conversationObj;
            initData();
            initConversationView();
            initListener();
        }
    }

    private void initData() {
        senderName = conversationItem.getNickname();
        senderUserface = conversationItem.getIconUrl();
        lastMsg = new StringBuilder();
        if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
            Message lastMessage = conversationItem.getConversation().latestMessage();
            createdAt = (null == lastMessage ? conversationItem.getConversation().createdAt() : lastMessage.createdAt());
//            if (conversationItem.getConversation().type() == Conversation.ConversationType.GROUP && null != lastMessage) {//如果是群聊，获取message发送者的姓名
//                String messageExtension = lastMessage.extension(Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME + ";" + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE);
//                if (!TextUtils.isEmpty(messageExtension)) {
//                    String[] messageExtensionArray = messageExtension.split(";");
//                    if (null != messageExtensionArray && messageExtensionArray.length > 1) {
//                        String senderName = messageExtensionArray[0];
//                        if (!TextUtils.isEmpty(senderName))
//                            lastMsg.append(senderName + ":");
//                    }
//                }
//            }

            if (null != lastMessage) {
                switch (lastMessage.messageContent().type()) {
                    case MessageContent.MessageContentType.TEXT:
                        lastMsg.append(((MessageContent.TextContent) lastMessage.messageContent()).text());
                        break;
                    case MessageContent.MessageContentType.IMAGE:
                        lastMsg.append("[图片]");
                        break;
                    case MessageContent.MessageContentType.AUDIO:
                        lastMsg.append("[语音]");
                        break;
                    case MessageContent.MessageContentType.FILE:
                        String fileName = ((MessageContent.FileContent) lastMessage.messageContent()).fileName();
                        lastMsg.append("[" + context.getString(R.string.file_default_name) + "]").append(TextUtils.isEmpty(fileName) ? "" : fileName);
                        break;
                    case MessageContent.MessageContentType.LINKED:
                        String forwardLink = ((MessageContent.LinkedContent) lastMessage.messageContent()).url();
                        String forwardTitle = ((MessageContent.LinkedContent) lastMessage.messageContent()).title();
                        String forwardText = ((MessageContent.LinkedContent) lastMessage.messageContent()).text();
                        if (!TextUtils.isEmpty(forwardLink)) {
                            if (forwardLink.startsWith("msg")) {
                                if (!TextUtils.isEmpty(forwardTitle)) {
                                    lastMsg.append("[" + forwardTitle + "]").append(TextUtils.isEmpty(forwardText) ? "" : forwardText);
                                } else {
                                    lastMsg.append("[" + context.getString(R.string.renmaiquan_share_default_name) + "]").append(TextUtils.isEmpty(forwardText) ? "" : forwardText);
                                }
                            } else if (forwardLink.startsWith("http")) {
                                lastMsg.append("[" + context.getString(R.string.link_default_name) + "]").append(TextUtils.isEmpty(forwardText) ? "" : forwardText);
                            } else if (forwardLink.startsWith("user")) {
                                lastMsg.append("[" + context.getString(R.string.vcard_share_default_name) + "]").append(TextUtils.isEmpty(forwardText) ? "" : forwardText);
                            } else if (forwardLink.startsWith("group")) {
                                lastMsg.append("[" + context.getString(R.string.cicle_share_default_name) + "]").append(TextUtils.isEmpty(forwardText) ? "" : forwardText);
                            } else {
                                lastMsg.append("[" + context.getString(R.string.chat_unsupport_message) + "]").append(TextUtils.isEmpty(forwardText) ? "" : forwardText);
                            }
                        } else {
                            lastMsg.append("[" + context.getString(R.string.chat_unsupport_message) + "]");
                        }
                        break;
                    default:
                        lastMsg.append("[" + context.getString(R.string.chat_unsupport_message) + "]");
                        break;
                }
            }
            unreadCount = conversationItem.getConversation().unreadMessageCount();
        } else {
            createdAt = conversationItem.getConversationListOtherItem().getCreateTime();
            lastMsg.append(conversationItem.getConversationListOtherItem().getLastMessage());
            unreadCount = conversationItem.getConversationListOtherItem().getUnreadCount();
        }
    }

    private void initConversationView() {
//        switch (conversationItem.getType()) {
//            case ConversationItem.IM_CONVERSATION_TYPE:
//                Glide.with(context)
//                        .load(senderUserface)
//                        .placeholder(R.drawable.avatar)
//                        .centerCrop()
//                        .crossFade()
//                        .into(userHeadIv);
//                break;
//            case ConversationItem.SYSTEM_MSG_TYPE:
//                userHeadIv.setImageResource(R.drawable.sysmsg);
//                break;
//            case ConversationItem.TOU_TIAO_TYPE:
//                userHeadIv.setImageResource(R.drawable.toutiao);
//                break;
//            default:
//                break;
//        }

        try {
            imageLoader.displayImage(senderUserface, userHeadIv, CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        userNameTv.setText(senderName);
        lastmsgTv.setText(lastMsg.toString());

        if (conversationItem.getType() == ConversationItem.IM_CONVERSATION_TYPE) {
            if (!TextUtils.isEmpty(conversationItem.getConversation().draftMessage())) {
                lastMsgTipTv.setText(context.getString(R.string.conversation_lastmsg_drft_tip));
                lastmsgTv.setText(conversationItem.getConversation().draftMessage());
            }
            boolean hasTopicReplyed = false;
            try {
                String customTopicReplyType = conversationItem.getConversation().extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                if (!TextUtils.isEmpty(customTopicReplyType) && customTopicReplyType.equals(Constants.CHAT_CUSTOM_TYPE.CONVERSATION_TOPIC_REPLY)) {
                    int topicReplyOpenId = Integer.parseInt(conversationItem.getConversation().extension(OPENID));
                    if (topicReplyOpenId == RenheApplication.getInstance().currentOpenId) {
                        String heighLightString = conversationItem.getConversation().extension(HIGHLIGHT);
                        if (!TextUtils.isEmpty(heighLightString)) {
                            lastMsgTipTv.setText(MessageFormat.format(context.getString(R.string.conversation_lastmsg_topic_replyed_tip), heighLightString));
                            hasTopicReplyed = true;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (conversationItem.getConversation().hasUnreadAtMeMessage()) {
                lastMsgTipTv.setText(context.getString(R.string.conversation_lastmsg_atme_tip));
            }
            if (conversationItem.getConversation().hasUnreadAtMeMessage() || !TextUtils.isEmpty(conversationItem.getConversation().draftMessage())
                    || hasTopicReplyed)
                lastMsgTipTv.setVisibility(View.VISIBLE);
            else
                lastMsgTipTv.setVisibility(View.GONE);
            //检查conversation是否处于消息免打扰的状态
            if (ConversationListUtil.checkIfConversationUnBother(conversationItem.getConversation().conversationId())) {
                if (unreadCount > 0) {
                    mentionBotherTv.setVisibility(View.VISIBLE);
                } else {
                    mentionBotherTv.setVisibility(View.GONE);
                }
                mentionTv.setVisibility(View.GONE);
            } else {
                if (unreadCount > 0) {
                    mentionTv.setVisibility(View.VISIBLE);
                    if(unreadCount > 99){
                        mentionTv.setText(TAB_ICON_NUM_TOO_LARGE);
                    }else {
                        mentionTv.setText(unreadCount + "");
                    }
                } else {
                    mentionTv.setVisibility(View.GONE);
                }
                mentionBotherTv.setVisibility(View.GONE);
            }
        } else {
            if (unreadCount > 0) {
                mentionTv.setVisibility(View.VISIBLE);
                if(unreadCount > 99){
                    mentionTv.setText(TAB_ICON_NUM_TOO_LARGE);
                }else {
                    mentionTv.setText(unreadCount + "");
                }
            } else {
                mentionTv.setVisibility(View.GONE);
            }
            lastMsgTipTv.setVisibility(View.GONE);
        }
        createTimeTv.setText(DateUtil.conversationFormatByDayForListDisply(context, new Date(createdAt)));

        if (!conversationRecyclerItemAdapter.isNormalConversationList()) {
            mentionTv.setVisibility(View.GONE);
            mentionBotherTv.setVisibility(View.GONE);
        }
    }

    public void initListener() {

    }
}

