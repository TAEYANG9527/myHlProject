package com.itcalf.renhe.eventbusbean;

/**
 * event bus 用于传递IM 对话 event的类型
 */
public class ChangeChatTitleEvent {

    private String title;

    public ChangeChatTitleEvent(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}