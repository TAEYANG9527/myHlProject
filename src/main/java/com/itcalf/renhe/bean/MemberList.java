package com.itcalf.renhe.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 人脉
 */
public class MemberList implements Serializable {
    private static final long serialVersionUID = 7527964386843338509L;
    private String sid;
    private String name;
    private String curTitle;//当前职位
    private String[] exTitle;//历史职位
    private String curCompany;//现任公司
    private String[] exCompany;
    private String schoolName;//学校
    private String curIndustry;//行业
    private String location;//所在地
    private int accountType;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
    private boolean isRealname;//是否是实名认证的会员
    private String userFace;
    private ArrayList<String> preferred;//我能提供

    /**
     * 当前人脉所在的搜索位置
     **/
    private int position;
    private boolean isConnection;// boolean 说明：是否是朋友
    private boolean isInvite;// boolean 说明：非好友的情况下10天内是否已经发出过加好友的邀请
    private BeInvitedInfo beInvitedInfo;
    private UserInfo userInfo;
    private boolean isReceived;//仅仅是用在列表页接收好友请求时，按钮状态从“接收”变为“已添加”
    public MemberList() {

    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurTitle() {
        return curTitle;
    }

    public void setCurTitle(String curTitle) {
        this.curTitle = curTitle;
    }

    public String getCurCompany() {
        return curCompany;
    }

    public void setCurCompany(String curCompany) {
        this.curCompany = curCompany;
    }

    public String[] getExTitle() {
        return exTitle;
    }

    public void setExTitle(String[] exTitle) {
        this.exTitle = exTitle;
    }

    public String[] getExCompany() {
        return exCompany;
    }

    public void setExCompany(String[] exCompany) {
        this.exCompany = exCompany;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getCurIndustry() {
        return curIndustry;
    }

    public void setCurIndustry(String curIndustry) {
        this.curIndustry = curIndustry;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public boolean isRealname() {
        return isRealname;
    }

    public void setRealname(boolean isRealname) {
        this.isRealname = isRealname;
    }

    public String getUserFace() {
        return userFace;
    }

    public void setUserFace(String userFace) {
        this.userFace = userFace;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ArrayList<String> getPreferred() {
        return preferred;
    }

    public void setPreferred(ArrayList<String> preferred) {
        this.preferred = preferred;
    }

    public boolean isConnection() {
        return isConnection;
    }

    public void setIsConnection(boolean isConnection) {
        this.isConnection = isConnection;
    }

    public boolean isInvite() {
        return isInvite;
    }

    public void setIsInvite(boolean isInvite) {
        this.isInvite = isInvite;
    }

    public BeInvitedInfo getBeInvitedInfo() {
        return beInvitedInfo;
    }

    public void setBeInvitedInfo(BeInvitedInfo beInvitedInfo) {
        this.beInvitedInfo = beInvitedInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setIsReceived(boolean isReceived) {
        this.isReceived = isReceived;
    }

    /**
     * 用于接受好友请求的bean类
     */
    public class BeInvitedInfo {
        boolean isBeInvited;// boolean 是否是已被邀请状态，若为true则app端需要显示“接收邀请”按钮，若为false则忽略
        int inviteId;// int 邀请id，用于接收邀请接口调用
        int inviteType;// int 邀请类型，用于接收邀请接口调用

        public boolean isBeInvited() {
            return isBeInvited;
        }

        public void setIsBeInvited(boolean isBeInvited) {
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

    public class UserInfo {
        private ContactInfo contactInfo;

        public ContactInfo getContactInfo() {
            return contactInfo;
        }

        public void setContactInfo(ContactInfo contactInfo) {
            this.contactInfo = contactInfo;
        }
    }

    public class ContactInfo {
        private int imId;

        public int getImId() {
            return imId;
        }

        public void setImId(int imId) {
            this.imId = imId;
        }
    }
}
