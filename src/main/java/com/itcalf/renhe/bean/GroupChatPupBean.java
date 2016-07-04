package com.itcalf.renhe.bean;

/**
 * Created by wangning on 2016/1/12.
 */
public class GroupChatPupBean {
    private String title;
    private int icon;
    private int unreadNum;
    private String destinationUrl;
    public GroupChatPupBean(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public GroupChatPupBean(String title, int icon, int unreadNum, String destinationUrl) {
        this.title = title;
        this.icon = icon;
        this.unreadNum = unreadNum;
        this.destinationUrl = destinationUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(int unreadNum) {
        this.unreadNum = unreadNum;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }
}