package com.itcalf.renhe.bean;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 联系人列表——手机通讯录联系人
 * Created by wangning on 2016/3/8.
 */
public class HlContactContactMember extends DataSupport implements Serializable {
    /**
     * 数据库主键字段ID,其中id这个字段可写可不写，因为即使不写这个字段，LitePal也会在表中自动生成一个id列，毕竟每张表都一定要有主键的嘛。
     */
    private int id;
    private String sid;    // 会员Id
    private String ownerSid;    // 本人的sid，即是登录用户的sid
    private String name;    // 姓名
    private String shortName;    // 头像简名
    private int colorIndex;    // 头像背景色
    private String mobile;    // 手机号
    private String fullPinYin;//名字全拼 比如 王宁 是wangning
    private String initial;//名字拼音首字母 比如 王宁 是w
    private String initialOfFullPinYin;//名字单字首字母组合 比如 王宁 是wn

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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
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
}
