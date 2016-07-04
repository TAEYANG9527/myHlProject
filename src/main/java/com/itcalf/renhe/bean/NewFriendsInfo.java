package com.itcalf.renhe.bean;

import java.io.Serializable;

public class NewFriendsInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private boolean readState;
    private int status;//0接受；1已添加；2拒绝3，等待验证4.添加
    private int inviteType;
    private int inviteId;
    private String purpose;
    private String fromContent;
    /**
     * fromType int 说明：0代表找人脉，1代表来源于网站；2代表来源于人脉搜索；
     * 3代表来源于档案二维码扫描；4代表来源于名片扫描；5代表来源于附近的人脉；
     * 6代表来源于人脉推荐；7代表来源于圈子；8代表来源于邮箱通讯录；9代表来源于手机通讯录
     */
    private int fromType;
    public NFuserInfo userInfo;
    private int redId;//说明:红包id，0为没有红包
    public NewFriendsInfo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isReadState() {
        return readState;
    }

    public void setReadState(boolean readState) {
        this.readState = readState;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getInviteType() {
        return inviteType;
    }

    public void setInviteType(int inviteType) {
        this.inviteType = inviteType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFromContent() {
        return fromContent;
    }

    public void setFromContent(String fromContent) {
        this.fromContent = fromContent;
    }

    /**
     * fromType int 说明：0代表找人脉，1代表来源于网站；2代表来源于人脉搜索；3代表来源于档案二维码扫描；4代表来源于名片扫描；5代表来源于附近的人脉；6代表来源于人脉推荐；7代表来源于圈子；8代表来源于邮箱通讯录；9代表来源于手机通讯录
     **/
    public int getFromType() {
        return fromType;
    }

    /**
     * fromType int 说明：0代表找人脉，1代表来源于网站；2代表来源于人脉搜索；3代表来源于档案二维码扫描；4代表来源于名片扫描；5代表来源于附近的人脉；6代表来源于人脉推荐；7代表来源于圈子；8代表来源于邮箱通讯录；9代表来源于手机通讯录
     **/
    public void setFromType(int fromType) {
        this.fromType = fromType;
    }

    public NFuserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(NFuserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public int getInviteId() {
        return inviteId;
    }

    public void setInviteId(int inviteId) {
        this.inviteId = inviteId;
    }

    public int getRedId() {
        return redId;
    }

    public void setRedId(int redId) {
        this.redId = redId;
    }
}
