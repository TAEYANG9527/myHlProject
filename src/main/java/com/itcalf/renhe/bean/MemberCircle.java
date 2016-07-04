package com.itcalf.renhe.bean;

import java.io.Serializable;

/**
 * 人脉圈
 */
public class MemberCircle implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;//消息的id
    private String sid;
    private String name;
    private String curCompany;//现任公司
    private String curTitle;//现任公司
    private long createdDate;//时间
    private int accountType;//账号vip等级类型：0：普通会员；1：VIP会员；2：黄金会员；3：铂金会员
    private boolean isRealname;//是否是实名认证的会员
    private String userFace;
    private String content;//正文

    private int position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCurCompany() {
        return curCompany;
    }

    public void setCurCompany(String curCompany) {
        this.curCompany = curCompany;
    }

    public String getCurTitle() {
        return curTitle;
    }

    public void setCurTitle(String curTitle) {
        this.curTitle = curTitle;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}