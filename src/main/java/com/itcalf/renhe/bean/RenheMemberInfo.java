package com.itcalf.renhe.bean;

import java.io.Serializable;

/**
 * 会员的人和网信息
 **/
public class RenheMemberInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String memberSId;
    private boolean isSelf;
    private boolean isConnection;
    private boolean isInvite;
    private String title;
    private String company;
    private String userface;
    private InvitedInfo beInvitedInfo;

    public String getMemberSId() {
        return memberSId;
    }

    public void setMemberSId(String memberSId) {
        this.memberSId = memberSId;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public boolean isConnection() {
        return isConnection;
    }

    public void setConnection(boolean isConnection) {
        this.isConnection = isConnection;
    }

    public boolean isInvite() {
        return isInvite;
    }

    public void setInvite(boolean isInvite) {
        this.isInvite = isInvite;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUserface() {
        return userface;
    }

    public void setUserface(String userface) {
        this.userface = userface;
    }

    public InvitedInfo getBeInvitedInfo() {
        return beInvitedInfo;
    }

    public void setBeInvitedInfo(InvitedInfo beInvitedInfo) {
        this.beInvitedInfo = beInvitedInfo;
    }

    /**
     * 被邀请信息
     **/
    public static class InvitedInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean isBeInvited;
        private int inviteId;
        private int inviteType;

        public boolean isBeInvited() {
            return isBeInvited;
        }

        public void setBeInvited(boolean isBeInvited) {
            this.isBeInvited = isBeInvited;
        }

        public int getInviteId() {
            return inviteId;
        }

        public void setInviteId(int inviteId) {
            this.inviteId = inviteId;
        }

        public int getInviteType() {
            return inviteType;
        }

        public void setInviteType(int inviteType) {
            this.inviteType = inviteType;
        }
    }
}