package com.itcalf.renhe.eventbusbean;

/**
 * event bus 用于传递IM 对话 event的类型
 */
public class SendMsgSuccessEvent {

    private com.alibaba.wukong.im.Message message;

    public SendMsgSuccessEvent(com.alibaba.wukong.im.Message message) {
        this.message = message;
    }

    public com.alibaba.wukong.im.Message getMessage() {
        return message;
    }

    public void setMessage(com.alibaba.wukong.im.Message message) {
        this.message = message;
    }
}