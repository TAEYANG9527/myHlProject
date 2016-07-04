package com.itcalf.renhe.bean;

import com.itcalf.renhe.dto.ContactList;

/**
 * description :手机，名片联系人查看
 * Created by Chans Renhenet
 * 2015/12/2
 */
public class MobileCardContact {
    private int state;
    private ContactList.Member userInfo;

    public MobileCardContact() {
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ContactList.Member getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(ContactList.Member userInfo) {
        this.userInfo = userInfo;
    }
}
