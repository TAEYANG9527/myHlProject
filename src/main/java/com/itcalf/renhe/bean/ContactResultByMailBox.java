package com.itcalf.renhe.bean;

import java.util.List;

public class ContactResultByMailBox {

    private int state;
    /**
     * 人和网好友数据
     */
    private List<MailBoxContact> friendlist;
    /**
     * 人和网会员但不是好友数据
     */
    private List<MailBoxContact> nofriendlist;
    /**
     * 不是人和网会员数据
     */
    private List<MailBoxContact> nomemberlist;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<MailBoxContact> getFriendlist() {
        return friendlist;
    }

    public void setFriendlist(List<MailBoxContact> friendlist) {
        this.friendlist = friendlist;
    }

    public List<MailBoxContact> getNofriendlist() {
        return nofriendlist;
    }

    public void setNofriendlist(List<MailBoxContact> nofriendlist) {
        this.nofriendlist = nofriendlist;
    }

    public List<MailBoxContact> getNomemberlist() {
        return nomemberlist;
    }

    public void setNomemberlist(List<MailBoxContact> nomemberlist) {
        this.nomemberlist = nomemberlist;
    }

    public static class MailBoxContact {

        private int select;//是否选中
        private boolean isInvited;//是否已邀请
        private String name;
        private String shortName;
        private int colorIndex;
        private int contactId;
        private String email;
        private boolean isRenheMember;
        private RenheMemberInfo renheMemberInfo;

        public int getSelect() {
            return select;
        }

        public void setSelect(int select) {
            this.select = select;
        }

        public boolean isInvited() {
            return isInvited;
        }

        public void setIsInvited(boolean isInvited) {
            this.isInvited = isInvited;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public int getContactId() {
            return contactId;
        }

        public void setContactId(int contactId) {
            this.contactId = contactId;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isRenheMember() {
            return isRenheMember;
        }

        public void setRenheMember(boolean isRenheMember) {
            this.isRenheMember = isRenheMember;
        }

        public RenheMemberInfo getRenheMemberInfo() {
            return renheMemberInfo;
        }

        public void setRenheMemberInfo(RenheMemberInfo renheMemberInfo) {
            this.renheMemberInfo = renheMemberInfo;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public void setColorIndex(int colorIndex) {
            this.colorIndex = colorIndex;
        }
    }
}
