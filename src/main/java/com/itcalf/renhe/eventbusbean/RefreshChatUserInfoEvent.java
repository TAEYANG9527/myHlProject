package com.itcalf.renhe.eventbusbean;

/**
 * event bus IM对话群聊做了禁言功能，是从im的user扩展字段取值的，如果用户在断网情况打开对话，然后再联网，则无法取到user，所以添加
 * 一个网络状态广播监听器，网络连接上后，发送event给chat，刷新获取user，判断禁言状态
 */
public class RefreshChatUserInfoEvent {


    public RefreshChatUserInfoEvent() {
    }

}