package com.itcalf.renhe.bean;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 联系人列表——人和会员（人和好友）
 * Created by wangning on 2016/3/8.
 */
public class HlContactRenheMember extends DataSupport implements Serializable {
    /**
     * 数据库主键字段ID,其中id这个字段可写可不写，因为即使不写这个字段，LitePal也会在表中自动生成一个id列，毕竟每张表都一定要有主键的嘛。
     */
    private int id;
    private String sid;    // 会员Id
    private String ownerSid;    // 本人的sid，即是登录用户的sid
    private String name;    // 姓名
    private String mobile;    // 手机号
    private String tel; // 电话
    private String userface;    // 邮箱
    private String title;    // 职位
    private String company;    // 公司
    private int accountType;    // 会员级别
    private boolean isRealname;    // 是否实名认证
    private int imId;    // IM的用户Id
    private boolean imValid;    // 是否开启IM
    private String fullPinYin;//名字全拼 比如 王宁 是wangning
    private String initial;//名字拼音首字母 比如 王宁 是w
    private String initialOfFullPinYin;//名字单字首字母组合 比如 王宁 是wn
    private boolean isFriend;//是否人和网好友,因为有可能是通讯录好友，并且是人和网会员，但不是人和网好友

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getOwnerSid() {
        return ownerSid;
    }

    public void setOwnerSid(String ownerSid) {
        this.ownerSid = ownerSid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getUserface() {
        return userface;
    }

    public void setUserface(String userface) {
        this.userface = userface;
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

    public int getImId() {
        return imId;
    }

    public void setImId(int imId) {
        this.imId = imId;
    }

    public boolean isImValid() {
        return imValid;
    }

    public void setImValid(boolean imValid1) {
        this.imValid = imValid1;
    }

    public String getFullPinYin() {
        return fullPinYin;
    }

    public void setFullPinYin(String fullPinYin) {
        this.fullPinYin = fullPinYin;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getInitialOfFullPinYin() {
        return initialOfFullPinYin;
    }

    public void setInitialOfFullPinYin(String initialOfFullPinYin) {
        this.initialOfFullPinYin = initialOfFullPinYin;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }
}
