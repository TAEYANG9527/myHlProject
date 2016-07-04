package com.itcalf.renhe.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.alibaba.wukong.im.User;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.bean.ChatMessage;
import com.itcalf.renhe.utils.ChatUtils;
import com.itcalf.renhe.viewholder.ChatCustomLuckyMoneyAdSystemMsgViewHolder;
import com.itcalf.renhe.viewholder.ChatCustomLuckyMoneySystemMsgViewHolder;
import com.itcalf.renhe.viewholder.ChatCustomSystemMsgViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalArchiveViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalCircleNewMemberViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalCircleViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalFileViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalImageViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalLeftAudioViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalLuckyMoneyAdViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalLuckyMoneyViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalPostTopicViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalRightAudioViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalRmqViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalTextViewHolder;
import com.itcalf.renhe.viewholder.ChatNormalWebViewHolder;
import com.itcalf.renhe.viewholder.ChatSystemMsgViewHolder;
import com.itcalf.renhe.viewholder.ChatUnKnownTypeViewHolder;
import com.itcalf.renhe.viewholder.EmptyViewHolder;
import com.itcalf.renhe.viewholder.FooterViewHolder;
import com.itcalf.renhe.viewholder.RecyclerHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 人脉圈列表的recycler adapter
 * Author:wang ning
 */
public class RecyclerChatItemAdapter extends BaseRecyclerAdapter<ChatMessage> {
    //人脉圈类型
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_TEXT = 1;//自己发的，普通留言，纯文字
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_IMAGE = 2;//自己发的，普通留言，图片
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_AUDIO = 3;//自己发的，普通留言，语音


    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_TEXT = 4;//别人发的，普通留言，纯文字
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_IMAGE = 5;//别人发的，普通留言，图片
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_AUDIO = 6;//别人发的，普通留言，语音

    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_ARCHIVE = 7;//自己发的，名片
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_CIRCLE = 8;//自己发的，圈子
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_RMQ = 9;//自己发的，人脉圈
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_WEB = 10;//自己发的，web
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_FILE = 11;//自己发的，文件

    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_ARCHIVE = 12;//别人发的，名片
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_CIRCLE = 13;//别人发的，圈子
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_RMQ = 14;//别人发的，人脉圈
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_WEB = 15;//别人发的，web
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_FILE = 16;//别人发的，文件

    public static final int ITEM_TYPE_SYSTEM_MSG_TYPE = 17;//系统消息
    public static final int ITEM_TYPE_LEFT_UNKOWN_TYPE = 18;//别人发的不支持的类型
    public static final int ITEM_TYPE_RIGHT_UNKOWN_TYPE = 19;//自己发的不支持的类型

    public static final int ITEM_TYPE_HEADER = 20;//headerview

    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_NEW_MEMBER_WELCOME = 21;//别人发的，新人加入圈子，给各位问好
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_NEW_MEMBER_WELCOME = 22;//自己发的，新人加入圈子，给各位问好
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_POST_TOPIC = 23;//别人发的，发布了一个话题
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_POST_TOPIC = 24;//自己发的，发布了一个话题
    public static final int ITEM_TYPE_SYSTEM_MSG_REPLY_TOPIC = 25;//别人回复你发送的系统消息
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY = 26;//别人发的红包
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY = 27;//自己发的红包
    public static final int ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY = 28;//别人抢了你发的红包的系统提示
    public static final int ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY_AD = 29;//别人发的红包广告
    public static final int ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY_AD = 30;//自己发的红包广告
    public static final int ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY_AD = 31;//别人抢了你发的红包广告的系统提示

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private ArrayList<ChatMessage> datas;
    private HashMap<Long, User> kikOutMemberInfo = new HashMap<>(); // 已经被踢出圈子的成员信息
    private List<String> imgsUrlList;//存储聊天记录里所有的image url
    private RecyclerView renMaiQuanRecyclerView;

    private Conversation conversation;
    private boolean isEnd = false;
    private boolean isShowTip = false;//加载更多失败时，提示上拉加载更多
    private String isPlayAudioUrl = "";
    private AnimationDrawable audioAnimationDrawable;//语音播放动画
    private ImageView audioImageView;
    private boolean isShowFooter = false;//是否显示加载更多的进度条

