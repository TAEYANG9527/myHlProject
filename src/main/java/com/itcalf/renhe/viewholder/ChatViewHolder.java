package com.itcalf.renhe.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.alibaba.wukong.im.User;
import com.alibaba.wukong.im.UserService;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.adapter.RecyclerChatItemAdapter;
import com.itcalf.renhe.bean.ChatMessage;
import com.itcalf.renhe.cache.CacheManager;
import com.itcalf.renhe.context.archives.MyHomeArchivesActivity;
import com.itcalf.renhe.context.wukong.im.RenheIMUtil;
import com.itcalf.renhe.utils.ChatUtils;
import com.itcalf.renhe.utils.DateUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author wangning  on 2015/10/12.
 */
public abstract class ChatViewHolder extends RecyclerHolder {
    public RecyclerView chatRecyclerView;
    public Conversation conversation;
    public ChatMessage chatMessage;
    public Message message;
    public String senderName;
    public String senderUserface;
    public ArrayList<ChatMessage> datas;
    public HashMap<Long, User> kikOutMemberInfo; // 已经被踢出圈子的成员信息
    //item 头部布局
    public ImageView userHeadIv;
    public TextView userNameTv;
    public TextView createTimeTv;
    public RelativeLayout contentRl;
    public TextView sendFailTv;
    public ProgressBar sendingPb;
    public RecyclerChatItemAdapter recyclerChatItemAdapter;

    //工具初始化
    public ChatUtils chatUtils;

    public ChatViewHolder(Context context, View itemView, RecyclerView chatRecyclerView,
                          RecyclerView.Adapter adapter, Conversation conversation) {
        super(context, itemView, adapter);
        if (null != adapter && adapter instanceof RecyclerChatItemAdapter) {
            recyclerChatItemAdapter = (RecyclerChatItemAdapter) adapter;
            datas = (ArrayList<ChatMessage>) recyclerChatItemAdapter.getDatasList();
            kikOutMemberInfo = recyclerChatItemAdapter.getKikOutMemberInfo();
        }
        this.chatRecyclerView = chatRecyclerView;
        this.conversation = conversation;
        userHeadIv = (ImageView) itemView.findViewById(R.id.user_head_iv);
        userNameTv = (TextView) itemView.findViewById(R.id.user_name_tv);
        createTimeTv = (TextView) itemView.findViewById(R.id.tv_sendtime);
        contentRl = (RelativeLayout) itemView.findViewById(R.id.content_rl);
        sendFailTv = (TextView) itemView.findViewById(R.id.tv_sendFail);
        sendingPb = (ProgressBar) itemView.findViewById(R.id.pb_sending);
        this.chatUtils = recyclerChatItemAdapter.getChatUtils();
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
        chatMessage.setChatViewHolder(this);
        message = chatMessage.getMessage();
        if (null == message)
            return;
        initSelfInfoView(position);
        initPublicView(position);
        initData();
        initListener();
    }

