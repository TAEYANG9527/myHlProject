package com.itcalf.renhe.bean;

public class TabHasNewBean {
    private int state;
    /**
     * 人脉圈动态
     */
    private int hasNew;
    /**
     * 新的未读消息number
     */
    private int notifyCount;
    /**
     * 最后一条提醒发送者的头像
     */
    private String senderUserface;

    /**
     * 资料完善的程度的状态
     */
    private boolean isPerfectInfo;
    private int newfriendcount;//最近新的好友未读提醒

    public TabHasNewBean() {

    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getNotifyCount() {
        return notifyCount;
    }

    public void setNotifyCount(int notifyCount) {
        this.notifyCount = notifyCount;
    }

    public String getSenderUserface() {
        return senderUserface;
    }

    public void setSenderUserface(String senderUserface) {
        this.senderUserface = senderUserface;
    }

    public void setHasNew(int hasNew) {
        this.hasNew = hasNew;
    }

    public int getHasNew() {
        return hasNew;
    }

    public int getNewfriendcount() {
        return newfriendcount;
    }

    public void setNewfriendcount(int newfriendcount) {
        this.newfriendcount = newfriendcount;
    }

    public void setIsPerfectInfo(boolean isPerfectInfo) {
        this.isPerfectInfo = isPerfectInfo;
    }

    public boolean getIsPerfectInfo() {
        return isPerfectInfo;
    }

}
