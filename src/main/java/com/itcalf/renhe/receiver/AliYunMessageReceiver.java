package com.itcalf.renhe.receiver;

import android.content.Context;

import com.alibaba.sdk.android.push.MessageReceiver;
import com.alibaba.sdk.android.push.notification.CPushMessage;
import com.itcalf.renhe.notification.PushNotification;
import com.orhanobut.logger.Logger;

import java.util.Map;

/**
 * @author: 王宁
 * @since: 16/4/15
 * @version: 1.1
 * @feature: 用于接收推送的通知和消息
 */
public class AliYunMessageReceiver extends MessageReceiver {
    /**
     * 推送通知的回调方法
     *
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    @Override
    public void onNotification(Context context, String title, String summary, Map<String, String> extraMap) {
        if (null != extraMap) {
            for (Map.Entry<String, String> entry : extraMap.entrySet()) {
                Logger.w("@Get diy param : Key=" + entry.getKey() + " , Value=" + entry.getValue());
            }
        } else {
            Logger.w("@收到通知 && 自定义消息为空");
        }
        Logger.w("收到一条推送通知 ： " + title);
    }

    /**
     * 推送消息的回调方法
     *
     * @param context
     * @param cPushMessage
     */
    @Override
    public void onMessage(Context context, CPushMessage cPushMessage) {
        try {
            Logger.w("收到一条推送消息 ： " + cPushMessage.getTitle());
        } catch (Exception e) {
            Logger.w(e.toString());
        }
        if (null != cPushMessage) {
            PushNotification.showPushNotification(context, cPushMessage);
        }
    }

    /**
     * 从通知栏打开通知的扩展处理
     *
     * @param context
     * @param title
     * @param summary
     * @param extraMap
     */
    @Override
    public void onNotificationOpened(Context context, String title, String summary, String
            extraMap) {
        Logger.w("onNotificationOpened ： " + " : " + title + " : " + summary + " : " + extraMap);
    }

    @Override
    public void onNotificationRemoved(Context context, String messageId) {
        Logger.w("onNotificationRemoved ： " + messageId);
    }
}