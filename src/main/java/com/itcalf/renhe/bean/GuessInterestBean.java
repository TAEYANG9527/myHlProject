package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.List;

/**
 * description :猜你感兴趣
 * Created by Chans Renhenet
 * 2015/11/30
 */
public class GuessInterestBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private int state;
    private List<NewFriendsInfo> interestList;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<NewFriendsInfo> getInterestList() {
        return interestList;
    }

    public void setInterestList(List<NewFriendsInfo> interestList) {
        this.interestList = interestList;
    }
}