    private ChatUtils chatUtils;

    public RecyclerChatItemAdapter(Context context, RecyclerView view, ArrayList<ChatMessage> datas, Conversation conversation
            , List<String> imgsUrlList) {
        super(view, datas, 0);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.datas = datas;
        this.conversation = conversation;
        this.renMaiQuanRecyclerView = view;
        this.imgsUrlList = imgsUrlList;
    }

    @Override
    public void convert(RecyclerHolder holder, ChatMessage item, int position) {
        if (null != holder)
            holder.initView(holder, item, position);
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_TEXT:
                return new ChatNormalTextViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_normal_text_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_TEXT:
                return new ChatNormalTextViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_normal_text_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_IMAGE:
                return new ChatNormalImageViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_image_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation, imgsUrlList);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_IMAGE:
                return new ChatNormalImageViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_image_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation, imgsUrlList);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_AUDIO:
                return new ChatNormalRightAudioViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_audio_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_AUDIO:
                return new ChatNormalLeftAudioViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_audio_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_ARCHIVE:
                return new ChatNormalArchiveViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_archive_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_ARCHIVE:
                return new ChatNormalArchiveViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_archive_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_CIRCLE:
                return new ChatNormalCircleViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_circle_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_CIRCLE:
                return new ChatNormalCircleViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_circle_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_RMQ:
                return new ChatNormalRmqViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_rmq_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_RMQ:
                return new ChatNormalRmqViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_rmq_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_WEB:
                return new ChatNormalWebViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_web_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_WEB:
                return new ChatNormalWebViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_web_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_FILE:
                return new ChatNormalFileViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_file_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_FILE:
                return new ChatNormalFileViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_file_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_NEW_MEMBER_WELCOME:
                return new ChatNormalCircleNewMemberViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_archive_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_NEW_MEMBER_WELCOME:
                return new ChatNormalCircleNewMemberViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_archive_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_POST_TOPIC:
                return new ChatNormalPostTopicViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_rmq_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_POST_TOPIC:
                return new ChatNormalPostTopicViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_rmq_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_SYSTEM_MSG_TYPE:
                return new ChatSystemMsgViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_system_msg_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_SYSTEM_MSG_REPLY_TOPIC:
                return new ChatCustomSystemMsgViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_system_msg_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY:
                return new ChatCustomLuckyMoneySystemMsgViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_luckymoney_system_msg_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY_AD:
                return new ChatCustomLuckyMoneyAdSystemMsgViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_luckymoney_system_msg_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY:
                return new ChatNormalLuckyMoneyViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_luckymoney_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY:
                return new ChatNormalLuckyMoneyViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_luckymoney_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY_AD:
                return new ChatNormalLuckyMoneyAdViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_luckymoney_ad_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY_AD:
                return new ChatNormalLuckyMoneyAdViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_luckymoney_ad_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_RIGHT_UNKOWN_TYPE://不支持查看的消息类型
                return new ChatUnKnownTypeViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_right_base_item_normal_text_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_LEFT_UNKOWN_TYPE://不支持查看的消息类型
                return new ChatUnKnownTypeViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_left_base_item_normal_text_layout,
                        parent, false), renMaiQuanRecyclerView, this, conversation);
            case ITEM_TYPE_HEADER:
                return new FooterViewHolder(mContext, mLayoutInflater.inflate(R.layout.chat_recycler_view_header,
                        parent, false), this, Constants.RENMAIQUAN_CONSTANTS.LOAD_TYPE_LOADING_WITHOUT_TEXT);
            default:
                break;
        }
        return new EmptyViewHolder(mContext, mLayoutInflater.inflate(R.layout.renmaiquan_item_empty_layout, parent, false), this);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() > 0) {
            if (position == 0 && isShowFooter()) {
                return ITEM_TYPE_HEADER;
            }
        }
        if (position < datas.size()) {
            ChatMessage chatMessage = datas.get(position);
            if (null != chatMessage) {
                Message message = chatMessage.getMessage();
                if (null != message) {
                    if (message.creatorType() == Message.CreatorType.SYSTEM) {
                        if ((null != conversation && conversation.type() == Conversation.ConversationType.CHAT)) {//普通文本消息
                            if (TextUtils.isEmpty(message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY))) {
                                return ITEM_TYPE_SYSTEM_MSG_TYPE;
                            } else {
                                String customType = message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                                if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY_NOTICE)) {//抢红包系统提示
                                    return ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY;
                                } else {
                                    return ITEM_TYPE_SYSTEM_MSG_TYPE;
                                }
                            }
                        } else {
                            if (TextUtils.isEmpty(message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY))) {
                                return ITEM_TYPE_SYSTEM_MSG_TYPE;
                            } else {
                                String customType = message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                                if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_TOPIC_REPLY)) {
                                    return ITEM_TYPE_SYSTEM_MSG_REPLY_TOPIC;
                                } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY_NOTICE)) {//抢红包系统提示
                                    return ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY;
                                } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY_AD_NOTICE)) {//抢红包广告系统提示
                                    return ITEM_TYPE_SYSTEM_MSG_LUCKYMONEY_AD;
                                } else {
                                    return ITEM_TYPE_SYSTEM_MSG_TYPE;
                                }
                            }
                        }
                    }
                    int type = message.messageContent().type();
                    switch (type) {
                        case MessageContent.MessageContentType.TEXT:
                            if (isSenderIsSelf(message)) {
                                if (null != conversation) {
                                    if ((conversation.type() == Conversation.ConversationType.CHAT)) {//普通文本消息
                                        if (TextUtils.isEmpty(message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY))) {
                                            return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_TEXT;
                                        } else {
                                            String customType = message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                                            if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY)) {//红包
                                                return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY;
                                            } else {
                                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_UNKOWN_TYPE :
                                                        ITEM_TYPE_LEFT_UNKOWN_TYPE;
                                            }
                                        }
                                    } else {//自定义的文本消息（发布一个新话题、新人进圈子的欢迎提示）
                                        String customType = message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                                        if (TextUtils.isEmpty(customType)) {
                                            return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_TEXT;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_CIRCLE_NEW_MEMBER_PROFILE)) {//刚加入圈子，发条打招呼内容
                                            return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_NEW_MEMBER_WELCOME;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_TOPIC_SEND)) {//成员发布新话题
                                            return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_POST_TOPIC;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY)) {//红包
                                            return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY_AD)) {//红包广告
                                            return ITEM_TYPE_RIGHT_NORMAL_MESSAGE_LUCKY_MONEY_AD;
                                        } else {
                                            return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_UNKOWN_TYPE :
                                                    ITEM_TYPE_LEFT_UNKOWN_TYPE;
                                        }
                                    }
                                } else {
                                    break;
                                }
                            } else {
                                if (null != conversation) {
                                    if (conversation.type() == Conversation.ConversationType.CHAT) {
                                        if (TextUtils.isEmpty(message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY))) {
                                            return ITEM_TYPE_LEFT_NORMAL_MESSAGE_TEXT;
                                        } else {
                                            String customType = message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                                            if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY)) {//红包
                                                return ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY;
                                            } else {
                                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_UNKOWN_TYPE :
                                                        ITEM_TYPE_LEFT_UNKOWN_TYPE;
                                            }
                                        }
                                    } else {
                                        String customType = message.extension(Constants.CHAT_CUSTOM_TYPE.CUSTOM_TYPE_KEY);
                                        if (TextUtils.isEmpty(customType)) {
                                            return ITEM_TYPE_LEFT_NORMAL_MESSAGE_TEXT;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_CIRCLE_NEW_MEMBER_PROFILE)) {//刚加入圈子，发条打招呼内容
                                            return ITEM_TYPE_LEFT_NORMAL_MESSAGE_NEW_MEMBER_WELCOME;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_TOPIC_SEND)) {//成员发布新话题
                                            return ITEM_TYPE_LEFT_NORMAL_MESSAGE_POST_TOPIC;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY)) {//红包
                                            return ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY;
                                        } else if (customType.equals(Constants.CHAT_CUSTOM_TYPE.MESSAGE_LUCKY_MONEY_AD)) {//红包广告
                                            return ITEM_TYPE_LEFT_NORMAL_MESSAGE_LUCKY_MONEY_AD;
                                        } else {
                                            return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_UNKOWN_TYPE :
                                                    ITEM_TYPE_LEFT_UNKOWN_TYPE;
                                        }
                                    }
                                } else {
                                    break;
                                }

                            }
                        case MessageContent.MessageContentType.IMAGE:
                            return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_IMAGE :
                                    ITEM_TYPE_LEFT_NORMAL_MESSAGE_IMAGE;
                        case MessageContent.MessageContentType.AUDIO:
                            return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_AUDIO :
                                    ITEM_TYPE_LEFT_NORMAL_MESSAGE_AUDIO;
                        case MessageContent.MessageContentType.LINKED:
                            MessageContent.LinkedContent messageContent = (MessageContent.LinkedContent) message.messageContent();
                            String linkedContentLink = messageContent.url();
                            if (linkedContentLink.startsWith("msg")) {
                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_RMQ :
                                        ITEM_TYPE_LEFT_NORMAL_MESSAGE_RMQ;
                            } else if (linkedContentLink.startsWith("http")) {
                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_WEB :
                                        ITEM_TYPE_LEFT_NORMAL_MESSAGE_WEB;
                            } else if (linkedContentLink.startsWith("user")) {
                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_ARCHIVE :
                                        ITEM_TYPE_LEFT_NORMAL_MESSAGE_ARCHIVE;
                            } else if (linkedContentLink.startsWith("group")) {
                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_CIRCLE :
                                        ITEM_TYPE_LEFT_NORMAL_MESSAGE_CIRCLE;
                            } else {
                                return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_UNKOWN_TYPE :
                                        ITEM_TYPE_LEFT_UNKOWN_TYPE;
                            }
                        case MessageContent.MessageContentType.FILE:
                            return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_NORMAL_MESSAGE_FILE :
                                    ITEM_TYPE_LEFT_NORMAL_MESSAGE_FILE;
                        default:
                            return isSenderIsSelf(message) ? ITEM_TYPE_RIGHT_UNKOWN_TYPE :
                                    ITEM_TYPE_LEFT_UNKOWN_TYPE;
                    }
                }
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return datas == null ? 1 : datas.size() + 1;//多了一个footer
    }

    public void addChatMessageItem(ChatMessage t) {
        if (t == null)
            return;
        if (isMessageExist(t))
            return;
        datas.add(t);
        notifyItemInserted(getItemCount());
    }

    public void addChatMessageItems(List<ChatMessage> list) {
        if (list == null || list.size() == 0)
            return;
        int oldCount = datas.size();
        for (ChatMessage t : list) {
            if (!isMessageExist(t)) {
                datas.add(t);
            }
        }
        if (datas.size() > oldCount) {
            sort();
            notifyDataSetChanged();
        }
    }

    private boolean isMessageExist(ChatMessage chatMessage) {
        for (ChatMessage t : datas) {
            if (chatMessage.getMessage().messageId() == t.getMessage().messageId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 更新单个message
     *
     * @param message
     */
    public void updateMessageItem(Message message) {
        if (message == null)
            return;
        ChatMessage chatMessage = null;
        boolean result = false;
        int targetPosition = 0;

        if (datas.size() > 0) {
            Iterator<ChatMessage> it = datas.iterator();
            while (it.hasNext()) {
                ChatMessage current = it.next();
                if (current.getMessage().equals(message)) {
                    chatMessage = current;
                    result = true;
                    break;
                }
                targetPosition++;
            }

            //新的数据替换老的数据
            if (result && null != chatMessage) {
                datas.set(targetPosition, chatMessage);
                notifyItemChanged(targetPosition);
            }
        }
    }

    /**
     * 刷新RecyclerView某个item view的状态
     *
     * @param message
     */
    public void updateItemView(Message message) {
        if (message == null)
            return;
        boolean result = false;
        int targetPosition = 0;

        if (datas.size() > 0) {
            Iterator<ChatMessage> it = datas.iterator();
            while (it.hasNext()) {
                ChatMessage current = it.next();
                if (current.getMessage().equals(message)) {
                    result = true;
                    break;
                }
                targetPosition++;
            }
            if (result) {
                RecyclerView.ViewHolder viewHolder = renMaiQuanRecyclerView.findViewHolderForAdapterPosition(targetPosition);
                if (null != viewHolder && viewHolder instanceof RecyclerHolder) {
                    if (viewHolder instanceof ChatNormalFileViewHolder) {//如果是发送文件，更新进度条
                        ChatNormalFileViewHolder holder = (ChatNormalFileViewHolder) viewHolder;
                        holder.fileProgressBar.setProgress(message.sendProgress());
                    }
                }
            }
        }
    }

    /**
     * 更新message集合
     *
     * @param list
     */
    public void updateMessagesItems(List<Message> list) {
        if (list == null || list.size() == 0)
            return;

        for (Message t : list) {
            //因为老是出现MessageListener的onChanged回调被无限调用，导致有onChanged的item一直闪烁，目前发现的都是别人发的消息会调用onChanged，所以在这暂时加个限制，只有自己发的消息才允许updateitem
            if (isSenderIsSelf(t))
                updateMessageItem(t);
        }
    }

    /**
     * 删除单条message
     *
     * @param message
     */
    public void deleteMessageItem(Message message) {
        if (message == null)
            return;
        boolean result = false;
        int targetPosition = 0;

        if (datas.size() > 0) {
            Iterator<ChatMessage> it = datas.iterator();
            while (it.hasNext()) {
                ChatMessage current = it.next();
                if (current.getMessage().equals(message)) {
                    result = true;
                    break;
                }
                targetPosition++;
            }

            //新的数据替换老的数据
            if (result) {
                datas.remove(targetPosition);
                notifyItemRangeChanged(targetPosition, getItemCount());
            }
        }
    }

    /**
     * 删除message集合
     *
     * @param list
     */
    public void deleteMessagesItems(List<Message> list) {
        if (list == null || list.size() == 0)
            return;

        for (Message t : list)
            deleteMessageItem(t);
    }

    public boolean isShowFooter() {
        return isShowFooter;
    }

    public void setIsShowFooter(boolean isShowFooter) {
        this.isShowFooter = isShowFooter;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setIsEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public boolean isShowTip() {
        return isShowTip;
    }

    public void setIsShowTip(boolean isShowTip) {
        this.isShowTip = isShowTip;
    }

    public List<ChatMessage> getDatasList() {
        return this.datas;
    }

    /**
     * 判断这条message是否是自己发的
     *
     * @return
     */
    public boolean isSenderIsSelf(Message message) {
        if (message.senderId() == RenheApplication.getInstance().currentOpenId) {
            return true; // 自己发送的消息
        } else {
            return false; // 别人发送的消息
        }
    }

    public HashMap<Long, User> getKikOutMemberInfo() {
        return kikOutMemberInfo;
    }

    public void setKikOutMemberInfo(HashMap<Long, User> kikOutMemberInfo) {
        this.kikOutMemberInfo = kikOutMemberInfo;
    }

    public void addKikOutMemberInfoItem(long openId, User user) {
        if (null != kikOutMemberInfo) {
            kikOutMemberInfo.put(openId, user);
        }
    }

    public String getIsPlayAudioUrl() {
        return isPlayAudioUrl;
    }

    public void setIsPlayAudioUrl(String isPlayAudioUrl) {
        this.isPlayAudioUrl = isPlayAudioUrl;
    }

    public AnimationDrawable getAudioAnimationDrawable() {
        return audioAnimationDrawable;
    }

    public void setAudioAnimationDrawable(AnimationDrawable audioAnimationDrawable) {
        this.audioAnimationDrawable = audioAnimationDrawable;
    }

    public ImageView getAudioImageView() {
        return audioImageView;
    }

    public void setAudioImageView(ImageView audioImageView) {
        this.audioImageView = audioImageView;
    }

    @Override
    public void sort() {
        super.sort();
        Collections.sort(datas, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage lhs, ChatMessage rhs) {
                return lhs.getMessage().createdAt() > rhs.getMessage().createdAt() ? 1 : -1;
            }
        });
    }

    public ChatUtils getChatUtils() {
        return chatUtils;
    }

    public void setChatUtils(ChatUtils chatUtils) {
        this.chatUtils = chatUtils;
    }
}
