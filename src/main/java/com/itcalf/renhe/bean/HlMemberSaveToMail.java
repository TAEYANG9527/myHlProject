package com.itcalf.renhe.bean;

import java.io.Serializable;

/**
 * Created by wangning on 2016/3/17.
 */
public class HlMemberSaveToMail implements Serializable {
    private HlContactRenheMember hlContactRenheMember;
    private boolean isSelect;//是否选中

    public HlContactRenheMember getHlContactRenheMember() {
        return hlContactRenheMember;
    }

    public void setHlContactRenheMember(HlContactRenheMember hlContactRenheMember) {
        this.hlContactRenheMember = hlContactRenheMember;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }
}