    private void initSelfInfoView(int position) {
        senderName = chatMessage.getSenderName();
        senderUserface = chatMessage.getSenderUserFace();

        if (TextUtils.isEmpty(senderName) && null != kikOutMemberInfo && null != kikOutMemberInfo.get(message.senderId())) {
            senderName = kikOutMemberInfo.get(message.senderId()).nickname();
        }
        if (TextUtils.isEmpty(senderUserface) && null != kikOutMemberInfo && null != kikOutMemberInfo.get(message.senderId())) {
            senderUserface = kikOutMemberInfo.get(message.senderId()).avatar();
        }
        if (conversation.type() == Conversation.ConversationType.GROUP) {
            if (isSenderIsSelf()) {
                userNameTv.setVisibility(View.GONE);
            } else {
                userNameTv.setVisibility(View.VISIBLE);
                userNameTv.setText(senderName);
            }
            try {
                imageLoader.displayImage(senderUserface, userHeadIv, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (TextUtils.isEmpty(senderName) || TextUtils.isEmpty(senderUserface)) {
                getUserInfoFromWuKong(position, message.senderId());
            }
        } else {
            try {
                imageLoader.displayImage(senderUserface, userHeadIv, CacheManager.circleImageOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            userNameTv.setVisibility(View.GONE);
        }
    }

    private void initPublicView(int position) {
        //创建时间
        if (message.createdAt() > 0) {
            if (position > 0) {
                if (null != datas.get(position - 1)) {
                    if (message.createdAt() - datas.get(position - 1).getMessage().createdAt() > Constants.CHAT_CONSTANTS.IM_SHOW_TIME_DIFF) {// 新的消息据上条相差5分钟，才显示时间
                        createTimeTv.setVisibility(View.VISIBLE);
                        createTimeTv.setText(DateUtil.newFormatByDay(context, new Date(message.createdAt())));
                    } else {
                        createTimeTv.setVisibility(View.GONE);
                    }
                } else {
                    createTimeTv.setVisibility(View.VISIBLE);
                    createTimeTv.setText(DateUtil.newFormatByDay(context, new Date(message.createdAt())));
                }
            } else {
                createTimeTv.setText(DateUtil.newFormatByDay(context, new Date(message.createdAt())));
            }
        } else {
            createTimeTv.setText("");
        }
        //发送状态
        if (!isSenderIsSelf()) {
            sendFailTv.setVisibility(View.GONE);
            sendingPb.setVisibility(View.GONE);
        } else {
            if (message.status() == Message.MessageStatus.OFFLINE) {
                sendFailTv.setVisibility(View.VISIBLE);
                sendingPb.setVisibility(View.GONE);
            } else if (message.status() == Message.MessageStatus.SENDING) {
                sendFailTv.setVisibility(View.GONE);
                sendingPb.setVisibility(View.VISIBLE);
            } else if (message.status() == Message.MessageStatus.SENT) {
                sendFailTv.setVisibility(View.GONE);
                sendingPb.setVisibility(View.GONE);
            } else {
                sendFailTv.setVisibility(View.GONE);
                sendingPb.setVisibility(View.GONE);
            }
        }
    }

    private void initData() {
        // 将未读消息置为已读状态,并减少对应会话的未读消息数
        if (!isSenderIsSelf() && message.messageContent().type() != MessageContent.MessageContentType.AUDIO) {
            if (!message.iHaveRead()) {
                if (null != message.conversation()) {
                    message.conversation().addUnreadCount(-1);
                }
                message.read();
            }
        }
    }

    private void getUserInfoFromWuKong(final int position, final long openId) {
        IMEngine.getIMService(UserService.class).getUser(new com.alibaba.wukong.Callback<User>() {

            @Override
            public void onException(String arg0, String arg1) {

            }

            @Override
            public void onProgress(User arg0, int arg1) {

            }

            @Override
            public void onSuccess(User user) {
                if (null != kikOutMemberInfo)
                    kikOutMemberInfo.put(openId, user);
                chatMessage.setSenderName(user.nickname());
                chatMessage.setSenderUserFace(user.avatar());
                datas.set(position, chatMessage);
                if (null != recyclerChatItemAdapter) {
                    recyclerChatItemAdapter.notifyItemChanged(position);
                }
            }
        }, openId);
    }

    public void initListener() {
        userHeadIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobclickAgent.onEvent(context, "im_avartar");
                Intent intent = new Intent(context, MyHomeArchivesActivity.class);
                intent.putExtra("name", senderName);
                intent.putExtra("openId", message.senderId());
                if (conversation.type() == Conversation.ConversationType.GROUP) {
                    intent.putExtra("circleName", conversation.title());//圈名，为了在添加好友时提示“我是来自XX 圈子的XXX”
                }
                intent.putExtra("from", Constants.ADDFRIENDTYPE[7]);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        //群聊 长按头像出现@他
        userHeadIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isSenderIsSelf() && conversation.type() == Conversation.ConversationType.GROUP) {
                    chatUtils.handleAtAction(message.senderId(), senderName, false);
                    return true;
                }
                return false;
            }
        });
        contentRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onContentRlClickListener();
            }
        });
        contentRl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (message.status() == Message.MessageStatus.SENT) {
                    onContentRlLongClickListener();
                    return true;
                }
                return false;
            }
        });
        sendFailTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RenheIMUtil.showAlertDialog(context, context.getString(R.string.im_sendfail_retry), new RenheIMUtil.DialogCallback() {
                    @Override
                    public void onPositive() {
                        chatUtils.send(message, null);
                        recyclerChatItemAdapter.updateMessageItem(message);
                    }

                    @Override
                    public void onCancle() {

                    }
                });
            }
        });
    }

    public void loadImage(ImageView imageView, String imageUrl) {
        try {
            imageLoader.displayImage(imageUrl, imageView,CacheManager.circleImageOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadImage(ImageView imageView, String imageUrl, DisplayImageOptions options) {
        try {
            imageLoader.displayImage(imageUrl, imageView, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadImage(ImageView imageView, String imageUrl, DisplayImageOptions options, SimpleImageLoadingListener animateFirstDisplayListener) {
        try {
            imageLoader.displayImage(imageUrl, imageView, options, animateFirstDisplayListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断这条message是否是自己发的
     *
     * @return
     */
    public boolean isSenderIsSelf() {
        if (message.senderId() == RenheApplication.getInstance().currentOpenId) {
            return true; // 自己发送的消息
        } else {
            return false; // 别人发送的消息
        }
    }

    public abstract void onContentRlClickListener();

    public abstract void onContentRlLongClickListener();

    public RelativeLayout getContentRl() {
        return contentRl;
    }

    public void setContentRl(RelativeLayout contentRl) {
        this.contentRl = contentRl;
    }
}

