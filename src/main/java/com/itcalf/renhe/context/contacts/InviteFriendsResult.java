package com.itcalf.renhe.context.contacts;

/**
 * description :多选邀请返回
 * Created by Chans Renhenet
 * 2015/10/20
 */
public class InviteFriendsResult {
    private int state;
    private int num;
    private String errorInfo;

    public InviteFriendsResult() {
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
