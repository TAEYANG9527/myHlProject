package com.itcalf.renhe.bean;

import java.io.Serializable;

/**
 * Created by wangning on 2016/3/8.
 */
public class HlContacts implements Serializable {
    public static final int HLCONTACTS_RENHE_MEMBER_TYPE = 1;
    public static final int HLCONTACTS_CONTACT_MEMBER_TYPE = 2;
    public static final int HLCONTACTS_CARD_MEMBER_TYPE = 3;
    private int type;//1:人和好友 2:通讯录好友 3:名片联系人
    private HlContactRenheMember hlContactRenheMember;
    private HlContactContactMember hlContactContactMember;
    private HlContactCardMember hlContactCardMember;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HlContactRenheMember getHlContactRenheMember() {
        return hlContactRenheMember;
    }

    public void setHlContactRenheMember(HlContactRenheMember hlContactRenheMember) {
        this.hlContactRenheMember = hlContactRenheMember;
    }

    public HlContactContactMember getHlContactContactMember() {
        return hlContactContactMember;
    }

    public void setHlContactContactMember(HlContactContactMember hlContactContactMember) {
        this.hlContactContactMember = hlContactContactMember;
    }

    public HlContactCardMember getHlContactCardMember() {
        return hlContactCardMember;
    }

    public void setHlContactCardMember(HlContactCardMember hlContactCardMember) {
        this.hlContactCardMember = hlContactCardMember;
    }
}
