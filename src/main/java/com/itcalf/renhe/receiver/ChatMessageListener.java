package com.itcalf.renhe.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.wukong.im.Conversation;
import com.alibaba.wukong.im.IMEngine;
import com.alibaba.wukong.im.Message;
import com.alibaba.wukong.im.MessageContent;
import com.alibaba.wukong.im.MessageListener;
import com.alibaba.wukong.im.User;
import com.alibaba.wukong.im.UserService;
import com.itcalf.renhe.Constants;
import com.itcalf.renhe.R;
import com.itcalf.renhe.RenheApplication;
import com.itcalf.renhe.context.MyPortal;
import com.itcalf.renhe.context.fragmentMain.TabMainFragmentActivity;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description.
 *
 * @author wangning
 */
public class ChatMessageListener implements MessageListener {
    private SharedPreferences msp, sp;
    private SharedPreferences.Editor mEditor;
    private String notifycationContent = "";
    private String result = "";
    private Context ct;
    private SharedPreferences blackListSp;

    @SuppressLint("CommitPrefEdits")
    public ChatMessageListener() {

    }

    @Override
    public void onAdded(List<Message> messagesList, DataType arg1) {
        if (null == ct)
            ct = RenheApplication.getInstance();
        if (null == msp)
            msp = RenheApplication.getInstance().getUserSharedPreferences();
        if (null == sp)
            sp = RenheApplication.getInstance().getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0);
        if (null == mEditor)
            mEditor = msp.edit();
        if (null == blackListSp)
            blackListSp = ct.getSharedPreferences(Constants.BLOCKED_CONTACTS_SHAREDPREFERENCES, 0);
        if (messagesList == null || messagesList.isEmpty())
            return;
        Logger.w("message onAdded");
        try {
            Map<String, com.alibaba.wukong.im.Message> hashMap = new HashMap<>();
            for (com.alibaba.wukong.im.Message message : messagesList) {
                if (message == null)
                    continue;
                Conversation conversation = message.conversation();
                if (message.senderId() != RenheApplication.getInstance().currentOpenId && !message.iHaveRead()) {
                    message.conversation().addUnreadCount(1);
                }
                if (msp.getBoolean(conversation.conversationId() + "msg_void_bother", false)) {
                    continue;
                }

                if (conversation.type() == Conversation.ConversationType.CHAT) {
                    if (blackListSp.getBoolean(conversation.getPeerId() + "", false)) {
                        continue;
                    }
                } else if (conversation.type() == Conversation.ConversationType.GROUP
                        && message.messageContent().type() == MessageContent.MessageContentType.TEXT) {
                    if (message.isAt()
                            && !conversation.conversationId().equals(RenheApplication.getInstance().getInChatOpenId())) {
                        conversation.updateAtMeStatus(true);
                    }
                }
                if (!hashMap.containsKey(message.conversation().conversationId())) {
                    hashMap.put(message.conversation().conversationId(), message);
                }
            }

            for (Map.Entry<String, com.alibaba.wukong.im.Message> entry : hashMap.entrySet()) {
                com.alibaba.wukong.im.Message message = entry.getValue();
                if (message.senderId() != RenheApplication.getInstance().currentOpenId && !message.iHaveRead()) {
                    showNotifyAfterGetUser(ct, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRemoved(List<Message> messages) {
    }

    @Override
    public void onChanged(List<Message> messages) {
    }

    private void showNotifyAfterGetUser(final Context context, final com.alibaba.wukong.im.Message message) {
        if (RenheApplication.getInstance().currentOpenId > 0 && sp.getBoolean("msgnotify", true)
                && !message.conversation().conversationId().equals(RenheApplication.getInstance().getInChatOpenId())) {
            final Conversation conversation = message.conversation();
            // 通知栏弹出通知
            String messageExtension = message.extension(Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_NAME + ";" + Constants.CONVERSATION_CONSTANTS.CHAT_CONVER_INFO_USERFACE);
            if (!TextUtils.isEmpty(messageExtension)) {
                String[] messageExtensionArray = messageExtension.split(";");
                if (null != messageExtensionArray && messageExtensionArray.length > 1) {
                    String senderName, senderIcon;
                    senderName = messageExtensionArray[0];
                    senderIcon = messageExtensionArray[1];
                    initNotify(context, conversation, message, senderName);
                } else {
                    IMEngine.getIMService(UserService.class).getUser(new com.alibaba.wukong.Callback<User>() {

                        @Override
                        public void onException(String arg0, String arg1) {
                            showNotify(context, "和聊", "您有一条新消息", 1, null, conversation, "您有一条新消息", message);
                        }

                        @Override
                        public void onProgress(User arg0, int arg1) {

                        }

                        @Override
                        public void onSuccess(User arg0) {
                            String senderName = arg0.nickname();
                            initNotify(context, conversation, message, senderName);
                        }

                    }, conversation.type() == Conversation.ConversationType.CHAT ? message.conversation().getPeerId()
                            : message.senderId());
                }

            } else {
                IMEngine.getIMService(UserService.class).getUser(new com.alibaba.wukong.Callback<User>() {

                    @Override
                    public void onException(String arg0, String arg1) {
                        showNotify(context, "和聊", "您有一条新消息", 1, null, conversation, "您有一条新消息", message);
                    }

                    @Override
                    public void onProgress(User arg0, int arg1) {

                    }

                    @Override
                    public void onSuccess(User arg0) {
                        String senderName = arg0.nickname();
                        initNotify(context, conversation, message, senderName);
                    }

                }, conversation.type() == Conversation.ConversationType.CHAT ? message.conversation().getPeerId()
                        : message.senderId());
            }

        }
    }

    private void initNotify(final Context context, Conversation conversation, final com.alibaba.wukong.im.Message message, String senderName) {
        if (message.messageContent().type() == MessageContent.MessageContentType.TEXT) {
            notifycationContent = ((MessageContent.TextContent) message.messageContent()).text();
        } else if (message.messageContent().type() == MessageContent.MessageContentType.IMAGE) {
            notifycationContent = senderName + "发送了一张图片";
        } else if (message.messageContent().type() == MessageContent.MessageContentType.AUDIO) {
            notifycationContent = senderName + "发送了一段语音";
        } else if (message.messageContent().type() == MessageContent.MessageContentType.LINKED) {
            /**** 对文本信息进行处理，支持表情显示 ***/
            String forwardLink = ((MessageContent.LinkedContent) message.messageContent()).url();
            if (!TextUtils.isEmpty(forwardLink)) {
                if (forwardLink.startsWith("msg")) {
                    notifycationContent = senderName + "分享了一条人脉圈";
                } else if (forwardLink.startsWith("http")) {
                    notifycationContent = senderName + "分享了一个链接";
                } else if (forwardLink.startsWith("user")) {
                    notifycationContent = senderName + "分享了一个联系人";
                } else if (forwardLink.startsWith("group")) {
                    notifycationContent = senderName + "分享了一个圈子";
                }
            } else {
                notifycationContent = senderName + "分享了一个链接";
            }
        } else if (message.messageContent().type() == MessageContent.MessageContentType.FILE) {
            notifycationContent = senderName + "发送了一个文件";
        } else {
            notifycationContent = "您有一条新消息";
        }
        String notificationTitle = senderName;
        int openId = conversation.type() == Conversation.ConversationType.CHAT ? (int) conversation.getPeerId()
                : Integer.parseInt(conversation.conversationId());
        if (conversation.type() == Conversation.ConversationType.GROUP) {
            notificationTitle = conversation.title();
            if (conversation.hasUnreadAtMeMessage() && notifycationContent.contains("@" + RenheApplication.getInstance().currentNickName)) {
                notifycationContent = "[有人@我] " + senderName + ":" + notifycationContent;
            } else {
                if (message.creatorType() != Message.CreatorType.SYSTEM
                        && message.messageContent().type() == MessageContent.MessageContentType.TEXT) {
                    notifycationContent = senderName + ":" + notifycationContent;
                }
            }
        }
        String tiker = "";
        if (conversation.type() == Conversation.ConversationType.GROUP) {
            tiker = "[" + conversation.title() + "]" + notifycationContent;
        } else if (conversation.type() == Conversation.ConversationType.CHAT) {
            switch (message.messageContent().type()) {
                case MessageContent.MessageContentType.LINKED:
                case MessageContent.MessageContentType.IMAGE:
                case MessageContent.MessageContentType.AUDIO:
                case MessageContent.MessageContentType.FILE:
                    break;
                default:
                    tiker = senderName + ":" + notifycationContent;
                    break;
            }
        } else {
            tiker = notifycationContent;
        }
        showNotify(context, notificationTitle, notifycationContent, openId, null, conversation, tiker, message);
    }

    /**
     * 聊天内容，notification通知
     *
     * @param context
     * @param title
     * @param content
     * @param imageBitmap
     */
    void showNotify(Context context, String title, String content, int mnotifyId, Bitmap imageBitmap, Conversation conversation,
                    String tiker, Message message) {
        mEditor = msp.edit();
        String mTiker = tiker;
        if (TextUtils.isEmpty(mTiker)) {
            if (conversation.type() == Conversation.ConversationType.CHAT) {
                switch (message.messageContent().type()) {
                    case MessageContent.MessageContentType.LINKED:
                    case MessageContent.MessageContentType.IMAGE:
                    case MessageContent.MessageContentType.AUDIO:
                    case MessageContent.MessageContentType.FILE:
                        mTiker = content;
                        break;
                    default:
                        mTiker = title + ":" + content;
                        break;
                }
            } else {
                mTiker = content;
            }
        }
        int notifyId = msp.getInt(mnotifyId + "", 0);
        int notifyNum = msp.getInt(mnotifyId + "num", 0);
        content = "[" + (conversation.unreadMessageCount() + 1) + "条]" + content;
        mEditor = msp.edit();
        mEditor.putInt(mnotifyId + "", notifyId);
        mEditor.putInt(mnotifyId + "num", notifyNum + 1);
        mEditor.commit();

        //针对群消息免打扰
        if (conversation.type() == Conversation.ConversationType.GROUP
                && msp.getBoolean(conversation.conversationId() + "msg_void_bother", false)) {
            return;
        }

        RenheApplication application = RenheApplication.getInstance();
        Intent newIntent;
        if (application.getLogin() != 1) {
            newIntent = new Intent(context, MyPortal.class);
            newIntent.putExtra("conversation", conversation);
        } else {
            newIntent = new Intent(context, TabMainFragmentActivity.class);
            newIntent.putExtra("notifyconversation", conversation);
            newIntent.putExtra("notifyId", mnotifyId + "");

        }
        newIntent.putExtra("fromNotify", true);
        PendingIntent contentReceiver = PendingIntent.getActivity(context, mnotifyId, newIntent, PendingIntent.FLAG_ONE_SHOT);

        if (Build.VERSION.SDK_INT < 21) {
            Notification.Builder mNotificationBuilder = new Notification.Builder(application).setTicker(mTiker).setContentTitle(title)
                    .setContentText(content).setContentIntent(contentReceiver).setSmallIcon(R.drawable.logo_48x48).setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_96x96))
                    .setWhen(System.currentTimeMillis());
//		if (imageBitmap == null) {
//			mNotificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon));
//		} else {
//			mNotificationBuilder.setLargeIcon(imageBitmap);
//		}

            if (null != mNotificationBuilder) {
                Notification n = mNotificationBuilder.getNotification();
                n.flags |= Notification.FLAG_AUTO_CANCEL;
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("sound", true)) {
                    n.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bibi);
                }
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("led", true)) {
                    n.defaults |= Notification.DEFAULT_LIGHTS;
                }
                if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                        .getBoolean("warnshake", true)) {
                    n.defaults |= Notification.DEFAULT_VIBRATE;
                }
                //				n.setLatestEventInfo(context, title, content, contentReceiver);

                NotificationManager mNotificationManager = (NotificationManager) context
                        .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(mnotifyId, n);// 通知一下才会生效
            }
        } else {
            NotificationManager mNotificationManager = (NotificationManager) application.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(application);
            mNotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_96x96))
                    .setSmallIcon(R.drawable.logo_48x48)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(contentReceiver)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true);
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("sound", true)) {
                mNotificationBuilder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bibi));
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("led", true)) {
                mNotificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
            }
            if (context.getSharedPreferences(RenheApplication.getInstance().getUserInfo().getSid() + "setting_info", 0)
                    .getBoolean("warnshake", true)) {
                mNotificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
            mNotificationManager.notify(mnotifyId, mNotificationBuilder.build());
        }
    }

    public class AnimateFirstDisplayListener extends SimpleImageLoadingListener {
        private String title;
        private String content;
        private int openId;
        private Context context;
        private Conversation conversation;
        private String tiker;
        private Message message;

        public AnimateFirstDisplayListener(Context context, String title, String content, int openId, Conversation conversation,
                                           String tiker, Message message) {
            this.title = title;
            this.content = content;
            this.openId = openId;
            this.context = context;
            this.conversation = conversation;
            this.tiker = tiker;
            this.message = message;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            showNotify(context, title, content, openId, loadedImage, conversation, tiker, message);
        }
    }
}
