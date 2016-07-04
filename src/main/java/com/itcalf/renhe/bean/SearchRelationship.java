package com.itcalf.renhe.bean;

import java.io.Serializable;

/**
 * @author Chan
 * @description 人脉高级搜索接口数据模型
 * @date 2015-4-22
 */
public class SearchRelationship implements Serializable {
    private static final long serialVersionUID = -7616454345512833252L;
    private int state; // 说明：1 请求成功；-1 权限不足; -2发生未知错误;
    private int limit;//限额
    private MemberList[] memberList;


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public MemberList[] getMemberList() {
        return memberList;
    }

    public void setMemberList(MemberList[] memberList) {
        this.memberList = memberList;
    }


}
