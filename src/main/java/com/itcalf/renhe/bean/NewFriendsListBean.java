package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.List;

public class NewFriendsListBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private int state;
    private List<NewFriendsInfo> newFriendList;
    private boolean cardFlag;//说明名片要不要显示小红点

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<NewFriendsInfo> getNewFriendList() {
        return newFriendList;
    }

    public void setNewFriendList(List<NewFriendsInfo> newFriendList) {
        this.newFriendList = newFriendList;
    }

    public boolean isFlag() {
        return cardFlag;
    }

    public void setFlag(boolean flag) {
        this.cardFlag = flag;
    }
}
